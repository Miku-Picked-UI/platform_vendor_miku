/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.speakercal;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

/** Material You colours from the wallpaper palette (Android 12+), with a fixed fallback. */
final class Palette {

    final boolean night;
    int bg, card, field, onSurface, onSurfaceVar, outline;
    int primary, onPrimary, primaryContainer, onPrimaryContainer;
    int secondaryContainer, onSecondaryContainer;
    int measured, error;

    Palette(Context c) {
        night = (c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (night) {
                bg = c.getColor(android.R.color.system_neutral1_900);
                card = blend(bg, c.getColor(android.R.color.system_neutral2_800), 0.55f);
                field = c.getColor(android.R.color.system_neutral2_800);
                onSurface = c.getColor(android.R.color.system_neutral1_100);
                onSurfaceVar = c.getColor(android.R.color.system_neutral2_200);
                outline = c.getColor(android.R.color.system_neutral2_500);
                primary = c.getColor(android.R.color.system_accent1_200);
                onPrimary = c.getColor(android.R.color.system_accent1_800);
                primaryContainer = c.getColor(android.R.color.system_accent1_700);
                onPrimaryContainer = c.getColor(android.R.color.system_accent1_100);
                secondaryContainer = c.getColor(android.R.color.system_accent2_700);
                onSecondaryContainer = c.getColor(android.R.color.system_accent2_100);
            } else {
                bg = c.getColor(android.R.color.system_neutral1_10);
                card = blend(bg, c.getColor(android.R.color.system_neutral2_100), 0.6f);
                field = c.getColor(android.R.color.system_neutral2_100);
                onSurface = c.getColor(android.R.color.system_neutral1_900);
                onSurfaceVar = c.getColor(android.R.color.system_neutral2_700);
                outline = c.getColor(android.R.color.system_neutral2_400);
                primary = c.getColor(android.R.color.system_accent1_600);
                onPrimary = c.getColor(android.R.color.system_accent1_0);
                primaryContainer = c.getColor(android.R.color.system_accent1_100);
                onPrimaryContainer = c.getColor(android.R.color.system_accent1_900);
                secondaryContainer = c.getColor(android.R.color.system_accent2_100);
                onSecondaryContainer = c.getColor(android.R.color.system_accent2_900);
            }
        } else if (night) {
            bg = 0xff121318; card = 0xff1e1f25; field = 0xff2a2b33;
            onSurface = 0xffe3e2e9; onSurfaceVar = 0xffc5c6d0; outline = 0xff8f909a;
            primary = 0xffb6c4ff; onPrimary = 0xff1d2d61; primaryContainer = 0xff344479;
            onPrimaryContainer = 0xffdce1ff; secondaryContainer = 0xff414659;
            onSecondaryContainer = 0xffdde1f9;
        } else {
            bg = 0xfffbf8ff; card = 0xfff0eff7; field = 0xffe3e2ec;
            onSurface = 0xff1a1b21; onSurfaceVar = 0xff45464f; outline = 0xff8f909a;
            primary = 0xff3d5afe; onPrimary = 0xffffffff; primaryContainer = 0xffdce1ff;
            onPrimaryContainer = 0xff00164e; secondaryContainer = 0xffdde1f9;
            onSecondaryContainer = 0xff161b2c;
        }
        measured = night ? 0xffffb4a9 : 0xffd0453a;
        error = night ? 0xffffb4ab : 0xffba1a1a;
    }

    static int alpha(int color, int a) {
        return (color & 0x00ffffff) | (a << 24);
    }

    static int blend(int a, int b, float t) {
        final int r = (int) (((a >> 16) & 0xff) * (1 - t) + ((b >> 16) & 0xff) * t);
        final int g = (int) (((a >> 8) & 0xff) * (1 - t) + ((b >> 8) & 0xff) * t);
        final int bl = (int) ((a & 0xff) * (1 - t) + (b & 0xff) * t);
        return 0xff000000 | (r << 16) | (g << 8) | bl;
    }
}
