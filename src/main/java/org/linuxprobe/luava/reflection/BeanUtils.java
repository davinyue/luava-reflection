package org.linuxprobe.luava.reflection;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Callable;

public class BeanUtils {
    /**
     * bean属性拷贝,把source bean的属性拷贝到target bean
     *
     * @param source       the source bean
     * @param target       the target bean
     * @param ignoreFields 忽略属性
     */
    public static void copyProperties(Object source, Object target, String... ignoreFields) {
        CopyOptions copyOptions = new CopyOptions();
        copyOptions.setIgnoreFields(Arrays.asList(ignoreFields));
        BeanUtils.copyProperties(source, target, copyOptions);
    }

    /**
     * bean属性拷贝,把source bean的属性拷贝到target bean
     *
     * @param source the source bean
     * @param target the target bean
     */
    public static void copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, new CopyOptions());
    }

    /**
     * bean属性拷贝,把source bean的属性拷贝到target bean
     *
     * @param source      the source bean
     * @param target      the target bean
     * @param copyOptions 拷贝属性
     */
    public static void copyProperties(Object source, Object target, CopyOptions copyOptions) {
        if (source == null || target == null) {
            return;
        }
        if (copyOptions == null) {
            copyOptions = new CopyOptions();
        }
        if (copyOptions.getFieldMapping() == null) {
            copyOptions.setFieldMapping(new HashMap<>());
        }
        if (copyOptions.getIgnoreFields() == null) {
            copyOptions.setIgnoreFields(new LinkedList<>());
        }
        Map<String, Field> sourceFieldMap = BeanUtils.getFieldMap(source.getClass());
        Map<String, Field> targetFieldMap = BeanUtils.getFieldMap(target.getClass());
        Set<String> fieldNames = sourceFieldMap.keySet();
        for (String fieldName : fieldNames) {
            Field sourceField = sourceFieldMap.get(fieldName);
            String targetFieldName = copyOptions.getFieldMapping().get(fieldName);
            if (targetFieldName == null) {
                targetFieldName = fieldName;
            }
            Field targetField = targetFieldMap.get(targetFieldName);
            if (targetField == null) {
                continue;
            }
            if (!copyOptions.getIgnoreFields().contains(fieldName)) {
                Object sourceValue = ReflectionUtils.getFieldValue(source, sourceField, copyOptions.isUseGetter());
                if (!(sourceValue == null && copyOptions.isIgnoreNullValue())) {
                    try {
                        ReflectionUtils.setFieldValue(target, targetField, sourceValue, copyOptions.isUseSetter());
                    } catch (Exception e) {
                        if (!copyOptions.isIgnoreError()) {
                            throw new IllegalArgumentException(
                                    "can't copy " + fieldName + " to " + targetField.getName(), e);
                        }
                    }
                }
            }
        }
    }

    /**
     * 是否需要递归
     */
    private static boolean isNeedRecursion(Object value) {
        Class<?> valueType = value.getClass();
        if (byte.class.isAssignableFrom(valueType) || Byte.class.isAssignableFrom(valueType)) {
            return false;
        } else if (char.class.isAssignableFrom(valueType)) {
            return false;
        } else if (Character.class.isAssignableFrom(valueType)) {
            return false;
        } else if (short.class.isAssignableFrom(valueType)) {
            return false;
        } else if (int.class.isAssignableFrom(valueType)) {
            return false;
        } else if (long.class.isAssignableFrom(valueType)) {
            return false;
        } else if (float.class.isAssignableFrom(valueType)) {
            return false;
        } else if (double.class.isAssignableFrom(valueType)) {
            return false;
        } else if (CharSequence.class.isAssignableFrom(valueType)) {
            return false;
        } else if (Number.class.isAssignableFrom(valueType)) {
            return false;
        } else if (InputStream.class.isAssignableFrom(valueType)) {
            return false;
        } else if (OutputStream.class.isAssignableFrom(valueType)) {
            return false;
        } else if (Readable.class.isAssignableFrom(valueType)) {
            return false;
        } else if (Writer.class.isAssignableFrom(valueType)) {
            return false;
        } else if (Runnable.class.isAssignableFrom(valueType)) {
            return false;
        } else {
            return !Callable.class.isAssignableFrom(valueType);
        }
    }

    /**
     * 转换成map
     *
     * @param source     需要转换成map的bean
     * @param userGatter 使用get函数
     */
    public static Map<String, Object> beanToMap(Object source, boolean userGatter) {
        if (source == null) {
            throw new IllegalArgumentException("source can not be null");
        } else {
            Map<String, Object> result = new HashMap<>();
            Map<String, Field> sourceFieldMap = BeanUtils.getFieldMap(source.getClass());
            Set<String> fieldNames = sourceFieldMap.keySet();
            for (String fieldName : fieldNames) {
                Field sourceField = sourceFieldMap.get(fieldName);
                Object sourceValue = ReflectionUtils.getFieldValue(source, sourceField, userGatter);
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
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                continue;
            }
            fieldMap.put(field.getName(), field);
        }
        return fieldMap;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class CopyOptions {
        /**
         * 是否忽略空值，当源对象的值为null时，true: 忽略而不注入此值，false: 注入null
         */
        private boolean ignoreNullValue = false;
        /**
         * 忽略的目标对象中属性列表，设置一个属性列表，不拷贝这些属性值
         */
        private List<String> ignoreFields = new LinkedList<>();
        /**
         * 是否忽略字段注入错误
         */
        private boolean ignoreError = false;
        /**
         * 拷贝属性的字段映射，用于不同的属性之前拷贝做对应表用
         */
        private Map<String, String> fieldMapping = new HashMap<>();
        /**
         * 使用get函数
         */
        private boolean useGetter = true;
        /**
         * 使用set函数
         */
        private boolean useSetter = true;

        public CopyOptions addIgnorePropertie(String propertie) {
            this.ignoreFields.add(propertie);
            return this;
        }

        public CopyOptions removeIgnorePropertie(String propertie) {
            this.ignoreFields.remove(propertie);
            return this;
        }

        public CopyOptions addFieldMapping(String sourceFieldName, String targetFieldname) {
            this.fieldMapping.put(sourceFieldName, targetFieldname);
            return this;
        }

        public CopyOptions removeFieldMapping(String sourceFieldName) {
            this.fieldMapping.remove(sourceFieldName);
            return this;
        }
    }
}
