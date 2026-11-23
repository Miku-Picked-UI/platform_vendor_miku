/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.glass

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Remembers the apps the framework has found a bottom bar in, by the sender's own identity. */
class TabBarSeenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pkg = sentFromPackage ?: return
        TabBarStore.addSeen(context, pkg)
    }
}
