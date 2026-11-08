# SPDX-FileCopyrightText: 2026 Miku-Picked UI
# SPDX-License-Identifier: Apache-2.0
#
# Selectable emoji styles (Settings > Miku settings > Picked features > Style > Emoji).
# SystemFonts loads /product/etc/fonts_customization_emoji_<style>.xml for
# persist.sys.ax_emoji_style=<style>; "android" (default) keeps Noto Color Emoji.
# Only freely licensed emoji sets are shipped, see LICENSES.md.

EMOJI_PATH := vendor/miku/emoji

PRODUCT_COPY_FILES += \
    $(EMOJI_PATH)/fonts/BlobmojiPicked.ttf:$(TARGET_COPY_OUT_PRODUCT)/fonts/BlobmojiPicked.ttf \
    $(EMOJI_PATH)/fonts/OpenMojiPicked.ttf:$(TARGET_COPY_OUT_PRODUCT)/fonts/OpenMojiPicked.ttf \
    $(EMOJI_PATH)/fonts/TwemojiPicked.ttf:$(TARGET_COPY_OUT_PRODUCT)/fonts/TwemojiPicked.ttf \
    $(EMOJI_PATH)/fonts_customization_emoji_blobmoji.xml:$(TARGET_COPY_OUT_PRODUCT)/etc/fonts_customization_emoji_blobmoji.xml \
    $(EMOJI_PATH)/fonts_customization_emoji_openmoji.xml:$(TARGET_COPY_OUT_PRODUCT)/etc/fonts_customization_emoji_openmoji.xml \
    $(EMOJI_PATH)/fonts_customization_emoji_twemoji.xml:$(TARGET_COPY_OUT_PRODUCT)/etc/fonts_customization_emoji_twemoji.xml


# Optional extra emoji styles from a repo of your own (not part of Miku-Picked UI), synced
# to vendor/emoji-extra by manifesto/local_manifests/emoji-extra.xml. It provides its own
# emoji.mk that copies fonts_customization_emoji_<style>.xml and its font to /product.
-include vendor/emoji-extra/emoji.mk
