package dev.colorcc;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.display.DisplayManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaRoute2Info;
import android.media.MediaRouter2;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.FloatProperty;
import android.util.LruCache;
import android.util.Property;
import android.view.Display;
import android.view.FrameMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextClock;
import android.widget.TextView;
import dev.colorcc.Logic;
import dev.colorcc.Motion;
import dev.colorcc.SideSlide;
import dev.colorcc.Slider;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/* loaded from: classes.dex */
final class Panel extends FrameLayout {
    private static final int ACTIVE_BLUE = -15374872;
    private static final int ACTIVE_BLUE_LIGHT = -10773505;
    private static final int ACTIVE_RED = -1756374;
    private static final float ARC = 22.0f;
    private static final float AWAY_SIZE = 0.84f;
    private static final int BACKDROP_BLUR = 24;
    private static final long BACKDROP_FRAME_MS = 33;
    private static final int BACKDROP_SHRINK = 2;
    private static final int BADGE_REMOVE = -263172008;
    private static final float CLOSE_PACE = 1.4f;
    private static final float CLOSE_SPAN = 0.32f;
    private static final float CLOSE_STEP = 0.035f;
    private static final int COVER_HEIGHT = 250;
    private static final int COVER_INSET = 14;
    private static final int COVER_RADIUS = 24;
    private static final float DROP_HEADER = 16.0f;
    private static final float DROP_STEP = 12.0f;
    private static final float DROP_TOP = 36.0f;
    private static final int EDIT_GAP = 14;
    private static final int EDIT_ROW = 63;
    private static final long FRAME_BUDGET_NS = 16700000;
    private static final float FULLY_OPEN = 0.99f;
    private static final int GAP = 14;
    private static final int GESTURE_ROOM = 22;
    private static final int GLASS_CARD_ON = 587202559;
    private static final int GLASS_DISC_ON = -83886081;
    private static final int GLASS_IDLE = 452984831;
    private static final long GPU_KEEP_MS = 30000;
    private static final int HEADER_ROOM = 94;
    private static final int HEADER_TOP = 64;
    private static final int HEADER_TOP_LANDSCAPE = 36;
    private static final long HOLD_MS = 280;
    private static final float ICON_COVERED = 0.15f;
    private static final float IN_BOUNCE = 0.38f;
    private static final float IN_S = 0.24f;
    private static final float IN_STEP_S = 0.012f;
    private static final float LARGE_UNIT_DP = 1.1f;
    private static final int LIST_ROW = 50;
    private static final long LIVE_BACKDROP_MS = 250;
    private static final int LIVE_DIM = 1711276032;
    private static final int MARGIN = 24;
    private static final float MAX_SMEAR = 0.05f;
    private static final float MEDIA_EDGE_LIGHT = 0.3f;
    private static final int MEDIA_RADIUS = 34;
    private static final int MEDIA_TINT_ALPHA = 240;
    private static final int MENU_HEIGHT = 380;
    private static final int PAGE_PADDING = 27;
    private static final long PAGE_SETTLE_MS = 40;
    private static final int PANE_GAP = 24;
    private static final int PANE_WIDTH = 340;
    private static final float PLAYER_TINT = 0.22f;
    private static final int PLAYER_WIDTH = 260;
    private static final long PREBUILD_DELAY_MS = 4000;
    private static final float PRESS_DEPTH = 0.08f;
    private static final int PULL_LIMIT = 140;
    private static final int RADIUS = 26;
    private static final int RINGER_IDLE = -1493172225;
    private static final int RINGER_INSET = 9;
    private static final int RINGER_NORMAL = 2;
    private static final int RINGER_SILENT = 0;
    private static final int RINGER_VIBRATE = 1;
    private static final int ROW_HEIGHT = 63;
    private static final int SCROLL_FADE = 64;
    private static final int SCROLL_SLACK = 40;
    private static final int SHEET_GLASS = 536870911;
    private static final int SPRINGS = 2113929217;
    private static final long STABLE_RUN_MS = 300000;
    private static final float STAGGER_SPAN = 0.42f;
    private static final float STAGGER_START = 0.06f;
    private static final float STAGGER_STEP = 0.045f;
    private static final int STATE_ACTIVE = 2;
    private static final int STATE_UNAVAILABLE = 0;
    private static final long STOCK_CLOSE_GRACE_MS = 300;
    private static final long STOCK_SCREEN_DELAY_MS = 180;
    private static final long STOCK_SCREEN_TIMEOUT_MS = 1000;
    private static final float SWELL = -1.1f;
    private static final long SWELL_MS = 90;
    private static final float TAP_DEPTH = 0.6f;
    private static final int TILES_PER_ROW = 4;
    private static final int TILE_DISC = 63;
    private static final int TILE_ICON = 26;
    private static final int TILE_ROW_BARE = 74;
    private static final int TILE_ROW_HEIGHT = 98;
    private static final float TOGGLE_KICK = 7.0f;
    private final ArgbEvaluator argb;
    private final AudioManager audio;
    private final ExecutorService audioThread;
    private boolean backdropAgain;
    private boolean backdropBroken;
    private boolean backdropBusy;
    private int backdropFailures;
    private boolean backdropLogged;
    private long backdropSum;
    private final ExecutorService backdropThread;
    private final Blocks blocks;
    private final Card bluetoothCard;
    volatile int blur;
    private final Set<View> bodyViews;
    private final List<Bound> bound;
    private volatile float brightMax;
    private volatile float brightMin;
    private final Slider brightness;
    private final FrameLayout brightnessHost;
    private final DisplayManager.DisplayListener brightnessWatch;
    private final LinearLayout clock;
    private int closeFrames;
    private long closeLastNs;
    private long closeMaxGapNs;
    private long closeMaxWorkNs;
    private float closePace;
    private final Motion.Value closeRide;
    private long closeWorkNs;
    private boolean closing;
    private final Column column;
    private final ViewGroup container;
    private final int[] containerAt;
    private final LinearLayout content;
    private MediaController controller;
    private List<Object> currentTiles;
    private boolean dead;
    private final float density;
    private final DisplayManager display;
    private final Dots dots;
    private long drawMaxNs;
    private long drawNs;
    private Editor editor;
    private final Runnable fadeBackdrop;
    private Object flowOwner;
    private Method flowValue;
    private final Map<String, Object> flows;
    private final boolean foldable;
    private Object frameObserver;
    private Window frameWindow;
    private final Disc gear;
    private GpuBlur gpu;
    private GpuBlur gpuSoft;
    private volatile boolean gpuWorks;
    private final float[] gravity;
    private boolean gravityRead;
    private int gridRowCount;
    private int headerPadding;
    private final LinearLayout headerRow;
    private float heldTop;
    private float heldX;
    private int iconBuilds;
    private boolean interactive;
    private final KeyguardManager keyguard;
    private float lastExpansion;
    private float lastFraction;
    private long lastFractionAt;
    private int lastLogKey;
    private int lastRow;
    private boolean lastStockFlows;
    private final List<Disc> lateIcons;
    private String layout;
    private int layoutsDuringMotion;
    private final FloatProperty<View> litLevel;
    private boolean liveBroken;
    private int liveDim;
    private int liveFailures;
    private boolean liveOn;
    private int liveRadius;
    private boolean lockedNow;
    private long lockedReadAt;
    private volatile float loggedBlur;
    private float loggedShift;
    private final Handler main;
    private final LruCache<Drawable.ConstantState, Bitmap> masks;
    private final MediaCard media;
    private final MediaController.Callback mediaCallback;
    private final MediaSessionManager mediaSessions;
    private int[] mediaTones;
    private final Disc more;
    private final Motion motion;
    private String motionKind;
    private boolean open;
    private float openPace;
    private boolean overPulling;
    private boolean override;
    private long overrideSince;
    private boolean page;
    private final int[] pageAt;
    private int pageRows;
    private final Pager pager;
    private final List<ViewGroup> parkedIn;
    private final List<View> parkedViews;
    private final Motion.Value peek;
    private float peekFrom;
    private final Disc pencil;
    private boolean pendingPage;
    private long pendingPageSince;
    private final Map<String, View> pinViews;
    private final Map<String, int[]> pins;
    private String places;
    private Player player;
    private float playerBack;
    private final ViewTreeObserver.OnPreDrawListener preDraw;
    private final Motion.Value pull;
    private boolean pullArmed;
    private long pullAt;
    private float pullFromY;
    private boolean pullLogged;
    private boolean pullOnOpen;
    private float pullSpeed;
    private long pullStartedAt;
    private boolean pulling;
    private int pullsLogged;
    private boolean quickCapture;
    private final Map<String, Integer> redrawnBy;
    private final Runnable refreshBackdrop;
    private final Runnable releaseGpu;
    private int renderedFrames;
    private int renderedSlow;
    private final BroadcastReceiver ringReceiver;
    private boolean ringRefreshQueued;
    private final RingerPill ringer;
    private boolean sawStockScreen;
    private float scrollFromY;
    private long scrollLastAt;
    private float scrollLastY;
    private int scrollMax;
    private float scrollSpeed;
    private float scrollStart;
    private final Motion.Value scroller;
    private boolean scrolling;
    private final MediaSessionManager.OnActiveSessionsChangedListener sessionsListener;
    private boolean settleWorkPending;
    private Sheet sheet;
    private float sheetBack;
    private boolean sheetLayered;
    private int shownBlur;
    private int shownRingerSlot;
    private float shownStatusShift;
    private final LinearLayout side;
    private final Card simCard;
    private long skipAt;
    private int skipWay;
    private final SideSlide slide;
    private final double[] slowStages;
    private final List<Body> staticBodies;
    private View statusCarrier;
    private View statusRow;
    private boolean statusRowLogged;
    private boolean statusSearched;
    private final List<View> stock;
    private long stockFlowsSince;
    private boolean stockHidden;
    private boolean stockNeeded;
    private View stockPage;
    private long stockScreenSince;
    private final Motion.Value stockValue;
    private float swipeDownX;
    private float swipeDownY;
    private int swipeOwner;
    private boolean systemIcons;
    private boolean systemWatched;
    private int tileBase;
    private final List<Body> tileBodies;
    private boolean tileNames;
    private int tileRows;
    private final FrameLayout tilesHost;
    private final SensorEventListener tilt;
    private long tiltDrawnAt;
    private Bitmap tonedArt;
    private final List<View> touchControls;
    private final int touchSlop;
    private float turnFrom;
    private boolean twoPane;
    private int uiNight;
    private final Slider volume;
    private final FrameLayout volumeHost;
    private int volumeStream;
    private final ConcurrentHashMap<Integer, Float> volumeWanted;
    private final TextView vpn;
    private final Map<Network, String> vpnNetworks;
    private ConnectivityManager.NetworkCallback vpnWatch;
    private final Card wifiCard;
    private Resources words;
    private boolean wordsMissing;
    private String worstFrame;
    private long worstFrameNs;
    private static final float[] MEDIA_TINT_STOPS = {0.0f, 0.35f, 0.8f, 1.0f};
    private static final float[] OUT = Logic.Spring.of(0.12f, 0.0f);
    private static final float[] SCROLL_GLIDE = {36.0f, 1.0f};
    private static final float[] PEEK = {320.0f, 1.0f};
    private static final int[] LIVE_BLUR = {27, 46, 80};
    private static final int[] STAGES = {0, 1, 2, 3, 4, 5, 6, 7, 12};
    private static final String[] STAGE_NAMES = {"wait", "input", "anim", "layout", "draw", "sync", "issue", "swap", "gpu"};

    /* compiled from: D8$$SyntheticClass */
    /* renamed from: dev.colorcc.Panel$27, reason: invalid class name */
    public final /* synthetic */ class AnonymousClass27 implements Runnable {
        public /* synthetic */ AnonymousClass27() {
        }

        @Override // java.lang.Runnable
        public final void run() {
            Panel.this.captureBackdrop();
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void access$11300(Panel panel) {
        panel.openMediaApp();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void access$1500(Panel panel) {
        panel.renderMedia();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void access$1800(Panel panel) {
        panel.refreshRing();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void access$4000(Panel panel) {
        panel.showVpn();
    }

    static /* synthetic */ int access$8608(Panel panel) {
        int i = panel.iconBuilds;
        panel.iconBuilds = i + 1;
        return i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int tileRow() {
        return this.tileNames ? 98 : 74;
    }

    private static long brightnessEvents() {
        try {
            return XposedHelpers.getStaticLongField(DisplayManager.class, "EVENT_FLAG_DISPLAY_BRIGHTNESS");
        } catch (Throwable unused) {
            return 32L;
        }
    }

    /* renamed from: lambda$new$0$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ boolean m13lambda$new$0$devcolorccPanel() {
        tick();
        return true;
    }

    /* renamed from: lambda$new$2$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m25lambda$new$2$devcolorccPanel(final List list) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.14
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m14lambda$new$1$devcolorccPanel(list);
            }
        });
    }

    static /* synthetic */ Thread lambda$new$3(Runnable runnable) {
        return new Thread(runnable, "ColorCC-audio");
    }

    Panel(Context context, ViewGroup viewGroup) {
        super(context);
        this.tileNames = true;
        this.main = new Handler(Looper.getMainLooper());
        this.argb = new ArgbEvaluator();
        Motion motion = new Motion(new Runnable() { // from class: dev.colorcc.Panel.87
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.die();
            }
        });
        this.motion = motion;
        this.flows = new HashMap();
        this.lastLogKey = -1;
        this.stock = new ArrayList();
        ArrayList arrayList = new ArrayList();
        this.touchControls = arrayList;
        this.bound = new ArrayList();
        this.staticBodies = new ArrayList();
        this.tileBodies = new ArrayList();
        this.currentTiles = new ArrayList();
        this.brightMin = 0.0f;
        this.brightMax = 1.0f;
        this.brightnessWatch = new DisplayManager.DisplayListener() { // from class: dev.colorcc.Panel.1
            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayAdded(int i) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayRemoved(int i) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayChanged(final int i) {
                if (i != 0 || !Panel.this.open || Panel.this.brightness.isPressed() || Panel.this.sheet.level.isPressed()) {
                    return;
                }
                Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.1.0
                    @Override // java.lang.Runnable
                    public final void run() {
                        m86lambda$onDisplayChanged$0$devcolorccPanel$1(i);
                    }
                });
            }

            /* renamed from: lambda$onDisplayChanged$0$dev-colorcc-Panel$1, reason: not valid java name */
            /* synthetic */ void m86lambda$onDisplayChanged$0$devcolorccPanel$1(int i) {
                float slider = Logic.toSlider(((Float) XposedHelpers.callMethod(Panel.this.display, "getBrightness", new Object[]{Integer.valueOf(i)})).floatValue(), Panel.this.brightMin, Panel.this.brightMax);
                Panel.this.brightness.setValue(slider);
                Panel.this.sheet.showBrightness(slider);
            }
        };
        this.layout = "wbmlvsd";
        this.tileBase = 11;
        this.pins = new LinkedHashMap();
        this.pinViews = new HashMap();
        this.lateIcons = new ArrayList();
        this.gridRowCount = 3;
        this.tileRows = 3;
        this.swipeOwner = 2;
        this.vpnNetworks = new HashMap();
        this.peekFrom = -1.0f;
        this.gravity = new float[3];
        this.tilt = new SensorEventListener() { // from class: dev.colorcc.Panel.2
            @Override // android.hardware.SensorEventListener
            public void onAccuracyChanged(Sensor sensor, int i) {
            }

            /* renamed from: lambda$onSensorChanged$0$dev-colorcc-Panel$2, reason: not valid java name */
            /* synthetic */ void m87lambda$onSensorChanged$0$devcolorccPanel$2(SensorEvent sensorEvent) {
                Panel.this.onTilt(sensorEvent.values);
            }

            @Override // android.hardware.SensorEventListener
            public void onSensorChanged(final SensorEvent sensorEvent) {
                Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.2.0
                    @Override // java.lang.Runnable
                    public final void run() {
                        m87lambda$onSensorChanged$0$devcolorccPanel$2(sensorEvent);
                    }
                });
            }
        };
        this.openPace = 1.0f;
        this.closePace = 1.0f;
        this.blur = 1;
        this.containerAt = new int[2];
        this.pageAt = new int[2];
        this.slide = new SideSlide(new SideSlide.Host() { // from class: dev.colorcc.Panel.3
            @Override // dev.colorcc.SideSlide.Host
            public List<SideSlide.Piece> pieces() {
                ArrayList arrayList2 = new ArrayList();
                Iterator it = Panel.this.staticBodies.iterator();
                while (it.hasNext()) {
                    arrayList2.add(((Body) it.next()).piece);
                }
                Iterator it2 = Panel.this.tileBodies.iterator();
                while (it2.hasNext()) {
                    arrayList2.add(((Body) it2.next()).piece);
                }
                return arrayList2;
            }

            @Override // dev.colorcc.SideSlide.Host
            public boolean shown() {
                View viewStockPage = Panel.this.stockPage();
                if (!viewStockPage.isShown()) {
                    return false;
                }
                viewStockPage.getLocationOnScreen(Panel.this.pageAt);
                int width = Panel.this.getRootView().getWidth();
                return Panel.this.pageAt[0] < width && Panel.this.pageAt[0] + Math.max(viewStockPage.getWidth(), width) > 0;
            }

            @Override // dev.colorcc.SideSlide.Host
            public boolean drawn() {
                return Panel.this.stockPage().isShown();
            }

            @Override // dev.colorcc.SideSlide.Host
            public boolean visible() {
                return Panel.this.open && Panel.this.getVisibility() == 0 && drawn();
            }

            @Override // dev.colorcc.SideSlide.Host
            public int rootHeight() {
                return Panel.this.getRootView().getHeight();
            }

            @Override // dev.colorcc.SideSlide.Host
            public boolean locked() {
                return Panel.this.locked();
            }

            @Override // dev.colorcc.SideSlide.Host
            public void onSlideIn() {
                Panel.this.setReveal(1.0f);
            }

            @Override // dev.colorcc.SideSlide.Host
            public void onAway() {
                Panel.this.setReveal(0.0f);
            }

            @Override // dev.colorcc.SideSlide.Host
            public void apply() {
                Panel.this.applySheet();
            }
        });
        this.preDraw = new ViewTreeObserver.OnPreDrawListener() { // from class: dev.colorcc.Panel.98
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public final boolean onPreDraw() {
                return Panel.this.m13lambda$new$0$devcolorccPanel();
            }
        };
        this.sessionsListener = new MediaSessionManager.OnActiveSessionsChangedListener() { // from class: dev.colorcc.Panel.100
            @Override // android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
            public final void onActiveSessionsChanged(List list) {
                Panel.this.m25lambda$new$2$devcolorccPanel(list);
            }
        };
        this.mediaCallback = new MediaController.Callback() { // from class: dev.colorcc.Panel.4
            @Override // android.media.session.MediaController.Callback
            public void onPlaybackStateChanged(PlaybackState playbackState) {
                final Panel panel = Panel.this;
                panel.guard(new Runnable() { // from class: dev.colorcc.Panel.4.0
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.access$1500(panel);
                    }
                });
            }

            @Override // android.media.session.MediaController.Callback
            public void onMetadataChanged(MediaMetadata mediaMetadata) {
                final Panel panel = Panel.this;
                panel.guard(new Runnable() { // from class: dev.colorcc.Panel.4.1
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.access$1500(panel);
                    }
                });
            }
        };
        this.ringReceiver = new BroadcastReceiver() { // from class: dev.colorcc.Panel.5
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                int intExtra = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                if (intExtra == 3 || intExtra == 2) {
                    Panel.this.showStream(intExtra);
                }
                final Panel panel = Panel.this;
                panel.guard(new Runnable() { // from class: dev.colorcc.Panel.5.0
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.access$1800(panel);
                    }
                });
                Panel panel2 = Panel.this;
                final Player player = panel2.player;
                Objects.requireNonNull(player);
                panel2.guard(new Runnable() { // from class: dev.colorcc.Panel.5.1
                    @Override // java.lang.Runnable
                    public final void run() {
                        player.readVolume();
                    }
                });
            }
        };
        this.volumeStream = 3;
        this.shownRingerSlot = -1;
        this.audioThread = Executors.newSingleThreadExecutor(new ThreadFactory() { // from class: dev.colorcc.Panel.101
            @Override // java.util.concurrent.ThreadFactory
            public final Thread newThread(Runnable runnable) {
                return Panel.lambda$new$3(runnable);
            }
        });
        this.volumeWanted = new ConcurrentHashMap<>();
        this.skipWay = 1;
        this.litLevel = new FloatProperty<View>("lit") { // from class: dev.colorcc.Panel.6
            @Override // android.util.FloatProperty
            public void setValue(View view, float f) {
                ((Glass) view.getBackground()).setLevel(f);
                ((TextView) view).setTextColor(((Integer) Panel.this.argb.evaluate(Logic.clamp(f), -1, -14671832)).intValue());
            }

            @Override // android.util.Property
            public Float get(View view) {
                return Float.valueOf(((Glass) view.getBackground()).level());
            }
        };
        this.uiNight = -1;
        this.motionKind = "close";
        this.redrawnBy = new HashMap();
        Set<View> setNewSetFromMap = Collections.newSetFromMap(new WeakHashMap());
        this.bodyViews = setNewSetFromMap;
        this.slowStages = new double[STAGES.length];
        this.masks = new LruCache<>(96);
        this.worstFrame = "not measured";
        this.loggedShift = -1.0f;
        this.parkedViews = new ArrayList();
        this.parkedIn = new ArrayList();
        this.lockedReadAt = -1000L;
        this.backdropThread = Executors.newSingleThreadExecutor();
        this.gpuWorks = true;
        this.releaseGpu = new Runnable() { // from class: dev.colorcc.Panel.102
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m30lambda$new$49$devcolorccPanel();
            }
        };
        this.loggedBlur = -1.0f;
        this.fadeBackdrop = new Runnable() { // from class: dev.colorcc.Panel.8
            @Override // java.lang.Runnable
            public void run() {
                if (Glass.blending()) {
                    Panel.reglaze(Panel.this);
                    Panel.this.postOnAnimation(this);
                }
            }
        };
        this.refreshBackdrop = new Runnable() { // from class: dev.colorcc.Panel.103
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m33lambda$new$55$devcolorccPanel();
            }
        };
        this.container = viewGroup;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        this.foldable = context.getPackageManager().hasSystemFeature("android.hardware.sensor.hinge_angle");
        this.density = unitNow();
        this.uiNight = getResources().getConfiguration().uiMode & 48;
        StringBuilder sb = new StringBuilder("panel made for a ");
        sb.append(displayMetrics.widthPixels);
        sb.append("x");
        sb.append(displayMetrics.heightPixels);
        sb.append(" screen, ");
        sb.append(getResources().getConfiguration().smallestScreenWidthDp);
        sb.append(" dp at its narrowest");
        sb.append(large() ? ", large: phone-sized panes" : "");
        Hook.log(sb.toString());
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.audio = (AudioManager) context.getSystemService(AudioManager.class);
        this.display = (DisplayManager) context.getSystemService(DisplayManager.class);
        this.keyguard = (KeyguardManager) context.getSystemService(KeyguardManager.class);
        this.mediaSessions = (MediaSessionManager) context.getSystemService(MediaSessionManager.class);
        setVisibility(4);
        Motion.Value value = motion.value(Logic.Spring.SNAPPY, new Motion.Listener() { // from class: dev.colorcc.Panel.104
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f, float f2) {
                Panel.this.m28lambda$new$4$devcolorccPanel(f, f2);
            }
        });
        this.stockValue = value;
        value.snap(1.0f);
        this.closeRide = motion.value(Logic.Spring.SNAPPY, new Motion.Listener() { // from class: dev.colorcc.Panel.105
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f, float f2) {
                Panel.this.m31lambda$new$5$devcolorccPanel(f, f2);
            }
        });
        this.pull = motion.value(Logic.Spring.PULL, new Motion.Listener() { // from class: dev.colorcc.Panel.106
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f, float f2) {
                Panel.this.m34lambda$new$6$devcolorccPanel(f, f2);
            }
        });
        this.scroller = motion.value(SCROLL_GLIDE, new Motion.Listener() { // from class: dev.colorcc.Panel.107
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f, float f2) {
                Panel.this.m35lambda$new$7$devcolorccPanel(f, f2);
            }
        });
        this.peek = motion.value(PEEK, new Motion.Listener() { // from class: dev.colorcc.Panel.88
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f, float f2) {
                Panel.this.m37lambda$new$9$devcolorccPanel(f, f2);
            }
        });
        Column column = new Column(context);
        this.column = column;
        column.setOrientation(1);
        column.setClipChildren(false);
        column.setClipToPadding(false);
        column.setPadding(dp(24.0f), dp(64.0f), dp(24.0f), 0);
        LinearLayout linearLayout = new LinearLayout(context);
        this.side = linearLayout;
        linearLayout.setOrientation(1);
        linearLayout.setClipChildren(false);
        linearLayout.setClipToPadding(false);
        linearLayout.setPadding(dp(24.0f), dp(64.0f), dp(24.0f), 0);
        linearLayout.setVisibility(8);
        LinearLayout linearLayoutHorizontal = horizontal(context);
        this.content = linearLayoutHorizontal;
        linearLayoutHorizontal.setGravity(1);
        linearLayoutHorizontal.addView(column, new LinearLayout.LayoutParams(0, -1, 1.0f));
        linearLayoutHorizontal.addView(linearLayout, new LinearLayout.LayoutParams(dp(340.0f), -1));
        addView(linearLayoutHorizontal, new FrameLayout.LayoutParams(-1, -1));
        setClipChildren(false);
        Glass.bendRoot = linearLayoutHorizontal;
        guard(new Runnable() { // from class: dev.colorcc.Panel.89
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m15lambda$new$10$devcolorccPanel();
            }
        });
        Disc disc = new Disc(context, dp(16.0f));
        this.pencil = disc;
        disc.icon.setImageDrawable(Icons.of("M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z"));
        disc.setContentDescription(word("edit_tiles", new Object[0]));
        disc.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.90
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Panel.this.m16lambda$new$11$devcolorccPanel(view);
            }
        });
        Disc disc2 = new Disc(context, dp(16.0f));
        this.gear = disc2;
        disc2.icon.setImageDrawable(Icons.of("M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z"));
        disc2.setContentDescription(word("settings", new Object[0]));
        disc2.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.91
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Panel.this.m18lambda$new$13$devcolorccPanel(view);
            }
        });
        Disc disc3 = new Disc(context, dp(16.0f));
        this.more = disc3;
        disc3.icon.setImageDrawable(Icons.of("M13,3h-2v10h2L13,3zM17.83,5.17l-1.42,1.42C17.99,7.86 19,9.81 19,12c0,3.87 -3.13,7 -7,7s-7,-3.13 -7,-7c0,-2.19 1.01,-4.14 2.58,-5.42L6.17,5.17C4.23,6.82 3,9.26 3,12c0,4.97 4.03,9 9,9s9,-4.03 9,-9c0,-2.74 -1.23,-5.18 -3.17,-6.83z"));
        disc3.setContentDescription(word("power_menu", new Object[0]));
        disc3.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.92
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Panel.this.m19lambda$new$14$devcolorccPanel(view);
            }
        });
        LinearLayout linearLayout2 = new LinearLayout(context);
        this.clock = linearLayout2;
        linearLayout2.setOrientation(1);
        linearLayout2.setGravity(16);
        TextClock textClockClockText = clockText(context, 15.0f, -1, "h:mm", "H:mm");
        textClockClockText.setTypeface(Typeface.DEFAULT_BOLD);
        linearLayout2.addView(textClockClockText);
        TextClock textClockClockText2 = clockText(context, 10.0f, -1275068417, "EEE, d MMM", "EEE, d MMM");
        linearLayout2.addView(textClockClockText2);
        fitWidest(textClockClockText, "h:mm", "H:mm");
        fitWidest(textClockClockText2, "EEE, d MMM");
        TextView textViewLabel = label(context, 12.0f, -1, 1);
        this.vpn = textViewLabel;
        textViewLabel.setGravity(17);
        textViewLabel.setIncludeFontPadding(false);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(1711276032);
        gradientDrawable.setCornerRadius(dp(14.0f));
        textViewLabel.setBackground(gradientDrawable);
        textViewLabel.setPadding(dp(12.0f), 0, dp(14.0f), 0);
        textViewLabel.setShadowLayer(dp(3.0f), 0.0f, dp(1.0f), -1728053248);
        Drawable drawableOf = Icons.of("M12.65,10C11.83,7.67 9.61,6 7,6c-3.31,0 -6,2.69 -6,6s2.69,6 6,6c2.61,0 4.83,-1.67 5.65,-4H17v4h4v-4h2v-4H12.65zM7,14c-1.1,0 -2,-0.9 -2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2z");
        drawableOf.setTint(-1);
        drawableOf.setBounds(0, 0, dp(13.0f), dp(13.0f));
        textViewLabel.setCompoundDrawables(drawableOf, null, null, null);
        textViewLabel.setCompoundDrawablePadding(dp(6.0f));
        textViewLabel.setVisibility(8);
        textViewLabel.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.93
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Panel.this.m21lambda$new$16$devcolorccPanel(view);
            }
        });
        LinearLayout linearLayoutHorizontal2 = horizontal(context);
        this.headerRow = linearLayoutHorizontal2;
        linearLayoutHorizontal2.setGravity(16);
        linearLayoutHorizontal2.setClipChildren(false);
        linearLayoutHorizontal2.addView(disc, headerDisc(0));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.leftMargin = dp(10.0f);
        linearLayoutHorizontal2.addView(linearLayout2, layoutParams);
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setClipChildren(false);
        frameLayout.addView(textViewLabel, new FrameLayout.LayoutParams(-2, dp(28.0f), 17));
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, -1, 1.0f);
        int iDp = dp(8.0f);
        layoutParams2.rightMargin = iDp;
        layoutParams2.leftMargin = iDp;
        linearLayoutHorizontal2.addView(frameLayout, layoutParams2);
        linearLayoutHorizontal2.addView(disc3, headerDisc(0));
        linearLayoutHorizontal2.addView(disc2, headerDisc(dp(12.0f)));
        linearLayoutHorizontal2.setMinimumHeight(dp(32.0f));
        column.addView(linearLayoutHorizontal2, new LinearLayout.LayoutParams(-1, -2));
        Card card = new Card(context);
        this.wifiCard = card;
        Card card2 = new Card(context);
        this.bluetoothCard = card2;
        MediaCard mediaCard = new MediaCard(context);
        this.media = mediaCard;
        Slider slider = new Slider(context, motion, dp(26.0f), dp(22.0f));
        this.brightness = slider;
        slider.setIcon(Icons.of("M12,7c-2.76,0 -5,2.24 -5,5s2.24,5 5,5 5,-2.24 5,-5 -2.24,-5 -5,-5zM2,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1H2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM20,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1h-2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM11,2v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1V2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM11,20v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1v-2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM5.99,4.58c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0s0.39,-1.03 0,-1.41L5.99,4.58zM18.36,16.95c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0 0.39,-0.39 0.39,-1.03 0,-1.41l-1.06,-1.06zM19.42,5.99c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06zM7.05,18.36c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06z"));
        slider.setIconSpin(180.0f);
        slider.setIconColor(-874996);
        slider.setContentDescription(word("brightness", new Object[0]));
        slider.setListener(new Slider.Listener() { // from class: dev.colorcc.Panel.94
            @Override // dev.colorcc.Slider.Listener
            public final void onChange(float f, boolean z) {
                Panel.this.onBrightness(f, z);
            }
        });
        slider.setBackground(new Glass(-1.0f, 452984831, 452984831, 0.5f, 0.5f).recessed());
        Slider slider2 = new Slider(context, motion, dp(26.0f), dp(22.0f));
        this.volume = slider2;
        slider2.setIcon(Icons.speaker());
        slider2.setIconColor(-15374872);
        slider2.setContentDescription(word("media_volume", new Object[0]));
        slider2.setListener(new Slider.Listener() { // from class: dev.colorcc.Panel.95
            @Override // dev.colorcc.Slider.Listener
            public final void onChange(float f, boolean z) {
                Panel.this.onVolume(f, z);
            }
        });
        slider2.setOnHold(new Runnable() { // from class: dev.colorcc.Panel.96
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m23lambda$new$18$devcolorccPanel();
            }
        });
        slider.setOnHold(new Runnable() { // from class: dev.colorcc.Panel.97
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m26lambda$new$20$devcolorccPanel();
            }
        });
        slider2.setBackground(new Glass(-1.0f, 452984831, 452984831, 0.5f, 0.5f).recessed());
        FrameLayout frameLayoutHost = host(context, slider);
        this.brightnessHost = frameLayoutHost;
        FrameLayout frameLayoutHost2 = host(context, slider2);
        this.volumeHost = frameLayoutHost2;
        RingerPill ringerPill = new RingerPill(context);
        this.ringer = ringerPill;
        Card card3 = new Card(context);
        this.simCard = card3;
        Blocks blocks = new Blocks(context);
        this.blocks = blocks;
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams3.topMargin = dp(20.0f);
        column.addView(blocks, layoutParams3);
        Pager pager = new Pager(context);
        this.pager = pager;
        FrameLayout frameLayout2 = new FrameLayout(context);
        this.tilesHost = frameLayout2;
        frameLayout2.setClipChildren(false);
        frameLayout2.addView(pager, new FrameLayout.LayoutParams(-1, -2));
        Dots dots = new Dots(context);
        this.dots = dots;
        column.addView(dots, new LinearLayout.LayoutParams(-1, dp(20.0f)));
        blocks.setTranslationZ(-1.0f);
        pager.setTranslationZ(-2.0f);
        dots.setTranslationZ(-3.0f);
        addStaticBody(disc, 0, true);
        addStaticBody(disc2, 1, true);
        addStaticBody(disc3, 2, true);
        addStaticBody(linearLayout2, 1, false);
        addStaticBody(textViewLabel, 2, true);
        addStaticBody(card, 3, true);
        addStaticBody(card2, 4, true);
        addStaticBody(mediaCard, 5, true);
        addStaticBody(frameLayoutHost, 6, false);
        addStaticBody(frameLayoutHost2, 7, false);
        addStaticBody(ringerPill, 8, false);
        addStaticBody(card3, 9, true);
        addStaticBody(dots, 21, false);
        arrayList.add(slider);
        arrayList.add(slider2);
        arrayList.add(ringerPill);
        Editor editor = new Editor(context);
        this.editor = editor;
        addView(editor, new FrameLayout.LayoutParams(-1, -1));
        arrayList.add(this.editor);
        Sheet sheet = new Sheet(context);
        this.sheet = sheet;
        addView(sheet, new FrameLayout.LayoutParams(-1, -1));
        Glass.menuRoot = this.sheet;
        Glass.slideRoot = this;
        Glass.moving = setNewSetFromMap;
        arrayList.add(this.sheet);
        Player player = new Player(context);
        this.player = player;
        addView(player, new FrameLayout.LayoutParams(-1, -1));
        arrayList.add(this.player);
        guard(new Runnable() { // from class: dev.colorcc.Panel.99
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m27lambda$new$21$devcolorccPanel();
            }
        });
        if (this.layout.indexOf(103) < 0) {
            String[] strArrWithTiles = Logic.withTiles(this.layout, null);
            this.layout = strArrWithTiles[0];
            this.places = strArrWithTiles[1];
        }
        blocks.arrange(this.layout);
    }

    /* renamed from: lambda$new$4$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m28lambda$new$4$devcolorccPanel(float f, float f2) {
        Iterator<View> it = this.stock.iterator();
        while (it.hasNext()) {
            it.next().setAlpha(Logic.clamp(f));
        }
    }

    /* renamed from: lambda$new$5$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m31lambda$new$5$devcolorccPanel(float f, float f2) {
        applySheet();
    }

    /* renamed from: lambda$new$6$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m34lambda$new$6$devcolorccPanel(float f, float f2) {
        Iterator<Body> it = this.staticBodies.iterator();
        while (it.hasNext()) {
            it.next().apply();
        }
        Iterator<Body> it2 = this.tileBodies.iterator();
        while (it2.hasNext()) {
            it2.next().apply();
        }
        if (Glass.look == 1 && Glass.bending()) {
            reglaze(this);
        }
    }

    /* renamed from: lambda$new$7$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m35lambda$new$7$devcolorccPanel(float f, float f2) {
        scrollCards(f);
    }

    /* renamed from: lambda$new$9$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m37lambda$new$9$devcolorccPanel(float f, float f2) {
        Iterator<Body> it = this.staticBodies.iterator();
        while (it.hasNext()) {
            it.next().apply();
        }
        Iterator<Body> it2 = this.tileBodies.iterator();
        while (it2.hasNext()) {
            it2.next().apply();
        }
        if (this.liveOn) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.23
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m36lambda$new$8$devcolorccPanel();
                }
            });
        }
        Hook.fadeShadeBlur(1.0f - Logic.clamp(f));
        float fRound = Math.round((1.0f - Logic.clamp(f)) * 8.0f) / 8.0f;
        if (!Hook.samsungLive() || fRound == Hook.peekClear) {
            return;
        }
        Hook.peekClear = fRound;
        guard(new Runnable() { // from class: dev.colorcc.Panel.24
            @Override // java.lang.Runnable
            public final void run() {
                Hook.redoLiveBlur();
            }
        });
    }

    /* renamed from: lambda$new$8$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m36lambda$new$8$devcolorccPanel() {
        m65lambda$onShadeExpansion$42$devcolorccPanel(this.lastExpansion);
    }

    /* renamed from: lambda$new$10$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m15lambda$new$10$devcolorccPanel() {
        SharedPreferences sharedPreferencesPrefs = prefs();
        Glass.look = Math.max(0, Math.min(3, sharedPreferencesPrefs.getInt("look", sharedPreferencesPrefs.getBoolean("blur_only", false) ? 1 : 0)));
        this.systemIcons = sharedPreferencesPrefs.getBoolean("systemIcons", false);
        this.tileNames = sharedPreferencesPrefs.getBoolean("tileNames", true);
        PageSwipe.moveRows = sharedPreferencesPrefs.getBoolean("notifSwipe", true);
        Glass.tint = Math.max(0, Math.min(2, sharedPreferencesPrefs.getInt("glassTint", 1)));
        Glass.shine = Math.max(0, Math.min(2, sharedPreferencesPrefs.getInt("edgeShine", 1)));
        Hook.notifGlass = sharedPreferencesPrefs.getBoolean("notifGlass", false);
        this.blur = Math.max(0, Math.min(2, sharedPreferencesPrefs.getInt("blur", !sharedPreferencesPrefs.getBoolean("light_blur", false) ? 1 : 0)));
    }

    /* renamed from: lambda$new$11$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m16lambda$new$11$devcolorccPanel(View view) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.74
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.openEditor();
            }
        });
    }

    /* renamed from: lambda$new$12$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m17lambda$new$12$devcolorccPanel() {
        openSettings("android.settings.SETTINGS");
    }

    /* renamed from: lambda$new$13$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m18lambda$new$13$devcolorccPanel(View view) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.30
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m17lambda$new$12$devcolorccPanel();
            }
        });
    }

    /* renamed from: lambda$new$14$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m19lambda$new$14$devcolorccPanel(View view) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.29
            @Override // java.lang.Runnable
            public final void run() {
                Hook.openPowerMenu();
            }
        });
    }

    /* renamed from: lambda$new$15$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m20lambda$new$15$devcolorccPanel() {
        openSettings("android.settings.VPN_SETTINGS");
    }

    /* renamed from: lambda$new$16$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m21lambda$new$16$devcolorccPanel(View view) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.18
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m20lambda$new$15$devcolorccPanel();
            }
        });
    }

    /* renamed from: lambda$new$17$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m22lambda$new$17$devcolorccPanel() {
        this.sheet.openVolumes();
    }

    /* renamed from: lambda$new$18$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m23lambda$new$18$devcolorccPanel() {
        swell(this.volume, new Runnable() { // from class: dev.colorcc.Panel.73
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m22lambda$new$17$devcolorccPanel();
            }
        });
    }

    /* renamed from: lambda$new$19$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m24lambda$new$19$devcolorccPanel() {
        this.sheet.openBrightness();
    }

    /* renamed from: lambda$new$20$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m26lambda$new$20$devcolorccPanel() {
        swell(this.brightness, new Runnable() { // from class: dev.colorcc.Panel.114
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m24lambda$new$19$devcolorccPanel();
            }
        });
    }

    /* renamed from: lambda$new$21$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m27lambda$new$21$devcolorccPanel() {
        String[] strArrWithTiles = Logic.withTiles(Logic.layout(prefs().getString("layout", null)), prefs().getString("places", null));
        this.layout = strArrWithTiles[0];
        this.places = strArrWithTiles[1];
        this.pins.putAll(Logic.pins(prefs().getString("pins", null)));
        this.tileRows = Math.max(2, Math.min(4, prefs().getInt("tileRows", 3)));
        Hook.samsungLiveSeen = prefs().getBoolean("samsungLive", false);
    }

    private final class Column extends LinearLayout {
        private final Paint fade;
        private boolean underHeader;

        Column(Context context) {
            super(context);
            Paint paint = new Paint();
            this.fade = paint;
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        }

        @Override // android.view.ViewGroup, android.view.View
        protected void dispatchDraw(Canvas canvas) {
            int scrollY = getScrollY();
            if (scrollY > 0 && Panel.this.headerRow.getParent() == this) {
                float bottom = Panel.this.headerRow.getBottom() + scrollY + Panel.this.dp(4.0f);
                float fDp = Panel.this.dp(64.0f);
                int iSaveLayer = canvas.saveLayer(0.0f, bottom, getWidth(), getHeight() + scrollY, null);
                this.underHeader = true;
                super.dispatchDraw(canvas);
                this.underHeader = false;
                float f = bottom + fDp;
                this.fade.setShader(new LinearGradient(0.0f, bottom, 0.0f, f, Math.round(Logic.clamp(scrollY / fDp) * 255.0f) << 24, 0, Shader.TileMode.CLAMP));
                canvas.drawRect(0.0f, bottom, getWidth(), f, this.fade);
                canvas.restoreToCount(iSaveLayer);
                super.drawChild(canvas, Panel.this.headerRow, getDrawingTime());
                return;
            }
            super.dispatchDraw(canvas);
        }

        @Override // android.view.ViewGroup
        protected boolean drawChild(Canvas canvas, View view, long j) {
            return !(this.underHeader && view == Panel.this.headerRow) && super.drawChild(canvas, view, j);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public SharedPreferences prefs() {
        return getContext().createDeviceProtectedStorageContext().getSharedPreferences("colorcc", 0);
    }

    private final class Blocks extends ViewGroup {
        private String order;
        private int[][] spans;
        private int[][] spots;

        Blocks(Context context) {
            super(context);
            this.order = "";
            this.spots = new int[0][];
            this.spans = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 2, 0);
            setClipChildren(false);
        }

        void arrange(String str) {
            int i;
            this.spans = Logic.sizes(Panel.this.places, str);
            this.spots = Logic.spots(Panel.this.places, str, 4);
            int iIndexOf = str.indexOf(103);
            if (iIndexOf >= 0) {
                ArrayList arrayList = new ArrayList(Arrays.asList(this.spots));
                arrayList.remove(iIndexOf);
                int[][] iArr = this.spots;
                int[][] iArr2 = {new int[iArr.length - 1], new int[iArr.length - 1]};
                int i2 = 0;
                for (int i3 = 0; i3 < this.spots.length; i3++) {
                    if (i3 != iIndexOf) {
                        int[] iArr3 = iArr2[0];
                        int[][] iArr4 = this.spans;
                        iArr3[i2] = iArr4[0][i3];
                        iArr2[1][i2] = iArr4[1][i3];
                        i2++;
                    }
                }
                this.spans = iArr2;
                this.spots = Logic.lift((int[][]) arrayList.toArray(new int[0][]), iArr2[1]);
                str = str.substring(0, iIndexOf) + str.substring(iIndexOf + 1);
            }
            this.order = str;
            removeAllViews();
            for (int i4 = 0; i4 < str.length(); i4++) {
                View viewViewOf = viewOf(Character.toLowerCase(str.charAt(i4)));
                if (viewViewOf instanceof Card) {
                    int[][] iArr5 = this.spans;
                    ((Card) viewViewOf).shape(iArr5[0][i4], iArr5[1][i4]);
                }
                addView(viewViewOf);
                Body bodyBodyOf = Panel.this.bodyOf(viewViewOf);
                if (bodyBodyOf != null) {
                    int[] iArr6 = this.spots[i4];
                    bodyBodyOf.order = (iArr6[1] * 2) + 3 + (iArr6[0] / 2);
                }
                if (bodyBodyOf != null) {
                    viewViewOf.setTranslationZ(-bodyBodyOf.order);
                }
            }
            for (Map.Entry entry : Panel.this.pins.entrySet()) {
                View view = (View) Panel.this.pinViews.get(entry.getKey());
                if (view != null) {
                    int[][] iArr7 = this.spots;
                    int length = iArr7.length;
                    int i5 = length + 1;
                    int[][] iArr8 = (int[][]) Arrays.copyOf(iArr7, i5);
                    int[][] iArr9 = {Arrays.copyOf(this.spans[0], i5), Arrays.copyOf(this.spans[1], i5)};
                    int[] iArrPinSize = Logic.pinSize((int[]) entry.getValue());
                    iArr9[0][length] = iArrPinSize[0];
                    iArr9[1][length] = iArrPinSize[1];
                    if (view instanceof Card) {
                        ((Card) view).shape(iArrPinSize[0], iArrPinSize[1]);
                    }
                    iArr8[length] = Logic.nearest(iArr9[0], iArr9[1], iArr8, length, (int[]) entry.getValue(), 4);
                    this.spots = iArr8;
                    this.spans = iArr9;
                    addView(view);
                    for (Body body : Panel.this.tileBodies) {
                        if (body.view == view) {
                            int[] iArr10 = iArr8[length];
                            body.order = (iArr10[1] * 2) + 3 + (iArr10[0] / 2);
                        }
                    }
                }
            }
            if (Panel.this.twoPane) {
                i = 11;
            } else {
                int[][] iArr11 = this.spots;
                int length2 = iArr11.length;
                int iRows = Logic.rows(iArr11, this.spans[1]);
                int i6 = length2 + 1;
                int[][] iArr12 = (int[][]) Arrays.copyOf(this.spots, i6);
                this.spots = iArr12;
                iArr12[length2] = new int[]{0, iRows};
                int[][] iArr13 = {Arrays.copyOf(this.spans[0], i6), Arrays.copyOf(this.spans[1], i6)};
                this.spans = iArr13;
                iArr13[0][length2] = Logic.spanW('g');
                this.spans[1][length2] = (int) Math.ceil(((Panel.this.gridRowCount * Panel.this.tileRow()) + 14) / 77.0f);
                addView(Panel.this.tilesHost);
                i = (iRows * 2) + 3;
            }
            for (Body body2 : Panel.this.tileBodies) {
                if (body2.view.getParent() != this) {
                    body2.order += i - Panel.this.tileBase;
                }
            }
            Panel.this.tileBase = i;
            requestLayout();
        }

        private View viewOf(char c) {
            if (c == 'b') {
                return Panel.this.bluetoothCard;
            }
            if (c == 's') {
                return Panel.this.ringer;
            }
            if (c == 'l') {
                return Panel.this.brightnessHost;
            }
            if (c == 'm') {
                return Panel.this.media;
            }
            if (c == 'v') {
                return Panel.this.volumeHost;
            }
            Panel panel = Panel.this;
            return c != 'w' ? panel.simCard : panel.wifiCard;
        }

        private int edge(int i, int i2) {
            return Math.round((i * (i2 + Panel.this.dp(14.0f))) / 4.0f);
        }

        @Override // android.view.View
        protected void onMeasure(int i, int i2) {
            int iDp;
            int size = View.MeasureSpec.getSize(i);
            int i3 = 0;
            int iMax = 0;
            while (true) {
                int[][] iArr = this.spots;
                if (i3 < iArr.length) {
                    int i4 = iArr[i3][0];
                    int iEdge = (edge(this.spans[0][i3] + i4, size) - Panel.this.dp(14.0f)) - edge(i4, size);
                    if (getChildAt(i3) == Panel.this.tilesHost) {
                        iEdge += Panel.this.dp(24.0f) * 2;
                    }
                    if (getChildAt(i3) == Panel.this.tilesHost) {
                        iDp = Panel.this.gridRowCount * Panel.this.dp(Panel.this.tileRow());
                    } else {
                        iDp = (this.spans[1][i3] * Panel.this.dp(63.0f)) + ((this.spans[1][i3] - 1) * Panel.this.dp(14.0f));
                    }
                    getChildAt(i3).measure(View.MeasureSpec.makeMeasureSpec(iEdge, 1073741824), View.MeasureSpec.makeMeasureSpec(iDp, 1073741824));
                    iMax = Math.max(iMax, (this.spots[i3][1] * (Panel.this.dp(63.0f) + Panel.this.dp(14.0f))) + iDp);
                    i3++;
                } else {
                    setMeasuredDimension(size, iMax);
                    return;
                }
            }
        }

        @Override // android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
            for (int i5 = 0; i5 < this.spots.length; i5++) {
                View childAt = getChildAt(i5);
                int iEdge = edge(this.spots[i5][0], i3 - i) - (childAt == Panel.this.tilesHost ? Panel.this.dp(24.0f) : 0);
                int iDp = this.spots[i5][1] * (Panel.this.dp(63.0f) + Panel.this.dp(14.0f));
                childAt.layout(iEdge, iDp, childAt.getMeasuredWidth() + iEdge, childAt.getMeasuredHeight() + iDp);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Motion.Value spring(final View view, final Property<View, Float> property, float[] fArr) {
        Map map = (Map) view.getTag(2113929217);
        if (map == null) {
            map = new HashMap();
            view.setTag(2113929217, map);
        }
        Motion.Value value = (Motion.Value) map.get(property);
        if (value == null) {
            final FloatProperty floatProperty = property instanceof FloatProperty ? (FloatProperty) property : null;
            value = this.motion.value(fArr, floatProperty != null ? new Motion.Listener() { // from class: dev.colorcc.Panel.55
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    floatProperty.setValue(view, f);
                }
            } : new Motion.Listener() { // from class: dev.colorcc.Panel.56
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Panel.lambda$spring$23(property, view, f, f2);
                }
            });
            map.put(property, value);
        }
        value.tune(fArr);
        value.track(property.get(view).floatValue());
        return value;
    }

    static /* synthetic */ void lambda$spring$23(Property property, View view, float f, float f2) {
        property.set(view, Float.valueOf(f));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scaleTo(View view, float f, float[] fArr) {
        spring(view, View.SCALE_X, fArr).to(f);
        spring(view, View.SCALE_Y, fArr).to(f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void skipped(int i) {
        this.skipWay = i;
        this.skipAt = SystemClock.uptimeMillis();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void slideIn(View... viewArr) {
        int i = SystemClock.uptimeMillis() - this.skipAt < 3000 ? this.skipWay : 1;
        for (View view : viewArr) {
            Motion.Value valueSpring = spring(view, View.ALPHA, Logic.Spring.SNAPPY);
            Motion.Value valueSpring2 = spring(view, View.TRANSLATION_X, Logic.Spring.BOUNCY);
            valueSpring.snap(0.0f);
            valueSpring2.snap(dp(32.0f) * i);
            valueSpring.to(1.0f);
            valueSpring2.to(0.0f);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void swellIn(View view) {
        Motion.Value valueSpring = spring(view, View.ALPHA, Logic.Spring.SNAPPY);
        valueSpring.snap(0.3f);
        valueSpring.to(1.0f);
        spring(view, View.SCALE_X, Logic.Spring.BOUNCY).snap(1.08f);
        spring(view, View.SCALE_Y, Logic.Spring.BOUNCY).snap(1.08f);
        scaleTo(view, 1.0f, Logic.Spring.BOUNCY);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void squish(View view) {
        view.setOnTouchListener(new View.OnTouchListener() { // from class: dev.colorcc.Panel.0
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view2, MotionEvent motionEvent) {
                return Panel.this.m77lambda$squish$24$devcolorccPanel(view2, motionEvent);
            }
        });
    }

    /* renamed from: lambda$squish$24$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ boolean m77lambda$squish$24$devcolorccPanel(View view, MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            scaleTo(view, 0.92f, Logic.Spring.PRESS_IN);
            return false;
        }
        if (actionMasked != 1 && actionMasked != 3) {
            return false;
        }
        scaleTo(view, 1.0f, Logic.Spring.PRESS_OUT);
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Drawable scrim() {
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{1929379840, 637534208, 1929379840});
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void backdrop() {
        RenderEffect renderEffectCreateBlurEffect;
        float fClamp = Logic.clamp(Math.max(this.sheetBack, this.playerBack));
        this.content.setAlpha(1.0f - Math.max(Logic.clamp(this.sheetBack) * 0.9f, Logic.clamp(this.playerBack) * 0.3f));
        int iRound = Math.round(dp(24.0f) * fClamp);
        if (iRound == this.shownBlur) {
            return;
        }
        this.shownBlur = iRound;
        LinearLayout linearLayout = this.content;
        if (iRound < 1) {
            renderEffectCreateBlurEffect = null;
        } else {
            float f = iRound;
            renderEffectCreateBlurEffect = RenderEffect.createBlurEffect(f, f, Shader.TileMode.CLAMP);
        }
        linearLayout.setRenderEffect(renderEffectCreateBlurEffect);
    }

    private LinearLayout.LayoutParams pagerParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, this.gridRowCount * dp(tileRow()));
        layoutParams.leftMargin = -dp(24.0f);
        layoutParams.rightMargin = -dp(24.0f);
        return layoutParams;
    }

    private boolean landscape() {
        return getResources().getConfiguration().orientation == 2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void arrange() {
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) {
            return;
        }
        boolean z = width > height || large();
        if (z != this.twoPane) {
            this.twoPane = z;
            ((ViewGroup) this.pager.getParent()).removeView(this.pager);
            ((ViewGroup) this.dots.getParent()).removeView(this.dots);
            LinearLayout linearLayout = z ? this.side : this.column;
            if (z) {
                this.side.addView(this.pager, pagerParams());
            } else {
                this.tilesHost.addView(this.pager, new FrameLayout.LayoutParams(-1, -2));
            }
            linearLayout.addView(this.dots, new LinearLayout.LayoutParams(-1, dp(20.0f)));
            this.column.setLayoutParams(z ? new LinearLayout.LayoutParams(dp(340.0f), -1) : new LinearLayout.LayoutParams(0, -1, 1.0f));
            this.side.setVisibility(z ? 0 : 8);
            this.blocks.arrange(this.layout);
        }
        float fMin = z ? Math.min(1.0f, Math.min(width / (dp(340.0f) * 2), height / ((this.headerPadding + (this.gridRowCount * dp(tileRow()))) + dp(32.0f)))) : 1.0f;
        scalePane(this.column, fMin, dp(340.0f));
        scalePane(this.side, fMin, 0);
        measureScroll();
    }

    private void measureScroll() {
        View childAt = null;
        for (int i = 0; i < this.column.getChildCount(); i++) {
            if (this.column.getChildAt(i).getVisibility() != 8) {
                childAt = this.column.getChildAt(i);
            }
        }
        int iLayoutTop = layoutTop(this);
        WindowInsets rootWindowInsets = getRootWindowInsets();
        int iMin = Math.min(getRootView().getHeight() - Math.max(rootWindowInsets == null ? 0 : rootWindowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom, dp(22.0f)), getHeight() + iLayoutTop);
        if (!this.twoPane && childAt != null) {
            float top = (iMin - iLayoutTop) - this.column.getTop();
            float bottom = childAt.getBottom();
            float f = (bottom <= top || bottom - top > ((float) dp(40.0f))) ? 1.0f : top / bottom;
            if (this.column.getScaleY() != f) {
                Column column = this.column;
                scalePane(column, f, column.getWidth() / 2);
            }
        }
        int iRound = childAt == null ? 0 : Math.round(childAt.getBottom() - (((iMin - iLayoutTop) - this.column.getTop()) / Math.max(0.1f, this.column.getScaleY())));
        this.scrollMax = iRound > dp(40.0f) ? dp(16.0f) + iRound : 0;
        float fValue = this.scroller.value();
        int i2 = this.scrollMax;
        if (fValue > i2) {
            this.scroller.snap(i2);
        }
    }

    private boolean overCards(float f) {
        if (!this.twoPane) {
            return true;
        }
        int[] iArr = new int[2];
        this.column.getLocationOnScreen(iArr);
        return f <= ((float) iArr[0]) + (((float) this.column.getWidth()) * this.column.getScaleX());
    }

    private static void scalePane(View view, float f, int i) {
        view.setPivotX(i);
        view.setPivotY(0.0f);
        view.setScaleX(f);
        view.setScaleY(f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void watchVpn() {
        NetworkRequest networkRequestBuild = new NetworkRequest.Builder().addTransportType(4).removeCapability(15).removeCapability(12).build();
        this.vpnWatch = new ConnectivityManager.NetworkCallback() { // from class: dev.colorcc.Panel.7
            /* renamed from: lambda$onCapabilitiesChanged$0$dev-colorcc-Panel$7, reason: not valid java name */
            /* synthetic */ String m88lambda$onCapabilitiesChanged$0$devcolorccPanel$7(NetworkCapabilities networkCapabilities) throws Exception {
                return Panel.this.vpnName(networkCapabilities);
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onCapabilitiesChanged(Network network, final NetworkCapabilities networkCapabilities) {
                Panel.this.vpnNetworks.put(network, (String) Panel.guarded(new Callable() { // from class: dev.colorcc.Panel.7.0
                    @Override // java.util.concurrent.Callable
                    public final Object call() throws Exception {
                        return m88lambda$onCapabilitiesChanged$0$devcolorccPanel$7(networkCapabilities);
                    }
                }));
                final Panel panel = Panel.this;
                panel.guard(new Runnable() { // from class: dev.colorcc.Panel.7.1
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.access$4000(panel);
                    }
                });
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLost(Network network) {
                Panel.this.vpnNetworks.remove(network);
                final Panel panel = Panel.this;
                panel.guard(new Runnable() { // from class: dev.colorcc.Panel.7.2
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.access$4000(panel);
                    }
                });
            }
        };
        ((ConnectivityManager) getContext().getSystemService(ConnectivityManager.class)).registerNetworkCallback(networkRequestBuild, this.vpnWatch, this.main);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showVpn() {
        String str = null;
        for (String str2 : this.vpnNetworks.values()) {
            if (str2 != null) {
                str = str2;
            }
        }
        setTextIfChanged(this.vpn, str == null ? "VPN" : str);
        this.vpn.setContentDescription(str == null ? word("vpn_connected", new Object[0]) : word("vpn_connected_to", str));
        int i = this.vpnNetworks.isEmpty() ? 8 : 0;
        if (this.vpn.getVisibility() != i) {
            this.vpn.setVisibility(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String vpnName(NetworkCapabilities networkCapabilities) {
        String[] packagesForUid;
        PackageManager packageManager = getContext().getPackageManager();
        Object systemService = getContext().getSystemService("vpn_management");
        Integer num = (Integer) XposedHelpers.callStaticMethod(ActivityManager.class, "getCurrentUser", new Object[0]);
        num.intValue();
        Object objCallMethod = systemService == null ? null : XposedHelpers.callMethod(systemService, "getVpnConfig", new Object[]{num});
        String str = (objCallMethod == null || XposedHelpers.getBooleanField(objCallMethod, "legacy")) ? null : (String) XposedHelpers.getObjectField(objCallMethod, "user");
        if (str == null && networkCapabilities.getOwnerUid() > 0 && (packagesForUid = packageManager.getPackagesForUid(networkCapabilities.getOwnerUid())) != null && packagesForUid.length > 0) {
            str = packagesForUid[0];
        }
        if (str == null) {
            return null;
        }
        try {
            return packageManager.getApplicationLabel(packageManager.getApplicationInfo(str, 0)).toString();
        } catch (PackageManager.NameNotFoundException unused) {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static <T> T guarded(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Throwable th) {
            Hook.log("VPN name not read: " + th);
            return null;
        }
    }

    private LinearLayout.LayoutParams headerDisc(int i) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(32.0f), dp(32.0f));
        layoutParams.leftMargin = i;
        return layoutParams;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.stock.clear();
        for (int i = 0; i < this.container.getChildCount(); i++) {
            View childAt = this.container.getChildAt(i);
            String simpleName = childAt.getClass().getSimpleName();
            if (simpleName.equals("SecQSPanelComposeRoot") || simpleName.equals("SecQSPanel")) {
                this.stock.add(childAt);
            }
        }
        getViewTreeObserver().addOnPreDrawListener(this.preDraw);
        watchFrames();
        guard(new Runnable() { // from class: dev.colorcc.Panel.38
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.watchVpn();
            }
        });
        guard(new Runnable() { // from class: dev.colorcc.Panel.39
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m38lambda$onAttachedToWindow$25$devcolorccPanel();
            }
        });
        this.main.postDelayed(new Runnable() { // from class: dev.colorcc.Panel.40
            @Override // java.lang.Runnable
            public final void run() {
                Hook.clearCrashes();
            }
        }, 300000L);
        this.main.postDelayed(new Runnable() { // from class: dev.colorcc.Panel.41
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m40lambda$onAttachedToWindow$27$devcolorccPanel();
            }
        }, 4000L);
    }

    /* renamed from: lambda$onAttachedToWindow$25$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m38lambda$onAttachedToWindow$25$devcolorccPanel() {
        this.mediaSessions.addOnActiveSessionsChangedListener(this.sessionsListener, null, this.main);
        m14lambda$new$1$devcolorccPanel(this.mediaSessions.getActiveSessions(null));
    }

    /* renamed from: lambda$onAttachedToWindow$27$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m40lambda$onAttachedToWindow$27$devcolorccPanel() {
        guard(new Runnable() { // from class: dev.colorcc.Panel.10
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m39lambda$onAttachedToWindow$26$devcolorccPanel();
            }
        });
    }

    /* renamed from: lambda$onAttachedToWindow$26$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m39lambda$onAttachedToWindow$26$devcolorccPanel() {
        if (!this.open && this.currentTiles.isEmpty() && Hook.host != null) {
            syncTiles();
        }
        if (!this.open && Hook.adapter != null && Hook.host != null && supportedScreen()) {
            parkStock(true);
        }
        Glass.prepare();
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (Math.abs(unitNow() - this.density) > 0.01f) {
            this.main.post(new Runnable() { // from class: dev.colorcc.Panel.82
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m51lambda$onConfigurationChanged$29$devcolorccPanel();
                }
            });
        }
        int i = configuration.uiMode & 48;
        if (i != this.uiNight) {
            this.uiNight = i;
            this.releaseGpu.run();
            this.backdropLogged = false;
            Hook.themeLogs = 4;
            Hook.log("light or dark switched: the GPU blur is made again");
        }
    }

    /* renamed from: lambda$onConfigurationChanged$28$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m50lambda$onConfigurationChanged$28$devcolorccPanel() {
        Hook.remake(this);
    }

    /* renamed from: lambda$onConfigurationChanged$29$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m51lambda$onConfigurationChanged$29$devcolorccPanel() {
        guard(new Runnable() { // from class: dev.colorcc.Panel.85
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m50lambda$onConfigurationChanged$28$devcolorccPanel();
            }
        });
    }

    private float unitNow() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return large() ? metrics.density * 1.1f : Math.min(metrics.widthPixels, metrics.heightPixels) / 340.0f;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean large() {
        return getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    void retire() {
        this.audioThread.shutdown();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnPreDrawListener(this.preDraw);
        guard(new Runnable() { // from class: dev.colorcc.Panel.49
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m52lambda$onDetachedFromWindow$30$devcolorccPanel();
            }
        });
        guard(new Runnable() { // from class: dev.colorcc.Panel.50
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m53lambda$onDetachedFromWindow$31$devcolorccPanel();
            }
        });
        final ConnectivityManager.NetworkCallback networkCallback = this.vpnWatch;
        if (networkCallback != null) {
            this.vpnWatch = null;
            guard(new Runnable() { // from class: dev.colorcc.Panel.51
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m54lambda$onDetachedFromWindow$32$devcolorccPanel(networkCallback);
                }
            });
        }
        final Object obj = this.frameObserver;
        if (obj != null) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.52
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m55lambda$onDetachedFromWindow$33$devcolorccPanel(obj);
                }
            });
            this.frameObserver = null;
        }
        this.main.removeCallbacksAndMessages(null);
        this.motion.stopAll();
        this.media.setVisibility(0);
        this.playerBack = 0.0f;
        this.sheetBack = 0.0f;
        backdrop();
        if (this.open) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.53
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.onClose();
                }
            });
        }
        guard(new Runnable() { // from class: dev.colorcc.Panel.54
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.unbindTiles();
            }
        });
        showStock();
        resetStatusRow();
    }

    /* renamed from: lambda$onDetachedFromWindow$30$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m52lambda$onDetachedFromWindow$30$devcolorccPanel() {
        this.mediaSessions.removeOnActiveSessionsChangedListener(this.sessionsListener);
    }

    /* renamed from: lambda$onDetachedFromWindow$31$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m53lambda$onDetachedFromWindow$31$devcolorccPanel() {
        MediaController mediaController = this.controller;
        if (mediaController != null) {
            mediaController.unregisterCallback(this.mediaCallback);
        }
        this.controller = null;
    }

    /* renamed from: lambda$onDetachedFromWindow$32$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m54lambda$onDetachedFromWindow$32$devcolorccPanel(ConnectivityManager.NetworkCallback networkCallback) {
        ((ConnectivityManager) getContext().getSystemService(ConnectivityManager.class)).unregisterNetworkCallback(networkCallback);
    }

    /* renamed from: lambda$onDetachedFromWindow$33$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m55lambda$onDetachedFromWindow$33$devcolorccPanel(Object obj) {
        XposedHelpers.callMethod(getRootView(), "removeFrameMetricsListener", new Object[]{obj});
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        View view = this;
        int top = 0;
        int left = 0;
        while (view != null) {
            top += view.getTop();
            left += view.getLeft();
            Object parent = view.getParent();
            view = parent instanceof View ? (View) parent : null;
        }
        final int iMax = Math.max(0, dp(landscape() ? 36.0f : 64.0f) - top);
        this.headerPadding = iMax;
        measureScroll();
        if (iMax != this.column.getPaddingTop()) {
            post(new Runnable() { // from class: dev.colorcc.Panel.57
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m56lambda$onLayout$34$devcolorccPanel(iMax);
                }
            });
        }
        final int i5 = -top;
        final int i6 = -left;
        final int i7 = -Math.max(0, (getRootView().getWidth() - left) - getWidth());
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.sheet.getLayoutParams();
        if (layoutParams.topMargin != i5 || layoutParams.leftMargin != i6 || layoutParams.rightMargin != i7) {
            post(new Runnable() { // from class: dev.colorcc.Panel.58
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m57lambda$onLayout$35$devcolorccPanel(i5, i6, i7);
                }
            });
        }
        if (this.slide.running || this.closing || this.settleWorkPending) {
            this.layoutsDuringMotion++;
        }
        if (Glass.bending()) {
            post(new Runnable() { // from class: dev.colorcc.Panel.59
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m58lambda$onLayout$36$devcolorccPanel();
                }
            });
        }
        if (z) {
            post(new Runnable() { // from class: dev.colorcc.Panel.60
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.arrange();
                }
            });
        }
        Iterator<Body> it = this.staticBodies.iterator();
        while (it.hasNext()) {
            it.next().aim();
        }
        Iterator<Body> it2 = this.tileBodies.iterator();
        while (it2.hasNext()) {
            it2.next().aim();
        }
        assignRows();
    }

    /* renamed from: lambda$onLayout$34$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m56lambda$onLayout$34$devcolorccPanel(int i) {
        this.column.setPadding(dp(24.0f), i, dp(24.0f), 0);
        this.side.setPadding(dp(24.0f), i, dp(24.0f), 0);
    }

    /* renamed from: lambda$onLayout$35$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m57lambda$onLayout$35$devcolorccPanel(int i, int i2, int i3) {
        View[] viewArr = {this.sheet, this.player};
        for (int i4 = 0; i4 < 2; i4++) {
            View view = viewArr[i4];
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            layoutParams.topMargin = i;
            layoutParams.leftMargin = i2;
            layoutParams.rightMargin = i3;
            view.requestLayout();
        }
    }

    /* renamed from: lambda$onLayout$36$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m58lambda$onLayout$36$devcolorccPanel() {
        reglaze(this.content);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 3) {
            this.swipeOwner = 2;
        } else {
            trackSwipe(motionEvent);
        }
        if (this.interactive || motionEvent.getActionMasked() != 0) {
            return super.dispatchTouchEvent(motionEvent);
        }
        return false;
    }

    private void trackSwipe(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        int actionMasked = motionEvent.getActionMasked();
        int i = 2;
        if (actionMasked == 0) {
            PageSwipe.touching = true;
            this.swipeDownX = rawX;
            this.swipeDownY = rawY;
            if (this.scroller.running()) {
                Motion.Value value = this.scroller;
                value.snap(Math.max(0.0f, Math.min(this.scrollMax, value.value())));
            }
            if (isOverControl(rawX, rawY)) {
                i = 1;
            } else if (this.interactive && this.pager.pageCount > 1 && contains(this.pager, rawX, rawY)) {
                i = 0;
            }
            this.swipeOwner = i;
            if (this.pulling) {
                releasePull();
            }
            boolean z = this.interactive;
            this.pullArmed = z && this.swipeOwner != 1;
            this.pullOnOpen = !z;
            this.pulling = false;
            return;
        }
        if (actionMasked != 1) {
            if (actionMasked == 2) {
                if (this.swipeOwner == 0) {
                    int iSwipeOwner = Logic.swipeOwner(rawX - this.swipeDownX, rawY - this.swipeDownY, this.touchSlop, this.pager.canScrollHorizontally(-1), this.pager.canScrollHorizontally(1));
                    this.swipeOwner = iSwipeOwner;
                    if (iSwipeOwner != 0) {
                        Hook.log("tile swipe goes to ".concat(this.swipeOwner == 1 ? "pager" : "shade"));
                    }
                }
                if (this.pullArmed && Math.max(Math.abs(rawX - this.swipeDownX), Math.abs(rawY - this.swipeDownY)) > this.touchSlop) {
                    this.pullArmed = false;
                    boolean z2 = Math.abs(rawY - this.swipeDownY) > Math.abs(rawX - this.swipeDownX) && this.scrollMax > 0 && overCards(this.swipeDownX) && (rawY < this.swipeDownY || this.scroller.value() > 0.0f);
                    this.scrolling = z2;
                    this.pulling = !z2 && rawY - this.swipeDownY > Math.abs(rawX - this.swipeDownX);
                    this.pullFromY = this.swipeDownY + this.touchSlop;
                    this.scrollFromY = rawY;
                    this.scrollStart = this.scroller.value();
                    this.scrollSpeed = 0.0f;
                    this.scrollLastY = rawY;
                    this.scrollLastAt = motionEvent.getEventTime();
                }
                if (this.scrolling) {
                    this.scroller.snap(Math.max(0.0f, Math.min(this.scrollMax, this.scrollStart - ((rawY - this.scrollFromY) / this.column.getScaleY()))));
                    long eventTime = motionEvent.getEventTime() - this.scrollLastAt;
                    if (eventTime > 0) {
                        this.scrollSpeed = (this.scrollSpeed * 0.6f) + ((((rawY - this.scrollLastY) * 0.4f) * 1000.0f) / eventTime);
                        this.scrollLastY = rawY;
                        this.scrollLastAt = motionEvent.getEventTime();
                    }
                }
                if (this.pullOnOpen && this.interactive && !this.pulling && !this.overPulling) {
                    this.pullOnOpen = false;
                    this.pulling = true;
                    this.pullFromY = rawY;
                }
                if (this.pulling) {
                    dragPull(Logic.rubber(rawY - this.pullFromY, dp(140.0f)), motionEvent.getEventTime());
                    return;
                }
                return;
            }
            if (actionMasked != 3) {
                return;
            }
        }
        PageSwipe.touching = false;
        this.swipeOwner = 2;
        if (this.pulling) {
            releasePull();
        }
        if (this.scrolling) {
            float scaleY = (-this.scrollSpeed) / this.column.getScaleY();
            float fSqrt = (float) Math.sqrt(SCROLL_GLIDE[0]);
            Motion.Value value2 = this.scroller;
            value2.to(Math.max(0.0f, Math.min(this.scrollMax, value2.value() + (scaleY / fSqrt))));
            this.scroller.kick(scaleY);
        }
        this.scrolling = false;
        this.pullOnOpen = false;
        this.pullArmed = false;
        this.pulling = false;
    }

    boolean blocksShadeSlide(MotionEvent motionEvent) {
        trackSwipe(motionEvent);
        if (motionEvent.getActionMasked() == 2) {
            return this.swipeOwner != 2 || this.editor.active;
        }
        return false;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return this.pulling || this.scrolling || super.onInterceptTouchEvent(motionEvent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void tick() {
        if (this.dead) {
            return;
        }
        try {
            long jNanoTime = System.nanoTime();
            update();
            measureClose(jNanoTime, System.nanoTime());
        } catch (Throwable th) {
            Hook.log("panel disabled until SystemUI restarts: " + th);
            die();
        }
    }

    private void measureClose(long j, long j2) {
        if ((this.closing || this.slide.running || this.settleWorkPending) && getVisibility() == 0) {
            String str = this.slide.running ? "slide" : this.closing ? "close" : "open";
            if (this.closeFrames > 0 && !str.equals(this.motionKind)) {
                reportMotion();
            }
            this.motionKind = str;
            if (this.closeFrames > 0) {
                this.closeMaxGapNs = Math.max(this.closeMaxGapNs, j - this.closeLastNs);
            }
            this.closeLastNs = j;
            this.closeFrames++;
            long j3 = j2 - j;
            this.closeWorkNs += j3;
            this.closeMaxWorkNs = Math.max(this.closeMaxWorkNs, j3);
            return;
        }
        reportMotion();
    }

    private void reportMotion() {
        if (this.closeFrames == 0) {
            return;
        }
        String str = this.renderedFrames == 0 ? "UNMEASURED" : this.renderedSlow == 0 ? "PASS" : "FAIL";
        int length = STAGES.length;
        double[] dArr = new double[length];
        for (int i = 0; i < length; i++) {
            int i2 = this.renderedSlow;
            dArr[i] = i2 == 0 ? 0.0d : this.slowStages[i] / i2;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "%s %s: %d of %d frames over 16.7 ms; ColorCC per frame: tick avg %.2f max %.2f ms, draw avg %.2f max %.2f ms; longest gap %.1f ms", this.motionKind, str, Integer.valueOf(this.renderedSlow), Integer.valueOf(this.renderedFrames), Double.valueOf((this.closeWorkNs / 1000000.0d) / this.closeFrames), Double.valueOf(this.closeMaxWorkNs / 1000000.0d), Double.valueOf((this.drawNs / 1000000.0d) / this.closeFrames), Double.valueOf(this.drawMaxNs / 1000000.0d), Double.valueOf(this.closeMaxGapNs / 1000000.0d)));
        sb.append("; slow frames avg ");
        sb.append(this.renderedSlow == 0 ? "none" : stages(dArr));
        sb.append("; worst frame ");
        sb.append(this.worstFrame);
        sb.append("; icons built ");
        sb.append(this.iconBuilds);
        sb.append(", panel layouts ");
        sb.append(this.layoutsDuringMotion);
        sb.append("; redrawn by ");
        sb.append(this.redrawnBy.isEmpty() ? "nothing" : this.redrawnBy.toString());
        Hook.log(sb.toString());
        this.redrawnBy.clear();
        this.iconBuilds = 0;
        this.layoutsDuringMotion = 0;
        this.closeFrames = 0;
        this.drawMaxNs = 0L;
        this.drawNs = 0L;
        this.closeMaxWorkNs = 0L;
        this.closeWorkNs = 0L;
        this.closeMaxGapNs = 0L;
        resetFrameStats();
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void onDescendantInvalidated(View view, View view2) {
        super.onDescendantInvalidated(view, view2);
        if (this.closeFrames <= 0 || this.bodyViews.contains(view2)) {
            return;
        }
        this.redrawnBy.merge(view2.getClass().getName().replaceFirst(".*\\.", ""), 1, new BiFunction() { // from class: dev.colorcc.Panel.112
            @Override // java.util.function.BiFunction
            public final Object apply(Object obj, Object obj2) {
                return Integer.valueOf(Integer.sum(((Integer) obj).intValue(), ((Integer) obj2).intValue()));
            }
        });
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        long jNanoTime = System.nanoTime();
        super.dispatchDraw(canvas);
        if (this.closeFrames > 0) {
            long jNanoTime2 = System.nanoTime() - jNanoTime;
            this.drawNs += jNanoTime2;
            this.drawMaxNs = Math.max(this.drawMaxNs, jNanoTime2);
        }
    }

    private void resetFrameStats() {
        this.renderedSlow = 0;
        this.renderedFrames = 0;
        this.worstFrameNs = 0L;
        this.worstFrame = "not measured";
        Arrays.fill(this.slowStages, 0.0d);
    }

    private static String stages(double[] dArr) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < dArr.length) {
            sb.append(i == 0 ? "" : " ");
            sb.append(STAGE_NAMES[i]);
            sb.append(' ');
            sb.append(String.format(Locale.ROOT, "%.1f", Double.valueOf(dArr[i])));
            i++;
        }
        return sb.toString();
    }

    private void watchFrames() {
        try {
            Window.OnFrameMetricsAvailableListener onFrameMetricsAvailableListener = new Window.OnFrameMetricsAvailableListener() { // from class: dev.colorcc.Panel.15
                @Override // android.view.Window.OnFrameMetricsAvailableListener
                public final void onFrameMetricsAvailable(Window window, FrameMetrics frameMetrics, int i) {
                    Panel.this.m81lambda$watchFrames$38$devcolorccPanel(window, frameMetrics, i);
                }
            };
            this.frameWindow = (Window) XposedHelpers.newInstance(XposedHelpers.findClass("com.android.internal.policy.PhoneWindow", (ClassLoader) null), new Object[]{getContext()});
            XposedHelpers.callMethod(getRootView(), "addFrameMetricsListener", new Class[]{Window.class, Window.OnFrameMetricsAvailableListener.class, Handler.class}, new Object[]{this.frameWindow, onFrameMetricsAvailableListener, this.main});
            this.frameObserver = onFrameMetricsAvailableListener;
        } catch (Throwable th) {
            this.frameObserver = null;
            Hook.log("frame stages not measured: " + th);
        }
    }

    /* renamed from: lambda$watchFrames$38$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m81lambda$watchFrames$38$devcolorccPanel(Window window, final FrameMetrics frameMetrics, int i) {
        if (this.closing || this.slide.running || this.settleWorkPending) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.47
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m80lambda$watchFrames$37$devcolorccPanel(frameMetrics);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: onFrame, reason: merged with bridge method [inline-methods] */
    public void m80lambda$watchFrames$37$devcolorccPanel(FrameMetrics frameMetrics) {
        long metric = frameMetrics.getMetric(8);
        this.renderedFrames++;
        if (metric > 16700000) {
            this.renderedSlow++;
            int i = 0;
            while (true) {
                int[] iArr = STAGES;
                if (i >= iArr.length) {
                    break;
                }
                double[] dArr = this.slowStages;
                dArr[i] = dArr[i] + stage(frameMetrics, iArr[i]);
                i++;
            }
        }
        if (metric <= this.worstFrameNs) {
            return;
        }
        this.worstFrameNs = metric;
        int length = STAGES.length;
        double[] dArr2 = new double[length];
        for (int i2 = 0; i2 < length; i2++) {
            dArr2[i2] = stage(frameMetrics, STAGES[i2]);
        }
        this.worstFrame = String.format(Locale.ROOT, "%.1f ms: ", Double.valueOf(metric / 1000000.0d)) + stages(dArr2);
    }

    private static double stage(FrameMetrics frameMetrics, int i) {
        return frameMetrics.getMetric(i) / 1000000.0d;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void die() {
        if (this.liveRadius > 0) {
            this.liveOn = false;
            guard(new Runnable() { // from class: dev.colorcc.Panel.25
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m8lambda$die$39$devcolorccPanel();
                }
            });
        }
        this.dead = true;
        this.interactive = false;
        setVisibility(8);
        showStock();
        resetStatusRow();
        Hook.gone();
    }

    /* renamed from: lambda$die$39$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m8lambda$die$39$devcolorccPanel() {
        m65lambda$onShadeExpansion$42$devcolorccPanel(0.0f);
    }

    /*
     * [Miku-Picked UI] Rebuilt from the bytecode (jadx could not decompile it). One UI's
     * SecQSPanelComposeAdapter flows (expansionFraction; detailVisible, legacyDetailVisible,
     * legacyCustomizerVisible, editMode) now come from SystemUI through Hook.
     */
    private void update() {
        holdInPlace();
        boolean attached = Hook.attached();
        float fClamp = attached ? Logic.clamp(Hook.expansion()) : 0.0f;
        boolean stockFlows = attached && Hook.stockScreen();
        if (!stockFlows) {
            Hook.detailClosing = false;
        }
        long jUptimeMillis = SystemClock.uptimeMillis();
        if (stockFlows && !this.lastStockFlows) {
            this.stockFlowsSince = jUptimeMillis;
        }
        this.lastStockFlows = stockFlows;
        if (Hook.detailClosing && jUptimeMillis - this.stockFlowsSince < STOCK_CLOSE_GRACE_MS) {
            Hook.detailClosing = false;
        }
        boolean z3 = stockFlows && !Hook.detailClosing;
        boolean z4 = this.sawStockScreen;
        if (z3) {
            if (!z4) {
                this.stockScreenSince = jUptimeMillis;
                this.main.postDelayed(new Runnable() {
                    @Override
                    public final void run() {
                        Panel.this.tick();
                    }
                }, 181L);
            }
            this.sawStockScreen = true;
        } else if (z4) {
            this.sawStockScreen = false;
            this.override = false;
        } else if (this.override && jUptimeMillis - this.overrideSince >= STOCK_SCREEN_TIMEOUT_MS) {
            this.override = false;
        }
        if (fClamp <= 0.001f) {
            this.override = false;
        }
        boolean zSplitEnabled = Hook.splitEnabled();
        if (zSplitEnabled && !locked()) {
            fClamp = Hook.shadeFraction(fClamp);
        }
        boolean zIsControlsPageState = Hook.isControlsPageState();
        boolean z5 = !attached || Hook.host == null || this.override || z3 || !supportedScreen();
        boolean zIsSliding = Hook.isSliding();
        boolean zOnControlPage = Logic.onControlPage(zSplitEnabled, zIsControlsPageState);
        if (zOnControlPage != this.pendingPage) {
            this.pendingPage = zOnControlPage;
            this.pendingPageSince = jUptimeMillis;
            if (!zIsSliding) {
                this.main.postDelayed(new Runnable() {
                    @Override
                    public final void run() {
                        Panel.this.tick();
                    }
                }, 41L);
            }
        }
        if (zIsSliding || fClamp <= 0.5f || jUptimeMillis - this.pendingPageSince >= PAGE_SETTLE_MS) {
            this.page = zOnControlPage;
        }
        boolean z6 = this.slide.running;
        float fDownToControls = Hook.downToControls();
        if (z5) {
            fDownToControls = 0.0f;
        } else if (fDownToControls < 0.0f) {
            if (z6) {
                fDownToControls = this.slide.away ? 0.0f : 1.0f;
            } else if (this.slide.waitingToSlideIn()) {
                fDownToControls = 0.0f;
            } else {
                fDownToControls = Logic.revealTarget(fClamp, this.page);
            }
        }
        logState(zSplitEnabled, zIsControlsPageState, fClamp, z5);
        if (fDownToControls > 0.0f && !this.open) {
            onOpen();
        } else if (fDownToControls <= 0.0f && this.open) {
            onClose();
        }
        boolean z7 = Logic.takesTouches(fClamp, fDownToControls) && (!z6 || this.slide.target == 1.0f);
        this.interactive = z7;
        if (!z7 && (this.pulling || this.pullArmed)) {
            this.pullArmed = false;
            this.pulling = false;
            releasePull();
        }
        whenSettled();
        if (fDownToControls > 0.0f && getVisibility() != 0) {
            setVisibility(0);
        }
        long jNanoTime = System.nanoTime();
        if (this.open && !this.closing && fClamp > this.lastFraction) {
            if (jNanoTime - this.lastFractionAt > 4000000) {
                float fOpenPace = Logic.openPace((fClamp - this.lastFraction) / ((jNanoTime - this.lastFractionAt) / 1.0E9f));
                if (fOpenPace > this.openPace) {
                    this.openPace = fOpenPace;
                }
            }
        } else if (this.open && this.closing && fClamp < this.lastFraction
                && jNanoTime - this.lastFractionAt > 4000000) {
            float fClosePace = Logic.openPace((this.lastFraction - fClamp) / ((jNanoTime - this.lastFractionAt) / 1.0E9f));
            if (fClosePace > this.closePace) {
                this.closePace = fClosePace;
            }
        }
        this.lastFraction = fClamp;
        this.lastFractionAt = jNanoTime;
        boolean zClosing = Logic.closing(this.closing, fDownToControls, this.turnFrom);
        boolean z9 = this.closing;
        if (zClosing != z9) {
            this.closing = zClosing;
            this.turnFrom = fDownToControls;
            this.closeRide.to(zClosing ? 1.0f : 0.0f);
            if (this.closing) {
                this.closePace = 1.0f;
            } else {
                Hook.fadeShadeBlur(1.0f - Logic.clamp(this.peek.value()));
            }
        } else {
            this.turnFrom = z9 ? Math.min(this.turnFrom, fDownToControls) : Math.max(this.turnFrom, fDownToControls);
        }
        if (this.closing && stillBehind()) {
            Hook.fadeShadeBlur(Logic.clamp(fClamp) * (1.0f - Logic.clamp(this.peek.value())));
        }
        float fClamp2 = this.closing ? Logic.clamp(2.0f * fDownToControls) : 1.0f;
        if (this.sheet.getAlpha() != fClamp2) {
            this.sheet.setAlpha(fClamp2);
        }
        if (this.player.getAlpha() != fClamp2) {
            this.player.setAlpha(fClamp2);
        }
        boolean zStepRows = stepRows(fDownToControls, SystemClock.uptimeMillis());
        if (fDownToControls <= 0.0f && zStepRows && getVisibility() != 4) {
            setVisibility(4);
        }
        boolean z10 = !attached || Hook.host == null || !supportedScreen();
        boolean z11 = z10 || (z3 && jUptimeMillis - this.stockScreenSince >= STOCK_SCREEN_DELAY_MS);
        boolean z12 = z10 || z3;
        this.stockNeeded = z12;
        if (z12) {
            parkStock(false);
        }
        if (z11) {
            this.stockValue.to(1.0f);
        } else {
            this.stockValue.snap(0.0f);
        }
        setStockHidden(!z11);
        float fClamp3 = Logic.clamp(this.stockValue.value());
        for (View view : this.stock) {
            if (view.getAlpha() != fClamp3) {
                view.setAlpha(fClamp3);
            }
        }
        swapStatusRow();
        if (this.interactive && this.page && !this.stockNeeded && !locked()) {
            showStatusRow();
        }
    }

    private void showStatusRow() {
        String str;
        View view = this.statusRow;
        if (!this.statusRowLogged) {
            this.statusRowLogged = true;
            if (view == null) {
                Hook.log("status row: not found");
            } else {
                int[] iArr = new int[2];
                view.getLocationOnScreen(iArr);
                View view2 = view.getParent() instanceof View ? (View) view.getParent() : null;
                StringBuilder sb = new StringBuilder("status row: visibility ");
                sb.append(view.getVisibility());
                sb.append(", alpha ");
                sb.append(view.getAlpha());
                sb.append(", moved ");
                sb.append(view.getTranslationY());
                sb.append(", at ");
                sb.append(iArr[1]);
                sb.append(", ");
                sb.append(view.getWidth());
                sb.append("x");
                sb.append(view.getHeight());
                sb.append(", shown ");
                sb.append(view.isShown());
                if (view2 == null) {
                    str = "";
                } else {
                    str = ", parent " + view2.getClass().getSimpleName() + " alpha " + view2.getAlpha() + " visibility " + view2.getVisibility();
                }
                sb.append(str);
                Hook.log(sb.toString());
            }
        }
        if (view == null) {
            return;
        }
        if (view.getVisibility() == 4) {
            view.setVisibility(0);
        }
        if (view.getAlpha() < 1.0f) {
            view.setAlpha(1.0f);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public View stockPage() {
        View view = this.stockPage;
        if (view == null || !view.isAttachedToWindow()) {
            this.stockPage = null;
            Iterator<View> it = this.stock.iterator();
            if (it.hasNext()) {
                View next = it.next();
                View viewComposeIn = composeIn(next);
                this.stockPage = viewComposeIn;
                if (viewComposeIn == null) {
                    this.stockPage = next;
                }
            }
        }
        View view2 = this.stockPage;
        return view2 != null ? view2 : this.container;
    }

    private static View composeIn(View view) {
        if (view.getClass().getName().equals("androidx.compose.ui.platform.AndroidComposeView")) {
            return view;
        }
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        ViewGroup viewGroup = (ViewGroup) view;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View viewComposeIn = composeIn(viewGroup.getChildAt(i));
            if (viewComposeIn != null) {
                return viewComposeIn;
            }
        }
        return null;
    }

    private void holdInPlace() {
        if (getVisibility() != 0) {
            return;
        }
        View view = this.container;
        int top = 0;
        int left = 0;
        while (view != null) {
            top += view.getTop();
            left += view.getLeft();
            Object parent = view.getParent();
            view = parent instanceof View ? (View) parent : null;
        }
        this.container.getLocationOnScreen(this.containerAt);
        this.heldTop = top - this.containerAt[1];
        this.heldX = (this.slide.running || Hook.isSliding()) ? 0.0f : left - this.containerAt[0];
        applySheet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applySheet() {
        float fSideAt;
        float fMin;
        float fPage = this.slide.running ? this.slide.page() : 1.0f;
        if (this.slide.running) {
            int width = getRootView().getWidth();
            fSideAt = this.slide.sideAt(fPage, width);
            SideSlide sideSlide = this.slide;
            float fSideAt2 = sideSlide.sideAt(sideSlide.tail(), width);
            fMin = Math.min(Math.abs(fSideAt - fSideAt2), getWidth() * 0.05f);
            if (fSideAt > fSideAt2) {
                fSideAt -= fMin;
            }
        } else {
            fSideAt = 0.0f;
            fMin = 0.0f;
        }
        float fMax = fMin / Math.max(1, getWidth());
        if (getPivotX() != 0.0f) {
            setPivotX(0.0f);
        }
        setScaleX(fMax + 1.0f);
        setScaleY(1.0f - (fMax * 0.25f));
        setAlpha(SideSlide.alphaAt(fPage));
        setTranslationX(this.heldX + fSideAt);
        setTranslationY(this.heldTop * (1.0f - this.closeRide.value()));
        if (this.slide.running != this.sheetLayered) {
            boolean z = this.slide.running;
            this.sheetLayered = z;
            setLayerType(z ? 2 : 0, null);
        }
    }

    private void swapStatusRow() {
        float alpha;
        float fStatusShift = statusShift();
        float scrollY = this.column.getScrollY() - fStatusShift;
        if (this.headerRow.getTranslationY() != scrollY) {
            this.headerRow.setTranslationY(scrollY);
        }
        if (this.statusRow == null) {
            return;
        }
        if (getVisibility() == 0) {
            alpha = fStatusShift * this.pencil.getAlpha() * getAlpha() * (this.editor.getVisibility() == 0 ? this.content.getAlpha() : 1.0f);
        } else {
            alpha = 0.0f;
        }
        if (alpha != this.shownStatusShift) {
            this.shownStatusShift = alpha;
            this.statusRow.setTranslationY(alpha);
        }
    }

    private void resetStatusRow() {
        View view = this.statusRow;
        if (view != null) {
            view.setTranslationY(0.0f);
        }
        this.shownStatusShift = 0.0f;
        this.statusSearched = false;
        this.statusCarrier = null;
        this.statusRow = null;
    }

    private float statusShift() {
        float f = 0.0f;
        if (!this.twoPane && !landscape()) {
            if (!this.statusSearched) {
                this.statusSearched = true;
                int identifier = getResources().getIdentifier("split_shade_status_bar", "id", "com.android.systemui");
                int identifier2 = getResources().getIdentifier("carrier_group", "id", "com.android.systemui");
                View viewFindViewById = null;
                View viewFindViewById2 = identifier == 0 ? null : getRootView().findViewById(identifier);
                this.statusRow = viewFindViewById2;
                if (viewFindViewById2 != null && identifier2 != 0) {
                    viewFindViewById = viewFindViewById2.findViewById(identifier2);
                }
                this.statusCarrier = viewFindViewById;
                if (viewFindViewById == null) {
                    Hook.log("status row not found, the buttons stay below it");
                }
            }
            View view = this.statusCarrier;
            if (view != null && view.getHeight() != 0 && this.headerRow.getHeight() != 0) {
                int iLayoutTop = layoutTop(this.headerRow);
                f = iLayoutTop;
                float height = ((this.headerRow.getHeight() / 2.0f) + f) - (layoutTop(this.statusCarrier) + (this.statusCarrier.getHeight() / 2.0f));
                boolean z = height > ((float) dp(8.0f)) && f - height >= ((float) layoutTop(this));
                f = z ? height : 0.0f;
                if (Math.abs(f - this.loggedShift) >= 1.0f) {
                    this.loggedShift = f;
                    StringBuilder sb = new StringBuilder("status row swap ");
                    sb.append(Math.round(height));
                    sb.append(" px, buttons top ");
                    sb.append(iLayoutTop);
                    sb.append(", panel top ");
                    sb.append(layoutTop(this));
                    sb.append(z ? "" : ", not done: the buttons would leave the panel");
                    Hook.log(sb.toString());
                }
            }
        }
        return f;
    }

    private static int layoutTop(View view) {
        int top = 0;
        while (view != null) {
            top += view.getTop();
            Object parent = view.getParent();
            view = parent instanceof View ? (View) parent : null;
        }
        return top;
    }

    boolean handleBack() {
        if (this.dead || !this.open) {
            return false;
        }
        if (this.editor.active) {
            Hook.log("back closes the editor without saving");
            guard(new Runnable() { // from class: dev.colorcc.Panel.79
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m11lambda$handleBack$40$devcolorccPanel();
                }
            });
            return true;
        }
        if (this.sheet.tile != null || this.sheet.volumesOpen || this.sheet.brightnessOpen || this.sheet.outputOpen) {
            Hook.log("back closes a menu");
            final Sheet sheet = this.sheet;
            Objects.requireNonNull(sheet);
            guard(new Runnable() { // from class: dev.colorcc.Panel.80
                @Override // java.lang.Runnable
                public final void run() {
                    sheet.close();
                }
            });
            return true;
        }
        if (this.player.opened) {
            Hook.log("back closes the player");
            final Player player = this.player;
            Objects.requireNonNull(player);
            guard(new Runnable() { // from class: dev.colorcc.Panel.81
                @Override // java.lang.Runnable
                public final void run() {
                    player.close();
                }
            });
            return true;
        }
        if (this.sheet.getVisibility() != 0 && this.player.getVisibility() != 0 && this.editor.getVisibility() != 0) {
            return false;
        }
        Hook.log("back while a menu closes, kept");
        return true;
    }

    /* renamed from: lambda$handleBack$40$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m11lambda$handleBack$40$devcolorccPanel() {
        this.editor.close(false);
    }

    boolean showing() {
        return this.open && !this.dead;
    }

    /**
     * [Miku-Picked UI] Whether the cards can still scroll {@code down} (towards the tiles) or
     * up, so SystemUI keeps the shade from taking that drag (One UI asked through its own
     * touch hooks instead).
     */
    boolean canScrollCards(boolean down) {
        if (this.dead || !this.interactive || this.scrollMax <= 0) {
            return false;
        }
        float at = this.scroller.value();
        return down ? at < this.scrollMax - 1 : at > 0.0f;
    }

    void onShadeExpansion(final float f) {
        if (this.dead) {
            return;
        }
        guard(new Runnable() { // from class: dev.colorcc.Panel.43
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m64lambda$onShadeExpansion$41$devcolorccPanel(f);
            }
        });
        guard(new Runnable() { // from class: dev.colorcc.Panel.44
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m65lambda$onShadeExpansion$42$devcolorccPanel(f);
            }
        });
    }

    /* renamed from: lambda$onShadeExpansion$41$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m64lambda$onShadeExpansion$41$devcolorccPanel(float f) {
        this.slide.onExpansion(f);
    }

    void onOverExpansion(float f, boolean z) {
        if (this.dead || !this.open || this.pulling) {
            return;
        }
        if (z && f > 0.0f) {
            this.overPulling = true;
            dragPull(Logic.rubber(f, dp(140.0f)), SystemClock.uptimeMillis());
        } else if (this.overPulling) {
            this.overPulling = false;
            releasePull();
        }
    }

    private void dragPull(float f, long j) {
        long j2 = j - this.pullAt;
        if (j2 > 0) {
            this.pullSpeed = j2 > 100 ? 0.0f : (this.pullSpeed * 0.6f) + ((((f - this.pull.value()) * 0.4f) * 1000.0f) / j2);
            this.pullAt = j;
        }
        this.pull.snap(f);
    }

    private void releasePull() {
        float fDp = dp(6000.0f);
        this.pull.to(0.0f);
        this.pull.kick(Math.max(-fDp, Math.min(fDp, this.pullSpeed)));
        this.pullSpeed = 0.0f;
    }

    boolean liveBackground() {
        WindowManager windowManager;
        return (this.dead || this.liveBroken || !Hook.liveBlurAvailable() || !replacesStockGrid() || Hook.samsungLive() || this.keyguard.isKeyguardLocked() || (windowManager = (WindowManager) getContext().getSystemService(WindowManager.class)) == null || !windowManager.isCrossWindowBlurEnabled()) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: applyLiveBlur, reason: merged with bridge method [inline-methods] */
    public void m65lambda$onShadeExpansion$42$devcolorccPanel(float f) {
        Object objCallMethod;
        int i;
        String str;
        if (f > 0.0f && this.lastExpansion <= 0.0f) {
            this.liveOn = liveBackground();
            if (Hook.notifGlass) {
                Glass.noteRoot = noteStack();
                if (this.gpuWorks && Glass.bending()) {
                    Glass.staleBackdrop();
                }
                this.quickCapture = true;
                guard(new AnonymousClass27());
            }
            this.pullStartedAt = SystemClock.uptimeMillis();
            this.pullLogged = false;
        } else if (f <= 0.0f && this.lastExpansion > 0.0f) {
            Hook.staleShadeBlur();
        }
        if (!this.pullLogged && f >= 0.999f && (i = this.pullsLogged) < 6) {
            this.pullLogged = true;
            this.pullsLogged = i + 1;
            StringBuilder sb = new StringBuilder("pull: shade open in ");
            sb.append(SystemClock.uptimeMillis() - this.pullStartedAt);
            sb.append(" ms, background ");
            if (this.liveOn) {
                str = "ColorCC live blur";
            } else if (Hook.samsungLive()) {
                str = "Samsung live blur at " + Hook.blurCurve();
            } else {
                str = "still picture";
            }
            sb.append(str);
            Hook.log(sb.toString());
        }
        this.lastExpansion = f;
        float fClamp = 1.0f - Logic.clamp(this.peek.value());
        int iMax = Math.max(1, dp(2.0f));
        int iRound = this.liveOn ? Math.round(((dp(LIVE_BLUR[this.blur]) * Logic.clamp(f)) * fClamp) / iMax) * iMax : 0;
        int iRound2 = iRound < 1 ? 0 : (Math.round((fClamp * 102.0f) * Logic.clamp(f)) & (-8)) << 24;
        if (iRound == this.liveRadius && iRound2 == this.liveDim) {
            return;
        }
        this.liveRadius = iRound;
        this.liveDim = iRound2;
        try {
            ClassLoader classLoader = getContext().getClassLoader();
            if (iRound > 0) {
                Object objNewInstance = XposedHelpers.newInstance(XposedHelpers.findClass("android.view.SemBlurInfo$Builder", classLoader), new Object[]{0});
                XposedHelpers.callMethod(objNewInstance, "setRadius", new Object[]{Integer.valueOf(iRound)});
                XposedHelpers.callMethod(objNewInstance, "setBackgroundColor", new Object[]{Integer.valueOf(iRound2)});
                objCallMethod = XposedHelpers.callMethod(objNewInstance, "build", new Object[0]);
            } else {
                objCallMethod = null;
            }
            XposedHelpers.callMethod(getRootView(), "semSetBlurInfo", new Class[]{XposedHelpers.findClass("android.view.SemBlurInfo", classLoader)}, new Object[]{objCallMethod});
            this.liveFailures = 0;
        } catch (Throwable th) {
            int i2 = this.liveFailures + 1;
            this.liveFailures = i2;
            this.liveBroken = i2 >= 3;
            this.liveOn = false;
            StringBuilder sb2 = new StringBuilder("live background failed");
            sb2.append(this.liveBroken ? " three times, Samsung's still picture stays" : ", tried again next pull");
            sb2.append(": ");
            sb2.append(th);
            Hook.log(sb2.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onTilt(float[] fArr) {
        float f;
        for (int i = 0; i < 3; i++) {
            float[] fArr2 = this.gravity;
            if (this.gravityRead) {
                float f2 = fArr2[i];
                f = f2 + ((fArr[i] - f2) * 0.2f);
            } else {
                f = fArr[i];
            }
            fArr2[i] = f;
        }
        this.gravityRead = true;
        long jUptimeMillis = SystemClock.uptimeMillis();
        if (this.editor.getVisibility() != 0 && jUptimeMillis - this.tiltDrawnAt >= 33) {
            float[] fArr3 = this.gravity;
            if (Glass.setLight(fArr3[0] * 2.0f, (-fArr3[1]) - 2.5f)) {
                this.tiltDrawnAt = jUptimeMillis;
                reglaze(this);
            }
        }
    }

    private void watchTilt(boolean z) {
        SensorManager sensorManager = (SensorManager) getContext().getSystemService(SensorManager.class);
        if (sensorManager == null) {
            return;
        }
        if (!z) {
            sensorManager.unregisterListener(this.tilt);
            return;
        }
        Sensor defaultSensor = sensorManager.getDefaultSensor(9);
        if (defaultSensor == null) {
            defaultSensor = sensorManager.getDefaultSensor(1);
        }
        if (defaultSensor == null) {
            return;
        }
        this.gravityRead = false;
        sensorManager.registerListener(this.tilt, defaultSensor, 1, this.main);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setReveal(float f) {
        boolean z = f >= 1.0f;
        Iterator<Body> it = this.staticBodies.iterator();
        while (it.hasNext()) {
            it.next().place(z);
        }
        Iterator<Body> it2 = this.tileBodies.iterator();
        while (it2.hasNext()) {
            it2.next().place(z);
        }
    }

    private float shortSide() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    private boolean stepRows(float f, long j) {
        float f2;
        float f3;
        Iterator it = Arrays.asList(this.staticBodies, this.tileBodies).iterator();
        boolean z = true;
        while (it.hasNext()) {
            for (Body body : (List<Body>) it.next()) {
                if (this.closing) {
                    f2 = 0.68f - ((this.lastRow - (body.header ? -1 : body.row)) * 0.035f);
                    f3 = 0.32f;
                } else {
                    f2 = body.header ? 0.0f : (body.row * 0.045f) + 0.06f;
                    f3 = 0.42f;
                }
                body.follow(Logic.clamp((f - f2) / f3));
                z &= body.drop.value() <= 0.004f && !body.drop.running();
            }
        }
        return f <= 0.0f && z;
    }

    private void assignRows() {
        ArrayList<Float> arrayList = new ArrayList();
        ArrayList<Body> arrayList2 = new ArrayList(this.staticBodies);
        arrayList2.addAll(this.tileBodies);
        for (Body body : arrayList2) {
            body.header = false;
            for (ViewParent parent = body.view.getParent(); parent instanceof View; parent = parent.getParent()) {
                if (parent == this.headerRow) {
                    body.header = true;
                }
            }
            if (!body.header) {
                arrayList.add(Float.valueOf(body.piece.topY));
            }
        }
        Collections.sort(arrayList);
        ArrayList arrayList3 = new ArrayList();
        for (Float f : arrayList) {
            float fFloatValue = f.floatValue();
            if (arrayList3.isEmpty() || fFloatValue - ((Float) arrayList3.get(arrayList3.size() - 1)).floatValue() > dp(24.0f)) {
                arrayList3.add(f);
            }
        }
        this.lastRow = 0;
        for (Body body2 : arrayList2) {
            if (!body2.header) {
                int i = 0;
                for (int i2 = 0; i2 < arrayList3.size(); i2++) {
                    if (body2.piece.topY >= ((Float) arrayList3.get(i2)).floatValue() - dp(24.0f)) {
                        i = i2;
                    }
                }
                body2.row = Math.min(23, i);
                body2.in = Logic.Spring.of((body2.row * 0.012f) + 0.24f, 0.38f);
                this.lastRow = Math.max(this.lastRow, body2.row);
            }
        }
    }

    private void logState(boolean z, boolean z2, float f, boolean z3) {
        String str;
        int i = f <= 0.0f ? 0 : f >= 1.0f ? 1 : 2;
        int i2 = (z ? 1 : 0) | (z2 ? 2 : 0) | (z3 ? 4 : 0) | (i << 3);
        if (i2 == this.lastLogKey) {
            return;
        }
        this.lastLogKey = i2;
        if (i == 0) {
            str = "0";
        } else {
            str = i == 1 ? "1" : "mid";
        }
        Hook.log("split=" + z + " qsState=" + z2 + " fraction=" + str + " stock=" + z3);
    }

    private void showStock() {
        parkStock(false);
        this.stockValue.snap(1.0f);
        setStockHidden(false);
    }

    private void parkStock(boolean z) {
        ViewGroup viewGroup;
        View viewComposeChild;
        if (!z) {
            for (int i = 0; i < this.parkedViews.size(); i++) {
                ViewGroup viewGroup2 = this.parkedIn.get(i);
                if (this.parkedViews.get(i).getParent() == null && viewGroup2.isAttachedToWindow() && composeChild(viewGroup2) == null) {
                    viewGroup2.addView(this.parkedViews.get(i));
                }
            }
            if (!this.parkedViews.isEmpty()) {
                Hook.log("stock quick panel put back");
            }
            this.parkedViews.clear();
            this.parkedIn.clear();
            return;
        }
        boolean zIsEmpty = this.parkedViews.isEmpty();
        for (View view : this.stock) {
            if ((view instanceof ViewGroup) && (viewComposeChild = composeChild((viewGroup = (ViewGroup) view))) != null) {
                viewGroup.removeView(viewComposeChild);
                this.parkedViews.add(viewComposeChild);
                this.parkedIn.add(viewGroup);
            }
        }
        if (zIsEmpty && !this.parkedViews.isEmpty()) {
            Hook.log("stock quick panel parked");
        }
        markCollapsible();
    }

    boolean stockParked() {
        return !this.parkedViews.isEmpty();
    }

    private void markCollapsible() {
        final Object obj = Hook.adapter;
        if (!stockParked() || obj == null) {
            return;
        }
        guard(new Runnable() { // from class: dev.colorcc.Panel.63
            @Override // java.lang.Runnable
            public final void run() {
                Panel.lambda$markCollapsible$43(obj);
            }
        });
    }

    static /* synthetic */ void lambda$markCollapsible$43(Object obj) {
        XposedHelpers.callMethod(XposedHelpers.getObjectField(obj, "_isCollapsible"), "setValue", new Object[]{Boolean.TRUE});
    }

    private static View composeChild(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt.getClass().getName().equals("androidx.compose.ui.platform.ComposeView")) {
                return childAt;
            }
        }
        return null;
    }

    private void setStockHidden(boolean z) {
        if (z == this.stockHidden) {
            return;
        }
        this.stockHidden = z;
        Iterator<View> it = this.stock.iterator();
        while (it.hasNext()) {
            it.next().setImportantForAccessibility(z ? 4 : 0);
        }
    }

    private boolean supportedScreen() {
        return (!large() || this.foldable) && getDisplay() != null && getDisplay().getDisplayId() == 0;
    }

    private void onOpen() {
        long jUptimeMillis = SystemClock.uptimeMillis();
        if (large()) {
            StringBuilder sb = new StringBuilder("open on the large screen: panel ");
            sb.append(getWidth());
            sb.append("x");
            sb.append(getHeight());
            sb.append(" in ");
            sb.append(this.container.getClass().getSimpleName());
            sb.append(" ");
            sb.append(this.container.getWidth());
            sb.append("x");
            sb.append(this.container.getHeight());
            sb.append(", ");
            sb.append(this.twoPane ? "two panes" : "one column");
            sb.append(", page shown ");
            sb.append(stockPage().isShown());
            Hook.log(sb.toString());
        }
        Hook.fadeShadeBlur(1.0f);
        openNow();
        markCollapsible();
        logSlow("open", jUptimeMillis);
    }

    private void logSlow(String str, long j) {
        long jUptimeMillis = SystemClock.uptimeMillis() - j;
        if (jUptimeMillis >= 8) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(" took ");
            sb.append(jUptimeMillis);
            sb.append(" ms");
            sb.append(locked() ? " on the lock screen" : "");
            Hook.log(sb.toString());
        }
    }

    boolean locked() {
        long jUptimeMillis = SystemClock.uptimeMillis();
        if (jUptimeMillis - this.lockedReadAt > 500) {
            this.lockedNow = this.keyguard.isKeyguardLocked();
            this.lockedReadAt = jUptimeMillis;
        }
        return this.lockedNow;
    }

    private void openNow() {
        this.open = true;
        this.closeRide.snap(0.0f);
        this.backdropSum = 0L;
        this.scroller.snap(0.0f);
        this.pager.home();
        this.openPace = 1.0f;
        if (this.currentTiles.isEmpty()) {
            syncTiles();
        }
        setTilesListening();
        readControlsInBackground();
        this.main.removeCallbacks(this.releaseGpu);
        boolean z = false;
        Glass.dark = (getResources().getConfiguration().uiMode & 48) == 32;
        if (this.gpuWorks && Glass.bending()) {
            z = true;
        }
        if (z) {
            Glass.staleBackdrop();
        }
        if (z || !Glass.bending() || stillBehind()) {
            this.quickCapture = true;
            guard(new AnonymousClass27());
        }
        this.settleWorkPending = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setTilesListening() {
        Iterator<Bound> it = this.bound.iterator();
        while (it.hasNext()) {
            XposedHelpers.callMethod(it.next().tile, "setListening", new Object[]{this, true});
        }
    }

    private void readControlsInBackground() {
        final Display display = getDisplay();
        new Thread(new Runnable() { // from class: dev.colorcc.Panel.9
            @Override // java.lang.Runnable
            public final void run() throws SecurityException, IllegalArgumentException {
                Panel.this.m69lambda$readControlsInBackground$46$devcolorccPanel(display);
            }
        }, "ColorCC-read").start();
    }

    /* renamed from: lambda$readControlsInBackground$46$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m69lambda$readControlsInBackground$46$devcolorccPanel(Display display) throws SecurityException, IllegalArgumentException {
        Process.setThreadPriority(10);
        try {
            try {
                Object objCallMethod = XposedHelpers.callMethod(display, "getBrightnessInfo", new Object[0]);
                float floatField = XposedHelpers.getFloatField(objCallMethod, "brightnessMinimum");
                float floatField2 = XposedHelpers.getFloatField(objCallMethod, "brightnessMaximum");
                if (floatField2 > floatField) {
                    this.brightMin = floatField;
                    this.brightMax = floatField2;
                }
            } catch (Throwable th) {
                Hook.log("brightness range not read, 0 to 1 used: " + th);
            }
            final float fFloatValue = ((Float) XposedHelpers.callMethod(this.display, "getBrightness", new Object[]{0})).floatValue();
            final int streamMaxVolume = this.audio.getStreamMaxVolume(this.volumeStream);
            final int streamVolume = this.audio.getStreamVolume(this.volumeStream);
            final int iIntValue = ((Integer) XposedHelpers.callMethod(this.audio, "getRingerModeInternal", new Object[0])).intValue();
            this.main.post(new Runnable() { // from class: dev.colorcc.Panel.16
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m68lambda$readControlsInBackground$45$devcolorccPanel(fFloatValue, iIntValue, streamVolume, streamMaxVolume);
                }
            });
        } catch (Throwable th2) {
            Hook.log("controls not read: " + th2);
        }
    }

    /* renamed from: lambda$readControlsInBackground$45$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m68lambda$readControlsInBackground$45$devcolorccPanel(final float f, final int i, final int i2, final int i3) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.113
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m67lambda$readControlsInBackground$44$devcolorccPanel(f, i, i2, i3);
            }
        });
    }

    /* renamed from: lambda$readControlsInBackground$44$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m67lambda$readControlsInBackground$44$devcolorccPanel(float f, int i, int i2, int i3) {
        if (this.open) {
            this.brightness.setValue(Logic.toSlider(f, this.brightMin, this.brightMax));
            showRing(i, i2, i3, false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reblur() {
        if (this.open) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.32
                @Override // java.lang.Runnable
                public final void run() {
                    Hook.reblur();
                }
            });
            if (this.liveOn) {
                this.liveRadius = -1;
                guard(new Runnable() { // from class: dev.colorcc.Panel.33
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.this.m70lambda$reblur$47$devcolorccPanel();
                    }
                });
            }
            guard(new AnonymousClass27());
        }
    }

    /* renamed from: lambda$reblur$47$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m70lambda$reblur$47$devcolorccPanel() {
        m65lambda$onShadeExpansion$42$devcolorccPanel(this.lastExpansion);
    }

    /* renamed from: lambda$new$49$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m30lambda$new$49$devcolorccPanel() {
        this.backdropThread.execute(new Runnable() { // from class: dev.colorcc.Panel.26
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m29lambda$new$48$devcolorccPanel();
            }
        });
    }

    /* renamed from: lambda$new$48$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m29lambda$new$48$devcolorccPanel() {
        GpuBlur gpuBlur = this.gpu;
        if (gpuBlur == null) {
            return;
        }
        gpuBlur.release();
        this.gpuSoft.release();
        this.gpuSoft = null;
        this.gpu = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void captureBackdrop() {
        if (this.backdropBroken) {
            return;
        }
        if (this.keyguard.isKeyguardLocked()) {
            if (Glass.setBackdrop(null, null, 0.0f)) {
                reglaze(this);
                return;
            }
            return;
        }
        int i = 1;
        if (this.backdropBusy) {
            this.backdropAgain = true;
            return;
        }
        this.backdropBusy = true;
        this.backdropAgain = false;
        Point point = new Point();
        getDisplay().getRealSize(point);
        final int displayId = getDisplay().getDisplayId();
        boolean zLandscape = landscape();
        if (Glass.look == 1 || this.gpuWorks) {
            i = 4;
        } else if (this.blur != 0 || this.quickCapture) {
            i = 2;
        }
        final int i2 = i;
        final boolean z = this.quickCapture;
        this.quickCapture = false;
        int i3 = point.x;
        int i4 = point.y;
        final int iMax = (zLandscape ? Math.max(i3, i4) : Math.min(i3, i4)) / i2;
        int i5 = point.x;
        int i6 = point.y;
        final int iMin = (zLandscape ? Math.min(i5, i6) : Math.max(i5, i6)) / i2;
        final Context context = getContext();
        final ClassLoader classLoader = context.getClassLoader();
        final ColorFilter colorFilter = this.liveOn ? Glass.LIVE_COLOURS : Glass.SHADE_COLOURS;
        final ColorFilter colorFilter2 = this.liveOn ? Glass.LIVE_FROM_SCREEN : Glass.SHADE_FROM_SCREEN;
        this.backdropThread.execute(new Runnable() { // from class: dev.colorcc.Panel.48
            @Override // java.lang.Runnable
            public final void run() throws SecurityException, IllegalArgumentException {
                Panel.this.m7lambda$captureBackdrop$53$devcolorccPanel(z, displayId, iMax, iMin, i2, colorFilter2);
            }
        });
    }

    /*
     * [Miku-Picked UI] Rebuilt (jadx could not decompile it). The screen comes from SystemUI
     * (Hook.screenshot: the display without the shade window) instead of Samsung's
     * SemWindowManager.screenshot, and the CPU fallback is the module's plain downscale blur
     * instead of Samsung's SemGfxImageFilter, which AOSP does not have.
     */
    /* synthetic */ void m7lambda$captureBackdrop$53$devcolorccPanel(boolean z, int i, int i2, int i3, final int i4, ColorFilter colorFilter) {
        Process.setThreadPriority(z ? -4 : 0);
        long jUptimeMillis = SystemClock.uptimeMillis();
        try {
            Bitmap shot = Hook.screenshot(this, i, i2, i3);
            if (shot == null) {
                throw new IllegalStateException("no screenshot");
            }
            Bitmap sharp = null;
            Bitmap soft = null;
            long sum = 0;
            if (this.gpuWorks) {
                try {
                    GpuBlur gpuBlur = this.gpu;
                    if (gpuBlur == null || gpuBlur.width != i2 || gpuBlur.height != i3) {
                        if (gpuBlur != null) {
                            gpuBlur.release();
                            this.gpuSoft.release();
                        }
                        this.gpu = new GpuBlur(i2, i3);
                        this.gpuSoft = new GpuBlur(Math.max(1, (i2 * i4) / 12), Math.max(1, (i3 * i4) / 12));
                    }
                    sharp = this.gpu.draw(shot, dp(LIVE_BLUR[this.blur]) / (float) i4, colorFilter);
                    if (sharp != null) {
                        soft = this.gpuSoft.draw(sharp, 6.0f, null);
                    }
                    sum = jUptimeMillis;
                } catch (Throwable th) {
                    this.gpuWorks = false;
                    Hook.log("GPU blur not used, the slower one instead: " + th);
                    sharp = null;
                    soft = null;
                }
            }
            if (sharp == null || soft == null) {
                if (shot.getConfig() == Bitmap.Config.HARDWARE) {
                    shot = shot.copy(Bitmap.Config.ARGB_8888, false);
                }
                Bitmap blurred = Bitmap.createScaledBitmap(Bitmap.createScaledBitmap(shot, Math.max(1, i2 / 12), Math.max(1, i3 / 12), true), i2, i3, true);
                sharp = Bitmap.createBitmap(blurred.getWidth(), blurred.getHeight(), Bitmap.Config.ARGB_8888);
                Paint paint = new Paint();
                paint.setColorFilter(colorFilter);
                new Canvas(sharp).drawBitmap(blurred, 0.0f, 0.0f, paint);
                sum = sumOf(sharp);
                soft = Glass.soften(sharp, i4);
            }
            final Bitmap bitmap3 = soft;
            final Bitmap bitmap4 = sharp;
            final long j = sum;
            final long jUptimeMillis2 = SystemClock.uptimeMillis() - jUptimeMillis;
            this.main.post(new Runnable() {
                @Override
                public final void run() {
                    Panel.this.m5lambda$captureBackdrop$51$devcolorccPanel(bitmap4, jUptimeMillis2, j, bitmap3, i4);
                }
            });
        } catch (Throwable th5) {
            this.main.post(new Runnable() {
                @Override
                public final void run() {
                    Panel.this.m6lambda$captureBackdrop$52$devcolorccPanel();
                }
            });
            StringBuilder sb = new StringBuilder("backdrop not captured");
            sb.append(this.backdropFailures >= 9 ? ", glass rims stay unbent: " : ": ");
            sb.append(th5);
            Hook.log(sb.toString());
        }
    }

    /* renamed from: lambda$captureBackdrop$51$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m5lambda$captureBackdrop$51$devcolorccPanel(final Bitmap bitmap, final long j, final long j2, final Bitmap bitmap2, final int i) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.108
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m4lambda$captureBackdrop$50$devcolorccPanel(bitmap, j, j2, bitmap2, i);
            }
        });
    }

    /* renamed from: lambda$captureBackdrop$50$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m4lambda$captureBackdrop$50$devcolorccPanel(Bitmap bitmap, long j, long j2, Bitmap bitmap2, int i) {
        boolean z = false;
        this.backdropFailures = 0;
        backdropDone();
        if (!this.backdropLogged) {
            this.backdropLogged = true;
            StringBuilder sb = new StringBuilder("backdrop ");
            sb.append(bitmap.getWidth());
            sb.append("x");
            sb.append(bitmap.getHeight());
            sb.append(" in ");
            sb.append(j);
            sb.append(" ms, ");
            sb.append(this.gpuWorks ? "GPU" : "CPU");
            sb.append(", middle ");
            sb.append(Integer.toHexString(bitmap.copy(Bitmap.Config.ARGB_8888, false).getPixel(bitmap.getWidth() / 2, bitmap.getHeight() / 2)));
            Hook.log(sb.toString());
        }
        if (!this.open && Hook.notifGlass && this.lastExpansion > 0.0f) {
            z = true;
        }
        if (this.open || z) {
            if (j2 != this.backdropSum && (z || !this.closing)) {
                this.backdropSum = j2;
                Glass.setBackdrop(bitmap, bitmap2, 1.0f / i);
                if (stillBehind() && this.open) {
                    Hook.showShadeBlur(bitmap);
                }
                reglaze(this);
                if (Hook.notifGlass) {
                    redrawNotifications();
                }
                removeCallbacks(this.fadeBackdrop);
                postOnAnimation(this.fadeBackdrop);
            }
            if (liveBackdrop()) {
                this.main.removeCallbacks(this.refreshBackdrop);
                this.main.postDelayed(this.refreshBackdrop, (this.liveOn || stillBehind()) ? Math.max(0L, 33 - j) : 250L);
            }
        }
    }

    /* renamed from: lambda$captureBackdrop$52$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m6lambda$captureBackdrop$52$devcolorccPanel() {
        int i = this.backdropFailures + 1;
        this.backdropFailures = i;
        boolean z = i >= 10;
        this.backdropBroken = z;
        this.backdropBusy = false;
        if (z || !liveBackdrop()) {
            return;
        }
        this.main.postDelayed(this.refreshBackdrop, 500L);
    }

    private void backdropDone() {
        this.backdropBusy = false;
        if (this.backdropAgain) {
            captureBackdrop();
        }
    }

    private boolean liveBackdrop() {
        if (!this.open || this.closing) {
            return false;
        }
        if (!this.settleWorkPending || (this.gpuWorks && stillBehind())) {
            return (this.liveOn || Hook.samsungLive() || stillBehind()) && this.editor.getVisibility() != 0 && this.sheetBack == 0.0f && this.playerBack == 0.0f && this.peek.value() == 0.0f;
        }
        return false;
    }

    private boolean stillBehind() {
        return (this.liveOn || Hook.samsungLive() || this.keyguard.isKeyguardLocked()) ? false : true;
    }

    /* renamed from: lambda$new$55$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m33lambda$new$55$devcolorccPanel() {
        guard(new Runnable() { // from class: dev.colorcc.Panel.22
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m32lambda$new$54$devcolorccPanel();
            }
        });
    }

    /* renamed from: lambda$new$54$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m32lambda$new$54$devcolorccPanel() {
        if (liveBackdrop()) {
            captureBackdrop();
        }
    }

    private static long sumOf(Bitmap bitmap) {
        int[] pixels = new int[288];
        Bitmap.createScaledBitmap(bitmap, 12, 24, true).getPixels(pixels, 0, 12, 0, 0, 12, 24);
        long j = 0;
        for (int i = 0; i < 288; i++) {
            j = (j * 31) + pixels[i];
        }
        return j;
    }

    private void onSettled() {
        long jUptimeMillis = SystemClock.uptimeMillis();
        Iterator<Disc> it = this.lateIcons.iterator();
        while (it.hasNext()) {
            it.next().showLateIcon();
        }
        this.lateIcons.clear();
        logSlow("late icons", jUptimeMillis);
        long jUptimeMillis2 = SystemClock.uptimeMillis();
        if (syncTiles()) {
            setTilesListening();
        }
        logSlow("tile check", jUptimeMillis2);
        this.systemWatched = true;
        guard(new Runnable() { // from class: dev.colorcc.Panel.75
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m61lambda$onSettled$56$devcolorccPanel();
            }
        });
        guard(new Runnable() { // from class: dev.colorcc.Panel.76
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m62lambda$onSettled$57$devcolorccPanel();
            }
        });
        boolean zIsKeyguardLocked = this.keyguard.isKeyguardLocked();
        this.gear.setEnabled(!zIsKeyguardLocked);
        this.gear.icon.setAlpha(zIsKeyguardLocked ? 0.4f : 1.0f);
        guard(new Runnable() { // from class: dev.colorcc.Panel.77
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m63lambda$onSettled$58$devcolorccPanel();
            }
        });
        guard(new AnonymousClass27());
        reportMotion();
    }

    /* renamed from: lambda$onSettled$56$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m61lambda$onSettled$56$devcolorccPanel() {
        IntentFilter intentFilter = new IntentFilter("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        getContext().registerReceiver(this.ringReceiver, intentFilter, null, this.main, 2);
        refreshRing();
    }

    /* renamed from: lambda$onSettled$57$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m62lambda$onSettled$57$devcolorccPanel() {
        XposedHelpers.callMethod(this.display, "registerDisplayListener", new Object[]{this.brightnessWatch, this.main, Long.valueOf(brightnessEvents())});
    }

    /* renamed from: lambda$onSettled$58$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m63lambda$onSettled$58$devcolorccPanel() {
        watchTilt(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onClose() {
        this.open = false;
        Hook.log("close, background ".concat(this.liveOn ? "ColorCC live blur" : Hook.samsungLive() ? "Samsung live blur" : "still picture"));
        guard(new Runnable() { // from class: dev.colorcc.Panel.65
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m41lambda$onClose$59$devcolorccPanel();
            }
        });
        this.main.removeCallbacks(this.refreshBackdrop);
        this.main.removeCallbacks(this.releaseGpu);
        this.main.postDelayed(this.releaseGpu, 30000L);
        this.peek.snap(0.0f);
        guard(new Runnable() { // from class: dev.colorcc.Panel.66
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m42lambda$onClose$60$devcolorccPanel();
            }
        });
        guard(new Runnable() { // from class: dev.colorcc.Panel.67
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m43lambda$onClose$61$devcolorccPanel();
            }
        });
        guard(new Runnable() { // from class: dev.colorcc.Panel.68
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m44lambda$onClose$62$devcolorccPanel();
            }
        });
        for (final Bound bound : this.bound) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.69
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m45lambda$onClose$63$devcolorccPanel(bound);
                }
            });
        }
        if (!this.stockNeeded) {
            this.main.post(new Runnable() { // from class: dev.colorcc.Panel.70
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m47lambda$onClose$65$devcolorccPanel();
                }
            });
        }
        this.settleWorkPending = false;
        if (this.systemWatched) {
            this.systemWatched = false;
            guard(new Runnable() { // from class: dev.colorcc.Panel.71
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m48lambda$onClose$66$devcolorccPanel();
                }
            });
            guard(new Runnable() { // from class: dev.colorcc.Panel.72
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m49lambda$onClose$67$devcolorccPanel();
                }
            });
        }
    }

    /* renamed from: lambda$onClose$59$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m41lambda$onClose$59$devcolorccPanel() {
        watchTilt(false);
    }

    /* renamed from: lambda$onClose$60$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m42lambda$onClose$60$devcolorccPanel() {
        this.editor.close(false);
    }

    /* renamed from: lambda$onClose$61$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m43lambda$onClose$61$devcolorccPanel() {
        this.sheet.close();
    }

    /* renamed from: lambda$onClose$62$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m44lambda$onClose$62$devcolorccPanel() {
        this.player.close();
    }

    /* renamed from: lambda$onClose$63$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m45lambda$onClose$63$devcolorccPanel(Bound bound) {
        XposedHelpers.callMethod(bound.tile, "setListening", new Object[]{this, false});
    }

    /* renamed from: lambda$onClose$65$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m47lambda$onClose$65$devcolorccPanel() {
        guard(new Runnable() { // from class: dev.colorcc.Panel.42
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m46lambda$onClose$64$devcolorccPanel();
            }
        });
    }

    /* renamed from: lambda$onClose$64$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m46lambda$onClose$64$devcolorccPanel() {
        if (this.open || this.stockNeeded) {
            return;
        }
        parkStock(true);
    }

    /* renamed from: lambda$onClose$66$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m48lambda$onClose$66$devcolorccPanel() {
        getContext().unregisterReceiver(this.ringReceiver);
    }

    /* renamed from: lambda$onClose$67$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m49lambda$onClose$67$devcolorccPanel() {
        this.display.unregisterDisplayListener(this.brightnessWatch);
    }

    private static boolean settled(List<Body> list) {
        for (Body body : list) {
            if (body.drop.running() || Math.abs(body.drop.value() - 1.0f) > 0.001f) {
                return false;
            }
        }
        return true;
    }

    private void whenSettled() {
        if (this.settleWorkPending && !this.slide.running && settled(this.staticBodies) && settled(this.tileBodies)) {
            this.settleWorkPending = false;
            onSettled();
        }
    }

    private final class Body {
        final Motion.Value drop;
        boolean header;
        private boolean layered;
        int order;
        final SideSlide.Piece piece;
        final Motion.Value press;
        private boolean releaseAtDepth;
        int row;
        private float side;
        private float[] tuned;
        final View view;
        float[] in = Logic.Spring.of(0.24f, 0.38f);
        private float goal = -1.0f;

        Body(View view, int i) {
            SideSlide.Piece piece = new SideSlide.Piece();
            this.piece = piece;
            this.view = view;
            this.order = i;
            view.forceHasOverlappingRendering(false);
            Panel.this.bodyViews.add(view);
            this.drop = Panel.this.motion.value(this.in, new Motion.Listener() { // from class: dev.colorcc.Panel.Body.0
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Body.this.m90lambda$new$0$devcolorccPanel$Body(f, f2);
                }
            });
            Panel.this.slide.adopt(piece);
            this.press = Panel.this.motion.value(Logic.Spring.PRESS, new Motion.Listener() { // from class: dev.colorcc.Panel.Body.1
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Body.this.m91lambda$new$1$devcolorccPanel$Body(f, f2);
                }
            });
            apply();
        }

        /* renamed from: lambda$new$0$dev-colorcc-Panel$Body, reason: not valid java name */
        /* synthetic */ void m90lambda$new$0$devcolorccPanel$Body(float f, float f2) {
            apply();
        }

        /* renamed from: lambda$new$1$dev-colorcc-Panel$Body, reason: not valid java name */
        /* synthetic */ void m91lambda$new$1$devcolorccPanel$Body(float f, float f2) {
            if (this.releaseAtDepth && f >= 0.6f) {
                release();
            }
            apply();
        }

        void place(boolean z) {
            float f = z ? 1.0f : 0.0f;
            this.goal = f;
            this.drop.snap(f);
            apply();
        }

        void follow(float f) {
            float[] fArr;
            float f2 = this.goal;
            if (f == f2) {
                return;
            }
            if (f <= f2) {
                fArr = Panel.OUT;
            } else {
                fArr = this.in;
            }
            this.goal = f;
            if (fArr != this.tuned) {
                this.tuned = fArr;
                this.drop.tune(fArr);
            }
            this.drop.to(f);
        }

        void aim() {
            if (Panel.this.getWidth() == 0) {
                return;
            }
            float height = this.view.getHeight() / 2.0f;
            float width = this.view.getWidth() / 2.0f;
            View view = this.view;
            while (view != null && view != Panel.this) {
                height += view.getTop();
                width += view.getLeft();
                Object parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
            this.piece.centerY = height;
            this.piece.topY = height - (this.view.getHeight() / 2.0f);
            this.side = Math.max(-1.0f, Math.min(1.0f, (width / Math.max(1.0f, Panel.this.getWidth() / 2.0f)) - 1.0f));
            apply();
        }

        void apply() {
            float fValue = this.drop.value();
            float f = 1.0f - fValue;
            float fValue2 = ((0.16000003f * fValue) + 0.84f) * (1.0f - (this.press.value() * 0.08f));
            float fVelocity = this.press.velocity() * 0.002f;
            float f2 = -f;
            float fStretch = Logic.stretch(this.drop.velocity(), 0.012f, 0.12f) - (Math.max(0.0f, f2) * 0.6f);
            this.view.setScaleX((fVelocity + 1.0f) * fValue2 * (1.0f - (0.5f * fStretch)));
            this.view.setScaleY(fValue2 * (1.0f - fVelocity) * (fStretch + 1.0f));
            float fClamp = Logic.clamp(fValue * 3.0f);
            View view = this.view;
            view.setAlpha(fClamp * (view != Panel.this.brightnessHost ? 1.0f - Logic.clamp(Panel.this.peek.value()) : 1.0f));
            this.view.setTranslationX((-this.side) * Panel.this.dp(22.0f) * f * Math.abs(f));
            this.view.setTranslationY((f2 * Panel.this.dp(this.header ? 16.0f : (this.row * 12.0f) + 36.0f)) + (Panel.this.pull.value() * ((Logic.clamp(this.piece.centerY / Math.max(1, Panel.this.getHeight())) * 0.65f) + 0.35f)));
            boolean zRunning = this.drop.running();
            if (zRunning != this.layered) {
                this.layered = zRunning;
                (this.view == Panel.this.brightnessHost ? Panel.this.brightness : this.view == Panel.this.volumeHost ? Panel.this.volume : this.view).setLayerType(zRunning ? 2 : 0, null);
            }
        }

        void pressable() {
            this.view.setOnTouchListener(new View.OnTouchListener() { // from class: dev.colorcc.Panel.Body.2
                @Override // android.view.View.OnTouchListener
                public final boolean onTouch(View view, MotionEvent motionEvent) {
                    return Body.this.m92lambda$pressable$2$devcolorccPanel$Body(view, motionEvent);
                }
            });
        }

        /* renamed from: lambda$pressable$2$dev-colorcc-Panel$Body, reason: not valid java name */
        /* synthetic */ boolean m92lambda$pressable$2$devcolorccPanel$Body(View view, MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                this.releaseAtDepth = false;
                this.press.tune(Logic.Spring.PRESS_IN);
                this.press.to(1.0f);
            } else if (actionMasked == 3) {
                release();
            } else if (actionMasked == 1) {
                if (this.press.value() >= 0.6f) {
                    release();
                } else {
                    this.releaseAtDepth = true;
                }
            }
            return false;
        }

        private void release() {
            this.releaseAtDepth = false;
            this.press.tune(Logic.Spring.PRESS_OUT);
            this.press.to(0.0f);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Body bodyAround(View view) {
        while (view != null) {
            Iterator it = Arrays.asList(this.staticBodies, this.tileBodies).iterator();
            while (it.hasNext()) {
                for (Body body : (List<Body>) it.next()) {
                    if (body.view == view) {
                        return body;
                    }
                }
            }
            view = view.getParent() instanceof View ? (View) view.getParent() : null;
        }
        return null;
    }

    private void swell(View view, final Runnable runnable) {
        final Body bodyBodyAround = bodyAround(view);
        if (bodyBodyAround != null) {
            bodyBodyAround.press.to(-1.1f);
        }
        this.main.postDelayed(new Runnable() { // from class: dev.colorcc.Panel.115
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m78lambda$swell$68$devcolorccPanel(bodyBodyAround, runnable);
            }
        }, 90L);
    }

    /* renamed from: lambda$swell$68$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m78lambda$swell$68$devcolorccPanel(Body body, Runnable runnable) {
        if (body != null) {
            body.press.to(0.0f);
        }
        guard(runnable);
    }

    private void addStaticBody(View view, int i, boolean z) {
        Body body = new Body(view, i);
        if (z) {
            body.pressable();
        }
        this.staticBodies.add(body);
    }

    private final class Sheet extends FrameLayout {
        private Disc adaptive;
        private boolean adaptiveOn;
        private final int[] at;
        private final Rewind back;
        private final TorchBeam beam;
        private boolean brightnessOpen;
        private final LinearLayout card;
        private Glass cardGlass;
        private Disc dark;
        private boolean darkOn;
        private int dndHours;
        private final LinearLayout extras;
        private final Drawable headphones;
        private View held;
        private final float[] heldSize;
        private boolean leaving;
        private final Slider level;
        private final LinearLayout list;
        private final BroadcastReceiver listReceiver;
        private final ScrollView listScroll;
        private boolean listWatched;
        private final Slider loud;
        private final TextView loudHeading;
        private final ImageView loudIcon;
        private int menu;
        private final LinearLayout note;
        private final int[] origin;
        boolean outputOpen;
        private String outputPackage;
        private Object outputRouter;
        private final List<Row> rows;
        private final TextView settings;
        private final Motion.Value show;
        private String shownRows;
        private Disc silent;
        private Boolean silentShown;
        private final Drawable speaker;
        private final TextView sub;
        private final ImageView sun;
        private Object tile;
        private final TextView title;
        private final Toggle toggle;
        private String torchCamera;
        private final TextView torchLabel;
        private int torchLevel;
        private int torchMax;
        private final LinearLayout torchMenu;
        private float torchStrength;
        private String usage;
        private final LinearLayout volumes;
        boolean volumesOpen;
        private Bound watch;
        private float windowH;
        private float windowRound;
        private float windowW;

        /* compiled from: D8$$SyntheticClass */
        /* renamed from: dev.colorcc.Panel$Sheet$0, reason: invalid class name */
        public final /* synthetic */ class AnonymousClass0 implements Runnable {
            public /* synthetic */ AnonymousClass0() {
            }

            @Override // java.lang.Runnable
            public final void run() {
                Sheet.this.refreshList();
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static /* synthetic */ void access$5400(Sheet sheet) {
            sheet.refreshList();
        }

        Sheet(Context context) {
            super(context);
            this.listReceiver = new BroadcastReceiver() { // from class: dev.colorcc.Panel.Sheet.1
                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context2, Intent intent) {
                    Panel panel = Panel.this;
                    final Sheet sheet = Sheet.this;
                    panel.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.1.0
                        @Override // java.lang.Runnable
                        public final void run() {
                            Sheet.access$5400(sheet);
                        }
                    });
                }
            };
            this.rows = new ArrayList();
            this.shownRows = "";
            this.torchLevel = -1;
            this.torchStrength = 1.0f;
            Drawable drawableSpeaker = Icons.speaker();
            this.speaker = drawableSpeaker;
            this.headphones = Icons.of("M12,3c-4.97,0 -9,4.03 -9,9v7c0,1.1 0.9,2 2,2h4v-8H5v-1c0,-3.87 3.13,-7 7,-7s7,3.13 7,7v1h-4v8h4c1.1,0 2,-0.9 2,-2v-7c0,-4.97 -4.03,-9 -9,-9z");
            this.dndHours = -1;
            this.origin = new int[2];
            this.heldSize = new float[2];
            this.at = new int[2];
            setVisibility(8);
            setBackground(Panel.scrim());
            forceHasOverlappingRendering(false);
            setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Sheet.26
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Sheet.this.m195lambda$new$0$devcolorccPanel$Sheet(view);
                }
            });
            LinearLayout linearLayout = new LinearLayout(context);
            this.card = linearLayout;
            linearLayout.setOrientation(1);
            linearLayout.setClickable(true);
            linearLayout.setPadding(Panel.this.dp(20.0f), Panel.this.dp(20.0f), Panel.this.dp(20.0f), Panel.this.dp(10.0f));
            linearLayout.setClipChildren(false);
            Glass glass = new Glass(Panel.this.dp(34.0f), 536870911, 536870911, 0.5f, 0.5f);
            this.cardGlass = glass;
            linearLayout.setBackground(glass);
            Panel.this.bodyViews.add(linearLayout);
            linearLayout.setOutlineProvider(new ViewOutlineProvider() { // from class: dev.colorcc.Panel.Sheet.2
                @Override // android.view.ViewOutlineProvider
                public void getOutline(View view, Outline outline) {
                    float width = (view.getWidth() - Sheet.this.windowW) / 2.0f;
                    float height = (view.getHeight() - Sheet.this.windowH) / 2.0f;
                    outline.setRoundRect(Math.round(width), Math.round(height), Math.round(width + Sheet.this.windowW), Math.round(height + Sheet.this.windowH), Math.min(Sheet.this.windowRound, Math.min(Sheet.this.windowW, Sheet.this.windowH) / 2.0f));
                }
            });
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            linearLayoutHorizontal.setGravity(16);
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(1);
            TextView textViewLabel = Panel.this.label(context, 20.0f, -1, 1);
            this.title = textViewLabel;
            textViewLabel.setTypeface(Typeface.create("sans-serif-medium", 0));
            TextView textViewLabel2 = Panel.this.label(context, 13.0f, -1711276033, 1);
            this.sub = textViewLabel2;
            linearLayout2.addView(textViewLabel);
            linearLayout2.addView(textViewLabel2);
            linearLayoutHorizontal.addView(linearLayout2, new LinearLayout.LayoutParams(0, -2, 1.0f));
            Toggle toggle = Panel.this.new Toggle(context);
            this.toggle = toggle;
            toggle.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Sheet.28
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Sheet.this.m200lambda$new$2$devcolorccPanel$Sheet(view);
                }
            });
            linearLayoutHorizontal.addView(toggle, new LinearLayout.LayoutParams(Panel.this.dp(52.0f), Panel.this.dp(30.0f)));
            linearLayout.addView(linearLayoutHorizontal);
            ImageView imageView = new ImageView(context);
            this.loudIcon = imageView;
            imageView.setImageDrawable(drawableSpeaker);
            TextView textViewLabel3 = Panel.this.label(context, 17.0f, -1, 1);
            this.loudHeading = textViewLabel3;
            Slider slider = new Slider(context, Panel.this.motion, Panel.this.dp(26.0f), Panel.this.dp(26.0f));
            this.loud = slider;
            slider.setContentDescription(Panel.this.word("volume", new Object[0]));
            slider.setListener(new Slider.Listener() { // from class: dev.colorcc.Panel.Sheet.29
                @Override // dev.colorcc.Slider.Listener
                public final void onChange(float f, boolean z) {
                    Sheet.this.m201lambda$new$3$devcolorccPanel$Sheet(f, z);
                }
            });
            slider.setBackground(new Glass(-2.0f, 452984831, 452984831, 0.5f, 0.5f).recessed());
            LinearLayout linearLayoutHorizontal2 = Panel.horizontal(context);
            this.silent = roundSwitch(context, linearLayoutHorizontal2, "M16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v2.21l2.45,2.45c0.03,-0.2 0.05,-0.41 0.05,-0.63zM19,12c0,0.94 -0.2,1.82 -0.54,2.64l1.51,1.51C20.63,14.91 21,13.5 21,12c0,-4.28 -2.99,-7.86 -7,-8.77v2.06c2.89,0.86 5,3.54 5,6.71zM4.27,3L3,4.27 7.73,9H3v6h4l5,5v-6.73l4.25,4.25c-0.67,0.52 -1.42,0.93 -2.25,1.18v2.06c1.38,-0.31 2.63,-0.95 3.69,-1.81L19.73,21 21,19.73l-9,-9L4.27,3zM12,4L9.91,6.09 12,8.18V4z", Panel.this.word("silent_mode", new Object[0]), new Runnable() { // from class: dev.colorcc.Panel.Sheet.30
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m202lambda$new$4$devcolorccPanel$Sheet();
                }
            });
            this.volumes = column(context, imageView, textViewLabel3, slider, linearLayoutHorizontal2);
            ImageView imageView2 = new ImageView(context);
            this.sun = imageView2;
            imageView2.setImageDrawable(Icons.of("M12,7c-2.76,0 -5,2.24 -5,5s2.24,5 5,5 5,-2.24 5,-5 -2.24,-5 -5,-5zM2,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1H2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM20,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1h-2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM11,2v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1V2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM11,20v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1v-2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM5.99,4.58c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0s0.39,-1.03 0,-1.41L5.99,4.58zM18.36,16.95c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0 0.39,-0.39 0.39,-1.03 0,-1.41l-1.06,-1.06zM19.42,5.99c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06zM7.05,18.36c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06z"));
            TextView textViewLabel4 = Panel.this.label(context, 17.0f, -1, 1);
            textViewLabel4.setText(Panel.this.word("brightness", new Object[0]));
            Slider slider2 = new Slider(context, Panel.this.motion, Panel.this.dp(26.0f), Panel.this.dp(26.0f));
            this.level = slider2;
            slider2.setContentDescription(Panel.this.word("brightness", new Object[0]));
            slider2.setListener(new Slider.Listener() { // from class: dev.colorcc.Panel.Sheet.31
                @Override // dev.colorcc.Slider.Listener
                public final void onChange(float f, boolean z) {
                    Sheet.this.m203lambda$new$5$devcolorccPanel$Sheet(f, z);
                }
            });
            slider2.setBackground(new Glass(-2.0f, 452984831, 452984831, 0.5f, 0.5f).recessed());
            LinearLayout linearLayoutHorizontal3 = Panel.horizontal(context);
            this.adaptive = roundSwitch(context, linearLayoutHorizontal3, "M12,7c-2.76,0 -5,2.24 -5,5s2.24,5 5,5 5,-2.24 5,-5 -2.24,-5 -5,-5zM2,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1H2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM20,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1h-2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM11,2v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1V2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM11,20v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1v-2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM5.99,4.58c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0s0.39,-1.03 0,-1.41L5.99,4.58zM18.36,16.95c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0 0.39,-0.39 0.39,-1.03 0,-1.41l-1.06,-1.06zM19.42,5.99c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06zM7.05,18.36c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06z", Panel.this.word("auto_brightness", new Object[0]), new Runnable() { // from class: dev.colorcc.Panel.Sheet.32
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m205lambda$new$7$devcolorccPanel$Sheet();
                }
            });
            this.dark = roundSwitch(context, linearLayoutHorizontal3, "M12,22c5.52,0 10,-4.48 10,-10S17.52,2 12,2 2,6.48 2,12s4.48,10 10,10zM13,4.07c3.94,0.49 7,3.85 7,7.93s-3.05,7.44 -7,7.93V4.07z", Panel.this.word("dark_mode", new Object[0]), new Runnable() { // from class: dev.colorcc.Panel.Sheet.33
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m207lambda$new$9$devcolorccPanel$Sheet();
                }
            });
            this.extras = column(context, imageView2, textViewLabel4, slider2, linearLayoutHorizontal3);
            LinearLayout linearLayout3 = new LinearLayout(context);
            this.list = linearLayout3;
            linearLayout3.setOrientation(1);
            ScrollView scrollView = new ScrollView(context);
            this.listScroll = scrollView;
            scrollView.setVerticalScrollBarEnabled(false);
            scrollView.setOutlineProvider(ViewOutlineProvider.BOUNDS);
            scrollView.setClipToOutline(true);
            scrollView.addView(linearLayout3, new FrameLayout.LayoutParams(-1, -2));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, 0, 1.0f);
            layoutParams.topMargin = Panel.this.dp(14.0f);
            linearLayout.addView(scrollView, layoutParams);
            LinearLayout linearLayout4 = new LinearLayout(context);
            this.note = linearLayout4;
            linearLayout4.setOrientation(1);
            linearLayout4.setGravity(17);
            linearLayout4.setVisibility(8);
            ImageView imageView3 = new ImageView(context);
            imageView3.setImageDrawable(Icons.of("M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9 9,-4.03 9,-9c0,-0.46 -0.04,-0.92 -0.1,-1.36 -0.98,1.37 -2.58,2.26 -4.4,2.26 -2.98,0 -5.4,-2.42 -5.4,-5.4 0,-1.81 0.89,-3.42 2.26,-4.4C12.92,3.04 12.46,3 12,3z"));
            imageView3.setImageTintList(ColorStateList.valueOf(-2130706433));
            linearLayout4.addView(imageView3, new LinearLayout.LayoutParams(Panel.this.dp(72.0f), Panel.this.dp(72.0f)));
            TextView textViewLabel5 = Panel.this.label(context, 15.0f, -1711276033, 1);
            textViewLabel5.setText(Panel.this.word("dnd_off", new Object[0]));
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams2.topMargin = Panel.this.dp(16.0f);
            linearLayout4.addView(textViewLabel5, layoutParams2);
            linearLayout.addView(linearLayout4, new LinearLayout.LayoutParams(-1, 0, 1.0f));
            LinearLayout linearLayout5 = new LinearLayout(context);
            this.torchMenu = linearLayout5;
            linearLayout5.setOrientation(1);
            linearLayout5.setGravity(1);
            linearLayout5.setVisibility(8);
            TextView textViewLabel6 = Panel.this.label(context, 16.0f, -1, 1);
            this.torchLabel = textViewLabel6;
            linearLayout5.addView(textViewLabel6, new LinearLayout.LayoutParams(-2, -2));
            TorchBeam torchBeam = Panel.this.new TorchBeam(context, new Consumer() { // from class: dev.colorcc.Panel.Sheet.34
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    Sheet.this.m198lambda$new$11$devcolorccPanel$Sheet((Float) obj);
                }
            });
            this.beam = torchBeam;
            torchBeam.setContentDescription(Panel.this.word("torch_brightness", new Object[0]));
            torchBeam.setBackground(new Glass(Panel.this.dp(34.0f), 536870911, 536870911, 0.5f, 0.5f));
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, Panel.this.dp(400.0f));
            layoutParams3.topMargin = Panel.this.dp(40.0f);
            linearLayout5.addView(torchBeam, layoutParams3);
            linearLayout.addView(linearLayout5, new LinearLayout.LayoutParams(-1, -2));
            LinearLayout linearLayout6 = new LinearLayout(context);
            linearLayout6.setOrientation(1);
            View view = new View(context);
            view.setBackgroundColor(536870911);
            linearLayout6.addView(view, new LinearLayout.LayoutParams(-1, 1));
            TextView textViewLabel7 = Panel.this.label(context, 16.0f, -1, 1);
            this.settings = textViewLabel7;
            textViewLabel7.setText(Panel.this.word("more_settings", new Object[0]));
            textViewLabel7.setGravity(17);
            textViewLabel7.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Sheet.35
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    Sheet.this.m199lambda$new$12$devcolorccPanel$Sheet(view2);
                }
            });
            linearLayout6.addView(textViewLabel7, new LinearLayout.LayoutParams(-1, Panel.this.dp(52.0f)));
            LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams4.topMargin = Panel.this.dp(8.0f);
            linearLayout.addView(linearLayout6, layoutParams4);
            addView(linearLayout, new FrameLayout.LayoutParams(Panel.this.dp(292.0f), Panel.this.dp(380.0f), 17));
            Motion.Value value = Panel.this.motion.value(Logic.Spring.MENU, new Motion.Listener() { // from class: dev.colorcc.Panel.Sheet.27
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Sheet.this.applyShow(f, f2);
                }
            });
            this.show = value;
            this.back = Panel.this.new Rewind(value, new Motion.Listener() { // from class: dev.colorcc.Panel.Sheet.27
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Sheet.this.applyShow(f, f2);
                }
            });
        }

        /* renamed from: lambda$new$0$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m195lambda$new$0$devcolorccPanel$Sheet(View view) {
            close();
        }

        /* renamed from: lambda$new$1$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m196lambda$new$1$devcolorccPanel$Sheet() {
            XposedHelpers.callMethod(this.tile, "click", new Object[]{null});
        }

        /* renamed from: lambda$new$2$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m200lambda$new$2$devcolorccPanel$Sheet(View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.19
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m196lambda$new$1$devcolorccPanel$Sheet();
                }
            });
        }

        /* renamed from: lambda$new$3$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m201lambda$new$3$devcolorccPanel$Sheet(float f, boolean z) {
            Panel panel = Panel.this;
            panel.setVolume(panel.volumeStream, f);
            Panel.this.volume.setValue(f);
            showLoudness(f);
            boolean z2 = f <= 0.0f;
            if (z2 != (Panel.this.shownRingerSlot == 2)) {
                Panel.this.onRingerTap(z2 ? 2 : 0);
            }
        }

        /* renamed from: lambda$new$4$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m202lambda$new$4$devcolorccPanel$Sheet() {
            Panel panel = Panel.this;
            panel.onRingerTap(panel.shownRingerSlot == 2 ? 0 : 2);
        }

        /* renamed from: lambda$new$5$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m203lambda$new$5$devcolorccPanel$Sheet(float f, boolean z) {
            Panel.this.applyBrightness(f, z);
            Panel.this.brightness.setValue(f);
            showSun(f);
        }

        /* renamed from: lambda$new$7$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m205lambda$new$7$devcolorccPanel$Sheet() {
            boolean z = !this.adaptiveOn;
            this.adaptiveOn = z;
            showSwitch(this.adaptive, z, true);
            final boolean z2 = this.adaptiveOn;
            Panel.this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.Sheet.3
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m204lambda$new$6$devcolorccPanel$Sheet(z2);
                }
            });
        }

        /* renamed from: lambda$new$6$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m204lambda$new$6$devcolorccPanel$Sheet(boolean z) {
            Panel.this.writeAdaptiveBrightness(z);
        }

        /* renamed from: lambda$new$9$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m207lambda$new$9$devcolorccPanel$Sheet() {
            boolean z = !this.darkOn;
            this.darkOn = z;
            showSwitch(this.dark, z, true);
            final boolean z2 = this.darkOn;
            Panel.this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.Sheet.14
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m206lambda$new$8$devcolorccPanel$Sheet(z2);
                }
            });
        }

        /* renamed from: lambda$new$8$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m206lambda$new$8$devcolorccPanel$Sheet(boolean z) {
            Panel.this.writeDarkMode(z);
        }

        /* renamed from: lambda$new$10$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m197lambda$new$10$devcolorccPanel$Sheet(Float f) throws CameraAccessException {
            onTorch(f.floatValue());
        }

        /* renamed from: lambda$new$11$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m198lambda$new$11$devcolorccPanel$Sheet(final Float f) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.36
                @Override // java.lang.Runnable
                public final void run() {
                    try {
                        Sheet.this.m197lambda$new$10$devcolorccPanel$Sheet(f);
                    } catch (CameraAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }
            });
        }

        /* renamed from: lambda$new$12$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m199lambda$new$12$devcolorccPanel$Sheet(View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.13
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.openTileSettings();
                }
            });
        }

        private LinearLayout column(Context context, ImageView imageView, TextView textView, Slider slider, LinearLayout linearLayout) {
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(1);
            linearLayout2.setGravity(1);
            linearLayout2.setVisibility(8);
            linearLayout2.setClipChildren(false);
            imageView.setImageTintList(ColorStateList.valueOf(-1));
            linearLayout2.addView(imageView, new LinearLayout.LayoutParams(Panel.this.dp(44.0f), Panel.this.dp(44.0f)));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.topMargin = Panel.this.dp(8.0f);
            linearLayout2.addView(textView, layoutParams);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(Panel.this.dp(104.0f), Panel.this.dp(250.0f));
            layoutParams2.topMargin = Panel.this.dp(24.0f);
            linearLayout2.addView(Panel.host(context, slider), layoutParams2);
            linearLayout.setClipChildren(false);
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams3.topMargin = Panel.this.dp(36.0f);
            linearLayout2.addView(linearLayout, layoutParams3);
            LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams4.topMargin = Panel.this.dp(10.0f);
            this.card.addView(linearLayout2, layoutParams4);
            return linearLayout2;
        }

        private void only(View view) {
            View[] viewArr = {this.volumes, this.extras, this.listScroll, this.note, this.torchMenu};
            for (int i = 0; i < 5; i++) {
                View view2 = viewArr[i];
                view2.setVisibility(view2 == view ? 0 : 8);
            }
        }

        private void arrive() {
            this.leaving = false;
            this.back.stop();
        }

        private void leave() {
            this.leaving = true;
            this.back.start();
        }

        private void land() {
            Body bodyBodyAround = Panel.this.bodyAround(this.held);
            if (bodyBodyAround != null) {
                bodyBodyAround.press.kick(7.0f);
            }
        }

        @Override // android.widget.FrameLayout, android.view.View
        protected void onMeasure(int i, int i2) {
            super.onMeasure(i, i2);
            if (this.card.getLayoutParams().height == -2) {
                LinearLayout linearLayout = this.card;
                linearLayout.measure(View.MeasureSpec.makeMeasureSpec(linearLayout.getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(0, 0));
            }
        }

        @Override // android.view.ViewGroup, android.view.View
        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            if (motionEvent.getActionMasked() == 0 && getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return super.dispatchTouchEvent(motionEvent);
        }

        private void from(View view) {
            this.held = view;
            view.getLocationOnScreen(this.origin);
            float scaleX = 1.0f;
            View view2 = view;
            while (view2 != null) {
                scaleX *= view2.getScaleX();
                view2 = view2.getParent() instanceof View ? (View) view2.getParent() : null;
            }
            this.heldSize[0] = view.getWidth() * scaleX;
            this.heldSize[1] = view.getHeight() * scaleX;
            int[] iArr = this.origin;
            iArr[0] = iArr[0] + Math.round(this.heldSize[0] / 2.0f);
            int[] iArr2 = this.origin;
            iArr2[1] = iArr2[1] + Math.round(this.heldSize[1] / 2.0f);
        }

        void openOutput(View view, String str) {
            if (this.tile != null || this.volumesOpen || this.brightnessOpen || this.outputOpen) {
                close();
            }
            from(view);
            Panel.this.player.close();
            this.outputOpen = true;
            this.outputPackage = str;
            this.menu = 7;
            this.title.setText(Panel.this.word("media_output", new Object[0]));
            this.toggle.setVisibility(8);
            this.sub.setVisibility(8);
            this.list.removeAllViews();
            this.shownRows = "";
            bare(false);
            only(this.listScroll);
            startOutputScan();
            refreshList();
            long[] jArr = {700, 1800, 3500};
            for (int i = 0; i < 3; i++) {
                postDelayed(new Runnable() { // from class: dev.colorcc.Panel.Sheet.40
                    @Override // java.lang.Runnable
                    public final void run() {
                        Sheet.this.m210lambda$openOutput$13$devcolorccPanel$Sheet();
                    }
                }, jArr[i]);
            }
            this.settings.setVisibility(0);
            arrive();
            setVisibility(0);
            this.show.to(1.0f);
        }

        /* renamed from: lambda$openOutput$13$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m210lambda$openOutput$13$devcolorccPanel$Sheet() {
            Panel.this.guard(new AnonymousClass0());
        }

        private void startOutputScan() {
            this.outputRouter = null;
            if (this.outputPackage == null) {
                return;
            }
            try {
                Object objCallStaticMethod = XposedHelpers.callStaticMethod(MediaRouter2.class, "getInstance", new Object[]{getContext(), this.outputPackage});
                this.outputRouter = objCallStaticMethod;
                try {
                    XposedHelpers.callMethod(objCallStaticMethod, "startScan", new Object[0]);
                } catch (Throwable th) {
                    Hook.log("outputs not scanned, the known ones only: " + th);
                }
            } catch (Throwable th2) {
                Hook.log("outputs not read: " + th2);
            }
        }

        private void stopOutputScan() {
            Object obj = this.outputRouter;
            this.outputRouter = null;
            if (obj == null) {
                return;
            }
            try {
                XposedHelpers.callMethod(obj, "stopScan", new Object[0]);
            } catch (Throwable unused) {
            }
        }

        private void listOutputs() {
            final Object obj = this.outputRouter;
            if (obj == null) {
                item(null, Panel.this.word("no_outputs", new Object[0]), null, false, null);
                return;
            }
            List list = (List) XposedHelpers.callMethod(obj, "getControllers", new Object[0]);
            final HashSet hashSet = new HashSet();
            if (!list.isEmpty()) {
                Iterator it = ((List) XposedHelpers.callMethod(list.get(list.size() - 1), "getSelectedRoutes", new Object[0])).iterator();
                while (it.hasNext()) {
                    hashSet.add(((MediaRoute2Info) it.next()).getId());
                }
            }
            ArrayList<MediaRoute2Info> arrayList = new ArrayList();
            HashSet hashSet2 = new HashSet();
            for (MediaRoute2Info mediaRoute2Info : (List<MediaRoute2Info>) XposedHelpers.callMethod(obj, "getAllRoutes", new Object[0])) {
                if (!TextUtils.isEmpty(mediaRoute2Info.getName()) && hashSet2.add(mediaRoute2Info.getId())) {
                    arrayList.add(mediaRoute2Info);
                }
            }
            arrayList.sort(new Comparator() { // from class: dev.colorcc.Panel.Sheet.17
                @Override // java.util.Comparator
                public final int compare(Object obj2, Object obj3) {
                    return Sheet.this.m189lambda$listOutputs$14$devcolorccPanel$Sheet(hashSet, (MediaRoute2Info) obj2, (MediaRoute2Info) obj3);
                }
            });
            for (final MediaRoute2Info mediaRoute2Info2 : arrayList) {
                boolean zContains = hashSet.contains(mediaRoute2Info2.getId());
                String strOutputIcon = outputIcon(mediaRoute2Info2.getType());
                String string = mediaRoute2Info2.getName().toString();
                Runnable runnable = null;
                String strWord = zContains ? Panel.this.word("connected", new Object[0]) : null;
                if (!zContains) {
                    runnable = new Runnable() { // from class: dev.colorcc.Panel.Sheet.18
                        @Override // java.lang.Runnable
                        public final void run() {
                            Sheet.this.m191lambda$listOutputs$16$devcolorccPanel$Sheet(obj, mediaRoute2Info2);
                        }
                    };
                }
                item(strOutputIcon, string, strWord, zContains, runnable).radio = Boolean.valueOf(zContains);
            }
            if (arrayList.isEmpty()) {
                item(null, Panel.this.word("no_outputs", new Object[0]), null, false, null);
            }
        }

        /* renamed from: lambda$listOutputs$14$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ int m189lambda$listOutputs$14$devcolorccPanel$Sheet(Set set, MediaRoute2Info mediaRoute2Info, MediaRoute2Info mediaRoute2Info2) {
            return Integer.compare(outputRank(mediaRoute2Info, set), outputRank(mediaRoute2Info2, set));
        }

        /* renamed from: lambda$listOutputs$16$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m191lambda$listOutputs$16$devcolorccPanel$Sheet(Object obj, MediaRoute2Info mediaRoute2Info) {
            XposedHelpers.callMethod(obj, "transferTo", new Object[]{mediaRoute2Info});
            postDelayed(new Runnable() { // from class: dev.colorcc.Panel.Sheet.6
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m190lambda$listOutputs$15$devcolorccPanel$Sheet();
                }
            }, 600L);
        }

        /* renamed from: lambda$listOutputs$15$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m190lambda$listOutputs$15$devcolorccPanel$Sheet() {
            Panel.this.guard(new AnonymousClass0());
        }

        private int outputRank(MediaRoute2Info mediaRoute2Info, Set<String> set) {
            if (mediaRoute2Info.getType() == 2) {
                return 0;
            }
            return set.contains(mediaRoute2Info.getId()) ? 1 : 2;
        }

        private String outputIcon(int i) {
            if (i == 2) {
                return "M16.5,3h-9C6.67,3 6,3.67 6,4.5v15c0,0.83 0.67,1.5 1.5,1.5h9c0.83,0 1.5,-0.67 1.5,-1.5v-15c0,-0.83 -0.67,-1.5 -1.5,-1.5zM16,19H8V5h8v14z";
            }
            if (i == 3 || i == 4 || i == 8 || i == 26 || i == 22 || i == 23) {
                return "M12,3c-4.97,0 -9,4.03 -9,9v7c0,1.1 0.9,2 2,2h4v-8H5v-1c0,-3.87 3.13,-7 7,-7s7,3.13 7,7v1h-4v8h4c1.1,0 2,-0.9 2,-2v-7c0,-4.97 -4.03,-9 -9,-9z";
            }
            return "M21,3H3C1.9,3 1,3.9 1,5v3h2V5h18v14h-7v2h7c1.1,0 2,-0.9 2,-2V5C23,3.9 22.1,3 21,3zM1,18v3h3C4,19.34 2.66,18 1,18zM1,14v2c2.76,0 5,2.24 5,5h2C8,17.13 4.87,14 1,14zM1,10v2c4.97,0 9,4.03 9,9h2C12,14.92 7.07,10 1,10z";
        }

        void openBrightness() {
            if (this.tile != null || this.volumesOpen) {
                close();
            }
            Panel.this.player.close();
            from(Panel.this.brightness);
            this.brightnessOpen = true;
            bare(true);
            only(this.extras);
            this.level.setValue(Panel.this.brightness.value());
            showSun(Panel.this.brightness.value());
            boolean z = (getResources().getConfiguration().uiMode & 48) == 32;
            this.darkOn = z;
            showSwitch(this.dark, z, false);
            Panel.this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.Sheet.15
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m209lambda$openBrightness$18$devcolorccPanel$Sheet();
                }
            });
            arrive();
            setVisibility(0);
            this.show.to(1.0f);
        }

        /* renamed from: lambda$openBrightness$18$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m209lambda$openBrightness$18$devcolorccPanel$Sheet() {
            final boolean zAdaptiveBrightness = Panel.this.adaptiveBrightness();
            Panel.this.main.post(new Runnable() { // from class: dev.colorcc.Panel.Sheet.25
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m208lambda$openBrightness$17$devcolorccPanel$Sheet(zAdaptiveBrightness);
                }
            });
        }

        /* renamed from: lambda$openBrightness$17$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m208lambda$openBrightness$17$devcolorccPanel$Sheet(boolean z) {
            if (this.brightnessOpen) {
                this.adaptiveOn = z;
                showSwitch(this.adaptive, z, false);
            }
        }

        private void bare(boolean z) {
            this.card.setBackground(z ? null : this.cardGlass);
            this.card.getChildAt(0).setVisibility(z ? 8 : 0);
            this.card.getChildAt(this.card.getChildCount() - 1).setVisibility(z ? 8 : 0);
            this.card.getLayoutParams().height = z ? -2 : Panel.this.dp(380.0f);
            this.card.requestLayout();
        }

        private Disc roundSwitch(Context context, LinearLayout linearLayout, String str, String str2, final Runnable runnable) {
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(1);
            linearLayout2.setGravity(1);
            linearLayout2.setClipChildren(false);
            Panel panel = Panel.this;
            Disc disc = panel.new Disc(context, panel.dp(24.0f));
            disc.icon.setImageDrawable(Icons.of(str));
            disc.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Sheet.39
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Sheet.this.m211lambda$roundSwitch$19$devcolorccPanel$Sheet(runnable, view);
                }
            });
            linearLayout2.addView(disc, new LinearLayout.LayoutParams(Panel.this.dp(58.0f), Panel.this.dp(58.0f)));
            TextView textViewLabel = Panel.this.label(context, 12.0f, -1, 2);
            textViewLabel.setText(str2);
            textViewLabel.setGravity(1);
            textViewLabel.setIncludeFontPadding(false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.topMargin = Panel.this.dp(8.0f);
            linearLayout2.addView(textViewLabel, layoutParams);
            TextView textViewLabel2 = Panel.this.label(context, 11.0f, -1275068417, 1);
            textViewLabel2.setGravity(1);
            disc.setTag(textViewLabel2);
            linearLayout2.addView(textViewLabel2, new LinearLayout.LayoutParams(-2, -2));
            linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(0, -2, 1.0f));
            disc.setContentDescription(str2.replace('\n', ' '));
            return disc;
        }

        /* renamed from: lambda$roundSwitch$19$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m211lambda$roundSwitch$19$devcolorccPanel$Sheet(Runnable runnable, View view) {
            Panel.this.guard(runnable);
        }

        private void showSwitch(Disc disc, boolean z, boolean z2) {
            if (z2) {
                disc.level.to(z ? 1.0f : 0.0f);
            } else {
                disc.level.snap(z ? 1.0f : 0.0f);
            }
            ((TextView) disc.getTag()).setText(Panel.this.word(z ? "on" : "off", new Object[0]));
            disc.setStateDescription(Panel.this.word(z ? "on" : "off", new Object[0]));
        }

        void openVolumes() {
            if (this.tile != null || this.brightnessOpen) {
                close();
            }
            Panel.this.player.close();
            from(Panel.this.volume);
            this.volumesOpen = true;
            bare(true);
            only(this.volumes);
            this.loudIcon.setImageDrawable(headphonesOn() ? this.headphones : this.speaker);
            this.loud.setValue(Panel.this.volume.value());
            showLoudness(Panel.this.volume.value());
            showSwitch(this.silent, Panel.this.shownRingerSlot == 2, false);
            showSilentIcon(Panel.this.shownRingerSlot == 2);
            Panel.this.refreshRing();
            arrive();
            setVisibility(0);
            this.show.to(1.0f);
        }

        private boolean headphonesOn() {
            for (AudioDeviceInfo audioDeviceInfo : Panel.this.audio.getDevices(2)) {
                int type = audioDeviceInfo.getType();
                if (type == 3 || type == 4 || type == 8 || type == 22 || type == 26) {
                    return true;
                }
            }
            return false;
        }

        private void showLoudness(float f) {
            String strWord;
            int iRound = Math.round(Logic.clamp(f) * 100.0f);
            TextView textView = this.loudHeading;
            if (iRound == 0) {
                strWord = Panel.this.word("volume", new Object[0]);
            } else {
                strWord = iRound + "%";
            }
            textView.setText(strWord);
            this.loudIcon.getDrawable().setLevel(Math.round(Logic.clamp(f) * 10000.0f));
        }

        private void showSun(float f) {
            float fClamp = Logic.clamp(f);
            this.sun.setRotation(120.0f * fClamp);
            float f2 = (fClamp * 0.35f) + 0.8f;
            this.sun.setScaleX(f2);
            this.sun.setScaleY(f2);
        }

        void showBrightness(float f) {
            if (this.brightnessOpen) {
                this.level.setValue(f);
                showSun(this.level.value());
            }
        }

        void showRing(float f, boolean z, boolean z2) {
            if (this.volumesOpen) {
                this.loud.setValue(f);
                showLoudness(this.loud.value());
                showSwitch(this.silent, z, z2);
                showSilentIcon(z);
            }
        }

        private void showSilentIcon(boolean z) {
            Boolean bool = this.silentShown;
            if (bool == null || bool.booleanValue() != z) {
                this.silentShown = Boolean.valueOf(z);
                this.silent.icon.setImageDrawable(Icons.of(z ? "M16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v2.21l2.45,2.45c0.03,-0.2 0.05,-0.41 0.05,-0.63zM19,12c0,0.94 -0.2,1.82 -0.54,2.64l1.51,1.51C20.63,14.91 21,13.5 21,12c0,-4.28 -2.99,-7.86 -7,-8.77v2.06c2.89,0.86 5,3.54 5,6.71zM4.27,3L3,4.27 7.73,9H3v6h4l5,5v-6.73l4.25,4.25c-0.67,0.52 -1.42,0.93 -2.25,1.18v2.06c1.38,-0.31 2.63,-0.95 3.69,-1.81L19.73,21 21,19.73l-9,-9L4.27,3zM12,4L9.91,6.09 12,8.18V4z" : "M18,16v-5c0,-3.07 -1.63,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.64,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2zM12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2z"));
            }
        }

        void open(Object obj, View view) {
            if (this.tile != null || this.volumesOpen || this.brightnessOpen) {
                close();
            }
            Panel.this.player.close();
            from(view);
            try {
                this.watch = Panel.this.new Bound(obj, new Consumer() { // from class: dev.colorcc.Panel.Sheet.16
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj2) {
                        Sheet.this.render(obj2);
                    }
                });
                this.tile = obj;
                this.toggle.setVisibility(0);
                this.menu = Logic.menu(String.valueOf(XposedHelpers.callMethod(obj, "getTileSpec", new Object[0])));
                this.title.setText(Panel.this.word(Logic.MENU_WORDS[this.menu], new Object[0]));
                this.sub.setVisibility(8);
                this.list.removeAllViews();
                this.shownRows = "";
                boolean z = this.menu == 6;
                bare(z);
                only(z ? this.torchMenu : this.listScroll);
                if (z) {
                    if (!findTorch()) {
                        Hook.log("torch menu without a camera torch");
                    }
                } else {
                    watchList();
                }
                render(XposedHelpers.callMethod(obj, "getState", new Object[0]));
                this.settings.setVisibility(0);
                arrive();
                setVisibility(0);
                this.show.to(1.0f);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        void close() {
            if (this.volumesOpen || this.brightnessOpen || this.outputOpen) {
                if (this.outputOpen) {
                    stopOutputScan();
                }
                this.outputOpen = false;
                this.brightnessOpen = false;
                this.volumesOpen = false;
                leave();
                return;
            }
            final Object obj = this.tile;
            if (obj == null) {
                return;
            }
            final Object obj2 = this.watch.callback;
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.4
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.lambda$close$20(obj, obj2);
                }
            });
            this.tile = null;
            this.watch = null;
            this.leaving = true;
            this.shownRows = "";
            if (this.listWatched) {
                this.listWatched = false;
                Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.5
                    @Override // java.lang.Runnable
                    public final void run() {
                        Sheet.this.m185lambda$close$21$devcolorccPanel$Sheet();
                    }
                });
            }
            leave();
        }

        static /* synthetic */ void lambda$close$20(Object obj, Object obj2) {
            XposedHelpers.callMethod(obj, "removeCallback", new Object[]{obj2});
        }

        /* renamed from: lambda$close$21$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m185lambda$close$21$devcolorccPanel$Sheet() {
            getContext().unregisterReceiver(this.listReceiver);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void applyShow(float f, float f2) {
            LinearLayout linearLayout;
            float fClamp = Logic.clamp(f);
            getBackground().setAlpha(Math.round(255.0f * fClamp));
            this.card.setAlpha(Logic.clamp(2.5f * f));
            float fClamp2 = Logic.clamp((f - 0.3f) / 0.5f);
            int i = 0;
            while (true) {
                int childCount = this.card.getChildCount();
                linearLayout = this.card;
                if (i >= childCount) {
                    break;
                }
                linearLayout.getChildAt(i).setAlpha(fClamp2);
                i++;
            }
            if (linearLayout.getWidth() > 0) {
                getLocationOnScreen(this.at);
                int width = this.card.getWidth();
                float f3 = width;
                float f4 = f3 / 2.0f;
                float height = this.card.getHeight();
                float f5 = height / 2.0f;
                float f6 = 1.0f - f;
                float fClamp3 = Logic.clamp(f);
                this.card.setPivotX(f4);
                this.card.setPivotY(f5);
                this.card.setTranslationX((this.origin[0] - ((this.at[0] + this.card.getLeft()) + f4)) * f6);
                this.card.setTranslationY((this.origin[1] - ((this.at[1] + this.card.getTop()) + f5)) * f6);
                float fMin = Math.min(1.0f, (getHeight() - Panel.this.dp(24.0f)) / height);
                float[] fArr = this.heldSize;
                float fMax = Math.max(0.05f, Math.max(fArr[0] / f3, fArr[1] / height));
                float f7 = ((fMin - fMax) * f) + fMax;
                this.card.setScaleX(f7);
                this.card.setScaleY(f7);
                float fMin2 = Math.min(f3, this.heldSize[0] / fMax);
                float fMin3 = Math.min(height, this.heldSize[1] / fMax);
                float[] fArr2 = this.heldSize;
                float fPillRadius = Glass.pillRadius(fArr2[0], fArr2[1]) / fMax;
                this.windowW = fMin2 + ((f3 - fMin2) * fClamp3);
                this.windowH = fMin3 + ((height - fMin3) * fClamp3);
                this.windowRound = fPillRadius + ((Panel.this.dp(34.0f) - fPillRadius) * fClamp3);
                boolean z = fClamp3 < 0.999f;
                if (this.card.getClipToOutline() != z) {
                    this.card.setClipToOutline(z);
                }
                if (z) {
                    this.card.invalidateOutline();
                }
            }
            Panel.this.sheetBack = fClamp;
            Panel.this.backdrop();
            if (!this.leaving || f > 0.01f || getVisibility() == 8) {
                return;
            }
            setVisibility(8);
            land();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void render(Object obj) {
            if (obj == null || this.tile == null) {
                return;
            }
            int iStateOf = Panel.stateOf(obj);
            boolean z = iStateOf == 2;
            this.toggle.set(z, getVisibility() == 0);
            this.toggle.setEnabled(iStateOf != 0);
            if (this.menu == 6) {
                this.torchLabel.setText(Panel.this.word(z ? "torch_on" : "torch_off", new Object[0]));
                if (this.beam.held()) {
                    return;
                }
                this.beam.set(z ? this.torchStrength : 0.0f, getVisibility() == 0);
                return;
            }
            refreshList();
        }

        private void watchList() {
            IntentFilter intentFilter = new IntentFilter();
            int i = this.menu;
            if (i == 2) {
                intentFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
                intentFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
                intentFilter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
                intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
            } else if (i == 1) {
                intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
                intentFilter.addAction("android.net.wifi.STATE_CHANGE");
                intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
                Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.37
                    @Override // java.lang.Runnable
                    public final void run() {
                        Sheet.this.m212lambda$watchList$22$devcolorccPanel$Sheet();
                    }
                });
            } else if (i == 3) {
                this.usage = null;
                Panel.this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.Sheet.38
                    @Override // java.lang.Runnable
                    public final void run() {
                        Sheet.this.m214lambda$watchList$24$devcolorccPanel$Sheet();
                    }
                });
            }
            if (intentFilter.countActions() > 0) {
                getContext().registerReceiver(this.listReceiver, intentFilter, null, Panel.this.main, 2);
                this.listWatched = true;
            }
        }

        /* renamed from: lambda$watchList$22$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m212lambda$watchList$22$devcolorccPanel$Sheet() {
            ((WifiManager) getContext().getSystemService(WifiManager.class)).startScan();
        }

        /* renamed from: lambda$watchList$24$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m214lambda$watchList$24$devcolorccPanel$Sheet() {
            final String usage = readUsage();
            Panel.this.main.post(new Runnable() { // from class: dev.colorcc.Panel.Sheet.41
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m213lambda$watchList$23$devcolorccPanel$Sheet(usage);
                }
            });
        }

        /* renamed from: lambda$watchList$23$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m213lambda$watchList$23$devcolorccPanel$Sheet(String str) {
            this.usage = str;
            Panel.this.guard(new AnonymousClass0());
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void refreshList() {
            int i;
            if (this.tile != null || this.outputOpen) {
                this.rows.clear();
                try {
                    int i2 = this.menu;
                    if (i2 == 2) {
                        listBluetooth();
                    } else if (i2 == 1) {
                        listWifi();
                    } else if (i2 == 3) {
                        listSims();
                    } else if (i2 == 4) {
                        listHotspot();
                    } else if (i2 == 5) {
                        listDnd();
                    } else if (i2 == 7) {
                        listOutputs();
                    }
                } catch (Throwable th) {
                    Hook.log("menu not listed: " + th);
                }
                StringBuilder sb = new StringBuilder();
                Iterator<Row> it = this.rows.iterator();
                while (it.hasNext()) {
                    sb.append(it.next());
                    sb.append('\n');
                }
                if (sb.toString().equals(this.shownRows)) {
                    return;
                }
                this.shownRows = sb.toString();
                this.list.removeAllViews();
                for (int i3 = 0; i3 < this.rows.size(); i3++) {
                    Row row = this.rows.get(i3);
                    addRow(row, (row.header || (i = i3 + 1) >= this.rows.size() || this.rows.get(i).header) ? false : true);
                }
            }
        }

        private void heading(String str, String str2, Runnable runnable) {
            Row row = new Row();
            row.header = true;
            row.name = str;
            row.side = str2;
            row.sideAction = runnable;
            this.rows.add(row);
        }

        private Row item(String str, String str2, String str3, boolean z, Runnable runnable) {
            Row row = new Row();
            row.icon = str;
            row.name = str2;
            row.status = str3;
            row.on = z;
            row.action = runnable;
            this.rows.add(row);
            return row;
        }

        private void listBluetooth() {
            BluetoothAdapter adapter = ((BluetoothManager) getContext().getSystemService(BluetoothManager.class)).getAdapter();
            if (adapter == null || !adapter.isEnabled()) {
                item(null, Panel.this.word("bluetooth_off", new Object[0]), null, false, null);
                return;
            }
            ArrayList<BluetoothDevice> arrayList = new ArrayList(adapter.getBondedDevices());
            arrayList.sort(new Comparator() { // from class: dev.colorcc.Panel.Sheet.20
                @Override // java.util.Comparator
                public final int compare(Object obj, Object obj2) {
                    return Sheet.this.m186lambda$listBluetooth$25$devcolorccPanel$Sheet((BluetoothDevice) obj, (BluetoothDevice) obj2);
                }
            });
            heading(Panel.this.word("paired_devices", new Object[0]), null, null);
            for (final BluetoothDevice bluetoothDevice : arrayList) {
                final boolean zConnected = connected(bluetoothDevice);
                item("M17.71,7.71L12,2h-1v7.59L6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 11,14.41V22h1l5.71,-5.71 -4.3,-4.29 4.3,-4.29zM13,5.83l1.88,1.88L13,9.59V5.83zM14.88,16.29L13,18.17v-3.76l1.88,1.88z", bluetoothDevice.getAlias() != null ? bluetoothDevice.getAlias() : bluetoothDevice.getName() != null ? bluetoothDevice.getName() : bluetoothDevice.getAddress(), zConnected ? Panel.this.word("connected", new Object[0]) : null, zConnected, new Runnable() { // from class: dev.colorcc.Panel.Sheet.21
                    @Override // java.lang.Runnable
                    public final void run() {
                        Sheet.this.m187lambda$listBluetooth$26$devcolorccPanel$Sheet(bluetoothDevice, zConnected);
                    }
                });
            }
            if (arrayList.isEmpty()) {
                item(null, Panel.this.word("no_paired", new Object[0]), null, false, null);
            }
        }

        /* renamed from: lambda$listBluetooth$25$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ int m186lambda$listBluetooth$25$devcolorccPanel$Sheet(BluetoothDevice bluetoothDevice, BluetoothDevice bluetoothDevice2) {
            return Boolean.compare(connected(bluetoothDevice2), connected(bluetoothDevice));
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* renamed from: bluetoothTap, reason: merged with bridge method [inline-methods] */
        public void m187lambda$listBluetooth$26$devcolorccPanel$Sheet(BluetoothDevice bluetoothDevice, boolean z) {
            Object obj = null;
            try {
                for (Object obj2 : bluetoothDevices(XposedHelpers.getObjectField(this.tile, "mController"))) {
                    if (bluetoothDevice.equals(XposedHelpers.getObjectField(obj2, "mDevice"))) {
                        obj = obj2;
                    }
                }
            } catch (Throwable th) {
                Hook.log("bluetooth devices not read from SystemUI: " + th);
            }
            StringBuilder sb = new StringBuilder("bluetooth ");
            sb.append(z ? "disconnect" : "connect");
            sb.append(obj != null ? " via SystemUI" : " direct");
            Hook.log(sb.toString());
            if (obj == null) {
                StringBuilder sb2 = new StringBuilder("bluetooth result: ");
                sb2.append(XposedHelpers.callMethod(bluetoothDevice, z ? "disconnect" : "connect", new Object[0]));
                Hook.log(sb2.toString());
            } else {
                if (z) {
                    XposedHelpers.callMethod(obj, "disconnect", new Object[0]);
                    return;
                }
                String[] strArr = {"connect", "connect$1", "connectDevice"};
                for (int i = 0; i < 3; i++) {
                    try {
                        XposedHelpers.callMethod(obj, strArr[i], new Object[0]);
                        return;
                    } catch (NoSuchMethodError unused) {
                    }
                }
                Hook.log("bluetooth connect method missing");
            }
        }

        /**
         * [Miku-Picked UI] SystemUI's cached Bluetooth devices: One UI's BluetoothController
         * has getDevices(); AOSP's keeps them in its LocalBluetoothManager.
         */
        private Collection bluetoothDevices(Object controller) {
            try {
                return (Collection) XposedHelpers.callMethod(controller, "getDevices", new Object[0]);
            } catch (NoSuchMethodError e) {
                Object manager = XposedHelpers.callMethod(XposedHelpers.getObjectField(controller, "mLocalBluetoothManager"), "getCachedDeviceManager", new Object[0]);
                return (Collection) XposedHelpers.callMethod(manager, "getCachedDevicesCopy", new Object[0]);
            }
        }

        private boolean connected(BluetoothDevice bluetoothDevice) {
            try {
                return ((Boolean) XposedHelpers.callMethod(bluetoothDevice, "isConnected", new Object[0])).booleanValue();
            } catch (Throwable unused) {
                return false;
            }
        }

        private void listWifi() {
            final WifiManager wifiManager = (WifiManager) getContext().getSystemService(WifiManager.class);
            if (!wifiManager.isWifiEnabled()) {
                item(null, Panel.this.word("wifi_off", new Object[0]), null, false, null);
                return;
            }
            String strSsid = ssid(wifiManager.getConnectionInfo().getSSID());
            HashMap map = new HashMap();
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            if (configuredNetworks != null) {
                for (WifiConfiguration wifiConfiguration : configuredNetworks) {
                    map.put(ssid(wifiConfiguration.SSID), Integer.valueOf(wifiConfiguration.networkId));
                }
            }
            ArrayList<ScanResult> arrayList = new ArrayList(wifiManager.getScanResults());
            arrayList.sort(new Comparator() { // from class: dev.colorcc.Panel.Sheet.9
                @Override // java.util.Comparator
                public final int compare(Object obj, Object obj2) {
                    return Sheet.lambda$listWifi$27((ScanResult) obj, (ScanResult) obj2);
                }
            });
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            for (ScanResult scanResult : arrayList) {
                String strSsid2 = ssid(scanResult.SSID);
                if (!strSsid2.isEmpty() && !linkedHashMap.containsKey(strSsid2)) {
                    linkedHashMap.put(strSsid2, scanResult);
                }
            }
            List<String> arrayList2 = new ArrayList(linkedHashMap.keySet());
            if (arrayList2.remove(strSsid)) {
                arrayList2.add(0, strSsid);
            }
            if (arrayList2.size() > 12) {
                arrayList2 = arrayList2.subList(0, 12);
            }
            boolean z = false;
            for (final String str : arrayList2) {
                boolean zEquals = str.equals(strSsid);
                final Integer num = (Integer) map.get(str);
                if (zEquals || num != null) {
                    if (!z) {
                        heading(Panel.this.word("saved_networks", new Object[0]), null, null);
                    }
                    mark(item("M1,9l2,2c4.97,-4.97 13.03,-4.97 18,0l2,-2C16.93,2.93 7.08,2.93 1,9zM9,17l3,3 3,-3c-1.65,-1.66 -4.34,-1.66 -6,0zM5,13l2,2c2.76,-2.76 7.24,-2.76 10,0l2,-2C15.14,9.14 8.87,9.14 5,13z", str, zEquals ? Panel.this.word("connected", new Object[0]) : null, zEquals, zEquals ? null : new Runnable() { // from class: dev.colorcc.Panel.Sheet.10
                        @Override // java.lang.Runnable
                        public final void run() {
                            Sheet.this.m193lambda$listWifi$28$devcolorccPanel$Sheet(wifiManager, str, num);
                        }
                    }), (ScanResult) linkedHashMap.get(str));
                    z = true;
                }
            }
            heading(Panel.this.word("other_networks", new Object[0]), Panel.this.word("refresh", new Object[0]), new Runnable() { // from class: dev.colorcc.Panel.Sheet.11
                @Override // java.lang.Runnable
                public final void run() {
                    wifiManager.startScan();
                }
            });
            boolean z2 = false;
            for (final String str2 : arrayList2) {
                if (!str2.equals(strSsid) && !map.containsKey(str2)) {
                    mark(item("M1,9l2,2c4.97,-4.97 13.03,-4.97 18,0l2,-2C16.93,2.93 7.08,2.93 1,9zM9,17l3,3 3,-3c-1.65,-1.66 -4.34,-1.66 -6,0zM5,13l2,2c2.76,-2.76 7.24,-2.76 10,0l2,-2C15.14,9.14 8.87,9.14 5,13z", str2, null, false, new Runnable() { // from class: dev.colorcc.Panel.Sheet.12
                        @Override // java.lang.Runnable
                        public final void run() {
                            Sheet.this.m194lambda$listWifi$30$devcolorccPanel$Sheet(wifiManager, str2);
                        }
                    }), (ScanResult) linkedHashMap.get(str2));
                    z2 = true;
                }
            }
            if (z2) {
                return;
            }
            item(null, Panel.this.word("searching", new Object[0]), null, false, null);
        }

        static /* synthetic */ int lambda$listWifi$27(ScanResult scanResult, ScanResult scanResult2) {
            return Integer.compare(scanResult2.level, scanResult.level);
        }

        /* renamed from: lambda$listWifi$30$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m194lambda$listWifi$30$devcolorccPanel$Sheet(WifiManager wifiManager, String str) {
            m193lambda$listWifi$28$devcolorccPanel$Sheet(wifiManager, str, null);
        }

        private void mark(Row row, ScanResult scanResult) {
            String str;
            if (scanResult == null) {
                return;
            }
            String str2 = scanResult.capabilities == null ? "" : scanResult.capabilities;
            row.lock = str2.contains("WEP") || str2.contains("PSK") || str2.contains("SAE") || str2.contains("EAP");
            int wifiStandard = scanResult.getWifiStandard();
            if (wifiStandard == 8) {
                str = "7";
            } else if (wifiStandard == 6) {
                str = scanResult.frequency >= 5925 ? "6E" : "6";
            } else {
                str = null;
            }
            row.standard = str;
        }

        private String ssid(String str) {
            if (str == null || str.equals("<unknown ssid>")) {
                return "";
            }
            return (str.length() > 1 && str.startsWith("\"") && str.endsWith("\"")) ? str.substring(1, str.length() - 1) : str;
        }

        /*
         * [Miku-Picked UI] Rebuilt from the bytecode (jadx could not decompile it). Wi-Fi
         * entries come from SystemUI's AccessPointController (Hook.accessPoints), which on
         * AOSP is not a field of the Internet tile.
         */
        /* renamed from: wifiTap, reason: merged with bridge method [inline-methods] */
        public void m193lambda$listWifi$28$devcolorccPanel$Sheet(WifiManager wifiManager, String str, Integer num) {
            Object obj = null;
            try {
                for (Object obj2 : (List) XposedHelpers.callMethod(XposedHelpers.getObjectField(Hook.accessPoints(this.tile), "mWifiPickerTracker"), "getWifiEntries", new Object[0])) {
                    if (str.equals(XposedHelpers.callMethod(obj2, "getSsid", new Object[0]))) {
                        obj = obj2;
                    }
                }
            } catch (Throwable th) {
                Hook.log("wifi entries not read from SystemUI: " + th);
            }
            StringBuilder sb = new StringBuilder("wifi connect ");
            sb.append(obj != null ? "via SystemUI" : "direct");
            sb.append(", saved ");
            sb.append(num != null);
            Hook.log(sb.toString());
            if (obj != null) {
                boolean open = ((Integer) XposedHelpers.callMethod(obj, "getSecurity", new Object[0])).intValue() == 0;
                if ((((Boolean) XposedHelpers.callMethod(obj, "isSaved", new Object[0])).booleanValue() || open)
                        && !((Boolean) XposedHelpers.callMethod(obj, "shouldEditBeforeConnect", new Object[0])).booleanValue()
                        && ((Boolean) XposedHelpers.callMethod(obj, "canConnect", new Object[0])).booleanValue()) {
                    XposedHelpers.callMethod(obj, "connect", new Object[]{null});
                    return;
                }
            }
            if (num != null && (obj == null || ((Boolean) XposedHelpers.callMethod(obj, "isSaved", new Object[0])).booleanValue())) {
                connectWifi(wifiManager, num.intValue());
            } else {
                openTileSettings();
            }
        }

        private void connectWifi(WifiManager wifiManager, int i) {
            try {
                WifiManager.class.getMethod("connect", Integer.TYPE, Class.forName("android.net.wifi.WifiManager$ActionListener")).invoke(wifiManager, Integer.valueOf(i), null);
            } catch (Throwable th) {
                Hook.log("wifi not connected: " + th);
            }
        }

        private void listSims() {
            final SubscriptionManager subscriptionManager = (SubscriptionManager) getContext().getSystemService(SubscriptionManager.class);
            List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            if (activeSubscriptionInfoList == null || activeSubscriptionInfoList.isEmpty()) {
                item(null, Panel.this.word("no_sim", new Object[0]), null, false, null);
                return;
            }
            int defaultDataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
            for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                final int subscriptionId = subscriptionInfo.getSubscriptionId();
                boolean z = subscriptionId == defaultDataSubscriptionId;
                String str = null;
                Runnable runnable = z ? null : new Runnable() { // from class: dev.colorcc.Panel.Sheet.24
                    @Override // java.lang.Runnable
                    public final void run() {
                        Sheet.this.m192lambda$listSims$32$devcolorccPanel$Sheet(subscriptionManager, subscriptionId);
                    }
                };
                String strValueOf = String.valueOf(subscriptionInfo.getDisplayName());
                if (z) {
                    str = this.usage;
                }
                Row rowItem = item(null, strValueOf, str, false, runnable);
                rowItem.badge = String.valueOf(subscriptionInfo.getSimSlotIndex() + 1);
                rowItem.radio = Boolean.valueOf(z);
            }
        }

        /* renamed from: lambda$listSims$32$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m192lambda$listSims$32$devcolorccPanel$Sheet(final SubscriptionManager subscriptionManager, final int i) {
            Panel.this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.Sheet.8
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.lambda$listSims$31(subscriptionManager, i);
                }
            });
        }

        static /* synthetic */ void lambda$listSims$31(SubscriptionManager subscriptionManager, int i) {
            try {
                XposedHelpers.callMethod(subscriptionManager, "setDefaultDataSubId", new Object[]{Integer.valueOf(i)});
            } catch (Throwable th) {
                Hook.log("data SIM not set: " + th);
            }
        }

        private String readUsage() {
            try {
                NetworkStatsManager networkStatsManager = (NetworkStatsManager) getContext().getSystemService(NetworkStatsManager.class);
                Calendar calendar = Calendar.getInstance();
                long timeInMillis = calendar.getTimeInMillis();
                calendar.set(11, 0);
                calendar.set(12, 0);
                calendar.set(13, 0);
                calendar.set(14, 0);
                long timeInMillis2 = calendar.getTimeInMillis();
                calendar.set(5, 1);
                return Panel.this.word("data_used", Formatter.formatShortFileSize(getContext(), used(networkStatsManager, timeInMillis2, timeInMillis)), Formatter.formatShortFileSize(getContext(), used(networkStatsManager, calendar.getTimeInMillis(), timeInMillis)));
            } catch (Throwable th) {
                Hook.log("data usage not read: " + th);
                return null;
            }
        }

        private long used(NetworkStatsManager networkStatsManager, long j, long j2) throws Exception {
            NetworkStats.Bucket bucketQuerySummaryForDevice = networkStatsManager.querySummaryForDevice(0, null, j, j2);
            return bucketQuerySummaryForDevice.getRxBytes() + bucketQuerySummaryForDevice.getTxBytes();
        }

        /* [Miku-Picked UI] Rebuilt from the bytecode (jadx could not decompile it). */
        private void listHotspot() {
            try {
                Object objCallMethod = XposedHelpers.callMethod(getContext().getSystemService(WifiManager.class), "getSoftApConfiguration", new Object[0]);
                item(null, String.valueOf(XposedHelpers.callMethod(objCallMethod, "getSsid", new Object[0])), Panel.this.word("name", new Object[0]), false, null);
                String strWord = Panel.this.keyguard.isKeyguardLocked() ? Panel.this.word("unlock_to_see", new Object[0]) : (String) XposedHelpers.callMethod(objCallMethod, "getPassphrase", new Object[0]);
                if (strWord != null) {
                    item(null, strWord, Panel.this.word("password", new Object[0]), false, null);
                }
            } catch (Throwable th) {
                Hook.log("hotspot details not read: " + th);
            }
            int iHotspotDevices = hotspotDevices();
            if (iHotspotDevices >= 0) {
                Panel panel = Panel.this;
                item(null, iHotspotDevices == 1 ? panel.word("device_connected", new Object[0]) : panel.word("devices_connected", Integer.valueOf(iHotspotDevices)), null, false, null);
            }
        }

        private int hotspotDevices() {
            String[] strArr = {"mHotspotController", "hotspotController", "mController"};
            for (int i = 0; i < 3; i++) {
                try {
                    return ((Integer) XposedHelpers.callMethod(XposedHelpers.getObjectField(this.tile, strArr[i]), "getNumConnectedDevices", new Object[0])).intValue();
                } catch (Throwable unused) {
                }
            }
            Hook.log("hotspot devices not read");
            return -1;
        }

        private void listDnd() {
            int i;
            final NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(NotificationManager.class);
            boolean z = ((Integer) XposedHelpers.callMethod(notificationManager, "getZenMode", new Object[0])).intValue() != 0;
            this.listScroll.setVisibility(z ? 0 : 8);
            this.note.setVisibility(z ? 8 : 0);
            long jDndEnd = z ? dndEnd(notificationManager) : 0L;
            this.sub.setVisibility(jDndEnd > 0 ? 0 : 8);
            if (jDndEnd > 0) {
                this.sub.setText(Panel.this.word("off_at", DateFormat.getTimeFormat(getContext()).format(new Date(jDndEnd))));
            }
            if (z) {
                if (jDndEnd <= 0) {
                    i = 0;
                } else {
                    i = this.dndHours;
                    if (i <= 0) {
                        i = -1;
                    }
                }
                int[] iArr = {0, 1, 2, 8};
                int i2 = 0;
                while (i2 < 4) {
                    final int i3 = iArr[i2];
                    Panel panel = Panel.this;
                    Sheet sheet = this;
                    sheet.item(null, i3 == 0 ? panel.word("dnd_until_off", new Object[0]) : panel.word("hours", Integer.valueOf(i3)), null, false, new Runnable() { // from class: dev.colorcc.Panel.Sheet.7
                        @Override // java.lang.Runnable
                        public final void run() {
                            Sheet.this.m188lambda$listDnd$33$devcolorccPanel$Sheet(notificationManager, i3);
                        }
                    }).radio = Boolean.valueOf(i3 == i);
                    i2++;
                }
            }
        }

        private long dndEnd(NotificationManager notificationManager) {
            try {
                Object objectField = XposedHelpers.getObjectField(XposedHelpers.callMethod(notificationManager, "getZenModeConfig", new Object[0]), "manualRule");
                Object objectField2 = objectField == null ? null : XposedHelpers.getObjectField(objectField, "conditionId");
                if (objectField2 == null) {
                    return 0L;
                }
                return ((Long) XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.service.notification.ZenModeConfig", (ClassLoader) null), "tryParseCountdownConditionId", new Object[]{objectField2})).longValue();
            } catch (Throwable th) {
                Hook.log("do not disturb end not read: " + th);
                return 0L;
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* renamed from: setDnd, reason: merged with bridge method [inline-methods] */
        public void m188lambda$listDnd$33$devcolorccPanel$Sheet(NotificationManager notificationManager, int i) {
            this.dndHours = i;
            XposedHelpers.callMethod(notificationManager, "setZenMode", new Class[]{Integer.TYPE, Uri.class, String.class}, new Object[]{1, i != 0 ? XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.service.notification.ZenModeConfig", (ClassLoader) null), "toCountdownConditionId", new Object[]{Long.valueOf(System.currentTimeMillis() + (i * 3600000))}) : null, "ColorCC"});
        }

        /* JADX WARN: Multi-variable type inference failed */
        private void addRow(final Row row, boolean z) {
            Context context = getContext();
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            linearLayoutHorizontal.setGravity(16);
            if (row.header) {
                TextView textViewLabel = Panel.this.label(context, 13.0f, -1711276033, 1);
                textViewLabel.setText(row.name);
                linearLayoutHorizontal.addView(textViewLabel, new LinearLayout.LayoutParams(0, -2, 1.0f));
                if (row.side != null) {
                    TextView textViewLabel2 = Panel.this.label(context, 13.0f, -1, 1);
                    textViewLabel2.setText(row.side);
                    textViewLabel2.setPadding(Panel.this.dp(12.0f), Panel.this.dp(8.0f), 0, Panel.this.dp(8.0f));
                    textViewLabel2.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Sheet.22
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            Sheet.this.m182lambda$addRow$34$devcolorccPanel$Sheet(row, view);
                        }
                    });
                    linearLayoutHorizontal.addView(textViewLabel2);
                }
                this.list.addView(linearLayoutHorizontal, new LinearLayout.LayoutParams(-1, Panel.this.dp(40.0f)));
                return;
            }
            boolean z2 = (row.icon == null && row.badge == null) ? false : true;
            if (row.badge != null) {
                TextView textViewLabel3 = Panel.this.label(context, 12.0f, -1, 1);
                textViewLabel3.setText(row.badge);
                textViewLabel3.setGravity(17);
                textViewLabel3.setTypeface(Typeface.DEFAULT_BOLD);
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setColor(-14503604);
                gradientDrawable.setCornerRadius(Panel.this.dp(4.0f));
                textViewLabel3.setBackground(gradientDrawable);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Panel.this.dp(20.0f), Panel.this.dp(26.0f));
                layoutParams.leftMargin = Panel.this.dp(2.0f);
                layoutParams.rightMargin = Panel.this.dp(18.0f);
                linearLayoutHorizontal.addView(textViewLabel3, layoutParams);
            } else if (row.icon != null) {
                int i = row.on ? -10773505 : -1;
                ImageView imageView = new ImageView(context);
                imageView.setImageDrawable(Icons.of(row.icon));
                imageView.setImageTintList(ColorStateList.valueOf(i));
                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(Panel.this.dp(24.0f), Panel.this.dp(24.0f));
                layoutParams2.rightMargin = Panel.this.dp(16.0f);
                View iconView = imageView;
                if (row.lock || row.standard != null) {
                    FrameLayout frameLayout = new FrameLayout(context);
                    frameLayout.setClipChildren(false);
                    frameLayout.addView(imageView, new FrameLayout.LayoutParams(-1, -1));
                    if (row.lock) {
                        ImageView imageView2 = new ImageView(context);
                        imageView2.setImageDrawable(Icons.of("M18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6v2H6c-1.1,0 -2,0.9 -2,2v10c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10c0,-1.1 -0.9,-2 -2,-2zM9,6c0,-1.66 1.34,-3 3,-3s3,1.34 3,3v2H9V6z"));
                        imageView2.setImageTintList(ColorStateList.valueOf(i));
                        FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(Panel.this.dp(10.0f), Panel.this.dp(10.0f), 8388693);
                        layoutParams3.rightMargin = -Panel.this.dp(3.0f);
                        layoutParams3.bottomMargin = -Panel.this.dp(1.0f);
                        frameLayout.addView(imageView2, layoutParams3);
                    }
                    if (row.standard != null) {
                        TextView textViewLabel4 = Panel.this.label(context, 9.0f, i, 1);
                        textViewLabel4.setText(row.standard);
                        textViewLabel4.setTypeface(Typeface.DEFAULT_BOLD);
                        textViewLabel4.setIncludeFontPadding(false);
                        FrameLayout.LayoutParams layoutParams4 = new FrameLayout.LayoutParams(-2, -2, 8388661);
                        layoutParams4.rightMargin = -Panel.this.dp(6.0f);
                        layoutParams4.topMargin = -Panel.this.dp(3.0f);
                        frameLayout.addView(textViewLabel4, layoutParams4);
                    }
                    iconView = frameLayout;
                }
                linearLayoutHorizontal.addView(iconView, layoutParams2);
            }
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            TextView textViewLabel5 = Panel.this.label(context, 16.0f, -1, 1);
            textViewLabel5.setText(row.name);
            linearLayout.addView(textViewLabel5);
            if (row.status != null) {
                TextView textViewLabel6 = Panel.this.label(context, 13.0f, -1711276033, 2);
                textViewLabel6.setText(row.status);
                linearLayout.addView(textViewLabel6);
            }
            linearLayoutHorizontal.addView(linearLayout, new LinearLayout.LayoutParams(0, -2, 1.0f));
            if (row.radio != null) {
                LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(Panel.this.dp(22.0f), Panel.this.dp(22.0f));
                layoutParams5.leftMargin = Panel.this.dp(12.0f);
                linearLayoutHorizontal.addView(radio(context, row.radio.booleanValue()), layoutParams5);
            }
            if (row.action != null) {
                GradientDrawable gradientDrawable2 = new GradientDrawable();
                gradientDrawable2.setColor(-1);
                gradientDrawable2.setCornerRadius(Panel.this.dp(12.0f));
                linearLayoutHorizontal.setBackground(new RippleDrawable(ColorStateList.valueOf(872415231), null, gradientDrawable2));
                linearLayoutHorizontal.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Sheet.23
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        Sheet.this.m184lambda$addRow$36$devcolorccPanel$Sheet(row, view);
                    }
                });
            }
            this.list.addView(linearLayoutHorizontal, new LinearLayout.LayoutParams(-1, Panel.this.dp(row.status != null ? 62.0f : 50.0f)));
            if (z) {
                View view = new View(context);
                view.setBackgroundColor(536870911);
                LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(-1, 1);
                layoutParams6.leftMargin = z2 ? Panel.this.dp(40.0f) : 0;
                this.list.addView(view, layoutParams6);
            }
        }

        /* renamed from: lambda$addRow$34$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m182lambda$addRow$34$devcolorccPanel$Sheet(Row row, View view) {
            Panel.this.guard(row.sideAction);
        }

        /* renamed from: lambda$addRow$36$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m184lambda$addRow$36$devcolorccPanel$Sheet(Row row, View view) {
            Hook.log("menu row tapped: " + row.name);
            Panel.this.guard(row.action);
            Panel.this.main.postDelayed(new Runnable() { // from class: dev.colorcc.Panel.Sheet.42
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m183lambda$addRow$35$devcolorccPanel$Sheet();
                }
            }, 1200L);
        }

        /* renamed from: lambda$addRow$35$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m183lambda$addRow$35$devcolorccPanel$Sheet() {
            Panel.this.guard(new AnonymousClass0());
        }

        private View radio(Context context, boolean z) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(1);
            ImageView imageView = new ImageView(context);
            if (!z) {
                gradientDrawable.setStroke(Panel.this.dp(1.5f), -2130706433);
            } else {
                gradientDrawable.setColor(-15374872);
                GradientDrawable gradientDrawable2 = new GradientDrawable();
                gradientDrawable2.setShape(1);
                gradientDrawable2.setColor(-1);
                imageView.setImageDrawable(gradientDrawable2);
                imageView.setPadding(Panel.this.dp(7.0f), Panel.this.dp(7.0f), Panel.this.dp(7.0f), Panel.this.dp(7.0f));
            }
            imageView.setBackground(gradientDrawable);
            return imageView;
        }

        private boolean findTorch() {
            try {
                CameraManager cameraManager = (CameraManager) getContext().getSystemService(CameraManager.class);
                for (String str : cameraManager.getCameraIdList()) {
                    CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(str);
                    Integer num = (Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (Boolean.TRUE.equals(cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) && num != null && num.intValue() == 1) {
                        Integer num2 = (Integer) cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL);
                        this.torchCamera = str;
                        int iIntValue = (num2 == null || num2.intValue() < 2) ? 1 : num2.intValue();
                        this.torchMax = iIntValue;
                        this.torchLevel = -1;
                        this.torchStrength = iIntValue < 2 ? 1.0f : cameraManager.getTorchStrengthLevel(str) / this.torchMax;
                        return true;
                    }
                }
            } catch (Throwable th) {
                Hook.log("torch levels missing: " + th);
            }
            this.torchCamera = null;
            return false;
        }

        private void onTorch(float f) throws CameraAccessException {
            int iMax = f < 0.03f ? 0 : Math.max(1, Math.round(f * this.torchMax));
            if (iMax == this.torchLevel || this.torchCamera == null) {
                return;
            }
            this.torchLevel = iMax;
            if (iMax > 0) {
                this.torchStrength = iMax / this.torchMax;
            }
            try {
                CameraManager cameraManager = (CameraManager) getContext().getSystemService(CameraManager.class);
                if (iMax <= 0) {
                    cameraManager.setTorchMode(this.torchCamera, false);
                    return;
                }
                int i = this.torchMax;
                String str = this.torchCamera;
                if (i < 2) {
                    cameraManager.setTorchMode(str, true);
                } else {
                    cameraManager.turnOnTorchWithStrengthLevel(str, iMax);
                }
            } catch (Exception e) {
                Hook.log("torch not set: " + e);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void openTileSettings() {
            if (this.outputOpen) {
                Panel.this.samsungOutput(this.outputPackage);
                return;
            }
            if (this.volumesOpen) {
                Panel.this.openSettings("android.settings.SOUND_SETTINGS");
                return;
            }
            if (this.brightnessOpen) {
                Panel.this.openSettings("android.settings.DISPLAY_SETTINGS");
                return;
            }
            Object obj = this.tile;
            Object objCallMethod = obj == null ? null : XposedHelpers.callMethod(obj, "getLongClickIntent", new Object[0]);
            if (objCallMethod instanceof Intent) {
                Panel.this.startSettings(new Intent((Intent) objCallMethod));
                return;
            }
            String str = new String[]{null, "android.settings.WIFI_SETTINGS", "android.settings.BLUETOOTH_SETTINGS", "android.settings.DATA_USAGE_SETTINGS", "android.settings.WIRELESS_SETTINGS", "android.settings.ZEN_MODE_PRIORITY_SETTINGS", null}[this.menu];
            if (str != null) {
                Panel.this.openSettings(str);
            }
        }
    }

    private static final class Row {
        Runnable action;
        String badge;
        boolean header;
        String icon;
        boolean lock;
        String name;
        boolean on;
        Boolean radio;
        String side;
        Runnable sideAction;
        String standard;
        String status;

        private Row() {
        }

        public String toString() {
            return this.header + "|" + this.icon + "|" + this.lock + "|" + this.standard + "|" + this.badge + "|" + this.name + "|" + this.status + "|" + this.on + "|" + this.radio + "|" + this.side;
        }
    }

    private final class TorchBeam extends View {
        private static final float BEAM_EDGE = 1.15f;
        private static final float BOX = 415.0f;
        private final Bitmap beamSprite;
        private final DashPathEffect dots;
        private float downLevel;
        private float downY;
        private final Paint glow;
        private boolean held;
        private final Paint led;
        private final Motion.Value level;
        private final Paint light;
        private final Paint line;
        private final Consumer<Float> listener;
        private final Paint metal;
        private boolean moved;
        private final RectF oval;
        private final Path path;
        private final Bitmap sprite;
        private final Path torch;

        TorchBeam(Context context, Consumer<Float> consumer) {
            super(context);
            Paint paint = new Paint(1);
            this.line = paint;
            Paint paint2 = new Paint(1);
            this.glow = paint2;
            this.light = new Paint(2);
            Paint paint3 = new Paint(1);
            this.metal = paint3;
            Paint paint4 = new Paint(1);
            this.led = paint4;
            this.path = new Path();
            Path path = new Path();
            this.torch = path;
            this.sprite = glowSprite();
            this.beamSprite = beamSprite();
            this.oval = new RectF();
            this.listener = consumer;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(-1);
            this.dots = new DashPathEffect(new float[]{0.1f, Panel.this.dp(7.0f)}, 0.0f);
            paint3.setColor(-855310);
            paint4.setColor(-38357);
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(-18.5f, 11.5f);
            path.quadTo(-18.5f, 9.5f, -16.5f, 9.5f);
            path.lineTo(16.5f, 9.5f);
            path.quadTo(18.5f, 9.5f, 18.5f, 11.5f);
            path.cubicTo(18.5f, 17.5f, 12.0f, 21.0f, 10.3f, 26.0f);
            path.lineTo(10.3f, 66.0f);
            path.quadTo(10.3f, 70.0f, 6.3f, 70.0f);
            path.lineTo(-6.3f, 70.0f);
            path.quadTo(-10.3f, 70.0f, -10.3f, 66.0f);
            path.lineTo(-10.3f, 26.0f);
            path.cubicTo(-12.0f, 21.0f, -18.5f, 17.5f, -18.5f, 11.5f);
            path.close();
            path.addRoundRect(-5.85f, 26.5f, 5.85f, 49.0f, 5.85f, 5.85f, Path.Direction.CW);
            paint2.setFilterBitmap(true);
            this.level = Panel.this.motion.value(Logic.Spring.SNAPPY, new Motion.Listener() { // from class: dev.colorcc.Panel.TorchBeam.0
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    TorchBeam.this.m217lambda$new$0$devcolorccPanel$TorchBeam(f, f2);
                }
            });
        }

        /* renamed from: lambda$new$0$dev-colorcc-Panel$TorchBeam, reason: not valid java name */
        /* synthetic */ void m217lambda$new$0$devcolorccPanel$TorchBeam(float f, float f2) {
            invalidate();
        }

        boolean held() {
            return this.held;
        }

        void set(float f, boolean z) {
            Motion.Value value = this.level;
            if (z) {
                value.to(f);
            } else {
                value.snap(f);
            }
        }

        private float base() {
            return getHeight() - Panel.this.dp(100.0f);
        }

        private float reach() {
            return base() - (getHeight() * 0.2f);
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                this.held = true;
                this.moved = false;
                this.downY = motionEvent.getY();
                this.downLevel = Logic.clamp(this.level.value());
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
            if (actionMasked == 1) {
                if (!this.moved) {
                    float fMax = this.level.value() > 0.03f ? 0.0f : Math.max(0.1f, Panel.this.sheet.torchStrength);
                    this.level.to(fMax);
                    this.listener.accept(Float.valueOf(fMax));
                }
                this.held = false;
                return true;
            }
            if (actionMasked != 2) {
                if (actionMasked != 3) {
                    return true;
                }
                this.held = false;
                return true;
            }
            if (!this.moved && Math.abs(motionEvent.getY() - this.downY) < ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                return true;
            }
            this.moved = true;
            float fClamp = Logic.clamp(this.downLevel + ((this.downY - motionEvent.getY()) / reach()));
            this.level.snap(fClamp);
            this.listener.accept(Float.valueOf(fClamp));
            return true;
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            float f;
            float f2;
            float width = getWidth();
            float f3 = width / 415.0f;
            float f4 = width / 2.0f;
            float fBase = base();
            float fReach = reach();
            float f5 = width * 0.37f;
            this.line.setPathEffect(this.dots);
            this.line.setStrokeWidth(Panel.this.dp(2.0f));
            this.line.setAlpha(110);
            curve(f4, fBase - fReach, f5, 0.19f * f5);
            canvas.drawPath(this.path, this.line);
            float fClamp = Logic.clamp(this.level.value());
            float f6 = 9.0f * f3;
            float f7 = (fBase - f6) - ((fReach - f6) * fClamp);
            float f8 = 30.0f * f3;
            float f9 = f8 + ((f5 - f8) * fClamp);
            if (fClamp > 0.01f) {
                float f10 = 0.87f * f9 * 1.15f;
                this.oval.set(f4 - f10, f7 - (4.0f * f3), f10 + f4, (2.0f * f3) + fBase);
                canvas.drawBitmap(this.beamSprite, (Rect) null, this.oval, this.light);
                f = f4;
                f2 = fBase;
                glowAt(canvas, f, f2, 14.0f * f3, f3 * 8.0f, -1);
            } else {
                f = f4;
                f2 = fBase;
            }
            this.line.setPathEffect(null);
            this.line.setStrokeWidth(6.0f * f3);
            this.line.setAlpha(255);
            curve(f, f7, f9, 0.18f * f9 * fClamp);
            canvas.drawPath(this.path, this.line);
            int iSave = canvas.save();
            canvas.translate(f, f2);
            canvas.scale(f3, f3);
            canvas.drawRoundRect(-18.5f, 0.0f, 18.5f, 7.5f, 3.0f, 3.0f, this.metal);
            canvas.drawPath(this.torch, this.metal);
            float fClamp2 = 33.3f - (Logic.clamp(fClamp * 8.0f) * 4.6f);
            canvas.drawRoundRect(-3.0f, fClamp2, 3.0f, fClamp2 + 12.0f, 3.0f, 3.0f, this.metal);
            canvas.drawCircle(0.0f, fClamp2 + 4.6f, 1.3f, this.led);
            canvas.restoreToCount(iSave);
        }

        private void glowAt(Canvas canvas, float f, float f2, float f3, float f4, int i) {
            if (f3 < 1.0f || f4 < 1.0f) {
                return;
            }
            this.glow.setColorFilter(new PorterDuffColorFilter((-16777216) | i, PorterDuff.Mode.SRC_IN));
            this.glow.setAlpha(i >>> 24);
            this.oval.set(f - f3, f2 - f4, f + f3, f2 + f4);
            canvas.drawBitmap(this.sprite, (Rect) null, this.oval, this.glow);
        }

        private Bitmap glowSprite() {
            Bitmap bitmapCreateBitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
            Paint paint = new Paint(1);
            paint.setShader(new RadialGradient(48.0f, 48.0f, 48.0f, new int[]{-1, -2130706433, 16777215}, new float[]{0.0f, 0.55f, 1.0f}, Shader.TileMode.CLAMP));
            new Canvas(bitmapCreateBitmap).drawRect(0.0f, 0.0f, 96.0f, 96.0f, paint);
            return bitmapCreateBitmap;
        }

        private Bitmap beamSprite() {
            int[] iArr = new int[32000];
            for (int i = 0; i < 200; i++) {
                for (int i2 = 0; i2 < 160; i2++) {
                    iArr[((199 - i) * 160) + i2] = Logic.beam(((((i2 + 0.5f) / 160.0f) * 2.0f) - 1.0f) * 1.15f, (i + 0.5f) / 200.0f);
                }
            }
            return Bitmap.createBitmap(iArr, 160, 200, Bitmap.Config.ARGB_8888);
        }

        private void curve(float f, float f2, float f3, float f4) {
            this.path.reset();
            float f5 = f2 + f4;
            this.path.moveTo(f - f3, f5);
            this.path.quadTo(f, f2 - f4, f3 + f, f5);
        }
    }

    private final class Rewind implements ValueAnimator.AnimatorUpdateListener {
        private final Motion.Listener out;
        private float reach = 1.0f;
        private ValueAnimator run;
        private final Motion.Value show;

        Rewind(Motion.Value value, Motion.Listener listener) {
            this.show = value;
            this.out = listener;
        }

        void start() {
            stop();
            float[] fArr = Logic.Spring.MENU;
            float fValue = this.show.value();
            float fSpringPeak = Logic.springPeak(fArr);
            float fSpringAt = Logic.springAt(fArr, fSpringPeak);
            boolean z = this.show.running() && this.show.velocity() > 0.0f && fValue < fSpringAt;
            if (z) {
                fSpringPeak = Logic.springTimeOf(fArr, fValue);
            }
            this.reach = z ? 1.0f : fValue / fSpringAt;
            this.show.snap(fValue);
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(fSpringPeak, 0.0f);
            this.run = valueAnimatorOfFloat;
            valueAnimatorOfFloat.setDuration(Math.round((fSpringPeak * 1000.0f) / 1.4f));
            this.run.setInterpolator(new LinearInterpolator());
            this.run.addUpdateListener(this);
            this.run.start();
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            final float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            final float fSpringAt = this.reach * Logic.springAt(Logic.Spring.MENU, fFloatValue);
            this.show.track(fSpringAt);
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Rewind.0
                @Override // java.lang.Runnable
                public final void run() {
                    Rewind.this.m173lambda$onAnimationUpdate$0$devcolorccPanel$Rewind(fSpringAt, fFloatValue);
                }
            });
        }

        /* renamed from: lambda$onAnimationUpdate$0$dev-colorcc-Panel$Rewind, reason: not valid java name */
        /* synthetic */ void m173lambda$onAnimationUpdate$0$devcolorccPanel$Rewind(float f, float f2) {
            this.out.onValue(f, (-this.reach) * Logic.springSpeed(Logic.Spring.MENU, f2));
        }

        void stop() {
            ValueAnimator valueAnimator = this.run;
            if (valueAnimator == null) {
                return;
            }
            valueAnimator.removeAllUpdateListeners();
            this.run.cancel();
            this.run = null;
        }
    }

    private final class Toggle extends View {
        private final Motion.Value on;
        private final Paint paint;

        Toggle(Context context) {
            super(context);
            this.paint = new Paint(1);
            setClickable(true);
            this.on = Panel.this.motion.value(Logic.Spring.SNAPPY, new Motion.Listener() { // from class: dev.colorcc.Panel.Toggle.0
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Toggle.this.m216lambda$new$0$devcolorccPanel$Toggle(f, f2);
                }
            });
        }

        /* renamed from: lambda$new$0$dev-colorcc-Panel$Toggle, reason: not valid java name */
        /* synthetic */ void m216lambda$new$0$devcolorccPanel$Toggle(float f, float f2) {
            invalidate();
        }

        void set(boolean z, boolean z2) {
            Motion.Value value = this.on;
            if (z2) {
                value.to(z ? 1.0f : 0.0f);
            } else {
                value.snap(z ? 1.0f : 0.0f);
            }
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            float width = getWidth();
            float height = getHeight();
            this.paint.setColor(((Integer) Panel.this.argb.evaluate(Logic.clamp(this.on.value()), 1090519039, -15374872)).intValue());
            float f = height / 2.0f;
            canvas.drawRoundRect(0.0f, 0.0f, width, height, f, f, this.paint);
            float f2 = height * 0.12f;
            float f3 = f - f2;
            this.paint.setColor(-1);
            canvas.drawCircle(f2 + f3 + (this.on.value() * ((width - (f2 * 2.0f)) - (2.0f * f3))), f, f3, this.paint);
        }
    }

    private final class Bound {
        final Consumer<Object> apply;
        final Object callback;
        final Object tile;

        Bound(Object obj, final Consumer<Object> consumer) throws ClassNotFoundException, IllegalArgumentException {
            this.tile = obj;
            this.apply = consumer;
            ClassLoader classLoader = obj.getClass().getClassLoader();
            Object objNewProxyInstance = Proxy.newProxyInstance(classLoader, new Class[]{Class.forName("com.android.systemui.plugins.qs.QSTile$Callback", false, classLoader)}, new InvocationHandler() { // from class: dev.colorcc.Panel.Bound.0
                @Override // java.lang.reflect.InvocationHandler
                public final Object invoke(Object obj2, Method method, Object[] objArr) throws Throwable {
                    return Bound.this.m94lambda$new$2$devcolorccPanel$Bound(consumer, obj2, method, objArr);
                }
            });
            this.callback = objNewProxyInstance;
            XposedHelpers.callMethod(obj, "addCallback", new Object[]{objNewProxyInstance});
            Object objCallMethod = XposedHelpers.callMethod(obj, "getState", new Object[0]);
            if (objCallMethod != null) {
                consumer.accept(objCallMethod);
            }
        }

        /* renamed from: lambda$new$2$dev-colorcc-Panel$Bound, reason: not valid java name */
        /* synthetic */ Object m94lambda$new$2$devcolorccPanel$Bound(final Consumer consumer, Object obj, Method method, final Object[] objArr) throws Throwable {
            String name = method.getName();
            name.hashCode();
            switch (name) {
                case "toString":
                    return "ColorCC.Callback";
                case "onStateChanged":
                    Panel.this.main.post(new Runnable() { // from class: dev.colorcc.Panel.Bound.2
                        @Override // java.lang.Runnable
                        public final void run() {
                            Bound.this.m93lambda$new$1$devcolorccPanel$Bound(consumer, objArr);
                        }
                    });
                    return null;
                case "equals":
                    return Boolean.valueOf(obj == objArr[0]);
                case "hashCode":
                    return Integer.valueOf(System.identityHashCode(obj));
                default:
                    return null;
            }
        }

        static /* synthetic */ void lambda$new$0(Consumer consumer, Object[] objArr) {
            consumer.accept(objArr[0]);
        }

        /* renamed from: lambda$new$1$dev-colorcc-Panel$Bound, reason: not valid java name */
        /* synthetic */ void m93lambda$new$1$devcolorccPanel$Bound(final Consumer consumer, final Object[] objArr) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Bound.1
                @Override // java.lang.Runnable
                public final void run() {
                    Bound.lambda$new$0(consumer, objArr);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean syncTiles() {
        Collection collection = (Collection) XposedHelpers.callMethod(Hook.host, "getTiles", new Object[0]);
        ArrayList arrayList = new ArrayList();
        for (Object obj : collection) {
            if (((Boolean) XposedHelpers.callMethod(obj, "isAvailable", new Object[0])).booleanValue()) {
                arrayList.add(obj);
            }
        }
        boolean zLandscape = landscape();
        int iMin = this.tileRows;
        if (zLandscape) {
            iMin = Math.min(iMin, Logic.gridRows(Math.round(getResources().getDisplayMetrics().heightPixels / this.density), 36));
        }
        if (arrayList.equals(this.currentTiles) && iMin == this.pageRows) {
            return false;
        }
        unbindTiles();
        this.currentTiles = arrayList;
        this.pageRows = iMin;
        this.wifiCard.setVisibility(4);
        this.bluetoothCard.setVisibility(4);
        this.simCard.setVisibility(4);
        ArrayList arrayList2 = new ArrayList();
        this.pinViews.clear();
        for (Object obj2 : arrayList) {
            String str = (String) XposedHelpers.callMethod(obj2, "getTileSpec", new Object[0]);
            int iKind = Logic.kind(str);
            if (iKind == 1) {
                this.wifiCard.bind(obj2);
            } else if (iKind == 2) {
                this.bluetoothCard.bind(obj2);
            } else if (iKind == 3) {
                this.simCard.bind(obj2);
            } else if (this.pins.containsKey(str)) {
                this.pinViews.put(str, pinView(obj2));
            } else {
                arrayList2.add(obj2);
            }
        }
        int iMin2 = Math.min(iMin, Logic.pages(arrayList2.size(), 4));
        this.gridRowCount = iMin2;
        this.pager.setTiles(arrayList2, iMin2 * 4);
        arrange();
        this.blocks.arrange(this.layout);
        return true;
    }

    private View pinView(final Object obj) {
        int[] iArrPinSize = Logic.pinSize(this.pins.get((String) XposedHelpers.callMethod(obj, "getTileSpec", new Object[0])));
        if (iArrPinSize[0] > 1 || iArrPinSize[1] > 1) {
            Card card = new Card(getContext());
            Body body = new Body(card, this.tileBase);
            body.pressable();
            this.tileBodies.add(body);
            card.body = body;
            card.bind(obj);
            return card;
        }
        final Disc disc = new Disc(getContext(), dp(26.0f));
        guard(new Runnable() { // from class: dev.colorcc.Panel.109
            @Override // java.lang.Runnable
            public final void run() {
                Panel.lambda$pinView$69(disc, obj);
            }
        });
        wireTile(disc, obj);
        final Body body2 = new Body(disc, this.tileBase);
        body2.pressable();
        this.tileBodies.add(body2);
        final int[] iArr = {-1};
        bindTile(obj, new Consumer() { // from class: dev.colorcc.Panel.110
            @Override // java.util.function.Consumer
            public final void accept(Object obj2) {
                Panel.this.m66lambda$pinView$70$devcolorccPanel(iArr, disc, body2, obj2);
            }
        });
        return disc;
    }

    static /* synthetic */ void lambda$pinView$69(Disc disc, Object obj) {
        disc.style((String) XposedHelpers.callMethod(obj, "getTileSpec", new Object[0]));
    }

    /* renamed from: lambda$pinView$70$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m66lambda$pinView$70$devcolorccPanel(int[] iArr, Disc disc, Body body, Object obj) {
        int iStateOf = stateOf(obj);
        int i = iArr[0];
        boolean z = (i == -1 || i == iStateOf || !this.interactive) ? false : true;
        iArr[0] = iStateOf;
        disc.render(obj, z);
        if (z && !disc.ownAnimation) {
            body.press.kick(7.0f);
        }
        disc.setContentDescription(text(obj, "label"));
        disc.setStateDescription(word(iStateOf == 2 ? "on" : "off", new Object[0]));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unbindTiles() {
        for (final Bound bound : this.bound) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.20
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.lambda$unbindTiles$71(bound);
                }
            });
            if (this.open) {
                guard(new Runnable() { // from class: dev.colorcc.Panel.21
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.this.m79lambda$unbindTiles$72$devcolorccPanel(bound);
                    }
                });
            }
        }
        this.bound.clear();
        this.tileBodies.clear();
        this.lateIcons.clear();
    }

    static /* synthetic */ void lambda$unbindTiles$71(Bound bound) {
        XposedHelpers.callMethod(bound.tile, "removeCallback", new Object[]{bound.callback});
    }

    /* renamed from: lambda$unbindTiles$72$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m79lambda$unbindTiles$72$devcolorccPanel(Bound bound) {
        XposedHelpers.callMethod(bound.tile, "setListening", new Object[]{this, false});
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void bindTile(Object obj, Consumer<Object> consumer) {
        try {
            this.bound.add(new Bound(obj, consumer));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    static /* synthetic */ void lambda$wireTile$73(Object obj) {
        XposedHelpers.callMethod(obj, "click", new Object[]{null});
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void wireTile(View view, final Object obj) {
        view.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.45
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                Panel.this.m82lambda$wireTile$74$devcolorccPanel(obj, view2);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() { // from class: dev.colorcc.Panel.46
            @Override // android.view.View.OnLongClickListener
            public final boolean onLongClick(View view2) {
                return Panel.this.m85lambda$wireTile$77$devcolorccPanel(obj, view2);
            }
        });
    }

    /* renamed from: lambda$wireTile$74$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m82lambda$wireTile$74$devcolorccPanel(final Object obj, View view) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.64
            @Override // java.lang.Runnable
            public final void run() {
                Panel.lambda$wireTile$73(obj);
            }
        });
    }

    /* renamed from: lambda$wireTile$77$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ boolean m85lambda$wireTile$77$devcolorccPanel(final Object obj, final View view) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.86
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m84lambda$wireTile$76$devcolorccPanel(obj, view);
            }
        });
        return true;
    }

    /* renamed from: lambda$wireTile$76$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m84lambda$wireTile$76$devcolorccPanel(final Object obj, final View view) {
        if (Logic.menu(String.valueOf(XposedHelpers.callMethod(obj, "getTileSpec", new Object[0]))) != 0) {
            swell(view, new Runnable() { // from class: dev.colorcc.Panel.61
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m83lambda$wireTile$75$devcolorccPanel(obj, view);
                }
            });
            return;
        }
        Object objCallMethod = XposedHelpers.callMethod(obj, "getLongClickIntent", new Object[0]);
        if (objCallMethod instanceof Intent) {
            startSettings(new Intent((Intent) objCallMethod));
        }
    }

    /* renamed from: lambda$wireTile$75$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m83lambda$wireTile$75$devcolorccPanel(Object obj, View view) {
        this.sheet.open(obj, view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int stateOf(Object obj) {
        return XposedHelpers.getIntField(obj, "state");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static CharSequence text(Object obj, String str) {
        return (CharSequence) XposedHelpers.getObjectField(obj, str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Object iconSource(Object obj) {
        Object obj2;
        String[] strArr = {"mAnimatedResId", "mResId", "mId"};
        for (int i = 0; i < 3; i++) {
            try {
                Field fieldFindFieldIfExists = XposedHelpers.findFieldIfExists(obj.getClass(), strArr[i]);
                if (fieldFindFieldIfExists != null) {
                    return obj.getClass().getName() + "#" + fieldFindFieldIfExists.getInt(obj);
                }
            } catch (Throwable unused) {
            }
        }
        Field fieldFindFieldIfExists2 = XposedHelpers.findFieldIfExists(obj.getClass(), "mDrawable");
        try {
            return (fieldFindFieldIfExists2 == null || (obj2 = fieldFindFieldIfExists2.get(obj)) == null) ? obj : obj2;
        } catch (IllegalAccessException e) {
            return obj;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Drawable stillDrawable(Object obj) {
        int i;
        if (XposedHelpers.findFieldIfExists(obj.getClass(), "mAnimatedResId") == null) {
            return null;
        }
        Field fieldFindFieldIfExists = XposedHelpers.findFieldIfExists(obj.getClass(), "mResId");
        if (fieldFindFieldIfExists == null) {
            i = 0;
        } else {
            try {
                i = fieldFindFieldIfExists.getInt(obj);
            } catch (Throwable unused) {
                return null;
            }
        }
        if (i == 0) {
            return null;
        }
        return getContext().getDrawable(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Object iconOf(Object obj) {
        Object objectField = XposedHelpers.getObjectField(obj, "iconSupplier");
        Object obj2 = objectField instanceof Supplier ? ((Supplier) objectField).get() : null;
        return obj2 != null ? obj2 : XposedHelpers.getObjectField(obj, "icon");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void writeDarkMode(boolean z) {
        try {
            UiModeManager uiModeManager = (UiModeManager) getContext().getSystemService(UiModeManager.class);
            if (((Boolean) XposedHelpers.callMethod(uiModeManager, "setNightModeActivated", new Object[]{Boolean.valueOf(z)})).booleanValue()) {
                return;
            }
            uiModeManager.setNightMode(z ? 2 : 1);
        } catch (Throwable th) {
            Hook.log("dark mode not set: " + th);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean adaptiveBrightness() {
        try {
            return ((Integer) XposedHelpers.callStaticMethod(Settings.System.class, "getIntForUser", new Object[]{getContext().getContentResolver(), "screen_brightness_mode", 0, -2})).intValue() == 1;
        } catch (Throwable th) {
            Hook.log("brightness setting not read: " + th);
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void writeAdaptiveBrightness(boolean z) {
        try {
            XposedHelpers.callStaticMethod(Settings.System.class, "putIntForUser", new Object[]{getContext().getContentResolver(), "screen_brightness_mode", Integer.valueOf(z ? 1 : 0), -2});
        } catch (Throwable th) {
            Hook.log("brightness setting not written: " + th);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void openEditor() {
        this.editor.open();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void redrawNotifications() {
        View viewNoteStack = noteStack();
        if (viewNoteStack != null) {
            invalidateAll(viewNoteStack);
        }
    }

    private View noteStack() {
        int identifier = getResources().getIdentifier("notification_stack_scroller", "id", "com.android.systemui");
        if (identifier == 0) {
            return null;
        }
        return getRootView().findViewById(identifier);
    }

    private static void invalidateAll(View view) {
        view.invalidate();
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                invalidateAll(viewGroup.getChildAt(i));
            }
        }
    }

    boolean windowBlurUsable() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(WindowManager.class);
        return (windowManager == null || !windowManager.isCrossWindowBlurEnabled() || Hook.windowBlurBlocked()) ? false : true;
    }

    void rememberSamsungLive() {
        if (Hook.samsungLiveSeen) {
            return;
        }
        Hook.samsungLiveSeen = true;
        guard(new Runnable() { // from class: dev.colorcc.Panel.31
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m75lambda$rememberSamsungLive$78$devcolorccPanel();
            }
        });
    }

    /* renamed from: lambda$rememberSamsungLive$78$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m75lambda$rememberSamsungLive$78$devcolorccPanel() {
        prefs().edit().putBoolean("samsungLive", true).apply();
    }

    String word(String str, Object... objArr) {
        try {
            if (this.words == null) {
                this.words = getContext().createPackageContext("dev.colorcc", 0).getResources();
            }
            int identifier = this.words.getIdentifier(str, "string", "dev.colorcc");
            if (identifier != 0) {
                int length = objArr.length;
                Resources resources = this.words;
                return length == 0 ? resources.getString(identifier) : resources.getString(identifier, objArr);
            }
        } catch (Throwable th) {
            if (!this.wordsMissing) {
                this.wordsMissing = true;
                Hook.log("words not read, names shown: " + th);
            }
        }
        return str;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void samsungOutput(String str) {
        Intent intentPutExtra = new Intent("com.android.systemui.action.LAUNCH_MEDIA_OUTPUT_DIALOG").setPackage("com.android.systemui").putExtra("package_name", str);
        if (str != null && !getContext().getPackageManager().queryBroadcastReceivers(intentPutExtra, 0).isEmpty()) {
            getContext().sendBroadcast(intentPutExtra);
        } else {
            openSettings("android.settings.SOUND_SETTINGS");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void openSettings(String str) {
        startSettings(new Intent(str));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startSettings(Intent intent) {
        intent.addFlags(268435456);
        Iterator<Bound> it = this.bound.iterator();
        while (it.hasNext()) {
            try {
                XposedHelpers.callMethod(XposedHelpers.getObjectField(it.next().tile, "mActivityStarter"), "postStartActivityDismissingKeyguard", new Object[]{intent, 0});
                return;
            } catch (Throwable unused) {
            }
        }
        if (this.keyguard.isKeyguardLocked()) {
            return;
        }
        getContext().startActivity(intent);
        XposedHelpers.callMethod(getContext().getSystemService("statusbar"), "collapsePanels", new Object[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void openMediaApp() {
        MediaController mediaController = this.controller;
        if (mediaController == null) {
            return;
        }
        PendingIntent sessionActivity = mediaController.getSessionActivity();
        if (sessionActivity != null) {
            Iterator<Bound> it = this.bound.iterator();
            while (it.hasNext()) {
                try {
                    XposedHelpers.callMethod(XposedHelpers.getObjectField(it.next().tile, "mActivityStarter"), "postStartActivityDismissingKeyguard", new Object[]{sessionActivity});
                    return;
                } catch (Throwable unused) {
                }
            }
            try {
                sessionActivity.send();
                XposedHelpers.callMethod(getContext().getSystemService("statusbar"), "collapsePanels", new Object[0]);
                return;
            } catch (Throwable th) {
                Hook.log("playing app's screen not opened: " + th);
            }
        }
        Intent launchIntentForPackage = getContext().getPackageManager().getLaunchIntentForPackage(mediaController.getPackageName());
        if (launchIntentForPackage != null) {
            startSettings(launchIntentForPackage);
        } else {
            Hook.log("playing app not opened: no screen to open");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBrightness(float f, boolean z) {
        if (z) {
            this.peekFrom = -1.0f;
            this.peek.to(0.0f);
        } else {
            if (this.peekFrom < 0.0f) {
                this.peekFrom = f;
            }
            if (Math.abs(f - this.peekFrom) > 0.02f) {
                this.peek.to(1.0f);
            }
        }
        applyBrightness(f, z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyBrightness(final float f, final boolean z) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.78
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m3lambda$applyBrightness$79$devcolorccPanel(z, f);
            }
        });
    }

    /* renamed from: lambda$applyBrightness$79$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m3lambda$applyBrightness$79$devcolorccPanel(boolean z, float f) {
        XposedHelpers.callMethod(this.display, z ? "setBrightness" : "setTemporaryBrightness", new Object[]{0, Float.valueOf(Logic.toLinear(f, this.brightMin, this.brightMax))});
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onVolume(float f, boolean z) {
        setVolume(this.volumeStream, f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setVolume(final int i, float f) {
        if (this.volumeWanted.put(Integer.valueOf(i), Float.valueOf(f)) != null) {
            return;
        }
        this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.34
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m76lambda$setVolume$80$devcolorccPanel(i);
            }
        });
    }

    /* renamed from: lambda$setVolume$80$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m76lambda$setVolume$80$devcolorccPanel(int i) {
        try {
            int iRound = Math.round(this.volumeWanted.remove(Integer.valueOf(i)).floatValue() * this.audio.getStreamMaxVolume(i));
            if (iRound == this.audio.getStreamVolume(i)) {
                return;
            }
            AudioManager audioManager = this.audio;
            int i2 = 2;
            if (i != 2) {
                i2 = 0;
            }
            audioManager.setStreamVolume(i, iRound, i2);
        } catch (Throwable th) {
            Hook.log("volume not set: " + th);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRingerTap(int i) {
        final int i2 = i == 0 ? 2 : i == 1 ? 1 : 0;
        showSlot(i, true);
        showStream(2);
        this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.62
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m60lambda$onRingerTap$82$devcolorccPanel(i2);
            }
        });
    }

    /* renamed from: lambda$onRingerTap$82$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m60lambda$onRingerTap$82$devcolorccPanel(int i) {
        try {
            XposedHelpers.callMethod(this.audio, "setRingerModeInternal", new Object[]{Integer.valueOf(i)});
        } catch (Throwable th) {
            Hook.log("ringer not set: " + th);
        }
        this.main.post(new Runnable() { // from class: dev.colorcc.Panel.36
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m59lambda$onRingerTap$81$devcolorccPanel();
            }
        });
    }

    /* renamed from: lambda$onRingerTap$81$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m59lambda$onRingerTap$81$devcolorccPanel() {
        guard(new Runnable() { // from class: dev.colorcc.Panel.28
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.refreshRing();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showStream(int i) {
        if (i == this.volumeStream) {
            return;
        }
        this.volumeStream = i;
        this.volume.setContentDescription(word(i == 3 ? "media_volume" : "ringtone_volume", new Object[0]));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshRing() {
        if (this.ringRefreshQueued) {
            return;
        }
        this.ringRefreshQueued = true;
        this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.17
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m74lambda$refreshRing$86$devcolorccPanel();
            }
        });
    }

    /* renamed from: lambda$refreshRing$86$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m74lambda$refreshRing$86$devcolorccPanel() {
        try {
            final int iIntValue = ((Integer) XposedHelpers.callMethod(this.audio, "getRingerModeInternal", new Object[0])).intValue();
            final int streamVolume = this.audio.getStreamVolume(this.volumeStream);
            final int streamMaxVolume = this.audio.getStreamMaxVolume(this.volumeStream);
            this.main.post(new Runnable() { // from class: dev.colorcc.Panel.83
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m72lambda$refreshRing$84$devcolorccPanel(iIntValue, streamVolume, streamMaxVolume);
                }
            });
        } catch (Throwable th) {
            this.main.post(new Runnable() { // from class: dev.colorcc.Panel.84
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m73lambda$refreshRing$85$devcolorccPanel();
                }
            });
            Hook.log("ringer not read: " + th);
        }
    }

    /* renamed from: lambda$refreshRing$84$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m72lambda$refreshRing$84$devcolorccPanel(final int i, final int i2, final int i3) {
        this.ringRefreshQueued = false;
        guard(new Runnable() { // from class: dev.colorcc.Panel.111
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m71lambda$refreshRing$83$devcolorccPanel(i, i2, i3);
            }
        });
    }

    /* renamed from: lambda$refreshRing$83$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m71lambda$refreshRing$83$devcolorccPanel(int i, int i2, int i3) {
        showRing(i, i2, i3, true);
    }

    /* renamed from: lambda$refreshRing$85$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m73lambda$refreshRing$85$devcolorccPanel() {
        this.ringRefreshQueued = false;
    }

    private void showRing(int i, int i2, int i3, boolean z) {
        this.volume.setValue(i3 == 0 ? 0.0f : i2 / i3);
        showSlot(Logic.thumb(i), z);
        this.sheet.showRing(i3 != 0 ? i2 / i3 : 0.0f, i == 0, z);
    }

    private void showSlot(int i, boolean z) {
        if (i == this.shownRingerSlot) {
            return;
        }
        this.shownRingerSlot = i;
        this.ringer.select(i, z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: syncMedia, reason: merged with bridge method [inline-methods] */
    public void m14lambda$new$1$devcolorccPanel(List<MediaController> list) {
        MediaController next;
        Iterator<MediaController> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                next = null;
                break;
            }
            next = it.next();
            PlaybackState playbackState = next.getPlaybackState();
            if (playbackState != null && playbackState.getState() == 3) {
                break;
            }
        }
        if (next == null && !list.isEmpty()) {
            next = list.get(0);
        }
        MediaController mediaController = this.controller;
        if (next != null ? mediaController == null || !next.getSessionToken().equals(this.controller.getSessionToken()) : mediaController != null) {
            MediaController mediaController2 = this.controller;
            if (mediaController2 != null) {
                mediaController2.unregisterCallback(this.mediaCallback);
            }
            this.controller = next;
            if (next != null) {
                next.registerCallback(this.mediaCallback, this.main);
            }
        }
        renderMedia();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void renderMedia() {
        Bitmap bitmap;
        MediaController mediaController = this.controller;
        MediaMetadata metadata = mediaController == null ? null : mediaController.getMetadata();
        CharSequence text = metadata == null ? null : metadata.getText("android.media.metadata.TITLE");
        PlaybackState playbackState = mediaController == null ? null : mediaController.getPlaybackState();
        if (metadata != null) {
            Bitmap bitmap2 = metadata.getBitmap("android.media.metadata.ALBUM_ART");
            if (bitmap2 == null) {
                bitmap2 = metadata.getBitmap("android.media.metadata.DISPLAY_ICON");
            }
            bitmap = bitmap2;
        } else {
            bitmap = null;
        }
        if (bitmap != this.tonedArt) {
            this.tonedArt = bitmap;
            this.mediaTones = bitmap == null ? null : artTones(bitmap);
        }
        CharSequence text2 = metadata != null ? metadata.getText("android.media.metadata.ARTIST") : null;
        boolean z = playbackState != null && playbackState.getState() == 3;
        this.media.render(text, text2, bitmap, z, mediaController != null);
        Player player = this.player;
        if (player != null) {
            if (mediaController == null) {
                player.close();
            } else {
                player.render(text, text2, bitmap, z);
            }
        }
    }

    boolean isOverControl(float f, float f2) {
        if (!this.interactive) {
            return false;
        }
        for (View view : this.touchControls) {
            if (view.isShown() && contains(view, f, f2)) {
                return true;
            }
        }
        return false;
    }

    /*
     * [Miku-Picked UI] Only answered One UI's SecQSPanelComposeAdapter.isInNonCollapsibleArea;
     * AOSP has no such query, so there is nothing to say.
     */
    Boolean collapsibleAt(float f, float f2) {
        return null;
    }

    boolean replacesStockGrid() {
        if (this.dead) {
            return false;
        }
        return !large() || this.foldable;
    }

    private static boolean contains(View view, float f, float f2) {
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        if (f >= iArr[0] && f <= iArr[0] + view.getWidth()) {
            if (f2 >= iArr[1] && f2 <= iArr[1] + view.getHeight()) {
                return true;
            }
        }
        return false;
    }

    private Object flowValue(Object obj, String str) throws NoSuchMethodException, SecurityException {
        if (obj != this.flowOwner) {
            this.flowOwner = obj;
            this.flows.clear();
        }
        Object objectField = this.flows.get(str);
        if (objectField == null) {
            objectField = XposedHelpers.getObjectField(obj, str);
            this.flows.put(str, objectField);
        }
        try {
            if (this.flowValue == null) {
                Class[] clsArr = new Class[0];
                Method method = objectField.getClass().getMethod("getValue", null);
                this.flowValue = method;
                method.setAccessible(true);
            }
            return this.flowValue.invoke(objectField, null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private float flowNumber(Object obj, String str) throws NoSuchMethodException, SecurityException {
        Object objFlowValue = flowValue(obj, str);
        if (objFlowValue instanceof Number) {
            return ((Number) objFlowValue).floatValue();
        }
        return 0.0f;
    }

    private boolean flowTrue(Object obj, String str) {
        try {
            return Boolean.TRUE.equals(flowValue(obj, str));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void guard(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable th) {
            Hook.log("action failed: " + th);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int dp(float f) {
        return Math.round(f * this.density);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static LinearLayout horizontal(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(0);
        linearLayout.setClipChildren(false);
        return linearLayout;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static FrameLayout host(Context context, View view) {
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setClipChildren(false);
        frameLayout.addView(view, new FrameLayout.LayoutParams(-1, -1));
        return frameLayout;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void setTextIfChanged(TextView textView, CharSequence charSequence) {
        if (charSequence == null) {
            charSequence = "";
        }
        if (TextUtils.equals(textView.getText(), charSequence)) {
            return;
        }
        textView.setText(charSequence);
    }

    private static void fitWidest(TextView textView, String... strArr) {
        Calendar calendar = Calendar.getInstance();
        int length = strArr.length;
        float f = 0.0f;
        int i = 0;
        while (i < length) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strArr[i], Locale.getDefault());
            float fMax = f;
            for (int i2 = 0; i2 < 288; i2++) {
                calendar.set(2026, i2 % 12, (i2 % 7) + 22, i2 % 24, 58);
                fMax = Math.max(fMax, textView.getPaint().measureText(simpleDateFormat.format(calendar.getTime())));
            }
            i++;
            f = fMax;
        }
        textView.setMinWidth(((int) Math.ceil(f)) + textView.getPaddingLeft() + textView.getPaddingRight());
    }

    private TextClock clockText(Context context, float f, int i, String str, String str2) {
        TextClock textClock = new TextClock(context);
        textClock.setFormat12Hour(str);
        textClock.setFormat24Hour(str2);
        textClock.setTextSize(0, f * this.density * getResources().getConfiguration().fontScale);
        textClock.setTextColor(i);
        textClock.setIncludeFontPadding(false);
        textClock.setSingleLine(true);
        return textClock;
    }

    private void scrollCards(float f) {
        int iRound = Math.round(Math.max(0.0f, Math.min(this.scrollMax, f)));
        if (this.column.getScrollY() == iRound) {
            return;
        }
        this.column.scrollTo(0, iRound);
        swapStatusRow();
        if (Glass.bending()) {
            reglaze(this.column);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public TextView label(Context context, float f, int i, int i2) {
        FitText fitText = new FitText(context);
        fitText.setTextSize(0, f * this.density * getResources().getConfiguration().fontScale);
        fitText.setTextColor(i);
        fitText.setMaxLines(i2);
        fitText.setEllipsize(TextUtils.TruncateAt.END);
        return fitText;
    }

    private static final class FitText extends TextView {
        private static final float SMALLEST = 0.78f;
        private float largest;
        private final Paint probe;

        FitText(Context context) {
            super(context);
            this.probe = new Paint();
        }

        @Override // android.widget.TextView
        public void setTextSize(int i, float f) {
            super.setTextSize(i, f);
            this.largest = getTextSize();
        }

        @Override // android.widget.TextView, android.view.View
        protected void onMeasure(int i, int i2) {
            int size = (View.MeasureSpec.getSize(i) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
            if (View.MeasureSpec.getMode(i) != 0 && size > 0) {
                float fMax = 0.0f;
                if (this.largest > 0.0f) {
                    this.probe.set(getPaint());
                    this.probe.setTextSize(this.largest);
                    String string = getText().toString();
                    for (String str : getMaxLines() == 1 ? new String[]{string} : string.split("\\s+")) {
                        fMax = Math.max(fMax, this.probe.measureText(str));
                    }
                    float f = size;
                    float fMax2 = this.largest;
                    if (fMax > f) {
                        fMax2 = Math.max(0.78f * fMax2, (fMax2 * f) / fMax);
                    }
                    if (fMax2 != getTextSize()) {
                        super.setTextSize(0, fMax2);
                    }
                }
            }
            super.onMeasure(i, i2);
        }
    }

    private static final class Cropped extends Drawable implements Drawable.Callback {
        private final int[] box;
        private final Drawable inner;
        private final int size;
        private int tint = -1;

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        Cropped(Drawable drawable, int[] iArr, int i) {
            this.inner = drawable;
            this.box = iArr;
            this.size = i;
            drawable.setBounds(0, 0, i, i);
            drawable.setCallback(this);
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            int[] iArr = this.box;
            return iArr[2] - iArr[0];
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            int[] iArr = this.box;
            return iArr[3] - iArr[1];
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            float fWidth = bounds.width() / getIntrinsicWidth();
            float fHeight = bounds.height() / getIntrinsicHeight();
            float f = bounds.left - (this.box[0] * fWidth);
            float f2 = bounds.top - (this.box[1] * fHeight);
            int i = this.size;
            int iSaveLayer = canvas.saveLayer(f, f2, f + (i * fWidth), f2 + (i * fHeight), null);
            int iSave = canvas.save();
            canvas.translate(bounds.left, bounds.top);
            canvas.scale(fWidth, fHeight);
            int[] iArr = this.box;
            canvas.translate(-iArr[0], -iArr[1]);
            this.inner.draw(canvas);
            canvas.restoreToCount(iSave);
            canvas.drawColor(this.tint, PorterDuff.Mode.SRC_IN);
            canvas.restoreToCount(iSaveLayer);
        }

        @Override // android.graphics.drawable.Drawable
        public void setTintList(ColorStateList colorStateList) {
            this.tint = colorStateList == null ? -1 : colorStateList.getDefaultColor();
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
            this.inner.setAlpha(i);
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            this.inner.setColorFilter(colorFilter);
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

    private final class Disc extends FrameLayout {
        int accent;
        int effect;
        final Glass glass;
        final ImageView icon;
        private final int iconSize;
        private final Runnable land;
        private Object lastIcon;
        private int lastTint;
        private Object lateIcon;
        final Motion.Value level;
        boolean ownAnimation;
        private Object[] pendingStill;
        private Runnable settleIcon;
        private int shownState;
        private Drawable stillShown;

        Disc(Context context, int i) {
            super(context);
            this.effect = 0;
            this.accent = -15374872;
            this.shownState = -1;
            this.land = new Runnable() { // from class: dev.colorcc.Panel.Disc.1
                @Override // java.lang.Runnable
                public final void run() {
                    Disc.this.land();
                }
            };
            this.iconSize = i;
            Glass glass = new Glass(-1.0f, 452984831, -83886081, 0.5f, 0.5f);
            this.glass = glass;
            setBackground(glass);
            setClickable(true);
            setClipChildren(false);
            ImageView imageView = new ImageView(context);
            this.icon = imageView;
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            addView(imageView, new FrameLayout.LayoutParams(i, i, 17));
            this.level = Panel.this.motion.value(Logic.Spring.FLOOD, new Motion.Listener() { // from class: dev.colorcc.Panel.Disc.2
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Disc.this.m100lambda$new$0$devcolorccPanel$Disc(f, f2);
                }
            });
        }

        /* renamed from: lambda$new$0$dev-colorcc-Panel$Disc, reason: not valid java name */
        /* synthetic */ void m100lambda$new$0$devcolorccPanel$Disc(float f, float f2) {
            this.glass.setLevel(f);
            tint(f);
        }

        void style(String str) {
            this.effect = Logic.effect(str);
            this.accent = Panel.this.systemIcons ? getContext().getColor(17170495) : Logic.accent(str, -15374872);
            this.lastTint = 0;
            tint(this.level.value());
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void land() {
            from(View.TRANSLATION_X, Logic.Spring.BOUNCY, -this.iconSize, 0.0f);
            from(View.TRANSLATION_Y, Logic.Spring.BOUNCY, this.iconSize, 0.0f);
            Panel.this.spring(this.icon, View.ALPHA, Logic.Spring.SNAPPY).to(1.0f);
        }

        private void play(boolean z) {
            removeCallbacks(this.land);
            this.icon.setPivotX(this.icon.getWidth() / 2.0f);
            this.icon.setPivotY(this.icon.getHeight() / 2.0f);
            float f = this.iconSize;
            switch (this.effect) {
                case 1:
                    from(View.ALPHA, Logic.Spring.WOBBLE, 0.25f, 1.0f);
                    break;
                case 2:
                    Panel.this.spring(this.icon, View.TRANSLATION_X, Logic.Spring.SNAPPY).to(f);
                    Panel.this.spring(this.icon, View.TRANSLATION_Y, Logic.Spring.SNAPPY).to(-f);
                    Panel.this.spring(this.icon, View.ALPHA, Logic.Spring.SNAPPY).to(0.0f);
                    postDelayed(this.land, 150L);
                    break;
                case 3:
                    if (z) {
                        from(View.ROTATION, Logic.Spring.WOBBLE, -90.0f, 0.0f);
                        break;
                    } else {
                        push(View.ROTATION, Logic.Spring.WOBBLE, 700.0f);
                        break;
                    }
                case 4:
                    from(View.TRANSLATION_Y, Logic.Spring.WOBBLE, f * (-0.45f), 0.0f);
                    break;
                case 5:
                    push(View.ROTATION, Logic.Spring.SWING, 675.0f);
                    break;
                case 6:
                    push(View.SCALE_X, Logic.Spring.BOUNCY, 10.0f);
                    push(View.SCALE_Y, Logic.Spring.BOUNCY, 10.0f);
                    break;
                case 7:
                    this.icon.setPivotY(this.icon.getHeight());
                    from(View.SCALE_X, Logic.Spring.BOUNCY, 0.4f, 1.0f);
                    from(View.SCALE_Y, Logic.Spring.BOUNCY, 0.4f, 1.0f);
                    break;
                case 8:
                    from(View.ROTATION_Y, Logic.Spring.BOUNCY, z ? -360.0f : 360.0f, 0.0f);
                    break;
                case 9:
                    push(View.TRANSLATION_Y, Logic.Spring.WOBBLE, f * (-5.0f));
                    break;
                case 10:
                    from(View.ROTATION, Logic.Spring.BOUNCY, z ? -360.0f : 360.0f, 0.0f);
                    break;
                case 11:
                    this.icon.setPivotX(0.0f);
                    from(View.SCALE_X, Logic.Spring.BOUNCY, 0.3f, 1.0f);
                    break;
                case 12:
                    push(View.SCALE_X, Logic.Spring.BOUNCY, -11.0f);
                    push(View.SCALE_Y, Logic.Spring.BOUNCY, -11.0f);
                    break;
                case 13:
                    push(View.TRANSLATION_X, Logic.Spring.JITTER, f * 4.5f);
                    break;
                case 14:
                    push(View.TRANSLATION_Y, Logic.Spring.BOUNCY, f * (-12.0f));
                    break;
                default:
                    from(View.SCALE_X, Logic.Spring.BOUNCY, 0.6f, 1.0f);
                    from(View.SCALE_Y, Logic.Spring.BOUNCY, 0.6f, 1.0f);
                    break;
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        private boolean animateIcon(Object obj) {
            long jRound;
            Drawable drawableMutate;
            try {
                Object objCallMethod = XposedHelpers.callMethod(obj, "getDrawable", new Object[]{getContext()});
                if (!(objCallMethod instanceof Animatable)) {
                    return false;
                }
                Drawable drawableMutate2 = ((Drawable) objCallMethod).mutate();
                int i = this.iconSize * 2;
                if (drawableMutate2 instanceof AnimationDrawable) {
                    AnimationDrawable animationDrawable = (AnimationDrawable) drawableMutate2;
                    animationDrawable.setOneShot(true);
                    jRound = 0;
                    for (int i2 = 0; i2 < animationDrawable.getNumberOfFrames(); i2++) {
                        jRound += animationDrawable.getDuration(i2);
                    }
                    drawableMutate = animationDrawable.getFrame(animationDrawable.getNumberOfFrames() - 1);
                } else {
                    try {
                        jRound = Math.round(((Float) XposedHelpers.callMethod(XposedHelpers.callMethod(drawableMutate2, "getComposition", new Object[0]), "getDuration", new Object[0])).floatValue());
                    } catch (Throwable unused) {
                        jRound = 800;
                    }
                    try {
                        drawableMutate = ((Drawable) XposedHelpers.callMethod(obj, "getDrawable", new Object[]{getContext()})).mutate();
                        toLastFrame(drawableMutate);
                    } catch (Throwable unused2) {
                        drawableMutate = drawableMutate2;
                    }
                }
                int[] iArrGlyphBox = glyphBox(drawableMutate, i);
                if (iArrGlyphBox == null) {
                    return false;
                }
                final Cropped cropped = new Cropped(drawableMutate2, iArrGlyphBox, i);
                this.icon.setImageDrawable(cropped);
                ((Animatable) drawableMutate2).start();
                Runnable runnable = this.settleIcon;
                if (runnable != null) {
                    removeCallbacks(runnable);
                }
                Runnable runnable2 = new Runnable() { // from class: dev.colorcc.Panel.Disc.0
                    @Override // java.lang.Runnable
                    public final void run() {
                        Disc.this.m99lambda$animateIcon$2$devcolorccPanel$Disc(cropped);
                    }
                };
                this.settleIcon = runnable2;
                postDelayed(runnable2, jRound + 50);
                return true;
            } catch (Throwable unused3) {
                return false;
            }
        }

        /* renamed from: lambda$animateIcon$2$dev-colorcc-Panel$Disc, reason: not valid java name */
        /* synthetic */ void m99lambda$animateIcon$2$devcolorccPanel$Disc(Cropped cropped) {
            this.settleIcon = null;
            if (this.icon.getDrawable() != cropped) {
                return;
            }
            final Object[] objArr = this.pendingStill;
            this.pendingStill = null;
            if (objArr != null) {
                Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Disc.4
                    @Override // java.lang.Runnable
                    public final void run() {
                        Disc.this.m98lambda$animateIcon$1$devcolorccPanel$Disc(objArr);
                    }
                });
            } else {
                this.icon.setImageDrawable(this.stillShown);
            }
        }

        /* renamed from: lambda$animateIcon$1$dev-colorcc-Panel$Disc, reason: not valid java name */
        /* synthetic */ void m98lambda$animateIcon$1$devcolorccPanel$Disc(Object[] objArr) {
            showIcon(objArr[0], objArr[1]);
        }

        /* JADX WARN: Multi-variable type inference failed */
        private void toLastFrame(Drawable drawable) {
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable frames = (AnimationDrawable) drawable;
                frames.selectDrawable(frames.getNumberOfFrames() - 1);
                return;
            }
            if (drawable instanceof Animatable) {
                try {
                    try {
                        XposedHelpers.callMethod(drawable, "setProgress", new Object[]{Float.valueOf(1.0f)});
                    } catch (Throwable unused) {
                        XposedHelpers.callMethod(drawable, "forceAnimationOnUI", new Object[0]);
                        Animatable animatable = (Animatable) drawable;
                        animatable.start();
                        animatable.stop();
                    }
                } catch (Throwable unused2) {
                    Animatable animatable2 = (Animatable) drawable;
                    animatable2.start();
                    animatable2.stop();
                }
            }
        }

        private int[] glyphBox(Drawable drawable, int i) {
            Bitmap bitmapCreateBitmap = Bitmap.createBitmap(i, i, Bitmap.Config.ARGB_8888);
            drawable.setBounds(0, 0, i, i);
            drawable.draw(new Canvas(bitmapCreateBitmap));
            Bitmap bitmapExtractAlpha = bitmapCreateBitmap.extractAlpha();
            bitmapCreateBitmap.recycle();
            byte[] bArr = new byte[bitmapExtractAlpha.getRowBytes() * bitmapExtractAlpha.getHeight()];
            bitmapExtractAlpha.copyPixelsToBuffer(ByteBuffer.wrap(bArr));
            int[] iArrAlphaBounds = Logic.alphaBounds(bArr, bitmapExtractAlpha.getRowBytes(), bitmapExtractAlpha.getWidth(), bitmapExtractAlpha.getHeight());
            bitmapExtractAlpha.recycle();
            return iArrAlphaBounds;
        }

        private void from(Property<View, Float> property, float[] fArr, float f, float f2) {
            Motion.Value valueSpring = Panel.this.spring(this.icon, property, fArr);
            valueSpring.snap(f);
            valueSpring.to(f2);
        }

        private void push(Property<View, Float> property, float[] fArr, float f) {
            Panel.this.spring(this.icon, property, fArr).kick(f);
        }

        private void tint(float f) {
            int iIntValue = ((Integer) Panel.this.argb.evaluate(Logic.clamp(f / 0.15f), -1, Integer.valueOf(this.accent))).intValue();
            if (iIntValue == this.lastTint) {
                return;
            }
            this.lastTint = iIntValue;
            this.icon.setImageTintList(ColorStateList.valueOf(iIntValue));
        }

        private Bitmap maskOf(Drawable drawable) {
            Panel.access$8608(Panel.this);
            try {
                int i = this.iconSize * 2;
                Bitmap bitmapCreateBitmap = Bitmap.createBitmap(i, i, Bitmap.Config.ARGB_8888);
                drawable.setBounds(0, 0, i, i);
                drawable.draw(new Canvas(bitmapCreateBitmap));
                Bitmap bitmapExtractAlpha = bitmapCreateBitmap.extractAlpha();
                bitmapCreateBitmap.recycle();
                byte[] bArr = new byte[bitmapExtractAlpha.getRowBytes() * bitmapExtractAlpha.getHeight()];
                bitmapExtractAlpha.copyPixelsToBuffer(ByteBuffer.wrap(bArr));
                int[] iArrAlphaBounds = Logic.alphaBounds(bArr, bitmapExtractAlpha.getRowBytes(), bitmapExtractAlpha.getWidth(), bitmapExtractAlpha.getHeight());
                if (iArrAlphaBounds == null) {
                    return bitmapExtractAlpha;
                }
                int i2 = iArrAlphaBounds[0];
                int i3 = iArrAlphaBounds[1];
                return Bitmap.createBitmap(bitmapExtractAlpha, i2, i3, iArrAlphaBounds[2] - i2, iArrAlphaBounds[3] - i3);
            } catch (Throwable th) {
                Hook.log("icon mask failed: " + th);
                return null;
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* [Miku-Picked UI] Restored the branch jadx dropped (from the bytecode). */
        public void render(Object obj, boolean z) {
            int i;
            Object obj2;
            int iStateOf = Panel.stateOf(obj);
            Object objIconOf = Panel.iconOf(obj);
            Object objIconSource = objIconOf == null ? null : Panel.iconSource(objIconOf);
            boolean z2 = (!Panel.this.interactive || this.shownState == -1 || this.effect != 13 || objIconOf == null || (obj2 = this.lastIcon) == null || objIconSource.equals(obj2)) ? false : true;
            if (objIconOf != null && !objIconSource.equals(this.lastIcon)) {
                if (!z && this.lastIcon != null && Panel.this.settleWorkPending) {
                    if (this.lateIcon == null) {
                        Panel.this.lateIcons.add(this);
                    }
                    this.lateIcon = objIconOf;
                } else {
                    this.lateIcon = null;
                    showIcon(objIconOf, objIconSource);
                }
            }
            float f = iStateOf == 2 ? 1.0f : 0.0f;
            Motion.Value value = this.level;
            if (z) {
                value.to(f);
            } else {
                value.snap(f);
            }
            tint(this.level.value());
            this.ownAnimation = false;
            if (z2 || (z && (i = this.shownState) != -1 && (iStateOf == 2) != (i == 2))) {
                boolean z3 = objIconOf != null && animateIcon(objIconOf);
                this.ownAnimation = z3;
                if (!z3) {
                    play(iStateOf == 2);
                }
            }
            this.shownState = iStateOf;
            setAlpha(iStateOf == 0 ? 0.45f : 1.0f);
        }

        void showLateIcon() {
            final Object obj = this.lateIcon;
            if (obj == null) {
                return;
            }
            this.lateIcon = null;
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Disc.3
                @Override // java.lang.Runnable
                public final void run() {
                    Disc.this.m101lambda$showLateIcon$3$devcolorccPanel$Disc(obj);
                }
            });
        }

        /* renamed from: lambda$showLateIcon$3$dev-colorcc-Panel$Disc, reason: not valid java name */
        /* synthetic */ void m101lambda$showLateIcon$3$devcolorccPanel$Disc(Object obj) {
            showIcon(obj, Panel.iconSource(obj));
        }

        private void showIcon(Object obj, Object obj2) {
            this.lastIcon = obj2;
            if (this.settleIcon == null) {
                Drawable drawableStillDrawable = Panel.this.stillDrawable(obj);
                if (drawableStillDrawable == null) {
                    drawableStillDrawable = (Drawable) XposedHelpers.callMethod(obj, "getDrawable", new Object[]{getContext()});
                }
                Drawable.ConstantState constantState = (drawableStillDrawable == null || (drawableStillDrawable instanceof Animatable) || (drawableStillDrawable instanceof DrawableContainer)) ? null : drawableStillDrawable.getConstantState();
                Bitmap bitmap = constantState != null ? (Bitmap) Panel.this.masks.get(constantState) : null;
                if (bitmap != null) {
                    this.icon.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                    this.stillShown = this.icon.getDrawable();
                    return;
                }
                if (drawableStillDrawable != null) {
                    Drawable drawableMutate = drawableStillDrawable.mutate();
                    toLastFrame(drawableMutate);
                    Bitmap bitmapMaskOf = maskOf(drawableMutate);
                    if (bitmapMaskOf != null && constantState != null) {
                        Panel.this.masks.put(constantState, bitmapMaskOf);
                    }
                    ImageView imageView = this.icon;
                    if (bitmapMaskOf != null) {
                        drawableMutate = new BitmapDrawable(getResources(), bitmapMaskOf);
                    }
                    imageView.setImageDrawable(drawableMutate);
                    this.stillShown = this.icon.getDrawable();
                    return;
                }
                return;
            }
            this.pendingStill = new Object[]{obj, obj2};
        }
    }

    private final class Card extends LinearLayout {
        private Body body;
        final Disc disc;
        final Glass glass;
        private int lastState;
        final Motion.Value level;
        final TextView sub;
        private final LinearLayout texts;
        final TextView title;

        Card(Context context) {
            super(context);
            this.lastState = -1;
            Glass glass = new Glass(-1.0f, 452984831, 587202559, 0.18f, 0.5f);
            this.glass = glass;
            setBackground(glass);
            setOrientation(0);
            setGravity(16);
            setPadding(Panel.this.dp(10.0f), 0, Panel.this.dp(15.0f), 0);
            setClickable(true);
            Disc disc = Panel.this.new Disc(context, Panel.this.dp(22.0f));
            this.disc = disc;
            disc.setClickable(false);
            addView(disc, new LinearLayout.LayoutParams(Panel.this.dp(42.0f), Panel.this.dp(42.0f)));
            LinearLayout linearLayout = new LinearLayout(context);
            this.texts = linearLayout;
            linearLayout.setOrientation(1);
            TextView textViewLabel = Panel.this.label(context, 12.5f, -1, 2);
            this.title = textViewLabel;
            TextView textViewLabel2 = Panel.this.label(context, 10.5f, -1275068417, 1);
            this.sub = textViewLabel2;
            textViewLabel.setIncludeFontPadding(false);
            textViewLabel2.setIncludeFontPadding(false);
            textViewLabel2.setVisibility(8);
            linearLayout.addView(textViewLabel);
            linearLayout.addView(textViewLabel2);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams.leftMargin = Panel.this.dp(9.0f);
            addView(linearLayout, layoutParams);
            this.level = Panel.this.motion.value(Logic.Spring.FLOOD, new Motion.Listener() { // from class: dev.colorcc.Panel.Card.2
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Card.this.m97lambda$new$0$devcolorccPanel$Card(f, f2);
                }
            });
        }

        /* renamed from: lambda$new$0$dev-colorcc-Panel$Card, reason: not valid java name */
        /* synthetic */ void m97lambda$new$0$devcolorccPanel$Card(float f, float f2) {
            this.glass.setLevel(f);
        }

        void shape(int i, int i2) {
            int iDp;
            int iDp2;
            boolean z = i == 1;
            int i3 = (z || i2 <= 1) ? 0 : 1;
            this.texts.setVisibility(z ? 8 : 0);
            setBackground(z ? null : this.glass);
            setOrientation(i3);
            setGravity(i3 != 0 ? 8388627 : 16);
            if (z) {
                iDp = 0;
            } else {
                iDp = Panel.this.dp(i3 != 0 ? 16.0f : 10.0f);
            }
            if (z) {
                iDp2 = 0;
            } else {
                iDp2 = Panel.this.dp(i3 == 0 ? 15.0f : 16.0f);
            }
            setPadding(iDp, 0, iDp2, 0);
            int iDp3 = z ? -1 : Panel.this.dp(42.0f);
            this.disc.setLayoutParams(new LinearLayout.LayoutParams(iDp3, iDp3));
            LinearLayout.LayoutParams layoutParams = i3 != 0 ? new LinearLayout.LayoutParams(-1, -2) : new LinearLayout.LayoutParams(0, -2, 1.0f);
            Panel panel = Panel.this;
            if (i3 != 0) {
                layoutParams.topMargin = panel.dp(10.0f);
            } else {
                layoutParams.leftMargin = panel.dp(9.0f);
            }
            this.texts.setLayoutParams(layoutParams);
        }

        void bind(final Object obj) {
            setVisibility(0);
            Panel.this.wireTile(this, obj);
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Card.0
                @Override // java.lang.Runnable
                public final void run() {
                    Card.this.m95lambda$bind$1$devcolorccPanel$Card(obj);
                }
            });
            this.lastState = -1;
            Panel.this.bindTile(obj, new Consumer() { // from class: dev.colorcc.Panel.Card.1
                @Override // java.util.function.Consumer
                public final void accept(Object obj2) {
                    Card.this.m96lambda$bind$2$devcolorccPanel$Card(obj2);
                }
            });
        }

        /* renamed from: lambda$bind$1$dev-colorcc-Panel$Card, reason: not valid java name */
        /* synthetic */ void m95lambda$bind$1$devcolorccPanel$Card(Object obj) {
            this.disc.style((String) XposedHelpers.callMethod(obj, "getTileSpec", new Object[0]));
        }

        /* renamed from: lambda$bind$2$dev-colorcc-Panel$Card, reason: not valid java name */
        /* synthetic */ void m96lambda$bind$2$devcolorccPanel$Card(Object obj) {
            int iStateOf = Panel.stateOf(obj);
            int i = this.lastState;
            boolean z = (i == -1 || i == iStateOf) ? false : true;
            this.lastState = iStateOf;
            Panel.setTextIfChanged(this.title, Panel.text(obj, "label"));
            Panel.setTextIfChanged(this.sub, Panel.text(obj, "secondaryLabel"));
            TextView textView = this.sub;
            textView.setVisibility(TextUtils.isEmpty(textView.getText()) ? 8 : 0);
            boolean z2 = z && Panel.this.interactive;
            this.disc.render(obj, z2);
            Motion.Value value = this.level;
            if (z2) {
                value.to(iStateOf != 2 ? 0.0f : 1.0f);
                if (this.body == null) {
                    this.body = Panel.this.bodyOf(this);
                }
                if (this.body != null && !this.disc.ownAnimation) {
                    this.body.press.kick(7.0f);
                }
            } else {
                value.snap(iStateOf != 2 ? 0.0f : 1.0f);
            }
            setContentDescription(Panel.text(obj, "label"));
            setStateDescription(Panel.this.word(iStateOf == 2 ? "on" : "off", new Object[0]));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Body bodyOf(View view) {
        for (Body body : this.staticBodies) {
            if (body.view == view) {
                return body;
            }
        }
        return null;
    }

    private final class RingerPill extends FrameLayout {
        private static final int SLOTS = 3;
        private final int[] activeColors;
        private final RingerGlyph[] cells;
        private float downX;
        private final Motion.Value position;
        private boolean sliding;
        private float step;
        private final View thumb;
        private int under;

        RingerPill(Context context) {
            super(context);
            this.cells = new RingerGlyph[3];
            this.activeColors = new int[]{-1756374, -1756374, -1756374};
            this.under = -1;
            setBackground(new Glass(-1.0f, 452984831, 452984831, 0.5f, 0.5f));
            View view = new View(context);
            this.thumb = view;
            view.setBackground(new Glass(-1.0f, -83886081, -83886081, 0.5f, 0.5f));
            addView(view);
            String[] strArr = {Panel.this.word("sound", new Object[0]), Panel.this.word("vibrate", new Object[0]), Panel.this.word("silent", new Object[0])};
            for (int i0 = 0; i0 < 3; i0++) {
                final int i = i0;
                RingerGlyph ringerGlyph = new RingerGlyph(context, Panel.this.motion, i, Panel.this.dp(21.0f));
                ringerGlyph.setContentDescription(strArr[i]);
                ringerGlyph.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.RingerPill.1
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view2) {
                        RingerPill.this.m175lambda$new$1$devcolorccPanel$RingerPill(i, view2);
                    }
                });
                this.cells[i] = ringerGlyph;
                addView(ringerGlyph, new FrameLayout.LayoutParams(0, 0));
            }
            this.position = Panel.this.motion.value(Logic.Spring.BOUNCY, new Motion.Listener() { // from class: dev.colorcc.Panel.RingerPill.2
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    RingerPill.this.m176lambda$new$2$devcolorccPanel$RingerPill(f, f2);
                }
            });
            m176lambda$new$2$devcolorccPanel$RingerPill(0.0f, 0.0f);
        }

        /* renamed from: lambda$new$0$dev-colorcc-Panel$RingerPill, reason: not valid java name */
        /* synthetic */ void m174lambda$new$0$devcolorccPanel$RingerPill(int i) {
            Panel.this.onRingerTap(i);
        }

        /* renamed from: lambda$new$1$dev-colorcc-Panel$RingerPill, reason: not valid java name */
        /* synthetic */ void m175lambda$new$1$devcolorccPanel$RingerPill(final int i, View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.RingerPill.3
                @Override // java.lang.Runnable
                public final void run() {
                    RingerPill.this.m174lambda$new$0$devcolorccPanel$RingerPill(i);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* renamed from: render, reason: merged with bridge method [inline-methods] */
        public void m176lambda$new$2$devcolorccPanel$RingerPill(float f, float f2) {
            float fMin = Math.min(Math.abs(f2) / 12.0f, 1.0f);
            this.thumb.setScaleX((fMin * 0.5f) + 1.0f);
            this.thumb.setScaleY(1.0f - (fMin * 0.22f));
            this.thumb.setTranslationX(this.step * f);
            for (int i = 0; i < 3; i++) {
                float fClamp = Logic.clamp(1.0f - Math.abs(f - i));
                this.cells[i].setColor(((Integer) Panel.this.argb.evaluate(fClamp, -1493172225, Integer.valueOf(this.activeColors[i]))).intValue());
                this.cells[i].setSelected(fClamp >= 0.5f);
            }
        }

        void select(int i, boolean z) {
            Motion.Value value = this.position;
            if (z) {
                value.to(i);
                this.cells[i].play();
            } else {
                value.snap(i);
            }
        }

        private float slotAt(float f) {
            float width = this.thumb.getWidth() / 2.0f;
            if (this.step <= 0.0f) {
                return 0.0f;
            }
            return Math.max(0.0f, Math.min(2.0f, ((f - Panel.this.dp(9.0f)) - width) / this.step));
        }

        @Override // android.view.ViewGroup
        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                this.downX = motionEvent.getX();
                this.sliding = false;
                getParent().requestDisallowInterceptTouchEvent(true);
            } else if (actionMasked == 2 && !this.sliding && Math.abs(motionEvent.getX() - this.downX) > Panel.this.touchSlop) {
                this.sliding = true;
                this.under = Panel.this.shownRingerSlot;
            }
            return this.sliding;
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                this.downX = motionEvent.getX();
                return true;
            }
            if (!this.sliding) {
                if (actionMasked != 2 || Math.abs(motionEvent.getX() - this.downX) <= Panel.this.touchSlop) {
                    return true;
                }
                this.sliding = true;
                this.under = Panel.this.shownRingerSlot;
            }
            float fSlotAt = slotAt(motionEvent.getX());
            if (actionMasked == 2) {
                this.position.to(fSlotAt);
                int iRound = Math.round(fSlotAt);
                if (iRound != this.under) {
                    this.under = iRound;
                    performHapticFeedback(4);
                }
                return true;
            }
            if (actionMasked == 1 || actionMasked == 3) {
                this.sliding = false;
                final int iRound2 = Math.round(fSlotAt);
                if (iRound2 != Panel.this.shownRingerSlot) {
                    Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.RingerPill.0
                        @Override // java.lang.Runnable
                        public final void run() {
                            RingerPill.this.m177lambda$onTouchEvent$3$devcolorccPanel$RingerPill(iRound2);
                        }
                    });
                } else {
                    this.position.to(iRound2);
                }
            }
            return true;
        }

        /* renamed from: lambda$onTouchEvent$3$dev-colorcc-Panel$RingerPill, reason: not valid java name */
        /* synthetic */ void m177lambda$onTouchEvent$3$devcolorccPanel$RingerPill(int i) {
            Panel.this.onRingerTap(i);
        }

        @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
            super.onLayout(z, i, i2, i3, i4);
            int i5 = i3 - i;
            int i6 = i4 - i2;
            int iDp = Panel.this.dp(9.0f);
            int iMax = Math.max(0, i6 - (iDp * 2));
            this.step = Math.max(0, (i5 - (iDp * 2)) - iMax) / 2.0f;
            int i7 = iDp + iMax;
            this.thumb.layout(iDp, iDp, i7, i7);
            for (int i8 = 0; i8 < 3; i8++) {
                float f = iMax;
                float f2 = this.step;
                float f3 = iDp + (f / 2.0f) + (i8 * f2);
                int iRound = Math.round(Math.max(f, f2) / 2.0f);
                this.cells[i8].layout(Math.max(0, Math.round(f3) - iRound), 0, Math.min(i5, Math.round(f3) + iRound), i6);
            }
            m176lambda$new$2$devcolorccPanel$RingerPill(this.position.value(), 0.0f);
        }
    }

    private final class MediaCard extends LinearLayout {
        private final ImageView art;
        private final TextView artist;
        private final ImageView next;
        private final Drawable noteIcon;
        private final Drawable pauseIcon;
        private final ImageView play;
        private final Drawable playIcon;
        private final Motion.Value playPop;
        private boolean playing;
        private final ImageView prev;
        private CharSequence shownTitle;
        private final ArtTint tint;
        private final TextView title;

        MediaCard(Context context) {
            super(context);
            Drawable drawableOf = Icons.of("M8,5v14l11,-7z");
            this.playIcon = drawableOf;
            this.pauseIcon = Icons.of("M6,19h4V5H6v14zM14,5v14h4V5h-4z");
            this.noteIcon = Icons.of("M12,3v10.55c-0.59,-0.34 -1.27,-0.55 -2,-0.55 -2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4V7h4V3h-6z");
            ArtTint artTint = new ArtTint(Panel.this, Panel.this.dp(34.0f));
            this.tint = artTint;
            setOrientation(1);
            Glass glass = new Glass(Panel.this.dp(34.0f), 452984831, 452984831, 0.5f, 0.5f);
            glass.setUnderlay(artTint);
            setBackground(glass);
            setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.MediaCard.0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MediaCard.this.m143lambda$new$1$devcolorccPanel$MediaCard(view);
                }
            });
            setPadding(Panel.this.dp(14.0f), Panel.this.dp(14.0f), Panel.this.dp(14.0f), Panel.this.dp(6.0f));
            ImageView imageView = new ImageView(context);
            this.art = imageView;
            imageView.setClipToOutline(true);
            imageView.setOutlineProvider(new ViewOutlineProvider() { // from class: dev.colorcc.Panel.MediaCard.1
                @Override // android.view.ViewOutlineProvider
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), Panel.this.dp(10.0f));
                }
            });
            ImageView imageView2 = new ImageView(context);
            imageView2.setImageDrawable(Icons.of("M21,3H3C1.9,3 1,3.9 1,5v3h2V5h18v14h-7v2h7c1.1,0 2,-0.9 2,-2V5C23,3.9 22.1,3 21,3zM1,18v3h3C4,19.34 2.66,18 1,18zM1,14v2c2.76,0 5,2.24 5,5h2C8,17.13 4.87,14 1,14zM1,10v2c4.97,0 9,4.03 9,9h2C12,14.92 7.07,10 1,10z"));
            imageView2.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView2.setPadding(Panel.this.dp(3.0f), Panel.this.dp(3.0f), Panel.this.dp(3.0f), Panel.this.dp(3.0f));
            imageView2.setContentDescription(Panel.this.word("sound_output", new Object[0]));
            imageView2.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.MediaCard.2
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MediaCard.this.m146lambda$new$3$devcolorccPanel$MediaCard(view);
                }
            });
            Panel.this.squish(imageView2);
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            linearLayoutHorizontal.addView(imageView, new LinearLayout.LayoutParams(Panel.this.dp(42.0f), Panel.this.dp(42.0f)));
            linearLayoutHorizontal.addView(new Space(context), new LinearLayout.LayoutParams(0, 0, 1.0f));
            linearLayoutHorizontal.addView(imageView2, new LinearLayout.LayoutParams(Panel.this.dp(28.0f), Panel.this.dp(28.0f)));
            addView(linearLayoutHorizontal, new LinearLayout.LayoutParams(-1, Panel.this.dp(42.0f)));
            addView(new Space(context), new LinearLayout.LayoutParams(0, 0, 1.0f));
            TextView textViewLabel = Panel.this.label(context, 13.0f, -1, 1);
            this.title = textViewLabel;
            TextView textViewLabel2 = Panel.this.label(context, 10.0f, -855638017, 1);
            this.artist = textViewLabel2;
            addView(textViewLabel, new LinearLayout.LayoutParams(-1, -2));
            addView(textViewLabel2, new LinearLayout.LayoutParams(-1, -2));
            LinearLayout linearLayoutHorizontal2 = Panel.horizontal(context);
            ImageView imageViewControl = control(context, Icons.of("M6,6h2v12H6zM9.5,12l8.5,6V6z"), Panel.this.word("previous", new Object[0]));
            this.prev = imageViewControl;
            ImageView imageViewControl2 = control(context, drawableOf, Panel.this.word("play", new Object[0]));
            this.play = imageViewControl2;
            ImageView imageViewControl3 = control(context, Icons.of("M6,18l8.5,-6L6,6v12zM16,6v12h2V6z"), Panel.this.word("next", new Object[0]));
            this.next = imageViewControl3;
            linearLayoutHorizontal2.addView(imageViewControl, new LinearLayout.LayoutParams(0, Panel.this.dp(36.0f), 1.0f));
            linearLayoutHorizontal2.addView(imageViewControl2, new LinearLayout.LayoutParams(0, Panel.this.dp(36.0f), 1.0f));
            linearLayoutHorizontal2.addView(imageViewControl3, new LinearLayout.LayoutParams(0, Panel.this.dp(36.0f), 1.0f));
            addView(linearLayoutHorizontal2, new LinearLayout.LayoutParams(-1, Panel.this.dp(36.0f)));
            this.playPop = Panel.this.motion.value(Logic.Spring.BOUNCY, new Motion.Listener() { // from class: dev.colorcc.Panel.MediaCard.3
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    MediaCard.this.m147lambda$new$4$devcolorccPanel$MediaCard(f, f2);
                }
            });
            imageViewControl.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.MediaCard.4
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MediaCard.this.m148lambda$new$6$devcolorccPanel$MediaCard(view);
                }
            });
            imageViewControl3.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.MediaCard.5
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MediaCard.this.m149lambda$new$8$devcolorccPanel$MediaCard(view);
                }
            });
            imageViewControl2.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.MediaCard.6
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MediaCard.this.m144lambda$new$10$devcolorccPanel$MediaCard(view);
                }
            });
        }

        /* renamed from: lambda$new$0$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m142lambda$new$0$devcolorccPanel$MediaCard() {
            Panel.this.player.open();
        }

        /* renamed from: lambda$new$1$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m143lambda$new$1$devcolorccPanel$MediaCard(View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.MediaCard.7
                @Override // java.lang.Runnable
                public final void run() {
                    MediaCard.this.m142lambda$new$0$devcolorccPanel$MediaCard();
                }
            });
        }

        /* renamed from: lambda$new$2$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m145lambda$new$2$devcolorccPanel$MediaCard(View view) {
            Panel.this.sheet.openOutput(view, Panel.this.controller == null ? null : Panel.this.controller.getPackageName());
        }

        /* renamed from: lambda$new$3$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m146lambda$new$3$devcolorccPanel$MediaCard(final View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.MediaCard.10
                @Override // java.lang.Runnable
                public final void run() {
                    MediaCard.this.m145lambda$new$2$devcolorccPanel$MediaCard(view);
                }
            });
        }

        /* renamed from: lambda$new$4$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m147lambda$new$4$devcolorccPanel$MediaCard(float f, float f2) {
            float f3 = 1.0f - (f * 0.25f);
            this.play.setScaleX(f3);
            this.play.setScaleY(f3);
        }

        /* renamed from: lambda$new$6$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m148lambda$new$6$devcolorccPanel$MediaCard(View view) {
            Panel.this.skipped(-1);
            transport(new Consumer() { // from class: dev.colorcc.Panel.MediaCard.12
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((MediaController.TransportControls) obj).skipToPrevious();
                }
            });
        }

        /* renamed from: lambda$new$8$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m149lambda$new$8$devcolorccPanel$MediaCard(View view) {
            Panel.this.skipped(1);
            transport(new Consumer() { // from class: dev.colorcc.Panel.MediaCard.8
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((MediaController.TransportControls) obj).skipToNext();
                }
            });
        }

        /* renamed from: lambda$new$10$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m144lambda$new$10$devcolorccPanel$MediaCard(View view) {
            transport(new Consumer() { // from class: dev.colorcc.Panel.MediaCard.9
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    MediaCard.this.m150lambda$new$9$devcolorccPanel$MediaCard((MediaController.TransportControls) obj);
                }
            });
        }

        /* renamed from: lambda$new$9$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m150lambda$new$9$devcolorccPanel$MediaCard(MediaController.TransportControls transportControls) {
            if (this.playing) {
                transportControls.pause();
            } else {
                transportControls.play();
            }
        }

        private ImageView control(Context context, Drawable drawable, String str) {
            ImageView imageView = new ImageView(context);
            imageView.setImageDrawable(drawable);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(Panel.this.dp(8.0f), Panel.this.dp(8.0f), Panel.this.dp(8.0f), Panel.this.dp(8.0f));
            imageView.setContentDescription(str);
            if (drawable != this.playIcon) {
                Panel.this.squish(imageView);
            }
            return imageView;
        }

        private void transport(final Consumer<MediaController.TransportControls> consumer) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.MediaCard.11
                @Override // java.lang.Runnable
                public final void run() {
                    MediaCard.this.m151lambda$transport$11$devcolorccPanel$MediaCard(consumer);
                }
            });
        }

        /* renamed from: lambda$transport$11$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m151lambda$transport$11$devcolorccPanel$MediaCard(Consumer consumer) {
            if (Panel.this.controller != null) {
                consumer.accept(Panel.this.controller.getTransportControls());
            }
        }

        void render(CharSequence charSequence, CharSequence charSequence2, Bitmap bitmap, boolean z, boolean z2) {
            if (z != this.playing) {
                this.playPop.kick(9.0f);
            }
            this.playing = z;
            CharSequence charSequenceWord = TextUtils.isEmpty(charSequence) ? Panel.this.word("not_playing", new Object[0]) : charSequence;
            boolean zEquals = TextUtils.equals(charSequenceWord, this.shownTitle);
            this.shownTitle = charSequenceWord;
            this.title.setText(charSequenceWord);
            this.title.setTextColor(TextUtils.isEmpty(charSequence) ? -1711276033 : -1);
            this.artist.setText(charSequence2);
            this.artist.setVisibility(TextUtils.isEmpty(charSequence2) ? 8 : 0);
            ImageView imageView = this.art;
            if (bitmap == null) {
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                this.art.setPadding(Panel.this.dp(10.0f), Panel.this.dp(10.0f), Panel.this.dp(10.0f), Panel.this.dp(10.0f));
                this.art.setImageDrawable(this.noteIcon);
                this.art.setImageTintList(ColorStateList.valueOf(-1711276033));
            } else {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                this.art.setPadding(0, 0, 0, 0);
                this.art.setImageTintList(null);
                this.art.setImageBitmap(bitmap);
            }
            this.tint.show(Panel.this.mediaTones);
            if (!zEquals && isShown()) {
                Panel.this.slideIn(this.title, this.artist);
                Panel.this.swellIn(this.art);
            }
            this.play.setImageDrawable(z ? this.pauseIcon : this.playIcon);
            this.play.setContentDescription(Panel.this.word(z ? "pause" : "play", new Object[0]));
            View[] viewArr = {this.prev, this.play, this.next};
            for (int i = 0; i < 3; i++) {
                View view = viewArr[i];
                view.setEnabled(z2);
                view.setAlpha(z2 ? 1.0f : 0.4f);
            }
        }
    }

    private final class ArtTint extends Drawable {
        private final RectF box;
        private final float corner;
        private final boolean even;
        private final Motion.Value fade;
        private int[] from;
        private float mix;
        private final Paint paint;
        private int[] to;

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }

        /* renamed from: lambda$new$0$dev-colorcc-Panel$ArtTint, reason: not valid java name */
        /* synthetic */ void m89lambda$new$0$devcolorccPanel$ArtTint(float f, float f2) {
            this.mix = Logic.clamp(f);
            invalidateSelf();
        }

        ArtTint(Panel panel, float f) {
            this(f, false);
        }

        ArtTint(float f, boolean z) {
            this.paint = new Paint(1);
            this.box = new RectF();
            this.from = new int[]{0, 0};
            this.to = new int[]{0, 0};
            this.mix = 1.0f;
            this.fade = Panel.this.motion.value(Logic.Spring.FLOOD, new Motion.Listener() { // from class: dev.colorcc.Panel.ArtTint.0
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f2, float f3) {
                    ArtTint.this.m89lambda$new$0$devcolorccPanel$ArtTint(f2, f3);
                }
            });
            this.corner = f;
            this.even = z;
        }

        void show(int[] iArr) {
            int[] iArr2;
            if (iArr == null) {
                iArr2 = new int[]{0, 0};
            } else {
                iArr2 = new int[]{(iArr[0] & 16777215) | (-268435456), (iArr[1] & 16777215) | (-268435456)};
            }
            if (Arrays.equals(iArr2, this.to)) {
                return;
            }
            int[] iArrCurrent = current();
            for (int i = 0; i < 2; i++) {
                if ((iArrCurrent[i] >>> 24) == 0) {
                    iArrCurrent[i] = iArr2[i] & 16777215;
                }
                if ((iArr2[i] >>> 24) == 0) {
                    iArr2[i] = iArrCurrent[i] & 16777215;
                }
            }
            this.from = iArrCurrent;
            this.to = iArr2;
            this.fade.snap(0.0f);
            this.fade.to(1.0f);
        }

        private int[] current() {
            return new int[]{((Integer) Panel.this.argb.evaluate(this.mix, Integer.valueOf(this.from[0]), Integer.valueOf(this.to[0]))).intValue(), ((Integer) Panel.this.argb.evaluate(this.mix, Integer.valueOf(this.from[1]), Integer.valueOf(this.to[1]))).intValue()};
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            int[] iArrCurrent = current();
            if ((iArrCurrent[0] >>> 24) == 0 && (iArrCurrent[1] >>> 24) == 0) {
                return;
            }
            this.box.set(getBounds());
            if (!this.even) {
                this.paint.setShader(new LinearGradient(0.0f, this.box.top, 0.0f, this.box.bottom, new int[]{faded(iArrCurrent[0], 0.0f), faded(iArrCurrent[0], 0.3f), faded(iArrCurrent[1], 0.7f), ((Integer) Panel.this.argb.evaluate(0.3f, Integer.valueOf(iArrCurrent[1]), Integer.valueOf(iArrCurrent[1] | 16777215))).intValue()}, Panel.MEDIA_TINT_STOPS, Shader.TileMode.CLAMP));
                RectF rectF = this.box;
                float f = this.corner;
                canvas.drawRoundRect(rectF, f, f, this.paint);
                return;
            }
            this.paint.setShader(new LinearGradient(0.0f, this.box.top, 0.0f, this.box.bottom, faded(iArrCurrent[0], 0.22f), faded(iArrCurrent[1], 0.22f), Shader.TileMode.CLAMP));
            RectF rectF2 = this.box;
            float f2 = this.corner;
            canvas.drawRoundRect(rectF2, f2, f2, this.paint);
        }

        private int faded(int i, float f) {
            return (Math.round((i >>> 24) * f) << 24) | (i & 16777215);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void mediaTransport(final Consumer<MediaController.TransportControls> consumer) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.35
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m12lambda$mediaTransport$87$devcolorccPanel(consumer);
            }
        });
    }

    /* renamed from: lambda$mediaTransport$87$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m12lambda$mediaTransport$87$devcolorccPanel(Consumer consumer) {
        MediaController mediaController = this.controller;
        if (mediaController != null) {
            consumer.accept(mediaController.getTransportControls());
        }
    }

    private static int[] artTones(Bitmap bitmap) {
        try {
            if (bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            }
            int[] iArr = new int[256];
            Bitmap.createScaledBitmap(bitmap, 16, 16, true).getPixels(iArr, 0, 16, 0, 0, 16, 16);
            return Logic.artTones(iArr, 16);
        } catch (Throwable th) {
            Hook.log("artwork colours not read: " + th);
            return null;
        }
    }

    private final class Player extends FrameLayout {
        private final ImageView[] actions;
        private final ImageView art;
        private final TextView artist;
        private final int[] at;
        private final Rewind back;
        private final ImageView badge;
        private String badgeFor;
        private final LinearLayout card;
        private final FrameLayout cover;
        private final TextView elapsed;
        private float fromHeight;
        private float fromScale;
        private float fromX;
        private float fromY;
        private final ImageView ghost;
        private float grown;
        private final SeekLine loudness;
        private final ImageView next;
        private final Drawable noteIcon;
        private boolean opened;
        private final Drawable pauseIcon;
        private final ImageView play;
        private final Drawable playIcon;
        private boolean playing;
        private final ImageView prev;
        private final SeekLine seek;
        private int sentVolume;
        private final Drawable shade;
        private final Motion.Value show;
        private CharSequence shownTitle;
        private final Runnable tick;
        private final ArtTint tint;
        private final TextView title;
        private final TextView total;
        private int volumeSteps;

        /* renamed from: lambda$new$0$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m156lambda$new$0$devcolorccPanel$Player() {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Player.23
                @Override // java.lang.Runnable
                public final void run() {
                    Player.this.showProgress();
                }
            });
        }

        Player(Context context) {
            super(context);
            this.sentVolume = -1;
            int i = 1;
            ArtTint artTint = Panel.this.new ArtTint(Panel.this.dp(34.0f), true);
            this.tint = artTint;
            this.playIcon = Icons.of("M8,5v14l11,-7z");
            this.pauseIcon = Icons.of("M6,19h4V5H6v14zM14,5v14h4V5h-4z");
            this.noteIcon = Icons.of("M12,3v10.55c-0.59,-0.34 -1.27,-0.55 -2,-0.55 -2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4V7h4V3h-6z");
            Drawable drawableScrim = Panel.scrim();
            this.shade = drawableScrim;
            this.tick = new Runnable() { // from class: dev.colorcc.Panel.Player.5
                @Override // java.lang.Runnable
                public final void run() {
                    Player.this.m156lambda$new$0$devcolorccPanel$Player();
                }
            };
            this.at = new int[2];
            this.fromScale = 1.0f;
            this.grown = 1.0f;
            this.actions = new ImageView[2];
            setVisibility(4);
            setBackground(drawableScrim);
            setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Player.8
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Player.this.m157lambda$new$1$devcolorccPanel$Player(view);
                }
            });
            LinearLayout linearLayout = new LinearLayout(context);
            this.card = linearLayout;
            linearLayout.setOrientation(1);
            linearLayout.setClickable(true);
            linearLayout.setPivotX(0.0f);
            linearLayout.setPivotY(0.0f);
            Glass glass = new Glass(Panel.this.dp(34.0f), 452984831, 452984831, 0.5f, 0.5f);
            glass.setUnderlay(artTint);
            linearLayout.setBackground(glass);
            linearLayout.setClipToOutline(true);
            linearLayout.setOutlineProvider(new ViewOutlineProvider() { // from class: dev.colorcc.Panel.Player.1
                @Override // android.view.ViewOutlineProvider
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), Math.max(1, Math.round(Player.this.fromHeight + ((view.getHeight() - Player.this.fromHeight) * Player.this.grown))), Panel.this.dp(34.0f) / (Player.this.fromScale + ((1.0f - Player.this.fromScale) * Player.this.grown)));
                }
            });
            FrameLayout frameLayout = new FrameLayout(context);
            this.cover = frameLayout;
            FadingImage fadingImage = new FadingImage(context);
            this.art = fadingImage;
            frameLayout.addView(fadingImage, new FrameLayout.LayoutParams(-1, -1));
            FadingImage fadingImage2 = new FadingImage(context);
            this.ghost = fadingImage2;
            fadingImage2.setAlpha(0.0f);
            frameLayout.addView(fadingImage2, new FrameLayout.LayoutParams(-1, -1));
            TextView textViewLabel = Panel.this.label(context, 22.0f, -1, 1);
            this.title = textViewLabel;
            textViewLabel.setTypeface(Typeface.DEFAULT_BOLD);
            textViewLabel.setGravity(1);
            textViewLabel.setShadowLayer(Panel.this.dp(6.0f), 0.0f, Panel.this.dp(1.0f), 1493172224);
            TextView textViewLabel2 = Panel.this.label(context, 15.0f, -637534209, 1);
            this.artist = textViewLabel2;
            textViewLabel2.setGravity(1);
            textViewLabel2.setShadowLayer(Panel.this.dp(6.0f), 0.0f, Panel.this.dp(1.0f), 1291845632);
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(1);
            linearLayout2.setPadding(Panel.this.dp(20.0f), 0, Panel.this.dp(20.0f), Panel.this.dp(4.0f));
            linearLayout2.addView(textViewLabel, new LinearLayout.LayoutParams(-1, -2));
            linearLayout2.addView(textViewLabel2, new LinearLayout.LayoutParams(-1, -2));
            frameLayout.addView(linearLayout2, new FrameLayout.LayoutParams(-1, -2, 80));
            ImageView imageView = new ImageView(context);
            this.badge = imageView;
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setOutlineProvider(new ViewOutlineProvider() { // from class: dev.colorcc.Panel.Player.2
                @Override // android.view.ViewOutlineProvider
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), Panel.this.dp(8.0f));
                }
            });
            imageView.setClipToOutline(true);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Panel.this.dp(30.0f), Panel.this.dp(30.0f), 8388661);
            layoutParams.topMargin = Panel.this.dp(16.0f);
            layoutParams.rightMargin = Panel.this.dp(16.0f);
            frameLayout.addView(imageView, layoutParams);
            frameLayout.setOutlineProvider(new ViewOutlineProvider() { // from class: dev.colorcc.Panel.Player.3
                @Override // android.view.ViewOutlineProvider
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), Panel.this.dp(24.0f));
                }
            });
            frameLayout.setClipToOutline(true);
            frameLayout.setContentDescription(Panel.this.word("open_app", new Object[0]));
            frameLayout.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Player.9
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Player.this.m166lambda$new$3$devcolorccPanel$Player(view);
                }
            });
            frameLayout.setOnTouchListener(new View.OnTouchListener() { // from class: dev.colorcc.Panel.Player.10
                @Override // android.view.View.OnTouchListener
                public final boolean onTouch(View view, MotionEvent motionEvent) {
                    return Player.this.m167lambda$new$4$devcolorccPanel$Player(view, motionEvent);
                }
            });
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setCornerRadius(Panel.this.dp(24.0f));
            gradientDrawable.setStroke(Math.max(1, Panel.this.dp(1.0f)), 1308622847);
            frameLayout.setForeground(gradientDrawable);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, Panel.this.dp(250.0f));
            int iDp = Panel.this.dp(14.0f);
            layoutParams2.topMargin = iDp;
            layoutParams2.rightMargin = iDp;
            layoutParams2.leftMargin = iDp;
            linearLayout.addView(frameLayout, layoutParams2);
            LinearLayout linearLayout3 = new LinearLayout(context);
            linearLayout3.setOrientation(1);
            linearLayout3.setPadding(Panel.this.dp(22.0f), Panel.this.dp(8.0f), Panel.this.dp(22.0f), Panel.this.dp(16.0f));
            TextView textViewLabel3 = Panel.this.label(context, 11.0f, -1711276033, 1);
            this.elapsed = textViewLabel3;
            TextView textViewLabel4 = Panel.this.label(context, 11.0f, -1711276033, 1);
            this.total = textViewLabel4;
            SeekLine seekLine = Panel.this.new SeekLine(context);
            this.seek = seekLine;
            seekLine.glow = true;
            seekLine.listener = new Slider.Listener() { // from class: dev.colorcc.Panel.Player.11
                @Override // dev.colorcc.Slider.Listener
                public final void onChange(float f, boolean z) {
                    Player.this.m169lambda$new$7$devcolorccPanel$Player(f, z);
                }
            };
            linearLayout3.addView(seekLine, new LinearLayout.LayoutParams(-1, Panel.this.dp(28.0f)));
            textViewLabel4.setGravity(8388613);
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            linearLayoutHorizontal.addView(textViewLabel3, new LinearLayout.LayoutParams(0, -2, 1.0f));
            linearLayoutHorizontal.addView(textViewLabel4, new LinearLayout.LayoutParams(0, -2, 1.0f));
            linearLayout3.addView(linearLayoutHorizontal, new LinearLayout.LayoutParams(-1, -2));
            ImageView imageViewButton = button(context, Icons.of("M6,6h2v12H6zM9.5,12l8.5,6V6z"), Panel.this.word("previous", new Object[0]), Panel.this.dp(12.0f));
            this.prev = imageViewButton;
            ImageView imageViewButton2 = button(context, Icons.of("M6,18l8.5,-6L6,6v12zM16,6v12h2V6z"), Panel.this.word("next", new Object[0]), Panel.this.dp(12.0f));
            this.next = imageViewButton2;
            ImageView[] imageViewArr = {imageViewButton, imageViewButton2};
            int i2 = 0;
            for (int i3 = 2; i2 < i3; i3 = 2) {
                imageViewArr[i2].setPadding(Panel.this.dp(2.0f), Panel.this.dp(11.0f), Panel.this.dp(2.0f), Panel.this.dp(11.0f));
                i2++;
                i = i;
            }
            int i4 = i;
            ImageView imageViewButton3 = button(context, this.playIcon, Panel.this.word("play", new Object[0]), Panel.this.dp(18.0f));
            this.play = imageViewButton3;
            imageViewButton3.setBackground(new Glass(-1.0f, 587202559, 587202559, 0.5f, 0.5f));
            int i5 = 0;
            while (true) {
                ImageView[] imageViewArr2 = this.actions;
                if (i5 < imageViewArr2.length) {
                    imageViewArr2[i5] = button(context, null, null, Panel.this.dp(13.0f));
                    this.actions[i5].setPadding(Panel.this.dp(2.0f), Panel.this.dp(12.0f), Panel.this.dp(2.0f), Panel.this.dp(12.0f));
                    this.actions[i5].setImageTintList(ColorStateList.valueOf(-419430401));
                    this.actions[i5].setVisibility(4);
                    Panel.this.squish(this.actions[i5]);
                    i5++;
                } else {
                    Panel.this.squish(this.prev);
                    Panel.this.squish(this.next);
                    Panel.this.squish(this.play);
                    LinearLayout linearLayoutHorizontal2 = Panel.horizontal(context);
                    linearLayoutHorizontal2.setGravity(16);
                    linearLayoutHorizontal2.addView(this.actions[0], new LinearLayout.LayoutParams(0, Panel.this.dp(48.0f), 1.0f));
                    linearLayoutHorizontal2.addView(this.prev, new LinearLayout.LayoutParams(0, Panel.this.dp(52.0f), 1.0f));
                    linearLayoutHorizontal2.addView(this.play, new LinearLayout.LayoutParams(Panel.this.dp(62.0f), Panel.this.dp(62.0f)));
                    linearLayoutHorizontal2.addView(this.next, new LinearLayout.LayoutParams(0, Panel.this.dp(52.0f), 1.0f));
                    linearLayoutHorizontal2.addView(this.actions[i4], new LinearLayout.LayoutParams(0, Panel.this.dp(48.0f), 1.0f));
                    LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, Panel.this.dp(66.0f));
                    layoutParams3.topMargin = Panel.this.dp(6.0f);
                    linearLayout3.addView(linearLayoutHorizontal2, layoutParams3);
                    LinearLayout linearLayoutHorizontal3 = Panel.horizontal(context);
                    linearLayoutHorizontal3.setGravity(16);
                    ImageView imageViewButton4 = button(context, Icons.of("M2,9v6h4l5,5V4L6,9H2z"), null, 0);
                    imageViewButton4.setImageTintList(ColorStateList.valueOf(-1711276033));
                    linearLayoutHorizontal3.addView(imageViewButton4, new LinearLayout.LayoutParams(Panel.this.dp(18.0f), Panel.this.dp(18.0f)));
                    SeekLine seekLine2 = Panel.this.new SeekLine(context);
                    this.loudness = seekLine2;
                    seekLine2.setContentDescription(Panel.this.word("media_volume", new Object[0]));
                    seekLine2.listener = new Slider.Listener() { // from class: dev.colorcc.Panel.Player.12
                        @Override // dev.colorcc.Slider.Listener
                        public final void onChange(float f, boolean z) {
                            Player.this.m171lambda$new$9$devcolorccPanel$Player(f, z);
                        }
                    };
                    LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(0, Panel.this.dp(28.0f), 1.0f);
                    layoutParams4.leftMargin = Panel.this.dp(12.0f);
                    layoutParams4.rightMargin = Panel.this.dp(12.0f);
                    linearLayoutHorizontal3.addView(seekLine2, layoutParams4);
                    Drawable drawableSpeaker = Icons.speaker();
                    drawableSpeaker.setLevel(10000);
                    ImageView imageViewButton5 = button(context, drawableSpeaker, null, 0);
                    imageViewButton5.setImageTintList(ColorStateList.valueOf(-1711276033));
                    linearLayoutHorizontal3.addView(imageViewButton5, new LinearLayout.LayoutParams(Panel.this.dp(20.0f), Panel.this.dp(20.0f)));
                    LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(-1, -2);
                    layoutParams5.topMargin = Panel.this.dp(4.0f);
                    linearLayout3.setClipChildren(false);
                    linearLayout3.addView(linearLayoutHorizontal3, layoutParams5);
                    LinearLayout linearLayoutHorizontal4 = Panel.horizontal(context);
                    linearLayoutHorizontal4.setGravity(16);
                    linearLayoutHorizontal4.setPadding(Panel.this.dp(12.0f), 0, Panel.this.dp(16.0f), 0);
                    linearLayoutHorizontal4.setBackground(new Glass(-1.0f, 587202559, 587202559, 0.5f, 0.5f));
                    ImageView imageViewButton6 = button(context, Icons.of("M21,3H3C1.9,3 1,3.9 1,5v3h2V5h18v14h-7v2h7c1.1,0 2,-0.9 2,-2V5C23,3.9 22.1,3 21,3zM1,18v3h3C4,19.34 2.66,18 1,18zM1,14v2c2.76,0 5,2.24 5,5h2C8,17.13 4.87,14 1,14zM1,10v2c4.97,0 9,4.03 9,9h2C12,14.92 7.07,10 1,10z"), null, 0);
                    imageViewButton6.setImageTintList(ColorStateList.valueOf(-1));
                    linearLayoutHorizontal4.addView(imageViewButton6, new LinearLayout.LayoutParams(Panel.this.dp(20.0f), Panel.this.dp(20.0f)));
                    TextView textViewLabel5 = Panel.this.label(context, 13.0f, -1, i4);
                    textViewLabel5.setText(Panel.this.word("media_output", new Object[0]));
                    LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(-2, -2);
                    layoutParams6.leftMargin = Panel.this.dp(8.0f);
                    linearLayoutHorizontal4.addView(textViewLabel5, layoutParams6);
                    linearLayoutHorizontal4.setContentDescription(Panel.this.word("media_output", new Object[0]));
                    linearLayoutHorizontal4.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Player.13
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            Player.this.m159lambda$new$11$devcolorccPanel$Player(view);
                        }
                    });
                    Panel.this.squish(linearLayoutHorizontal4);
                    LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(-2, Panel.this.dp(38.0f));
                    layoutParams7.gravity = 1;
                    layoutParams7.topMargin = Panel.this.dp(12.0f);
                    linearLayout3.addView(linearLayoutHorizontal4, layoutParams7);
                    this.card.addView(linearLayout3, new LinearLayout.LayoutParams(-1, -2));
                    this.prev.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Player.14
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            Player.this.m160lambda$new$13$devcolorccPanel$Player(view);
                        }
                    });
                    this.next.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Player.15
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            Player.this.m161lambda$new$15$devcolorccPanel$Player(view);
                        }
                    });
                    this.play.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Player.16
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            Player.this.m163lambda$new$17$devcolorccPanel$Player(view);
                        }
                    });
                    addView(this.card, new FrameLayout.LayoutParams(Panel.this.dp(260.0f), -2, 17));
                    Motion.Value value = Panel.this.motion.value(Logic.Spring.MENU, new Motion.Listener() { // from class: dev.colorcc.Panel.Player.6
                        @Override // dev.colorcc.Motion.Listener
                        public final void onValue(float f, float f2) {
                            Player.this.m165lambda$new$19$devcolorccPanel$Player(f, f2);
                        }
                    });
                    this.show = value;
                    this.back = Panel.this.new Rewind(value, new Motion.Listener() { // from class: dev.colorcc.Panel.Player.7
                        @Override // dev.colorcc.Motion.Listener
                        public final void onValue(float f, float f2) {
                            Player.this.m165lambda$new$19$devcolorccPanel$Player(f, f2);
                        }
                    });
                    return;
                }
            }
        }

        /* renamed from: lambda$new$1$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m157lambda$new$1$devcolorccPanel$Player(View view) {
            close();
        }

        /* renamed from: lambda$new$3$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m166lambda$new$3$devcolorccPanel$Player(View view) {
            final Panel panel = Panel.this;
            panel.guard(new Runnable() { // from class: dev.colorcc.Panel.Player.26
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.access$11300(panel);
                }
            });
        }

        /* renamed from: lambda$new$4$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ boolean m167lambda$new$4$devcolorccPanel$Player(View view, MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                Panel.this.scaleTo(view, 0.97f, Logic.Spring.PRESS_IN);
                return false;
            }
            if (actionMasked != 1 && actionMasked != 3) {
                return false;
            }
            Panel.this.scaleTo(view, 1.0f, Logic.Spring.PRESS_OUT);
            return false;
        }

        /* renamed from: lambda$new$7$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m169lambda$new$7$devcolorccPanel$Player(final float f, final boolean z) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Player.21
                @Override // java.lang.Runnable
                public final void run() {
                    Player.this.m168lambda$new$6$devcolorccPanel$Player(f, z);
                }
            });
        }

        /* renamed from: lambda$new$6$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m168lambda$new$6$devcolorccPanel$Player(final float f, boolean z) {
            final long jDuration = duration();
            if (jDuration <= 0) {
                return;
            }
            this.elapsed.setText(Logic.clock((long) (jDuration * f)));
            if (z) {
                Panel.this.mediaTransport(new Consumer() { // from class: dev.colorcc.Panel.Player.22
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        Player.lambda$new$5(f, jDuration, (MediaController.TransportControls) obj);
                    }
                });
            }
        }

        static /* synthetic */ void lambda$new$5(float f, long j, MediaController.TransportControls transportControls) {
            transportControls.seekTo((long) (f * j));
        }

        /* renamed from: lambda$new$9$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m171lambda$new$9$devcolorccPanel$Player(final float f, boolean z) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Player.25
                @Override // java.lang.Runnable
                public final void run() {
                    Player.this.m170lambda$new$8$devcolorccPanel$Player(f);
                }
            });
        }

        /* renamed from: lambda$new$10$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m158lambda$new$10$devcolorccPanel$Player(View view) {
            Panel.this.sheet.openOutput(view, Panel.this.controller == null ? null : Panel.this.controller.getPackageName());
        }

        /* renamed from: lambda$new$11$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m159lambda$new$11$devcolorccPanel$Player(final View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Player.24
                @Override // java.lang.Runnable
                public final void run() {
                    Player.this.m158lambda$new$10$devcolorccPanel$Player(view);
                }
            });
        }

        /* renamed from: lambda$new$13$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m160lambda$new$13$devcolorccPanel$Player(View view) {
            Panel.this.skipped(-1);
            Panel.this.mediaTransport(new Consumer() { // from class: dev.colorcc.Panel.Player.19
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((MediaController.TransportControls) obj).skipToPrevious();
                }
            });
        }

        /* renamed from: lambda$new$15$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m161lambda$new$15$devcolorccPanel$Player(View view) {
            Panel.this.skipped(1);
            Panel.this.mediaTransport(new Consumer() { // from class: dev.colorcc.Panel.Player.0
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((MediaController.TransportControls) obj).skipToNext();
                }
            });
        }

        /* renamed from: lambda$new$17$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m163lambda$new$17$devcolorccPanel$Player(View view) {
            Panel.this.mediaTransport(new Consumer() { // from class: dev.colorcc.Panel.Player.17
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    Player.this.m162lambda$new$16$devcolorccPanel$Player((MediaController.TransportControls) obj);
                }
            });
        }

        /* renamed from: lambda$new$16$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m162lambda$new$16$devcolorccPanel$Player(MediaController.TransportControls transportControls) {
            if (this.playing) {
                transportControls.pause();
            } else {
                transportControls.play();
            }
        }

        private ImageView button(Context context, Drawable drawable, String str, int i) {
            ImageView imageView = new ImageView(context);
            imageView.setImageDrawable(drawable);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(i, i, i, i);
            imageView.setContentDescription(str);
            return imageView;
        }

        @Override // android.view.ViewGroup, android.view.View
        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            if (motionEvent.getActionMasked() == 0 && getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return super.dispatchTouchEvent(motionEvent);
        }

        @Override // android.view.View
        protected void onSizeChanged(int i, int i2, int i3, int i4) {
            super.onSizeChanged(i, i2, i3, i4);
            int iMax = Math.max(Panel.this.dp(110.0f), Math.min(Panel.this.dp(250.0f), i2 - Panel.this.dp(300.0f)));
            if (this.cover.getLayoutParams().height == iMax) {
                return;
            }
            this.cover.getLayoutParams().height = iMax;
            final FrameLayout frameLayout = this.cover;
            Objects.requireNonNull(frameLayout);
            post(new Runnable() { // from class: dev.colorcc.Panel.Player.4
                @Override // java.lang.Runnable
                public final void run() {
                    frameLayout.requestLayout();
                }
            });
        }

        void open() {
            if (Panel.this.controller == null || this.opened) {
                return;
            }
            Panel.this.sheet.close();
            this.opened = true;
            this.shownTitle = null;
            this.ghost.setAlpha(0.0f);
            Panel.this.renderMedia();
            this.back.stop();
            measureFrom();
            m165lambda$new$19$devcolorccPanel$Player(this.show.value(), this.show.velocity());
            setVisibility(0);
            this.show.to(1.0f);
            Panel.this.main.removeCallbacks(this.tick);
            Panel.this.main.post(this.tick);
        }

        void close() {
            if (this.opened) {
                this.opened = false;
                Panel.this.main.removeCallbacks(this.tick);
                measureFrom();
                this.back.start();
            }
        }

        private void measureFrom() {
            int width = this.card.getWidth();
            if (width == 0) {
                this.fromY = 0.0f;
                this.fromX = 0.0f;
                this.fromScale = 0.85f;
                this.fromHeight = this.card.getHeight();
                return;
            }
            getLocationOnScreen(this.at);
            float left = this.at[0] + this.card.getLeft();
            float top = this.at[1] + this.card.getTop();
            Panel.this.media.getLocationOnScreen(this.at);
            this.fromScale = ((Panel.this.media.getWidth() * Panel.this.media.getScaleX()) * Panel.this.column.getScaleX()) / width;
            int[] iArr = this.at;
            this.fromX = iArr[0] - left;
            this.fromY = iArr[1] - top;
            this.fromHeight = ((Panel.this.media.getHeight() * Panel.this.media.getScaleY()) * Panel.this.column.getScaleY()) / this.fromScale;
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* renamed from: applyShow, reason: merged with bridge method [inline-methods] and merged with bridge method [inline-methods] */
        public void m165lambda$new$19$devcolorccPanel$Player(float f, float f2) {
            this.grown = Logic.clamp(f);
            float f3 = this.fromScale;
            float f4 = f3 + ((1.0f - f3) * f);
            this.card.setScaleX(f4);
            this.card.setScaleY(f4);
            float f5 = 1.0f - f;
            this.card.setTranslationX(this.fromX * f5);
            this.card.setTranslationY(this.fromY * f5);
            this.card.invalidateOutline();
            this.card.setAlpha(Logic.clamp((f - 0.05f) / 0.45f));
            Panel.this.media.setVisibility(f < 0.6f ? 0 : 4);
            this.shade.setAlpha(Math.round(this.grown * 255.0f));
            Panel.this.playerBack = this.grown;
            Panel.this.backdrop();
            if (this.opened || f2 != 0.0f || f > 0.0f) {
                return;
            }
            setVisibility(4);
        }

        void render(CharSequence charSequence, CharSequence charSequence2, Bitmap bitmap, boolean z) {
            if (this.opened) {
                this.playing = z;
                this.title.setText(TextUtils.isEmpty(charSequence) ? Panel.this.word("not_playing", new Object[0]) : charSequence);
                this.artist.setText(charSequence2);
                this.artist.setVisibility(TextUtils.isEmpty(charSequence2) ? 8 : 0);
                if (TextUtils.isEmpty(charSequence)) {
                    charSequence = "";
                }
                CharSequence charSequence3 = this.shownTitle;
                boolean z2 = (charSequence3 == null || TextUtils.equals(charSequence, charSequence3) || this.show.value() < 1.0f) ? false : true;
                this.shownTitle = charSequence;
                if (z2 && this.art.getDrawable() != null) {
                    this.ghost.setScaleType(this.art.getScaleType());
                    this.ghost.setPadding(this.art.getPaddingLeft(), this.art.getPaddingTop(), this.art.getPaddingRight(), this.art.getPaddingBottom());
                    this.ghost.setImageTintList(this.art.getImageTintList());
                    this.ghost.setImageDrawable(this.art.getDrawable());
                    Panel.this.spring(this.ghost, View.ALPHA, Logic.Spring.REVEAL).snap(1.0f);
                    Panel.this.spring(this.ghost, View.ALPHA, Logic.Spring.REVEAL).to(0.0f);
                    Panel.this.spring(this.ghost, View.SCALE_X, Logic.Spring.REVEAL).snap(1.0f);
                    Panel.this.spring(this.ghost, View.SCALE_Y, Logic.Spring.REVEAL).snap(1.0f);
                    Panel.this.scaleTo(this.ghost, 0.94f, Logic.Spring.REVEAL);
                }
                ImageView imageView = this.art;
                if (bitmap == null) {
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    this.art.setPadding(Panel.this.dp(96.0f), Panel.this.dp(60.0f), Panel.this.dp(96.0f), Panel.this.dp(100.0f));
                    this.art.setImageDrawable(this.noteIcon);
                    this.art.setImageTintList(ColorStateList.valueOf(-1711276033));
                } else {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    this.art.setPadding(0, 0, 0, 0);
                    this.art.setImageTintList(null);
                    this.art.setImageBitmap(bitmap);
                }
                if (z2) {
                    Panel.this.swellIn(this.art);
                    Panel.this.slideIn(this.title, this.artist);
                }
                this.play.setImageDrawable(z ? this.pauseIcon : this.playIcon);
                this.play.setContentDescription(Panel.this.word(z ? "pause" : "play", new Object[0]));
                this.tint.show(Panel.this.mediaTones);
                showActions();
                showBadge();
                this.seek.light(Panel.this.mediaTones == null ? -1 : Logic.seekColor(Panel.this.mediaTones));
                this.seek.breathing = z;
                this.seek.invalidate();
                readVolume();
                showProgress();
            }
        }

        private void showActions() {
            Context contextCreatePackageContext;
            Drawable drawable = null;
            PlaybackState playbackState = Panel.this.controller == null ? null : Panel.this.controller.getPlaybackState();
            List<PlaybackState.CustomAction> customActions = playbackState == null ? Collections.EMPTY_LIST : playbackState.getCustomActions();
            try {
                contextCreatePackageContext = getContext().createPackageContext(Panel.this.controller.getPackageName(), 0);
            } catch (Throwable unused) {
                contextCreatePackageContext = null;
            }
            int i = 0;
            while (i < this.actions.length) {
                final PlaybackState.CustomAction customAction = i < customActions.size() ? customActions.get(i) : null;
                if (customAction == null || contextCreatePackageContext == null) {
                    drawable = null;
                } else {
                    try {
                        drawable = contextCreatePackageContext.getDrawable(customAction.getIcon());
                    } catch (Throwable unused2) {
                    }
                }
                this.actions[i].setVisibility(drawable == null ? 4 : 0);
                if (drawable != null) {
                    this.actions[i].setImageDrawable(drawable);
                    this.actions[i].setContentDescription(customAction.getName());
                    this.actions[i].setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Player.18
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            Player.this.m172lambda$showActions$21$devcolorccPanel$Player(customAction, view);
                        }
                    });
                }
                i++;
            }
        }

        /* renamed from: lambda$showActions$21$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m172lambda$showActions$21$devcolorccPanel$Player(final PlaybackState.CustomAction customAction, View view) {
            Panel.this.mediaTransport(new Consumer() { // from class: dev.colorcc.Panel.Player.20
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((MediaController.TransportControls) obj).sendCustomAction(customAction, (Bundle) null);
                }
            });
        }

        private void showBadge() {
            String packageName = Panel.this.controller == null ? null : Panel.this.controller.getPackageName();
            if (packageName == null || packageName.equals(this.badgeFor)) {
                return;
            }
            this.badgeFor = packageName;
            try {
                this.badge.setImageDrawable(getContext().getPackageManager().getApplicationIcon(packageName));
                this.badge.setVisibility(0);
            } catch (Throwable unused) {
                this.badge.setVisibility(8);
            }
        }

        void readVolume() {
            MediaController.PlaybackInfo playbackInfo;
            if (!this.opened || Panel.this.controller == null || this.loudness.dragging || (playbackInfo = Panel.this.controller.getPlaybackInfo()) == null || playbackInfo.getMaxVolume() <= 0) {
                return;
            }
            this.volumeSteps = playbackInfo.getMaxVolume();
            int currentVolume = playbackInfo.getCurrentVolume();
            this.sentVolume = currentVolume;
            this.loudness.set(currentVolume / this.volumeSteps);
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* renamed from: setVolume, reason: merged with bridge method [inline-methods] */
        public void m170lambda$new$8$devcolorccPanel$Player(float f) {
            int i;
            int iRound;
            if (Panel.this.controller == null || (i = this.volumeSteps) <= 0 || (iRound = Math.round(f * i)) == this.sentVolume) {
                return;
            }
            this.sentVolume = iRound;
            Panel.this.controller.setVolumeTo(iRound, 0);
        }

        private long duration() {
            MediaMetadata metadata = Panel.this.controller == null ? null : Panel.this.controller.getMetadata();
            if (metadata == null) {
                return 0L;
            }
            return metadata.getLong("android.media.metadata.DURATION");
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showProgress() {
            long position;
            Panel.this.main.removeCallbacks(this.tick);
            if (!this.opened || Panel.this.controller == null) {
                return;
            }
            PlaybackState playbackState = Panel.this.controller.getPlaybackState();
            long jDuration = duration();
            if (playbackState != null) {
                position = playbackState.getPosition();
                if (playbackState.getState() == 3) {
                    position += (long) ((SystemClock.elapsedRealtime() - playbackState.getLastPositionUpdateTime()) * playbackState.getPlaybackSpeed());
                }
            } else {
                position = 0;
            }
            long jMin = jDuration > 0 ? Math.min(position, jDuration) : Math.max(0L, position);
            this.seek.setEnabled(jDuration > 0);
            if (!this.seek.dragging) {
                this.seek.set(jDuration > 0 ? jMin / jDuration : 0.0f);
                this.elapsed.setText(Logic.clock(jMin));
            }
            this.total.setText(jDuration > 0 ? Logic.clock(jDuration) : "");
            this.seek.setStateDescription(Logic.clock(jMin) + " of " + Logic.clock(jDuration));
            Panel.this.main.postDelayed(this.tick, 500L);
        }
    }

    private static final class FadingImage extends ImageView {
        private final Paint fade;

        FadingImage(Context context) {
            super(context);
            Paint paint = new Paint();
            this.fade = paint;
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        }

        @Override // android.view.View
        protected void onSizeChanged(int i, int i2, int i3, int i4) {
            super.onSizeChanged(i, i2, i3, i4);
            this.fade.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, i2, new int[]{-16777216, -16777216, -1728053248, 0}, new float[]{0.0f, 0.5f, 0.78f, 1.0f}, Shader.TileMode.CLAMP));
        }

        @Override // android.widget.ImageView, android.view.View
        protected void onDraw(Canvas canvas) {
            int iSaveLayer = canvas.saveLayer(0.0f, 0.0f, getWidth(), getHeight(), null);
            super.onDraw(canvas);
            canvas.drawRect(0.0f, 0.0f, getWidth(), getHeight(), this.fade);
            canvas.restoreToCount(iSaveLayer);
        }
    }

    private final class SeekLine extends View {
        private final RectF bar;
        boolean breathing;
        boolean dragging;
        private final Paint fill;
        boolean glow;
        private final Paint halo;
        private long lastFrameMs;
        Slider.Listener listener;
        private int lit;
        private float phase;
        private final Paint track;
        private final RectF trail;
        private float value;

        SeekLine(Context context) {
            super(context);
            Paint paint = new Paint(1);
            this.track = paint;
            Paint paint2 = new Paint(1);
            this.fill = paint2;
            Paint paint3 = new Paint(1);
            this.halo = paint3;
            this.bar = new RectF();
            this.trail = new RectF();
            this.lit = -1;
            paint.setColor(1090519039);
            paint2.setColor(-1);
            paint3.setMaskFilter(new BlurMaskFilter(Panel.this.dp(5.0f), BlurMaskFilter.Blur.NORMAL));
            setContentDescription(Panel.this.word("playback_position", new Object[0]));
        }

        void set(float f) {
            float fClamp = Logic.clamp(f);
            if (fClamp == this.value) {
                return;
            }
            this.value = fClamp;
            invalidate();
        }

        void light(int i) {
            if (i == this.lit) {
                return;
            }
            this.lit = i;
            this.fill.setColor(i);
            invalidate();
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            float fDp = Panel.this.dp(this.dragging ? 8.0f : 5.0f);
            float height = getHeight() / 2.0f;
            float f = fDp / 2.0f;
            this.bar.set(0.0f, height - f, getWidth(), height + f);
            canvas.drawRoundRect(this.bar, f, f, this.track);
            boolean z = this.glow;
            RectF rectF = this.bar;
            if (!z) {
                rectF.right = getWidth() * this.value;
                if (this.bar.right > fDp) {
                    canvas.drawRoundRect(this.bar, f, f, this.fill);
                    return;
                }
                return;
            }
            rectF.right = Math.max(2.4f * fDp, getWidth() * this.value);
            long jUptimeMillis = SystemClock.uptimeMillis();
            float fMin = this.lastFrameMs == 0 ? 0.0f : Math.min(0.05f, (jUptimeMillis - this.lastFrameMs) / 1000.0f);
            this.lastFrameMs = jUptimeMillis;
            boolean z2 = this.breathing;
            if (z2) {
                this.phase = (this.phase + (fMin * 2.38f)) % 6.2831855f;
            }
            float fSin = (z2 ? (((float) Math.sin(this.phase)) * 0.15f) + 0.05f : 0.0f) + 0.8f;
            this.halo.setColor(Color.argb(Math.round(114.75f * fSin), Color.red(this.lit), Color.green(this.lit), Color.blue(this.lit)));
            this.trail.set(Math.max(0.0f, this.bar.right - Panel.this.dp(70.0f)), this.bar.top, this.bar.right, this.bar.bottom);
            canvas.drawRoundRect(this.trail, f, f, this.halo);
            this.halo.setColor(Color.argb(Math.round(fSin * 229.5f), (Color.red(this.lit) + 255) / 2, (Color.green(this.lit) + 255) / 2, (Color.blue(this.lit) + 255) / 2));
            canvas.drawCircle(this.bar.right - (0.6f * fDp), height, fDp * 0.95f, this.halo);
            canvas.drawRoundRect(this.bar, f, f, this.fill);
            if (this.breathing) {
                postInvalidateOnAnimation();
            } else {
                this.lastFrameMs = 0L;
            }
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            if (!isEnabled()) {
                return false;
            }
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                getParent().requestDisallowInterceptTouchEvent(true);
                this.dragging = true;
                follow(motionEvent, false);
                return true;
            }
            if (actionMasked == 1) {
                follow(motionEvent, true);
                this.dragging = false;
                invalidate();
                return true;
            }
            if (actionMasked == 2) {
                follow(motionEvent, false);
                return true;
            }
            if (actionMasked != 3) {
                return false;
            }
            this.dragging = false;
            invalidate();
            return true;
        }

        private void follow(MotionEvent motionEvent, boolean z) {
            this.value = Logic.clamp(motionEvent.getX() / Math.max(1, getWidth()));
            invalidate();
            Slider.Listener listener = this.listener;
            if (listener != null) {
                listener.onChange(this.value, z);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String specOf(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return (String) XposedHelpers.callMethod(obj, "getSpec", new Object[0]);
        } catch (Throwable unused) {
            return String.valueOf(obj);
        }
    }

    private final class Editor extends FrameLayout {
        private static final String BADGE = "badge";
        private static final int GROW_LISTENER = 2113929220;
        private static final int GROW_TURN = 2113929221;
        private static final String HANDLE = "handle";
        private static final char PIN = 'p';
        private static final int PIN_SPEC = 2113929219;
        private static final int RESIZE_ACTION = 2113929218;
        private boolean active;
        private final TextView allIn;
        private final TileGrid availableGrid;
        private List<String> before;
        private final List<Bound> bindings;
        private final TileGrid blockGrid;
        private int blurBefore;
        private final Segmented blurChoice;
        private final LinearLayout body;
        private final TextView cardsHint;
        private final TileGrid chosenGrid;
        private final List<Object> created;
        private boolean draftIcons;
        private int draftLook;
        private boolean draftNames;
        private boolean draftNotif;
        private int draftRows;
        private boolean draftSwipe;
        private View edgeCell;
        private TileGrid edgeGrid;
        private final Runnable edgeStep;
        private float edgeX;
        private float edgeY;
        private final List<String> fixed;
        private Motion.Value headerAway;
        private final TileGrid hiddenGrid;
        private Segmented iconChoice;
        private boolean layered;
        private Map<String, Object> live;
        private int lookBefore;
        private final Segmented lookChoice;
        private final ScrollView lookScroll;
        private Segmented namesChoice;
        private Segmented notifChoice;
        private final LinearLayout page;
        private final List<View> parts;
        private Segmented rowsChoice;
        private final ScrollView scroll;
        private int shineBefore;
        private Segmented shineChoice;
        private final Motion.Value shown;
        private Segmented swipeChoice;
        private int tab;
        private final Motion.Value tabAt;
        private Segmented tabs;
        private final TextView tilesHint;
        private int tintBefore;
        private Segmented tintChoice;
        private final View top;
        private boolean wide;

        @Override // android.view.ViewGroup, android.view.View
        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            if (motionEvent.getActionMasked() == 0 && getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return super.dispatchTouchEvent(motionEvent);
        }

        Editor(Context context) {
            super(context);
            this.draftRows = 3;
            this.draftNames = true;
            this.draftSwipe = true;
            this.bindings = new ArrayList();
            this.created = new ArrayList();
            this.fixed = new ArrayList();
            this.before = new ArrayList();
            this.live = new HashMap();
            ArrayList arrayList = new ArrayList();
            this.parts = arrayList;
            this.edgeStep = new Runnable() { // from class: dev.colorcc.Panel.Editor.23
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m131lambda$new$39$devcolorccPanel$Editor();
                }
            };
            setVisibility(8);
            setClickable(true);
            LinearLayout linearLayout = new LinearLayout(context);
            this.page = linearLayout;
            linearLayout.setOrientation(1);
            linearLayout.setClipToPadding(false);
            linearLayout.setClipChildren(false);
            FrameLayout frameLayout = new FrameLayout(context);
            Disc disc = Panel.this.new Disc(context, Panel.this.dp(14.0f));
            disc.icon.setImageDrawable(Icons.of("M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z"));
            disc.setContentDescription(Panel.this.word("cancel", new Object[0]));
            disc.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Editor.32
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Editor.this.m120lambda$new$1$devcolorccPanel$Editor(view);
                }
            });
            Panel.this.squish(disc);
            frameLayout.addView(disc, new FrameLayout.LayoutParams(Panel.this.dp(36.0f), Panel.this.dp(36.0f), 8388627));
            TextView textViewLabel = Panel.this.label(context, 17.0f, -1, 1);
            textViewLabel.setText(Panel.this.word("edit", new Object[0]));
            textViewLabel.setTypeface(Typeface.DEFAULT_BOLD);
            textViewLabel.setGravity(17);
            frameLayout.addView(textViewLabel, new FrameLayout.LayoutParams(-2, -2, 17));
            TextView textViewPill = pill(context, Panel.this.word("done", new Object[0]), 14.0f, true);
            textViewPill.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Editor.33
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Editor.this.m130lambda$new$3$devcolorccPanel$Editor(view);
                }
            });
            frameLayout.addView(textViewPill, new FrameLayout.LayoutParams(Panel.this.dp(76.0f), Panel.this.dp(34.0f), 8388629));
            linearLayout.addView(frameLayout, new LinearLayout.LayoutParams(-1, Panel.this.dp(48.0f)));
            arrayList.add(frameLayout);
            this.tabs = Panel.this.new Segmented(context, new String[]{Panel.this.word("layout", new Object[0]), Panel.this.word("look", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.34
                @Override // java.util.function.IntConsumer
                public final void accept(int i) {
                    Editor.this.m132lambda$new$4$devcolorccPanel$Editor(i);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f));
            layoutParams.topMargin = Panel.this.dp(4.0f);
            linearLayout.addView(this.tabs, layoutParams);
            arrayList.add(this.tabs);
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(1);
            linearLayout2.setClipChildren(false);
            TextView textViewHint = hint(context, Panel.this.word("edit_how", new Object[0]));
            textViewHint.setMaxLines(3);
            textViewHint.setGravity(1);
            linearLayout2.addView(textViewHint);
            linearLayout2.addView(section(context, Panel.this.word("in_panel", new Object[0]), 10));
            TileGrid tileGrid = Panel.this.new TileGrid(context, true, 4, Panel.this.dp(63.0f), Panel.this.dp(14.0f), true);
            this.blockGrid = tileGrid;
            linearLayout2.addView(tileGrid, new LinearLayout.LayoutParams(-1, -2));
            TileGrid tileGrid2 = Panel.this.new TileGrid(context, true, 4, Panel.this.dp(98.0f), 0, false);
            this.chosenGrid = tileGrid2;
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams2.topMargin = Panel.this.dp(16.0f);
            linearLayout2.addView(tileGrid2, layoutParams2);
            linearLayout2.addView(section(context, Panel.this.word("tap_to_add", new Object[0]), 26));
            TextView textViewHint2 = hint(context, Panel.this.word("cards_sliders", new Object[0]));
            this.cardsHint = textViewHint2;
            linearLayout2.addView(textViewHint2);
            TileGrid tileGrid3 = Panel.this.new TileGrid(context, false, 4, Panel.this.dp(63.0f), Panel.this.dp(14.0f), true);
            this.hiddenGrid = tileGrid3;
            linearLayout2.addView(tileGrid3, new LinearLayout.LayoutParams(-1, -2));
            TextView textViewHint3 = hint(context, Panel.this.word("tiles", new Object[0]));
            this.tilesHint = textViewHint3;
            linearLayout2.addView(textViewHint3);
            TileGrid tileGrid4 = Panel.this.new TileGrid(context, false, 4, Panel.this.dp(98.0f), 0, false);
            this.availableGrid = tileGrid4;
            linearLayout2.addView(tileGrid4, new LinearLayout.LayoutParams(-1, -2));
            TextView textViewHint4 = hint(context, Panel.this.word("all_in", new Object[0]));
            this.allIn = textViewHint4;
            linearLayout2.addView(textViewHint4);
            TextView textViewPill2 = pill(context, Panel.this.word("reset", new Object[0]), 14.0f, false);
            textViewPill2.setPadding(Panel.this.dp(20.0f), 0, Panel.this.dp(20.0f), 0);
            textViewPill2.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Editor.35
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Editor.this.m134lambda$new$6$devcolorccPanel$Editor(view);
                }
            });
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, Panel.this.dp(44.0f));
            layoutParams3.gravity = 1;
            layoutParams3.topMargin = Panel.this.dp(26.0f);
            linearLayout2.addView(textViewPill2, layoutParams3);
            TextView textViewHint5 = hint(context, Panel.this.word("reset_how", new Object[0]));
            textViewHint5.setGravity(1);
            linearLayout2.addView(textViewHint5);
            for (int i = 0; i < linearLayout2.getChildCount(); i++) {
                this.parts.add(linearLayout2.getChildAt(i));
            }
            LinearLayout linearLayout3 = new LinearLayout(context);
            linearLayout3.setOrientation(1);
            linearLayout3.setClipChildren(false);
            linearLayout3.addView(section(context, Panel.this.word("appearance", new Object[0]), 10));
            LinearLayout linearLayout4 = new LinearLayout(context);
            linearLayout4.setOrientation(1);
            linearLayout4.setBackground(new Glass(Panel.this.dp(26.0f), 452984831, 452984831, 0.5f, 0.5f));
            linearLayout4.setPadding(Panel.this.dp(16.0f), Panel.this.dp(12.0f), Panel.this.dp(16.0f), Panel.this.dp(16.0f));
            linearLayout4.addView(hint(context, Panel.this.word("glass", new Object[0])));
            Segmented segmented = Panel.this.new Segmented(context, new String[]{Panel.this.word("liquid", new Object[0]), Panel.this.word("frosted", new Object[0]), Panel.this.word("opaque", new Object[0]), Panel.this.word("none", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.36
                @Override // java.util.function.IntConsumer
                public final void accept(int i2) {
                    Editor.this.setLook(i2);
                }
            });
            this.lookChoice = segmented;
            linearLayout4.addView(segmented, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            linearLayout4.addView(hint(context, Panel.this.word("background_blur", new Object[0])));
            Segmented segmented2 = Panel.this.new Segmented(context, new String[]{Panel.this.word("light", new Object[0]), Panel.this.word("normal", new Object[0]), Panel.this.word("strong", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.37
                @Override // java.util.function.IntConsumer
                public final void accept(int i2) {
                    Editor.this.setBlur(i2);
                }
            });
            this.blurChoice = segmented2;
            linearLayout4.addView(segmented2, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            linearLayout4.addView(hint(context, Panel.this.word("tile_rows", new Object[0])));
            Segmented segmented3 = Panel.this.new Segmented(context, new String[]{"2", "3", "4"}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.38
                @Override // java.util.function.IntConsumer
                public final void accept(int i2) {
                    Editor.this.m135lambda$new$7$devcolorccPanel$Editor(i2);
                }
            });
            this.rowsChoice = segmented3;
            linearLayout4.addView(segmented3, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            linearLayout4.addView(hint(context, Panel.this.word("icon_colours", new Object[0])));
            Segmented segmented4 = Panel.this.new Segmented(context, new String[]{"ColorOS", Panel.this.word("system", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.39
                @Override // java.util.function.IntConsumer
                public final void accept(int i2) {
                    Editor.this.m136lambda$new$8$devcolorccPanel$Editor(i2);
                }
            });
            this.iconChoice = segmented4;
            linearLayout4.addView(segmented4, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            linearLayout4.addView(hint(context, Panel.this.word("tile_names", new Object[0])));
            Segmented segmented5 = Panel.this.new Segmented(context, new String[]{Panel.this.word("show", new Object[0]), Panel.this.word("hide", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.40
                @Override // java.util.function.IntConsumer
                public final void accept(int i2) {
                    Editor.this.m137lambda$new$9$devcolorccPanel$Editor(i2);
                }
            });
            this.namesChoice = segmented5;
            linearLayout4.addView(segmented5, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            linearLayout4.addView(hint(context, Panel.this.word("glass_tint", new Object[0])));
            Segmented segmented6 = Panel.this.new Segmented(context, new String[]{Panel.this.word("clear", new Object[0]), Panel.this.word("balanced", new Object[0]), Panel.this.word("strong", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.24
                @Override // java.util.function.IntConsumer
                public final void accept(int i2) {
                    Editor.this.m121lambda$new$10$devcolorccPanel$Editor(i2);
                }
            });
            this.tintChoice = segmented6;
            linearLayout4.addView(segmented6, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            linearLayout4.addView(hint(context, Panel.this.word("edge_shine", new Object[0])));
            Segmented segmented7 = Panel.this.new Segmented(context, new String[]{Panel.this.word("soft", new Object[0]), Panel.this.word("normal", new Object[0]), Panel.this.word("bright", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.25
                @Override // java.util.function.IntConsumer
                public final void accept(int i2) {
                    Editor.this.m122lambda$new$11$devcolorccPanel$Editor(i2);
                }
            });
            this.shineChoice = segmented7;
            linearLayout4.addView(segmented7, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            linearLayout4.addView(hint(context, Panel.this.word("notifications", new Object[0])));
            Segmented segmented8 = Panel.this.new Segmented(context, new String[]{Panel.this.word("notif_stock", new Object[0]), Panel.this.word("notif_match", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.26
                @Override // java.util.function.IntConsumer
                public final void accept(int i2) {
                    Editor.this.m123lambda$new$12$devcolorccPanel$Editor(i2);
                }
            });
            this.notifChoice = segmented8;
            linearLayout4.addView(segmented8, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            linearLayout4.addView(hint(context, Panel.this.word("notif_swipe", new Object[0])));
            Segmented segmented9 = Panel.this.new Segmented(context, new String[]{Panel.this.word("on", new Object[0]), Panel.this.word("off", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.27
                @Override // java.util.function.IntConsumer
                public final void accept(int i2) {
                    Editor.this.m124lambda$new$13$devcolorccPanel$Editor(i2);
                }
            });
            this.swipeChoice = segmented9;
            linearLayout4.addView(segmented9, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            linearLayout3.addView(linearLayout4, new LinearLayout.LayoutParams(-1, -2));
            TextView textViewLabel2 = Panel.this.label(context, 10.0f, -2130706433, 1);
            textViewLabel2.setText("Made by Guest");
            textViewLabel2.setGravity(1);
            LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams4.topMargin = Panel.this.dp(20.0f);
            linearLayout3.addView(textViewLabel2, layoutParams4);
            for (int i2 = 0; i2 < linearLayout3.getChildCount(); i2++) {
                this.parts.add(linearLayout3.getChildAt(i2));
            }
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            this.body = linearLayoutHorizontal;
            linearLayoutHorizontal.setClipChildren(true);
            ScrollView scrollViewScroller = scroller(context, linearLayout2);
            this.scroll = scrollViewScroller;
            ScrollView scrollViewScroller2 = scroller(context, linearLayout3);
            this.lookScroll = scrollViewScroller2;
            linearLayoutHorizontal.addView(scrollViewScroller, paneParams());
            linearLayoutHorizontal.addView(scrollViewScroller2, paneParams());
            LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(-1, 0, 1.0f);
            layoutParams5.topMargin = Panel.this.dp(4.0f);
            int i3 = -Panel.this.dp(14.0f);
            layoutParams5.rightMargin = i3;
            layoutParams5.leftMargin = i3;
            layoutParams5.bottomMargin = -Panel.this.dp(94.0f);
            this.page.addView(linearLayoutHorizontal, layoutParams5);
            this.top = frameLayout;
            addView(this.page, new FrameLayout.LayoutParams(Panel.this.dp(340.0f), -1, 1));
            this.tabAt = Panel.this.motion.value(Logic.Spring.BOUNCY, new Motion.Listener() { // from class: dev.colorcc.Panel.Editor.28
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Editor.this.m125lambda$new$14$devcolorccPanel$Editor(f, f2);
                }
            });
            showTab(0, false);
            this.headerAway = Panel.this.motion.value(Logic.Spring.SNAPPY, new Motion.Listener() { // from class: dev.colorcc.Panel.Editor.29
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Editor.this.m126lambda$new$15$devcolorccPanel$Editor(f, f2);
                }
            });
            ScrollView[] scrollViewArr = {scrollViewScroller, scrollViewScroller2};
            for (int i4 = 0; i4 < 2; i4++) {
                scrollViewArr[i4].setOnScrollChangeListener(new View.OnScrollChangeListener() { // from class: dev.colorcc.Panel.Editor.30
                    @Override // android.view.View.OnScrollChangeListener
                    public final void onScrollChange(View view, int i5, int i6, int i7, int i8) {
                        Editor.this.m128lambda$new$17$devcolorccPanel$Editor(view, i5, i6, i7, i8);
                    }
                });
            }
            this.shown = Panel.this.motion.value(Logic.Spring.REVEAL, new Motion.Listener() { // from class: dev.colorcc.Panel.Editor.31
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Editor.this.applyShown(f, f2);
                }
            });
        }

        /* renamed from: lambda$new$0$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m119lambda$new$0$devcolorccPanel$Editor() {
            close(false);
        }

        /* renamed from: lambda$new$1$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m120lambda$new$1$devcolorccPanel$Editor(View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.4
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m119lambda$new$0$devcolorccPanel$Editor();
                }
            });
        }

        /* renamed from: lambda$new$2$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m129lambda$new$2$devcolorccPanel$Editor() {
            close(true);
        }

        /* renamed from: lambda$new$3$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m130lambda$new$3$devcolorccPanel$Editor(View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.51
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m129lambda$new$2$devcolorccPanel$Editor();
                }
            });
        }

        /* renamed from: lambda$new$4$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m132lambda$new$4$devcolorccPanel$Editor(int i) {
            this.tab = i;
            this.tabs.choose(i, true);
            showTab(i, true);
            this.headerAway.to(0.0f);
        }

        /* renamed from: lambda$new$6$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m134lambda$new$6$devcolorccPanel$Editor(View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.9
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m133lambda$new$5$devcolorccPanel$Editor();
                }
            });
        }

        /* renamed from: lambda$new$7$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m135lambda$new$7$devcolorccPanel$Editor(int i) {
            this.draftRows = i + 2;
            this.rowsChoice.choose(i, true);
        }

        /* renamed from: lambda$new$8$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m136lambda$new$8$devcolorccPanel$Editor(int i) {
            this.draftIcons = i == 1;
            this.iconChoice.choose(i, true);
        }

        /* renamed from: lambda$new$9$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m137lambda$new$9$devcolorccPanel$Editor(int i) {
            this.draftNames = i == 0;
            this.namesChoice.choose(i, true);
        }

        /* renamed from: lambda$new$10$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m121lambda$new$10$devcolorccPanel$Editor(int i) {
            this.tintChoice.choose(i, true);
            Glass.tint = i;
            Panel.redraw(this);
        }

        /* renamed from: lambda$new$11$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m122lambda$new$11$devcolorccPanel$Editor(int i) {
            this.shineChoice.choose(i, true);
            Glass.shine = i;
            Panel.redraw(this);
        }

        /* renamed from: lambda$new$12$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m123lambda$new$12$devcolorccPanel$Editor(int i) {
            this.draftNotif = i == 1;
            this.notifChoice.choose(i, true);
        }

        /* renamed from: lambda$new$13$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m124lambda$new$13$devcolorccPanel$Editor(int i) {
            this.draftSwipe = i == 0;
            this.swipeChoice.choose(i, true);
        }

        /* renamed from: lambda$new$14$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m125lambda$new$14$devcolorccPanel$Editor(float f, float f2) {
            placeTabs(f);
        }

        /* renamed from: lambda$new$15$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m126lambda$new$15$devcolorccPanel$Editor(float f, float f2) {
            placeHeader(f);
        }

        /* renamed from: lambda$new$17$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m128lambda$new$17$devcolorccPanel$Editor(View view, int i, final int i2, int i3, final int i4) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.43
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m127lambda$new$16$devcolorccPanel$Editor(i2, i4);
                }
            });
        }

        private ScrollView scroller(Context context, View view) {
            ScrollView scrollView = new ScrollView(context);
            scrollView.setVerticalScrollBarEnabled(false);
            scrollView.setOverScrollMode(2);
            scrollView.setVerticalFadingEdgeEnabled(true);
            scrollView.setFadingEdgeLength(Panel.this.dp(28.0f));
            view.setPadding(Panel.this.dp(14.0f), 0, Panel.this.dp(14.0f), Panel.this.dp(94.0f));
            scrollView.addView(view, new FrameLayout.LayoutParams(-1, -2));
            scrollView.setOutlineProvider(ViewOutlineProvider.BOUNDS);
            scrollView.setClipToOutline(true);
            return scrollView;
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* renamed from: followScroll, reason: merged with bridge method [inline-methods] */
        public void m127lambda$new$16$devcolorccPanel$Editor(int i, int i2) {
            if (this.active && this.edgeGrid == null) {
                if (i > Panel.this.dp(8.0f) && i >= i2 - Panel.this.dp(2.0f)) {
                    if (i <= i2 + Panel.this.dp(2.0f) || i <= Panel.this.dp(48.0f)) {
                        return;
                    }
                    this.headerAway.to(1.0f);
                    return;
                }
                this.headerAway.to(0.0f);
            }
        }

        private void placeHeader(float f) {
            float f2 = (-(this.body.getTop() - this.top.getTop())) * f;
            View[] viewArr = {this.top, this.tabs};
            for (int i = 0; i < 2; i++) {
                View view = viewArr[i];
                view.setTranslationY(f2);
                view.setAlpha(1.0f - f);
            }
            this.body.setTranslationY(f2);
            this.top.setVisibility(f > 0.99f ? 4 : 0);
        }

        private LinearLayout.LayoutParams paneParams() {
            return new LinearLayout.LayoutParams(0, -1, 1.0f);
        }

        @Override // android.widget.FrameLayout, android.view.View
        protected void onMeasure(int i, int i2) {
            int size = View.MeasureSpec.getSize(i);
            boolean z = size > View.MeasureSpec.getSize(i2) || Panel.this.large();
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.scroll.getLayoutParams();
            LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.lookScroll.getLayoutParams();
            if (!z) {
                int iDp = Panel.this.dp(320.0f);
                layoutParams2.width = iDp;
                layoutParams.width = iDp;
                layoutParams2.weight = 0.0f;
                layoutParams.weight = 0.0f;
                layoutParams2.leftMargin = Panel.this.dp(24.0f);
            } else {
                layoutParams2.width = 0;
                layoutParams.width = 0;
                layoutParams2.weight = 1.0f;
                layoutParams.weight = 1.0f;
                layoutParams2.leftMargin = Panel.this.dp(-4.0f);
            }
            if (z != this.wide) {
                this.wide = z;
                this.tabs.setVisibility(z ? 8 : 0);
                placeTabs(this.tabAt.value());
            }
            FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) this.page.getLayoutParams();
            boolean z2 = this.wide;
            Panel panel = Panel.this;
            layoutParams3.width = z2 ? Math.min(size, panel.dp(656.0f)) : panel.dp(340.0f);
            super.onMeasure(i, i2);
        }

        private void showTab(int i, boolean z) {
            Motion.Value value = this.tabAt;
            if (z) {
                value.to(i);
            } else {
                value.snap(i);
            }
            placeTabs(this.tabAt.value());
        }

        private void placeTabs(float f) {
            View[] viewArr = {this.scroll, this.lookScroll};
            int iRound = Math.round((((LinearLayout.LayoutParams) this.scroll.getLayoutParams()).width + Panel.this.dp(24.0f)) * f);
            for (int i = 0; i < 2; i++) {
                View view = viewArr[i];
                float fMin = this.wide ? 0.0f : Math.min(1.0f, Math.abs(i - f));
                view.setTranslationX(this.wide ? 0.0f : -iRound);
                view.setAlpha(1.0f - (0.7f * fMin));
                float f2 = 1.0f - (0.06f * fMin);
                view.setScaleX(f2);
                view.setScaleY(f2);
                view.setVisibility(fMin < 0.999f ? 0 : 4);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void applyShown(float f, float f2) {
            float fClamp = Logic.clamp(f);
            Panel.this.content.setAlpha(1.0f - fClamp);
            int size = this.parts.size();
            int i = size - 1;
            float fMax = 0.45f / Math.max(1, i);
            int i2 = 0;
            for (int i3 = 0; i3 < size; i3++) {
                float fClamp2 = Logic.clamp((f - ((this.active ? i3 : i - i3) * fMax)) / 0.55f);
                View view = this.parts.get(i3);
                view.setAlpha(fClamp2);
                float f3 = fClamp2 - 1.0f;
                boolean z = this.active;
                float f4 = z ? (2.7f * f3 * f3 * f3) + 1.0f + (1.7f * f3 * f3) : fClamp2;
                Panel panel = Panel.this;
                view.setTranslationY((z ? panel.dp(32.0f) : -panel.dp(20.0f)) * (1.0f - f4));
                float f5 = (fClamp2 * 0.06f) + 0.94f;
                view.setScaleX(f5);
                view.setScaleY(f5);
            }
            boolean z2 = f2 != 0.0f;
            if (z2 != this.layered) {
                this.layered = z2;
                Iterator<View> it = this.parts.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    View next = it.next();
                    if (!z2) {
                        i = 0;
                    }
                    next.setLayerType(i, null);
                }
                Panel.this.content.setLayerType(z2 ? 2 : 0, null);
            }
            LinearLayout linearLayout = Panel.this.content;
            if (this.active && !z2 && fClamp >= 1.0f) {
                i2 = 4;
            }
            linearLayout.setVisibility(i2);
            if (this.active || z2 || f > 0.0f) {
                return;
            }
            setVisibility(8);
            this.chosenGrid.clear();
            this.availableGrid.clear();
            this.blockGrid.clear();
            this.hiddenGrid.clear();
        }

        private TextView section(Context context, String str, int i) {
            TextView textViewLabel = Panel.this.label(context, 14.0f, -1, 1);
            textViewLabel.setText(str);
            textViewLabel.setTypeface(Typeface.DEFAULT_BOLD);
            textViewLabel.setPadding(Panel.this.dp(4.0f), Panel.this.dp(i), 0, Panel.this.dp(10.0f));
            return textViewLabel;
        }

        private TextView hint(Context context, String str) {
            TextView textViewLabel = Panel.this.label(context, 12.0f, -1711276033, 2);
            textViewLabel.setText(str);
            textViewLabel.setPadding(Panel.this.dp(4.0f), Panel.this.dp(8.0f), 0, Panel.this.dp(8.0f));
            return textViewLabel;
        }

        private TextView pill(Context context, String str, float f, boolean z) {
            TextView textViewLabel = Panel.this.label(context, f, -1, 1);
            textViewLabel.setText(str);
            textViewLabel.setGravity(17);
            textViewLabel.setIncludeFontPadding(false);
            textViewLabel.setBackground(new Glass(-1.0f, 587202559, -83886081, 0.5f, 0.5f));
            Panel panel = Panel.this;
            panel.spring(textViewLabel, panel.litLevel, Logic.Spring.FLOOD).snap(z ? 1.0f : 0.0f);
            Panel.this.squish(textViewLabel);
            return textViewLabel;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setLook(int i) {
            this.draftLook = i;
            this.lookChoice.choose(i, true);
            Glass.look = i;
            Panel.redraw(this);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setBlur(int i) {
            this.blurChoice.choose(i, true);
            if (i == Panel.this.blur) {
                return;
            }
            Panel.this.blur = i;
            Panel.this.reblur();
        }

        void open() {
            Object obj = Hook.host;
            if (obj == null || this.active) {
                return;
            }
            this.active = true;
            this.before = new ArrayList();
            Iterator it = ((List) XposedHelpers.callMethod(obj, "getSpecs", new Object[0])).iterator();
            while (it.hasNext()) {
                this.before.add(Panel.specOf(it.next()));
            }
            this.live = new HashMap();
            for (Object obj2 : (Collection) XposedHelpers.callMethod(obj, "getTiles", new Object[0])) {
                this.live.put((String) XposedHelpers.callMethod(obj2, "getTileSpec", new Object[0]), obj2);
            }
            this.fixed.clear();
            for (String str : this.before) {
                if (Logic.kind(str) != 0 || !shows(str)) {
                    this.fixed.add(str);
                }
            }
            fill(obj, this.before, Panel.this.layout, Panel.this.places, Panel.this.pins);
            this.lookBefore = Glass.look;
            int i = Glass.look;
            this.draftLook = i;
            this.lookChoice.choose(i, false);
            this.blurBefore = Panel.this.blur;
            this.blurChoice.choose(Panel.this.blur, false);
            this.draftRows = Panel.this.tileRows;
            this.rowsChoice.choose(Panel.this.tileRows - 2, false);
            this.draftIcons = Panel.this.systemIcons;
            this.iconChoice.choose(Panel.this.systemIcons ? 1 : 0, false);
            boolean z = PageSwipe.moveRows;
            this.draftSwipe = z;
            this.swipeChoice.choose(!z ? 1 : 0, false);
            this.draftNames = Panel.this.tileNames;
            this.namesChoice.choose(!Panel.this.tileNames ? 1 : 0, false);
            this.tintBefore = Glass.tint;
            this.tintChoice.choose(Glass.tint, false);
            this.shineBefore = Glass.shine;
            this.shineChoice.choose(Glass.shine, false);
            boolean z2 = Hook.notifGlass;
            this.draftNotif = z2;
            this.notifChoice.choose(z2 ? 1 : 0, false);
            this.tab = 0;
            this.tabs.choose(0, false);
            showTab(0, false);
            this.headerAway.snap(0.0f);
            placeHeader(0.0f);
            this.scroll.scrollTo(0, 0);
            this.lookScroll.scrollTo(0, 0);
            this.page.setPadding(Panel.this.dp(24.0f), Panel.this.headerPadding, Panel.this.dp(24.0f), Panel.this.dp(16.0f));
            if (getVisibility() != 0) {
                this.shown.snap(0.0f);
            }
            setVisibility(0);
            this.shown.to(1.0f);
        }

        private void fill(Object obj, List<String> list, String str, String str2, Map<String, int[]> map) {
            this.chosenGrid.clear();
            this.availableGrid.clear();
            this.blockGrid.clear();
            this.hiddenGrid.clear();
            HashSet hashSet = new HashSet();
            ArrayList<String> arrayList = new ArrayList();
            for (String str3 : list) {
                hashSet.add(str3);
                hashSet.add(samsungSpec(str3));
                if (Logic.kind(str3) == 0 && shows(str3)) {
                    if (map.containsKey(str3)) {
                        arrayList.add(str3);
                    } else {
                        addCell(this.chosenGrid, str3);
                    }
                }
            }
            Iterator<String> it = candidates(obj).iterator();
            while (true) {
                boolean z = true;
                if (!it.hasNext()) {
                    break;
                }
                String next = it.next();
                String strSamsungSpec = samsungSpec(next);
                if (!hashSet.contains(next) && !hashSet.contains(strSamsungSpec)) {
                    z = false;
                }
                hashSet.add(next);
                hashSet.add(strSamsungSpec);
                if (!z && usable(obj, next) && shows(next)) {
                    addCell(this.availableGrid, next);
                }
            }
            int[][] iArrSizes = Logic.sizes(str2, str);
            int[][] iArrSpots = Logic.spots(str2, str, 4);
            ArrayList arrayList2 = new ArrayList();
            ArrayList arrayList3 = new ArrayList();
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) != 'g') {
                    this.blockGrid.addCell(blockCell(str.charAt(i), iArrSizes[0][i], iArrSizes[1][i]), this.blockGrid.order.size());
                    arrayList2.add(iArrSpots[i]);
                    arrayList3.add(Integer.valueOf(iArrSizes[1][i]));
                }
            }
            int size = arrayList3.size();
            int[] iArr = new int[size];
            for (int i2 = 0; i2 < size; i2++) {
                iArr[i2] = ((Integer) arrayList3.get(i2)).intValue();
            }
            this.blockGrid.spots = Logic.lift((int[][]) arrayList2.toArray(new int[0][]), iArr);
            for (String str4 : arrayList) {
                View viewPinCell = pinCell(str4);
                if (viewPinCell != null) {
                    this.blockGrid.addCell(viewPinCell, 0, map.get(str4));
                }
            }
            for (char c : "wbmlvsd".toCharArray()) {
                if (str.toLowerCase(Locale.ROOT).indexOf(c) < 0) {
                    View viewBlockCell = blockCell(c, Logic.spanW(c), Logic.spanH(c));
                    glyph(viewBlockCell, false);
                    TileGrid tileGrid = this.hiddenGrid;
                    tileGrid.addCell(viewBlockCell, tileGrid.order.size());
                }
            }
            showAddHints();
        }

        private void showAddHints() {
            int i = 8;
            this.cardsHint.setVisibility(this.hiddenGrid.order.isEmpty() ? 8 : 0);
            this.tilesHint.setVisibility(this.availableGrid.order.isEmpty() ? 8 : 0);
            TextView textView = this.allIn;
            if (this.hiddenGrid.order.isEmpty() && this.availableGrid.order.isEmpty()) {
                i = 0;
            }
            textView.setVisibility(i);
        }

        private String nameOf(char c) {
            return Panel.this.word(new String[]{"wifi", "bluetooth", "media", "brightness", "volume", "sound_modes", "mobile_data"}["wbmlvsd".indexOf(Character.toLowerCase(c))], new Object[0]);
        }

        private View blockCell(char c, int i, int i2) {
            Context context = getContext();
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setClipChildren(false);
            frameLayout.setTag("" + c + i + i2);
            frameLayout.setContentDescription(nameOf(c));
            frameLayout.addView(face(c), new FrameLayout.LayoutParams(-1, -1));
            frameLayout.addView(badge(context, frameLayout), new FrameLayout.LayoutParams(Panel.this.dp(22.0f), Panel.this.dp(22.0f), 8388659));
            addHandle(context, frameLayout);
            wire(frameLayout);
            return frameLayout;
        }

        private void addHandle(Context context, FrameLayout frameLayout) {
            ResizeHandle resizeHandle = Panel.this.new ResizeHandle(context);
            resizeHandle.setTag("handle");
            resizeHandle.setImportantForAccessibility(2);
            frameLayout.addView(resizeHandle, new FrameLayout.LayoutParams(Panel.this.dp(26.0f), Panel.this.dp(26.0f), 8388693));
            frameLayout.setAccessibilityDelegate(new View.AccessibilityDelegate() { // from class: dev.colorcc.Panel.Editor.1
                @Override // android.view.View.AccessibilityDelegate
                public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                    super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                    if (view.getParent() == Editor.this.blockGrid) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(2113929218, Panel.this.word("resize", new Object[0])));
                    }
                }

                @Override // android.view.View.AccessibilityDelegate
                public boolean performAccessibilityAction(final View view, int i, Bundle bundle) {
                    if (i == 2113929218) {
                        Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.1.0
                            @Override // java.lang.Runnable
                            public final void run() {
                                m141lambda$performAccessibilityAction$0$devcolorccPanel$Editor$1(view);
                            }
                        });
                        return true;
                    }
                    return super.performAccessibilityAction(view, i, bundle);
                }

                /* renamed from: lambda$performAccessibilityAction$0$dev-colorcc-Panel$Editor$1, reason: not valid java name */
                /* synthetic */ void m141lambda$performAccessibilityAction$0$devcolorccPanel$Editor$1(View view) {
                    Editor.this.resize(view);
                }
            });
        }

        private View face(char c) {
            Context context = getContext();
            char lowerCase = Character.toLowerCase(c);
            if (lowerCase == 'l' || lowerCase == 'v') {
                return Panel.this.new SliderFace(context, lowerCase == 'l');
            }
            if (lowerCase == 'm') {
                return Panel.this.new MediaFace(context);
            }
            Panel panel = Panel.this;
            final Disc disc = panel.new Disc(context, panel.dp(16.0f));
            disc.setClickable(false);
            if (lowerCase == 's') {
                disc.icon.setImageDrawable(Icons.of("M18,16v-5c0,-3.07 -1.63,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.64,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2zM12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2z"));
                disc.level.snap(1.0f);
            } else {
                int i = lowerCase != 'w' ? lowerCase == 'b' ? 2 : 3 : 1;
                Iterator<Map.Entry<String, Object>> it = this.live.entrySet().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Map.Entry<String, Object> next = it.next();
                    if (Logic.kind(next.getKey()) == i) {
                        disc.style(next.getKey());
                        try {
                            this.bindings.add(Panel.this.new Bound(next.getValue(), new Consumer() { // from class: dev.colorcc.Panel.Editor.5
                                @Override // java.util.function.Consumer
                                public final void accept(Object obj) {
                                    disc.render(obj, false);
                                }
                            }));
                            break;
                        } catch (ClassNotFoundException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                }
            }
            return Panel.this.new CardFace(context, disc, nameOf(c));
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void resize(View view) {
            int[] iArrNextSize = Logic.nextSize(((String) view.getTag()).charAt(0), ((String) view.getTag()).charAt(1) - '0', ((String) view.getTag()).charAt(2) - '0');
            setSize(view, iArrNextSize[0], iArrNextSize[1]);
        }

        void setSize(View view, int i, int i2) {
            String str = (String) view.getTag();
            boolean z = str.charAt(1) + 65488 == i && str.charAt(2) + 65488 == i2;
            float width = view.getWidth() * view.getScaleX();
            float height = view.getHeight() * view.getScaleY();
            view.setTag("" + str.charAt(0) + i + i2);
            if (!z) {
                growFrom(view, width, height);
            }
            this.blockGrid.settle(view);
        }

        private void growFrom(View view, final float f, final float f2) {
            Object tag = view.getTag(2113929220);
            if (tag instanceof View.OnLayoutChangeListener) {
                view.removeOnLayoutChangeListener((View.OnLayoutChangeListener) tag);
            }
            final int width = view.getWidth();
            final int height = view.getHeight();
            final Object obj = new Object();
            view.setTag(2113929221, obj);
            View.OnLayoutChangeListener onLayoutChangeListener = new View.OnLayoutChangeListener() { // from class: dev.colorcc.Panel.Editor.2
                @Override // android.view.View.OnLayoutChangeListener
                public void onLayoutChange(final View view2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                    if (i3 - i == width && i4 - i2 == height) {
                        return;
                    }
                    view2.removeOnLayoutChangeListener(this);
                    view2.setTag(2113929220, null);
                    view2.setPivotX(0.0f);
                    view2.setPivotY(0.0f);
                    Panel.this.spring(view2, View.SCALE_X, Logic.Spring.BOUNCY).snap(f / Math.max(1, i3 - i));
                    Panel.this.spring(view2, View.SCALE_Y, Logic.Spring.BOUNCY).snap(f2 / Math.max(1, i4 - i2));
                    Panel.this.scaleTo(view2, 1.0f, Logic.Spring.BOUNCY);
                    final Object obj2 = obj;
                    view2.postDelayed(new Runnable() { // from class: dev.colorcc.Panel.Editor.2.0
                        @Override // java.lang.Runnable
                        public final void run() {
                            lambda$onLayoutChange$0(view2, obj2);
                        }
                    }, 600L);
                }

                static /* synthetic */ void lambda$onLayoutChange$0(View view2, Object obj2) {
                    if (view2.getTag(2113929221) != obj2) {
                        return;
                    }
                    view2.setPivotX(view2.getWidth() / 2.0f);
                    view2.setPivotY(view2.getHeight() / 2.0f);
                }
            };
            view.setTag(2113929220, onLayoutChangeListener);
            view.addOnLayoutChangeListener(onLayoutChangeListener);
        }

        private TextView badge(Context context, final View view) {
            TextView textViewLabel = Panel.this.label(context, 16.0f, -1, 1);
            textViewLabel.setTag("badge");
            textViewLabel.setText("−");
            textViewLabel.setGravity(17);
            textViewLabel.setIncludeFontPadding(false);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(1);
            gradientDrawable.setColor(-263172008);
            gradientDrawable.setStroke(Panel.this.dp(1.0f), 1090519039);
            textViewLabel.setBackground(gradientDrawable);
            textViewLabel.setContentDescription(Panel.this.word("remove", new Object[0]));
            textViewLabel.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Editor.50
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    Editor.this.m105lambda$badge$20$devcolorccPanel$Editor(view, view2);
                }
            });
            return textViewLabel;
        }

        /* renamed from: lambda$badge$20$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m105lambda$badge$20$devcolorccPanel$Editor(final View view, View view2) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.49
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m104lambda$badge$19$devcolorccPanel$Editor(view);
                }
            });
        }

        private void glyph(View view, boolean z) {
            TextView textView = (TextView) view.findViewWithTag("badge");
            if (textView == null) {
                return;
            }
            View view2 = textView;
            while (true) {
                if (view2 == view) {
                    break;
                }
                ViewGroup viewGroup = (ViewGroup) view2.getParent();
                int i = 0;
                while (i < viewGroup.getChildCount()) {
                    if (viewGroup.getChildAt(i) != view2) {
                        viewGroup.getChildAt(i).setAlpha(z ? 1.0f : 0.4f);
                    }
                    i++;
                }
                view2 = (View) view2.getParent();
            }
            textView.setText(z ? "−" : "+");
            textView.setContentDescription(Panel.this.word(z ? "remove" : "add", new Object[0]));
            ((GradientDrawable) textView.getBackground()).setColor(z ? -263172008 : -15374872);
            View viewFindViewWithTag = view.findViewWithTag("handle");
            if (viewFindViewWithTag != null) {
                viewFindViewWithTag.setVisibility(z ? 0 : 8);
            }
        }

        private void wire(View view) {
            view.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Editor.47
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    Editor.this.m140lambda$wire$22$devcolorccPanel$Editor(view2);
                }
            });
            view.setOnTouchListener(new View.OnTouchListener() { // from class: dev.colorcc.Panel.Editor.48
                @Override // android.view.View.OnTouchListener
                public final boolean onTouch(View view2, MotionEvent motionEvent) {
                    return Editor.lambda$wire$23(view2, motionEvent);
                }
            });
        }

        /* renamed from: lambda$wire$22$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m140lambda$wire$22$devcolorccPanel$Editor(final View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.57
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m139lambda$wire$21$devcolorccPanel$Editor(view);
                }
            });
        }

        /* renamed from: lambda$wire$21$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m139lambda$wire$21$devcolorccPanel$Editor(View view) {
            ViewParent parent = view.getParent();
            if (parent == this.hiddenGrid || parent == this.availableGrid) {
                m104lambda$badge$19$devcolorccPanel$Editor(view);
            }
        }

        static /* synthetic */ boolean lambda$wire$23(View view, MotionEvent motionEvent) {
            ViewParent parent = view.getParent();
            return (parent instanceof TileGrid) && ((TileGrid) parent).drag(view, motionEvent);
        }

        private List<String> candidates(Object obj) {
            List<String> listDefaults = defaults(obj);
            List<String> listSpecList = specList("sec_quick_settings_tiles_stock");
            if (listSpecList.isEmpty()) {
                listSpecList = specList("quick_settings_tiles_stock");
            }
            listDefaults.addAll(listSpecList);
            listDefaults.addAll(appTiles());
            return listDefaults;
        }

        private List<String> appTiles() {
            final ArrayList arrayList = new ArrayList();
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.46
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m103lambda$appTiles$24$devcolorccPanel$Editor(arrayList);
                }
            });
            return arrayList;
        }

        /* renamed from: lambda$appTiles$24$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m103lambda$appTiles$24$devcolorccPanel$Editor(List list) {
            Iterator<ResolveInfo> it = getContext().getPackageManager().queryIntentServices(new Intent("android.service.quicksettings.action.QS_TILE"), 128).iterator();
            while (it.hasNext()) {
                ServiceInfo serviceInfo = it.next().serviceInfo;
                if ("android.permission.BIND_QUICK_SETTINGS_TILE".equals(serviceInfo.permission) && (serviceInfo.metaData == null || !serviceInfo.metaData.getBoolean("android.service.quicksettings.SEM_DEFAULT_TILE_DEXMODE_ONLY"))) {
                    list.add("custom(" + new ComponentName(serviceInfo.packageName, serviceInfo.name).flattenToShortString() + ")");
                }
            }
        }

        private String samsungSpec(final String str) {
            Object objQuiet = quiet(new Callable() { // from class: dev.colorcc.Panel.Editor.8
                @Override // java.util.concurrent.Callable
                public final Object call() throws Exception {
                    return Editor.this.m138lambda$samsungSpec$25$devcolorccPanel$Editor(str);
                }
            });
            return objQuiet instanceof String ? (String) objQuiet : str;
        }

        /* renamed from: lambda$samsungSpec$25$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ Object m138lambda$samsungSpec$25$devcolorccPanel$Editor(String str) throws Exception {
            return XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.android.systemui.qs.pipeline.data.repository.TileNameConverter", getContext().getClassLoader()), "toTileSpec", new Object[]{getResources(), str});
        }

        private List<String> defaults(final Object obj) {
            final ArrayList arrayList = new ArrayList();
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.52
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.lambda$defaults$26(obj, arrayList);
                }
            });
            if (arrayList.isEmpty()) {
                arrayList.addAll(specList("sec_quick_settings_tiles_default"));
            }
            if (arrayList.isEmpty()) {
                // [Miku-Picked UI] AOSP: QSHost.getDefaultSpecs, the same list SystemUI resets to.
                arrayList.addAll(specList("quick_settings_tiles_new_default"));
            }
            return arrayList;
        }

        static /* synthetic */ void lambda$defaults$26(Object obj, List list) {
            Iterator it = ((List) XposedHelpers.callMethod(obj, "getDefaultTileList", new Object[0])).iterator();
            while (it.hasNext()) {
                list.add(Panel.specOf(it.next()));
            }
        }

        private List<String> specList(String str) {
            ArrayList arrayList = new ArrayList();
            int identifier = getResources().getIdentifier(str, "string", "com.android.systemui");
            if (identifier != 0) {
                for (String str2 : getResources().getString(identifier).split(",")) {
                    arrayList.add(str2.trim());
                }
            }
            return arrayList;
        }

        /* [Miku-Picked UI] Rebuilt from the bytecode (jadx could not decompile it). */
        private boolean shows(final String str) {
            if (str.startsWith("custom(") && str.endsWith(")")) {
                ComponentName componentNameUnflattenFromString = ComponentName.unflattenFromString(str.substring(7, str.length() - 1));
                if (componentNameUnflattenFromString == null) {
                    return false;
                }
                try {
                    getContext().getPackageManager().getServiceInfo(componentNameUnflattenFromString, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    return false;
                }
                if (Boolean.FALSE.equals(quiet(new Callable() {
                    @Override
                    public final Object call() throws Exception {
                        return Editor.lambda$shows$27(str);
                    }
                }))) {
                    return false;
                }
            }
            final Object objTileFor = tileFor(str);
            return objTileFor != null && !Boolean.FALSE.equals(quiet(new Callable() {
                @Override
                public final Object call() throws Exception {
                    return Editor.lambda$shows$28(objTileFor);
                }
            }));
        }

        static /* synthetic */ Object lambda$shows$27(String str) throws Exception {
            return XposedHelpers.callMethod(Hook.host, "isAvailableCustomTile", new Object[]{str});
        }

        static /* synthetic */ Object lambda$shows$28(Object obj) throws Exception {
            return XposedHelpers.callMethod(obj, "isAvailable", new Object[0]);
        }

        private boolean usable(final Object obj, final String str) {
            return (str.isEmpty() || Logic.kind(str) != 0 || Boolean.TRUE.equals(quiet(new Callable() { // from class: dev.colorcc.Panel.Editor.6
                @Override // java.util.concurrent.Callable
                public final Object call() throws Exception {
                    return Editor.lambda$usable$29(obj, str);
                }
            })) || Boolean.TRUE.equals(quiet(new Callable() { // from class: dev.colorcc.Panel.Editor.7
                @Override // java.util.concurrent.Callable
                public final Object call() throws Exception {
                    return Editor.lambda$usable$30(obj, str);
                }
            }))) ? false : true;
        }

        static /* synthetic */ Object lambda$usable$29(Object obj, String str) throws Exception {
            return XposedHelpers.callMethod(obj, "isUnsupportedTile", new Object[]{str});
        }

        static /* synthetic */ Object lambda$usable$30(Object obj, String str) throws Exception {
            return XposedHelpers.callMethod(obj, "shouldBeHiddenByKnox", new Object[]{str});
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* renamed from: reset, reason: merged with bridge method [inline-methods] */
        public void m133lambda$new$5$devcolorccPanel$Editor() {
            Object obj = Hook.host;
            if (obj == null || !this.active) {
                return;
            }
            List<String> arrayList = new ArrayList<>(this.fixed);
            for (String str : defaults(obj)) {
                if (!arrayList.contains(str) && usable(obj, str)) {
                    arrayList.add(str);
                }
            }
            if (arrayList.size() == this.fixed.size()) {
                arrayList = this.before;
            }
            List<String> list = arrayList;
            String[] strArrWithTiles = Logic.withTiles("wbmlvsd", null);
            fill(obj, list, strArrWithTiles[0], strArrWithTiles[1], new HashMap());
            setLook(0);
            setBlur(1);
            this.draftRows = 3;
            this.rowsChoice.choose(1, true);
            this.draftIcons = false;
            this.iconChoice.choose(0, true);
            this.draftNames = true;
            this.namesChoice.choose(0, true);
            this.draftSwipe = true;
            this.swipeChoice.choose(0, true);
            Glass.shine = 1;
            Glass.tint = 1;
            this.tintChoice.choose(1, true);
            this.shineChoice.choose(1, true);
            this.draftNotif = false;
            this.notifChoice.choose(0, true);
            Panel.redraw(this);
            this.scroll.smoothScrollTo(0, 0);
        }

        private Object quiet(Callable<Object> callable) {
            try {
                return callable.call();
            } catch (Throwable unused) {
                return null;
            }
        }

        private void addCell(TileGrid tileGrid, String str) {
            addCell(tileGrid, str, tileGrid.order.size());
        }

        private Object tileFor(final String str) {
            Object obj = this.live.get(str);
            if (obj != null) {
                return obj;
            }
            final Object objQuiet = quiet(new Callable() { // from class: dev.colorcc.Panel.Editor.41
                @Override // java.util.concurrent.Callable
                public final Object call() throws Exception {
                    return Editor.lambda$tileFor$31(str);
                }
            });
            if (objQuiet == null) {
                return null;
            }
            this.created.add(objQuiet);
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.42
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.lambda$tileFor$32(objQuiet);
                }
            });
            return objQuiet;
        }

        static /* synthetic */ Object lambda$tileFor$31(String str) throws Exception {
            return XposedHelpers.callMethod(Hook.host, "createTile", new Object[]{str});
        }

        static /* synthetic */ void lambda$tileFor$32(Object obj) {
            XposedHelpers.callMethod(obj, "refreshState", new Object[0]);
        }

        private void addCell(TileGrid tileGrid, String str, int i) {
            Object objTileFor = tileFor(str);
            if (objTileFor == null) {
                return;
            }
            Context context = getContext();
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setClipChildren(false);
            frameLayout.setTag(str);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            linearLayout.setGravity(1);
            linearLayout.setClipChildren(false);
            FrameLayout frameLayout2 = new FrameLayout(context);
            frameLayout2.setClipChildren(false);
            Panel panel = Panel.this;
            final Disc disc = panel.new Disc(context, panel.dp(26.0f));
            disc.setClickable(false);
            disc.style(str);
            frameLayout2.addView(disc, new FrameLayout.LayoutParams(-1, -1));
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Panel.this.dp(22.0f), Panel.this.dp(22.0f), 8388659);
            layoutParams.leftMargin = -Panel.this.dp(3.0f);
            layoutParams.topMargin = -Panel.this.dp(3.0f);
            frameLayout2.addView(badge(context, frameLayout), layoutParams);
            linearLayout.addView(frameLayout2, new LinearLayout.LayoutParams(Panel.this.dp(63.0f), Panel.this.dp(63.0f)));
            final TextView textViewLabel = Panel.this.label(context, 10.0f, -1, 2);
            textViewLabel.setGravity(1);
            textViewLabel.setIncludeFontPadding(false);
            textViewLabel.setText(str);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams2.topMargin = Panel.this.dp(3.0f);
            linearLayout.addView(textViewLabel, layoutParams2);
            frameLayout.addView(linearLayout, new FrameLayout.LayoutParams(-1, -1));
            try {
                this.bindings.add(Panel.this.new Bound(objTileFor, new Consumer() { // from class: dev.colorcc.Panel.Editor.22
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        Editor.lambda$addCell$33(disc, textViewLabel, obj);
                    }
                }));
                wire(frameLayout);
                glyph(frameLayout, tileGrid.reorder);
                tileGrid.addCell(frameLayout, i);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        static /* synthetic */ void lambda$addCell$33(Disc disc, TextView textView, Object obj) {
            disc.render(obj, false);
            CharSequence charSequenceText = Panel.text(obj, "label");
            if (TextUtils.isEmpty(charSequenceText)) {
                return;
            }
            Panel.setTextIfChanged(textView, charSequenceText);
        }

        private View pinCell(String str) {
            Object objTileFor = tileFor(str);
            if (objTileFor == null) {
                return null;
            }
            Context context = getContext();
            final FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setClipChildren(false);
            int[] iArrPinSize = Logic.pinSize((int[]) Panel.this.pins.get(str));
            frameLayout.setTag("p" + iArrPinSize[0] + iArrPinSize[1]);
            frameLayout.setTag(2113929219, str);
            Panel panel = Panel.this;
            final Disc disc = panel.new Disc(context, panel.dp(16.0f));
            disc.setClickable(false);
            disc.style(str);
            final CardFace cardFace = Panel.this.new CardFace(context, disc, "");
            cardFace.setBackground(null);
            cardFace.fill = true;
            frameLayout.addView(cardFace, new FrameLayout.LayoutParams(-1, -1));
            frameLayout.addView(badge(context, frameLayout), new FrameLayout.LayoutParams(Panel.this.dp(22.0f), Panel.this.dp(22.0f), 8388659));
            addHandle(context, frameLayout);
            try {
                this.bindings.add(Panel.this.new Bound(objTileFor, new Consumer() { // from class: dev.colorcc.Panel.Editor.10
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        Editor.lambda$pinCell$34(disc, frameLayout, cardFace, obj);
                    }
                }));
                wire(frameLayout);
                return frameLayout;
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        static /* synthetic */ void lambda$pinCell$34(Disc disc, FrameLayout frameLayout, CardFace cardFace, Object obj) {
            disc.render(obj, false);
            frameLayout.setContentDescription(Panel.text(obj, "label"));
            CharSequence charSequenceText = Panel.text(obj, "label");
            if (TextUtils.isEmpty(charSequenceText)) {
                return;
            }
            Panel.setTextIfChanged(cardFace.name, charSequenceText);
        }

        private boolean isPin(View view) {
            return view.getTag(2113929219) != null;
        }

        boolean overBlocks(float f) {
            int[] iArr = new int[2];
            this.blockGrid.getLocationOnScreen(iArr);
            return f < ((float) (iArr[1] + this.blockGrid.getHeight()));
        }

        boolean drop(final TileGrid tileGrid, final View view, float f, final float f2) {
            final Runnable runnable;
            Editor editor;
            final float[] fArr = {f, f2};
            if ((tileGrid == this.chosenGrid || tileGrid == this.availableGrid) && overBlocks(f2)) {
                runnable = new Runnable() { // from class: dev.colorcc.Panel.Editor.53
                    @Override // java.lang.Runnable
                    public final void run() {
                        Editor.this.m115lambda$drop$35$devcolorccPanel$Editor(view, fArr);
                    }
                };
                editor = this;
            } else if (tileGrid == this.blockGrid && isPin(view)) {
                if (overBlocks(f2)) {
                    editor = this;
                    runnable = null;
                } else {
                    editor = this;
                    runnable = new Runnable() { // from class: dev.colorcc.Panel.Editor.54
                        @Override // java.lang.Runnable
                        public final void run() {
                            Editor.this.m116lambda$drop$36$devcolorccPanel$Editor(view, tileGrid, f2, fArr);
                        }
                    };
                }
            } else {
                editor = this;
                runnable = editor.across(tileGrid, f2) ? new Runnable() { // from class: dev.colorcc.Panel.Editor.55
                    @Override // java.lang.Runnable
                    public final void run() {
                        Editor.this.m117lambda$drop$37$devcolorccPanel$Editor(view, fArr);
                    }
                } : null;
            }
            if (runnable == null) {
                return false;
            }
            editor.post(new Runnable() { // from class: dev.colorcc.Panel.Editor.56
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m118lambda$drop$38$devcolorccPanel$Editor(runnable);
                }
            });
            return true;
        }

        /* renamed from: lambda$drop$36$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m116lambda$drop$36$devcolorccPanel$Editor(View view, TileGrid tileGrid, float f, float[] fArr) {
            unpin(view, across(tileGrid, f) ? this.availableGrid : this.chosenGrid, fArr);
        }

        /* renamed from: lambda$drop$38$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m118lambda$drop$38$devcolorccPanel$Editor(Runnable runnable) {
            Panel.this.guard(runnable);
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* renamed from: pin, reason: merged with bridge method [inline-methods] */
        public void m115lambda$drop$35$devcolorccPanel$Editor(View view, float[] fArr) {
            View viewPinCell;
            if ((view.getParent() instanceof TileGrid) && (viewPinCell = pinCell((String) view.getTag())) != null) {
                ((TileGrid) view.getParent()).removeCell(view);
                glyph(viewPinCell, true);
                int[] iArrCellAt = this.blockGrid.cellAt(fArr[0], fArr[1]);
                this.blockGrid.addCell(viewPinCell, 0, iArrCellAt);
                this.blockGrid.placeAt(viewPinCell, iArrCellAt);
                showAddHints();
                popIn(viewPinCell);
            }
        }

        private void unpin(View view, TileGrid tileGrid, float[] fArr) {
            if (view.getParent() instanceof TileGrid) {
                String str = (String) view.getTag(2113929219);
                this.blockGrid.removeCell(view);
                int[] iArrCellAt = (fArr == null || !tileGrid.reorder) ? null : tileGrid.cellAt(fArr[0], fArr[1]);
                int iMin = Math.min(tileGrid.order.size(), iArrCellAt == null ? tileGrid.order.size() : (iArrCellAt[1] * tileGrid.columns) + iArrCellAt[0]);
                addCell(tileGrid, str, iMin);
                showAddHints();
                if (iMin < tileGrid.order.size()) {
                    popIn(tileGrid.order.get(iMin));
                }
            }
        }

        private void popIn(View view) {
            Panel.this.spring(view, View.SCALE_X, Logic.Spring.BOUNCY).snap(0.7f);
            Panel.this.spring(view, View.SCALE_Y, Logic.Spring.BOUNCY).snap(0.7f);
            Panel.this.scaleTo(view, 1.0f, Logic.Spring.BOUNCY);
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* renamed from: move, reason: merged with bridge method [inline-methods] */
        public void m104lambda$badge$19$devcolorccPanel$Editor(View view) {
            m117lambda$drop$37$devcolorccPanel$Editor(view, null);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: move, reason: merged with bridge method [inline-methods] */
        public void m117lambda$drop$37$devcolorccPanel$Editor(View view, float[] fArr) {
            if (view.getParent() instanceof TileGrid) {
                if (isPin(view)) {
                    unpin(view, this.availableGrid, null);
                    return;
                }
                TileGrid tileGrid = (TileGrid) view.getParent();
                TileGrid tileGrid2 = this.chosenGrid;
                TileGrid tileGrid3 = this.availableGrid;
                if (tileGrid == tileGrid2) {
                    tileGrid2 = tileGrid3;
                } else if (tileGrid != tileGrid3 && tileGrid == (tileGrid2 = this.blockGrid)) {
                    tileGrid2 = this.hiddenGrid;
                }
                int[] at = new int[2];
                view.getLocationOnScreen(at);
                int size = 0;
                final float width = at[0] + ((view.getWidth() * view.getScaleX()) / 2.0f);
                final float height = at[1] + ((view.getHeight() * view.getScaleY()) / 2.0f);
                tileGrid.removeCell(view);
                int[] iArrCellAt = fArr != null ? tileGrid2.cellAt(fArr[0], fArr[1]) : null;
                if (iArrCellAt != null) {
                    size = iArrCellAt[0] + (iArrCellAt[1] * tileGrid2.columns);
                } else if (tileGrid2.reorder) {
                    size = tileGrid2.order.size();
                }
                tileGrid2.addCell(view, size, iArrCellAt);
                glyph(view, tileGrid2.reorder);
                showAddHints();
                tileGrid.setTranslationZ(0.0f);
                tileGrid2.setTranslationZ(Panel.this.dp(1.0f));
                view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: dev.colorcc.Panel.Editor.3
                    @Override // android.view.View.OnLayoutChangeListener
                    public void onLayoutChange(View view2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                        if (i3 - i == 0) {
                            return;
                        }
                        view2.removeOnLayoutChangeListener(this);
                        int[] at2 = new int[2];
                        view2.getLocationOnScreen(at2);
                        float width2 = at2[0] + ((view2.getWidth() * view2.getScaleX()) / 2.0f);
                        float height2 = at2[1] + ((view2.getHeight() * view2.getScaleY()) / 2.0f);
                        Motion.Value valueSpring = Panel.this.spring(view2, View.TRANSLATION_X, Logic.Spring.REVEAL);
                        Motion.Value valueSpring2 = Panel.this.spring(view2, View.TRANSLATION_Y, Logic.Spring.REVEAL);
                        valueSpring.snap((width - width2) + view2.getTranslationX());
                        valueSpring2.snap((height - height2) + view2.getTranslationY());
                        valueSpring.to(0.0f);
                        valueSpring2.to(0.0f);
                    }
                });
                Panel.this.spring(view, View.SCALE_X, Logic.Spring.BOUNCY).snap(0.7f);
                Panel.this.spring(view, View.SCALE_Y, Logic.Spring.BOUNCY).snap(0.7f);
                Panel.this.scaleTo(view, 1.0f, Logic.Spring.BOUNCY);
            }
        }

        boolean across(TileGrid tileGrid, float f) {
            int[] iArr = new int[2];
            this.chosenGrid.getLocationOnScreen(iArr);
            return (tileGrid == this.blockGrid || tileGrid == this.chosenGrid) == ((f > ((float) ((iArr[1] + this.chosenGrid.getHeight()) + Panel.this.dp(12.0f))) ? 1 : (f == ((float) ((iArr[1] + this.chosenGrid.getHeight()) + Panel.this.dp(12.0f))) ? 0 : -1)) > 0);
        }

        /* renamed from: lambda$new$39$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m131lambda$new$39$devcolorccPanel$Editor() {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.0
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.stepEdge();
                }
            });
        }

        void edgeScroll(TileGrid tileGrid, View view, float f, float f2) {
            this.edgeGrid = tileGrid;
            this.edgeCell = view;
            this.edgeX = f;
            this.edgeY = f2;
            removeCallbacks(this.edgeStep);
            if (tileGrid != null) {
                postOnAnimation(this.edgeStep);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void stepEdge() {
            if (this.edgeGrid == null) {
                return;
            }
            int[] iArr = new int[2];
            this.scroll.getLocationOnScreen(iArr);
            float fDp = Panel.this.dp(72.0f);
            float f = this.edgeY - iArr[1];
            float height = (iArr[1] + this.scroll.getHeight()) - this.edgeY;
            float f2 = f < fDp ? -(1.0f - (f / fDp)) : height < fDp ? 1.0f - (height / fDp) : 0.0f;
            int scrollY = this.scroll.getScrollY();
            this.scroll.scrollBy(0, Math.round(Math.max(-1.5f, Math.min(1.5f, f2)) * Panel.this.dp(14.0f)));
            if (this.scroll.getScrollY() == scrollY) {
                return;
            }
            this.edgeGrid.follow(this.edgeCell, this.edgeX, this.edgeY);
            postOnAnimation(this.edgeStep);
        }

        void close(boolean z) {
            if (this.active) {
                this.active = false;
                edgeScroll(null, null, 0.0f, 0.0f);
                try {
                    if (z) {
                        if (this.draftLook != this.lookBefore) {
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.11
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m106lambda$close$40$devcolorccPanel$Editor();
                                }
                            });
                        }
                        if (this.draftRows != Panel.this.tileRows) {
                            Panel.this.tileRows = this.draftRows;
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.13
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m107lambda$close$41$devcolorccPanel$Editor();
                                }
                            });
                            Panel.this.currentTiles = new ArrayList();
                        }
                        if (this.draftSwipe != PageSwipe.moveRows) {
                            PageSwipe.moveRows = this.draftSwipe;
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.14
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m108lambda$close$42$devcolorccPanel$Editor();
                                }
                            });
                        }
                        if (this.draftNames != Panel.this.tileNames) {
                            Panel.this.tileNames = this.draftNames;
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.15
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m109lambda$close$43$devcolorccPanel$Editor();
                                }
                            });
                            Panel.this.currentTiles = new ArrayList();
                        }
                        if (this.draftIcons != Panel.this.systemIcons) {
                            Panel.this.systemIcons = this.draftIcons;
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.16
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m110lambda$close$44$devcolorccPanel$Editor();
                                }
                            });
                            Panel.this.currentTiles = new ArrayList();
                        }
                        if (Panel.this.blur != this.blurBefore) {
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.17
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m111lambda$close$45$devcolorccPanel$Editor();
                                }
                            });
                        }
                        if (Glass.tint != this.tintBefore || Glass.shine != this.shineBefore) {
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.18
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m112lambda$close$46$devcolorccPanel$Editor();
                                }
                            });
                        }
                        if (this.draftNotif != Hook.notifGlass) {
                            Hook.notifGlass = this.draftNotif;
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.19
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m113lambda$close$47$devcolorccPanel$Editor();
                                }
                            });
                        }
                        Panel.this.redrawNotifications();
                        StringBuilder sb = new StringBuilder();
                        ArrayList arrayList = new ArrayList();
                        ArrayList arrayList2 = new ArrayList();
                        LinkedHashMap linkedHashMap = new LinkedHashMap();
                        this.blockGrid.size();
                        for (int i = 0; i < this.blockGrid.order.size(); i++) {
                            View view = this.blockGrid.order.get(i);
                            if (isPin(view)) {
                                int[] iArr = this.blockGrid.spots[i];
                                int i2 = this.blockGrid.sizes[0][i];
                                int i3 = this.blockGrid.sizes[1][i];
                                String str = (String) view.getTag(2113929219);
                                if (i2 != 1 || i3 != 1) {
                                    iArr = new int[]{iArr[0], iArr[1], i2, i3};
                                }
                                linkedHashMap.put(str, iArr);
                            } else {
                                sb.append(((String) view.getTag()).charAt(0));
                                arrayList.add(this.blockGrid.spots[i]);
                                arrayList2.add(new int[]{this.blockGrid.sizes[0][i], this.blockGrid.sizes[1][i]});
                            }
                        }
                        int[][] iArr2 = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 2, arrayList2.size());
                        for (int i4 = 0; i4 < arrayList2.size(); i4++) {
                            iArr2[0][i4] = ((int[]) arrayList2.get(i4))[0];
                            iArr2[1][i4] = ((int[]) arrayList2.get(i4))[1];
                        }
                        int iRows = Logic.rows((int[][]) arrayList.toArray(new int[0][]), iArr2[1]);
                        for (int[] iArr3 : (java.util.Collection<int[]>) linkedHashMap.values()) {
                            iRows = Math.max(iRows, iArr3[1] + Logic.pinSize(iArr3)[1]);
                        }
                        sb.append('g');
                        arrayList.add(new int[]{0, iRows});
                        int[] iArr4 = iArr2[0];
                        int[] iArrCopyOf = Arrays.copyOf(iArr4, iArr4.length + 1);
                        int[] iArr5 = iArr2[1];
                        int[][] iArr6 = {iArrCopyOf, Arrays.copyOf(iArr5, iArr5.length + 1)};
                        int[] iArr7 = iArr6[0];
                        iArr7[iArr7.length - 1] = Logic.spanW('g');
                        int[] iArr8 = iArr6[1];
                        iArr8[iArr8.length - 1] = Logic.spanH('g');
                        String strLayout = Logic.layout(sb.toString());
                        String strEncode = Logic.encode((int[][]) arrayList.toArray(new int[0][]), iArr6);
                        String strEncodePins = Logic.encodePins(Panel.this.pins);
                        final String strEncodePins2 = Logic.encodePins(linkedHashMap);
                        if (!strLayout.equals(Panel.this.layout) || !strEncode.equals(Panel.this.places) || !strEncodePins2.equals(strEncodePins)) {
                            Panel.this.layout = strLayout;
                            Panel.this.places = strEncode;
                            Panel.this.pins.clear();
                            Panel.this.pins.putAll(linkedHashMap);
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.20
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m114lambda$close$48$devcolorccPanel$Editor(strEncodePins2);
                                }
                            });
                            if (!strEncodePins2.equals(strEncodePins)) {
                                Panel.this.currentTiles = new ArrayList();
                            }
                            Panel.this.blocks.arrange(Panel.this.layout);
                        }
                    } else {
                        int i5 = Panel.this.blur;
                        int i6 = this.blurBefore;
                        if (i5 != i6) {
                            Panel.this.blur = i6;
                            Panel.this.reblur();
                        }
                        Glass.tint = this.tintBefore;
                        Glass.shine = this.shineBefore;
                    }
                    Glass.look = z ? this.draftLook : this.lookBefore;
                    Panel.redraw(Panel.this);
                    if (z && Hook.host != null) {
                        ArrayList arrayList3 = new ArrayList(this.fixed);
                        Iterator<View> it = this.chosenGrid.order.iterator();
                        while (it.hasNext()) {
                            arrayList3.add((String) it.next().getTag());
                        }
                        for (View view2 : this.blockGrid.order) {
                            if (isPin(view2)) {
                                arrayList3.add((String) view2.getTag(2113929219));
                            }
                        }
                        if (Panel.this.currentTiles.isEmpty() && Panel.this.syncTiles() && Panel.this.open) {
                            Panel.this.setTilesListening();
                        }
                        if (!arrayList3.equals(this.before)) {
                            XposedHelpers.callMethod(Hook.host, "changeTilesByUser", new Object[]{new ArrayList(this.before), arrayList3});
                            Hook.log("editor done: " + this.before.size() + " tiles now " + arrayList3.size());
                            Panel.this.followTiles(0);
                        }
                    }
                } catch (Throwable th) {
                    Hook.log("editor not saved: " + th);
                }
                for (final Bound bound : this.bindings) {
                    Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.21
                        @Override // java.lang.Runnable
                        public final void run() {
                            Editor.lambda$close$49(bound);
                        }
                    });
                }
                this.bindings.clear();
                for (final Object obj : this.created) {
                    Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.12
                        @Override // java.lang.Runnable
                        public final void run() {
                            Editor.lambda$close$50(obj);
                        }
                    });
                }
                this.created.clear();
                this.live = new HashMap();
                this.shown.to(0.0f);
            }
        }

        /* renamed from: lambda$close$40$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m106lambda$close$40$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putInt("look", this.draftLook).apply();
        }

        /* renamed from: lambda$close$41$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m107lambda$close$41$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putInt("tileRows", Panel.this.tileRows).apply();
        }

        /* renamed from: lambda$close$42$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m108lambda$close$42$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putBoolean("notifSwipe", this.draftSwipe).apply();
        }

        /* renamed from: lambda$close$43$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m109lambda$close$43$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putBoolean("tileNames", Panel.this.tileNames).apply();
        }

        /* renamed from: lambda$close$44$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m110lambda$close$44$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putBoolean("systemIcons", Panel.this.systemIcons).apply();
        }

        /* renamed from: lambda$close$45$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m111lambda$close$45$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putInt("blur", Panel.this.blur).apply();
        }

        /* renamed from: lambda$close$46$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m112lambda$close$46$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putInt("glassTint", Glass.tint).putInt("edgeShine", Glass.shine).apply();
        }

        /* renamed from: lambda$close$47$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m113lambda$close$47$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putBoolean("notifGlass", this.draftNotif).apply();
        }

        /* renamed from: lambda$close$48$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m114lambda$close$48$devcolorccPanel$Editor(String str) {
            Panel.this.prefs().edit().putString("layout", Panel.this.layout).putString("places", Panel.this.places).putString("pins", str).apply();
        }

        static /* synthetic */ void lambda$close$49(Bound bound) {
            XposedHelpers.callMethod(bound.tile, "removeCallback", new Object[]{bound.callback});
        }

        static /* synthetic */ void lambda$close$50(Object obj) {
            XposedHelpers.callMethod(obj, "destroy", new Object[0]);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void followTiles(final int i) {
        this.main.postDelayed(new Runnable() { // from class: dev.colorcc.Panel.37
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m10lambda$followTiles$89$devcolorccPanel(i);
            }
        }, 200L);
    }

    /* renamed from: lambda$followTiles$89$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m10lambda$followTiles$89$devcolorccPanel(final int i) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.19
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m9lambda$followTiles$88$devcolorccPanel(i);
            }
        });
    }

    /* renamed from: lambda$followTiles$88$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m9lambda$followTiles$88$devcolorccPanel(int i) {
        if (!syncTiles()) {
            if (i < 20) {
                followTiles(i + 1);
                return;
            } else {
                Hook.log("tiles not changed by Samsung");
                return;
            }
        }
        if (this.open) {
            setTilesListening();
        }
        Hook.log("tiles followed after " + ((i + 1) * 200) + " ms");
    }

    private final class CardFace extends ViewGroup {
        private final Glass back;
        private final Disc disc;
        boolean fill;
        private final TextView name;

        CardFace(Context context, Disc disc, String str) {
            super(context);
            Glass glass = new Glass(-1.0f, 452984831, 452984831, 0.5f, 0.5f);
            this.back = glass;
            setBackground(glass);
            this.disc = disc;
            addView(disc);
            TextView textViewLabel = Panel.this.label(context, 12.0f, -1, 1);
            this.name = textViewLabel;
            textViewLabel.setText(str);
            addView(textViewLabel);
        }

        @Override // android.view.View
        protected void onMeasure(int i, int i2) {
            int size = View.MeasureSpec.getSize(i);
            int size2 = View.MeasureSpec.getSize(i2);
            int iDp = Panel.this.dp(5.0f);
            boolean z = this.fill;
            boolean z2 = z && ((float) size) < ((float) size2) * 1.5f;
            if (z) {
                setBackground(z2 ? null : this.back);
            }
            int iMin = Math.min(size, size2);
            if (!z2) {
                iMin = Math.max(0, Math.min(iMin - (iDp * 2), Panel.this.dp(38.0f)));
            }
            this.disc.measure(View.MeasureSpec.makeMeasureSpec(iMin, 1073741824), View.MeasureSpec.makeMeasureSpec(iMin, 1073741824));
            int iDp2 = ((size - iMin) - (iDp * 3)) - Panel.this.dp(4.0f);
            this.name.setVisibility(iDp2 >= Panel.this.dp(36.0f) ? 0 : 8);
            if (iDp2 >= Panel.this.dp(36.0f)) {
                this.name.measure(View.MeasureSpec.makeMeasureSpec(iDp2, Integer.MIN_VALUE), 0);
            }
            setMeasuredDimension(size, size2);
        }

        @Override // android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
            int i5 = i3 - i;
            int i6 = i4 - i2;
            int iDp = Panel.this.dp(5.0f);
            int measuredWidth = this.disc.getMeasuredWidth();
            boolean z2 = this.name.getVisibility() == 0;
            int i7 = z2 ? iDp : (i5 - measuredWidth) / 2;
            int i8 = i7 + measuredWidth;
            this.disc.layout(i7, (i6 - measuredWidth) / 2, i8, (measuredWidth + i6) / 2);
            if (z2) {
                int iDp2 = i8 + iDp + Panel.this.dp(2.0f);
                int measuredHeight = this.name.getMeasuredHeight();
                TextView textView = this.name;
                textView.layout(iDp2, (i6 - measuredHeight) / 2, textView.getMeasuredWidth() + iDp2, (i6 + measuredHeight) / 2);
            }
        }
    }

    private final class SliderFace extends View {
        private final RectF box;
        private final Paint fill;
        private final Drawable glyph;
        private final Path pill;

        SliderFace(Context context, boolean z) {
            super(context);
            Paint paint = new Paint(1);
            this.fill = paint;
            this.pill = new Path();
            this.box = new RectF();
            setBackground(new Glass(-1.0f, 452984831, 452984831, 0.5f, 0.5f).recessed());
            Drawable drawableOf = z ? Icons.of("M12,7c-2.76,0 -5,2.24 -5,5s2.24,5 5,5 5,-2.24 5,-5 -2.24,-5 -5,-5zM2,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1H2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM20,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1h-2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM11,2v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1V2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM11,20v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1v-2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM5.99,4.58c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0s0.39,-1.03 0,-1.41L5.99,4.58zM18.36,16.95c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0 0.39,-0.39 0.39,-1.03 0,-1.41l-1.06,-1.06zM19.42,5.99c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06zM7.05,18.36c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06z") : Icons.speaker();
            this.glyph = drawableOf;
            drawableOf.setLevel(10000);
            drawableOf.setTint(z ? -14671832 : -15374872);
            paint.setColor(-218103809);
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            float f;
            Canvas canvas2;
            float width = getWidth();
            float height = getHeight();
            float fPillRadius = Glass.pillRadius(width, height);
            this.box.set(0.0f, 0.0f, width, height);
            this.pill.rewind();
            this.pill.addRoundRect(this.box, fPillRadius, fPillRadius, Path.Direction.CW);
            boolean z = height >= width;
            canvas.save();
            canvas.clipPath(this.pill);
            Paint paint = this.fill;
            if (z) {
                canvas.drawRect(0.0f, height * 0.5f, width, height, paint);
                f = height;
                canvas2 = canvas;
            } else {
                f = height;
                canvas2 = canvas;
                canvas2.drawRect(0.0f, 0.0f, width * 0.5f, f, paint);
            }
            canvas2.restore();
            int iRound = Math.round(Math.min(Panel.this.dp(22.0f), Math.min(width, f) * 0.42f));
            int iRound2 = Math.round(z ? width / 2.0f : width * 0.25f);
            int iRound3 = Math.round(z ? 0.75f * f : f / 2.0f);
            int i = iRound / 2;
            this.glyph.setBounds(iRound2 - i, iRound3 - i, iRound2 + i, iRound3 + i);
            this.glyph.draw(canvas2);
        }
    }

    private final class MediaFace extends FrameLayout {
        MediaFace(Context context) {
            super(context);
            setBackground(new Glass(Panel.this.dp(18.0f), 452984831, 452984831, 0.5f, 0.5f));
            setClipChildren(false);
            ImageView imageViewGlyph = glyph(context, "M12,3v10.55c-0.59,-0.34 -1.27,-0.55 -2,-0.55 -2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4V7h4V3h-6z", -1275068417);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Panel.this.dp(16.0f), Panel.this.dp(16.0f), 8388659);
            int iDp = Panel.this.dp(10.0f);
            layoutParams.topMargin = iDp;
            layoutParams.leftMargin = iDp;
            addView(imageViewGlyph, layoutParams);
            TextView textViewLabel = Panel.this.label(context, 12.0f, -1, 1);
            textViewLabel.setText(Panel.this.word("media", new Object[0]));
            textViewLabel.setTypeface(Typeface.DEFAULT_BOLD);
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-2, -2, 8388659);
            layoutParams2.leftMargin = Panel.this.dp(10.0f);
            layoutParams2.topMargin = Panel.this.dp(32.0f);
            addView(textViewLabel, layoutParams2);
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            linearLayoutHorizontal.setGravity(17);
            String[] strArr = {"M6,6h2v12H6zM9.5,12l8.5,6V6z", "M8,5v14l11,-7z", "M6,18l8.5,-6L6,6v12zM16,6v12h2V6z"};
            for (int i = 0; i < 3; i++) {
                String str = strArr[i];
                LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(Panel.this.dp(14.0f), Panel.this.dp(14.0f));
                int iDp2 = Panel.this.dp(8.0f);
                layoutParams3.rightMargin = iDp2;
                layoutParams3.leftMargin = iDp2;
                linearLayoutHorizontal.addView(glyph(context, str, -1), layoutParams3);
            }
            FrameLayout.LayoutParams layoutParams4 = new FrameLayout.LayoutParams(-1, -2, 80);
            layoutParams4.bottomMargin = Panel.this.dp(10.0f);
            addView(linearLayoutHorizontal, layoutParams4);
        }

        private ImageView glyph(Context context, String str, int i) {
            ImageView imageView = new ImageView(context);
            imageView.setImageDrawable(Icons.of(str));
            imageView.setImageTintList(ColorStateList.valueOf(i));
            return imageView;
        }
    }

    private final class Segmented extends FrameLayout {
        private final Motion.Value at;
        private final TextView[] options;
        private final View thumb;

        Segmented(Context context, String[] strArr, final IntConsumer intConsumer) {
            super(context);
            setBackground(new Glass(-1.0f, 452984831, 452984831, 0.5f, 0.5f).recessed());
            View view = new View(context);
            this.thumb = view;
            view.setBackground(new Glass(-1.0f, -83886081, -83886081, 0.5f, 0.5f));
            addView(view);
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            this.options = new TextView[strArr.length];
            for (int i0 = 0; i0 < strArr.length; i0++) {
                final int i = i0;
                TextView textViewLabel = Panel.this.label(context, 13.0f, -1, 1);
                textViewLabel.setText(strArr[i]);
                textViewLabel.setGravity(17);
                textViewLabel.setIncludeFontPadding(false);
                textViewLabel.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Segmented.0
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view2) {
                        Segmented.this.m179lambda$new$1$devcolorccPanel$Segmented(intConsumer, i, view2);
                    }
                });
                this.options[i] = textViewLabel;
                linearLayoutHorizontal.addView(textViewLabel, new LinearLayout.LayoutParams(0, -1, 1.0f));
            }
            addView(linearLayoutHorizontal, new FrameLayout.LayoutParams(-1, -1));
            this.at = Panel.this.motion.value(Logic.Spring.BOUNCY, new Motion.Listener() { // from class: dev.colorcc.Panel.Segmented.1
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Segmented.this.place(f, f2);
                }
            });
        }

        /* renamed from: lambda$new$1$dev-colorcc-Panel$Segmented, reason: not valid java name */
        /* synthetic */ void m179lambda$new$1$devcolorccPanel$Segmented(final IntConsumer intConsumer, final int i, View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Segmented.2
                @Override // java.lang.Runnable
                public final void run() {
                    intConsumer.accept(i);
                }
            });
        }

        void choose(int i, boolean z) {
            Motion.Value value = this.at;
            if (z) {
                value.to(i);
            } else {
                value.snap(i);
            }
            place(this.at.value(), this.at.velocity());
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void place(float f, float f2) {
            int iDp = Panel.this.dp(3.0f);
            int width = (getWidth() - (iDp * 2)) / this.options.length;
            if (width <= 0) {
                return;
            }
            this.thumb.setTranslationX(iDp + (width * f));
            float fMin = Math.min(0.3f, Math.abs(f2) * 0.05f);
            this.thumb.setScaleX(fMin + 1.0f);
            this.thumb.setScaleY(1.0f - (fMin * 0.3f));
            int i = 0;
            while (true) {
                TextView[] textViewArr = this.options;
                if (i >= textViewArr.length) {
                    return;
                }
                textViewArr[i].setTextColor(((Integer) Panel.this.argb.evaluate(Logic.clamp(1.0f - Math.abs(f - i)), -1, -14671832)).intValue());
                i++;
            }
        }

        @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
            super.onLayout(z, i, i2, i3, i4);
            int iDp = Panel.this.dp(3.0f);
            this.thumb.layout(0, iDp, ((i3 - i) - (iDp * 2)) / this.options.length, (i4 - i2) - iDp);
            place(this.at.value(), 0.0f);
        }
    }

    private final class ResizeHandle extends View {
        private final Paint line;
        private final RectF ring;

        ResizeHandle(Context context) {
            super(context);
            Paint paint = new Paint(1);
            this.line = paint;
            this.ring = new RectF();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Panel.this.dp(2.6f));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(-1);
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            float width = getWidth() - Panel.this.dp(7.0f);
            float f = -width;
            this.ring.set(f, f, width, width);
            canvas.drawArc(this.ring, 8.0f, 74.0f, false, this.line);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void reglaze(View view) {
        if (view.getBackground() instanceof Glass) {
            view.invalidate();
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                reglaze(viewGroup.getChildAt(i));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void redraw(View view) {
        view.invalidate();
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                redraw(viewGroup.getChildAt(i));
            }
        }
    }

    private final class TileGrid extends ViewGroup {
        private final int[] at;
        private final int cellHeight;
        private final int columns;
        private float downX;
        private float downY;
        private View dragged;
        private float fingerX;
        private float fingerY;
        private final boolean free;
        private final int gap;
        private float grabX;
        private float grabY;
        private Runnable hold;
        private int[][] home;
        final List<View> order;
        final boolean reorder;
        private boolean sized;
        private int[][] sizes;
        private View sizing;
        private final boolean spans;
        int[][] spots;

        TileGrid(Context context, boolean z, int i, int i2, int i3, boolean z2) {
            super(context);
            this.order = new ArrayList();
            this.at = new int[2];
            this.spots = new int[0][];
            this.sizes = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 2, 0);
            this.reorder = z;
            this.columns = i;
            this.cellHeight = i2;
            this.gap = i3;
            this.spans = z2;
            this.free = z2 && z;
            setClipChildren(false);
        }

        void addCell(View view, int i) {
            addCell(view, i, null);
        }

        void addCell(View view, int i, int[] iArr) {
            view.layout(0, 0, 0, 0);
            Panel.this.spring(view, View.TRANSLATION_X, Logic.Spring.SNAPPY).snap(0.0f);
            Panel.this.spring(view, View.TRANSLATION_Y, Logic.Spring.SNAPPY).snap(0.0f);
            List<View> list = this.order;
            list.add(this.free ? list.size() : Math.min(i, list.size()), view);
            addView(view);
            if (this.free) {
                size();
                int size = this.order.size() - 1;
                int[][] iArr2 = (int[][]) Arrays.copyOf(this.spots, this.order.size());
                this.spots = iArr2;
                int[][] iArr3 = this.sizes;
                int[] iArr4 = iArr3[0];
                int[] iArr5 = iArr3[1];
                if (iArr == null) {
                    iArr = new int[]{0, 0};
                }
                iArr2[size] = Logic.nearest(iArr4, iArr5, iArr2, size, iArr, this.columns);
            }
        }

        int[] cellAt(float f, float f2) {
            getLocationOnScreen(this.at);
            return new int[]{Math.max(0, Math.min(this.columns - 1, (int) Math.floor((f - this.at[0]) / (cellWidth(getWidth()) + this.gap)))), Math.max(0, (int) Math.floor((f2 - this.at[1]) / (this.cellHeight + this.gap)))};
        }

        void removeCell(View view) {
            int iIndexOf = this.order.indexOf(view);
            this.order.remove(iIndexOf);
            removeView(view);
            if (this.free) {
                ArrayList arrayList = new ArrayList(Arrays.asList(this.spots));
                arrayList.remove(iIndexOf);
                size();
                this.spots = Logic.lift((int[][]) arrayList.toArray(new int[0][]), this.sizes[1]);
            }
        }

        void clear() {
            this.order.clear();
            this.spots = new int[0][];
            removeAllViews();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void size() {
            this.sizes = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 2, this.order.size());
            for (int i = 0; i < this.order.size(); i++) {
                String str = this.spans ? (String) this.order.get(i).getTag() : null;
                this.sizes[0][i] = str == null ? 1 : str.charAt(1) - '0';
                this.sizes[1][i] = str == null ? 1 : str.charAt(2) - '0';
            }
        }

        private int place() {
            size();
            if (!this.free) {
                int[][] iArr = this.sizes;
                this.spots = Logic.place(iArr[0], iArr[1], this.columns);
            }
            return Logic.rows(this.spots, this.sizes[1]);
        }

        void placeAt(View view, int[] iArr) {
            size();
            int iIndexOf = this.order.indexOf(view);
            if (iIndexOf < 0 || iArr == null) {
                return;
            }
            int iMax = Math.max(0, Math.min(iArr[0], this.columns - this.sizes[0][iIndexOf]));
            int[][] iArr2 = this.sizes;
            this.spots = Logic.lift(Logic.shove(iArr2[0], iArr2[1], this.spots, iIndexOf, iMax, iArr[1], this.columns), this.sizes[1]);
            requestLayout();
        }

        void settle(View view) {
            size();
            int iIndexOf = this.order.indexOf(view);
            int iMax = Math.max(0, Math.min(this.spots[iIndexOf][0], this.columns - this.sizes[0][iIndexOf]));
            int[][] iArr = this.sizes;
            int[] iArr2 = iArr[0];
            if (iArr2[iIndexOf] == 2) {
                iMax -= iMax % 2;
            }
            int[] iArr3 = iArr[1];
            int[][] iArr4 = this.spots;
            this.spots = Logic.lift(Logic.shove(iArr2, iArr3, iArr4, iIndexOf, iMax, iArr4[iIndexOf][1], this.columns), this.sizes[1]);
            requestLayout();
        }

        private int cellWidth(int i) {
            int i2 = this.columns;
            return (i - ((i2 - 1) * this.gap)) / i2;
        }

        @Override // android.view.View
        protected void onMeasure(int i, int i2) {
            int size = View.MeasureSpec.getSize(i);
            int iPlace = place();
            int iCellWidth = cellWidth(size);
            for (int i3 = 0; i3 < this.order.size(); i3++) {
                View view = this.order.get(i3);
                int i4 = this.sizes[0][i3];
                int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec((i4 * iCellWidth) + ((i4 - 1) * this.gap), 1073741824);
                int i5 = this.sizes[1][i3];
                view.measure(iMakeMeasureSpec, View.MeasureSpec.makeMeasureSpec((this.cellHeight * i5) + ((i5 - 1) * this.gap), 1073741824));
            }
            setMeasuredDimension(size, iPlace != 0 ? (this.cellHeight * iPlace) + ((iPlace - 1) * this.gap) : 0);
        }

        @Override // android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
            place();
            int iCellWidth = cellWidth(i3 - i);
            for (int i5 = 0; i5 < this.order.size(); i5++) {
                View view = this.order.get(i5);
                int[] iArr = this.spots[i5];
                int i6 = iArr[0];
                int i7 = this.gap;
                int i8 = i6 * (iCellWidth + i7);
                int i9 = iArr[1] * (this.cellHeight + i7);
                boolean z2 = view.getRight() > 0 || view.getBottom() > 0;
                int left = view.getLeft();
                int top = view.getTop();
                int[][] iArr2 = this.sizes;
                int i10 = iArr2[0][i5];
                int i11 = this.gap;
                int i12 = iArr2[1][i5];
                view.layout(i8, i9, (i10 * iCellWidth) + i8 + ((i10 - 1) * i11), (this.cellHeight * i12) + i9 + ((i12 - 1) * i11));
                if (view == this.dragged) {
                    view.setTranslationX((this.fingerX - this.grabX) - i8);
                    view.setTranslationY((this.fingerY - this.grabY) - i9);
                } else if (z2 && (left != i8 || top != i9)) {
                    Motion.Value valueSpring = Panel.this.spring(view, View.TRANSLATION_X, Logic.Spring.SNAPPY);
                    Motion.Value valueSpring2 = Panel.this.spring(view, View.TRANSLATION_Y, Logic.Spring.SNAPPY);
                    valueSpring.shift(left - i8);
                    valueSpring2.shift(top - i9);
                    valueSpring.to(0.0f);
                    valueSpring2.to(0.0f);
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: startDrag, reason: merged with bridge method [inline-methods] */
        public void m215lambda$drag$0$devcolorccPanel$TileGrid(View view) {
            this.dragged = view;
            this.home = this.spots;
            setTranslationZ(Panel.this.dp(2.0f));
            this.grabX = this.fingerX - view.getLeft();
            this.grabY = this.fingerY - view.getTop();
            view.cancelLongPress();
            view.setPressed(false);
            getParent().requestDisallowInterceptTouchEvent(true);
            view.performHapticFeedback(0);
            Panel.this.spring(view, View.TRANSLATION_X, Logic.Spring.SNAPPY).snap(view.getTranslationX());
            Panel.this.spring(view, View.TRANSLATION_Y, Logic.Spring.SNAPPY).snap(view.getTranslationY());
            Panel.this.spring(view, View.TRANSLATION_Z, Logic.Spring.SNAPPY).to(Panel.this.dp(8.0f));
            Panel.this.scaleTo(view, this.spans ? 1.05f : 1.12f, Logic.Spring.BOUNCY);
        }

        private void reflow() {
            onLayout(false, getLeft(), getTop(), getRight(), getBottom());
        }

        void follow(View view, float f, float f2) {
            getLocationOnScreen(this.at);
            int[] iArr = this.at;
            float f3 = f - iArr[0];
            float f4 = f2 - iArr[1];
            this.fingerX = f3;
            this.fingerY = f4;
            view.setTranslationX((f3 - this.grabX) - view.getLeft());
            view.setTranslationY((f4 - this.grabY) - view.getTop());
            if (!this.reorder || Panel.this.editor.across(this, f2)) {
                return;
            }
            if (this == Panel.this.editor.chosenGrid && Panel.this.editor.overBlocks(f2)) {
                return;
            }
            if (this != Panel.this.editor.blockGrid || Panel.this.editor.overBlocks(f2)) {
                int iIndexOf = this.order.indexOf(view);
                int iRows = Logic.rows(this.spots, this.sizes[1]);
                if (this.free) {
                    int i = 0;
                    int iMax = 0;
                    while (true) {
                        int[][] iArr2 = this.home;
                        if (i >= iArr2.length) {
                            break;
                        }
                        if (i != iIndexOf) {
                            iMax = Math.max(iMax, iArr2[i][1] + this.sizes[1][i]);
                        }
                        i++;
                    }
                    float fCellWidth = (f3 - this.grabX) / (cellWidth(getWidth()) + this.gap);
                    int i2 = this.spots[iIndexOf][0];
                    int i3 = this.columns;
                    int i4 = this.sizes[0][iIndexOf];
                    int iSnap = Logic.snap(fCellWidth, i2, i3 - i4, i4 != 2 ? 1 : 2);
                    int iSnap2 = Logic.snap((f4 - this.grabY) / (this.cellHeight + this.gap), this.spots[iIndexOf][1], iMax, 1);
                    int[] iArr3 = this.spots[iIndexOf];
                    if (iSnap == iArr3[0] && iSnap2 == iArr3[1]) {
                        return;
                    }
                    int[][] iArr4 = this.sizes;
                    this.spots = Logic.shove(iArr4[0], iArr4[1], this.home, iIndexOf, iSnap, iSnap2, this.columns);
                } else {
                    int iDropIndex = Logic.dropIndex(this.order.size(), iIndexOf, (((f3 - this.grabX) + (view.getWidth() / 2.0f)) + (this.gap / 2.0f)) / (cellWidth(getWidth()) + this.gap), (((f4 - this.grabY) + (view.getHeight() / 2.0f)) + (this.gap / 2.0f)) / (this.cellHeight + this.gap), this.columns);
                    if (iDropIndex == iIndexOf) {
                        return;
                    }
                    this.order.remove(view);
                    this.order.add(iDropIndex, view);
                }
                reflow();
                if (Logic.rows(this.spots, this.sizes[1]) != iRows) {
                    requestLayout();
                }
            }
        }

        boolean drag(final View view, MotionEvent motionEvent) {
            getLocationOnScreen(this.at);
            float rawX = motionEvent.getRawX() - this.at[0];
            float rawY = motionEvent.getRawY() - this.at[1];
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                this.downX = rawX;
                this.fingerX = rawX;
                this.downY = rawY;
                this.fingerY = rawY;
                if (!this.spans || !this.reorder || view.findViewWithTag("handle") == null || rawX <= view.getRight() - Panel.this.dp(30.0f) || rawY <= view.getBottom() - Panel.this.dp(30.0f)) {
                    Panel.this.scaleTo(view, 0.92f, Logic.Spring.PRESS);
                    Runnable runnable = new Runnable() { // from class: dev.colorcc.Panel.TileGrid.0
                        @Override // java.lang.Runnable
                        public final void run() {
                            TileGrid.this.m215lambda$drag$0$devcolorccPanel$TileGrid(view);
                        }
                    };
                    this.hold = runnable;
                    postDelayed(runnable, 280L);
                    return false;
                }
                this.sizing = view;
                this.sized = false;
                getParent().requestDisallowInterceptTouchEvent(true);
                Panel.this.scaleTo(view, 1.03f, Logic.Spring.PRESS);
                return true;
            }
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    this.fingerX = rawX;
                    this.fingerY = rawY;
                    if (this.sizing == view) {
                        int[] iArrFit = Logic.fit(((String) view.getTag()).charAt(0), Math.round(((rawX - view.getLeft()) + this.gap) / (cellWidth(getWidth()) + this.gap)), Math.round(((rawY - view.getTop()) + this.gap) / (this.cellHeight + this.gap)));
                        if (iArrFit[0] != ((String) view.getTag()).charAt(1) - '0' || iArrFit[1] != ((String) view.getTag()).charAt(2) - '0') {
                            this.sized = true;
                            Panel.this.editor.setSize(view, iArrFit[0], iArrFit[1]);
                            view.performHapticFeedback(4);
                        }
                        return true;
                    }
                    if (this.dragged != view) {
                        if (Math.hypot(rawX - this.downX, rawY - this.downY) > Panel.this.touchSlop) {
                            release(view);
                        }
                        return false;
                    }
                    follow(view, motionEvent.getRawX(), motionEvent.getRawY());
                    Panel.this.editor.edgeScroll(this, view, motionEvent.getRawX(), motionEvent.getRawY());
                    return true;
                }
                if (actionMasked != 3) {
                    return this.dragged == view;
                }
            }
            if (this.sizing == view) {
                this.sizing = null;
                view.cancelLongPress();
                view.setPressed(false);
                if (this.sized || motionEvent.getActionMasked() != 1 || Math.hypot(rawX - this.downX, rawY - this.downY) > Panel.this.touchSlop) {
                    Panel.this.scaleTo(view, 1.0f, Logic.Spring.BOUNCY);
                } else {
                    Panel.this.editor.resize(view);
                }
                int iRows = Logic.rows(this.spots, this.sizes[1]);
                this.spots = Logic.lift(this.spots, this.sizes[1]);
                reflow();
                if (Logic.rows(this.spots, this.sizes[1]) != iRows) {
                    requestLayout();
                }
                return true;
            }
            if (this.dragged != view) {
                release(view);
                return false;
            }
            this.dragged = null;
            Panel.this.editor.edgeScroll(null, null, 0.0f, 0.0f);
            if (motionEvent.getActionMasked() == 1 && Panel.this.editor.drop(this, view, motionEvent.getRawX(), motionEvent.getRawY())) {
                Panel.this.spring(view, View.TRANSLATION_Z, Logic.Spring.SNAPPY).to(0.0f);
                if (this.free) {
                    this.spots = this.home;
                    reflow();
                }
                return true;
            }
            if (this.free) {
                int iRows2 = Logic.rows(this.spots, this.sizes[1]);
                this.spots = Logic.lift(this.spots, this.sizes[1]);
                reflow();
                if (Logic.rows(this.spots, this.sizes[1]) != iRows2) {
                    requestLayout();
                }
            }
            setTranslationZ(0.0f);
            Panel.this.spring(view, View.TRANSLATION_X, Logic.Spring.SNAPPY).to(0.0f);
            Panel.this.spring(view, View.TRANSLATION_Y, Logic.Spring.SNAPPY).to(0.0f);
            Panel.this.spring(view, View.TRANSLATION_Z, Logic.Spring.SNAPPY).to(0.0f);
            Panel.this.scaleTo(view, 1.0f, Logic.Spring.BOUNCY);
            return true;
        }

        private void release(View view) {
            Runnable runnable = this.hold;
            if (runnable != null) {
                removeCallbacks(runnable);
            }
            this.hold = null;
            Panel.this.scaleTo(view, 1.0f, Logic.Spring.BOUNCY);
        }
    }

    private final class Pager extends HorizontalScrollView {
        private final LinearLayout content;
        private final Runnable flatten;
        private boolean flung;
        int pageCount;
        private boolean pagesLayered;
        private final Motion.Value snap;

        Pager(Context context) {
            super(context);
            this.pageCount = 1;
            this.flatten = new Runnable() { // from class: dev.colorcc.Panel.Pager.2
                @Override // java.lang.Runnable
                public final void run() {
                    Pager.this.m154lambda$new$2$devcolorccPanel$Pager();
                }
            };
            setHorizontalScrollBarEnabled(false);
            setOverScrollMode(2);
            setFillViewport(true);
            setClipChildren(false);
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            this.content = linearLayoutHorizontal;
            addView(linearLayoutHorizontal, new FrameLayout.LayoutParams(-2, -1));
            this.snap = Panel.this.motion.value(Logic.Spring.PAGE, new Motion.Listener() { // from class: dev.colorcc.Panel.Pager.3
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Pager.this.m152lambda$new$0$devcolorccPanel$Pager(f, f2);
                }
            });
            setOnScrollChangeListener(new View.OnScrollChangeListener() { // from class: dev.colorcc.Panel.Pager.4
                @Override // android.view.View.OnScrollChangeListener
                public final void onScrollChange(View view, int i, int i2, int i3, int i4) {
                    Pager.this.m153lambda$new$1$devcolorccPanel$Pager(view, i, i2, i3, i4);
                }
            });
        }

        /* renamed from: lambda$new$0$dev-colorcc-Panel$Pager, reason: not valid java name */
        /* synthetic */ void m152lambda$new$0$devcolorccPanel$Pager(float f, float f2) {
            scrollTo(Math.round(f), 0);
        }

        /* renamed from: lambda$new$1$dev-colorcc-Panel$Pager, reason: not valid java name */
        /* synthetic */ void m153lambda$new$1$devcolorccPanel$Pager(View view, int i, int i2, int i3, int i4) {
            if (getWidth() > 0) {
                Panel.this.dots.set(this.pageCount, i / getWidth());
                applyPageDepth(i);
            }
            layerPages(true);
            removeCallbacks(this.flatten);
            postDelayed(this.flatten, 120L);
        }

        /* renamed from: lambda$new$2$dev-colorcc-Panel$Pager, reason: not valid java name */
        /* synthetic */ void m154lambda$new$2$devcolorccPanel$Pager() {
            layerPages(false);
        }

        private void layerPages(boolean z) {
            if (z == this.pagesLayered) {
                return;
            }
            this.pagesLayered = z;
            for (int i = 0; i < this.content.getChildCount(); i++) {
                this.content.getChildAt(i).setLayerType(z ? 2 : 0, null);
            }
        }

        private void applyPageDepth(int i) {
            int width = getWidth();
            for (int i2 = 0; i2 < this.content.getChildCount(); i2++) {
                View childAt = this.content.getChildAt(i2);
                float fClamp = Logic.clamp(Math.abs(i2 - (i / width)));
                if (fClamp > 0.99f) {
                    fClamp = 1.0f;
                }
                float f = 1.0f - (0.14f * fClamp);
                childAt.setScaleX(f);
                childAt.setScaleY(f);
                childAt.setAlpha(1.0f - (0.6f * fClamp));
                childAt.setVisibility(fClamp < 1.0f ? 0 : 4);
            }
        }

        @Override // android.widget.HorizontalScrollView, android.view.View
        protected void onSizeChanged(int i, int i2, int i3, int i4) {
            super.onSizeChanged(i, i2, i3, i4);
            setClipBounds(new Rect(0, (-i2) * 4, i, i2 * 5));
        }

        void setTiles(List<Object> list, int i) {
            Context context = getContext();
            this.content.removeAllViews();
            this.pagesLayered = false;
            this.pageCount = Logic.pages(list.size(), i);
            int i2 = i / 4;
            getLayoutParams().height = Panel.this.dp(Panel.this.tileRow()) * i2;
            requestLayout();
            int i3 = 0;
            while (i3 < this.pageCount) {
                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(1);
                linearLayout.setClipChildren(false);
                linearLayout.setClipToPadding(false);
                linearLayout.setPadding(Panel.this.dp(27.0f), 0, Panel.this.dp(27.0f), 0);
                linearLayout.setVisibility(i3 == 0 ? 0 : 4);
                linearLayout.forceHasOverlappingRendering(false);
                for (int i4 = 0; i4 < i2; i4++) {
                    LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
                    for (int i5 = 0; i5 < 4; i5++) {
                        int i6 = (i3 * i) + (i4 * 4) + i5;
                        View viewTileView = i6 < list.size() ? tileView(context, list.get(i6), Panel.this.tileBase + (i4 * 3) + i5) : new Space(context);
                        viewTileView.setTranslationZ(-i5);
                        linearLayoutHorizontal.addView(viewTileView, new LinearLayout.LayoutParams(0, Panel.this.dp(Panel.this.tileRow()), 1.0f));
                    }
                    linearLayoutHorizontal.setTranslationZ(-i4);
                    linearLayout.addView(linearLayoutHorizontal);
                }
                this.content.addView(linearLayout, new LinearLayout.LayoutParams(-1, -1));
                i3++;
            }
            this.snap.snap(0.0f);
            Panel.this.dots.set(this.pageCount, 0.0f);
        }

        private View tileView(Context context, final Object obj, int i) {
            final LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            linearLayout.setGravity(1);
            linearLayout.setClickable(true);
            Panel panel = Panel.this;
            final Disc disc = panel.new Disc(context, panel.dp(26.0f));
            disc.setClickable(false);
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Pager.0
                @Override // java.lang.Runnable
                public final void run() {
                    Pager.lambda$tileView$3(disc, obj);
                }
            });
            linearLayout.addView(disc, new LinearLayout.LayoutParams(Panel.this.dp(63.0f), Panel.this.dp(63.0f)));
            final TextView textViewLabel = Panel.this.label(context, 10.0f, -1, 2);
            textViewLabel.setGravity(1);
            if (!Panel.this.tileNames) {
                textViewLabel.setVisibility(8);
            }
            textViewLabel.setIncludeFontPadding(false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            layoutParams.topMargin = Panel.this.dp(3.0f);
            linearLayout.addView(textViewLabel, layoutParams);
            Panel.this.wireTile(linearLayout, obj);
            final Body body = Panel.this.new Body(linearLayout, i);
            body.pressable();
            Panel.this.tileBodies.add(body);
            final int[] iArr = {-1};
            Panel.this.bindTile(obj, new Consumer() { // from class: dev.colorcc.Panel.Pager.1
                @Override // java.util.function.Consumer
                public final void accept(Object obj2) {
                    Pager.this.m155lambda$tileView$4$devcolorccPanel$Pager(iArr, textViewLabel, disc, body, linearLayout, obj2);
                }
            });
            return linearLayout;
        }

        static /* synthetic */ void lambda$tileView$3(Disc disc, Object obj) {
            disc.style((String) XposedHelpers.callMethod(obj, "getTileSpec", new Object[0]));
        }

        /* renamed from: lambda$tileView$4$dev-colorcc-Panel$Pager, reason: not valid java name */
        /* synthetic */ void m155lambda$tileView$4$devcolorccPanel$Pager(int[] iArr, TextView textView, Disc disc, Body body, LinearLayout linearLayout, Object obj) {
            int iStateOf = Panel.stateOf(obj);
            int i = iArr[0];
            boolean z = (i == -1 || i == iStateOf) ? false : true;
            iArr[0] = iStateOf;
            Panel.setTextIfChanged(textView, Panel.text(obj, "label"));
            boolean z2 = z && Panel.this.interactive;
            disc.render(obj, z2);
            if (z2 && !disc.ownAnimation) {
                body.press.kick(7.0f);
            }
            linearLayout.setContentDescription(Panel.text(obj, "label"));
            linearLayout.setStateDescription(Panel.this.word(iStateOf == 2 ? "on" : "off", new Object[0]));
        }

        @Override // android.widget.HorizontalScrollView, android.widget.FrameLayout, android.view.View
        protected void onMeasure(int i, int i2) {
            int size = View.MeasureSpec.getSize(i);
            for (int i3 = 0; i3 < this.content.getChildCount(); i3++) {
                this.content.getChildAt(i3).getLayoutParams().width = size;
            }
            super.onMeasure(i, i2);
        }

        void home() {
            if (getScrollX() == 0) {
                return;
            }
            this.snap.snap(0.0f);
            scrollTo(0, 0);
        }

        private void snapTo(int i) {
            this.snap.snap(getScrollX());
            this.snap.to(Math.max(0, Math.min(this.pageCount - 1, i)) * getWidth());
        }

        @Override // android.widget.HorizontalScrollView
        public void fling(int i) {
            if (getWidth() == 0) {
                return;
            }
            this.flung = true;
            float f = i;
            snapTo(Logic.pageAfterFling(getScrollX(), getWidth(), f, this.pageCount));
            float width = getWidth() * 4.0f;
            this.snap.kick(Math.max(-width, Math.min(width, f)));
        }

        @Override // android.view.ViewGroup, android.view.View
        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked != 0 || Panel.this.swipeOwner != 0) {
                if (actionMasked == 2 && Panel.this.swipeOwner == 2) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
            } else {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            if (actionMasked == 0 && this.pageCount > 1) {
                this.flung = false;
                this.snap.snap(getScrollX());
            }
            boolean zDispatchTouchEvent = super.dispatchTouchEvent(motionEvent);
            if ((actionMasked == 1 || actionMasked == 3) && this.pageCount > 1 && !this.flung && getWidth() > 0) {
                snapTo(Math.round(getScrollX() / getWidth()));
            }
            return zDispatchTouchEvent;
        }

        @Override // android.widget.HorizontalScrollView, android.view.ViewGroup
        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            return this.pageCount > 1 && Panel.this.swipeOwner != 2 && super.onInterceptTouchEvent(motionEvent);
        }

        @Override // android.widget.HorizontalScrollView, android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            return this.pageCount > 1 && super.onTouchEvent(motionEvent);
        }
    }

    private final class Dots extends View {
        private final Glass back;
        private int count;
        private final Paint paint;
        private final RectF pill;
        private float position;

        Dots(Context context) {
            super(context);
            this.paint = new Paint(1);
            this.pill = new RectF();
            Glass glass = new Glass(-1.0f, 452984831, 452984831, 0.5f, 0.5f);
            this.back = glass;
            this.count = 1;
            glass.setCallback(this);
        }

        void set(int i, float f) {
            if (i == this.count && f == this.position) {
                return;
            }
            this.count = i;
            this.position = f;
            setStateDescription("Page " + (Math.round(f) + 1) + " of " + i);
            invalidate();
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            if (this.count < 2) {
                return;
            }
            float fDp = Panel.this.dp(3.0f);
            float fDp2 = Panel.this.dp(12.0f);
            float height = getHeight() / 2.0f;
            float width = (getWidth() / 2.0f) - (((this.count - 1) * fDp2) / 2.0f);
            float fDp3 = Panel.this.dp(8.0f);
            int i = 0;
            this.back.setBounds(Math.round((width - fDp) - fDp3), 0, Math.round(((this.count - 1) * fDp2) + width + fDp + fDp3), getHeight());
            this.back.draw(canvas);
            this.paint.setColor(1728053247);
            while (true) {
                int i2 = this.count;
                if (i >= i2) {
                    float fMax = Math.max(0.0f, Math.min(i2 - 1.0f, this.position));
                    float f = (int) fMax;
                    float f2 = (fMax - f) * 2.0f;
                    this.pill.set((width + ((f + Math.max(f2 - 1.0f, 0.0f)) * fDp2)) - fDp, height - fDp, ((Math.min(f2, 1.0f) + f) * fDp2) + width + fDp, height + fDp);
                    this.paint.setColor(-1);
                    canvas.drawRoundRect(this.pill, fDp, fDp, this.paint);
                    return;
                }
                canvas.drawCircle((i * fDp2) + width, height, fDp, this.paint);
                i++;
            }
        }
    }
}
