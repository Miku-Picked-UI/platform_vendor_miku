package dev.colorcc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/* loaded from: classes.dex */
final class Logic {
    static final int BLUETOOTH = 2;
    static final float CASCADE_S = 0.25f;
    static final String COMPONENTS = "wbmlvsd";
    static final int DATA_GREEN = -14700715;
    private static final float DROP_MARGIN = 0.3f;
    private static final float FULLY_OPEN = 0.99f;
    static final int FX_BOB = 9;
    static final int FX_DROP = 4;
    static final int FX_FILL = 11;
    static final int FX_FLICKER = 1;
    static final int FX_FLIP = 8;
    static final int FX_FLY = 2;
    static final int FX_GROW = 7;
    static final int FX_JUMP = 14;
    static final int FX_POP = 0;
    static final int FX_PULSE = 6;
    static final int FX_SHAKE = 13;
    static final int FX_SHUTTER = 12;
    static final int FX_SPIN = 3;
    static final int FX_SWING = 5;
    static final int FX_TURN = 10;
    private static final float MAX_PACE = 1.5f;
    static final int MENU_BLUETOOTH = 2;
    static final int MENU_DATA = 3;
    static final int MENU_DND = 5;
    static final int MENU_HOTSPOT = 4;
    static final int MENU_NONE = 0;
    static final int MENU_OUTPUT = 7;
    static final int MENU_TORCH = 6;
    static final int MENU_WIFI = 1;
    static final String[] MENU_WORDS = {"", "wifi", "bluetooth", "mobile_data", "hotspot", "dnd", "torch", "media_output"};
    static final int MOBILE_DATA = 3;
    static final int OTHER = 0;
    static final int OWNER_PAGER = 1;
    static final int OWNER_SHADE = 2;
    static final int OWNER_UNDECIDED = 0;
    static final char PIN = 'p';
    private static final int STAGGER_MAX_ORDER = 21;
    private static final float STAGGER_SPAN = 0.16f;
    private static final float STAGGER_STEP = 0.038f;
    static final char TILES = 'g';
    private static final float TONE_PEAK = 140.0f;
    private static final float TURN = 0.03f;
    static final int WIFI = 1;

    static boolean closing(boolean z, float f, float f2) {
        return z ? f < 1.0f && f <= f2 + 0.03f : f < f2 - 0.03f || (f <= 0.0f && f2 > 0.0f);
    }

    static boolean controlsPage(int i, int i2, boolean z) {
        return (i == 2 && (i2 == 0 || i2 == 1)) ? i2 == 0 : z;
    }

    private static float cube(float f) {
        return f * f * f;
    }

    static boolean onControlPage(boolean z, boolean z2) {
        return !z || z2;
    }

    static float rubber(float f, float f2) {
        if (f <= 0.0f) {
            return 0.0f;
        }
        return ((f * f2) * 0.75f) / (f2 + (f * 0.75f));
    }

    static int spanW(char c) {
        if (c == 'B' || c == 'D') {
            return 1;
        }
        if (c == 'M' || c == 'S') {
            return 4;
        }
        if (c == 'W') {
            return 1;
        }
        if (c != 'g') {
            return (c == 'l' || c == 'v') ? 1 : 2;
        }
        return 4;
    }

    private static float sq(float f) {
        return f * f;
    }

    static boolean takesTouches(float f, float f2) {
        return f >= 0.99f && f2 >= 0.99f;
    }

    static int thumb(int i) {
        if (i == 2) {
            return 0;
        }
        return i == 1 ? 1 : 2;
    }

    static int menu(String str) {
        int iKind = kind(str);
        if (iKind == 1) {
            return 1;
        }
        if (iKind == 2) {
            return 2;
        }
        if (iKind == 3) {
            return 3;
        }
        String lowerCase = str.toLowerCase(Locale.ROOT);
        if (lowerCase.startsWith("custom(")) {
            return 0;
        }
        if (lowerCase.contains("hotspot")) {
            return 4;
        }
        if (lowerCase.equals("dnd") || lowerCase.contains("donotdisturb")) {
            return 5;
        }
        return (lowerCase.contains("flash") || lowerCase.equals("torch")) ? 6 : 0;
    }

    static int kind(String str) {
        if ("wifi".equalsIgnoreCase(str) || "internet".equalsIgnoreCase(str)) {
            return 1;
        }
        if ("bt".equalsIgnoreCase(str) || "Bluetooth".equalsIgnoreCase(str)) {
            return 2;
        }
        return ("cell".equalsIgnoreCase(str) || "cellular".equalsIgnoreCase(str) || "mobile_data".equalsIgnoreCase(str) || "MobileData".equalsIgnoreCase(str)) ? 3 : 0;
    }

