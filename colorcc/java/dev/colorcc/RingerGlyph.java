package dev.colorcc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import dev.colorcc.Logic;
import dev.colorcc.Motion;

/* loaded from: classes.dex */
final class RingerGlyph extends View {
    private static final float BELL_PIVOT_Y = 3.0f;
    private static final float BUZZ_KICK = 80.0f;
    private static final float BUZZ_REACH = 1.6f;
    static final int SILENT = 2;
    private static final float SILENT_KICK_DEGREES = 300.0f;
    static final int SOUND = 0;
    private static final float SWING_KICK_DEGREES = 620.0f;
    static final int VIBRATE = 1;
    private int color;
    private final Path detail;
    private final int kind;
    private final Path main;
    private final Path outer;
    private final Paint paint;
    private final float size;
    private final Motion.Value slash;
    private final Paint slashPaint;
    private final Motion.Value swing;

    RingerGlyph(Context context, Motion motion, int i, float f) {
        super(context);
        Paint paint = new Paint(1);
        this.paint = paint;
        Paint paint2 = new Paint(1);
        this.slashPaint = paint2;
        this.color = -1;
        this.kind = i;
        this.size = f;
        paint.setColor(-1);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeCap(Paint.Cap.ROUND);
        paint2.setStrokeWidth(2.0f);
        if (i == 1) {
            this.main = Icons.path("M16.5,3h-9C6.67,3 6,3.67 6,4.5v15c0,0.83 0.67,1.5 1.5,1.5h9c0.83,0 1.5,-0.67 1.5,-1.5v-15c0,-0.83 -0.67,-1.5 -1.5,-1.5zM16,19H8V5h8v14z");
            this.detail = Icons.path("M3,17h2V7H3v10zM19,17h2V7h-2v10z");
            this.outer = Icons.path("M0,15h2V9H0v6zM22,9v6h2V9h-2z");
        } else {
            this.main = Icons.path("M18,16v-5c0,-3.07 -1.63,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.64,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z");
            this.detail = Icons.path("M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2z");
            this.outer = null;
        }
        this.swing = motion.value(i == 1 ? Logic.Spring.BUZZ : Logic.Spring.SWING, new Motion.Listener() { // from class: dev.colorcc.RingerGlyph.0
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f2, float f3) {
                RingerGlyph.this.m218lambda$new$0$devcolorccRingerGlyph(f2, f3);
            }
        });
        Motion.Value value = motion.value(Logic.Spring.SNAPPY, new Motion.Listener() { // from class: dev.colorcc.RingerGlyph.1
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f2, float f3) {
                RingerGlyph.this.m219lambda$new$1$devcolorccRingerGlyph(f2, f3);
            }
        });
        this.slash = value;
        value.snap(1.0f);
    }

    /* renamed from: lambda$new$0$dev-colorcc-RingerGlyph, reason: not valid java name */
    /* synthetic */ void m218lambda$new$0$devcolorccRingerGlyph(float f, float f2) {
        invalidate();
    }

    /* renamed from: lambda$new$1$dev-colorcc-RingerGlyph, reason: not valid java name */
    /* synthetic */ void m219lambda$new$1$devcolorccRingerGlyph(float f, float f2) {
        invalidate();
    }

    void setColor(int i) {
        if (i == this.color) {
            return;
        }
        this.color = i;
        this.paint.setColor(i);
        invalidate();
    }

    void play() {
        int i = this.kind;
        if (i == 1) {
            this.swing.kick(80.0f);
            return;
        }
        Motion.Value value = this.swing;
        if (i == 0) {
            value.kick(620.0f);
            return;
        }
        value.kick(300.0f);
        this.slash.snap(0.0f);
        this.slash.to(1.0f);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        int iSave = canvas.save();
        canvas.translate((getWidth() - this.size) / 2.0f, (getHeight() - this.size) / 2.0f);
        float f = this.size;
        canvas.scale(f / 24.0f, f / 24.0f);
        if (this.kind == 1) {
            drawVibrate(canvas);
        } else {
            drawBell(canvas);
        }
        canvas.restoreToCount(iSave);
    }

    private void drawBell(Canvas canvas) {
        float fValue = this.swing.value();
        int iSave = canvas.save();
        canvas.rotate(fValue, 12.0f, 3.0f);
        canvas.drawPath(this.main, this.paint);
        canvas.translate((-fValue) * 0.05f, 0.0f);
        canvas.drawPath(this.detail, this.paint);
        canvas.restoreToCount(iSave);
        if (this.kind == 2) {
            this.slashPaint.setColor(this.color);
            float fClamp = (Logic.clamp(this.slash.value()) * 17.0f) + 3.5f;
            canvas.drawLine(3.5f, 3.5f, fClamp, fClamp, this.slashPaint);
        }
    }

    private void drawVibrate(Canvas canvas) {
        float fValue = this.swing.value();
        float fClamp = Logic.clamp(Math.abs(fValue) / 1.6f);
        int iSave = canvas.save();
        canvas.translate(fValue, 0.0f);
        canvas.rotate(2.2f * fValue, 12.0f, 12.0f);
        canvas.drawPath(this.main, this.paint);
        canvas.restoreToCount(iSave);
        drawWaves(canvas, this.detail, (0.1f * fClamp) + 1.0f, 1.0f - (Logic.clamp((-fValue) / 1.6f) * 0.5f));
        drawWaves(canvas, this.outer, (fClamp * 0.14f) + 1.0f, 1.0f - (Logic.clamp(fValue / 1.6f) * 0.5f));
    }

    private void drawWaves(Canvas canvas, Path path, float f, float f2) {
        int iSave = canvas.save();
        canvas.scale(f, 1.0f, 12.0f, 12.0f);
        this.paint.setAlpha(Math.round(Color.alpha(this.color) * f2));
        canvas.drawPath(path, this.paint);
        this.paint.setColor(this.color);
        canvas.restoreToCount(iSave);
    }
}
