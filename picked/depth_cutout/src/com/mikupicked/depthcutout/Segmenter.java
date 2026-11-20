/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.depthcutout;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Collections;

/**
 * Salient object segmentation with U²-Net (u2netp, Apache-2.0, Xuebin Qin et al.; the ONNX export
 * from rembg). Input: 1x3x320x320 float, RGB normalised with the ImageNet mean and std. Output 0
 * (d0, already through a sigmoid): 1x1x320x320, min-max normalised here, like rembg does.
 */
final class Segmenter implements AutoCloseable {

    static final int SIZE = 320;
    private static final String MODEL = "u2netp.onnx";
    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD = {0.229f, 0.224f, 0.225f};

    private final OrtEnvironment mEnv;
    private final OrtSession mSession;
    private final String mInput;

    Segmenter(Context context) throws IOException, OrtException {
        mEnv = OrtEnvironment.getEnvironment();
        final byte[] model;
        try (InputStream in = context.getAssets().open(MODEL)) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream(5 << 20);
            final byte[] buf = new byte[64 << 10];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            model = out.toByteArray();
        }
        try (OrtSession.SessionOptions options = new OrtSession.SessionOptions()) {
            options.setIntraOpNumThreads(
                    Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors())));
            mSession = mEnv.createSession(model, options);
        }
        mInput = mSession.getInputNames().iterator().next();
    }

    /** @param rgb SIZE*SIZE packed RGB bytes; @return SIZE*SIZE mask bytes, 255 = subject */
    byte[] run(byte[] rgb) throws OrtException {
        final int n = SIZE * SIZE;
        final FloatBuffer in = FloatBuffer.allocate(3 * n);
        for (int c = 0; c < 3; c++) {
            final float mean = MEAN[c], std = STD[c];
            for (int i = 0; i < n; i++) {
                in.put(c * n + i, ((rgb[i * 3 + c] & 0xff) / 255f - mean) / std);
            }
        }
        final float[] out = new float[n];
        try (OnnxTensor tensor = OnnxTensor.createTensor(mEnv, in, new long[] {1, 3, SIZE, SIZE});
                OrtSession.Result result = mSession.run(Collections.singletonMap(mInput, tensor))) {
            final OnnxValue value = result.get(0);
            ((OnnxTensor) value).getFloatBuffer().get(out);
        }
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (float v : out) {
            if (v < min) min = v;
            if (v > max) max = v;
        }
        final float range = Math.max(max - min, 1e-6f);
        final byte[] mask = new byte[n];
        for (int i = 0; i < n; i++) {
            mask[i] = (byte) Math.round((out[i] - min) / range * 255f);
        }
        return mask;
    }

    @Override
    public void close() {
        try {
            mSession.close();
        } catch (OrtException e) {
            // ignore
        }
    }
}
