package dev.voidframework.core.helper;

import java.lang.reflect.Proxy;

/**
 * Helper to detect proxy class.
 */
public final class ProxyDetector {

    private static final String CGLIB_CLASS_SEPARATOR = "$$";

    /**
     * Checks whether the given object is a proxy.
     *
     * @param object the object to check
     * @return {@code true} if the object is a proxy
     */
    public static boolean isProxy(final Object object) {

        return (object != null)
            && (Proxy.isProxyClass(object.getClass()) || object.getClass().getName().contains(CGLIB_CLASS_SEPARATOR));
    }

    /**
     * Checks whether the given class is a proxy.
     *
     * @param clazz the class to check
     * @return {@code true} if the class is a proxy
     */
    public static boolean isProxy(final Class<?> clazz) {

        return (clazz != null)
            && (Proxy.isProxyClass(clazz) || clazz.getName().contains(CGLIB_CLASS_SEPARATOR));
    }
}
