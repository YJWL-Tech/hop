/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.pipeline.transforms.drools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.util.Utils;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.ITransformData;
import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;

public class DecisionTableData extends BaseTransformData implements ITransformData {
  private static final Class<?> PKG = DecisionTable.class; // for i18n purposes

  private IRowMeta outputRowMeta;
  private IRowMeta inputRowMeta;
  private String decisionTableFile;
  private String javaClassName;
  private String worksheetName;
  private List<FieldMapping> fieldMappings;
  private List<Object[]> inputRows = new ArrayList<>();
  private List<Object> factObjects = new ArrayList<>();
  private List<Object> resultObjects = new ArrayList<>();
  private KieHelper kieHelper;
  private String generatedDRL;
  private boolean keepInputFields;

  public boolean isKeepInputFields() {
    return keepInputFields;
  }

  public void setKeepInputFields(boolean keepInputFields) {
    this.keepInputFields = keepInputFields;
  }

  public IRowMeta getOutputRowMeta() {
    return outputRowMeta;
  }

  public void setOutputRowMeta(IRowMeta outputRowMeta) {
    this.outputRowMeta = outputRowMeta;
  }

  public IRowMeta getInputRowMeta() {
    return inputRowMeta;
  }

  public void setInputRowMeta(IRowMeta inputRowMeta) {
    this.inputRowMeta = inputRowMeta;
  }

  public String getDecisionTableFile() {
    return decisionTableFile;
  }

  public void setDecisionTableFile(String decisionTableFile) {
    this.decisionTableFile = decisionTableFile;
  }

  public String getJavaClassName() {
    return javaClassName;
  }

  public void setJavaClassName(String javaClassName) {
    this.javaClassName = javaClassName;
  }

  public String getWorksheetName() {
    return worksheetName;
  }

  public void setWorksheetName(String worksheetName) {
    this.worksheetName = worksheetName;
  }

  public List<FieldMapping> getFieldMappings() {
    return fieldMappings;
  }

  public void setFieldMappings(List<FieldMapping> fieldMappings) {
    this.fieldMappings = fieldMappings;
  }

  public List<Object[]> getInputRows() {
    return inputRows;
  }

  public List<Object> getFactObjects() {
    return factObjects;
  }

  public List<Object> getResultObjects() {
    return resultObjects;
  }

  public String getGeneratedDRL() {
    return generatedDRL;
  }

  /** Initialize the decision table by loading and parsing the Excel file */
  public void initializeDecisionTable() throws RuleValidationException {
    try {
      // Load and parse the Excel decision table using Drools
      parseExcelDecisionTable();

      // Initialize Drools KieHelper with the generated DRL
      initializeDrools();

    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize decision table: " + e.getMessage(), e);
    }
  }

  /** Parse Excel file and extract decision table rules using Drools DecisionTable API */
  private void parseExcelDecisionTable() throws IOException {
    if (Utils.isEmpty(decisionTableFile)) {
      throw new IOException("Decision table file path is empty");
    }

    try (InputStream inputStream = new FileInputStream(decisionTableFile)) {
      // Use Drools SpreadsheetCompiler to compile the decision table to DRL
      SpreadsheetCompiler compiler = new SpreadsheetCompiler();

      // Determine input type based on file extension
      InputType inputType = determineInputType(decisionTableFile);

      // Compile decision table to DRL
      if (Utils.isEmpty(worksheetName)) {
        generatedDRL = compiler.compile(inputStream, inputType);
      } else {
        generatedDRL = compiler.compile(inputStream, worksheetName);
      }

      if (Utils.isEmpty(generatedDRL)) {
        throw new IOException("Failed to compile decision table - no DRL generated");
      }
    }
  }

  /** Determine input type based on file extension */
  private InputType determineInputType(String fileName) {
    if (fileName.toLowerCase().endsWith(".xlsx")) {
      return InputType.XLS;
    } else if (fileName.toLowerCase().endsWith(".xls")) {
      return InputType.XLS;
    } else if (fileName.toLowerCase().endsWith(".csv")) {
      return InputType.CSV;
    } else {
      return InputType.XLS; // Default to XLS for Excel files
    }
  }

  /** Initialize Drools KieHelper with the generated DRL */
  private void initializeDrools() throws RuleValidationException {
    // To ensure the plugin classloader use for dependency resolution
    ClassLoader orig = Thread.currentThread().getContextClassLoader();
    ClassLoader loader = getClass().getClassLoader();
    Thread.currentThread().setContextClassLoader(loader);

    try {
      System.out.println(generatedDRL);
      Resource ruleSet = ResourceFactory.newReaderResource(new StringReader(generatedDRL));
      kieHelper = new KieHelper();
      kieHelper.addResource(ruleSet, ResourceType.DRL);

      Results results = kieHelper.verify();
      System.out.println(results.getMessages());
      if (results.hasMessages(Message.Level.ERROR)) {
        throw new RuleValidationException(results.getMessages());
      }
    } finally {
      // Reset classloader back to original
      Thread.currentThread().setContextClassLoader(orig);
    }
  }

