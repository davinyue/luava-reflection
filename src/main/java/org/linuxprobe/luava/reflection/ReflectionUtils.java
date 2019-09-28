package org.linuxprobe.luava.reflection;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ReflectionUtils {
    /**
     * 获取该类型的所有属性，包括它的超类的属性
     *
     * @param objClass 要查找的类类型
     */
    public static List<Field> getAllFields(Class<?> objClass) {
        objClass = getRealCalssOfProxyClass(objClass);
        List<Field> fields = new LinkedList<>();
        while (objClass != Object.class) {
            fields.addAll(Arrays.asList(objClass.getDeclaredFields()));
            objClass = objClass.getSuperclass();
        }
        return fields;
    }

    /**
     * 根据属性名称和类型查找属性
     *
     * @param objClass  要查找的类类型
     * @param fieldName 属性名称
     * @param fieldType 属性类型
     */
    public static Field getField(Class<?> objClass, String fieldName, Class<?> fieldType) {
        List<Field> fields = getAllFields(objClass);
        for (Field field : fields) {
            if (field.getName().equals(fieldName) && field.getType() == fieldType) {
                return field;
            }
        }
        return null;
    }

    /**
     * 根据属性名称查找属性
     *
     * @param objClass  要查找的类类型
     * @param fieldName 属性名称
     */
    public static Field getField(Class<?> objClass, String fieldName) {
        List<Field> fields = getAllFields(objClass);
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    /**
     * 获取属性的set方法
     *
     * @param objClass 要查找的类类型
     * @param field    属性
     */
    public static Method getMethodOfFieldSet(Class<?> objClass, Field field) {
        if (objClass == null || field == null) {
            return null;
        }
        objClass = getRealCalssOfProxyClass(objClass);
        String fieldName = field.getName();
        String funSuffix = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method methodOfSet = null;
        try {
            methodOfSet = objClass.getMethod("set" + funSuffix, field.getType());
        } catch (NoSuchMethodException | SecurityException e) {
            if (objClass == Object.class) {
                throw new IllegalArgumentException(e);
            } else {
                return getMethodOfFieldSet(objClass.getSuperclass(), field);
            }
        }
        return methodOfSet;
    }

    /**
     * 获取属性的set方法
     *
     * @param objClass  要查找的类类型
     * @param fieldName 属性名称
     */
    public static Method getMethodOfFieldSet(Class<?> objClass, String fieldName) {
        if (objClass == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        } else {
            Field field = getField(objClass, fieldName);
            return getMethodOfFieldSet(objClass, field);
        }
    }

    /**
     * 获取属性的get方法
     *
     * @param objClass 要查找的类类型
     * @param field    属性
     */
    public static Method getMethodOfFieldGet(Class<?> objClass, Field field) {
        if (objClass == null || field == null) {
            return null;
        }
        objClass = getRealCalssOfProxyClass(objClass);
        String fieldName = field.getName();
        String prefix = "get";
        String funSuffix = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        if (boolean.class.isAssignableFrom(field.getType())) {
            if (fieldName.matches("^is[A-Z0-9_]+.*$")) {
                prefix = "";
                funSuffix = fieldName;
            } else {
                prefix = "is";
            }
        }
        Method methodOfGet = null;
        try {
            methodOfGet = objClass.getMethod(prefix + funSuffix);
        } catch (NoSuchMethodException | SecurityException e) {
            if (objClass == Object.class) {
                throw new IllegalArgumentException(e);
            } else {
                return getMethodOfFieldGet(objClass.getSuperclass(), field);
            }
        }
        return methodOfGet;
    }

    /**
     * 获取属性的get方法
     *
     * @param objClass  要查找的类类型
     * @param fieldName 属性名称
     */
    public static Method getMethodOfFieldGet(Class<?> objClass, String fieldName) {
        if (objClass == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        } else {
            Field field = getField(objClass, fieldName);
            return getMethodOfFieldGet(objClass, field);
        }
    }

    /**
     * 设置属性值
     *
     * @param obj        要设置的对象
     * @param field      要设置的属性
     * @param value      要设置的值
     * @param userSetter 是否使用set函数
     */
    public static void setFieldValue(Object obj, Field field, Object value, boolean userSetter) {
        if (obj == null || field == null) {
            return;
        }
        Class<?> objClass = getRealCalssOfProxyClass(obj.getClass());
        if (!userSetter) {
            field.setAccessible(true);
            try {
                field.set(objClass, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            Method methodOfSet = getMethodOfFieldSet(objClass, field);
            methodOfSet.setAccessible(true);
            try {
                methodOfSet.invoke(obj, value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    /**
     * 设置属性值
     *
     * @param obj   要设置的对象
     * @param field 要设置的属性
     * @param value 要设置的值
     */
    public static void setFieldValue(Object obj, Field field, Object value) {
        setFieldValue(obj, field, value, false);
    }

    /**
     * 设置属性值
     *
     * @param obj        要设置的对象
     * @param fieldName  要设置的属性名称
     * @param value      要设置的值
     * @param userSetter 是否使用set函数
     */
    public static void setFieldValue(Object obj, String fieldName, Object value, boolean userSetter) {
        if (obj == null || fieldName == null || fieldName.isEmpty()) {
            return;
        } else {
            Field field = getField(obj.getClass(), fieldName);
            if (field != null) {
                setFieldValue(obj, field, value, userSetter);
            }
        }
    }

    /**
     * 设置属性值
     *
     * @param obj       要设置的对象
     * @param fieldName 要设置的属性名称
     * @param value     要设置的值
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        setFieldValue(obj, fieldName, value, false);
    }

    /**
     * 获取属性值
     *
     * @param obj        要获取的对象
     * @param field      要获取的属性
     * @param userGetter 是否使用get函数
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object obj, Field field, boolean userGetter) {
        if (obj == null || field == null) {
            return null;
        }
        Class<?> objClass = getRealCalssOfProxyClass(obj.getClass());
        T result = null;
        if (!userGetter) {
            field.setAccessible(true);
            try {
                result = (T) field.get(obj);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            Method getMethod = getMethodOfFieldGet(objClass, field);
            getMethod.setAccessible(true);
            try {
                result = (T) getMethod.invoke(obj);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return result;
    }

    /**
     * 获取属性值
     *
     * @param obj   要获取的对象
     * @param field 要获取的属性
     */
    public static <T> T getFieldValue(Object obj, Field field) {
        return getFieldValue(obj, field, false);
    }

    /**
     * 获取属性值
     *
     * @param obj        要获取的对象
     * @param fieldName  要获取的属性名称
     * @param userGetter 是否使用get函数
     */
    public static <T> T getFieldValue(Object obj, String fieldName, boolean userGetter) {
        if (obj == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        }
        Field field = getField(obj.getClass(), fieldName);
        if (field == null) {
            return null;
        } else {
            return getFieldValue(obj, field, userGetter);
        }
    }

    /**
     * 获取属性值
     *
     * @param obj       要获取的对象
     * @param fieldName 要获取的属性名称
     */
    public static <T> T getFieldValue(Object obj, String fieldName) {
        return getFieldValue(obj, fieldName, false);
    }

    /**
     * 根据set方法和get方法获取属性名称
     *
     * @param method set或get方法
     */
    public static String getFieldNameByMethod(Method method) {
        String fieldName = method.getName().substring(3, method.getName().length());
        fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1, fieldName.length());
        return fieldName;
    }

    /**
     * 根据set方法和get方法获取属性名称
     *
     * @param objClass 要查找的类类型
     * @param method   set或get方法
     */
    public static Field getFieldByMethod(Class<?> objClass, Method method) {
        objClass = getRealCalssOfProxyClass(objClass);
        String fieldName = getFieldNameByMethod(method);
        return getField(objClass, fieldName);
    }

    /**
     * 获取代理类的真实类
     *
     * @param objClass 要获取的类类型
     */
    public static Class<?> getRealCalssOfProxyClass(Class<?> objClass) {
        while (objClass.getSimpleName().contains("CGLIB$")) {
            objClass = objClass.getSuperclass();
        }
        return objClass;
    }

    /**
     * 获取类的父类泛型类型参数
     *
     * @param objClass 要获取的类类型
     * @param order    获取第几个泛型参数
     */
    public static Class<?> getGenericSuperclass(Class<?> objClass, int order) {
        Type genType = objClass.getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        return (Class<?>) params[order];
    }

    /**
     * 获取field的泛型类型参数,eg List&lt;T&gt;, 获取T的类型
     *
     * @param field 要获取的field
     * @param order 获取第几个泛型参数
     */
    public static Class<?> getFiledGenericclass(Field field, int order) {
        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
        if (parameterizedType == null) {
            throw new IllegalArgumentException("必须指定泛型类型");
        }
        Type type = parameterizedType.getActualTypeArguments()[0];
        Class<?> genericsCalss = null;
        try {
            genericsCalss = Class.forName(type.getTypeName());
            return genericsCalss;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 判断一个类是否是代理类
     *
     * @param objClass 要判断的类类型
     */
    public static boolean isProxyClass(Class<?> objClass) {
        if (objClass.getSimpleName().contains("CGLIB$")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取方法
     *
     * @param objClass       要获取的对象的类类型
     * @param name           方法名称
     * @param parameterTypes 方法参数类型
     */
    public static Method getMethod(Class<?> objClass, String name, Class<?>... parameterTypes) {
        try {
            Method method = objClass.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException | SecurityException e) {
            if (objClass == Object.class) {
                throw new IllegalArgumentException(e);
            } else {
                return getMethod(objClass.getSuperclass(), name, parameterTypes);
            }
        }
    }

    /**
     * 获取方法
     *
     * @param objClass 要获取的对象的类类型
     * @param name     方法名称
     */
    public static Method getMethod(Class<?> objClass, String name) {
        return getMethod(objClass, name, (Class<?>[]) null);
    }

    /**
     * 执行方法
     *
     * @param object 需要执行的对象
     * @param method 需要执行的方法
     * @param args   参数
     */
    public static void invokeMethod(Object object, Method method, Object... args) {
        method.setAccessible(true);
        try {
            method.invoke(object, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 执行方法
     *
     * @param object 需要执行的对象
     * @param method 需要执行的方法
     */
    public static void invokeMethod(Object object, Method method) {
        invokeMethod(object, method, (Object[]) null);
    }

    /**
     * 执行方法
     *
     * @param object     需要执行的对象
     * @param methodName 需要执行的方法名称
     * @param args       参数
     */
    public static void invokeMethod(Object object, String methodName, Object... args) {
        Method method = getMethod(object.getClass(), methodName);
        invokeMethod(object, method, args);
    }

    /**
     * 执行方法
     *
     * @param object     需要执行的对象
     * @param methodName 需要执行的方法名称
     */
    public static void invokeMethod(Object object, String methodName) {
        Method method = getMethod(object.getClass(), methodName);
        invokeMethod(object, method, (Object[]) null);
    }
}