    static int[] alphaBounds(byte[] bArr, int i, int i2, int i3) {
        int iMax = -1;
        int iMin = i2;
        int iMin2 = i3;
        int iMax2 = -1;
        for (int i4 = 0; i4 < i3; i4++) {
            for (int i5 = 0; i5 < i2; i5++) {
                if ((bArr[(i4 * i) + i5] & 255) > 16) {
                    iMin = Math.min(iMin, i5);
                    iMin2 = Math.min(iMin2, i4);
                    iMax = Math.max(iMax, i5);
                    iMax2 = Math.max(iMax2, i4);
                }
            }
        }
        if (iMax < 0) {
            return null;
        }
        return new int[]{iMin, iMin2, iMax + 1, iMax2 + 1};
    }

    static int pages(int i, int i2) {
        if (i <= 0) {
            return 1;
        }
        return ((i + i2) - 1) / i2;
    }

    static int gridRows(int i, int i2) {
        return Math.max(2, Math.min(3, ((i - i2) - 30) / 98));
    }

    static int swipeOwner(float f, float f2, float f3, boolean z, boolean z2) {
        if (Math.abs(f) <= f3 && Math.abs(f2) <= f3) {
            return 0;
        }
        if (Math.abs(f2) >= Math.abs(f)) {
            return 2;
        }
        if (f < 0.0f) {
            if (z2) {
                return 1;
            }
        } else if (z) {
            return 1;
        }
        return 2;
    }

    static int pageAfterFling(int i, int i2, float f, int i3) {
        double d = i / i2;
        return Math.max(0, Math.min(i3 - 1, (int) (f > 0.0f ? Math.ceil(d) : Math.floor(d))));
    }

    static float clamp(float f) {
        if (f < 0.0f) {
            return 0.0f;
        }
        return Math.min(f, 1.0f);
    }

    static float toLinear(float f, float f2, float f3) {
        return f2 + (clamp(f) * (f3 - f2));
    }

    static float toSlider(float f, float f2, float f3) {
        if (f3 <= f2) {
            return 0.0f;
        }
        return clamp((f - f2) / (f3 - f2));
    }

    static float revealTarget(float f, boolean z) {
        if (z) {
            return clamp(f);
        }
        return 0.0f;
    }

    static float elementTarget(float f, int i) {
        return clamp((f - (Math.min(i, 21) * 0.038f)) / 0.16f);
    }

    static float openPace(float f) {
        return (clamp((f - 2.0f) / 6.0f) * 0.5f) + 1.0f;
    }

    static float flick(float f) {
        return clamp((f - 1.0f) / 0.5f);
    }

    static float openingSequence(float f) {
        return Math.min(f * 1.5f, 1.0f);
    }

    static float cascade(float f, float f2, float f3, float f4) {
        return Math.min(f2, f + ((Math.max(0.0f, Math.min(f3, 0.033333335f)) * f4) / 0.25f));
    }

    static float springAt(float[] fArr, float f) {
        float fSqrt = (float) Math.sqrt(fArr[0]);
        float f2 = fArr[1];
        float fSqrt2 = ((float) Math.sqrt(1.0f - (f2 * f2))) * fSqrt;
        double d = f * fSqrt2;
        return 1.0f - (((float) Math.exp(((-f2) * fSqrt) * f)) * (((float) Math.cos(d)) + (((f2 * fSqrt) / fSqrt2) * ((float) Math.sin(d)))));
    }

    static float springSpeed(float[] fArr, float f) {
        float fSqrt = (float) Math.sqrt(fArr[0]);
        float f2 = fArr[1];
        float fSqrt2 = ((float) Math.sqrt(1.0f - (f2 * f2))) * fSqrt;
        return ((((float) Math.exp(((-f2) * fSqrt) * f)) * fArr[0]) / fSqrt2) * ((float) Math.sin(fSqrt2 * f));
    }

    static float springPeak(float[] fArr) {
        float fSqrt = (float) Math.sqrt(fArr[0]);
        float f = fArr[1];
        return 3.1415927f / (fSqrt * ((float) Math.sqrt(1.0f - (f * f))));
    }

    static float springTimeOf(float[] fArr, float f) {
        float fSpringPeak = springPeak(fArr);
        float f2 = 0.0f;
        for (int i = 0; i < 30; i++) {
            float f3 = (f2 + fSpringPeak) / 2.0f;
            if (springAt(fArr, f3) < f) {
                f2 = f3;
            } else {
                fSpringPeak = f3;
            }
        }
        return (f2 + fSpringPeak) / 2.0f;
    }

    static float closingSequence(float f) {
        return clamp((f - 0.25f) / 0.72f);
    }

