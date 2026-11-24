/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.speakercal;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Published measurements to tune from instead of measuring: headphones and earbuds from AutoEq
 * (jaakkopasanen/AutoEq, its raw curve and the target it was measured against) and speakers from
 * spinorama.org (its automatic EQ, whose inverse is how the speaker deviates from neutral, and
 * the bass extension from its metadata).
 */
final class Library {

    static final int HEADPHONES = 0, SPEAKER = 1;

    private static final String AUTOEQ =
            "https://raw.githubusercontent.com/jaakkopasanen/AutoEq/master/results/";
    private static final String SPINORAMA = "https://www.spinorama.org/json/";
    private static final String CACHE = "models-v1.tsv";
    private static final long MAX_AGE_MS = 14L * 24 * 3600 * 1000;

    /** One entry of the model list. */
    static final class Model {
        final int kind;
        final String name, detail, source;
        /** AutoEq: the result folder. spinorama: "r3,r6,r9,r12|type:fc:gain:q;...". */
        final String ref;
        final String key, compact;

        Model(int kind, String name, String detail, String source, String ref) {
            this.kind = kind;
            this.name = name;
            this.detail = detail;
            this.source = source;
            this.ref = ref;
            key = normalize(name);
            compact = key.replace(" ", "");
        }
    }

    /** A model's response, ready for {@link Calibration#solve(Profile, Calibration.Style)}. */
    static final class Profile {
        final boolean headphones;
        final String name, source;
        /** dB relative to the mids on {@link Dsp#GRID}. */
        final double[] measured;
        /** The neutral target it was measured against, relative to the mids; null = flat. */
        final double[] neutral;

        Profile(boolean headphones, String name, String source, double[] measured, double[] neutral) {
            this.headphones = headphones;
            this.name = name;
            this.source = source;
            this.measured = measured;
            this.neutral = neutral;
        }
    }

    private Library() {}

    // -------------------------------------------------------------------------------------------
    // Model list
    // -------------------------------------------------------------------------------------------

    /** The model list: from the cache when it's recent, else downloaded (a few MB, once). */
    static List<Model> load(Context c) throws IOException {
        final File cache = new File(c.getFilesDir(), CACHE);
        if (cache.exists() && System.currentTimeMillis() - cache.lastModified() < MAX_AGE_MS) {
            try {
                return readCache(cache);
            } catch (IOException ignored) {
                // download again
            }
        }
        final List<Model> out = new ArrayList<>();
        IOException err = null;
        int sources = 0;
        try {
            out.addAll(downloadAutoEq());
            sources++;
        } catch (IOException e) {
            err = e;
        }
        try {
            out.addAll(downloadSpinorama());
            sources++;
        } catch (IOException e) {
            err = e;
        }
        if (sources == 2) {
            writeCache(cache, out);
        } else if (cache.exists()) {
            // Offline or a site is down: an old list is better than half a list
            try {
                return readCache(cache);
            } catch (IOException ignored) {
                // fall through
            }
        }
        if (out.isEmpty()) throw err != null ? err : new IOException("no models");
        return out;
    }

    /** Models whose name contains every word of {@code query}, best matches first. */
    static List<Model> search(List<Model> models, String query, int max) {
        final String q = normalize(query);
        final List<Model> out = new ArrayList<>();
        if (q.isEmpty()) return out;
        final String[] words = q.split(" ");
        final String qc = q.replace(" ", "");
        for (Model m : models) {
            boolean all = true;
            for (String w : words) {
                if (!m.key.contains(w)) {
                    all = false;
                    break;
                }
            }
            if (all || m.compact.contains(qc)) out.add(m);
        }
        out.sort((a, b) -> {
            final int sa = score(a, q), sb = score(b, q);
            if (sa != sb) return sa - sb;
            if (a.name.length() != b.name.length()) return a.name.length() - b.name.length();
            return a.name.compareToIgnoreCase(b.name);
        });
        return out.size() > max ? new ArrayList<>(out.subList(0, max)) : out;
    }

    private static int score(Model m, String q) {
        if (m.key.equals(q)) return 0;
        if (m.key.startsWith(q)) return 1;
        if (m.key.contains(" " + q)) return 2; // "buds" in "galaxy buds"
        return 3;
    }

