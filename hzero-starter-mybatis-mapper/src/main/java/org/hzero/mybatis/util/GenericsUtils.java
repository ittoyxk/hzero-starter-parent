package org.hzero.mybatis.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

/**
 * <p>
 * 泛型工具类
 * </p>
 *
 * @author qingsheng.chen 2018/7/10 星期二 14:04
 */
public class GenericsUtils {
    private GenericsUtils() {
    }

    public static Class<?> getSuperClassGenricType(Class<?> clazz) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (params.length == 0) {
            return Object.class;
        }
        if (!(params[0] instanceof Class)) {
            return Object.class;
        }
        return (Class<?>) params[0];
    }

    /**
     * For a specific class that implements or extends a parametrized type returns the
     * parameter of that interface at a given position. For example, for this class:
     *
     * <pre>
     * {@code
     * class MessageChannelBinder implements Binder<MessageChannel, ?, ?>
     * }
     *
     * <pre>
     * {@code
     * getParameterType(MessageChannelBinder.class, Binder.class, 0);
     * }
     *
     * will return {@code Binder}
     *
     * @param evaluatedClass the evaluated class
     * @param interfaceClass the parametrized interface
     * @param position the position
     * @return the parameter type if any
     * @throws IllegalStateException if the evaluated class does not implement the
     * interface or
     */
    public static Class<?> getParameterType(Class<?> evaluatedClass, Class<?> interfaceClass, int position) {
        Class<?> bindableType = null;
        Assert.isTrue(interfaceClass.isInterface(), "'interfaceClass' must be an interface");
        if (!interfaceClass.isAssignableFrom(evaluatedClass)) {
            throw new IllegalStateException(evaluatedClass + " does not implement " + interfaceClass);
        }
        ResolvableType currentType = ResolvableType.forType(evaluatedClass);
        while (!Object.class.equals(currentType.getRawClass()) && bindableType == null) {
            ResolvableType[] interfaces = currentType.getInterfaces();
            ResolvableType resolvableType = null;
            for (ResolvableType interfaceType : interfaces) {
                if (interfaceClass.equals(interfaceType.getRawClass())) {
                    resolvableType = interfaceType;
                    break;
                }
            }
            if (resolvableType == null) {
                currentType = currentType.getSuperType();
            }
            else {
                ResolvableType[] generics = resolvableType.getGenerics();
                ResolvableType generic = generics[position];
                Class<?> resolvedParameter = generic.resolve();
                if (resolvedParameter != null) {
                    bindableType = resolvedParameter;
                }
                else {
                    bindableType = Object.class;
                }
            }
        }
        if (bindableType == null) {
            throw new IllegalStateException("Cannot find parameter of " + evaluatedClass.getName() + " for "
                    + interfaceClass + " at position " + position);
        }
        return bindableType;
    }
}
