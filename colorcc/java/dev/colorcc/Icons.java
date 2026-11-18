package dev.colorcc;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/* loaded from: classes.dex */
final class Icons {
    static final String BELL_BODY = "M18,16v-5c0,-3.07 -1.63,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.64,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z";
    static final String BELL_CLAPPER = "M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2z";
    static final String BLUETOOTH = "M17.71,7.71L12,2h-1v7.59L6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 11,14.41V22h1l5.71,-5.71 -4.3,-4.29 4.3,-4.29zM13,5.83l1.88,1.88L13,9.59V5.83zM14.88,16.29L13,18.17v-3.76l1.88,1.88z";
    static final String CLOSE = "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z";
    static final String CONTRAST = "M12,22c5.52,0 10,-4.48 10,-10S17.52,2 12,2 2,6.48 2,12s4.48,10 10,10zM13,4.07c3.94,0.49 7,3.85 7,7.93s-3.05,7.44 -7,7.93V4.07z";
    static final String GEAR = "M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z";
    static final String HEADPHONES = "M12,3c-4.97,0 -9,4.03 -9,9v7c0,1.1 0.9,2 2,2h4v-8H5v-1c0,-3.87 3.13,-7 7,-7s7,3.13 7,7v1h-4v8h4c1.1,0 2,-0.9 2,-2v-7c0,-4.97 -4.03,-9 -9,-9z";
    static final String KEY = "M12.65,10C11.83,7.67 9.61,6 7,6c-3.31,0 -6,2.69 -6,6s2.69,6 6,6c2.61,0 4.83,-1.67 5.65,-4H17v4h4v-4h2v-4H12.65zM7,14c-1.1,0 -2,-0.9 -2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2z";
    static final String LOCK = "M18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6v2H6c-1.1,0 -2,0.9 -2,2v10c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10c0,-1.1 -0.9,-2 -2,-2zM9,6c0,-1.66 1.34,-3 3,-3s3,1.34 3,3v2H9V6z";
    static final String MOON = "M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9 9,-4.03 9,-9c0,-0.46 -0.04,-0.92 -0.1,-1.36 -0.98,1.37 -2.58,2.26 -4.4,2.26 -2.98,0 -5.4,-2.42 -5.4,-5.4 0,-1.81 0.89,-3.42 2.26,-4.4C12.92,3.04 12.46,3 12,3z";
    static final String MUSIC = "M12,3v10.55c-0.59,-0.34 -1.27,-0.55 -2,-0.55 -2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4V7h4V3h-6z";
    static final String NEXT = "M6,18l8.5,-6L6,6v12zM16,6v12h2V6z";
    static final String OUTPUT = "M21,3H3C1.9,3 1,3.9 1,5v3h2V5h18v14h-7v2h7c1.1,0 2,-0.9 2,-2V5C23,3.9 22.1,3 21,3zM1,18v3h3C4,19.34 2.66,18 1,18zM1,14v2c2.76,0 5,2.24 5,5h2C8,17.13 4.87,14 1,14zM1,10v2c4.97,0 9,4.03 9,9h2C12,14.92 7.07,10 1,10z";
    static final String PAUSE = "M6,19h4V5H6v14zM14,5v14h4V5h-4z";
    static final String PENCIL = "M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z";
    static final String PHONE = "M16.5,3h-9C6.67,3 6,3.67 6,4.5v15c0,0.83 0.67,1.5 1.5,1.5h9c0.83,0 1.5,-0.67 1.5,-1.5v-15c0,-0.83 -0.67,-1.5 -1.5,-1.5zM16,19H8V5h8v14z";
    static final String PLAY = "M8,5v14l11,-7z";
    static final String POWER = "M13,3h-2v10h2L13,3zM17.83,5.17l-1.42,1.42C17.99,7.86 19,9.81 19,12c0,3.87 -3.13,7 -7,7s-7,-3.13 -7,-7c0,-2.19 1.01,-4.14 2.58,-5.42L6.17,5.17C4.23,6.82 3,9.26 3,12c0,4.97 4.03,9 9,9s9,-4.03 9,-9c0,-2.74 -1.23,-5.18 -3.17,-6.83z";
    static final String PREV = "M6,6h2v12H6zM9.5,12l8.5,6V6z";
    static final String SPEAKER = "M2,9v6h4l5,5V4L6,9H2z";
    static final String SUN = "M12,7c-2.76,0 -5,2.24 -5,5s2.24,5 5,5 5,-2.24 5,-5 -2.24,-5 -5,-5zM2,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1H2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM20,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1h-2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM11,2v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1V2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM11,20v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1v-2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM5.99,4.58c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0s0.39,-1.03 0,-1.41L5.99,4.58zM18.36,16.95c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0 0.39,-0.39 0.39,-1.03 0,-1.41l-1.06,-1.06zM19.42,5.99c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06zM7.05,18.36c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06z";
    static final String VOLUME_OFF = "M16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v2.21l2.45,2.45c0.03,-0.2 0.05,-0.41 0.05,-0.63zM19,12c0,0.94 -0.2,1.82 -0.54,2.64l1.51,1.51C20.63,14.91 21,13.5 21,12c0,-4.28 -2.99,-7.86 -7,-8.77v2.06c2.89,0.86 5,3.54 5,6.71zM4.27,3L3,4.27 7.73,9H3v6h4l5,5v-6.73l4.25,4.25c-0.67,0.52 -1.42,0.93 -2.25,1.18v2.06c1.38,-0.31 2.63,-0.95 3.69,-1.81L19.73,21 21,19.73l-9,-9L4.27,3zM12,4L9.91,6.09 12,8.18V4z";
    static final String WAVES_INNER = "M3,17h2V7H3v10zM19,17h2V7h-2v10z";
    static final String WAVES_OUTER = "M0,15h2V9H0v6zM22,9v6h2V9h-2z";
    static final String WIFI = "M1,9l2,2c4.97,-4.97 13.03,-4.97 18,0l2,-2C16.93,2.93 7.08,2.93 1,9zM9,17l3,3 3,-3c-1.65,-1.66 -4.34,-1.66 -6,0zM5,13l2,2c2.76,-2.76 7.24,-2.76 10,0l2,-2C15.14,9.14 8.87,9.14 5,13z";

