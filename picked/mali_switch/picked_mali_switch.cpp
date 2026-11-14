/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 *
 * picked_mali_switch: the root helper behind Settings' GPU driver switch.
 *
 * Kernels with FlopKernel's Mali KMD selector carry the token
 * "mali.version=rXXpY" in the kernel image; it selects which Mali KMD module
 * binds to the GPU at boot (the kernel reports it as ro.boot.mali_version).
 * Like FloppyCompanion, this patches those five characters in place in the
 * boot partition; the kernel image is uncompressed and the token keeps its
 * length, so nothing else in the image moves.
 *
 * Settings sets sys.picked.mali.request=<version>:<nonce>; init starts this
 * helper, which reports in sys.picked.mali.status:
 *   ok:<version>         patched, takes effect after a reboot
 *   unchanged:<version>  the boot image already selects it
 *   error:<reason>       nothing was written (or the write failed)
 */

#include <ctype.h>
#include <errno.h>
#include <fcntl.h>
#include <limits.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <algorithm>
#include <string>
#include <vector>

#include <android-base/file.h>
#include <android-base/logging.h>
#include <android-base/properties.h>
#include <android-base/unique_fd.h>

using android::base::GetProperty;
using android::base::SetProperty;
using android::base::unique_fd;

namespace {

constexpr char kBootLink[] = "/dev/block/by-name/boot";
constexpr char kRequestProp[] = "sys.picked.mali.request";
constexpr char kStatusProp[] = "sys.picked.mali.status";
constexpr char kKey[] = "mali.version=";  // followed by "rNNpN\0"
constexpr size_t kKeyLen = sizeof(kKey) - 1;
constexpr size_t kVersionLen = 5;
constexpr size_t kTokenLen = kKeyLen + kVersionLen + 1;  // with the NUL
constexpr size_t kChunk = 1 << 20;

// The KMDs the kernel builds (drivers/gpu/arm, CONFIG_MALI_MIDGARD_R*).
const char* const kVersions[] = {"r38p0", "r38p1", "r44p1", "r49p1", "r54p1", "r54p3"};

bool isKnownVersion(const std::string& v) {
    for (const char* known : kVersions) {
        if (v == known) return true;
    }
    return false;
}

// "rNNpN" followed by a NUL: the token's value, not mali_version_selector's
// bare "mali.version=" __setup string.
bool isVersionAt(const char* p) {
    return p[0] == 'r' && isdigit(static_cast<unsigned char>(p[1])) &&
           isdigit(static_cast<unsigned char>(p[2])) && p[3] == 'p' &&
           isdigit(static_cast<unsigned char>(p[4])) && p[5] == '\0';
}

int finish(const std::string& status) {
    LOG(INFO) << status;
    SetProperty(kStatusProp, status);
    return status.rfind("error:", 0) == 0 ? 1 : 0;
}

}  // namespace

int main() {
    android::base::InitLogging(nullptr, android::base::LogdLogger(android::base::SYSTEM));

    // <version>:<nonce>; the nonce only makes repeated requests trigger init.
    std::string request = GetProperty(kRequestProp, "");
    std::string target = request.substr(0, request.find(':'));
    if (!isKnownVersion(target)) return finish("error:unknown driver " + target);

    // Only kernels with the selector report a version; never patch others.
    std::string active = GetProperty("ro.boot.mali_version", "");
    if (active.empty()) return finish("error:kernel has no Mali driver selector");

    char real[PATH_MAX];
    if (!realpath(kBootLink, real)) return finish("error:no boot partition");

    unique_fd fd(TEMP_FAILURE_RETRY(open(real, O_RDWR | O_CLOEXEC)));
    if (fd < 0) return finish(std::string("error:open boot: ") + strerror(errno));

    off_t size = lseek(fd, 0, SEEK_END);
    if (size <= 0) return finish("error:boot partition size");

    // Find every "mali.version=rNNpN\0"; chunks overlap by a token so none is split.
    std::vector<off_t> found;
    std::vector<char> buf(kChunk + kTokenLen);
    for (off_t pos = 0; pos < size; pos += kChunk) {
        size_t want = static_cast<size_t>(std::min<off_t>(kChunk + kTokenLen, size - pos));
        if (!android::base::ReadFullyAtOffset(fd, buf.data(), want, pos)) {
            return finish(std::string("error:read boot: ") + strerror(errno));
        }
        if (want < kTokenLen) break;
        size_t limit = std::min(want - kTokenLen + 1, kChunk);
        for (size_t i = 0; i < limit; i++) {
            if (buf[i] == 'm' && memcmp(&buf[i], kKey, kKeyLen) == 0 &&
                isVersionAt(&buf[i + kKeyLen])) {
                found.push_back(pos + static_cast<off_t>(i));
            }
        }
    }
    if (found.size() != 1) {
        return finish("error:found " + std::to_string(found.size()) +
                      " driver tokens in the boot image, expected 1");
    }
    off_t at = found[0] + static_cast<off_t>(kKeyLen);

    char current[kVersionLen + 1] = {};
    if (!android::base::ReadFullyAtOffset(fd, current, kVersionLen, at)) {
        return finish("error:read token");
    }
    if (target == current) return finish("unchanged:" + target);

    if (!android::base::WriteFullyAtOffset(fd, target.data(), kVersionLen, at) ||
        fsync(fd) != 0) {
        return finish(std::string("error:write boot: ") + strerror(errno));
    }

    char check[kVersionLen + 1] = {};
    if (!android::base::ReadFullyAtOffset(fd, check, kVersionLen, at) || target != check) {
        // Put the old value back rather than leave something unexpected.
        android::base::WriteFullyAtOffset(fd, current, kVersionLen, at);
        fsync(fd);
        return finish("error:verify failed, restored " + std::string(current));
    }
    LOG(INFO) << "mali.version " << current << " -> " << target << " at offset " << at;
    return finish("ok:" + target);
}
