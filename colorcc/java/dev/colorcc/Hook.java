package dev.colorcc;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.WindowManagerGlobal;
import android.widget.FrameLayout;
import android.window.ScreenCaptureInternal;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * [Miku-Picked UI] ColorCC built into SystemUI.
 *
 * In the LSPosed module this class installed hooks into One UI's SystemUI and read its state by
 * reflection. Built into SystemUI, the host (com.android.systemui.picked.colorcc) places the
 * panel and tells it what it needs through {@link Bridge}; the rest of the module keeps calling
 * the same static methods it always called here.
 *
 * What One UI had and AOSP has no counterpart for is answered the way the module answered it
 * when that part of One UI was missing (it already tolerated each one): no split
 * notifications / controls pages, no Samsung window or captured blur (AOSP's own shade blur
 * stays behind the panel), no detail screens.
 */
public final class Hook {
    private static final String TAG = "ColorCC";
    private static final int MAX_CRASHES = 3;
    private static final File CRASHES =
            new File("/data/user_de/0/com.android.systemui/files/colorcc_crashes");
    private static final float[] BLUR = {0.075f, 0.13f, 0.25f};

    /** What the panel needs from SystemUI. Called on the main thread. */
    public interface Bridge {
        /** SystemUI's QSHost. */
        Object qsHost();

        /** SystemUI's AccessPointController (Wi-Fi entries for the Wi-Fi menu), or null. */
        Object accessPointController();

        /** How far the control centre is open, 0 to 1. */
        float expansion();

        /** Whether a stock Quick Settings screen (the tile editor) is showing over the panel. */
        boolean stockScreen();

        void openPowerMenu();

        /** The panel stopped (an error); SystemUI should show its stock panel again. */
        void onPanelGone();
    }

    static volatile Object host;
    /** One UI's SecQSPanelComposeAdapter; there is none on AOSP. */
    static volatile Object adapter;
    static volatile boolean detailClosing;
    static volatile boolean notifGlass;
    static volatile boolean samsungLiveSeen;
    static volatile int themeLogs;
    static volatile float peekClear = 1.0f;

    private static volatile Bridge bridge;
    private static volatile Panel panel;
    private static boolean crashWatch;

    private Hook() {}

    // ---- the host's side ----

    /** Whether ColorCC switched itself off after crashing SystemUI repeatedly. */
    public static boolean disabled() {
        return readCrashes() >= MAX_CRASHES;
    }

    /**
     * Makes the panel inside {@code container} (full size). Returns the panel, or null when it
     * could not be made or ColorCC is switched off; the host then shows its stock panel.
     */
    public static View attach(ViewGroup container, Bridge b) {
        if (disabled()) {
            log("disabled after repeated crashes. Delete " + CRASHES + " to re-enable.");
            return null;
        }
        watchCrashes();
        bridge = b;
        host = b.qsHost();
        return place(container);
    }

    /** Takes the panel out of {@code container} again. */
    public static void detach(ViewGroup container) {
        Panel p = panel;
        if (p != null && p.getParent() == container) {
            container.removeView(p);
            p.retire();
        }
        panel = null;
    }

    /** The shade moved; {@code fraction} as {@link Bridge#expansion}. */
    public static void onShadeExpansion(float fraction) {
        Panel p = panel;
        if (p != null) {
            p.onShadeExpansion(fraction);
        }
    }

    /** The shade is pulled past its end ({@code overExpansion} px, finger down if tracking). */
    public static void onOverExpansion(float overExpansion, boolean tracking) {
        Panel p = panel;
        if (p != null) {
            p.onOverExpansion(overExpansion, tracking);
        }
    }

    /** Back pressed while the shade is open; true when the panel used it (closed a menu). */
    public static boolean onBack() {
        Panel p = panel;
        return p != null && p.handleBack();
    }

    /** Whether the shade should leave this touch to the panel (tile pages, scrolling cards). */
    public static boolean blocksShadeSlide(MotionEvent event) {
        Panel p = panel;
        return p != null && p.blocksShadeSlide(event);
    }

    /** Whether the panel's cards can scroll further {@code down} (else up). */
    public static boolean canScrollCards(boolean down) {
        Panel p = panel;
        return p != null && p.canScrollCards(down);
    }

    /** Whether the panel is open and running. */
    public static boolean showing() {
        Panel p = panel;
        return p != null && p.showing();
    }

    // ---- the panel's side ----

    static boolean attached() {
        return bridge != null;
    }

    static float expansion() {
        Bridge b = bridge;
        return b == null ? 0.0f : b.expansion();
    }

    static boolean stockScreen() {
        Bridge b = bridge;
        return b != null && b.stockScreen();
    }

    /** The AccessPointController for Wi-Fi entries: SystemUI's own, or the tile's on One UI. */
    static Object accessPoints(Object tile) {
        Bridge b = bridge;
        Object controller = b == null ? null : b.accessPointController();
        return controller != null
                ? controller
                : XposedHelpers.getObjectField(tile, "accessPointController");
    }

    static void openPowerMenu() {
        Bridge b = bridge;
        if (b != null) {
            b.openPowerMenu();
        }
    }

