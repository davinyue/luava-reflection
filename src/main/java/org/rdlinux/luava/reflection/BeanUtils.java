package org.rdlinux.luava.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BeanUtils {
    /**
     * bean属性拷贝,把source bean的属性拷贝到target bean
     *
     * @param source       the source bean
     * @param target       the target bean
     * @param ignoreFields 忽略属性
     */
    public static void copyProperties(Object source, Object target, String... ignoreFields) {
        CopyOption copyOptions = new CopyOption();
        copyOptions.addIgnoreFields(ignoreFields);
        BeanUtils.copyProperties(source, target, copyOptions);
    }

    /**
     * bean属性拷贝,把source bean的属性拷贝到target bean
     *
     * @param source     the source bean
     * @param target     the target bean
     * @param copyOption 拷贝属性
     */
    public static void copyProperties(Object source, Object target, CopyOption copyOption) {
        if (source == null || target == null) {
            return;
        }
        if (copyOption == null) {
            copyOption = new CopyOption();
        }
        Map<String, Field> sourceFieldMap = BeanUtils.getFieldMap(source.getClass());
        Map<String, Field> targetFieldMap = BeanUtils.getFieldMap(target.getClass());
        Set<String> fieldNames = sourceFieldMap.keySet();
        for (String fieldName : fieldNames) {
            if (copyOption.getIgnoreFields().contains(fieldName)) {
                continue;
            }
            String targetFieldName = copyOption.getFieldMapping().get(fieldName);
            if (targetFieldName == null) {
                targetFieldName = fieldName;
            }

            Field targetField = targetFieldMap.get(targetFieldName);
            if (targetField == null) {
                continue;
            }

            Field sourceField = sourceFieldMap.get(fieldName);
            if (sourceField == null) {
                continue;
            }

            Object sourceValue = ReflectionUtils.getFieldValue(source, sourceField, copyOption.isUseGetter());
            if (sourceValue == null && copyOption.isIgnoreNullValue()) {
                continue;
            }

            try {
                ReflectionUtils.setFieldValue(target, targetField, sourceValue, copyOption.isUseSetter());
            } catch (Exception e) {
                if (!copyOption.isIgnoreError()) {
                    throw new IllegalArgumentException(
                            String.format("Can not copy the value of the field named '%s' to the field '%s'.",
                                    fieldName, targetField.getName()),
                            e);
                }
            }
        }
    }

//    /**
//     * 是否需要递归
//     */
//    private static boolean isNeedRecursion(Object value) {
//        Class<?> valueType = value.getClass();
//        if (byte.class.isAssignableFrom(valueType) || Byte.class.isAssignableFrom(valueType)) {
//            return false;
//        } else if (char.class.isAssignableFrom(valueType)) {
//            return false;
//        } else if (Character.class.isAssignableFrom(valueType)) {
//            return false;
//        } else if (short.class.isAssignableFrom(valueType)) {
//            return false;
//        } else if (int.class.isAssignableFrom(valueType)) {
//            return false;
//        } else if (long.class.isAssignableFrom(valueType)) {
//            return false;
//        } else if (float.class.isAssignableFrom(valueType)) {
//            return false;
//        } else if (double.class.isAssignableFrom(valueType)) {
//            return false;
//        } else if (CharSequence.class.isAssignableFrom(valueType)) {
//            return false;
//        } else if (Number.class.isAssignableFrom(valueType)) {
//            return false;
//        } else if (InputStream.class.isAssignableFrom(valueType)) {
//            return false;
//        } else if (OutputStream.class.isAssignableFrom(valueType)) {
//            return false;
//        } else if (Readable.class.isAssignableFrom(valueType)) {
//            return false;
//        } else if (Writer.class.isAssignableFrom(valueType)) {
//            return false;
//        } else if (Runnable.class.isAssignableFrom(valueType)) {
//            return false;
//        } else {
//            return !Callable.class.isAssignableFrom(valueType);
//        }
//    }

    /**
     * 转换成map
     *
     * @param source    需要转换成map的bean
     * @param useGetter 使用get函数
     */
    public static Map<String, Object> beanToMap(Object source, boolean useGetter) {
        if (source == null) {
            throw new IllegalArgumentException("source can not be null");
        } else {
            Map<String, Object> result = new HashMap<>();
            Map<String, Field> sourceFieldMap = BeanUtils.getFieldMap(source.getClass());
            Set<String> fieldNames = sourceFieldMap.keySet();
            for (String fieldName : fieldNames) {
                Field sourceField = sourceFieldMap.get(fieldName);
                Object sourceValue = ReflectionUtils.getFieldValue(source, sourceField, useGetter);
                if (sourceValue != null) {
                    result.put("fieldName", sourceValue);
                }
            }
            return result;
        }
    }

    /**
     * 转换成map
     *
     * @param source 需要转换成map的bean
     */
    public static Map<String, Object> beanToMap(Object source) {
        return BeanUtils.beanToMap(source, true);
    }

    private static Map<String, Field> getFieldMap(Class<?> clazz) {
        List<Field> fields = ReflectionUtils.getAllFields(clazz);
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                continue;
            }
            fieldMap.put(field.getName(), field);
        }
        return fieldMap;
    }
}
