/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.speakercal;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class MainActivity extends Activity {

    private static final int REQ_MIC = 1;
    private static final double[] TONE_AMPS = {0.03, 0.06, 0.12};
    private static final int[] TEST_COUNTS = {1, 3, 5};
    /** Seconds between tests, to move the phone. */
    private static final int GAP_S = 4;

    private static final Typeface MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL);

    private final Handler mMain = new Handler(Looper.getMainLooper());
    private SharedPreferences mPrefs;
    private Palette mP;

    private ScrollView mScroll;
    private int mStyle;
    private final LinearLayout[] mStyleRows = new LinearLayout[Calibration.STYLES.length];
    private final TextView[] mStyleNames = new TextView[Calibration.STYLES.length];
    private final TextView[] mStyleSums = new TextView[Calibration.STYLES.length];
    private final View[] mStyleDots = new View[Calibration.STYLES.length];
    private final TextView[] mStyleChips = new TextView[Calibration.STYLES.length];
    private Segmented mMics, mTones, mTests;
    private EditText mName;
    private CheckBox mFxOff;
    private TextView mStart, mCancel, mSave, mShare, mMessage;
    private LinearLayout mMeasureCard, mResultCard;
    private PulseView mPulse;
    private CurveView mCurve;
    private BandsView mBands;
    private TextView mBassValue, mBassLabel, mSpreadValue, mSpreadLabel, mSubcut;

    private Measurer mMeasurer;
    private Dsp.Response mResponse; // last measurement (average of its tests)
    private double[][] mRunCurves; // its single tests, leveled, when there were several
    private double mSpread;
    private Calibration.Result mResult;
    private Uri mSavedUri;

    // Looking up a model
    private static final int MAX_MATCHES = 8;
    private EditText mLookup;
    private TextView mLookupStatus, mLookupRetry, mResultHint, mHeadphoneNote;
    private LinearLayout mLookupList;
    private List<Library.Model> mModels;
    private boolean mModelsLoading;
    private int mLookupGen;
    private Library.Profile mProfile; // the result is from a published curve when set
    private final Runnable mFilter = this::filterModels;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mPrefs = getSharedPreferences("cal", MODE_PRIVATE);
        mP = new Palette(this);
        final Window w = getWindow();
        w.setBackgroundDrawable(new ColorDrawable(mP.bg));
        w.setStatusBarColor(mP.bg);
        w.setNavigationBarColor(mP.bg);
        setContentView(buildUi());
        final WindowInsetsController ic = w.getInsetsController();
        if (ic != null && !mP.night) {
            final int light = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
            ic.setSystemBarsAppearance(light, light);
        }
        w.getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(0, insets.getSystemWindowInsetTop(), 0,
                    insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
    }

    @Override
    protected void onDestroy() {
        if (mMeasurer != null) mMeasurer.cancel();
        super.onDestroy();
    }

    // ---------------------------------------------------------------------------------------
    // Building blocks
    // ---------------------------------------------------------------------------------------

    private int dp(float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    private GradientDrawable round(int color, float radiusDp) {
        final GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    /** Touch ripple over {@code content} (or alone when null), clipped to the same corners. */
    private Drawable ripple(Drawable content, float radiusDp) {
        return new RippleDrawable(ColorStateList.valueOf(Palette.alpha(mP.onSurface, 0x1f)),
                content, round(0xffffffff, radiusDp));
    }

    private TextView text(CharSequence s, float sp, int color) {
        final TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        return t;
    }

    private TextView text(int res, float sp, int color) {
        return text(getString(res), sp, color);
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /** A rounded card with a title and an optional line under it. */
    private LinearLayout card(LinearLayout parent, int title, int subtitle) {
        final LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(round(mP.card, 28));
        c.setPadding(dp(20), dp(18), dp(20), dp(20));
        final LinearLayout.LayoutParams lp = wrap();
        lp.bottomMargin = dp(12);
        parent.addView(c, lp);
        if (title != 0) {
            final TextView t = text(title, 17, mP.onSurface);
            t.setTypeface(MEDIUM);
            c.addView(t);
        }
        if (subtitle != 0) {
            final TextView s = text(subtitle, 13, mP.onSurfaceVar);
            s.setPadding(0, dp(2), 0, 0);
            c.addView(s);
        }
        return c;
    }

    private View gap(int dp) {
        final View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(dp)));
        return v;
    }

    private TextView button(int label, boolean filled, View.OnClickListener l) {
        final TextView b = text(label, 15, filled ? mP.onPrimary : mP.onSecondaryContainer);
        b.setTypeface(MEDIUM);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(24), 0, dp(24), 0);
        b.setMinHeight(dp(52));
        b.setBackground(ripple(round(filled ? mP.primary : mP.secondaryContainer, 26), 26));
        b.setOnClickListener(l);
        return b;
    }

    private static void setButtonEnabled(View b, boolean on) {
        b.setEnabled(on);
        b.setAlpha(on ? 1f : 0.38f);
    }

    /** A row of options, one picked, in a pill. */
    private final class Segmented {
        final LinearLayout view;
        final TextView[] items;
        int selected;
        boolean enabled = true;

        Segmented(int[] labels, int selected, IntConsumer onPick) {
            view = new LinearLayout(MainActivity.this);
            view.setBackground(round(mP.field, 24));
            view.setPadding(dp(4), dp(4), dp(4), dp(4));
            items = new TextView[labels.length];
            for (int i = 0; i < labels.length; i++) {
                final int idx = i;
                final TextView t = text(labels[i], 14, mP.onSurfaceVar);
                t.setGravity(Gravity.CENTER);
                t.setSingleLine(true);
                t.setOnClickListener(v -> {
                    if (!enabled || idx == this.selected) return;
                    select(idx);
                    onPick.accept(idx);
                });
                items[i] = t;
                view.addView(t, new LinearLayout.LayoutParams(0, dp(40), 1));
            }
            select(selected);
        }

        void select(int idx) {
            selected = idx;
            for (int i = 0; i < items.length; i++) {
                final boolean on = i == idx;
                items[i].setBackground(on ? ripple(round(mP.secondaryContainer, 20), 20)
                        : ripple(null, 20));
                items[i].setTextColor(on ? mP.onSecondaryContainer : mP.onSurfaceVar);
                items[i].setTypeface(on ? MEDIUM : Typeface.DEFAULT);
            }
        }

        void setEnabled(boolean on) {
            enabled = on;
            view.setAlpha(on ? 1f : 0.5f);
        }
    }

    // ---------------------------------------------------------------------------------------
    // UI
    // ---------------------------------------------------------------------------------------

    private View buildUi() {
        mScroll = new ScrollView(this);
        mScroll.setClipToPadding(false);
        final LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(16), dp(20), dp(16), dp(28));
        mScroll.addView(col);

        // Header
        final ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_speaker_cal);
        final LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(56), dp(56));
        ilp.leftMargin = dp(8);
        col.addView(icon, ilp);
        final TextView title = text(R.string.app_name, 30, mP.onSurface);
        title.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        title.setPadding(dp(8), dp(14), dp(8), 0);
        col.addView(title);
        final TextView intro = text(R.string.intro, 15, mP.onSurfaceVar);
        intro.setPadding(dp(8), dp(6), dp(8), dp(20));
        col.addView(intro);

        // Style
        final LinearLayout style = card(col, R.string.section_style, 0);
        mStyle = Math.max(0, Math.min(Calibration.STYLES.length - 1, mPrefs.getInt("style", 2)));
        for (int i = 0; i < Calibration.STYLES.length; i++) {
            final int idx = i;
            final Calibration.Style s = Calibration.STYLES[i];
            final LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(16), dp(12), dp(16), dp(12));
            final View dot = new View(this);
            final LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(dp(20), dp(20));
            dlp.rightMargin = dp(16);
            row.addView(dot, dlp);
            final LinearLayout texts = new LinearLayout(this);
            texts.setOrientation(LinearLayout.VERTICAL);
            final TextView name = text(s.name, 15, mP.onSurface);
            name.setTypeface(MEDIUM);
            final TextView sum = text(s.summary, 13, mP.onSurfaceVar);
            texts.addView(name);
            texts.addView(sum);
            row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            row.setOnClickListener(v -> selectStyle(idx));
            final LinearLayout.LayoutParams rlp = wrap();
            rlp.topMargin = dp(i == 0 ? 12 : 6);
            style.addView(row, rlp);
            mStyleRows[i] = row;
            mStyleNames[i] = name;
            mStyleSums[i] = sum;
            mStyleDots[i] = dot;
        }
        updateStyleRows();

        buildLookup(col);

        final TextView or = text(R.string.or_measure, 20, mP.onSurface);
        or.setPadding(dp(8), dp(16), dp(8), 0);
        col.addView(or);
        final TextView orHint = text(R.string.or_measure_hint, 14, mP.onSurfaceVar);
        orHint.setPadding(dp(8), dp(2), dp(8), dp(12));
        col.addView(orHint);

        // Microphone
        final LinearLayout mic = card(col, R.string.section_mic, R.string.mic_hint);
        mMics = new Segmented(new int[] {R.string.mic_bottom, R.string.mic_top, R.string.mic_both},
                clamp(mPrefs.getInt("mic", Measurer.MIC_TOP), 3),
                i -> mPrefs.edit().putInt("mic", i).apply());
        mic.addView(gap(12));
        mic.addView(mMics.view, wrap());

        // Test tone volume
        final LinearLayout tone = card(col, R.string.section_tone, R.string.tone_hint);
        mTones = new Segmented(new int[] {R.string.tone_quiet, R.string.tone_normal, R.string.tone_loud},
                clamp(mPrefs.getInt("tone", 1), 3),
                i -> mPrefs.edit().putInt("tone", i).apply());
        tone.addView(gap(12));
        tone.addView(mTones.view, wrap());

        // Number of tests
        final LinearLayout tests = card(col, R.string.section_tests, R.string.tests_hint);
        mTests = new Segmented(new int[] {R.string.tests_1, R.string.tests_3, R.string.tests_5},
                clamp(mPrefs.getInt("tests", 1), 3),
                i -> mPrefs.edit().putInt("tests", i).apply());
        tests.addView(gap(12));
        tests.addView(mTests.view, wrap());

        // Speaker name
        final LinearLayout nameCard = card(col, R.string.section_name, R.string.name_hint);
        mName = new EditText(this);
        mName.setSingleLine(true);
        mName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mName.setText(mPrefs.getString("name", getString(R.string.default_name)));
        mName.setTextColor(mP.onSurface);
        mName.setHint(R.string.default_name);
        mName.setHintTextColor(Palette.alpha(mP.onSurfaceVar, 0x99));
        mName.setTextSize(16);
        mName.setBackground(round(mP.field, 16));
        mName.setPadding(dp(16), dp(14), dp(16), dp(14));
        nameCard.addView(gap(12));
        nameCard.addView(mName, wrap());

        // Before you start + measuring
        mMeasureCard = card(col, R.string.section_ready, 0);
        final int[] steps = {R.string.step_connect, R.string.step_fx, R.string.step_place,
                R.string.step_quiet};
        for (int i = 0; i < steps.length; i++) {
            final LinearLayout row = new LinearLayout(this);
            row.setPadding(0, dp(12), 0, 0);
            final TextView badge = text(String.valueOf(i + 1), 12, mP.onPrimaryContainer);
            badge.setTypeface(Typeface.DEFAULT_BOLD);
            badge.setGravity(Gravity.CENTER);
            final GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(mP.primaryContainer);
            badge.setBackground(circle);
            final LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(dp(24), dp(24));
            blp.rightMargin = dp(14);
            row.addView(badge, blp);
            row.addView(text(steps[i], 14, mP.onSurface),
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            mMeasureCard.addView(row);
        }
        mFxOff = new CheckBox(this);
        mFxOff.setText(R.string.fx_off);
        mFxOff.setTextColor(mP.onSurface);
        mFxOff.setTextSize(15);
        mFxOff.setButtonTintList(ColorStateList.valueOf(mP.primary));
        final LinearLayout.LayoutParams clp = wrap();
        clp.topMargin = dp(10);
        mMeasureCard.addView(mFxOff, clp);

        mPulse = new PulseView(this, null);
        mPulse.setColors(mP.primary, mP.onSurface);
        mPulse.setVisibility(View.GONE);
        mMeasureCard.addView(mPulse, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(320)));

        mStart = button(R.string.start, true, v -> start());
        mCancel = button(R.string.cancel, false, v -> {
            if (mMeasurer != null) mMeasurer.cancel();
        });
        mCancel.setVisibility(View.GONE);
        final LinearLayout.LayoutParams slp = wrap();
        slp.topMargin = dp(14);
        mMeasureCard.addView(mStart, slp);
        mMeasureCard.addView(mCancel, wrap());

        mMessage = text("", 14, mP.error);
        mMessage.setPadding(dp(4), dp(10), dp(4), 0);
        mMessage.setVisibility(View.GONE);
        mMeasureCard.addView(mMessage);

        buildResult(col);
        updateStyleRows();
        return mScroll;
    }

    private void buildLookup(LinearLayout col) {
        final LinearLayout c = card(col, R.string.section_lookup, R.string.lookup_hint);
        mLookup = new EditText(this);
        mLookup.setSingleLine(true);
        mLookup.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mLookup.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mLookup.setHint(R.string.lookup_field);
        mLookup.setHintTextColor(Palette.alpha(mP.onSurfaceVar, 0x99));
        mLookup.setTextColor(mP.onSurface);
        mLookup.setTextSize(16);
        mLookup.setBackground(round(mP.field, 16));
        mLookup.setPadding(dp(16), dp(14), dp(16), dp(14));
        mLookup.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int d) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int d) {}
            @Override public void afterTextChanged(Editable e) {
                mMain.removeCallbacks(mFilter);
                mMain.postDelayed(mFilter, 200);
            }
        });
        mLookup.setOnEditorActionListener((v, id, ev) -> {
            if (id != EditorInfo.IME_ACTION_SEARCH) return false;
            mMain.removeCallbacks(mFilter);
            filterModels();
            hideKeyboard();
            return true;
        });
        mLookup.setOnFocusChangeListener((v, has) -> {
            if (has) loadModels(); // start downloading while they type
        });
        c.addView(gap(12));
        c.addView(mLookup, wrap());

        mLookupList = new LinearLayout(this);
        mLookupList.setOrientation(LinearLayout.VERTICAL);
        c.addView(mLookupList, wrap());

        mLookupStatus = text("", 13, mP.onSurfaceVar);
        mLookupStatus.setPadding(dp(4), dp(10), dp(4), 0);
        mLookupStatus.setVisibility(View.GONE);
        c.addView(mLookupStatus);

        mLookupRetry = button(R.string.lookup_retry, false, v -> loadModels());
        mLookupRetry.setVisibility(View.GONE);
        final LinearLayout.LayoutParams rlp = wrap();
        rlp.topMargin = dp(10);
        c.addView(mLookupRetry, rlp);
    }

    private void setLookupStatus(CharSequence s, boolean error) {
        mLookupStatus.setText(s);
        mLookupStatus.setTextColor(error ? mP.error : mP.onSurfaceVar);
        mLookupStatus.setVisibility(s == null || s.length() == 0 ? View.GONE : View.VISIBLE);
    }

    private void hideKeyboard() {
        final InputMethodManager imm = getSystemService(InputMethodManager.class);
        if (imm != null) imm.hideSoftInputFromWindow(mLookup.getWindowToken(), 0);
    }

    private void loadModels() {
        if (mModels != null || mModelsLoading) return;
        mModelsLoading = true;
        mLookupRetry.setVisibility(View.GONE);
        setLookupStatus(getString(R.string.lookup_loading), false);
        new Thread(() -> {
            List<Library.Model> models = null;
            String err = null;
            try {
                models = Library.load(getApplicationContext());
            } catch (Exception e) {
                err = e.getMessage() != null ? e.getMessage() : e.toString();
            }
            final List<Library.Model> m = models;
            final String error = err;
            mMain.post(() -> {
                mModelsLoading = false;
                if (isDestroyed()) return;
                if (m == null) {
                    setLookupStatus(getString(R.string.lookup_error, error), true);
                    mLookupRetry.setVisibility(View.VISIBLE);
                    return;
                }
                mModels = m;
                filterModels();
            });
        }, "ModelList").start();
    }

    private void filterModels() {
        mLookupList.removeAllViews();
        final String q = mLookup.getText().toString().trim();
        if (mModels == null) {
            if (!q.isEmpty()) loadModels();
            return;
        }
        if (q.isEmpty()) {
            int h = 0;
            for (Library.Model m : mModels) if (m.kind == Library.HEADPHONES) h++;
            setLookupStatus(getString(R.string.lookup_count, h, mModels.size() - h), false);
            return;
        }
        final List<Library.Model> all = Library.search(mModels, q, Integer.MAX_VALUE);
        if (all.isEmpty()) {
            setLookupStatus(getString(R.string.lookup_none, q), false);
            return;
        }
        for (int i = 0; i < all.size() && i < MAX_MATCHES; i++) addModelRow(all.get(i));
        setLookupStatus(all.size() > MAX_MATCHES
                ? getString(R.string.lookup_more, all.size() - MAX_MATCHES) : null, false);
    }

    private void addModelRow(Library.Model m) {
        final LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(16), dp(10), dp(16), dp(10));
        final GradientDrawable bg = round(0, 16);
        bg.setStroke(dp(1), Palette.alpha(mP.outline, 0x55));
        row.setBackground(ripple(bg, 16));
        final TextView name = text(m.name, 15, mP.onSurface);
        name.setTypeface(MEDIUM);
        final String kind = getString(m.kind == Library.HEADPHONES
                ? R.string.kind_headphones : R.string.kind_speaker);
        final TextView detail = text(m.kind == Library.HEADPHONES ? kind + " · " + m.detail
                : m.detail.substring(0, 1).toUpperCase() + m.detail.substring(1) + " · " + m.source,
                13, mP.onSurfaceVar);
        row.addView(name);
        row.addView(detail);
        row.setOnClickListener(v -> pickModel(m));
        final LinearLayout.LayoutParams lp = wrap();
        lp.topMargin = dp(8);
        mLookupList.addView(row, lp);
    }

    private void pickModel(Library.Model m) {
        hideKeyboard();
        mLookupList.removeAllViews();
        setLookupStatus(getString(R.string.lookup_fetching, m.name), false);
        final int gen = ++mLookupGen;
        new Thread(() -> {
            Library.Profile p = null;
            String err = null;
            try {
                p = Library.profile(m);
            } catch (Exception e) {
                err = e.getMessage() != null ? e.getMessage() : e.toString();
            }
            final Library.Profile profile = p;
            final String error = err;
            mMain.post(() -> {
                if (gen != mLookupGen || isDestroyed()) return;
                if (profile == null) {
                    setLookupStatus(getString(R.string.lookup_error, error), true);
                    return;
                }
                mProfile = profile;
                mResponse = null;
                mRunCurves = null;
                mName.setText(m.name);
                mPrefs.edit().putString("name", m.name).apply();
                setLookupStatus(getString(R.string.lookup_using, m.name), false);
                showResult();
                mScroll.post(() -> mScroll.smoothScrollTo(0, mResultCard.getTop() - dp(12)));
            });
        }, "ModelCurve").start();
    }

    private void buildResult(LinearLayout col) {
        mResultCard = card(col, R.string.section_result, 0);
        mResultCard.setVisibility(View.GONE);

        // The style again, right by the graph, so switching shows the change at once
        final android.widget.HorizontalScrollView chipScroll =
                new android.widget.HorizontalScrollView(this);
        chipScroll.setHorizontalScrollBarEnabled(false);
        chipScroll.setClipToPadding(false);
        final LinearLayout chips = new LinearLayout(this);
        chipScroll.addView(chips);
        for (int i = 0; i < Calibration.STYLES.length; i++) {
            final int idx = i;
            final TextView chip = text(Calibration.STYLES[i].name, 14, mP.onSurfaceVar);
            chip.setGravity(Gravity.CENTER);
            chip.setSingleLine(true);
            chip.setPadding(dp(16), 0, dp(16), 0);
            chip.setMinHeight(dp(36));
            chip.setOnClickListener(v -> selectStyle(idx));
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, dp(36));
            if (i > 0) lp.leftMargin = dp(8);
            chips.addView(chip, lp);
            mStyleChips[i] = chip;
        }
        final LinearLayout.LayoutParams clp = wrap();
        clp.topMargin = dp(12);
        mResultCard.addView(chipScroll, clp);

        // Two stat tiles
        final LinearLayout tiles = new LinearLayout(this);
        final LinearLayout.LayoutParams tlp = wrap();
        tlp.topMargin = dp(12);
        mResultCard.addView(tiles, tlp);
        final TextView[] bass = tile(tiles, false);
        mBassValue = bass[0];
        mBassLabel = bass[1];
        mBassLabel.setText(R.string.tile_bass_label);
        final TextView[] spread = tile(tiles, true);
        mSpreadValue = spread[0];
        mSpreadLabel = spread[1];

        mCurve = new CurveView(this, null);
        mCurve.setColors(mP.onSurface, mP.measured, Palette.alpha(mP.onSurface, 0x99), mP.primary);
        final LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(260));
        glp.topMargin = dp(18);
        mResultCard.addView(mCurve, glp);
        mResultHint = text(R.string.result_graph_hint, 12, mP.onSurfaceVar);
        mResultHint.setPadding(0, dp(6), 0, 0);
        mResultCard.addView(mResultHint);

        final TextView eq = text(R.string.result_eq, 15, mP.onSurface);
        eq.setTypeface(MEDIUM);
        eq.setPadding(0, dp(20), 0, dp(4));
        mResultCard.addView(eq);
        mBands = new BandsView(this, null);
        mBands.setColors(mP);
        mResultCard.addView(mBands, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(180)));
        mSubcut = text("", 13, mP.onSurfaceVar);
        mSubcut.setPadding(0, dp(6), 0, 0);
        mResultCard.addView(mSubcut);
        mHeadphoneNote = text(R.string.result_headphones, 13, mP.onSurfaceVar);
        mHeadphoneNote.setPadding(0, dp(6), 0, 0);
        mResultCard.addView(mHeadphoneNote);

        final LinearLayout save = new LinearLayout(this);
        final LinearLayout.LayoutParams svlp = wrap();
        svlp.topMargin = dp(18);
        mResultCard.addView(save, svlp);
        mSave = button(R.string.save, true, v -> save());
        mShare = button(R.string.share, false, v -> share());
        setButtonEnabled(mShare, false);
        final LinearLayout.LayoutParams b1 = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        b1.rightMargin = dp(10);
        save.addView(mSave, b1);
        save.addView(mShare, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        final TextView imp = text(R.string.import_hint, 13, mP.onSurfaceVar);
        imp.setPadding(0, dp(14), 0, 0);
        mResultCard.addView(imp);
    }

    /** A stat tile: {value, label}. */
    private TextView[] tile(LinearLayout parent, boolean last) {
        final LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setBackground(round(mP.primaryContainer, 20));
        t.setPadding(dp(16), dp(14), dp(16), dp(14));
        final TextView value = text("", 22, mP.onPrimaryContainer);
        value.setTypeface(MEDIUM);
        final TextView label = text("", 12, Palette.alpha(mP.onPrimaryContainer, 0xcc));
        t.addView(value);
        t.addView(label);
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        if (!last) lp.rightMargin = dp(10);
        parent.addView(t, lp);
        return new TextView[] {value, label};
    }

    private static int clamp(int v, int n) {
        return Math.max(0, Math.min(n - 1, v));
    }

    private void selectStyle(int idx) {
        if (idx == mStyle) return;
        mStyle = idx;
        mPrefs.edit().putInt("style", idx).apply();
        updateStyleRows();
        if (mResponse != null || mProfile != null) showResult(); // re-solve, no new measurement needed
    }

    private void updateStyleRows() {
        for (int i = 0; i < mStyleChips.length; i++) {
            if (mStyleChips[i] == null) break;
            final boolean on = i == mStyle;
            final GradientDrawable bg = round(on ? mP.secondaryContainer : 0, 18);
            if (!on) bg.setStroke(dp(1), Palette.alpha(mP.outline, 0x77));
            mStyleChips[i].setBackground(ripple(bg, 18));
            mStyleChips[i].setTextColor(on ? mP.onSecondaryContainer : mP.onSurfaceVar);
            mStyleChips[i].setTypeface(on ? MEDIUM : Typeface.DEFAULT);
        }
        for (int i = 0; i < mStyleRows.length; i++) {
            final boolean on = i == mStyle;
            final GradientDrawable bg = round(on ? mP.primaryContainer : 0, 20);
            if (!on) bg.setStroke(dp(1), Palette.alpha(mP.outline, 0x66));
            mStyleRows[i].setBackground(ripple(bg, 20));
            mStyleNames[i].setTextColor(on ? mP.onPrimaryContainer : mP.onSurface);
            final Calibration.Style st = Calibration.STYLES[i];
            mStyleSums[i].setText(mProfile != null && mProfile.headphones && st.hpSummary != 0
                    ? st.hpSummary : st.summary);
            mStyleSums[i].setTextColor(on ? Palette.alpha(mP.onPrimaryContainer, 0xcc)
                    : mP.onSurfaceVar);
            final GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            if (on) {
                dot.setColor(mP.primary);
                dot.setStroke(dp(5), mP.primaryContainer);
                final GradientDrawable ring = new GradientDrawable();
                ring.setShape(GradientDrawable.OVAL);
                ring.setStroke(dp(2), mP.primary);
                mStyleDots[i].setBackground(new android.graphics.drawable.LayerDrawable(
                        new Drawable[] {dot, ring}));
            } else {
                dot.setStroke(dp(2), mP.outline);
                mStyleDots[i].setBackground(dot);
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // Measuring
    // ---------------------------------------------------------------------------------------

    private void showMessage(CharSequence m) {
        mMessage.setText(m);
        mMessage.setVisibility(m == null || m.length() == 0 ? View.GONE : View.VISIBLE);
    }

    private void start() {
        if (!mFxOff.isChecked()) {
            showMessage(getString(R.string.err_fx_on));
            return;
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO}, REQ_MIC);
            return;
        }
        mPrefs.edit().putString("name", mName.getText().toString().trim()).apply();
        final int mic = mMics.selected;
        final double amp = TONE_AMPS[mTones.selected];
        final int n = TEST_COUNTS[mTests.selected];

        showMessage(null);
        mResultCard.setVisibility(View.GONE);
        mStart.setVisibility(View.GONE);
        mCancel.setVisibility(View.VISIBLE);
        mPulse.reset();
        mPulse.setStep(n > 1 ? getString(R.string.status_step, 1, n) : "");
        mPulse.setStatus(getString(R.string.status_listening));
        mPulse.setVisibility(View.VISIBLE);
        setInputsEnabled(false);
        mScroll.post(() -> mScroll.smoothScrollTo(0, mMeasureCard.getTop() + mPulse.getTop() - dp(24)));

        mMeasurer = new Measurer();
        final Measurer m = mMeasurer;
        new Thread(() -> {
            final List<Dsp.Response> runs = new ArrayList<>();
            int test = 0;
            try {
                for (test = 0; test < n; test++) {
                    final int t = test;
                    if (t > 0) {
                        // Countdown to move the phone a little
                        mPulse.setStep(getString(R.string.status_next, t + 1, n));
                        mPulse.setStatus(getString(R.string.status_move));
                        mPulse.update((float) t / n, 0, 0);
                        for (int s = GAP_S; s > 0; s--) {
                            mPulse.setCenter(String.valueOf(s));
                            for (int k = 0; k < 10; k++) {
                                if (m.isCancelled()) throw new InterruptedException();
                                Thread.sleep(100);
                            }
                        }
                        mPulse.setCenter(null);
                        mPulse.setStep(getString(R.string.status_step, t + 1, n));
                        mPulse.setStatus(getString(R.string.status_listening));
                    }
                    final Measurer.Recording rec = m.run(amp, mic,
                            (p, hz, lvl) -> mPulse.update((t + p) / n, hz, lvl));
                    mPulse.setStatus(getString(R.string.status_analysing));
                    final float chPeak = mic == Measurer.MIC_BOTTOM ? rec.channelPeak[0]
                            : mic == Measurer.MIC_TOP ? rec.channelPeak[1]
                            : Math.max(rec.channelPeak[0], rec.channelPeak[1]);
                    if (chPeak < 0.01f) {
                        done(inTest(t, n, getString(R.string.err_silent)), false);
                        return;
                    }
                    if (rec.clipped > 0) {
                        done(inTest(t, n, getString(R.string.err_clipped)), false);
                        return;
                    }
                    runs.add(Dsp.measure(rec.signal, amp));
                }
                final Dsp.Response avg = n == 1 ? runs.get(0) : Dsp.average(runs);
                final double spread = Dsp.spread(runs);
                final double[][] curves;
                if (n > 1) {
                    curves = new double[n][];
                    for (int i = 0; i < n; i++) curves[i] = Dsp.leveled(runs.get(i));
                } else {
                    curves = null;
                }
                mMain.post(() -> {
                    mProfile = null;
                    ++mLookupGen; // drop a model curve still on its way
                    setLookupStatus(null, false);
                    mResponse = avg;
                    mRunCurves = curves;
                    mSpread = spread;
                    showResult();
                    mScroll.post(() -> mScroll.smoothScrollTo(0, mResultCard.getTop() - dp(12)));
                });
                done(null, true);
            } catch (InterruptedException e) {
                done(getString(R.string.cancelled), false);
            } catch (Exception e) {
                done(inTest(test, n, getString(R.string.err_mic, e.getMessage())), false);
            }
        }, "SpeakerCal").start();
    }

    private String inTest(int test, int n, String msg) {
        return n > 1 ? getString(R.string.err_in_test, test + 1, msg) : msg;
    }

    private void done(String message, boolean ok) {
        mMain.post(() -> {
            mPulse.setVisibility(View.GONE);
            mCancel.setVisibility(View.GONE);
            mStart.setVisibility(View.VISIBLE);
            mStart.setText(ok ? R.string.measure_again : R.string.start);
            setInputsEnabled(true);
            showMessage(message);
        });
    }

    private void setInputsEnabled(boolean on) {
        mMics.setEnabled(on);
        mTones.setEnabled(on);
        mTests.setEnabled(on);
        mName.setEnabled(on);
        mFxOff.setEnabled(on);
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] grants) {
        if (req == REQ_MIC && grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED) {
            start();
        } else {
            showMessage(getString(R.string.err_permission));
        }
    }

    // ---------------------------------------------------------------------------------------
    // Result
    // ---------------------------------------------------------------------------------------

    private Calibration.Style style() {
        return Calibration.STYLES[mStyle];
    }

    private void showResult() {
        mResult = mProfile != null ? Calibration.solve(mProfile, style())
                : Calibration.solve(mResponse, style());
        mSavedUri = null;
        setButtonEnabled(mShare, false);

        final int n = mRunCurves == null ? 1 : mRunCurves.length;
        mCurve.setNames(getString(mProfile != null ? R.string.curve_published
                        : n > 1 ? R.string.curve_measured_avg : R.string.curve_measured),
                getString(R.string.curve_target), getString(R.string.curve_corrected));
        mResultHint.setText(mProfile != null ? R.string.result_graph_hint_db
                : R.string.result_graph_hint);
        mHeadphoneNote.setVisibility(mProfile != null && mProfile.headphones
                ? View.VISIBLE : View.GONE);
        updateStyleRows(); // headphone or speaker wording
        mCurve.setRuns(mRunCurves);
        mCurve.setData(mResult.measured, mResult.target, mResult.corrected, mResult.bassLimitHz);
        mBands.setGains(mResult.soundEq);

        mBassValue.setText(getString(R.string.tile_bass_value, (int) Math.round(mResult.bassLimitHz)));
        if (mProfile != null) {
            mSpreadValue.setText(mProfile.source);
            mSpreadLabel.setText(R.string.tile_source_label);
        } else if (n > 1) {
            mSpreadValue.setText(getString(R.string.tile_spread_value, mSpread));
            mSpreadLabel.setText(getString(mSpread < 1.5 ? R.string.tile_spread_good
                    : mSpread < 3 ? R.string.tile_spread_ok : R.string.tile_spread_bad, n));
        } else {
            mSpreadValue.setText(R.string.tile_single_value);
            mSpreadLabel.setText(R.string.tile_single_label);
        }

        if (mResult.fir[0] != 0 || mResult.fir[1] != 0) {
            mSubcut.setText(getString(mResult.headphones ? R.string.result_subbass
                    : R.string.result_subcut, mResult.fir[0], mResult.fir[1]));
            mSubcut.setVisibility(View.VISIBLE);
        } else {
            mSubcut.setVisibility(View.GONE);
        }
        mResultCard.setVisibility(View.VISIBLE);
    }

    private String fileName() {
        String name = mName.getText().toString().trim();
        if (name.isEmpty()) name = getString(R.string.default_name);
        name = (name + " " + getString(style().name)).replaceAll("[^a-zA-Z0-9_\\- ]", "").trim();
        return name + ".json";
    }

    private void save() {
        try {
            final String json = Calibration.presetJson(mResult, style());
            final ContentValues cv = new ContentValues();
            cv.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName());
            cv.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
            cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/AxionFx");
            final Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
            if (uri == null) throw new IllegalStateException("insert failed");
            try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            mSavedUri = uri;
            setButtonEnabled(mShare, true);
            Toast.makeText(this, getString(R.string.saved, fileName()), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.err_save, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void share() {
        if (mSavedUri == null) return;
        final Intent i = new Intent(Intent.ACTION_SEND)
                .setType("application/json")
                .putExtra(Intent.EXTRA_STREAM, mSavedUri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(i, getString(R.string.share)));
    }
}
