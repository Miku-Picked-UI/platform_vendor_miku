package dev.colorcc;

import android.view.Choreographer;
import dev.colorcc.Logic;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
final class SideSlide implements Choreographer.FrameCallback {
    private static final float FADE = 1.5f;
    private static final long HOLD_NS = 1500000000;
    static final float LEAD_S = 0.03f;
    private static final float SIDE_AT = 0.0f;
    private static final long WAIT_NS = 1200000000;
    boolean away;
    private final Host host;
    private boolean introDone;
    private float lastFraction;
    private long lastNs;
    private boolean open;
    private boolean posted;
    private long qsSettledNs;
    private boolean qsSideNext;
    private long qsWaitUntil;
    private boolean qsWanted;
    boolean running;
    private float speed;
    private float tailSpeed;
    float target;
    private static final float[] SETTLE = Logic.Spring.of(0.34f, 0.15f);
    private static final float[] TAIL = Logic.Spring.of(0.24f, 0.35f);
    private static final float[] FOLLOW = Logic.Spring.of(0.15f, 0.0f);
    private final List<Piece> pieces = new ArrayList();
    private final float[] xv = new float[2];
    private float page = 1.0f;
    private float tail = 1.0f;
    float dir = 1.0f;

    interface Host {
        void apply();

        boolean drawn();

        boolean locked();

        void onAway();

        void onSlideIn();

        List<Piece> pieces();

        int rootHeight();

        boolean shown();

        boolean visible();
    }

    static final class Piece {
        float centerY;
        float lead;
        float topY;
        float x = 1.0f;

        Piece() {
        }
    }

    SideSlide(Host host) {
        this.host = host;
    }

    void onExpansion(float f) {
        float f2 = this.lastFraction;
        this.lastFraction = f;
        if (f <= 0.0f) {
            if (f2 > 0.0f) {
                this.open = false;
                this.qsWaitUntil = 0L;
                stop();
                return;
            }
            return;
        }
        if (f2 > 0.0f) {
            return;
        }
        stop();
        this.open = true;
        this.introDone = false;
        this.qsWanted = false;
        this.qsWaitUntil = 0L;
        this.lastNs = System.nanoTime();
        post();
    }

    boolean waitingToSlideIn() {
        return (this.running || this.qsWaitUntil == 0 || !this.qsSideNext) ? false : true;
    }

