package dev.colorcc;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.RuntimeShader;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.HorizontalScrollView;
import java.util.Collections;
import java.util.Set;

/* loaded from: classes.dex */
final class Glass extends Drawable implements Drawable.Callback {
    private static final int DARK_EXTRA = 12;
    private static final long FADE_MS = 180;
    private static final int FLOOD_LIGHT = -1;
    private static final int FLOOD_SHADE = -1315342;
    private static final int FROST = 8;
    static final int FROSTED = 1;
    private static final int FROST_ALONE = 32;
    private static final String LENS = "uniform shader backdrop;\nuniform shader soft;\nuniform shader before;\nuniform shader beforeSoft;\nuniform float beforeScale;\nuniform float softScale;\nuniform float beforeSoftScale;\nuniform float blend;\nuniform float4 box;\nuniform float radius;\nuniform float2 origin;\nuniform float scale;\nuniform float zoom;\nuniform float seen;\nuniform float widen;\nuniform float band;\nuniform float stroke;\nuniform float4 look;\nuniform float4 depth;\nuniform float2 light;\nuniform float vivid;\nuniform float thick;\nlayout(color) uniform half4 wash;\nhalf4 main(float2 xy) {\n  float2 h = box.zw * 0.5;\n  float2 p = xy - box.xy - h;\n  float r = min(radius, min(h.x, h.y));\n  float2 q = abs(p) - h + r;\n  float d = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;\n  half cover = half(clamp(0.5 - d, 0.0, 1.0));\n  if (cover <= 0.0) { return half4(0.0); }\n  float2 n = (q.x > 0.0 && q.y > 0.0) ? normalize(q) : (q.x > q.y ? float2(1.0, 0.0) : float2(0.0, 1.0));\n  n *= float2(p.x < 0.0 ? -1.0 : 1.0, p.y < 0.0 ? -1.0 : 1.0);\n  half3 env = half3(1.0);\n  if (seen > 0.5) {\n    float2 base = origin + (box.xy + h + p * widen) * zoom;\n    float2 at = base * scale;\n    env = backdrop.eval(at).rgb;\n    if (depth.z > 0.0) { env = mix(env, soft.eval(at * softScale).rgb, half(depth.z)); }\n    if (blend < 1.0) {\n      float2 bt = base * beforeScale;\n      half3 old = before.eval(bt).rgb;\n      if (depth.z > 0.0) { old = mix(old, beforeSoft.eval(bt * beforeSoftScale).rgb, half(depth.z)); }\n      env = mix(old, env, half(blend));\n    }\n    half l = dot(env, half3(0.299, 0.587, 0.114));\n    env = clamp(mix(half3(l), env, half(vivid)), 0.0, 1.0);\n    env = env + (1.0 - env) * half(look.w);\n  }\n  half4 w = half4(wash.rgb * wash.a, wash.a);\n  half f = half(look.x * seen);\n  half3 c = w.rgb + env * f * (1.0 - w.a);\n  half a = w.a + f * (1.0 - w.a);\n  float t = clamp(1.0 + d / band, 0.0, 1.0);\n  half g = half(look.y * t * t * (seen > 0.5 ? 1.0 : 0.3));\n  c = (env + (1.0 - env) * half(depth.w)) * g + c * (1.0 - g);\n  a = g + a * (1.0 - g);\n  float lit = dot(n, light);\n  float top = clamp((1.0 - dot(p / h, light)) / 1.1, 0.0, 1.0);\n  half sheen = half(depth.x * (1.0 - top) * (1.0 - top));\n  c = half3(sheen) + c * (1.0 - sheen);\n  a = sheen + a * (1.0 - sheen);\n  float inner = clamp(1.0 + d / (stroke * 5.0), 0.0, 1.0);\n  half shade = half(abs(depth.y) * inner * inner * max(depth.y > 0.0 ? lit : -lit, 0.0));\n  c = c * (1.0 - shade);\n  a = shade + a * (1.0 - shade);\n  half th = half(thick * clamp(dot(p / h, light), -1.0, 1.0));\n  c = th > 0.0 ? c + (a - c) * th : c * (1.0 + th);\n  float3 n3 = fract(floor(xy).xyx * 0.1031);\n  n3 += float3(dot(n3, n3.yzx + 33.33));\n  half dither = half((fract((n3.x + n3.y) * n3.z) - 0.5) / 255.0);\n  float s = stroke * 0.5;\n  half line = half(clamp(1.0 - abs(d + s) / s, 0.0, 1.0) * look.z * (0.4 + 0.6 * abs(lit)));\n  c = mix(env, half3(1.0), 0.55) * line + c * (1.0 - line);\n  a = line + a * (1.0 - line);\n  return half4(clamp(c + dither * a, 0.0, a), a) * cover;\n}\n";
    static final int LIQUID = 0;
    private static final float LIQUID_WASH = 0.4f;
    static final int OPAQUE = 2;
    private static final int OPAQUE_BASE = 12;
    private static final double OPAQUE_CURVE = 1.6d;
    private static final float PILL_MAX = 34.0f;
    static final int PLAIN = 3;
    private static final int PLAIN_EDGE = 1308622847;
    private static final float RAISED = 0.16f;
    static final float ROUND = -2.0f;
    private static final float SHADE_SATURATION = 1.8f;
    static final int SOFT_OF_SCREEN = 12;
    private static final int SOFT_PASSES = 32;
    private static final long STREAM_GAP_MS = 500;
    private static final float WELL = 0.3f;
    private static Shader backdrop;
    private static float backdropScale;
    private static Shader before;
    private static float beforeScale;
    private static Shader beforeSoft;
    static View bendRoot;
    private static long changedAt;
    static boolean dark;
    private static RuntimeShader lens;
    private static boolean lensBroken;
    static int look;
    static View menuRoot;
    static View noteRoot;
    static View slideRoot;
    private static Shader soft;
    private static boolean stale;
    private final int active;
    private int alpha;
    private final float[] at;
    private final Paint body;
    private final float centerX;
    private final float centerY;
    private final Paint edge;
    private final Paint flood;
    private final Paint glass;
    private final int idle;
    private final RectF inset;
    private float level;
    private final float radius;
    private boolean recessed;
    private final RectF rect;
    private final Path shape;
    private boolean steady;
    private Drawable underlay;
    private final float unit;
    private static final float[][] LOOK = {new float[]{0.0f, 0.75f, 1.0f, 0.04f}, new float[]{1.0f, 0.15f, 0.9f, 0.16f}, new float[]{0.0f, 0.4f, 1.0f, 0.1f}};
    private static final float[] BAND = {5.0f, 3.0f, 3.0f};
    private static final float[] GLOW_LIFT = {0.12f, 0.3f, 0.3f};
    static int tint = 1;
    static int shine = 1;
    private static final float[] TINT = {0.5f, 1.0f, 1.6f};
    private static final float[] SHINE = {0.5f, 1.0f, 1.6f};
    private static final float[] SHEEN = {0.1f, 0.12f, 0.14f};
    private static final float[] THICK = {0.0f, 0.07f, 0.06f};
    private static final float[] DIFFUSE = {0.6f, 1.0f, 0.8f};
    private static final float[] VIVID = {1.15f, 1.3f, 1.4f};
    private static final float[] WIDEN = {1.3f, 1.1f, 1.2f};
    private static final Shader NOTHING = new BitmapShader(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    private static float softScale = 1.0f;
    private static float beforeSoftScale = 1.0f;
    static Set<View> moving = Collections.EMPTY_SET;
    static final ColorFilter SHADE_COLOURS = shadeColours();
    static final ColorFilter LIVE_COLOURS = liveColours();
    static final ColorFilter SHADE_FROM_SCREEN = fromScreen(1.1999999f, 0.744f);
    static final ColorFilter LIVE_FROM_SCREEN = fromScreen(1.0f, 0.6f);
    private static float lightX = 0.0f;
    private static float lightY = -1.0f;

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    Glass steady() {
        this.steady = true;
        return this;
    }

    Glass(float f, int i, int i2, float f2, float f3) {
        Paint paint = new Paint(1);
        this.body = paint;
        this.flood = new Paint(1);
        Paint paint2 = new Paint(1);
        this.edge = paint2;
        this.glass = new Paint();
        this.rect = new RectF();
        this.inset = new RectF();
        this.shape = new Path();
        this.at = new float[]{0.0f, 0.0f, 1.0f};
        this.alpha = 255;
        this.radius = f;
        this.idle = i;
        this.active = i2;
        this.centerX = f2;
        this.centerY = f3;
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        float fMin = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / 360.0f;
        this.unit = fMin;
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(fMin * 0.6f);
        paint2.setColor(1308622847);
        paint.setColor(i);
    }

    static void prepare() {
        lens();
    }

    private static RuntimeShader lens() {
        if (lens == null && !lensBroken) {
            try {
                lens = new RuntimeShader("uniform shader backdrop;\nuniform shader soft;\nuniform shader before;\nuniform shader beforeSoft;\nuniform float beforeScale;\nuniform float softScale;\nuniform float beforeSoftScale;\nuniform float blend;\nuniform float4 box;\nuniform float radius;\nuniform float2 origin;\nuniform float scale;\nuniform float zoom;\nuniform float seen;\nuniform float widen;\nuniform float band;\nuniform float stroke;\nuniform float4 look;\nuniform float4 depth;\nuniform float2 light;\nuniform float vivid;\nuniform float thick;\nlayout(color) uniform half4 wash;\nhalf4 main(float2 xy) {\n  float2 h = box.zw * 0.5;\n  float2 p = xy - box.xy - h;\n  float r = min(radius, min(h.x, h.y));\n  float2 q = abs(p) - h + r;\n  float d = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;\n  half cover = half(clamp(0.5 - d, 0.0, 1.0));\n  if (cover <= 0.0) { return half4(0.0); }\n  float2 n = (q.x > 0.0 && q.y > 0.0) ? normalize(q) : (q.x > q.y ? float2(1.0, 0.0) : float2(0.0, 1.0));\n  n *= float2(p.x < 0.0 ? -1.0 : 1.0, p.y < 0.0 ? -1.0 : 1.0);\n  half3 env = half3(1.0);\n  if (seen > 0.5) {\n    float2 base = origin + (box.xy + h + p * widen) * zoom;\n    float2 at = base * scale;\n    env = backdrop.eval(at).rgb;\n    if (depth.z > 0.0) { env = mix(env, soft.eval(at * softScale).rgb, half(depth.z)); }\n    if (blend < 1.0) {\n      float2 bt = base * beforeScale;\n      half3 old = before.eval(bt).rgb;\n      if (depth.z > 0.0) { old = mix(old, beforeSoft.eval(bt * beforeSoftScale).rgb, half(depth.z)); }\n      env = mix(old, env, half(blend));\n    }\n    half l = dot(env, half3(0.299, 0.587, 0.114));\n    env = clamp(mix(half3(l), env, half(vivid)), 0.0, 1.0);\n    env = env + (1.0 - env) * half(look.w);\n  }\n  half4 w = half4(wash.rgb * wash.a, wash.a);\n  half f = half(look.x * seen);\n  half3 c = w.rgb + env * f * (1.0 - w.a);\n  half a = w.a + f * (1.0 - w.a);\n  float t = clamp(1.0 + d / band, 0.0, 1.0);\n  half g = half(look.y * t * t * (seen > 0.5 ? 1.0 : 0.3));\n  c = (env + (1.0 - env) * half(depth.w)) * g + c * (1.0 - g);\n  a = g + a * (1.0 - g);\n  float lit = dot(n, light);\n  float top = clamp((1.0 - dot(p / h, light)) / 1.1, 0.0, 1.0);\n  half sheen = half(depth.x * (1.0 - top) * (1.0 - top));\n  c = half3(sheen) + c * (1.0 - sheen);\n  a = sheen + a * (1.0 - sheen);\n  float inner = clamp(1.0 + d / (stroke * 5.0), 0.0, 1.0);\n  half shade = half(abs(depth.y) * inner * inner * max(depth.y > 0.0 ? lit : -lit, 0.0));\n  c = c * (1.0 - shade);\n  a = shade + a * (1.0 - shade);\n  half th = half(thick * clamp(dot(p / h, light), -1.0, 1.0));\n  c = th > 0.0 ? c + (a - c) * th : c * (1.0 + th);\n  float3 n3 = fract(floor(xy).xyx * 0.1031);\n  n3 += float3(dot(n3, n3.yzx + 33.33));\n  half dither = half((fract((n3.x + n3.y) * n3.z) - 0.5) / 255.0);\n  float s = stroke * 0.5;\n  half line = half(clamp(1.0 - abs(d + s) / s, 0.0, 1.0) * look.z * (0.4 + 0.6 * abs(lit)));\n  c = mix(env, half3(1.0), 0.55) * line + c * (1.0 - line);\n  a = line + a * (1.0 - line);\n  return half4(clamp(c + dither * a, 0.0, a), a) * cover;\n}\n");
            } catch (Throwable th) {
                lensBroken = true;
                Hook.log("glass shader not compiled, plain glass instead: " + th);
            }
        }
        return lens;
    }

    private static ColorFilter shadeColours() {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(1.8f);
        colorMatrix.postConcat(new ColorMatrix(new float[]{1.24f, 0.0f, 0.0f, 0.0f, -46.0f, 0.0f, 1.24f, 0.0f, 0.0f, -46.0f, 0.0f, 0.0f, 1.24f, 0.0f, -46.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f}));
        return new ColorMatrixColorFilter(colorMatrix);
    }

    private static ColorFilter liveColours() {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(1.5f);
        colorMatrix.postConcat(new ColorMatrix(new float[]{1.0f, 0.0f, 0.0f, 0.0f, -37.0f, 0.0f, 1.0f, 0.0f, 0.0f, -37.0f, 0.0f, 0.0f, 1.0f, 0.0f, -37.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f}));
        return new ColorMatrixColorFilter(colorMatrix);
    }

    private static ColorFilter fromScreen(float f, float f2) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(f);
        colorMatrix.postConcat(new ColorMatrix(new float[]{f2, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, f2, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, f2, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f}));
        return new ColorMatrixColorFilter(colorMatrix);
    }

