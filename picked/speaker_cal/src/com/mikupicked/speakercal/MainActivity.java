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
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int REQ_MIC = 1;
    private static final double[] TONE_AMPS = {0.03, 0.06, 0.12};

    private final Handler mMain = new Handler(Looper.getMainLooper());
    private SharedPreferences mPrefs;

    private RadioGroup mStyles, mMics, mTones;
    private EditText mName;
    private CheckBox mFxOff;
    private Button mStart, mCancel, mSave, mShare;
    private PulseView mPulse;
    private CurveView mCurve;
    private TextView mResultText, mMessage;
    private LinearLayout mResultBox;

    private Measurer mMeasurer;
    private Dsp.Response mResponse; // last measurement
    private Calibration.Result mResult;
    private Uri mSavedUri;

    private int mAccent, mOnSurface;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        mPrefs = getSharedPreferences("cal", MODE_PRIVATE);
        final TypedArray ta = obtainStyledAttributes(
                new int[] {android.R.attr.colorAccent, android.R.attr.textColorPrimary});
        mAccent = ta.getColor(0, 0xff82b1ff);
        mOnSurface = ta.getColor(1, 0xffffffff);
        ta.recycle();
        setContentView(buildUi());
        getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(0, insets.getSystemWindowInsetTop(), 0,
                    insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
    }

    // ---------------------------------------------------------------------------------------
    // UI
    // ---------------------------------------------------------------------------------------

    private int dp(float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    private TextView header(int text) {
        final TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(15);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(mAccent);
        t.setPadding(0, dp(18), 0, dp(6));
        return t;
    }

    private TextView body(CharSequence text) {
        final TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(14);
        return t;
    }

    private View buildUi() {
        final ScrollView scroll = new ScrollView(this);
        final LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(20), dp(16), dp(20), dp(32));
        scroll.addView(col);

        final TextView title = new TextView(this);
        title.setText(R.string.app_name);
        title.setTextSize(26);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(title);
        col.addView(body(getString(R.string.intro)));

        // Style
        col.addView(header(R.string.section_style));
        mStyles = new RadioGroup(this);
        for (int i = 0; i < Calibration.STYLES.length; i++) {
            final Calibration.Style s = Calibration.STYLES[i];
            final RadioButton rb = new RadioButton(this);
            rb.setId(100 + i);
            rb.setText(getString(s.name) + "\n" + getString(s.summary));
            rb.setPadding(dp(8), dp(6), 0, dp(6));
            mStyles.addView(rb);
        }
        mStyles.check(100 + mPrefs.getInt("style", 2));
        mStyles.setOnCheckedChangeListener((g, id) -> {
            mPrefs.edit().putInt("style", id - 100).apply();
            if (mResponse != null) showResult(); // re-solve, no new measurement needed
        });
        col.addView(mStyles);

        // Microphone
        col.addView(header(R.string.section_mic));
        mMics = new RadioGroup(this);
        mMics.setOrientation(RadioGroup.HORIZONTAL);
        final int[] micNames = {R.string.mic_bottom, R.string.mic_top, R.string.mic_both};
        for (int i = 0; i < 3; i++) {
            final RadioButton rb = new RadioButton(this);
            rb.setId(200 + i);
            rb.setText(micNames[i]);
            rb.setPadding(dp(4), 0, dp(16), 0);
            mMics.addView(rb);
        }
        mMics.check(200 + mPrefs.getInt("mic", Measurer.MIC_TOP));
        mMics.setOnCheckedChangeListener((g, id) -> mPrefs.edit().putInt("mic", id - 200).apply());
        col.addView(mMics);
        col.addView(body(getString(R.string.mic_hint)));

        // Test tone volume
        col.addView(header(R.string.section_tone));
        mTones = new RadioGroup(this);
        mTones.setOrientation(RadioGroup.HORIZONTAL);
        final int[] toneNames = {R.string.tone_quiet, R.string.tone_normal, R.string.tone_loud};
        for (int i = 0; i < 3; i++) {
            final RadioButton rb = new RadioButton(this);
            rb.setId(300 + i);
            rb.setText(toneNames[i]);
            rb.setPadding(dp(4), 0, dp(16), 0);
            mTones.addView(rb);
        }
        mTones.check(300 + mPrefs.getInt("tone", 1));
        mTones.setOnCheckedChangeListener((g, id) -> mPrefs.edit().putInt("tone", id - 300).apply());
        col.addView(mTones);

        // Speaker name
        col.addView(header(R.string.section_name));
        mName = new EditText(this);
        mName.setSingleLine(true);
        mName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mName.setText(mPrefs.getString("name", getString(R.string.default_name)));
        col.addView(mName);

        // Before you start
        col.addView(header(R.string.section_ready));
        col.addView(body(getString(R.string.ready_steps)));
        mFxOff = new CheckBox(this);
        mFxOff.setText(R.string.fx_off);
        col.addView(mFxOff);

        final LinearLayout buttons = new LinearLayout(this);
        buttons.setPadding(0, dp(12), 0, 0);
        mStart = new Button(this);
        mStart.setText(R.string.start);
        mStart.setOnClickListener(v -> start());
        mCancel = new Button(this);
        mCancel.setText(R.string.cancel);
        mCancel.setVisibility(View.GONE);
        mCancel.setOnClickListener(v -> {
            if (mMeasurer != null) mMeasurer.cancel();
        });
        buttons.addView(mStart);
        buttons.addView(mCancel);
        col.addView(buttons);

        mMessage = body("");
        mMessage.setPadding(0, dp(8), 0, 0);
        col.addView(mMessage);

        mPulse = new PulseView(this, null);
        mPulse.setColors(mAccent, mOnSurface);
        mPulse.setVisibility(View.GONE);
        col.addView(mPulse, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(300)));

        // Result
        mResultBox = new LinearLayout(this);
        mResultBox.setOrientation(LinearLayout.VERTICAL);
        mResultBox.setVisibility(View.GONE);
        mResultBox.addView(header(R.string.section_result));
        mCurve = new CurveView(this, null);
        mCurve.setColors(mOnSurface, 0xffef9a9a, (mOnSurface & 0x00ffffff) | 0x99000000, mAccent);
        mCurve.setNames(getString(R.string.curve_measured), getString(R.string.curve_target),
                getString(R.string.curve_corrected));
        mResultBox.addView(mCurve, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(260)));
        mResultText = body("");
        mResultText.setPadding(0, dp(8), 0, 0);
        mResultText.setTypeface(Typeface.MONOSPACE);
        mResultText.setTextSize(12);
        mResultBox.addView(mResultText);
        final LinearLayout save = new LinearLayout(this);
        save.setPadding(0, dp(8), 0, 0);
        mSave = new Button(this);
        mSave.setText(R.string.save);
        mSave.setOnClickListener(v -> save());
        mShare = new Button(this);
        mShare.setText(R.string.share);
        mShare.setEnabled(false);
        mShare.setOnClickListener(v -> share());
        save.addView(mSave);
        save.addView(mShare);
        mResultBox.addView(save);
        mResultBox.addView(body(getString(R.string.import_hint)));
        col.addView(mResultBox);
        return scroll;
    }

    // ---------------------------------------------------------------------------------------
    // Measuring
    // ---------------------------------------------------------------------------------------

    private void start() {
        if (!mFxOff.isChecked()) {
            mMessage.setText(R.string.err_fx_on);
            return;
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO}, REQ_MIC);
            return;
        }
        mPrefs.edit().putString("name", mName.getText().toString().trim()).apply();
        final int mic = mMics.getCheckedRadioButtonId() - 200;
        final double amp = TONE_AMPS[mTones.getCheckedRadioButtonId() - 300];

        mMessage.setText("");
        mResultBox.setVisibility(View.GONE);
        mStart.setVisibility(View.GONE);
        mCancel.setVisibility(View.VISIBLE);
        mPulse.reset();
        mPulse.setStatus(getString(R.string.status_listening));
        mPulse.setVisibility(View.VISIBLE);
        setInputsEnabled(false);

        mMeasurer = new Measurer();
        final Measurer m = mMeasurer;
        new Thread(() -> {
            try {
                final Measurer.Recording rec = m.run(amp, mic,
                        (p, hz, lvl) -> mPulse.update(p, hz, lvl));
                mMain.post(() -> mPulse.setStatus(getString(R.string.status_analysing)));
                final float chPeak = mic == Measurer.MIC_BOTTOM ? rec.channelPeak[0]
                        : mic == Measurer.MIC_TOP ? rec.channelPeak[1]
                        : Math.max(rec.channelPeak[0], rec.channelPeak[1]);
                if (chPeak < 0.01f) {
                    finish(getString(R.string.err_silent), false);
                    return;
                }
                if (rec.clipped > 0) {
                    finish(getString(R.string.err_clipped), false);
                    return;
                }
                final Dsp.Response resp = Dsp.measure(rec.signal, amp);
                mMain.post(() -> {
                    mResponse = resp;
                    showResult();
                });
                finish(null, true);
            } catch (InterruptedException e) {
                finish(getString(R.string.cancelled), false);
            } catch (Exception e) {
                finish(getString(R.string.err_mic, e.getMessage()), false);
            }
        }, "SpeakerCal").start();
    }

    private void finish(String message, boolean ok) {
        mMain.post(() -> {
            mPulse.setVisibility(View.GONE);
            mCancel.setVisibility(View.GONE);
            mStart.setVisibility(View.VISIBLE);
            mStart.setText(ok ? R.string.measure_again : R.string.start);
            setInputsEnabled(true);
            if (message != null) mMessage.setText(message);
        });
    }

    private void setInputsEnabled(boolean on) {
        for (RadioGroup g : new RadioGroup[] {mMics, mTones}) {
            for (int i = 0; i < g.getChildCount(); i++) g.getChildAt(i).setEnabled(on);
        }
        mName.setEnabled(on);
        mFxOff.setEnabled(on);
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] grants) {
        if (req == REQ_MIC && grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED) {
            start();
        } else {
            mMessage.setText(R.string.err_permission);
        }
    }

    // ---------------------------------------------------------------------------------------
    // Result
    // ---------------------------------------------------------------------------------------

    private Calibration.Style style() {
        return Calibration.STYLES[mStyles.getCheckedRadioButtonId() - 100];
    }

    private void showResult() {
        final Calibration.Style s = style();
        mResult = Calibration.solve(mResponse, s);
        mSavedUri = null;
        mShare.setEnabled(false);
        mCurve.setData(mResult.measured, mResult.target, mResult.corrected, mResult.bassLimitHz);
        final StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.result_bass_limit, (int) Math.round(mResult.bassLimitHz)))
                .append("\n\n").append(getString(R.string.result_eq)).append('\n');
        final String[] labels = {"63", "125", "250", "500", "1k", "2k", "4k", "8k", "16k"};
        for (int i = 0; i < 9; i++) {
            sb.append(String.format(Locale.ROOT, "%5s Hz  %+5.1f dB\n", labels[i], mResult.soundEq[i]));
        }
        if (mResult.fir[0] != 0 || mResult.fir[1] != 0) {
            sb.append('\n').append(getString(R.string.result_subcut,
                    mResult.fir[0], mResult.fir[1]));
        }
        mResultText.setText(sb.toString());
        mResultBox.setVisibility(View.VISIBLE);
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
            mShare.setEnabled(true);
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