    private static String normalize(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    private static List<Model> readCache(File f) throws IOException {
        final List<Model> out = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                final String[] p = line.split("\t", -1);
                if (p.length != 5) continue;
                out.add(new Model(p[0].equals("H") ? HEADPHONES : SPEAKER, p[1], p[2], p[3], p[4]));
            }
        }
        if (out.isEmpty()) throw new IOException("empty cache");
        return out;
    }

    private static void writeCache(File f, List<Model> models) {
        final File tmp = new File(f.getPath() + ".tmp");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
            for (Model m : models) {
                w.write((m.kind == HEADPHONES ? "H" : "S") + "\t" + clean(m.name) + "\t"
                        + clean(m.detail) + "\t" + clean(m.source) + "\t" + clean(m.ref) + "\n");
            }
        } catch (IOException e) {
            tmp.delete();
            return;
        }
        tmp.renameTo(f);
    }

    private static String clean(String s) {
        return s.replace('\t', ' ').replace('\n', ' ');
    }

    private static HttpURLConnection open(String url) throws IOException {
        final HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(15000);
        c.setReadTimeout(30000);
        c.setRequestProperty("User-Agent", "SpeakerCalibrator");
        // Android's HttpURLConnection asks for gzip and unpacks it by itself
        final int code = c.getResponseCode();
        if (code != HttpURLConnection.HTTP_OK) {
            c.disconnect();
            throw new IOException("HTTP " + code + " for " + url);
        }
        return c;
    }

    /**
     * AutoEq's results/INDEX.md, one line per result:
     * {@code - [Name](./source/rig type/Name) by source on rig}.
     */
    private static List<Model> downloadAutoEq() throws IOException {
        final List<Model> out = new ArrayList<>();
        final HttpURLConnection c = open(AUTOEQ + "INDEX.md");
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                c.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (!line.startsWith("- [")) continue;
                final int i = line.indexOf("](./");
                final int j = line.lastIndexOf(") by ");
                if (i < 0 || j < i) continue;
                final String name = line.substring(3, i);
                final String path = line.substring(i + 4, j);
                final String by = line.substring(j + 5).trim();
                final String[] seg = path.split("/");
                String type = seg.length >= 3 ? decode(seg[1]) : "";
                final int sp = type.lastIndexOf(' ');
                if (sp >= 0) type = type.substring(sp + 1); // "711 in-ear" -> "in-ear"
                final int on = by.indexOf(" on ");
                final String source = on >= 0 ? by.substring(0, on) : by;
                out.add(new Model(HEADPHONES, name, type.isEmpty() ? by : type + " · " + by,
                        source, path));
            }
        } finally {
            c.disconnect();
        }
        if (out.isEmpty()) throw new IOException("AutoEq index is empty");
        return out;
    }

    private static String decode(String s) {
        try {
            return java.net.URLDecoder.decode(s.replace("+", "%2B"), "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    /** spinorama.org: eqdata.json (each speaker's EQs) and metadata.json (shape, bass extension). */
    private static List<Model> downloadSpinorama() throws IOException {
        final Map<String, String> eqs = new HashMap<>();
        HttpURLConnection c = open(SPINORAMA + "eqdata.json");
        try (JsonReader r = reader(c.getInputStream())) {
            r.beginObject();
            while (r.hasNext()) {
                final String name = r.nextName();
                final String peq = readSpeakerEq(r);
                if (peq != null) eqs.put(name, peq);
            }
            r.endObject();
        } finally {
            c.disconnect();
        }

        final List<Model> out = new ArrayList<>();
        c = open(SPINORAMA + "metadata.json");
        try (JsonReader r = reader(c.getInputStream())) {
            r.beginObject();
            while (r.hasNext()) {
                final String name = r.nextName();
                final String[] meta = readSpeakerMeta(r); // shape, origin, refs
                final String peq = eqs.get(name);
                if (peq == null) continue;
                out.add(new Model(SPEAKER, name, shapeName(meta[0]),
                        "spinorama.org" + (meta[1].isEmpty() ? "" : " (" + meta[1] + ")"),
                        meta[2] + "|" + peq));
            }
            r.endObject();
        } finally {
            c.disconnect();
        }
        if (out.isEmpty()) throw new IOException("spinorama list is empty");
        return out;
    }

    private static JsonReader reader(InputStream in) {
        return new JsonReader(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
    }

    /** {"brand":..,"eqs":{"autoeq":{"peq":[{Q,dbGain,freq,type}..]}, ..}}: the autoeq one, else the first. */
    private static String readSpeakerEq(JsonReader r) throws IOException {
        String preferred = null, first = null;
        r.beginObject();
        while (r.hasNext()) {
            if (!r.nextName().equals("eqs") || r.peek() != JsonToken.BEGIN_OBJECT) {
                r.skipValue();
                continue;
            }
            r.beginObject();
            while (r.hasNext()) {
                final String eqName = r.nextName();
                final String peq = readPeqObject(r);
                if (peq == null) continue;
                if (eqName.equals("autoeq")) preferred = peq;
                if (first == null) first = peq;
            }
            r.endObject();
        }
        r.endObject();
        return preferred != null ? preferred : first;
    }

    private static String readPeqObject(JsonReader r) throws IOException {
        if (r.peek() != JsonToken.BEGIN_OBJECT) {
            r.skipValue();
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        r.beginObject();
        while (r.hasNext()) {
            if (!r.nextName().equals("peq") || r.peek() != JsonToken.BEGIN_ARRAY) {
                r.skipValue();
                continue;
            }
            r.beginArray();
            while (r.hasNext()) {
                double q = 1, gain = 0, freq = 0;
                int type = Dsp.PEAK;
                r.beginObject();
                while (r.hasNext()) {
                    final String k = r.nextName();
                    if (r.peek() != JsonToken.NUMBER) {
                        r.skipValue();
                        continue;
                    }
                    switch (k) {
                        case "Q": q = r.nextDouble(); break;
                        case "dbGain": gain = r.nextDouble(); break;
                        case "freq": freq = r.nextDouble(); break;
                        case "type": type = r.nextInt(); break;
                        default: r.skipValue();
                    }
                }
                r.endObject();
                if (freq <= 0 || q <= 0) continue;
                if (sb.length() > 0) sb.append(';');
                sb.append(type).append(':').append(freq).append(':').append(gain).append(':').append(q);
            }
            r.endArray();
        }
        r.endObject();
        return sb.length() > 0 ? sb.toString() : null;
    }

    /** {shape, origin, "r3,r6,r9,r12"} of a metadata.json speaker, from its default measurement. */
    private static String[] readSpeakerMeta(JsonReader r) throws IOException {
        String shape = "", def = null;
        final Map<String, String[]> ms = new HashMap<>(); // measurement -> {origin, refs}
        String firstKey = null;
        r.beginObject();
        while (r.hasNext()) {
            final String k = r.nextName();
            if (k.equals("shape") && r.peek() == JsonToken.STRING) {
                shape = r.nextString();
            } else if (k.equals("default_measurement") && r.peek() == JsonToken.STRING) {
                def = r.nextString();
            } else if (k.equals("measurements") && r.peek() == JsonToken.BEGIN_OBJECT) {
                r.beginObject();
                while (r.hasNext()) {
                    final String mk = r.nextName();
                    ms.put(mk, readMeasurement(r));
                    if (firstKey == null) firstKey = mk;
                }
                r.endObject();
            } else {
                r.skipValue();
            }
        }
        r.endObject();
        String[] m = def != null ? ms.get(def) : null;
        if (m == null && firstKey != null) m = ms.get(firstKey);
        if (m == null) m = new String[] {"", ""};
        return new String[] {shape, m[0], m[1]};
    }

    private static String[] readMeasurement(JsonReader r) throws IOException {
        String origin = "";
        final double[] refs = new double[4];
        if (r.peek() != JsonToken.BEGIN_OBJECT) {
            r.skipValue();
            return new String[] {"", ""};
        }
        r.beginObject();
        while (r.hasNext()) {
            final String k = r.nextName();
            if (k.equals("origin") && r.peek() == JsonToken.STRING) {
                origin = r.nextString();
            } else if (k.equals("estimates") && r.peek() == JsonToken.BEGIN_OBJECT) {
                r.beginObject();
                while (r.hasNext()) {
                    final String ek = r.nextName();
                    final int idx = ek.equals("ref_3dB") ? 0 : ek.equals("ref_6dB") ? 1
                            : ek.equals("ref_9dB") ? 2 : ek.equals("ref_12dB") ? 3 : -1;
                    if (idx >= 0 && r.peek() == JsonToken.NUMBER) refs[idx] = r.nextDouble();
                    else r.skipValue();
                }
                r.endObject();
            } else {
                r.skipValue();
            }
        }
        r.endObject();
        final String rs = refs[0] > 0
                ? refs[0] + "," + refs[1] + "," + refs[2] + "," + refs[3] : "";
        return new String[] {origin.replace("Misc", "").trim(), rs};
    }

    private static String shapeName(String s) {
        switch (s) {
            case "liveportable": return "portable speaker";
            case "bookshelves": return "bookshelf speaker";
            case "floorstanders": return "floorstanding speaker";
            case "center": return "centre speaker";
            case "toursound": return "PA speaker";
            case "inwall": return "in-wall speaker";
            case "omnidirectional": return "360° speaker";
            case "soundbar": return "soundbar";
            case "": return "speaker";
            default: return s + " speaker";
        }
    }

    // -------------------------------------------------------------------------------------------
    // One model's curve
    // -------------------------------------------------------------------------------------------

    /** The model's response: downloads the AutoEq curve, or computes it from spinorama's EQ. */
    static Profile profile(Model m) throws IOException {
        return m.kind == HEADPHONES ? autoEqProfile(m) : speakerProfile(m);
    }

    /** AutoEq's {name}.csv: frequency,raw,...,target — the bare headphone and its target. */
    private static Profile autoEqProfile(Model m) throws IOException {
        final String folder = m.ref;
        final String file = folder.substring(folder.lastIndexOf('/') + 1);
        final HttpURLConnection c = open(AUTOEQ + folder + "/" + file + ".csv");
        final List<double[]> rows = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                c.getInputStream(), StandardCharsets.UTF_8))) {
            final String header = r.readLine();
            if (header == null) throw new IOException("empty curve");
            final String[] cols = header.trim().split(",");
            int fi = -1, ri = -1, ti = -1;
            for (int i = 0; i < cols.length; i++) {
                if (cols[i].equals("frequency")) fi = i;
                else if (cols[i].equals("raw")) ri = i;
                else if (cols[i].equals("target")) ti = i;
            }
            if (fi < 0 || ri < 0) throw new IOException("curve has no raw column");
            String line;
            while ((line = r.readLine()) != null) {
                final String[] p = line.split(",");
                try {
                    final double f = Double.parseDouble(p[fi]);
                    final double raw = Double.parseDouble(p[ri]);
                    final double tgt = ti >= 0 && ti < p.length && !p[ti].isEmpty()
                            ? Double.parseDouble(p[ti]) : 0;
                    if (f > 0) rows.add(new double[] {f, raw, tgt});
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                    // skip
                }
            }
        } finally {
            c.disconnect();
        }
        if (rows.size() < 20) throw new IOException("curve is too short");
        final double[] raw = toGrid(rows, 1), target = toGrid(rows, 2);
        level(raw);
        level(target);
        return new Profile(true, m.name, m.source, raw, target);
    }

    /** Column {@code col} of {f, ...} rows, averaged over ±1/6 octave around each grid point. */
    private static double[] toGrid(List<double[]> rows, int col) {
        final double[] out = new double[Dsp.GRID.length];
        for (int g = 0; g < out.length; g++) {
            final double lo = Dsp.GRID[g] * Math.pow(2, -1.0 / 6), hi = Dsp.GRID[g] * Math.pow(2, 1.0 / 6);
            double s = 0;
            int n = 0;
            for (double[] row : rows) {
                if (row[0] >= lo && row[0] <= hi) {
                    s += row[col];
                    n++;
                }
            }
            out[g] = n > 0 ? s / n : nearest(rows, col, Dsp.GRID[g]);
        }
        return out;
    }

    private static double nearest(List<double[]> rows, int col, double f) {
        double best = Double.MAX_VALUE, v = 0;
        for (double[] row : rows) {
            final double d = Math.abs(Math.log(row[0] / f));
            if (d < best) {
                best = d;
                v = row[col];
            }
        }
        return v;
    }

    private static void level(double[] db) {
        final double ref = Dsp.midRef(db);
        for (int i = 0; i < db.length; i++) db[i] -= ref;
    }

    /**
     * spinorama's automatic EQ makes the speaker neutral, so the speaker is the EQ turned upside
     * down. Below its bass extension the EQ doesn't try, so the roll-off is modelled from the
     * -3 dB and -12 dB points as a high-pass of the matching steepness.
     */
    private static Profile speakerProfile(Model m) throws IOException {
        final int bar = m.ref.indexOf('|');
        if (bar < 0) throw new IOException("bad entry");
        final String refs = m.ref.substring(0, bar);
        final String[] fs = m.ref.substring(bar + 1).split(";");
        final double[][] filters = new double[fs.length][];
        for (int i = 0; i < fs.length; i++) {
            final String[] p = fs[i].split(":");
            filters[i] = new double[] {Integer.parseInt(p[0]), Double.parseDouble(p[1]),
                    Double.parseDouble(p[2]), Double.parseDouble(p[3])};
        }
        double f3 = 0, f12 = 0;
        if (!refs.isEmpty()) {
            final String[] r = refs.split(",");
            f3 = Double.parseDouble(r[0]);
            f12 = Double.parseDouble(r[3]);
        }
        // |H|² = 1 / (1 + (f3/f)^2n), so -3 dB at f3 and -12 dB at f12
        double twoN = 8;
        if (f3 > 0 && f12 > 0 && f12 < f3) {
            twoN = Math.log(Math.pow(10, 1.2) - 1) / Math.log(f3 / f12);
        }
        twoN = Math.max(4, Math.min(24, twoN));

        final double[] meas = new double[Dsp.GRID.length];
        for (int g = 0; g < meas.length; g++) {
            final double f = Dsp.GRID[g];
            double v = 0;
            for (double[] fl : filters) v -= Dsp.biquadDb((int) fl[0], fl[1], fl[3], fl[2], f);
            if (f3 > 21) v -= 10 * Math.log10(1 + Math.pow(f3 / f, twoN));
            meas[g] = v;
        }
        level(meas);
        return new Profile(false, m.name, m.source, meas, null);
    }
}
