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

import java.util.ArrayList;
import java.util.List;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopPluginException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;

@Transform(
    id = "DecisionTable",
    image = "decision_table.svg",
    name = "i18n::DecisionTable.Name",
    description = "i18n::DecisionTable.Description",
    categoryDescription = "i18n::Rules.Category",
    keywords = "i18n::DecisionTable.keyword",
    documentationUrl = "/pipeline/transforms/decisiontable.html")
public class DecisionTableMeta extends BaseTransformMeta<DecisionTable, DecisionTableData> {

  private static final Class<?> PKG = Rules.class; // for i18n purposes

  @HopMetadataProperty(key = "decision-table-file")
  private String decisionTableFile;

  @HopMetadataProperty(key = "java-class-name")
  private String javaClassName = "DecisionTableFact";

  @HopMetadataProperty(key = "worksheet-name")
  private String worksheetName;

  @HopMetadataProperty(groupKey = "field-mappings", key = "field-mapping")
  private List<FieldMapping> fieldMappings = new ArrayList<>();

  @HopMetadataProperty(groupKey = "output-fields", key = "output-field")
  private List<RuleResultItem> outputFields = new ArrayList<>();

  @HopMetadataProperty(key = "keep-input-fields")
  private boolean keepInputFields = false;

  @HopMetadataProperty(key = "auto-generate-mappings")
  private boolean autoGenerateMappings = true;

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

  public List<RuleResultItem> getOutputFields() {
    return outputFields;
  }

  public void setOutputFields(List<RuleResultItem> outputFields) {
    this.outputFields = outputFields;
  }

  public boolean isKeepInputFields() {
    return keepInputFields;
  }

  public void setKeepInputFields(boolean keepInputFields) {
    this.keepInputFields = keepInputFields;
  }

  public boolean isAutoGenerateMappings() {
    return autoGenerateMappings;
  }

  public void setAutoGenerateMappings(boolean autoGenerateMappings) {
    this.autoGenerateMappings = autoGenerateMappings;
  }

  @Override
  public void setDefault() {
    decisionTableFile = "";
    javaClassName = "DecisionTableFact";
    worksheetName = "";
    fieldMappings = new ArrayList<>();
    outputFields = new ArrayList<>();
    keepInputFields = true;
    autoGenerateMappings = true;
  }

  @Override
  public void getFields(
      IRowMeta inputRowMeta,
      String name,
      IRowMeta[] info,
      TransformMeta nextTransform,
      IVariables variables,
      IHopMetadataProvider metadataProvider)
      throws HopTransformException {

    if (!keepInputFields) {
      inputRowMeta.clear();
    }

    try {
      // Add output fields from decision table result
      if (outputFields != null) {
        for (RuleResultItem outputField : outputFields) {
          int type = ValueMetaFactory.getIdForValueMeta(outputField.getType());
          IValueMeta vm = ValueMetaFactory.createValueMeta(outputField.getName(), type);
          vm.setOrigin(name);
          inputRowMeta.addValueMeta(vm);
        }
      }
    } catch (HopPluginException e) {
      throw new HopTransformException("Unable to get decision table result columns", e);
    }
  }

  /** Generate automatic field mappings based on input row meta */
  public void generateAutoMappings(IRowMeta inputRowMeta) {
    if (!autoGenerateMappings || inputRowMeta == null) {
      return;
    }

    fieldMappings.clear();

    String[] fieldNames = inputRowMeta.getFieldNames();
    for (String fieldName : fieldNames) {
      IValueMeta valueMeta = inputRowMeta.getValueMeta(inputRowMeta.indexOfValue(fieldName));

      String javaFieldName = FieldMapping.autoGenerateJavaFieldName(fieldName);
      String javaType = FieldMapping.hopTypeToJavaType(valueMeta.getTypeDesc());

      FieldMapping mapping = new FieldMapping(fieldName, javaFieldName, javaType, "auto");
      fieldMappings.add(mapping);
    }
  }

  public String[] getExpectedResultList() {
    String[] result = new String[outputFields.size()];
    for (int i = 0; i < outputFields.size(); i++) {
      result[i] = outputFields.get(i).getName();
    }
    return result;
  }
}
