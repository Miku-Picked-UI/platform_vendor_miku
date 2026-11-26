/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.lyrics;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Full-screen lyrics for the active player: the album art blurred and darkened behind, the
 * lines scrolling with the song (the current one bright and large, the others dim), tap a line
 * to jump there, and the song's controls at the bottom.
 */
public class LyricsActivity extends Activity {

    /** Optional: the player to follow (SystemUI passes the Now Bar's session package). */
    public static final String EXTRA_PACKAGE = "package";

    private static final float DIM = 0.38f;
    private static final float CURRENT_SCALE = 1.0f;
    private static final float OTHER_SCALE = 0.86f;
    private static final long ANIM_MS = 280;
    private static final long MAX_TICK_MS = 1000;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService mNetwork = Executors.newSingleThreadExecutor();

    private MediaSessionManager mSessions;
    private MediaController mController;
    private String mPreferredPackage;

    private ImageView mBackground;
    private TextView mTitle;
    private TextView mArtist;
    private ScrollView mScroll;
    private LinearLayout mLinesView;
    private TextView mStatus;
    private ImageButton mPlayPause;

    private String mTrackKey;
    private long mFetchId;
    private Lrclib.Lyrics mLyrics;
    private final List<TextView> mLineViews = new ArrayList<>();
    private int mCurrent = -2;
    private boolean mUserScrolling;
    private long mUserScrollAt;

