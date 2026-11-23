/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.edgelighting;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Exposes the edge lighting style to SystemUI.
 *
 * <p>{@code /state} has one row with the style, the selected image and its version (modification
 * time) so callers can tell when to reload it. {@code /image/<id>} is an image PNG, and the
 * {@code resolve} call returns the image id of the first keyword found in the passed text.
 */
public class EdgeLightingProvider extends ContentProvider {

    private static final String[] COLUMNS = {
            "enabled", "version", "selected", "effect", "size", "amount", "duration", "rotation",
            "app_icon", "screen_off_only"};

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (!"/state".equals(uri.getPath())) return null;
        Context c = getContext();
        String selected = EdgeLightingStore.selected(c);
        File image = EdgeLightingStore.image(c, selected);
        long version = image != null ? image.lastModified() : 0;
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        cursor.addRow(new Object[] {
                EdgeLightingStore.isEnabled(c) ? 1 : 0,
                version,
                selected,
                EdgeLightingStore.effect(c),
                EdgeLightingStore.level(c, EdgeLightingStore.KEY_SIZE, 2),
                EdgeLightingStore.level(c, EdgeLightingStore.KEY_AMOUNT, 1),
                EdgeLightingStore.level(c, EdgeLightingStore.KEY_DURATION, 1),
                EdgeLightingStore.level(c, EdgeLightingStore.KEY_ROTATION, 1),
                EdgeLightingStore.flag(c, EdgeLightingStore.KEY_APP_ICON, false) ? 1 : 0,
                EdgeLightingStore.flag(c, EdgeLightingStore.KEY_SCREEN_OFF_ONLY, true) ? 1 : 0});
        cursor.setNotificationUri(c.getContentResolver(), EdgeLightingStore.STATE_URI);
        return cursor;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (!"resolve".equals(method)) return null;
        Bundle result = new Bundle();
        result.putString("id", EdgeLightingStore.resolve(getContext(), arg));
        return result;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        List<String> segments = uri.getPathSegments();
        if (!"r".equals(mode) || segments.size() != 2 || !"image".equals(segments.get(0))
                || segments.get(1).contains("/") || segments.get(1).startsWith(".")) {
            throw new FileNotFoundException(uri.toString());
        }
        return ParcelFileDescriptor.open(EdgeLightingStore.image(getContext(), segments.get(1)),
                ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public String getType(Uri uri) {
        return uri.getPath() != null && uri.getPath().startsWith("/image/") ? "image/png" : null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
