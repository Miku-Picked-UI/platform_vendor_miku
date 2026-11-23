/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.speakercal;

/**
 * Signal processing for the calibration: the exponential sine sweep, Farina deconvolution into
 * an impulse response, the smoothed frequency response, and exact models of AxionFx's
 * equalizers (cascaded RBJ peaking biquads) to solve band gains with.
 */
final class Dsp {

    static final int RATE = 48000;
    static final double F0 = 20, F1 = 20000;
    static final double PRE_S = 0.5, POST_S = 3.0, SWEEP_S = 6.0;

    /** AxionFx's FIR EQ (15 bands, Q 1.4) and the Equalizer app's Sound EQ (9 bands, Q 1.41). */
    static final double[] FIR_FREQS = {25, 40, 63, 100, 160, 250, 400, 630, 1000, 1600, 2500,
            4000, 6300, 10000, 16000};
    static final double FIR_Q = 1.4;
    static final double[] SEQ_FREQS = {63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000};
    static final double SEQ_Q = 1.41;
    static final double SEQ_MAX_DB = 12;

    private Dsp() {}

    /** The sweep, mono, at amplitude {@code amp}, with fades at both ends. */
    static float[] sweep(double amp) {
        final int n = (int) (SWEEP_S * RATE);
        final float[] s = new float[n];
        final double k = Math.log(F1 / F0);
        final int fade = RATE / 20;
        for (int i = 0; i < n; i++) {
            final double t = (double) i / RATE;
            double v = Math.sin(2 * Math.PI * F0 * SWEEP_S / k * (Math.exp(t * k / SWEEP_S) - 1));
            if (i < fade) v *= 0.5 - 0.5 * Math.cos(Math.PI * i / fade);
            if (i > n - fade) v *= 0.5 - 0.5 * Math.cos(Math.PI * (n - i) / fade);
            s[i] = (float) (v * amp);
        }
        return s;
    }

    /** Instantaneous sweep frequency {@code t} seconds into the sweep. */
    static double sweepFreqAt(double t) {
        if (t <= 0) return F0;
        if (t >= SWEEP_S) return F1;
        return F0 * Math.exp(t * Math.log(F1 / F0) / SWEEP_S);
    }

    /** Result of one measurement: smoothed level in dB on {@link #GRID}, and the IR peak. */
    static final class Response {
        final double[] db = new double[GRID.length];
        double peakDb;
    }

    /** 1/12-octave grid from 20 Hz to 20 kHz, the curves are drawn and solved on. */
    static final double[] GRID;

    static {
        final int n = (int) Math.round(12 * Math.log(F1 / F0) / Math.log(2)) + 1;
        GRID = new double[n];
        for (int i = 0; i < n; i++) GRID[i] = F0 * Math.pow(2, i / 12.0);
    }

    /**
     * Deconvolves {@code rec} (the recording, starting at the same time as playback) with the
     * sweep played at {@code amp} and returns the 1/3-octave smoothed response.
     */
    static Response measure(float[] rec, double amp) {
        final float[] s = sweep(amp);
        final int n = s.length;
        final double k = Math.log(F1 / F0);
        // Inverse filter: time-reversed sweep, -6 dB/octave
        final int len = rec.length + n - 1;
        int nfft = 1;
        while (nfft < len) nfft <<= 1;
        final double[] ar = new double[nfft], ai = new double[nfft];
        final double[] br = new double[nfft], bi = new double[nfft];
        for (int i = 0; i < rec.length; i++) ar[i] = rec[i];
        for (int i = 0; i < n; i++) {
            final double t = (double) i / RATE;
            br[i] = s[n - 1 - i] * Math.exp(-t * k / SWEEP_S);
        }
        fft(ar, ai, false);
        fft(br, bi, false);
        for (int i = 0; i < nfft; i++) {
            final double re = ar[i] * br[i] - ai[i] * bi[i];
            final double im = ar[i] * bi[i] + ai[i] * br[i];
            ar[i] = re;
            ai[i] = im;
        }
        fft(ar, ai, true);
        // ar is now the impulse response (plus distortion products before it)
        int peak = 0;
        double pv = 0;
        for (int i = 0; i < len; i++) {
            final double v = Math.abs(ar[i]);
            if (v > pv) {
                pv = v;
                peak = i;
            }
        }
        // 5 ms before the direct sound, 150 ms after: the speaker in the room at the mic
        final int a = Math.max(0, peak - RATE * 5 / 1000);
        final int b = Math.min(len, peak + RATE * 150 / 1000);
        final int segN = b - a;
        final int tail = RATE * 30 / 1000;
        final int m = 1 << 16;
        final double[] xr = new double[m], xi = new double[m];
        for (int i = 0; i < segN && i < m; i++) {
            double w = 1;
            if (i > segN - tail) w = 0.5 + 0.5 * Math.cos(Math.PI * (i - (segN - tail)) / tail);
            xr[i] = ar[a + i] * w;
        }
        fft(xr, xi, false);
        final double[] pow = new double[m / 2];
        for (int i = 0; i < m / 2; i++) pow[i] = xr[i] * xr[i] + xi[i] * xi[i];
        final Response r = new Response();
        r.peakDb = 20 * Math.log10(pv + 1e-20);
        final double df = (double) RATE / m;
        for (int g = 0; g < GRID.length; g++) {
            final double lo = GRID[g] * Math.pow(2, -1.0 / 6), hi = GRID[g] * Math.pow(2, 1.0 / 6);
            int i0 = Math.max(1, (int) Math.floor(lo / df)), i1 = Math.min(m / 2 - 1, (int) Math.ceil(hi / df));
            double sum = 0;
            int cnt = 0;
            for (int i = i0; i <= i1; i++) {
                sum += pow[i];
                cnt++;
            }
            r.db[g] = 10 * Math.log10(sum / Math.max(1, cnt) + 1e-30);
        }
        return r;
    }

