package com.github.vfyjxf.nee.utils;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ReflectionHelper {


    private static final Map<FieldDescriptor, Field> FIELD_CACHE = new HashMap<>();
    private static final Map<MethodDescriptor, Method> METHOD_CACHE = new HashMap<>();

    public static Optional<Class<?>> getClassByName(String className) {
        try {
            return Optional.of(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Not support obfuscation method.
     */
    @SuppressWarnings("unchecked")
    public static <T, R> R invoke(Class<? extends T> classToAccess, T object, String methodName, Object... args) {
        Method method = methodHelper(methodName, classToAccess, args);
        if (method == null) return null;
        try {
            return (R) method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    public static <T> T possibleValueGetter(@Nonnull List<Class<?>> classes, Object object, @Nonnull String fieldName) {
        for (Class<?> aClass : classes) {
            if (aClass == null || !aClass.isInstance(object)) continue;
            T value = getFieldValue(aClass, object, fieldName);
            if (value != null) return value;
        }
        return null;
    }

    public static <T> T compositeInvoker(@Nonnull List<Class<?>> classes, Object object, boolean nonReturn, String methodName, Object... args) {
        for (Class<?> aClass : classes) {
            if (aClass == null || !aClass.isInstance(object)) continue;
            Method method = methodHelper(methodName, aClass, args);
            try {
                if (nonReturn) {
                    method.invoke(object, args);
                    return null;
                } else {
                    return (T) method.invoke(object, args);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

        }
        return null;
    }

    public static <T, R> R possibleCalls(Class<? extends T> classToAccess, T object, List<String> methodNames, Object... args) {
        for (String methodName : methodNames) {
            Method method = methodHelper(methodName, classToAccess, args);
            if (method == null) continue;
            try {
                return (R) method.invoke(object, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private static Method methodHelper(String methodName, Class<?> aClass, Object[] args) {
        Class<?>[] argTypes = Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);
        MethodDescriptor methodDescriptor = new MethodDescriptor(aClass, methodName, argTypes);
        Method method = METHOD_CACHE.get(methodDescriptor);
        if (method == null) {
            try {
                method = aClass.getDeclaredMethod(methodName, argTypes);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
            } catch (NoSuchMethodException ignored) {
            }
            METHOD_CACHE.put(methodDescriptor, method);
        }
        return method;
    }

    public static <T> Field findField(@Nonnull Class<? extends T> classToAccess, @Nonnull String fieldName) {
        FieldDescriptor fieldDescriptor = new FieldDescriptor(classToAccess, fieldName);
        Field field = FIELD_CACHE.get(fieldDescriptor);
        if (field == null) {
            field = ObfuscationReflectionHelper.findField(fieldDescriptor.clazz, fieldDescriptor.fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            FIELD_CACHE.put(fieldDescriptor, field);
        }
        return field;
    }

    public static <T, E> T getFieldValue(@Nonnull Class<? extends E> classToAccess, @Nonnull E object, @Nonnull String fieldName) {
        return getFieldValue(object, new FieldDescriptor(classToAccess, fieldName, false));
    }

    public static <T> T getStaticFieldValue(@Nonnull Class<?> classToAccess, @Nonnull String fieldName) {
        return getFieldValue(null, new FieldDescriptor(classToAccess, fieldName, true));
    }

    public static <T, E> void setPrivateValue(Class<? super T> classToAccess, @Nullable T instance, @Nullable E value, String fieldName) {
        try {
            findField(classToAccess, fieldName).set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getFieldValue(@Nullable Object obj, @Nonnull FieldDescriptor fieldDescriptor) {
        try {
            return (T) (findField(fieldDescriptor.clazz, fieldDescriptor.fieldName).get(obj));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getMethod(@Nonnull Class<?> clazz, @Nonnull String methodName, Class<?> returnType, Class<?>... parameterTypes) {
        MethodDescriptor methodDescriptor = new MethodDescriptor(clazz, methodName, parameterTypes);
        Method method = METHOD_CACHE.get(methodDescriptor);
        if (method == null) {
            method = ObfuscationReflectionHelper.findMethod(clazz, methodName, returnType, parameterTypes);
            METHOD_CACHE.put(methodDescriptor, method);
        }
        return method;
    }

    private static class FieldDescriptor {
        @Nonnull
        private final Class<?> clazz;
        @Nonnull
        private final String fieldName;
        private final boolean isStatic;

        public FieldDescriptor(@Nonnull Class<?> clazz, @Nonnull String fieldName, boolean isStatic) {
            this.clazz = clazz;
            this.fieldName = fieldName;
            this.isStatic = isStatic;
        }

        public FieldDescriptor(@Nonnull Class<?> clazz, @Nonnull String fieldName) {
            this(clazz, fieldName, false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FieldDescriptor that = (FieldDescriptor) o;
            return isStatic == that.isStatic && clazz.equals(that.clazz) && fieldName.equals(that.fieldName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, fieldName, isStatic);
        }

    }

    private static class MethodDescriptor {
        @Nonnull
        private final Class<?> clazz;
        @Nonnull
        private final String methodName;
        @Nonnull
        private final Class<?>[] parameterTypes;

        public MethodDescriptor(@Nonnull Class<?> clazz, @Nonnull String methodName, @Nonnull Class<?>[] parameterTypes) {
            this.clazz = clazz;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodDescriptor that = (MethodDescriptor) o;
            return clazz.equals(that.clazz) &&
                    methodName.equals(that.methodName) &&
                    Arrays.equals(parameterTypes, that.parameterTypes);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(clazz, methodName);
            result = 31 * result + Arrays.hashCode(parameterTypes);
            return result;
        }

    }

}
