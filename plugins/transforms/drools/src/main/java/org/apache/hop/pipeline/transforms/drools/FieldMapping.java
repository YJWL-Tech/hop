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

import org.apache.hop.core.util.Utils;
import org.apache.hop.metadata.api.HopMetadataProperty;

/** Field mapping class for decision table input field to Java field mapping */
public class FieldMapping {

  @HopMetadataProperty(key = "input-field")
  private String inputFieldName;

  @HopMetadataProperty(key = "java-field")
  private String javaFieldName;

  @HopMetadataProperty(key = "java-type")
  private String javaType;

  @HopMetadataProperty(key = "mapping-type")
  private String mappingType; // auto, manual

  public FieldMapping() {
    this.mappingType = "auto";
  }

  public FieldMapping(String inputFieldName, String javaFieldName, String javaType) {
    this.inputFieldName = inputFieldName;
    this.javaFieldName = javaFieldName;
    this.javaType = javaType;
    this.mappingType = "auto";
  }

  public FieldMapping(
      String inputFieldName, String javaFieldName, String javaType, String mappingType) {
    this.inputFieldName = inputFieldName;
    this.javaFieldName = javaFieldName;
    this.javaType = javaType;
    this.mappingType = mappingType;
  }

  public String getInputFieldName() {
    return inputFieldName;
  }

  public void setInputFieldName(String inputFieldName) {
    this.inputFieldName = inputFieldName;
  }

  public String getJavaFieldName() {
    return javaFieldName;
  }

  public void setJavaFieldName(String javaFieldName) {
    this.javaFieldName = javaFieldName;
  }

  public String getJavaType() {
    return javaType;
  }

  public void setJavaType(String javaType) {
    this.javaType = javaType;
  }

  public String getMappingType() {
    return mappingType;
  }

  public void setMappingType(String mappingType) {
    this.mappingType = mappingType;
  }

  /**
   * Auto-generate Java field name from database field name Examples: business_name -> businessName,
   * CUSTOMER_ID -> customerId
   */
  public static String autoGenerateJavaFieldName(String dbFieldName) {
    if (Utils.isEmpty(dbFieldName)) {
      return "";
    }

    String[] parts = dbFieldName.toLowerCase().split("_");
    StringBuilder javaFieldName = new StringBuilder(parts[0]);

    for (int i = 1; i < parts.length; i++) {
      if (!Utils.isEmpty(parts[i])) {
        javaFieldName.append(parts[i].substring(0, 1).toUpperCase());
        if (parts[i].length() > 1) {
          javaFieldName.append(parts[i].substring(1));
        }
      }
    }

    return javaFieldName.toString();
  }

  /** Convert Hop ValueMeta type to Java type */
  public static String hopTypeToJavaType(String hopType) {
    if (Utils.isEmpty(hopType)) {
      return "String";
    }

    switch (hopType.toLowerCase()) {
      case "string":
        return "String";
      case "integer":
        return "Integer";
      case "number":
        return "Double";
      case "boolean":
        return "Boolean";
      case "date":
        return "java.util.Date";
      case "bigdecimal":
        return "java.math.BigDecimal";
      case "long":
        return "Long";
      default:
        return "String";
    }
  }

  @Override
  public String toString() {
    return "FieldMapping{"
        + "inputFieldName='"
        + inputFieldName
        + '\''
        + ", javaFieldName='"
        + javaFieldName
        + '\''
        + ", javaType='"
        + javaType
        + '\''
        + ", mappingType='"
        + mappingType
        + '\''
        + '}';
  }
}
