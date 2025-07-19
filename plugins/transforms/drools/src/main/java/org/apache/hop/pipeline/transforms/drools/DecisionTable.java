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

import java.util.List;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;

public class DecisionTable extends BaseTransform<DecisionTableMeta, DecisionTableData> {
  private static final Class<?> PKG = Rules.class; // for i18n purposes

  public DecisionTable(
      TransformMeta transformMeta,
      DecisionTableMeta meta,
      DecisionTableData data,
      int copyNr,
      PipelineMeta pipelineMeta,
      Pipeline pipeline) {
    super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
  }

  @Override
  public boolean init() {
    return super.init();
  }

  public boolean runtimeInit() throws HopTransformException {
    try {
      data.setOutputRowMeta(getInputRowMeta().clone());
      data.setInputRowMeta(getInputRowMeta());

      // Set configuration from meta
      data.setDecisionTableFile(resolve(meta.getDecisionTableFile()));
      data.setJavaClassName(meta.getJavaClassName());
      data.setWorksheetName(meta.getWorksheetName());
      data.setFieldMappings(meta.getFieldMappings());
      data.setKeepInputFields(meta.isKeepInputFields());

      // Generate auto mappings only if no mappings exist and auto-generate is enabled
      if (meta.isAutoGenerateMappings()
          && (meta.getFieldMappings() == null || meta.getFieldMappings().isEmpty())) {
        meta.generateAutoMappings(getInputRowMeta());
        data.setFieldMappings(meta.getFieldMappings());
      }

      // Initialize decision table first to get generated DRL
      try {
        data.initializeDecisionTable();
      } catch (RuleValidationException e) {
        for (String message : e.getMessages()) {
          logError(message);
        }
        throw new HopTransformException(
            BaseMessages.getString(PKG, "DecisionTable.Error.CompileDecisionTable"));
      }

      // Configure output row metadata based on actual fields
      configureOutputRowMeta();

      return true;
    } catch (Exception e) {
      throw new HopTransformException(e);
    }
  }

  /** Configure output row metadata based on Java class fields or field mappings */
  private void configureOutputRowMeta() throws HopTransformException {
    try {
      // Start with input fields if keepInputFields is true
      if (!meta.isKeepInputFields()) {
        data.getOutputRowMeta().clear();
      }

      // If outputFields are configured, use them
      if (meta.getOutputFields() != null && !meta.getOutputFields().isEmpty()) {
        for (RuleResultItem outputField : meta.getOutputFields()) {
          int type = ValueMetaFactory.getIdForValueMeta(outputField.getType());
          IValueMeta vm = ValueMetaFactory.createValueMeta(outputField.getName(), type);
          vm.setOrigin(getTransformName());
          data.getOutputRowMeta().addValueMeta(vm);
        }
      } else {
        // Auto-generate output fields from Java class or field mappings
        autoGenerateOutputFields();
      }

    } catch (Exception e) {
      throw new HopTransformException("Failed to configure output row metadata", e);
    }
  }

  /** Auto-generate output fields from Java class or field mappings */
  private void autoGenerateOutputFields() throws Exception {
    if (data.getJavaClassName() != null && !data.getJavaClassName().isEmpty()) {
      // Generate from Java class
      generateOutputFieldsFromJavaClass();
    } else if (data.getFieldMappings() != null && !data.getFieldMappings().isEmpty()) {
      // Generate from field mappings
      generateOutputFieldsFromMappings();
    } else {
      // Fallback: add a generic result field
      addDefaultResultField();
    }
  }

  /** Generate output fields from Java class reflection */
  private void generateOutputFieldsFromJavaClass() throws Exception {
    Class<?> javaClass = Class.forName(data.getJavaClassName());
    java.lang.reflect.Field[] fields = javaClass.getDeclaredFields();

    for (java.lang.reflect.Field field : fields) {
      String fieldName = field.getName();
      String hopType = javaTypeToHopType(field.getType());

      int type = ValueMetaFactory.getIdForValueMeta(hopType);
      IValueMeta vm = ValueMetaFactory.createValueMeta(fieldName, type);
      vm.setOrigin(getTransformName());
      data.getOutputRowMeta().addValueMeta(vm);
    }
  }

  /** Generate output fields from field mappings */
  private void generateOutputFieldsFromMappings() throws Exception {
    for (FieldMapping mapping : data.getFieldMappings()) {
      String fieldName = mapping.getJavaFieldName();
      String hopType = javaTypeStringToHopType(mapping.getJavaType());

      int type = ValueMetaFactory.getIdForValueMeta(hopType);
      IValueMeta vm = ValueMetaFactory.createValueMeta(fieldName, type);
      vm.setOrigin(getTransformName());
      data.getOutputRowMeta().addValueMeta(vm);
    }
  }

  /** Add default result field when no specific configuration is available */
  private void addDefaultResultField() throws Exception {
    IValueMeta vm =
        ValueMetaFactory.createValueMeta("result", ValueMetaFactory.getIdForValueMeta("String"));
    vm.setOrigin(getTransformName());
    data.getOutputRowMeta().addValueMeta(vm);
  }

  /** Convert Java type to Hop type */
  private String javaTypeToHopType(Class<?> javaType) {
    if (javaType == String.class) {
      return "String";
    } else if (javaType == Integer.class || javaType == int.class) {
      return "Integer";
    } else if (javaType == Double.class || javaType == double.class) {
      return "Number";
    } else if (javaType == Boolean.class || javaType == boolean.class) {
      return "Boolean";
    } else if (javaType == Long.class || javaType == long.class) {
      return "Integer";
    } else if (javaType == java.util.Date.class) {
      return "Date";
    } else if (javaType == java.math.BigDecimal.class) {
      return "BigNumber";
    } else {
      return "String"; // Default to String for unknown types
    }
  }

  /** Convert Java type string to Hop type */
  private String javaTypeStringToHopType(String javaType) {
    switch (javaType) {
      case "String":
        return "String";
      case "Integer":
        return "Integer";
      case "Double":
        return "Number";
      case "Boolean":
        return "Boolean";
      case "Long":
        return "Integer";
      case "java.util.Date":
        return "Date";
      case "java.math.BigDecimal":
        return "BigNumber";
      default:
        return "String";
    }
  }

  @Override
  public boolean processRow() throws HopException {
    try {
      Object[] r = getRow(); // get row, set busy!

      if (r == null) { // no more input to be expected...

        // Execute the decision table rules
        data.executeRules();

        // Get results and output them
        List<Object[]> results = data.getResults();
        for (Object[] resultRow : results) {
          putRow(data.getOutputRowMeta(), resultRow);
        }

        data.shutdown();
        setOutputDone();
        return false;
      }

      if (first) {
        if (!runtimeInit()) {
          return false;
        }
        first = false;
      }

      // Load the row for processing
      data.loadRow(r);

      return true;
    } catch (Exception e) {
      throw new HopException(e);
    }
  }
}
