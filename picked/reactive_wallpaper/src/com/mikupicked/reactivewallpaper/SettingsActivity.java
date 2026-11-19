/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.reactivewallpaper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

/** Settings for the reactive wallpaper (opened from the wallpaper picker). */
public class SettingsActivity extends Activity {

    private static final int REQUEST_PHOTO = 1;
    private static final int MAX_SIDE = 2560;

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences(ReactiveWallpaperService.PREFS, MODE_PRIVATE);

        final int pad = dp(20);
        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        final Button pick = new Button(this);
        pick.setText(R.string.pick_photo);
        pick.setOnClickListener(v -> startActivityForResult(
                new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("image/*"), REQUEST_PHOTO));
        root.addView(pick);

        final Button sky = new Button(this);
        sky.setText(R.string.use_sky);
        sky.setOnClickListener(v -> {
            ReactiveWallpaperService.photoFile(this).delete();
            bump();
        });
        root.addView(sky);

        addSwitch(root, R.string.parallax, R.string.parallax_summary,
                ReactiveWallpaperService.KEY_PARALLAX);
        addSwitch(root, R.string.daylight, R.string.daylight_summary,
                ReactiveWallpaperService.KEY_DAYLIGHT);
        addSwitch(root, R.string.battery, R.string.battery_summary,
                ReactiveWallpaperService.KEY_BATTERY);
        // Edge-to-edge is enforced at target SDK 36: keep the content clear of the action bar,
        // status bar and navigation bar, and let it scroll on small screens.
        final ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        scroll.setOnApplyWindowInsetsListener((v, insets) -> {
            final android.graphics.Insets bars = insets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsets.CONSUMED;
        });
        setContentView(scroll);
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    private void addSwitch(LinearLayout root, int title, int summary, String key) {
        final Switch sw = new Switch(this);
        sw.setText(title);
        sw.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        sw.setChecked(mPrefs.getBoolean(key, true));
        sw.setOnCheckedChangeListener((b, checked) -> mPrefs.edit().putBoolean(key, checked)
                .apply());
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(20);
        root.addView(sw, lp);
        final TextView sub = new TextView(this);
        sub.setText(summary);
        sub.setAlpha(0.7f);
        root.addView(sub);
    }

    /** Tells the running engine to reload (any preference change does). */
    private void bump() {
        mPrefs.edit().putLong(ReactiveWallpaperService.KEY_VERSION, System.currentTimeMillis())
                .apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_PHOTO || resultCode != RESULT_OK || data == null
                || data.getData() == null) {
            return;
        }
        try {
            final Bitmap b = ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(getContentResolver(), data.getData()),
                    (decoder, info, src) -> {
                        final int longest = Math.max(info.getSize().getWidth(),
                                info.getSize().getHeight());
                        decoder.setTargetSampleSize(Math.max(1, longest / MAX_SIDE));
                        decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                    });
            final File out = ReactiveWallpaperService.photoFile(this);
            final File tmp = new File(out.getParentFile(), "photo.tmp");
            try (FileOutputStream os = new FileOutputStream(tmp)) {
                b.compress(Bitmap.CompressFormat.JPEG, 92, os);
            }
            b.recycle();
            if (!tmp.renameTo(out)) throw new java.io.IOException("rename");
            bump();
        } catch (Exception e) {
            Toast.makeText(this, R.string.photo_failed, Toast.LENGTH_LONG).show();
        }
    }
}
