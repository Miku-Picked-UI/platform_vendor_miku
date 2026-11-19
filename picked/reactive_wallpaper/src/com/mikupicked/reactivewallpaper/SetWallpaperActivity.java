/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.reactivewallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Opens the system live-wallpaper preview for this wallpaper (used by Settings, Picked features).
 *
 * The preview needs EXTRA_LIVE_WALLPAPER_COMPONENT as a ComponentName parcelable. A preference
 * XML {@code <intent><extra>} can only pass a String, which the picker ignores, and it then
 * closes straight away.
 */
public class SetWallpaperActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent preview = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        new ComponentName(this, ReactiveWallpaperService.class));
        try {
            startActivity(preview);
        } catch (ActivityNotFoundException e) {
            // No live-wallpaper picker: fall back to the generic chooser.
            try {
                startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(this, R.string.no_picker, Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }
}
