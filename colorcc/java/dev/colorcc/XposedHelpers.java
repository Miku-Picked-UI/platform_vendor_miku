package dev.colorcc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * [Miku-Picked UI] The handful of de.robv.android.xposed.XposedHelpers calls ColorCC makes,
 * with the same behaviour, so the panel code can keep its reflective calls into SystemUI and
 * the framework now that it is built into SystemUI instead of loaded by LSPosed.
 *
 * Like Xposed, members are looked up through the whole class hierarchy (private ones too),
 * methods are matched to the arguments' runtime types (boxed primitives match primitive
 * parameters, null matches any object parameter), and failures are thrown as unchecked
 * exceptions; an exception thrown by the called method itself is rethrown unwrapped when it
 * is unchecked.
 */
final class XposedHelpers {
    private static final Map<String, Method> METHODS = new HashMap<>();
    private static final Map<String, Field> FIELDS = new HashMap<>();

    private XposedHelpers() {}

    static final class ClassNotFoundError extends Error {
        ClassNotFoundError(Throwable cause) {
            super(cause);
        }
    }

    static Class<?> findClass(String name, ClassLoader loader) {
        try {
            return Class.forName(
                    name, false, loader != null ? loader : XposedHelpers.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundError(e);
        }
    }

    // ---- fields ----

    static Field findFieldIfExists(Class<?> cls, String name) {
        String key = cls.getName() + '#' + name;
        synchronized (FIELDS) {
            if (FIELDS.containsKey(key)) return FIELDS.get(key);
        }
        Field found = null;
        for (Class<?> c = cls; c != null && found == null; c = c.getSuperclass()) {
            try {
                found = c.getDeclaredField(name);
                found.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
            }
        }
        synchronized (FIELDS) {
            FIELDS.put(key, found);
        }
        return found;
    }

    static Field findField(Class<?> cls, String name) {
        Field f = findFieldIfExists(cls, name);
        if (f == null) throw new NoSuchFieldError(cls.getName() + "#" + name);
        return f;
    }

    static Object getObjectField(Object obj, String name) {
        try {
            return findField(obj.getClass(), name).get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    static boolean getBooleanField(Object obj, String name) {
        try {
            return findField(obj.getClass(), name).getBoolean(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    static int getIntField(Object obj, String name) {
        try {
            return findField(obj.getClass(), name).getInt(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    static float getFloatField(Object obj, String name) {
        try {
            return findField(obj.getClass(), name).getFloat(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    static long getStaticLongField(Class<?> cls, String name) {
        try {
            return findField(cls, name).getLong(null);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    // ---- methods ----

    static Object callMethod(Object obj, String name, Object[] args) {
        return invoke(findMethod(obj.getClass(), name, null, args, false), obj, args);
    }

    static Object callMethod(Object obj, String name, Class<?>[] types, Object[] args) {
        return invoke(findMethod(obj.getClass(), name, types, args, false), obj, args);
    }

    static Object callStaticMethod(Class<?> cls, String name, Object[] args) {
        return invoke(findMethod(cls, name, null, args, true), null, args);
    }

    static Object newInstance(Class<?> cls, Object[] args) {
        for (Constructor<?> c : cls.getDeclaredConstructors()) {
            if (fits(c.getParameterTypes(), args)) {
                try {
                    c.setAccessible(true);
                    return c.newInstance(args);
                } catch (InvocationTargetException e) {
                    throw unwrap(e);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new NoSuchMethodError(cls.getName() + ".<init> for " + args.length + " arguments");
    }

    private static Method findMethod(
            Class<?> cls, String name, Class<?>[] types, Object[] args, boolean wantStatic) {
        StringBuilder key = new StringBuilder(cls.getName()).append('#').append(name);
        if (types != null) {
            for (Class<?> t : types) key.append(',').append(t.getName());
        } else {
            for (Object a : args) key.append(',').append(a == null ? "null" : a.getClass().getName());
        }
        String k = key.toString();
        synchronized (METHODS) {
            Method m = METHODS.get(k);
            if (m != null) return m;
        }
        Method found = null;
        for (Class<?> c = cls; c != null && found == null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (!m.getName().equals(name)) continue;
                if (wantStatic != Modifier.isStatic(m.getModifiers())) continue;
                boolean match =
                        types != null
                                ? java.util.Arrays.equals(m.getParameterTypes(), types)
                                : fits(m.getParameterTypes(), args);
                if (match) {
                    found = m;
                    break;
                }
            }
        }
        if (found == null && !wantStatic) {
            // Interface default methods and public methods declared on interfaces.
            for (Method m : cls.getMethods()) {
                if (m.getName().equals(name)
                        && (types != null
                                ? java.util.Arrays.equals(m.getParameterTypes(), types)
                                : fits(m.getParameterTypes(), args))) {
                    found = m;
                    break;
                }
            }
        }
        if (found == null) {
            throw new NoSuchMethodError(cls.getName() + "#" + name + " for " + args.length
                    + " arguments");
        }
        found.setAccessible(true);
        synchronized (METHODS) {
            METHODS.put(k, found);
        }
        return found;
    }

    private static boolean fits(Class<?>[] params, Object[] args) {
        if (params.length != args.length) return false;
        for (int i = 0; i < params.length; i++) {
            Object a = args[i];
            Class<?> p = params[i];
            if (a == null) {
                if (p.isPrimitive()) return false;
                continue;
            }
            if (p.isPrimitive()) p = box(p);
            if (!p.isInstance(a)) return false;
        }
        return true;
    }

    private static Class<?> box(Class<?> p) {
        if (p == int.class) return Integer.class;
        if (p == long.class) return Long.class;
        if (p == float.class) return Float.class;
        if (p == double.class) return Double.class;
        if (p == boolean.class) return Boolean.class;
        if (p == byte.class) return Byte.class;
        if (p == short.class) return Short.class;
        if (p == char.class) return Character.class;
        return p;
    }

    private static Object invoke(Method m, Object obj, Object[] args) {
        try {
            return m.invoke(obj, args);
        } catch (InvocationTargetException e) {
            throw unwrap(e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    private static RuntimeException unwrap(InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) return (RuntimeException) cause;
        if (cause instanceof Error) throw (Error) cause;
        return new RuntimeException(cause);
    }
}