    static boolean blending() {
        if (before == null) {
            return false;
        }
        if (SystemClock.uptimeMillis() - changedAt < 180) {
            return true;
        }
        beforeSoft = null;
        before = null;
        return false;
    }

    static boolean setBackdrop(Bitmap bitmap, Bitmap bitmap2, float f) {
        boolean z = false;
        if (bitmap == null && backdrop == null) {
            return false;
        }
        boolean z2 = stale;
        stale = false;
        if (bitmap == null) {
            backdrop = null;
            soft = null;
            beforeSoft = null;
            before = null;
            return true;
        }
        long jUptimeMillis = SystemClock.uptimeMillis();
        if (!z2 && jUptimeMillis - changedAt > 500) {
            z = true;
        }
        before = z ? backdrop : null;
        beforeSoft = z ? soft : null;
        beforeScale = backdropScale;
        beforeSoftScale = softScale;
        changedAt = jUptimeMillis;
        BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bitmapShader.setFilterMode(2);
        BitmapShader bitmapShader2 = new BitmapShader(bitmap2, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bitmapShader2.setFilterMode(2);
        backdrop = bitmapShader;
        soft = bitmapShader2;
        backdropScale = f;
        softScale = bitmap2.getWidth() / bitmap.getWidth();
        return true;
    }

    static Bitmap soften(Bitmap bitmap, int i) {
        int iMax = Math.max(1, Math.min(bitmap.getWidth(), (bitmap.getWidth() * i) / 12));
        int iMax2 = Math.max(1, Math.min(bitmap.getHeight(), (bitmap.getHeight() * i) / 12));
        while (bitmap.getWidth() / 2 >= iMax && bitmap.getHeight() / 2 >= iMax2) {
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, true);
        }
        int[] iArr = new int[iMax * iMax2];
        Bitmap.createScaledBitmap(bitmap, iMax, iMax2, true).getPixels(iArr, 0, iMax, 0, 0, iMax, iMax2);
        for (int i2 = 0; i2 < 32; i2++) {
            Logic.smooth(iArr, iMax, iMax2);
        }
        return Bitmap.createBitmap(iArr, iMax, iMax2, Bitmap.Config.ARGB_8888);
    }