  /** Load a row of data for processing */
  public void loadRow(Object[] row) {
    inputRows.add(row);

    // Convert row to fact object
    Object factObject = convertRowToFactObject(row);
    factObjects.add(factObject);
  }

  /** Convert input row to fact object based on field mappings */
  private Object convertRowToFactObject(Object[] row) {
    // 根据是否配置了Java类名来决定创建Java对象还是Map
    if (!Utils.isEmpty(javaClassName)) {
      try {
        // 创建Java对象
        return createJavaObject(row);
      } catch (Exception e) {
        // 如果创建Java对象失败，降级为Map
        System.err.println(
            "Warning: Failed to create Java object of class "
                + javaClassName
                + ", fallback to Map: "
                + e.getMessage());
        return convertRowToMap(row);
      }
    } else {
      // 创建Map对象
      return convertRowToMap(row);
    }
  }

  /** Create Java object instance */
  private Object createJavaObject(Object[] row) throws Exception {
    Class<?> factClass = Class.forName(javaClassName);
    Object factObject = factClass.getDeclaredConstructor().newInstance();

    if (fieldMappings != null && inputRowMeta != null) {
      for (FieldMapping mapping : fieldMappings) {
        String inputFieldName = mapping.getInputFieldName();
        String javaFieldName = mapping.getJavaFieldName();

        int fieldIndex = inputRowMeta.indexOfValue(inputFieldName);
        if (fieldIndex >= 0 && fieldIndex < row.length) {
          Object value = row[fieldIndex];

          // Convert value to appropriate Java type
          Object convertedValue = convertValue(value, mapping.getJavaType());

          // Set field value using reflection
          setFieldValue(factObject, javaFieldName, convertedValue);
        }
      }
    }

    return factObject;
  }

  /** Fallback method to convert row to Map when no class name is specified */
  private Map<String, Object> convertRowToMap(Object[] row) {
    Map<String, Object> factData = new HashMap<>();

    if (fieldMappings != null && inputRowMeta != null) {
      for (FieldMapping mapping : fieldMappings) {
        String inputFieldName = mapping.getInputFieldName();
        String javaFieldName = mapping.getJavaFieldName();

        int fieldIndex = inputRowMeta.indexOfValue(inputFieldName);
        if (fieldIndex >= 0 && fieldIndex < row.length) {
          Object value = row[fieldIndex];

          // Convert value to appropriate Java type
          Object convertedValue = convertValue(value, mapping.getJavaType());
          factData.put(javaFieldName, convertedValue);
        }
      }
    }

    return factData;
  }