    static LinkedHashMap<String, int[]> pins(String str) throws NumberFormatException {
        LinkedHashMap<String, int[]> linkedHashMap = new LinkedHashMap<>();
        if (str != null) {
            for (String str2 : str.split("\n")) {
                String[] strArrSplit = str2.split("\t");
                if ((strArrSplit.length == 3 || strArrSplit.length == 5) && !strArrSplit[0].isEmpty()) {
                    try {
                        int i = Integer.parseInt(strArrSplit[1]);
                        int i2 = Integer.parseInt(strArrSplit[2]);
                        int i3 = strArrSplit.length == 5 ? Integer.parseInt(strArrSplit[3]) : 1;
                        int i4 = strArrSplit.length == 5 ? Integer.parseInt(strArrSplit[4]) : 1;
                        int[] iArrFit = fit('p', i3, i4);
                        if (i >= 0 && i + i3 <= 4 && i2 >= 0 && iArrFit[0] == i3 && iArrFit[1] == i4) {
                            linkedHashMap.put(strArrSplit[0], (i3 == 1 && i4 == 1) ? new int[]{i, i2} : new int[]{i, i2, i3, i4});
                        }
                    } catch (NumberFormatException unused) {
                    }
                }
            }
        }
        return linkedHashMap;
    }

    static String encodePins(Map<String, int[]> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, int[]> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(entry.getKey());
            sb.append('\t');
            sb.append(entry.getValue()[0]);
            sb.append('\t');
            sb.append(entry.getValue()[1]);
            int[] iArrPinSize = pinSize(entry.getValue());
            if (iArrPinSize[0] != 1 || iArrPinSize[1] != 1) {
                sb.append('\t');
                sb.append(iArrPinSize[0]);
                sb.append('\t');
                sb.append(iArrPinSize[1]);
            }
        }
        return sb.toString();
    }

    static int[] pinSize(int[] iArr) {
        return (iArr == null || iArr.length < 4) ? new int[]{1, 1} : new int[]{iArr[2], iArr[3]};
    }

    static String[] withTiles(String str, String str2) {
        if (str.indexOf(103) >= 0) {
            return new String[]{str, str2};
        }
        int[][] iArrSizes = sizes(str2, str);
        int[][] iArrSpots = spots(str2, str, 4);
        int[][] iArr = (int[][]) Arrays.copyOf(iArrSpots, iArrSpots.length + 1);
        iArr[iArrSpots.length] = new int[]{0, rows(iArrSpots, iArrSizes[1])};
        int[][] iArr2 = {Arrays.copyOf(iArrSizes[0], iArrSpots.length + 1), Arrays.copyOf(iArrSizes[1], iArrSpots.length + 1)};
        iArr2[0][iArrSpots.length] = spanW('g');
        iArr2[1][iArrSpots.length] = spanH('g');
        return new String[]{str + 'g', encode(iArr, iArr2)};
    }

    static String layout(String str) {
        if (str == null) {
            return "wbmlvsd";
        }
        if (str.indexOf(116) >= 0) {
            StringBuilder sb = new StringBuilder();
            for (char c : str.toCharArray()) {
                int iIndexOf = "cCmMrRt".indexOf(c);
                if (iIndexOf < 0) {
                    return "wbmlvsd";
                }
                sb.append(new String[]{"wb", "bw", "mlv", "lvm", "sd", "ds", ""}[iIndexOf]);
            }
            str = sb.toString();
        }
        StringBuilder sb2 = new StringBuilder();
        StringBuilder sb3 = new StringBuilder();
        for (char c2 : str.toCharArray()) {
            char lowerCase = Character.toLowerCase(c2);
            if (("wbmlvsd".indexOf(lowerCase) < 0 && lowerCase != 'g') || sb3.indexOf(String.valueOf(lowerCase)) >= 0) {
                return "wbmlvsd";
            }
            sb3.append(lowerCase);
            sb2.append(c2);
        }
        return sb2.toString();
    }

    static int[] nextSize(char c, int i, int i2) {
        int[] iArrLimits = limits(c);
        if (i < iArrLimits[1]) {
            return fit(c, i + 1, i2);
        }
        return fit(c, iArrLimits[0], i2 < iArrLimits[3] ? i2 + 1 : iArrLimits[2]);
    }

    static int spanH(char c) {
        char lowerCase = Character.toLowerCase(c);
        if (lowerCase == 'g') {
            return 4;
        }
        return (lowerCase == 'm' || lowerCase == 'l' || lowerCase == 'v') ? 2 : 1;
    }

    static int[][] place(int[] iArr, int[] iArr2, int i) {
        int[][] iArr3 = new int[iArr.length][];
        ArrayList arrayList = new ArrayList();
        for (int i2 = 0; i2 < iArr.length; i2++) {
            int iMax = Math.max(1, Math.min(iArr[i2], i));
            int iMax2 = Math.max(1, iArr2[i2]);
            int i3 = iMax != 2 ? 1 : 2;
            int i4 = 0;
            while (iArr3[i2] == null) {
                int i5 = 0;
                while (true) {
                    int i6 = i5 + iMax;
                    if (i6 > i || iArr3[i2] != null) {
                        break;
                    }
                    if (free(arrayList, i5, i4, iMax, iMax2)) {
                        for (int i7 = i4; i7 < i4 + iMax2; i7++) {
                            while (arrayList.size() <= i7) {
                                arrayList.add(new boolean[i]);
                            }
                            for (int i8 = i5; i8 < i6; i8++) {
                                ((boolean[]) arrayList.get(i7))[i8] = true;
                            }
                        }
                        iArr3[i2] = new int[]{i5, i4};
                    }
                    i5 += i3;
                }
                i4++;
            }
        }
        return iArr3;
    }

    private static boolean free(List<boolean[]> list, int i, int i2, int i3, int i4) {
        for (int i5 = i2; i5 < i2 + i4 && i5 < list.size(); i5++) {
            for (int i6 = i; i6 < i + i3; i6++) {
                if (list.get(i5)[i6]) {
                    return false;
                }
            }
        }
        return true;
    }

    static int dropIndex(int i, int i2, float f, float f2, int i3) {
        float f3 = 0.0f;
        float f4 = Float.MAX_VALUE;
        int i4 = i2;
        for (int i5 = 0; i5 < i; i5++) {
            float fHypot = (float) Math.hypot(((i5 % i3) + 0.5f) - f, ((i5 / i3) + 0.5f) - f2);
            if (i5 == i2) {
                f3 = fHypot;
            }
            if (fHypot < f4) {
                i4 = i5;
                f4 = fHypot;
            }
        }
        return f3 - f4 > 0.3f ? i4 : i2;
    }

    static int snap(float f, int i, int i2, int i3) {
        float f2 = i3;
        return Math.abs(f - ((float) i)) > (f2 / 2.0f) + 0.3f ? Math.max(0, Math.min(i2, Math.round(f / f2) * i3)) : i;
    }

    static int[][] shove(int[] iArr, int[] iArr2, int[][] iArr3, int i, int i2, int i3, int i4) {
        int i5;
        int[] iArr4;
        int[] iArr5;
        int[][] iArr6 = new int[iArr3.length][];
        iArr6[i] = new int[]{i2, i3};
        int i6 = 0;
        while (i6 < iArr3.length) {
            if (i6 != i) {
                int[] iArr7 = iArr3[i6];
                iArr4 = iArr;
                iArr5 = iArr2;
                if (clear(iArr4, iArr5, iArr6, i6, iArr7[0], iArr7[1])) {
                    iArr6[i6] = iArr3[i6];
                }
            } else {
                iArr4 = iArr;
                iArr5 = iArr2;
            }
            i6++;
            iArr = iArr4;
            iArr2 = iArr5;
        }
        int[] iArr8 = iArr;
        int[] iArr9 = iArr2;
        int i7 = 0;
        while (i7 < iArr3.length) {
            if (iArr6[i7] == null) {
                i5 = i4;
                iArr6[i7] = nearest(iArr8, iArr9, iArr6, i7, iArr3[i7], i5);
            } else {
                i5 = i4;
            }
            i7++;
            i4 = i5;
        }
        return iArr6;
    }

    static int[] nearest(int[] iArr, int[] iArr2, int[][] iArr3, int i, int[] iArr4, int i2) {
        int i3;
        int[][] iArr5 = iArr3;
        int i4 = i;
        int i5 = 0;
        int iMax = 0;
        for (int i6 = 0; i6 < iArr5.length; i6++) {
            int[] iArr6 = iArr5[i6];
            if (iArr6 != null && i6 != i4) {
                iMax = Math.max(iMax, iArr6[1] + iArr2[i6]);
            }
        }
        int[] iArr7 = null;
        double d = Double.MAX_VALUE;
        int i7 = 0;
        while (i7 <= iMax) {
            int[] iArr8 = iArr7;
            double d2 = d;
            int i8 = i5;
            while (iArr[i4] + i8 <= i2) {
                int i9 = i5;
                int i10 = iMax;
                double dHypot = Math.hypot(i8 - iArr4[i5], i7 - iArr4[1]);
                int i11 = 2;
                if (dHypot < d2) {
                    i3 = i7;
                    if (clear(iArr, iArr2, iArr5, i4, i8, i3)) {
                        iArr8 = new int[2];
                        iArr8[i9] = i8;
                        iArr8[1] = i3;
                        d2 = dHypot;
                    }
                } else {
                    i3 = i7;
                }
                if (iArr[i] != 2) {
                    i11 = 1;
                }
                i8 += i11;
                iArr5 = iArr3;
                i4 = i;
                i7 = i3;
                i5 = i9;
                iMax = i10;
            }
            i7++;
            iArr5 = iArr3;
            i4 = i;
            iArr7 = iArr8;
            d = d2;
        }
        return iArr7;
    }

    private static boolean clear(int[] iArr, int[] iArr2, int[][] iArr3, int i, int i2, int i3) {
        int[] iArr4;
        for (int i4 = 0; i4 < iArr3.length; i4++) {
            if (i4 != i && (iArr4 = iArr3[i4]) != null) {
                int i5 = iArr4[0];
                if (i2 < iArr[i4] + i5 && i5 < iArr[i] + i2) {
                    int i6 = iArr4[1];
                    if (i3 < iArr2[i4] + i6 && i6 < iArr2[i] + i3) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static int[][] lift(int[][] iArr, int[] iArr2) {
        int[][] iArr3 = new int[iArr.length][];
        for (int i = 0; i < iArr.length; i++) {
            iArr3[i] = (int[]) iArr[i].clone();
        }
        int iRows = rows(iArr, iArr2) - 1;
        while (iRows >= 0) {
            boolean z = true;
            for (int i2 = 0; i2 < iArr.length; i2++) {
                int i3 = iArr[i2][1];
                z &= iRows < i3 || iRows >= i3 + iArr2[i2];
            }
            if (z) {
                for (int i4 = 0; i4 < iArr.length; i4++) {
                    if (iArr[i4][1] > iRows) {
                        int[] iArr4 = iArr3[i4];
                        iArr4[1] = iArr4[1] - 1;
                    }
                }
            }
            iRows--;
        }
        return iArr3;
    }

    static int[][] spots(String str, String str2, int i) {
        int[][] iArrSizes = sizes(str, str2);
        int length = str2.length();
        int i2 = 0;
        if (str != null) {
            i2 = str.length() == length * 4 ? 4 : str.length() == length * 2 ? 2 : 0;
        }
        if (i2 == 0) {
            return place(iArrSizes[0], iArrSizes[1], i);
        }
        int[][] iArr = new int[length][];
        for (int i3 = 0; i3 < length; i3++) {
            int i4 = i2 * i3;
            int iDigit = Character.digit(str.charAt(i4), 36);
            int iDigit2 = Character.digit(str.charAt(i4 + 1), 36);
            if (iDigit < 0 || iDigit2 < 0 || iArrSizes[0][i3] + iDigit > i
                    || !clear(iArrSizes[0], iArrSizes[1], iArr, i3, iDigit, iDigit2)) {
                return place(iArrSizes[0], iArrSizes[1], i);
            }
            iArr[i3] = new int[]{iDigit, iDigit2};
        }
        return iArr;
    }

    static String encode(int[][] iArr, int[][] iArr2) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iArr.length; i++) {
            sb.append(Character.forDigit(iArr[i][0], 36));
            sb.append(Character.forDigit(iArr[i][1], 36));
            sb.append(Character.forDigit(iArr2[0][i], 36));
            sb.append(Character.forDigit(iArr2[1][i], 36));
        }
        return sb.toString();
    }

    static int[] limits(char c) {
        char lowerCase = Character.toLowerCase(c);
        if (lowerCase == 'g') {
            return new int[]{4, 4, 4, 4};
        }
        if (lowerCase == 's') {
            return new int[]{2, 4, 1, 1};
        }
        if (lowerCase == 'v' || lowerCase == 'l') {
            return new int[]{1, 4, 1, 4};
        }
        if (lowerCase == 'm') {
            return new int[]{2, 4, 2, 4};
        }
        return new int[]{1, 4, 1, 2};
    }

    static int[] fit(char c, int i, int i2) {
        int[] iArrLimits = limits(c);
        int iMax = Math.max(iArrLimits[0], Math.min(iArrLimits[1], i));
        int iMax2 = Math.max(iArrLimits[2], Math.min(iArrLimits[3], i2));
        char lowerCase = Character.toLowerCase(c);
        return new int[]{iMax, ((lowerCase == 'l' || lowerCase == 'v') && iMax == 1 && iMax2 == 1) ? 2 : iMax2};
    }

    static int[][] sizes(String str, String str2) {
        int[][] iArrSpans = spans(str2);
        int length = str2.length();
        if (str != null && str.length() == length * 4) {
            for (int i = 0; i < length; i++) {
                int i2 = i * 4;
                int iDigit = Character.digit(str.charAt(i2 + 2), 36);
                int iDigit2 = Character.digit(str.charAt(i2 + 3), 36);
                int[] iArrFit = fit(str2.charAt(i), iDigit, iDigit2);
                if (iArrFit[0] != iDigit || iArrFit[1] != iDigit2) {
                    return spans(str2);
                }
                iArrSpans[0][i] = iDigit;
                iArrSpans[1][i] = iDigit2;
            }
        }
        return iArrSpans;
    }

    static int beam(float f, float f2) {
        float fSqrt;
        float f3;
        float f4;
        if (f2 < 0.42f) {
            fSqrt = ((1.0f - cube(1.0f - (f2 / 0.42f))) * 0.85f) + 0.15f;
        } else {
            fSqrt = (float) Math.sqrt(Math.max(0.0f, 1.0f - sq((f2 - 0.42f) / 0.58000004f)));
        }
        float fAbs = fSqrt < 0.001f ? 2.0f : Math.abs(f) / fSqrt;
        float fSmooth = (1.0f - smooth(1.0f - ((smooth(0.2f, 0.9f, f2) * 0.45f) + 0.15f), 1.0f, fAbs)) * 0.92f * ((float) Math.exp(-sq(Math.max(0.0f, f2 - 0.1f) / 0.38f)));
        float fExp = ((float) Math.exp(-(sq(f / 0.75f) + sq((f2 - 0.08f) / 0.22f)))) * (1.0f - smooth(0.8f, 1.05f, fAbs));
        float f5 = (fAbs - 0.92f) / 0.16f;
        float fSin = (f5 <= 0.0f || f5 >= 1.0f) ? 0.0f : ((float) Math.sin(f5 * 3.141592653589793d)) * smooth(0.03f, 0.1f, f2) * (1.0f - smooth(0.22f, 0.4f, f2)) * (1.0f - fExp);
        float fMin = Math.min(1.0f, Math.max(Math.max(fSmooth, fExp), fSin * 0.55f));
        int iRound = 0;
        if (fMin <= 0.002f) {
            return 0;
        }
        if (f < 0.0f) {
            f3 = f5 * 130.0f;
            f4 = 50.0f;
        } else {
            f3 = f5 * 90.0f;
            f4 = 190.0f;
        }
        int iHue = hue(f3 + f4, 0.55f);
        float fSmooth2 = smooth(0.05f, 0.5f, f2) * (1.0f - fExp);
        int i = 16;
        while (i >= 0) {
            float f6 = 255.0f - ((i == 16 ? 0.0f : i == 8 ? 18.0f : 34.0f) * fSmooth2);
            iRound = (iRound << 8) | Math.round(f6 + ((((iHue >> i) & 255) - f6) * fSin * 0.9f));
            i -= 8;
        }
        return (Math.round(fMin * 255.0f) << 24) | iRound;
    }

    private static int hue(float f, float f2) {
        int iRound = 0;
        for (int i = 5; i >= 1; i -= 2) {
            float f3 = (i + (f / 60.0f)) % 6.0f;
            iRound = (iRound << 8) | Math.round((1.0f - (Math.max(0.0f, Math.min(Math.min(f3, 4.0f - f3), 1.0f)) * f2)) * 255.0f);
        }
        return iRound;
    }

    private static float smooth(float f, float f2, float f3) {
        float fClamp = clamp((f3 - f) / (f2 - f));
        return fClamp * fClamp * (3.0f - (fClamp * 2.0f));
    }

    static void smooth(int[] iArr, int i, int i2) {
        int[] iArr2 = new int[iArr.length];
        int i3 = 0;
        int i4 = 0;
        while (true) {
            char c = 2;
            if (i4 >= 2) {
                return;
            }
            int[] iArr3 = i4 == 0 ? iArr : iArr2;
            int[] iArr4 = i4 == 0 ? iArr2 : iArr;
            int i5 = i3;
            while (i5 < i2) {
                int i6 = i3;
                while (i6 < i) {
                    int i7 = i5 * i;
                    int i8 = i7 + i6;
                    int iMax = i4 == 0 ? Math.max(i3, i6 - 1) + i7 : (Math.max(i3, i5 - 1) * i) + i6;
                    int iMin = i4 == 0 ? i7 + Math.min(i - 1, i6 + 1) : (Math.min(i2 - 1, i5 + 1) * i) + i6;
                    int i9 = i3;
                    int i10 = i9;
                    while (i9 < 32) {
                        i10 |= ((((((iArr3[iMax] >>> i9) & 255) + (((iArr3[i8] >>> i9) & 255) * 2)) + ((iArr3[iMin] >>> i9) & 255)) + 2) / 4) << i9;
                        i9 += 8;
                        c = c;
                    }
                    iArr4[i8] = i10;
                    i6++;
                    i3 = 0;
                }
                i5++;
                i3 = 0;
            }
            i4++;
            i3 = 0;
        }
    }

    static float detail(int[] iArr, int i) {
        long jAbs = 0;
        int i2 = 0;
        for (int i3 = 1; i3 < iArr.length; i3++) {
            if (i3 % i != 0) {
                jAbs += Math.abs(((iArr[i3] >> 8) & 255) - ((iArr[i3 - 1] >> 8) & 255));
                i2++;
            }
        }
        if (i2 == 0) {
            return 0.0f;
        }
        return jAbs / i2;
    }

    static int seekColor(int[] iArr) {
        int i = 24;
        int i2 = 0;
        for (int i3 : iArr) {
            int i4 = (i3 >> 16) & 255;
            int i5 = (i3 >> 8) & 255;
            int i6 = i3 & 255;
            int iMax = Math.max(i4, Math.max(i5, i6)) - Math.min(i4, Math.min(i5, i6));
            if (iMax > i) {
                i2 = i3;
                i = iMax;
            }
        }
        if (i2 == 0) {
            return -1;
        }
        float f = (i2 >> 16) & 255;
        float f2 = (i2 >> 8) & 255;
        float f3 = i2 & 255;
        float fMax = Math.max(f, Math.max(f2, f3));
        float fMin = fMax - Math.min(f, Math.min(f2, f3));
        float fMax2 = Math.max(0.45f, Math.min(0.65f, fMin / fMax));
        return (Math.round((1.0f - (((fMax - f) * fMax2) / fMin)) * 255.0f) << 16) | (-16777216) | (Math.round((1.0f - (((fMax - f2) * fMax2) / fMin)) * 255.0f) << 8) | Math.round((1.0f - ((fMax2 * (fMax - f3)) / fMin)) * 255.0f);
    }

    static int rows(int[][] iArr, int[] iArr2) {
        int iMax = 0;
        for (int i = 0; i < iArr.length; i++) {
            iMax = Math.max(iMax, iArr[i][1] + iArr2[i]);
        }
        return iMax;
    }

    static int[][] spans(String str) {
        int[][] iArr = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 2, str.length());
        for (int i = 0; i < str.length(); i++) {
            iArr[0][i] = spanW(str.charAt(i));
            iArr[1][i] = spanH(str.charAt(i));
        }
        return iArr;
    }

    static int effect(String str) {
        if (str == null) {
            return 0;
        }
        String lowerCase = str.toLowerCase(Locale.ROOT);
        if (has(lowerCase, "flashlight", "torch")) {
            return 1;
        }
        if (has(lowerCase, "airplane", "aeroplane", "flight")) {
            return 2;
        }
        if (has(lowerCase, "rotat")) {
            return 3;
        }
        if (has(lowerCase, "location", "gps")) {
            return 4;
        }
        if (has(lowerCase, "wallet", "pay", "home", "device", "multi", "font", "link", "dex")) {
            return 14;
        }
        if (has(lowerCase, "hotspot", "tether", "nfc", "share", "nearby", "cast", "smartview", "mirror")) {
            return 6;
        }
        if (has(lowerCase, "wifi", "internet")) {
            return 7;
        }
        if (has(lowerCase, "bluetooth") || lowerCase.equals("bt")) {
            return 8;
        }
        if (has(lowerCase, "data", "cell")) {
            return 9;
        }
        if (has(lowerCase, "dark", "uimode", "invert", "sync")) {
            return 10;
        }
        if (has(lowerCase, "dnd", "disturb", "zen", "night", "bluelight", "eye", "sleep", "bedtime")) {
            return 5;
        }
        if (has(lowerCase, "power", "battery", "saver")) {
            return 11;
        }
        if (has(lowerCase, "record", "screenshot", "capture", "camera", "qr")) {
            return 12;
        }
        return has(lowerCase, "sound", "vibrat", "mute", "alarm", "ringer") ? 13 : 0;
    }

    static int accent(String str, int i) {
        if (str != null) {
            if (kind(str) == 3) {
                return -14700715;
            }
            String lowerCase = str.toLowerCase(Locale.ROOT);
            if (has(lowerCase, "flashlight", "torch", "dnd", "disturb", "zen", "sleep", "bedtime")) {
                return -11651332;
            }
            if (has(lowerCase, "airplane", "aeroplane", "flight")) {
                return -101120;
            }
            if (has(lowerCase, "rotat", "sound", "vibrat", "mute", "record")) {
                return -2286833;
            }
            if (has(lowerCase, "power", "battery", "saver", "eye", "bluelight")) {
                return -936946;
            }
        }
        return i;
    }

    private static boolean has(String str, String... strArr) {
        for (String str2 : strArr) {
            if (str.contains(str2)) {
                return true;
            }
        }
        return false;
    }

    static int[] artTones(int[] iArr, int i) {
        long[] jArr = new long[6];
        int[] iArr2 = new int[2];
        for (int i2 = 0; i2 < i; i2++) {
            for (int i3 = 0; i3 < i; i3++) {
                int i4 = iArr[(i2 * i) + i3];
                int i5 = i3 + i2 < i ? 0 : 1;
                int i6 = i5 * 3;
                jArr[i6] = jArr[i6] + ((i4 >> 16) & 255);
                int i7 = i6 + 1;
                jArr[i7] = jArr[i7] + ((i4 >> 8) & 255);
                int i8 = i6 + 2;
                jArr[i8] = jArr[i8] + (i4 & 255);
                iArr2[i5] = iArr2[i5] + 1;
            }
        }
        int[] iArr3 = new int[2];
        for (int i9 = 0; i9 < 2; i9++) {
            int i10 = i9 * 3;
            float fMax = Math.max(1, iArr2[i9]);
            iArr3[i9] = tone(jArr[i10] / fMax, jArr[i10 + 1] / fMax, jArr[i10 + 2] / fMax);
        }
        return iArr3;
    }

    static int tone(float f, float f2, float f3) {
        float f4 = (0.299f * f) + (0.587f * f2) + (0.114f * f3);
        float[] fArr = {((f - f4) * 1.35f) + f4, ((f2 - f4) * 1.35f) + f4, f4 + ((f3 - f4) * 1.35f)};
        float fMin = Math.min(140.0f / Math.max(1.0f, Math.max(fArr[0], Math.max(fArr[1], fArr[2]))), 1.6f);
        int iRound = -16777216;
        for (int i = 0; i < 3; i++) {
            iRound |= Math.round(Math.max(0.0f, Math.min(255.0f, fArr[i] * fMin))) << (16 - (i * 8));
        }
        return iRound;
    }

    static String clock(long j) {
        long jMax = Math.max(0L, j) / 1000;
        long j2 = jMax / 3600;
        long j3 = (jMax / 60) % 60;
        long j4 = jMax % 60;
        if (j2 > 0) {
            return String.format(Locale.ROOT, "%d:%02d:%02d", Long.valueOf(j2), Long.valueOf(j3), Long.valueOf(j4));
        }
        return String.format(Locale.ROOT, "%d:%02d", Long.valueOf(j3), Long.valueOf(j4));
    }

    static final class Spring {
        private static final float MAX_DT = 0.033333335f;
        private static final float SUB_STEP = 0.004166667f;
        private float damping;
        private float stiffness;
        float target;
        float value;
        float velocity;
        static final float[] BOUNCY = {380.0f, 0.62f};
        static final float[] SNAPPY = {700.0f, 0.85f};
        static final float[] PRESS = {600.0f, 0.55f};
        static final float[] PRESS_IN = {900.0f, 1.0f};
        static final float[] PRESS_OUT = {420.0f, 0.55f};
        static final float[] REVEAL = {380.0f, 0.68f};
        static final float[] OPEN = {360.0f, 0.82f};
        static final float[] PULL = of(0.37f, -0.15f);
        static final float[] MENU = {260.0f, 0.72f};
        static final float[] SWING = {420.0f, 0.12f};
        static final float[] BUZZ = {2400.0f, 0.07f};
        static final float[] PAGE = of(0.28f, 0.12f);
        static final float[] FLOOD = {140.0f, 1.0f};
        static final float[] SLOSH = {170.0f, 0.35f};
        static final float[] WOBBLE = {300.0f, 0.3f};
        static final float[] JITTER = {900.0f, 0.15f};

        static float[] of(float f, float f2) {
            float f3 = (float) (6.283185307179586d / f);
            return new float[]{f3 * f3, 1.0f - f2};
        }

        Spring(float[] fArr) {
            tune(fArr);
        }

        void tune(float[] fArr) {
            float f = fArr[0];
            this.stiffness = f;
            this.damping = fArr[1] * 2.0f * ((float) Math.sqrt(f));
        }

        boolean step(float f) {
            float fMin = Math.min(f, 0.033333335f);
            while (fMin > 0.0f) {
                float fMin2 = Math.min(fMin, 0.004166667f);
                float f2 = this.velocity;
                float f3 = this.stiffness;
                float f4 = this.target;
                float f5 = this.value;
                float f6 = f2 + (((f3 * (f4 - f5)) - (this.damping * f2)) * fMin2);
                this.velocity = f6;
                this.value = f5 + (f6 * fMin2);
                fMin -= fMin2;
            }
            if (Math.abs(this.target - this.value) >= 0.0015f || Math.abs(this.velocity) >= 0.02f) {
                return false;
            }
            this.value = this.target;
            this.velocity = 0.0f;
            return true;
        }
    }

    static float stretch(float f, float f2, float f3) {
        return Math.min(f3, Math.abs(f) * f2);
    }

    private Logic() {
    }
}