    private final MediaController.Callback mCallback = new MediaController.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            showTrack(metadata);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            updatePlayPause();
            tick();
        }

        @Override
        public void onSessionDestroyed() {
            mHandler.post(LyricsActivity.this::pickSession);
        }
    };

    private final MediaSessionManager.OnActiveSessionsChangedListener mSessionsListener =
            controllers -> pickSession();

    private final Runnable mTick = this::tick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setDecorFitsSystemWindows(false);
        mSessions = getSystemService(MediaSessionManager.class);
        mPreferredPackage = getIntent().getStringExtra(EXTRA_PACKAGE);
        setContentView(buildViews());
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        mPreferredPackage = intent.getStringExtra(EXTRA_PACKAGE);
        pickSession();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            mSessions.addOnActiveSessionsChangedListener(mSessionsListener, null, mHandler);
        } catch (SecurityException e) {
            // Without MEDIA_CONTENT_CONTROL there is nothing to follow.
        }
        pickSession();
    }

    @Override
    protected void onStop() {
        mSessions.removeOnActiveSessionsChangedListener(mSessionsListener);
        setController(null);
        mHandler.removeCallbacks(mTick);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mNetwork.shutdownNow();
        super.onDestroy();
    }

    // ---- Views

    private int dp(float value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics()));
    }

    private View buildViews() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        mBackground = new ImageView(this);
        mBackground.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mBackground.setRenderEffect(RenderEffect.createBlurEffect(dp(60), dp(60),
                Shader.TileMode.CLAMP));
        mBackground.setColorFilter(Color.argb(0x8C, 0, 0, 0));
        root.addView(mBackground, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        root.addView(column, new FrameLayout.LayoutParams(-1, -1));

        // Header: song, artist, close.
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(24), dp(12), dp(12), dp(8));
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        mTitle = text(20, Typeface.BOLD, 1f);
        mArtist = text(15, Typeface.NORMAL, 0.7f);
        mTitle.setSingleLine(true);
        mTitle.setEllipsize(TextUtils.TruncateAt.END);
        mArtist.setSingleLine(true);
        mArtist.setEllipsize(TextUtils.TruncateAt.END);
        texts.addView(mTitle);
        texts.addView(mArtist);
        header.addView(texts, new LinearLayout.LayoutParams(0, -2, 1f));
        ImageButton close = button(R.drawable.ic_close, R.string.lyrics_close, 44);
        close.setOnClickListener(v -> finish());
        header.addView(close);
        column.addView(header, new LinearLayout.LayoutParams(-1, -2));

        // Lyrics.
        FrameLayout middle = new FrameLayout(this);
        mScroll = new ScrollView(this);
        mScroll.setVerticalScrollBarEnabled(false);
        mScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mScroll.setFadingEdgeLength(dp(80));
        mScroll.setVerticalFadingEdgeEnabled(true);
        mScroll.setOnScrollChangeListener((v, x, y, ox, oy) -> {
            if (mUserScrolling) mUserScrollAt = SystemClock.uptimeMillis();
        });
        mScroll.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            if (action == android.view.MotionEvent.ACTION_DOWN) mUserScrolling = true;
            if (action == android.view.MotionEvent.ACTION_UP
                    || action == android.view.MotionEvent.ACTION_CANCEL) {
                mUserScrolling = false;
                mUserScrollAt = SystemClock.uptimeMillis();
            }
            return false;
        });
        mLinesView = new LinearLayout(this);
        mLinesView.setOrientation(LinearLayout.VERTICAL);
        mScroll.addView(mLinesView, new FrameLayout.LayoutParams(-1, -2));
        middle.addView(mScroll, new FrameLayout.LayoutParams(-1, -1));
        mStatus = text(17, Typeface.NORMAL, 0.7f);
        mStatus.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams statusParams = new FrameLayout.LayoutParams(-1, -2,
                Gravity.CENTER);
        middle.addView(mStatus, statusParams);
        column.addView(middle, new LinearLayout.LayoutParams(-1, 0, 1f));

        // Controls.
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);
        controls.setPadding(0, dp(8), 0, dp(12));
        ImageButton prev = button(R.drawable.ic_prev, R.string.lyrics_previous, 56);
        mPlayPause = button(R.drawable.ic_play, R.string.lyrics_play_pause, 72);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.argb(0x33, 0xFF, 0xFF, 0xFF));
        mPlayPause.setBackground(circle);
        ImageButton next = button(R.drawable.ic_next, R.string.lyrics_next, 56);
        prev.setOnClickListener(v -> {
            if (mController != null) mController.getTransportControls().skipToPrevious();
        });
        next.setOnClickListener(v -> {
            if (mController != null) mController.getTransportControls().skipToNext();
        });
        mPlayPause.setOnClickListener(v -> {
            if (mController == null) return;
            if (isPlaying()) {
                mController.getTransportControls().pause();
            } else {
                mController.getTransportControls().play();
            }
        });
        LinearLayout.LayoutParams gap = new LinearLayout.LayoutParams(-2, -2);
        gap.setMargins(dp(20), 0, dp(20), 0);
        controls.addView(prev);
        controls.addView(mPlayPause, gap);
        controls.addView(next);
        column.addView(controls, new LinearLayout.LayoutParams(-1, -2));
        TextView source = text(11, Typeface.NORMAL, 0.4f);
        source.setText(R.string.lyrics_source);
        source.setGravity(Gravity.CENTER);
        column.addView(source, new LinearLayout.LayoutParams(-1, -2));

        root.setOnApplyWindowInsetsListener((v, insets) -> {
            android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars()
                    | WindowInsets.Type.displayCutout());
            column.setPadding(0, bars.top, 0, bars.bottom + dp(8));
            return WindowInsets.CONSUMED;
        });
        return root;
    }

    private TextView text(float sp, int style, float alpha) {
        TextView view = new TextView(this);
        view.setTextColor(Color.WHITE);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setTypeface(Typeface.create(Typeface.DEFAULT, style));
        view.setAlpha(alpha);
        return view;
    }

    private ImageButton button(int icon, int description, int sizeDp) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(icon);
        button.setContentDescription(getString(description));
        button.setBackground(null);
        button.setScaleType(ImageView.ScaleType.CENTER);
        button.setLayoutParams(new LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp)));
        return button;
    }

    // ---- Media session

    private void pickSession() {
        List<MediaController> controllers;
        try {
            controllers = mSessions.getActiveSessions(null);
        } catch (SecurityException e) {
            controllers = new ArrayList<>();
        }
        MediaController pick = null;
        for (MediaController c : controllers) {
            if (c.getPackageName().equals(mPreferredPackage)) {
                pick = c;
                break;
            }
        }
        if (pick == null) {
            for (MediaController c : controllers) {
                PlaybackState s = c.getPlaybackState();
                if (s != null && s.getState() == PlaybackState.STATE_PLAYING) {
                    pick = c;
                    break;
                }
            }
        }
        if (pick == null && !controllers.isEmpty()) pick = controllers.get(0);
        setController(pick);
    }

    private void setController(MediaController controller) {
        if (mController != null && controller != null
                && mController.getSessionToken().equals(controller.getSessionToken())) {
            return;
        }
        if (mController != null) mController.unregisterCallback(mCallback);
        mController = controller;
        if (controller == null) {
            if (!isFinishing()) showNothing();
            return;
        }
        controller.registerCallback(mCallback, mHandler);
        showTrack(controller.getMetadata());
        updatePlayPause();
    }

    private void showNothing() {
        mTrackKey = null;
        mFetchId++;
        mLyrics = null;
        mTitle.setText("");
        mArtist.setText("");
        mBackground.setImageDrawable(null);
        setLines(null);
        mStatus.setText(R.string.lyrics_nothing_playing);
    }

    private void showTrack(MediaMetadata metadata) {
        if (metadata == null) {
            showNothing();
            return;
        }
        String title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
        String artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
        if (TextUtils.isEmpty(artist)) {
            artist = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
        }
        String album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM);
        long duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
        mTitle.setText(title);
        mArtist.setText(artist);
        Bitmap art = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
        if (art == null) art = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
        mBackground.setImageBitmap(art);

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(artist)) {
            mTrackKey = null;
            setLines(null);
            mStatus.setText(R.string.lyrics_none);
            return;
        }
        String key = title + "\u0000" + artist + "\u0000" + (duration / 1000);
        if (key.equals(mTrackKey)) return;
        mTrackKey = key;
        long fetchId = ++mFetchId;
        mLyrics = null;
        setLines(null);
        mStatus.setText(R.string.lyrics_loading);
        final String t = title;
        final String a = artist;
        mNetwork.execute(() -> {
            Lrclib.Lyrics lyrics = Lrclib.fetch(t, a, album, duration);
            mHandler.post(() -> {
                if (fetchId != mFetchId || isDestroyed()) return;
                mLyrics = lyrics;
                if (lyrics == null) {
                    mStatus.setText(R.string.lyrics_none);
                    return;
                }
                mStatus.setText("");
                setLines(lyrics);
                tick();
            });
        });
    }

    private void setLines(Lrclib.Lyrics lyrics) {
        mLinesView.removeAllViews();
        mLineViews.clear();
        mCurrent = -2;
        if (lyrics == null) return;
        int pad = dp(28);
        // Room above the first and below the last line, so either can sit in the middle.
        View top = new View(this);
        mLinesView.addView(top, new LinearLayout.LayoutParams(-1, dp(200)));
        for (int i = 0; i < lyrics.lines.size(); i++) {
            Lrclib.Line line = lyrics.lines.get(i);
            TextView view = text(28, Typeface.BOLD, lyrics.synced ? DIM : 0.9f);
            view.setText(TextUtils.isEmpty(line.text) ? "♪" : line.text);
            view.setPadding(pad, dp(10), pad, dp(10));
            view.setPivotX(0f);
            if (lyrics.synced) {
                view.setScaleX(OTHER_SCALE);
                view.setScaleY(OTHER_SCALE);
                final long time = line.timeMs;
                view.setOnClickListener(v -> {
                    if (mController != null) {
                        mController.getTransportControls().seekTo(time);
                        mUserScrollAt = 0;
                    }
                });
            }
            mLinesView.addView(view, new LinearLayout.LayoutParams(-1, -2));
            mLineViews.add(view);
        }
        View bottom = new View(this);
        mLinesView.addView(bottom, new LinearLayout.LayoutParams(-1, dp(320)));
        if (!lyrics.synced) {
            mStatus.setText("");
            TextView note = text(13, Typeface.NORMAL, 0.6f);
            note.setText(R.string.lyrics_unsynced);
            note.setPadding(pad, 0, pad, 0);
            mLinesView.addView(note, 1);
        }
    }

    // ---- Following the song

    private boolean isPlaying() {
        PlaybackState state = mController != null ? mController.getPlaybackState() : null;
        return state != null && state.getState() == PlaybackState.STATE_PLAYING;
    }

    private void updatePlayPause() {
        mPlayPause.setImageResource(isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private long position() {
        PlaybackState state = mController != null ? mController.getPlaybackState() : null;
        if (state == null) return 0;
        long position = state.getPosition();
        if (state.getState() == PlaybackState.STATE_PLAYING) {
            position += (long) ((SystemClock.elapsedRealtime()
                    - state.getLastPositionUpdateTime()) * state.getPlaybackSpeed());
        }
        return position;
    }

    private void tick() {
        mHandler.removeCallbacks(mTick);
        Lrclib.Lyrics lyrics = mLyrics;
        if (lyrics == null || !lyrics.synced || mLineViews.isEmpty()) return;
        long pos = position();
        int index = -1;
        for (int i = 0; i < lyrics.lines.size(); i++) {
            if (lyrics.lines.get(i).timeMs <= pos) index = i; else break;
        }
        if (index != mCurrent) {
            int previous = mCurrent;
            mCurrent = index;
            if (previous >= 0 && previous < mLineViews.size()) highlight(mLineViews.get(previous), false);
            if (index >= 0) {
                TextView view = mLineViews.get(index);
                highlight(view, true);
                // Keep the current line a third down the screen, unless the user is browsing.
                if (!mUserScrolling && SystemClock.uptimeMillis() - mUserScrollAt > 3000) {
                    view.post(() -> mScroll.smoothScrollTo(0,
                            Math.max(0, view.getTop() - mScroll.getHeight() / 3)));
                }
            }
        }
        if (isPlaying()) {
            long delay = MAX_TICK_MS;
            if (index + 1 < lyrics.lines.size()) {
                delay = Math.min(delay, Math.max(16, lyrics.lines.get(index + 1).timeMs - pos));
            }
            mHandler.postDelayed(mTick, delay);
        }
    }

    private void highlight(TextView view, boolean current) {
        view.animate()
                .alpha(current ? 1f : DIM)
                .scaleX(current ? CURRENT_SCALE : OTHER_SCALE)
                .scaleY(current ? CURRENT_SCALE : OTHER_SCALE)
                .setDuration(ANIM_MS)
                .start();
    }
}