    static boolean bending() {
        return backdrop != null;
    }

    static void staleBackdrop() {
        stale = backdrop != null;
    }

    static boolean setLight(float f, float f2) {
        float fHypot = (float) Math.hypot(f, f2);
        if (fHypot < 0.001f) {
            return false;
        }
        float f3 = f / fHypot;
        float f4 = f2 / fHypot;
        if ((lightX * f3) + (lightY * f4) > 0.9994f) {
            return false;
        }
        lightX = f3;
        lightY = f4;
        return true;
    }

    Glass recessed() {
        this.recessed = true;
        return this;
    }

    void setUnderlay(Drawable drawable) {
        this.underlay = drawable;
        drawable.setCallback(this);
        drawable.setBounds(getBounds());
    }

    float level() {
        return this.level;
    }

    void setLevel(float f) {
        float fClamp = Logic.clamp(f);
        if (fClamp == this.level) {
            return;
        }
        this.level = fClamp;
        invalidateSelf();
    }

    private float cornerRadius() {
        return cornerFor(this.rect.width(), this.rect.height());
    }

    float cornerFor(float f, float f2) {
        float fMin = Math.min(f, f2) / 2.0f;
        float f3 = this.radius;
        return f3 == -2.0f ? fMin : f3 < 0.0f ? pillRadius(f, f2) : Math.min(f3, fMin);
    }

