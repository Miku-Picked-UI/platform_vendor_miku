/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.edgelighting;

import com.penguin.laboratory.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.io.IOException;

/** Built-in shapes, rendered in the chosen colour into images SystemUI can load like cutouts. */
final class Stickers {

    static final String[] NAMES = {"heart", "star", "sparkle", "drop", "flower", "bubble"};
    static final int[] DRAWABLES = {
            R.drawable.sticker_heart, R.drawable.sticker_star, R.drawable.sticker_sparkle,
            R.drawable.sticker_drop, R.drawable.sticker_flower, R.drawable.sticker_bubble};

    private static final int SIZE_PX = 256;

    private Stickers() {}

    static String id(String name) {
        return "sticker-" + name;
    }

    static boolean isSticker(String id) {
        return id != null && id.startsWith("sticker-");
    }

    /** Renders sticker {@code index} in {@code color} and selects it. */
    static void select(Context context, int index, int color) throws IOException {
        Cutouts.save(context, render(context, DRAWABLES[index], color), id(NAMES[index]));
        EdgeLightingStore.put(context, EdgeLightingStore.KEY_SELECTED, id(NAMES[index]));
    }

    /** Re-renders the selected sticker after the colour changed. */
    static void recolor(Context context, int color) throws IOException {
        String selected = EdgeLightingStore.selected(context);
        if (!isSticker(selected)) return;
        for (int i = 0; i < NAMES.length; i++) {
            if (id(NAMES[i]).equals(selected)) select(context, i, color);
        }
    }

    static Bitmap render(Context context, int drawableRes, int color) {
        Drawable d = context.getDrawable(drawableRes).mutate();
        d.setTint(color);
        Bitmap bitmap = Bitmap.createBitmap(SIZE_PX, SIZE_PX, Bitmap.Config.ARGB_8888);
        d.setBounds(0, 0, SIZE_PX, SIZE_PX);
        d.draw(new Canvas(bitmap));
        return bitmap;
    }
}
