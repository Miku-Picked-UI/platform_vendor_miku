/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.speakercal;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

/**
 * Plays the sweep to the media output (the speaker it's routed to) while recording the
 * microphones in stereo. On phones with two mics the stereo capture holds both: channel 0 is
 * usually the bottom mic, channel 1 the top (earpiece) one.
 */
final class Measurer {

    static final int MIC_BOTTOM = 0, MIC_TOP = 1, MIC_BOTH = 2;

    interface Listener {
        /** Called often while measuring, from the measuring thread. */
        void onProgress(float progress, double sweepHz, float micLevel);
    }

    static final class Recording {
        float[] signal; // the chosen mic(s), mono
        int clipped;
        float[] channelPeak = new float[2];
    }

    private volatile boolean mCancelled;

    void cancel() {
        mCancelled = true;
    }

    boolean isCancelled() {
        return mCancelled;
    }

    /** Blocks for about 11 seconds. Throws if the microphone can't be opened. */
    Recording run(double amp, int mic, Listener l) throws Exception {
        final int rate = Dsp.RATE;
        final float[] sweep = Dsp.sweep(amp);
        final int pre = (int) (Dsp.PRE_S * rate), post = (int) (Dsp.POST_S * rate);
        final int playFrames = pre + sweep.length + post;
        final int recFrames = playFrames + rate;

        final int minBuf = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord rec = null;
        for (int src : new int[] {MediaRecorder.AudioSource.UNPROCESSED,
                MediaRecorder.AudioSource.VOICE_RECOGNITION, MediaRecorder.AudioSource.MIC}) {
            try {
                rec = new AudioRecord(src, rate, AudioFormat.CHANNEL_IN_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT, Math.max(minBuf, rate * 4));
                if (rec.getState() == AudioRecord.STATE_INITIALIZED) break;
                rec.release();
            } catch (RuntimeException e) {
                // try the next source
            }
            rec = null;
        }
        if (rec == null) throw new IllegalStateException("microphone");

        final AudioTrack track = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setSampleRate(rate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build())
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(rate * 2 * 4 / 2)
                .build();

        final short[] pcm = new short[recFrames * 2];
        final AudioRecord r = rec;
        final long[] playedAt = {0};
        final Thread player = new Thread(() -> {
            final int chunk = 2400;
            final float[] buf = new float[chunk * 2];
            track.play();
            for (int f = 0; f < playFrames && !mCancelled; f += chunk) {
                final int c = Math.min(chunk, playFrames - f);
                for (int i = 0; i < c; i++) {
                    final int si = f + i - pre;
                    final float v = si >= 0 && si < sweep.length ? sweep[si] : 0f;
                    buf[i * 2] = buf[i * 2 + 1] = v;
                }
                track.write(buf, 0, c * 2, AudioTrack.WRITE_BLOCKING);
            }
            playedAt[0] = 1;
        }, "SpeakerCal-play");

        rec.startRecording();
        player.start();
        int got = 0;
        final int chunk = 2400 * 2;
        while (got < pcm.length && !mCancelled) {
            final int c = r.read(pcm, got, Math.min(chunk, pcm.length - got));
            if (c <= 0) break;
            // Live level of the chosen mic(s), and where the sweep is, for the visual
            double sum = 0;
            int cnt = 0;
            for (int i = got; i + 1 < got + c; i += 2) {
                final double v = pick(pcm[i], pcm[i + 1], mic);
                sum += v * v;
                cnt++;
            }
            got += c;
            final float level = (float) Math.sqrt(sum / Math.max(1, cnt)) / 32768f;
            final int head = track.getPlaybackHeadPosition();
            final double t = (double) head / rate - Dsp.PRE_S;
            if (l != null) {
                l.onProgress(Math.min(1f, (float) got / pcm.length),
                        t < 0 ? 0 : Dsp.sweepFreqAt(t), level);
            }
        }
        player.join();
        track.stop();
        track.release();
        rec.stop();
        rec.release();
        if (mCancelled) throw new InterruptedException();

        final Recording out = new Recording();
        out.signal = new float[recFrames];
        for (int i = 0; i < recFrames; i++) {
            final short a = pcm[i * 2], b = pcm[i * 2 + 1];
            out.signal[i] = (float) (pick(a, b, mic) / 32768.0);
            if (Math.abs(a) >= 32000 && mic != MIC_TOP) out.clipped++;
            if (Math.abs(b) >= 32000 && mic != MIC_BOTTOM) out.clipped++;
            out.channelPeak[0] = Math.max(out.channelPeak[0], Math.abs(a) / 32768f);
            out.channelPeak[1] = Math.max(out.channelPeak[1], Math.abs(b) / 32768f);
        }
        return out;
    }

    private static double pick(short ch0, short ch1, int mic) {
        switch (mic) {
            case MIC_BOTTOM:
                return ch0;
            case MIC_TOP:
                return ch1;
            default:
                return (ch0 + ch1) * 0.5;
        }
    }
}
