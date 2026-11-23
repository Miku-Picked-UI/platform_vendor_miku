/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.wonderland;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.util.Size;

import java.io.IOException;

final class Images {

    private Images() {}

    /** Decodes {@code uri} upright, its longest side no more than {@code maxPx}. */
    static Bitmap decode(Context context, Uri uri, int maxPx) throws IOException {
        return ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.getContentResolver(), uri),
                (decoder, info, src) -> {
                    Size size = info.getSize();
                    int longest = Math.max(size.getWidth(), size.getHeight());
                    if (longest > maxPx) {
                        float scale = (float) maxPx / longest;
                        decoder.setTargetSize(Math.round(size.getWidth() * scale),
                                Math.round(size.getHeight() * scale));
                    }
                    decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                });
    }
}