    /** Mean level of {@code db} between 630 Hz and 2.5 kHz: the reference "mids". */
    static double midRef(double[] db) {
        double s = 0;
        int c = 0;
        for (int i = 0; i < GRID.length; i++) {
            if (GRID[i] >= 630 && GRID[i] <= 2500) {
                s += db[i];
                c++;
            }
        }
        return s / c;
    }

    /** In-place radix-2 complex FFT; {@code inverse} also scales by 1/n. */
    static void fft(double[] re, double[] im, boolean inverse) {
        final int n = re.length;
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >> 1;
            for (; (j & bit) != 0; bit >>= 1) j ^= bit;
            j ^= bit;
            if (i < j) {
                double t = re[i];
                re[i] = re[j];
                re[j] = t;
                t = im[i];
                im[i] = im[j];
                im[j] = t;
            }
        }
        for (int len = 2; len <= n; len <<= 1) {
            final double ang = 2 * Math.PI / len * (inverse ? 1 : -1);
            final double wr = Math.cos(ang), wi = Math.sin(ang);
            for (int i = 0; i < n; i += len) {
                double cr = 1, ci = 0;
                final int half = len >> 1;
                for (int j = 0; j < half; j++) {
                    final int u = i + j, v = u + half;
                    final double vr = re[v] * cr - im[v] * ci;
                    final double vi = re[v] * ci + im[v] * cr;
                    re[v] = re[u] - vr;
                    im[v] = im[u] - vi;
                    re[u] += vr;
                    im[u] += vi;
                    final double nr = cr * wr - ci * wi;
                    ci = cr * wi + ci * wr;
                    cr = nr;
                }
            }
        }
        if (inverse) {
            for (int i = 0; i < n; i++) {
                re[i] /= n;
                im[i] /= n;
            }
        }
    }

    /** Magnitude in dB at {@code f} of an RBJ peaking filter. */
    static double peakingDb(double fc, double q, double gainDb, double f) {
        if (gainDb == 0) return 0;
        final double A = Math.pow(10, gainDb / 40);
        final double w0 = 2 * Math.PI * fc / RATE;
        final double alpha = Math.sin(w0) / (2 * q);
        final double c = Math.cos(w0);
        final double b0 = 1 + alpha * A, b1 = -2 * c, b2 = 1 - alpha * A;
        final double a0 = 1 + alpha / A, a1 = -2 * c, a2 = 1 - alpha / A;
        final double w = 2 * Math.PI * f / RATE;
        final double cw = Math.cos(w), sw = Math.sin(w), c2 = Math.cos(2 * w), s2 = Math.sin(2 * w);
        final double nr = b0 + b1 * cw + b2 * c2, ni = -(b1 * sw + b2 * s2);
        final double dr = a0 + a1 * cw + a2 * c2, di = -(a1 * sw + a2 * s2);
        return 10 * Math.log10((nr * nr + ni * ni) / (dr * dr + di * di));
    }

    /** Summed response of a bank of peaking filters at {@code f}. */
    static double bankDb(double[] freqs, double q, double[] gains, double f) {
        double s = 0;
        for (int i = 0; i < freqs.length; i++) s += peakingDb(freqs[i], q, gains[i], f);
        return s;
    }

    /**
     * Sound EQ gains (dB, half-dB steps, within ±12) whose summed curve, on top of
     * {@code baseDb} (e.g. the FIR sub cut), best matches {@code wantDb} on the grid between
     * 50 Hz and 16 kHz.
     */
    static double[] solveSoundEq(double[] wantDb, double[] baseDb) {
        final double[] g = new double[SEQ_FREQS.length];
        for (int it = 0; it < 300; it++) {
            for (int b = 0; b < SEQ_FREQS.length; b++) {
                double err = 0;
                int c = 0;
                for (int i = 0; i < GRID.length; i++) {
                    final double f = GRID[i];
                    if (f < 50 || f > 16000) continue;
                    if (f < SEQ_FREQS[b] / 1.41 || f > SEQ_FREQS[b] * 1.41) continue;
                    err += wantDb[i] - baseDb[i] - bankDb(SEQ_FREQS, SEQ_Q, g, f);
                    c++;
                }
                if (c > 0) g[b] += 0.4 * err / c;
                g[b] = Math.max(-SEQ_MAX_DB, Math.min(SEQ_MAX_DB, g[b]));
            }
        }
        for (int b = 0; b < g.length; b++) g[b] = Math.round(g[b] * 2) / 2.0;
        return g;
    }
}