    static float pillRadius(float f, float f2) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return Math.min(Math.min(f, f2) / 2.0f, (Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) * 34.0f) / 360.0f);
    }

    @Override // android.graphics.drawable.Drawable
    protected void onBoundsChange(Rect rect) {
        this.rect.set(rect);
        float fCornerRadius = cornerRadius();
        this.shape.reset();
        this.shape.addRoundRect(this.rect, fCornerRadius, fCornerRadius, Path.Direction.CW);
        this.flood.setShader(new LinearGradient(this.rect.left, this.rect.top, this.rect.right, this.rect.bottom, -1, -1315342, Shader.TileMode.CLAMP));
        Drawable drawable = this.underlay;
        if (drawable != null) {
            drawable.setBounds(rect);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        if (this.rect.isEmpty() || this.alpha == 0) {
            return;
        }
        float fCornerRadius = cornerRadius();
        this.flood.setAlpha(scaled(this.active));
        RuntimeShader runtimeShaderLens = canvas.isHardwareAccelerated() ? lens() : null;
        boolean z = (runtimeShaderLens == null || backdrop == null || stale || !restPosition(this.at)) ? false : true;
        if (lit() || (this.idle >>> 24) >= 240) {
            canvas.drawRoundRect(this.rect, fCornerRadius, fCornerRadius, this.flood);
            return;
        }
        int i = look;
        if (i == 3) {
            Drawable drawable = this.underlay;
            if (drawable != null) {
                drawable.draw(canvas);
            }
            this.body.setColor(this.idle);
            this.body.setAlpha(scaled(this.idle));
            canvas.drawRoundRect(this.rect, fCornerRadius, fCornerRadius, this.body);
            drawLevel(canvas);
            return;
        }
        float[] fArr = LOOK[i];
        Drawable drawable2 = this.underlay;
        if (runtimeShaderLens == null) {
            if (drawable2 != null) {
                drawable2.draw(canvas);
            }
            drawPlain(canvas, fCornerRadius);
        } else if (drawable2 != null) {
            if (z) {
                drawLens(canvas, runtimeShaderLens, fCornerRadius, true, fArr[0], 0.0f, 0.0f, fArr[3], VIVID[i], 0, false);
            }
            this.underlay.draw(canvas);
            drawLens(canvas, runtimeShaderLens, fCornerRadius, z, 0.0f, fArr[1], fArr[2], fArr[3], VIVID[look], washColour(z), true);
        } else {
            boolean z2 = z;
            drawLens(canvas, runtimeShaderLens, fCornerRadius, z2, fArr[0], fArr[1], fArr[2], fArr[3], VIVID[i], washColour(z2), true);
        }
        drawLevel(canvas);
    }

    private void drawLevel(Canvas canvas) {
        if (this.level > 0.001f) {
            int iSave = canvas.save();
            canvas.clipPath(this.shape);
            canvas.drawCircle(this.rect.left + (this.rect.width() * this.centerX), this.rect.top + (this.rect.height() * this.centerY), ((float) Math.hypot(this.rect.width(), this.rect.height())) * this.level * 1.1f, this.flood);
            canvas.restoreToCount(iSave);
        }
    }

    private void drawLens(Canvas canvas, RuntimeShader runtimeShader, float f, boolean z, float f2, float f3, float f4, float f5, float f6, int i, boolean z2) {
        float f7;
        float fMin = Math.min(this.rect.width(), this.rect.height());
        runtimeShader.setFloatUniform("box", this.rect.left, this.rect.top, this.rect.width(), this.rect.height());
        runtimeShader.setFloatUniform("radius", f);
        float[] fArr = this.at;
        boolean z3 = false;
        runtimeShader.setFloatUniform("origin", fArr[0], fArr[1]);
        runtimeShader.setFloatUniform("scale", backdropScale);
        runtimeShader.setFloatUniform("zoom", this.at[2]);
        float f8 = 0.0f;
        runtimeShader.setFloatUniform("seen", z ? 1.0f : 0.0f);
        runtimeShader.setFloatUniform("widen", WIDEN[look]);
        runtimeShader.setFloatUniform("band", Math.max(1.0f, Math.min(fMin / 2.0f, this.unit * BAND[look])));
        runtimeShader.setFloatUniform("stroke", this.unit * 0.6f);
        float[] fArr2 = SHINE;
        runtimeShader.setFloatUniform("look", f2, f3, Math.min(1.0f, fArr2[shine] * f4), dark ? 0.25f * f5 : f5);
        float f9 = (!z2 || this.recessed) ? 0.0f : SHEEN[look] * fArr2[shine];
        if (z2) {
            f7 = this.recessed ? 0.3f : -0.16f;
        } else {
            f7 = 0.0f;
        }
        runtimeShader.setFloatUniform("depth", f9, f7, z ? DIFFUSE[look] : 0.0f, GLOW_LIFT[look]);
        runtimeShader.setInputShader("soft", z ? soft : NOTHING);
        if (z && before != null) {
            z3 = true;
        }
        runtimeShader.setInputShader("before", z3 ? before : NOTHING);
        runtimeShader.setInputShader("beforeSoft", z3 ? beforeSoft : NOTHING);
        runtimeShader.setFloatUniform("beforeScale", beforeScale);
        runtimeShader.setFloatUniform("softScale", softScale);
        runtimeShader.setFloatUniform("beforeSoftScale", beforeSoftScale);
        runtimeShader.setFloatUniform("blend", z3 ? Logic.clamp((SystemClock.uptimeMillis() - changedAt) / 180.0f) : 1.0f);
        runtimeShader.setFloatUniform("light", lightX, lightY);
        runtimeShader.setFloatUniform("vivid", f6);
        if (z2) {
            f8 = this.recessed ? -THICK[look] : THICK[look];
        }
        runtimeShader.setFloatUniform("thick", f8);
        runtimeShader.setColorUniform("wash", i);
        runtimeShader.setInputShader("backdrop", z ? backdrop : NOTHING);
        this.glass.setShader(runtimeShader);
        this.glass.setAlpha(this.alpha);
        canvas.drawRect(this.rect, this.glass);
    }

    private boolean lit() {
        return this.level >= 0.999f && (this.active >>> 24) >= 240;
    }

    private int washColour(boolean z) {
        int iRound;
        int i = look;
        if (i == OPAQUE) {
            int iRound2 = (int) Math.round((Math.pow((this.idle >>> 24) / 255.0d, OPAQUE_CURVE) * 243.0d) + 12.0d);
            return iRound2 | (iRound2 << 16) | (-16777216) | (iRound2 << 8);
        }
        if (i == FROSTED) {
            iRound = Math.min(255, (this.idle >>> 24) + 8 + (z ? 0 : 32));
        } else if (i == LIQUID && z && (this.idle >>> 24) < 240) {
            iRound = Math.round((this.idle >>> 24) * LIQUID_WASH);
        } else {
            iRound = this.idle >>> 24;
        }
        if (!this.steady && iRound < 240) {
            iRound = Math.min(239, Math.round(iRound * TINT[tint]));
        }
        if (dark && iRound < 240) {
            return Math.min(255, iRound + 12) << 24;
        }
        return (this.idle & 16777215) | (iRound << 24);
    }

    private void drawPlain(Canvas canvas, float f) {
        int iWashColour = washColour(false);
        this.body.setColor(iWashColour);
        this.body.setAlpha(scaled(iWashColour));
        canvas.drawRoundRect(this.rect, f, f, this.body);
        float strokeWidth = this.edge.getStrokeWidth() / 2.0f;
        this.inset.set(this.rect.left + strokeWidth, this.rect.top + strokeWidth, this.rect.right - strokeWidth, this.rect.bottom - strokeWidth);
        float fMax = Math.max(0.0f, f - strokeWidth);
        this.edge.setAlpha(scaled(1308622847));
        canvas.drawRoundRect(this.inset, fMax, fMax, this.edge);
    }

    private boolean restPosition(float[] fArr) {
        Object callback = getCallback();
        while (callback instanceof Drawable) {
            callback = ((Drawable) callback).getCallback();
        }
        View view = callback instanceof View ? (View) callback : null;
        float left = 0.0f;
        View view2 = view;
        boolean z = false;
        boolean z2 = false;
        float scaleX = 1.0f;
        float top = 0.0f;
        while (view2 != null) {
            Object parent = view2.getParent();
            if (view2 == slideRoot) {
                z2 = true;
            }
            boolean z3 = z2 || moving.contains(view2);
            if (view2 != view && !z3) {
                left = ((left - view2.getPivotX()) * view2.getScaleX()) + view2.getPivotX();
                top = ((top - view2.getPivotY()) * view2.getScaleY()) + view2.getPivotY();
                scaleX *= view2.getScaleX();
            }
            boolean z4 = parent instanceof View;
            if (!z4 || !(((View) parent).getParent() instanceof HorizontalScrollView)) {
                left += view2.getLeft();
            }
            if (!z3) {
                left += view2.getTranslationX();
                top += view2.getTranslationY();
            }
            top += view2.getTop() - ((!z4 || z2) ? 0 : ((View) parent).getScrollY());
            if (view2 == bendRoot || view2 == menuRoot || view2 == noteRoot) {
                z = true;
            }
            view2 = z4 ? (View) parent : null;
        }
        fArr[0] = left;
        fArr[1] = top;
        fArr[2] = scaleX;
        return z;
    }

    private int scaled(int i) {
        return Math.round(((i >>> 24) * this.alpha) / 255.0f);
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.alpha = i;
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.body.setColorFilter(colorFilter);
        this.flood.setColorFilter(colorFilter);
        this.edge.setColorFilter(colorFilter);
        this.glass.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void invalidateDrawable(Drawable drawable) {
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void scheduleDrawable(Drawable drawable, Runnable runnable, long j) {
        scheduleSelf(runnable, j);
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void unscheduleDrawable(Drawable drawable, Runnable runnable) {
        unscheduleSelf(runnable);
    }
}
