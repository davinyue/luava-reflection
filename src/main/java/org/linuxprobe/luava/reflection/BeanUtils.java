package org.linuxprobe.luava.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
		copyProperties(source, target, copyOptions);
	}

	/**
	 * bean属性拷贝,把source bean的属性拷贝到target bean
	 * 
	 * @param source the source bean
	 * @param target the target bean
	 */
	public static void copyProperties(Object source, Object target) {
		copyProperties(source, target, new CopyOptions());
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
		Map<String, Field> sourceFieldMap = getFieldMap(source.getClass());
		Map<String, Field> targetFieldMap = getFieldMap(target.getClass());
		Set<String> fieldNames = sourceFieldMap.keySet();
		for (String fieldName : fieldNames) {
			Field sourceField = sourceFieldMap.get(fieldName);
			if (copyOptions.getIgnoreFields().contains(fieldName)) {
				continue;
			} else {
				Object sourceValue = null;
				if (copyOptions.isUseGetter()) {
					sourceValue = ReflectionUtils.getFieldValue(source, sourceField, true);
				} else {
					sourceValue = ReflectionUtils.getFieldValue(source, sourceField, false);
				}
				if (sourceValue == null && copyOptions.isIgnoreNullValue()) {
					continue;
				} else {
					String targetFieldName = copyOptions.getFieldMapping().get(fieldName);
					if (targetFieldName == null) {
						targetFieldName = fieldName;
					}
					Field targetField = targetFieldMap.get(targetFieldName);
					if (targetField == null) {
						continue;
					}
					try {
						if (copyOptions.isUseSetter()) {
							ReflectionUtils.setFieldValue(target, targetField, sourceValue, true);
						} else {
							ReflectionUtils.setFieldValue(target, targetField, sourceValue, false);
						}
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
		/** 是否忽略空值，当源对象的值为null时，true: 忽略而不注入此值，false: 注入null */
		private boolean ignoreNullValue = false;
		/** 忽略的目标对象中属性列表，设置一个属性列表，不拷贝这些属性值 */
		private List<String> ignoreFields = new LinkedList<>();
		/** 是否忽略字段注入错误 */
		private boolean ignoreError = false;
		/** 拷贝属性的字段映射，用于不同的属性之前拷贝做对应表用 */
		private Map<String, String> fieldMapping = new HashMap<>();
		/** 使用get函数 */
		private boolean useGetter = true;
		/** 使用set函数 */
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