    private void post() {
        if (this.posted) {
            return;
        }
        this.posted = true;
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override // android.view.Choreographer.FrameCallback
    public void doFrame(long j) {
        this.posted = false;
        try {
            float fMin = Math.min(0.033333335f, Math.max(0.0f, (j - this.lastNs) / 1.0E9f));
            this.lastNs = j;
            if (this.open) {
                track(j);
            }
            if (this.running && !step(j, fMin)) {
                stop();
            }
            if (!this.open && !this.running) {
                return;
            }
            post();
        } catch (Throwable th) {
            Hook.log("side slide stopped: " + th);
            stop();
        }
    }

    private void track(long j) {
        boolean zIsControlsPageState;
        int iSplitField = Hook.splitField("currentState");
        boolean z = iSplitField == 2;
        boolean z2 = z && !Hook.slidingDown();
        if (iSplitField == 2) {
            int iSplitHeading = Hook.splitHeading(z2 ? 0.0f : 0.5f);
            if (iSplitHeading == 0 || iSplitHeading == 1) {
                iSplitField = iSplitHeading;
            }
        }
        if (iSplitField >= 0) {
            zIsControlsPageState = iSplitField == 0 || iSplitField == 3;
        } else {
            zIsControlsPageState = Hook.isControlsPageState();
        }
        boolean z3 = iSplitField == 1 || iSplitField == 3;
        boolean z4 = z2 || ((!z && iSplitField >= 0 && this.introDone && (this.lastFraction > 0.5f ? 1 : (this.lastFraction == 0.5f ? 0 : -1)) > 0) && !this.host.locked());
        if (zIsControlsPageState != this.qsWanted) {
            this.qsWanted = zIsControlsPageState;
            if (!zIsControlsPageState) {
                if (z4 || this.running) {
                    qsOut(j);
                } else {
                    this.qsWaitUntil = 0L;
                }
            } else if (z4 && this.introDone) {
                this.qsSideNext = true;
                if (this.running) {
                    qsBack(j);
                } else {
                    this.qsWaitUntil = 1200000000 + j;
                }
            } else if (z2 && this.introDone) {
                this.qsWaitUntil = 0L;
            } else {
                Hook.log("page change without a slide".concat(z ? ", sliding down" : ""));
                if (!z4 && this.running) {
                    stop();
                }
                this.qsSideNext = false;
                this.qsWaitUntil = 1200000000 + j;
            }
        }
        if ((zIsControlsPageState || z3) && this.qsWaitUntil == 0) {
            this.introDone = true;
        }
        if (!this.qsWanted || this.qsWaitUntil == 0) {
            return;
        }
        if (qsIn(j) || j > this.qsWaitUntil) {
            if (!this.running && this.qsSideNext && j > this.qsWaitUntil) {
                Hook.log("slide in skipped, page never shown");
            }
            this.qsWaitUntil = 0L;
        }
    }

    private boolean qsIn(long j) {
        if (this.running) {
            this.target = 1.0f;
            this.qsSettledNs = 0L;
            if (this.away) {
                this.host.onSlideIn();
            }
            this.away = false;
            return true;
        }
        if (!this.host.shown()) {
            return false;
        }
        if (!this.qsSideNext) {
            return true;
        }
        build(0.0f);
        Hook.log("slide in");
        this.target = 1.0f;
        this.qsSettledNs = 0L;
        this.away = false;
        this.running = true;
        this.host.onSlideIn();
        this.host.apply();
        return true;
    }

    private void qsBack(long j) {
        Hook.log("slide back".concat(this.away ? " after landing" : ""));
        this.target = 1.0f;
        this.qsSettledNs = 0L;
        if (this.away) {
            this.host.onSlideIn();
        }
        this.away = false;
    }

    private void qsOut(long j) {
        this.qsWaitUntil = 0L;
        Hook.log("slide out".concat(this.running ? " while running" : this.host.visible() ? "" : " skipped, panel not showing"));
        if (this.running) {
            this.target = 0.0f;
            this.qsSettledNs = 0L;
        } else if (this.host.visible()) {
            build(1.0f);
            this.target = 0.0f;
            this.qsSettledNs = 0L;
            this.away = false;
            this.running = true;
        }
    }

    private void build(float f) {
        this.pieces.clear();
        this.pieces.addAll(this.host.pieces());
        this.dir = Hook.isReversed() ? -1.0f : 1.0f;
        this.page = f;
        this.speed = 0.0f;
        this.tail = f;
        this.tailSpeed = 0.0f;
        Iterator<Piece> it = this.pieces.iterator();
        while (it.hasNext()) {
            it.next().x = f;
        }
    }

    void adopt(Piece piece) {
        if (this.running) {
            piece.x = this.page;
            this.pieces.add(piece);
        }
    }

    float page() {
        return this.page;
    }

    float speed() {
        return this.speed;
    }

    float tail() {
        return this.tail;
    }

    private boolean step(long j, float f) {
        if (this.target == 0.0f && !this.host.drawn()) {
            if (!this.away) {
                if (this.page > 0.1f) {
                    Hook.log("slide out cut at " + Math.round(this.page * 100.0f) + "%: Samsung hid its page");
                }
                this.host.onAway();
            }
            return false;
        }
        boolean zFollowing = PageSwipe.following();
        float fShown = zFollowing ? PageSwipe.shown() : this.target;
        float[] fArr = zFollowing ? FOLLOW : SETTLE;
        float[] fArr2 = this.xv;
        fArr2[0] = this.page;
        fArr2[1] = this.speed;
        float f2 = fArr[0];
        SpringStep.step(fArr2, fShown, f2, fArr[1] * 2.0f * ((float) Math.sqrt(f2)), f);
        float[] fArr3 = this.xv;
        float f3 = fArr3[0];
        this.page = f3;
        this.speed = fArr3[1];
        fArr3[0] = this.tail;
        fArr3[1] = this.tailSpeed;
        float[] fArr4 = TAIL;
        float f4 = fArr4[0];
        SpringStep.step(fArr3, f3, f4, fArr4[1] * 2.0f * ((float) Math.sqrt(f4)), f);
        float[] fArr5 = this.xv;
        this.tail = fArr5[0];
        this.tailSpeed = fArr5[1];
        boolean z = Math.abs(fShown - this.page) > 0.001f || Math.abs(this.speed) > 0.01f || Math.abs(this.page - this.tail) > 0.001f || Math.abs(this.tailSpeed) > 0.01f;
        Iterator<Piece> it = this.pieces.iterator();
        while (it.hasNext()) {
            it.next().x = this.page;
        }
        this.host.apply();
        if (z) {
            return true;
        }
        if (this.target == 1.0f) {
            return false;
        }
        if (this.qsSettledNs == 0) {
            this.qsSettledNs = j;
            this.away = true;
            this.host.onAway();
        }
        return j - this.qsSettledNs < 1500000000;
    }

    void stop() {
        boolean z = this.running;
        this.running = false;
        this.away = false;
        this.page = 1.0f;
        this.speed = 0.0f;
        this.tail = 1.0f;
        this.tailSpeed = 0.0f;
        Iterator<Piece> it = this.pieces.iterator();
        while (it.hasNext()) {
            it.next().x = 1.0f;
        }
        this.pieces.clear();
        if (z) {
            this.host.apply();
        }
    }

    float sideAt(float f, int i) {
        return i * this.dir * (1.0f - f);
    }

    static float alphaAt(float f) {
        return Math.max(0.0f, Math.min(1.0f, f * 1.5f));
    }
}