    static Path path(String str) {
        return (Path) XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.util.PathParser", (ClassLoader) null), "createPathFromPathData", new Object[]{str});
    }

    static Drawable of(String str) {
        return new PathIcon(str);
    }

    static Drawable speaker() {
        return new Speaker();
    }

    private static final class Speaker extends Drawable {
        private final RectF arc;
        private final Path body = Icons.path("M2,9v6h4l5,5V4L6,9H2z");
        private int color;
        private final Paint paint;

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return 72;
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return 72;
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        Speaker() {
            Paint paint = new Paint(1);
            this.paint = paint;
            this.arc = new RectF();
            this.color = -1;
            paint.setStrokeWidth(1.8f);
            paint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override // android.graphics.drawable.Drawable
        protected boolean onLevelChange(int i) {
            invalidateSelf();
            return true;
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            int iSave = canvas.save();
            canvas.translate(bounds.left, bounds.top);
            canvas.scale(bounds.width() / 24.0f, bounds.height() / 24.0f);
            this.paint.setColor(this.color);
            this.paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(this.body, this.paint);
            this.paint.setStyle(Paint.Style.STROKE);
            float fMax = Math.max(0.0f, 1.0f - ((getLevel() / 10000.0f) * 30.0f));
            if (fMax > 0.0f) {
                this.paint.setAlpha(Math.round((this.color >>> 24) * fMax));
                canvas.drawLine(3.0f, 3.0f, 21.0f, 21.0f, this.paint);
            }
            float level = (getLevel() / 10000.0f) * 3.0f;
            for (int i = 0; i < 3; i++) {
                float f = i;
                if (level <= f) {
                    break;
                }
                this.paint.setAlpha(Math.round((this.color >>> 24) * Math.min(1.0f, level - f)));
                float f2 = (f * 3.0f) + 5.0f;
                this.arc.set(9.0f - f2, 12.0f - f2, 9.0f + f2, f2 + 12.0f);
                canvas.drawArc(this.arc, -40.0f, 80.0f, false, this.paint);
            }
            canvas.restoreToCount(iSave);
        }

        @Override // android.graphics.drawable.Drawable
        public void setTintList(ColorStateList colorStateList) {
            if (colorStateList != null) {
                this.color = colorStateList.getDefaultColor();
            }
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
            this.color = (i << 24) | (this.color & 16777215);
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            this.paint.setColorFilter(colorFilter);
            invalidateSelf();
        }
    }

    private static final class PathIcon extends Drawable {
        private final Paint paint;
        private final Path path;

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return 72;
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return 72;
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        PathIcon(String str) {
            Paint paint = new Paint(1);
            this.paint = paint;
            this.path = Icons.path(str);
            paint.setColor(-1);
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            int iSave = canvas.save();
            canvas.translate(bounds.left, bounds.top);
            canvas.scale(bounds.width() / 24.0f, bounds.height() / 24.0f);
            canvas.drawPath(this.path, this.paint);
            canvas.restoreToCount(iSave);
        }

        @Override // android.graphics.drawable.Drawable
        public void setTintList(ColorStateList colorStateList) {
            if (colorStateList != null) {
                this.paint.setColor(colorStateList.getDefaultColor());
            }
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
            this.paint.setAlpha(i);
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            this.paint.setColorFilter(colorFilter);
            invalidateSelf();
        }
    }

    private Icons() {
    }
}