  /** Set field value using reflection */
  private void setFieldValue(Object object, String fieldName, Object value) {
    try {
      Class<?> clazz = object.getClass();

      // Try to find setter method first (setFieldName)
      String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

      if (value != null) {
        Class<?> valueClass = value.getClass();
        try {
          java.lang.reflect.Method setter = clazz.getMethod(setterName, valueClass);
          setter.invoke(object, value);
          return;
        } catch (NoSuchMethodException e) {
          // Try with primitive types
          if (valueClass == Integer.class) {
            try {
              java.lang.reflect.Method setter = clazz.getMethod(setterName, int.class);
              setter.invoke(object, value);
              return;
            } catch (NoSuchMethodException ex) {
              // Continue to field access
            }
          } else if (valueClass == Double.class) {
            try {
              java.lang.reflect.Method setter = clazz.getMethod(setterName, double.class);
              setter.invoke(object, value);
              return;
            } catch (NoSuchMethodException ex) {
              // Continue to field access
            }
          } else if (valueClass == Boolean.class) {
            try {
              java.lang.reflect.Method setter = clazz.getMethod(setterName, boolean.class);
              setter.invoke(object, value);
              return;
            } catch (NoSuchMethodException ex) {
              // Continue to field access
            }
          } else if (valueClass == Long.class) {
            try {
              java.lang.reflect.Method setter = clazz.getMethod(setterName, long.class);
              setter.invoke(object, value);
              return;
            } catch (NoSuchMethodException ex) {
              // Continue to field access
            }
          }
        }
      }

      // If setter method not found, try direct field access
      try {
        java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
      } catch (NoSuchFieldException e) {
        // Log warning but don't fail the entire process
        System.err.println(
            "Warning: Could not set field '" + fieldName + "' on class " + clazz.getSimpleName());
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to set field '" + fieldName + "' on object", e);
    }
  }

  /** Convert value to the specified Java type */
  private Object convertValue(Object value, String javaType) {
    if (value == null) {
      return null;
    }

    try {
      switch (javaType) {
        case "String":
          return value.toString();
        case "Integer":
          return Integer.valueOf(value.toString());
        case "Double":
          return Double.valueOf(value.toString());
        case "Boolean":
          return Boolean.valueOf(value.toString());
        case "Long":
          return Long.valueOf(value.toString());
        default:
          return value;
      }
    } catch (Exception e) {
      return value; // Return original value if conversion fails
    }
  }

  /** Execute the decision table rules */
  public void executeRules() {
    if (kieHelper == null) {
      return;
    }

    // To ensure the plugin classloader use for dependency resolution
    ClassLoader orig = Thread.currentThread().getContextClassLoader();
    ClassLoader loader = getClass().getClassLoader();
    Thread.currentThread().setContextClassLoader(loader);

    try {
      KieSession session = kieHelper.getKieContainer().newKieSession();

      // Insert fact objects into the session
      for (Object factObject : factObjects) {
        // 直接插入对象，不管是Map还是Java对象
        System.out.println("Before rules - Object: " + factObject);
        session.insert(factObject);
      }

      // Fire rules
      int firedRules = session.fireAllRules();
      System.out.println("Fired " + firedRules + " rules");

      // Collect results
      Collection<?> objects = session.getObjects();
      for (Object obj : objects) {
        System.out.println("After rules - Object: " + obj);
        resultObjects.add(obj);
      }

      session.dispose();
    } finally {
      // Reset classloader back to original
      Thread.currentThread().setContextClassLoader(orig);
    }
  }

  /** Get the results from rule execution */
  public List<Object[]> getResults() {
    List<Object[]> results = new ArrayList<>();
    int fieldCount = outputRowMeta != null ? outputRowMeta.size() : 1;

    int fieldStart = 0;
    if (keepInputFields) {
      fieldStart = inputRowMeta.size();
      results.addAll(inputRows);
    }

    for (Object resultObj : resultObjects) {
      Object[] resultRow = new Object[fieldCount];

      // 使用通用方法获取字段值，避免重复代码
      for (int i = fieldStart; i < fieldCount; i++) {
        String fieldName = outputRowMeta.getValueMeta(i).getName();
        resultRow[i] = getFieldValueFromObject(resultObj, fieldName);
      }

      results.add(resultRow);
    }

    // 如果没有结果，返回空行
    if (results.isEmpty()) {
      return inputRows;
    }
    return results;
  }

  /** 通用方法：从不同类型的对象中获取字段值 支持 Map 和普通 Java 对象（使用反射） */
  private Object getFieldValueFromObject(Object obj, String fieldName) {
    if (obj == null || fieldName == null) {
      return null;
    }

    try {
      Object value = null;

      // 处理 Map 类型
      if (obj instanceof Map) {
        Map<String, Object> map = (Map<String, Object>) obj;

        // 首先尝试直接用字段名查找
        if (map.containsKey(fieldName)) {
          value = map.get(fieldName);
        } else {
          // 如果找不到，尝试用映射转换后的字段名查找
          String mappedFieldName = findMappedFieldName(fieldName);
          if (mappedFieldName != null && map.containsKey(mappedFieldName)) {
            value = map.get(mappedFieldName);
          } else {
            // 如果还是找不到，尝试反向映射（用Java字段名反推原始字段名）
            String originalFieldName = findOriginalFieldName(fieldName);
            if (originalFieldName != null && map.containsKey(originalFieldName)) {
              value = map.get(originalFieldName);
            }
          }
        }
      } else {
        // 处理普通 Java 对象，使用反射获取值
        value = getFieldValueByReflection(obj, fieldName);
      }

      // 进行类型转换以确保与Hop类型兼容
      return convertToHopCompatibleType(value, fieldName);

    } catch (Exception e) {
      System.err.println(
          "Warning: Failed to get field '"
              + fieldName
              + "' from object "
              + obj.getClass().getSimpleName()
              + ": "
              + e.getMessage());
      return null;
    }
  }

  /** 根据原始字段名查找映射后的Java字段名 */
  private String findMappedFieldName(String originalFieldName) {
    if (fieldMappings == null) {
      return null;
    }

    for (FieldMapping mapping : fieldMappings) {
      if (originalFieldName.equals(mapping.getInputFieldName())) {
        return mapping.getJavaFieldName();
      }
    }
    return null;
  }

  /** 根据Java字段名查找原始字段名 */
  private String findOriginalFieldName(String javaFieldName) {
    if (fieldMappings == null) {
      return null;
    }

    for (FieldMapping mapping : fieldMappings) {
      if (javaFieldName.equals(mapping.getJavaFieldName())) {
        return mapping.getInputFieldName();
      }
    }
    return null;
  }

  /** 将值转换为Hop兼容的类型 */
  private Object convertToHopCompatibleType(Object value, String fieldName) {
    if (value == null) {
      return null;
    }

    try {
      // 获取字段的期望类型
      if (outputRowMeta != null) {
        int fieldIndex = outputRowMeta.indexOfValue(fieldName);
        if (fieldIndex >= 0) {
          org.apache.hop.core.row.IValueMeta valueMeta = outputRowMeta.getValueMeta(fieldIndex);

          // 根据期望类型进行转换
          switch (valueMeta.getType()) {
            case org.apache.hop.core.row.IValueMeta.TYPE_STRING:
              return value.toString();

            case org.apache.hop.core.row.IValueMeta.TYPE_INTEGER:
              if (value instanceof Integer) {
                return ((Integer) value).longValue(); // Hop的Integer实际上是Long
              } else if (value instanceof Long) {
                return value;
              } else {
                return Long.valueOf(value.toString());
              }

            case org.apache.hop.core.row.IValueMeta.TYPE_NUMBER:
              if (value instanceof Double) {
                return value;
              } else if (value instanceof Float) {
                return ((Float) value).doubleValue();
              } else if (value instanceof Number) {
                return ((Number) value).doubleValue();
              } else {
                return Double.valueOf(value.toString());
              }

            case org.apache.hop.core.row.IValueMeta.TYPE_BOOLEAN:
              if (value instanceof Boolean) {
                return value;
              } else {
                return Boolean.valueOf(value.toString());
              }

            case org.apache.hop.core.row.IValueMeta.TYPE_DATE:
              if (value instanceof java.util.Date) {
                return value;
              } else {
                // 尝试从字符串解析日期
                return new java.util.Date(value.toString());
              }

            case org.apache.hop.core.row.IValueMeta.TYPE_BIGNUMBER:
              if (value instanceof java.math.BigDecimal) {
                return value;
              } else {
                return new java.math.BigDecimal(value.toString());
              }

            default:
              return value;
          }
        }
      }

      // 如果找不到字段元数据，返回原值
      return value;

    } catch (Exception e) {
      System.err.println(
          "Warning: Failed to convert value '"
              + value
              + "' for field '"
              + fieldName
              + "': "
              + e.getMessage());
      return value; // 返回原值作为fallback
    }
  }

  /** 使用反射从 Java 对象中获取字段值 优先使用 getter 方法，如果没有则尝试直接访问字段 */
  private Object getFieldValueByReflection(Object obj, String fieldName) {
    if (obj == null || fieldName == null) {
      return null;
    }

    Class<?> clazz = obj.getClass();

    try {
      // 1. 尝试 getter 方法（getFieldName）
      String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
      try {
        java.lang.reflect.Method getter = clazz.getMethod(getterName);
        return getter.invoke(obj);
      } catch (NoSuchMethodException e) {
        // 继续尝试下一种方法
      }

      // 2. 尝试 boolean 类型的 getter（isFieldName）
      String booleanGetterName =
          "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
      try {
        java.lang.reflect.Method booleanGetter = clazz.getMethod(booleanGetterName);
        return booleanGetter.invoke(obj);
      } catch (NoSuchMethodException e) {
        // 继续尝试下一种方法
      }

      // 3. 尝试直接访问字段
      try {
        java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
      } catch (NoSuchFieldException e) {
        // 继续尝试下一种方法
      }

      // 4. 尝试查找所有字段，支持大小写不敏感匹配
      for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
        if (field.getName().equalsIgnoreCase(fieldName)) {
          field.setAccessible(true);
          return field.get(obj);
        }
      }

      // 所有方法都失败了
      throw new RuntimeException(
          "Field '" + fieldName + "' not found in class " + clazz.getSimpleName());

    } catch (RuntimeException e) {
      // 重新抛出运行时异常
      throw e;
    } catch (Exception e) {
      // 捕获invoke和field.get可能抛出的其他异常
      throw new RuntimeException(
          "Failed to get field '" + fieldName + "' from object of class " + clazz.getSimpleName(),
          e);
    }
  }

  /** Shutdown and cleanup resources */
  public void shutdown() {
    if (kieHelper != null) {
      kieHelper = null;
    }
    inputRows.clear();
    factObjects.clear();
    resultObjects.clear();
  }
}