    /**
     * The screen behind the shade at {@code width} x {@code height}, for the glass: the display
     * captured without the window {@code inWindow} is in (One UI: SemWindowManager.screenshot
     * below the status bar layer). Runs on the backdrop thread.
     */
    static Bitmap screenshot(View inWindow, int displayId, int width, int height) {
        ViewRootImpl root = inWindow.getViewRootImpl();
        SurfaceControl shade = root == null ? null : root.getSurfaceControl();
        if (shade == null || !shade.isValid() || inWindow.getDisplay() == null) {
            return null;
        }
        Point real = new Point();
        inWindow.getDisplay().getRealSize(real);
        // The module asks for the screen's own orientation; scale the capture to that size.
        boolean landscape = width > height;
        int realW = landscape ? Math.max(real.x, real.y) : Math.min(real.x, real.y);
        int realH = landscape ? Math.min(real.x, real.y) : Math.max(real.x, real.y);
        // Raw builder: CaptureArgs.Builder is self-typed (Builder<T extends Builder<T>>).
        @SuppressWarnings({"rawtypes", "unchecked"})
        ScreenCaptureInternal.CaptureArgs.Builder builder =
                new ScreenCaptureInternal.CaptureArgs.Builder();
        builder.setFrameScale(width / (float) realW, height / (float) realH);
        builder.setExcludeLayers(new SurfaceControl[] {shade});
        ScreenCaptureInternal.CaptureArgs args = builder.build();
        ScreenCaptureInternal.SynchronousScreenCaptureListener listener =
                ScreenCaptureInternal.createSyncCaptureListener();
        try {
            WindowManagerGlobal.getWindowManagerService().captureDisplay(displayId, args, listener);
        } catch (Exception e) {
            throw new IllegalStateException("screen not captured", e);
        }
        ScreenCaptureInternal.ScreenshotHardwareBuffer buffer = listener.getBuffer();
        return buffer == null ? null : buffer.asBitmap();
    }

    static float capturedShare(int i) {
        return BLUR[i];
    }

    /** Samsung's live window blur (SecPanelWindowBlurBinder); not on AOSP. */
    static boolean samsungLive() {
        return false;
    }

    /** ColorCC's own live window blur (Samsung's SemBlurInfo); not on AOSP. */
    static boolean liveBlurAvailable() {
        return false;
    }

    static boolean windowBlurBlocked() {
        return false;
    }

    static float blurCurve() {
        return -1.0f;
    }

    // Samsung's captured shade blur picture (SecCapturedBlurBitmapGenerator); AOSP blurs the
    // shade window itself, so there is no picture to fade, replace or redo.
    static void fadeShadeBlur(float f) {}

    static void staleShadeBlur() {}

    static void showShadeBlur(Bitmap bitmap) {}

    static void reblur() {}

    static void redoLiveBlur() {}

    // One UI's split notifications / controls pages (SecPanelSplitHelper); AOSP has one shade.
    static boolean splitEnabled() {
        return false;
    }

    static float shadeFraction(float f) {
        return f;
    }

    static boolean isSliding() {
        return false;
    }

    static int splitHeading() {
        return -1;
    }

    static int splitHeading(float f) {
        return -1;
    }

    static float downToControls() {
        return -1.0f;
    }

    static boolean isControlsPageState() {
        return true;
    }

    static int splitField(String str) {
        return -1;
    }

    static boolean slidingDown() {
        return false;
    }

    static boolean isReversed() {
        return false;
    }

    private static View place(ViewGroup viewGroup) {
        try {
            Panel panel2 = new Panel(viewGroup.getContext(), viewGroup);
            viewGroup.addView(panel2, new FrameLayout.LayoutParams(-1, -1));
            panel = panel2;
            return panel2;
        } catch (Throwable th) {
            panel = null;
            log("panel not created, stock panel stays: " + th);
            return null;
        }
    }

    static void remake(Panel panel2) {
        if (panel == panel2 && (panel2.getParent() instanceof ViewGroup)) {
            ViewGroup viewGroup = (ViewGroup) panel2.getParent();
            viewGroup.removeView(panel2);
            panel2.retire();
            if (place(viewGroup) == null) {
                gone();
            }
            log("panel made again for a new screen size");
        }
    }

    /** The panel stopped after an error (Panel.die). */
    static void gone() {
        Bridge b = bridge;
        if (b != null) {
            b.onPanelGone();
        }
    }

    // ---- crash safety: three SystemUI crashes from ColorCC switch it off ----

    private static synchronized void watchCrashes() {
        if (crashWatch) {
            return;
        }
        crashWatch = true;
        final Thread.UncaughtExceptionHandler next = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, throwable) -> {
                    if (isOurs(throwable)) {
                        writeCrashes(readCrashes() + 1);
                    }
                    if (next != null) {
                        next.uncaughtException(thread, throwable);
                    }
                });
    }

    private static boolean isOurs(Throwable th) {
        StringWriter stringWriter = new StringWriter();
        th.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString().contains("dev.colorcc.");
    }

    private static int readCrashes() {
        try {
            return Integer.parseInt(
                    new String(Files.readAllBytes(CRASHES.toPath()), StandardCharsets.UTF_8)
                            .trim());
        } catch (Throwable unused) {
            return 0;
        }
    }

    private static void writeCrashes(int i) {
        try {
            CRASHES.getParentFile().mkdirs();
            Files.write(CRASHES.toPath(), String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        } catch (Throwable th) {
            log("crash count not saved: " + th);
        }
    }

    static void clearCrashes() {
        CRASHES.delete();
    }

    static void log(String str) {
        Log.i(TAG, str);
    }
}
