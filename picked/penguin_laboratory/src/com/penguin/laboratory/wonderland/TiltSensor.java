/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.wonderland;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Reports how far the phone is tilted from the way it's held, from -1 to 1 on each axis.
 *
 * <p>The resting angle slowly follows the phone, so the scene settles back to centre however it
 * is held and only the movement shows.
 */
final class TiltSensor implements SensorEventListener {

    // Tilt, in radians, that reaches the full drift
    private static final float FULL_TILT_RAD = 0.35f;
    private static final float FOLLOW = 0.2f;
    private static final float RECENTRE = 0.012f;

    private final SensorManager mSensorManager;
    private final Sensor mSensor;
    private final float[] mRotation = new float[9];
    private final float[] mOrientation = new float[3];

    private boolean mRunning;
    private boolean mHasBaseline;
    private float mBasePitch, mBaseRoll;
    private float mTargetX, mTargetY;
    private float mX, mY;

    TiltSensor(Context context) {
        mSensorManager = context.getSystemService(SensorManager.class);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
    }

    void start() {
        if (mRunning || mSensor == null) return;
        mRunning = true;
        mHasBaseline = false;
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    void stop() {
        if (!mRunning) return;
        mRunning = false;
        mSensorManager.unregisterListener(this);
    }

    boolean isRunning() {
        return mRunning;
    }

    /** Eases towards the latest reading; call once a frame. Returns whether the tilt moved. */
    boolean step() {
        float x = mX + (mTargetX - mX) * FOLLOW;
        float y = mY + (mTargetY - mY) * FOLLOW;
        boolean moved = Math.abs(x - mX) > 1e-4f || Math.abs(y - mY) > 1e-4f;
        mX = x;
        mY = y;
        return moved;
    }

    float x() {
        return mX;
    }

    float y() {
        return mY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(mRotation, event.values);
        SensorManager.getOrientation(mRotation, mOrientation);
        float pitch = mOrientation[1];
        float roll = mOrientation[2];
        if (!mHasBaseline) {
            mBasePitch = pitch;
            mBaseRoll = roll;
            mHasBaseline = true;
        }
        mBasePitch += (pitch - mBasePitch) * RECENTRE;
        mBaseRoll += (roll - mBaseRoll) * RECENTRE;
        mTargetX = clamp((roll - mBaseRoll) / FULL_TILT_RAD);
        mTargetY = clamp((pitch - mBasePitch) / FULL_TILT_RAD);
    }

    private static float clamp(float v) {
        return Math.max(-1f, Math.min(1f, v));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
