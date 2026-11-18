package dev.colorcc;

import android.app.KeyguardManager;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
final class PageSwipe {
    private static final long FOLLOW_NS = 120000000;
    private static volatile float controls = 1.0f;
    private static volatile long reportedAt;
    static volatile boolean touching;
    private static final List<View> rows = new ArrayList();
    private static final Matrix matrix = new Matrix();
    static volatile boolean moveRows = true;

    private PageSwipe() {
    }

    static boolean following() {
        long j = reportedAt;
        if (j != 0) {
            return System.nanoTime() - j < 120000000 || touching || Hook.isSliding();
        }
        return false;
    }

    static float shown() {
        return controls;
    }

    static void onSlide(Object obj, Object[] objArr) {
        int intField = XposedHelpers.getIntField(obj, "panelTransitionState");
        Object obj2 = objArr[2];
        String strName = obj2 instanceof Enum ? ((Enum) obj2).name() : "";
        int i = 0;
        boolean z = strName.equals("LEFT") || strName.equals("RIGHT");
        if (intField != 2 || !z || ((Float) objArr[1]).floatValue() != 0.0f) {
            end();
            return;
        }
        float fClamp = Logic.clamp(((Float) objArr[0]).floatValue());
        if (((Integer) objArr[3]).intValue() != 0) {
            fClamp = 1.0f - fClamp;
        }
        controls = fClamp;
        boolean z2 = reportedAt == 0;
        reportedAt = System.nanoTime();
        View view = view(obj, "shadeRootView");
        if (z2 && moveRows) {
            collect(view);
        }
        if (view != null && !rows.isEmpty() && view.getTranslationX() != 0.0f) {
            view.setTranslationX(0.0f);
        }
        float f = 1.0f - controls;
        float f2 = Hook.isReversed() ? 1.0f : -1.0f;
        int width = view == null ? 0 : view.getRootView().getWidth();
        while (true) {
            List<View> list = rows;
            if (i >= list.size()) {
                return;
            }
            float fClamp2 = Logic.clamp(f);
            Matrix matrix2 = matrix;
            matrix2.setTranslate(width * f2 * (1.0f - fClamp2), 0.0f);
            list.get(i).setAnimationMatrix(matrix2);
            i++;
        }
    }

    private static void collect(View view) {
        rows.clear();
        if (view == null) {
            return;
        }
        KeyguardManager keyguardManager = (KeyguardManager) view.getContext().getSystemService(KeyguardManager.class);
        if (keyguardManager == null || !keyguardManager.isKeyguardLocked()) {
            int identifier = view.getResources().getIdentifier("notification_stack_scroller", "id", "com.android.systemui");
            View viewFindViewById = identifier == 0 ? null : view.getRootView().findViewById(identifier);
            if (viewFindViewById instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewFindViewById;
                int height = viewGroup.getRootView().getHeight();
                int[] iArr = new int[2];
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    View childAt = viewGroup.getChildAt(i);
                    if (childAt.getVisibility() == 0 && childAt.getWidth() != 0 && childAt.getHeight() != 0) {
                        childAt.getLocationOnScreen(iArr);
                        if (iArr[1] <= height) {
                            rows.add(childAt);
                        }
                    }
                }
            }
        }
    }

    private static void end() {
        if (reportedAt == 0) {
            return;
        }
        reportedAt = 0L;
        Iterator<View> it = rows.iterator();
        while (it.hasNext()) {
            it.next().setAnimationMatrix(null);
        }
        rows.clear();
    }

    private static View view(Object obj, String str) {
        Object objectField = XposedHelpers.getObjectField(obj, str);
        if (objectField == null) {
            return null;
        }
        return (View) XposedHelpers.callMethod(objectField, "getView", new Object[0]);
    }
}
