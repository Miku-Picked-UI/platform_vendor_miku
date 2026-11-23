/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.speakercal;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Turns a measured speaker response and a chosen sound style into an AxionFx preset: the tone
 * goes into the Equalizer app's 9 bands (Sound EQ), the FIR EQ only cuts what the speaker can't
 * play below its bass limit, and the style picks the effects.
 */
final class Calibration {

    /** A sound style: target curve (dB relative to the mids, at the FIR band frequencies) and effects. */
    static final class Style {
        final int name, summary;
        final double[] target; // at Dsp.FIR_FREQS
        final int outputGain, limiter;
        final boolean tube, exciter, clarity, compressor;
        final int tubeDrive, tubeMix;

        Style(int name, int summary, double[] target, int outputGain, int limiter, boolean tube,
                int tubeDrive, int tubeMix, boolean exciter, boolean clarity, boolean compressor) {
            this.name = name;
            this.summary = summary;
            this.target = target;
            this.outputGain = outputGain;
            this.limiter = limiter;
            this.tube = tube;
            this.tubeDrive = tubeDrive;
            this.tubeMix = tubeMix;
            this.exciter = exciter;
            this.clarity = clarity;
            this.compressor = compressor;
        }
    }

    //                                   25  40  63  100 160 250 400 630 1k 1.6k 2.5k 4k 6.3k 10k 16k
    static final Style[] STYLES = {
        new Style(R.string.style_studio, R.string.style_studio_summary,
                new double[] {0, 0, 1, 1, 1, 0.5, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                95, -1, false, 0, 0, false, false, false),
        new Style(R.string.style_warm, R.string.style_warm_summary,
                new double[] {0, 0, 4, 4, 3, 1.5, 0.5, 0, 0, 0, 0, -0.5, -1, -1.5, -2},
                110, -1, true, 60, 20, false, false, false),
        new Style(R.string.style_party, R.string.style_party_summary,
                new double[] {0, 0, 9, 8, 6, 1.5, -4, -2.5, 0, 0, 0.5, 1, 2, 3, 3},
                180, 0, true, 100, 45, true, true, true),
        new Style(R.string.style_bass, R.string.style_bass_summary,
                new double[] {0, 0, 12, 10, 7, 3, 0, -1, 0, 0, 0, 0.5, 1, 1, 1},
                170, 0, true, 80, 30, false, false, true),
        new Style(R.string.style_vocal, R.string.style_vocal_summary,
                new double[] {0, 0, 2, 2, 1, -1.5, -1, 0, 1, 2, 2.5, 2, 1, 1, 0.5},
                110, -1, false, 0, 0, false, true, false),
    };

    /** What the calibration found and decided, for the graph and the preset. */
    static final class Result {
        double[] measured; // relative to mids, on Dsp.GRID
        double[] target; // on Dsp.GRID
        double[] corrected; // predicted measured + EQ, on Dsp.GRID
        double[] soundEq = new double[9]; // dB
        double[] fir = new double[15]; // dB (only the sub cut)
        double bassLimitHz;
    }

    /** Phone microphones exaggerate the top octaves; only half of the correction is trusted there. */
    private static final double TRUST_ABOVE_HZ = 6000;

    static Result solve(Dsp.Response r, Style style) {
        final double[] grid = Dsp.GRID;
        final int n = grid.length;
        final Result res = new Result();
        final double ref = Dsp.midRef(r.db);
        res.measured = new double[n];
        for (int i = 0; i < n; i++) res.measured[i] = r.db[i] - ref;

        // Bass limit: the lowest frequency where the speaker is within 10 dB of its mids
        double limit = 60;
        for (int i = 0; i < n; i++) {
            if (grid[i] >= 30 && res.measured[i] > -10) {
                limit = grid[i];
                break;
            }
        }
        res.bassLimitHz = limit;

        // Below the limit: no boosts, just a firm cut so the woofer isn't driven to distortion
        if (limit > 45) {
            res.fir[0] = -8;
            res.fir[1] = -10;
        } else if (limit > 32) {
            res.fir[0] = -6;
        }
        final double[] base = new double[n];
        for (int i = 0; i < n; i++) base[i] = Dsp.bankDb(Dsp.FIR_FREQS, Dsp.FIR_Q, res.fir, grid[i]);

        res.target = new double[n];
        final double[] want = new double[n];
        for (int i = 0; i < n; i++) {
            res.target[i] = interpTarget(style.target, grid[i]);
            double d = res.target[i] - res.measured[i];
            if (grid[i] < limit) d = 0; // can't be fixed with EQ
            if (grid[i] > TRUST_ABOVE_HZ) d *= 0.5;
            want[i] = Math.max(-12, Math.min(12, d));
        }
        res.soundEq = Dsp.solveSoundEq(want, base);

        res.corrected = new double[n];
        for (int i = 0; i < n; i++) {
            res.corrected[i] = res.measured[i] + base[i]
                    + Dsp.bankDb(Dsp.SEQ_FREQS, Dsp.SEQ_Q, res.soundEq, grid[i]);
        }
        // Show the corrected curve relative to its own mids, like the others
        final double cref = Dsp.midRef(res.corrected);
        for (int i = 0; i < n; i++) res.corrected[i] -= cref;
        return res;
    }

    /** Target at {@code f}, interpolated on a log-frequency axis between the band points. */
    private static double interpTarget(double[] t, double f) {
        final double[] fr = Dsp.FIR_FREQS;
        if (f <= fr[0]) return t[0];
        if (f >= fr[fr.length - 1]) return t[t.length - 1];
        for (int i = 0; i < fr.length - 1; i++) {
            if (f <= fr[i + 1]) {
                final double x = Math.log(f / fr[i]) / Math.log(fr[i + 1] / fr[i]);
                return t[i] + (t[i + 1] - t[i]) * x;
            }
        }
        return 0;
    }

    /**
     * The AxionFx preset. Lists every effect switch, because AxionFx keeps settings a preset
     * leaves out from whatever was loaded before.
     */
    static String presetJson(Result res, Style s) throws JSONException {
        final JSONObject o = new JSONObject();
        o.put("master_enabled", true);
        o.put("output_gain", s.outputGain);
        o.put("output_pan", 0);
        o.put("fir_eq_enabled", true);
        for (int i = 0; i < 15; i++) o.put("fir_eq_band_" + i, (int) Math.round(res.fir[i] * 10));
        o.put("eq_enabled", false);
        o.put("sound_eq_preset", 6); // Custom
        for (int i = 0; i < 9; i++) {
            final int v = (int) Math.round(res.soundEq[i] * 10);
            o.put("sound_eq_band_" + i, v);
            o.put("sound_eq_custom_" + i, v);
        }
        o.put("dynsys_enabled", false);
        o.put("mcomp_enabled", false);
        o.put("compressor_enabled", s.compressor);
        o.put("agc_enabled", false);
        o.put("limiter_enabled", true);
        o.put("limiter_threshold", s.limiter);
        o.put("tube_enabled", s.tube);
        if (s.tube) {
            o.put("tube_drive", s.tubeDrive);
            o.put("tube_mix", s.tubeMix);
        }
        o.put("exciter_enabled", s.exciter);
        if (s.exciter) {
            o.put("exciter_drive", 35);
            o.put("exciter_blend", 20);
            o.put("exciter_freq", 3000);
        }
        o.put("clarity_enabled", s.clarity);
        if (s.clarity) {
            o.put("clarity_mode", 1);
            o.put("clarity_amount", 50);
        }
        o.put("bass_enabled", false);
        o.put("spkopt_enabled", false);
        o.put("widener_enabled", false);
        o.put("reverb_enabled", false);
        o.put("crossfeed_enabled", false);
        o.put("surround_enabled", false);
        o.put("spatial_enabled", false);
        o.put("convolver_enabled", false);
        return o.toString(2);
    }
}
