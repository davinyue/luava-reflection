package org.linuxprobe.luava.reflection;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;

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

    /**
     * 拷贝选项
     */
    @Getter
    @Accessors(chain = true)
    public static class CopyOption {

        /**
         * 默认构造方法
         */
        public CopyOption() {
            fieldMapping = new HashMap<>();
            ignoreError = false;
            ignoreFields = new LinkedList<>();
            ignoreNullValue = false;
            useGetter = true;
            useSetter = true;
        }

        /**
         * 拷贝属性的字段映射，用于不同的属性之前拷贝做对应表用
         */
        private Map<String, String> fieldMapping;
        /**
         * 是否忽略字段注入错误
         */
        @Setter
        private boolean ignoreError;
        /**
         * 忽略的目标对象中属性名称列表，设置一个属性名称列表，不拷贝这些属性值
         */
        private List<String> ignoreFields;
        /**
         * 是否忽略空值，当源对象的值为null时，true: 忽略而不注入此值，false: 注入null
         */
        @Setter
        private boolean ignoreNullValue;
        /**
         * 使用get方法
         */
        @Setter
        private boolean useGetter;
        /**
         * 使用set方法
         */
        @Setter
        private boolean useSetter;

        /**
         * 添加 源属性与目标属性的对应关系。
         * 如果源属性名已存在，则不变化。
         *
         * @param sourceFieldName 源属性名称
         * @param targetFieldName 目标属性名称
         * @return the instance  of <tt>CopyOption</tt>
         * @throws IllegalArgumentException If the <tt>sourceFieldName</tt> or <tt>targetFieldName</tt> is whitespace, empty ("") or null.
         */
        public CopyOption addFieldMapping(String sourceFieldName, String targetFieldName) {
            if (StringUtils.isBlank(sourceFieldName)) {
                throw new IllegalArgumentException("The sourceFieldName can not be whitespace, empty (\"\") or null.");
            }
            if (StringUtils.isBlank(targetFieldName)) {
                throw new IllegalArgumentException("The targetFieldName can not be whitespace, empty (\"\") or null.");
            }
            if (fieldMapping.containsKey(sourceFieldName)) {
                return this;
            }
            this.fieldMapping.put(sourceFieldName, targetFieldName);
            return this;
        }

        /**
         * 移除 源属性与目标属性的对应关系
         * <p>
         * 如果源属性名不存在，则不变化。
         *
         * @param sourceFieldName 源属性名称
         * @return the instance  of <tt>CopyOption</tt>
         * @throws IllegalArgumentException If the <tt>sourceFieldName</tt> is whitespace, empty ("") or null.
         */
        public CopyOption removeFieldMapping(String sourceFieldName) {
            if (StringUtils.isBlank(sourceFieldName)) {
                throw new IllegalArgumentException("The sourceFieldName can not be whitespace, empty (\"\") or null.");
            }
            if (fieldMapping.containsKey(sourceFieldName)) {
                this.fieldMapping.remove(sourceFieldName);
            }
            return this;
        }

        /**
         * 添加 要忽略的属性名称。
         * 如果属性值为空白字符或空串("")或null则忽略。
         * 如果属性名已存在，则不变化。
         *
         * @param sourceFieldNames 要忽略的源对象中的属性名称
         * @return the instance  of <tt>CopyOption</tt>
         */
        public CopyOption addIgnoreFields(String... sourceFieldNames) {
            if (sourceFieldNames == null || sourceFieldNames.length == 0) {
                return this;
            }

            for (String fieldName : sourceFieldNames) {
                if (StringUtils.isBlank(fieldName)) {
                    continue;
                }
                if (this.ignoreFields.contains(fieldName)) {
                    continue;
                }
                this.ignoreFields.add(fieldName);
            }
            return this;
        }

        /**
         * 移除 要忽略的属性名称。
         * 如果属性值为空白字符或空串("")或null则忽略。
         * 如果属性名不存在，则不变化。
         *
         * @param sourceFieldNames 要忽略的源对象中的属性名称
         * @return the instance  of <tt>CopyOption</tt>
         */
        public CopyOption removeIgnoreFields(String... sourceFieldNames) {
            if (sourceFieldNames == null || sourceFieldNames.length == 0) {
                return this;
            }

            for (String fieldName : sourceFieldNames) {
                if (StringUtils.isBlank(fieldName)) {
                    continue;
                }
                if (this.ignoreFields.contains(fieldName)) {
                    this.ignoreFields.remove(fieldName);
                }
            }
            return this;
        }
    }
}
