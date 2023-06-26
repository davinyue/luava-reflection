package org.rdlinux.luava.reflection;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 拷贝选项
 */
public class CopyOption {
    /**
     * 拷贝属性的字段映射，用于不同的属性之前拷贝做对应表用
     */
    private Map<String, String> fieldMapping;
    /**
     * 是否忽略字段注入错误
     */
    private boolean ignoreError;
    /**
     * 忽略的目标对象中属性名称列表，设置一个属性名称列表，不拷贝这些属性值
     */
    private List<String> ignoreFields;
    /**
     * 是否忽略空值，当源对象的值为null时，true: 忽略而不注入此值，false: 注入null
     */
    private boolean ignoreNullValue;
    /**
     * 使用get方法
     */
    private boolean useGetter;
    /**
     * 使用set方法
     */
    private boolean useSetter;

    /**
     * 默认构造方法
     */
    public CopyOption() {
        this.fieldMapping = new HashMap<>();
        this.ignoreError = false;
        this.ignoreFields = new LinkedList<>();
        this.ignoreNullValue = false;
        this.useGetter = true;
        this.useSetter = true;
    }

    public Map<String, String> getFieldMapping() {
        return this.fieldMapping;
    }

    public boolean isIgnoreError() {
        return this.ignoreError;
    }

    public CopyOption setIgnoreError(boolean ignoreError) {
        this.ignoreError = ignoreError;
        return this;
    }

    public List<String> getIgnoreFields() {
        return this.ignoreFields;
    }

    public boolean isIgnoreNullValue() {
        return this.ignoreNullValue;
    }

    public CopyOption setIgnoreNullValue(boolean ignoreNullValue) {
        this.ignoreNullValue = ignoreNullValue;
        return this;
    }

    public boolean isUseGetter() {
        return this.useGetter;
    }

    public CopyOption setUseGetter(boolean useGetter) {
        this.useGetter = useGetter;
        return this;
    }

    public boolean isUseSetter() {
        return this.useSetter;
    }

    public CopyOption setUseSetter(boolean useSetter) {
        this.useSetter = useSetter;
        return this;
    }

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
        if (this.fieldMapping.containsKey(sourceFieldName)) {
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
        this.fieldMapping.remove(sourceFieldName);
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
            this.ignoreFields.remove(fieldName);
        }
        return this;
    }
}
