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

import org.apache.hop.core.Props;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.widget.ColumnInfo;
import org.apache.hop.ui.core.widget.ColumnsResizer;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class DecisionTableDialog extends BaseTransformDialog {

  private static final Class<?> PKG = Rules.class;

  private DecisionTableMeta input;

  private TextVar wDecisionTableFile;
  private TextVar wJavaClassName;
  private TextVar wWorksheetName;
  private Button wAutoGenerateMappings;
  private Button wKeepInputFields;
  private TableView wFieldMappings;
  private TableView wOutputFields;

  public DecisionTableDialog(
      Shell parent,
      IVariables variables,
      DecisionTableMeta transformMeta,
      PipelineMeta pipelineMeta) {
    super(parent, variables, transformMeta, pipelineMeta);
    input = transformMeta;
  }

  @Override
  public String open() {
    Shell parent = getParent();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    PropsUi.setLook(shell);
    setShellImage(shell, input);

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = PropsUi.getFormMargin();
    formLayout.marginHeight = PropsUi.getFormMargin();

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "DecisionTable.Shell.Title"));

    int middle = props.getMiddlePct();
    int margin = PropsUi.getMargin();

    // THE BUTTONS
    wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    setButtonPositions(new Button[] {wOk, wCancel}, margin, null);

    // TransformName line
    wlTransformName = new Label(shell, SWT.RIGHT);
    wlTransformName.setText(BaseMessages.getString(PKG, "DecisionTable.TransformName.Label"));
    PropsUi.setLook(wlTransformName);
    fdlTransformName = new FormData();
    fdlTransformName.left = new FormAttachment(0, 0);
    fdlTransformName.right = new FormAttachment(middle, -margin);
    fdlTransformName.top = new FormAttachment(0, margin);
    wlTransformName.setLayoutData(fdlTransformName);
    wTransformName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wTransformName.setText(transformName);
    PropsUi.setLook(wTransformName);
    fdTransformName = new FormData();
    fdTransformName.left = new FormAttachment(middle, 0);
    fdTransformName.top = new FormAttachment(0, margin);
    fdTransformName.right = new FormAttachment(100, 0);
    wTransformName.setLayoutData(fdTransformName);

    CTabFolder wTabFolder = new CTabFolder(shell, SWT.BORDER);
    PropsUi.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment(0, 0);
    fdTabFolder.top = new FormAttachment(wTransformName, 20);
    fdTabFolder.right = new FormAttachment(100, 0);
    fdTabFolder.bottom = new FormAttachment(wOk, -margin);
    wTabFolder.setLayoutData(fdTabFolder);

    addDecisionTableTab(wTabFolder, margin);
    addFieldMappingTab(wTabFolder, margin);
    addOutputTab(wTabFolder, margin);

    // Add listeners
    wOk.addListener(SWT.Selection, e -> ok());
    wCancel.addListener(SWT.Selection, e -> cancel());

    wTabFolder.setSelection(0);

    getData();

    input.setChanged(changed);

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return transformName;
  }

  private void addDecisionTableTab(CTabFolder wTabFolder, int margin) {
    CTabItem wDecisionTableTab = new CTabItem(wTabFolder, SWT.NONE);
    wDecisionTableTab.setFont(GuiResource.getInstance().getFontDefault());
    wDecisionTableTab.setText(BaseMessages.getString(PKG, "DecisionTable.Tabs.DecisionTable"));

    Composite wDecisionTableComp = new Composite(wTabFolder, SWT.NONE);
    PropsUi.setLook(wDecisionTableComp);

    FormLayout decisionTableLayout = new FormLayout();
    decisionTableLayout.marginWidth = 3;
    decisionTableLayout.marginHeight = 3;
    wDecisionTableComp.setLayout(decisionTableLayout);

    ModifyListener lsMod = e -> input.setChanged();

    // Decision table file
    Label wlDecisionTableFile = new Label(wDecisionTableComp, SWT.RIGHT);
    wlDecisionTableFile.setText(
        BaseMessages.getString(PKG, "DecisionTable.DecisionTableFile.Label"));
    PropsUi.setLook(wlDecisionTableFile);
    FormData fdlDecisionTableFile = new FormData();
    fdlDecisionTableFile.left = new FormAttachment(0, 0);
    fdlDecisionTableFile.top = new FormAttachment(0, margin);
    fdlDecisionTableFile.right = new FormAttachment(50, -margin);
    wlDecisionTableFile.setLayoutData(fdlDecisionTableFile);

    Button wbDecisionTableFile = new Button(wDecisionTableComp, SWT.PUSH);
    wbDecisionTableFile.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
    PropsUi.setLook(wbDecisionTableFile);
    FormData fdDecisionTableFile = new FormData();
    fdDecisionTableFile.right = new FormAttachment(100, 0);
    fdDecisionTableFile.top = new FormAttachment(0, margin);
    wbDecisionTableFile.setLayoutData(fdDecisionTableFile);

    wDecisionTableFile =
        new TextVar(variables, wDecisionTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wDecisionTableFile);
    wDecisionTableFile.addModifyListener(lsMod);
    FormData fdDecisionTableFileText = new FormData();
    fdDecisionTableFileText.left = new FormAttachment(50, 0);
    fdDecisionTableFileText.top = new FormAttachment(0, margin);
    fdDecisionTableFileText.right = new FormAttachment(wbDecisionTableFile, -margin);
    wDecisionTableFile.setLayoutData(fdDecisionTableFileText);

    wbDecisionTableFile.addListener(
        SWT.Selection,
        e ->
            BaseDialog.presentFileDialog(
                shell,
                wDecisionTableFile,
                variables,
                new String[] {"*.xls", "*.xlsx", "*"},
                new String[] {"Excel files (*.xls, *.xlsx)", "All files (*.*)"},
                true));

    // Java class name
    Label wlJavaClassName = new Label(wDecisionTableComp, SWT.RIGHT);
    wlJavaClassName.setText(BaseMessages.getString(PKG, "DecisionTable.JavaClassName.Label"));
    PropsUi.setLook(wlJavaClassName);
    FormData fdlJavaClassName = new FormData();
    fdlJavaClassName.left = new FormAttachment(0, 0);
    fdlJavaClassName.top = new FormAttachment(wDecisionTableFile, margin);
    fdlJavaClassName.right = new FormAttachment(50, -margin);
    wlJavaClassName.setLayoutData(fdlJavaClassName);

    wJavaClassName = new TextVar(variables, wDecisionTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wJavaClassName);
    wJavaClassName.addModifyListener(lsMod);
    FormData fdJavaClassName = new FormData();
    fdJavaClassName.left = new FormAttachment(50, 0);
    fdJavaClassName.top = new FormAttachment(wDecisionTableFile, margin);
    fdJavaClassName.right = new FormAttachment(100, 0);
    wJavaClassName.setLayoutData(fdJavaClassName);

    // Worksheet name
    Label wlWorksheetName = new Label(wDecisionTableComp, SWT.RIGHT);
    wlWorksheetName.setText(BaseMessages.getString(PKG, "DecisionTable.WorksheetName.Label"));
    PropsUi.setLook(wlWorksheetName);
    FormData fdlWorksheetName = new FormData();
    fdlWorksheetName.left = new FormAttachment(0, 0);
    fdlWorksheetName.top = new FormAttachment(wJavaClassName, margin);
    fdlWorksheetName.right = new FormAttachment(50, -margin);
    wlWorksheetName.setLayoutData(fdlWorksheetName);

    wWorksheetName = new TextVar(variables, wDecisionTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wWorksheetName);
    wWorksheetName.addModifyListener(lsMod);
    FormData fdWorksheetName = new FormData();
    fdWorksheetName.left = new FormAttachment(50, 0);
    fdWorksheetName.top = new FormAttachment(wJavaClassName, margin);
    fdWorksheetName.right = new FormAttachment(100, 0);
    wWorksheetName.setLayoutData(fdWorksheetName);

    // Auto generate mappings
    wAutoGenerateMappings = new Button(wDecisionTableComp, SWT.CHECK);
    wAutoGenerateMappings.setText(
        BaseMessages.getString(PKG, "DecisionTable.AutoGenerateMappings.Label"));
    PropsUi.setLook(wAutoGenerateMappings);
    FormData fdAutoGenerateMappings = new FormData();
    fdAutoGenerateMappings.left = new FormAttachment(0, 0);
    fdAutoGenerateMappings.top = new FormAttachment(wWorksheetName, margin);
    fdAutoGenerateMappings.right = new FormAttachment(100, 0);
    wAutoGenerateMappings.setLayoutData(fdAutoGenerateMappings);

    // Keep input fields
    wKeepInputFields = new Button(wDecisionTableComp, SWT.CHECK);
    wKeepInputFields.setText(BaseMessages.getString(PKG, "DecisionTable.KeepInputFields.Label"));
    PropsUi.setLook(wKeepInputFields);
    FormData fdKeepInputFields = new FormData();
    fdKeepInputFields.left = new FormAttachment(0, 0);
    fdKeepInputFields.top = new FormAttachment(wAutoGenerateMappings, margin);
    fdKeepInputFields.right = new FormAttachment(100, 0);
    wKeepInputFields.setLayoutData(fdKeepInputFields);

    FormData fdDecisionTableComp = new FormData();
    fdDecisionTableComp.left = new FormAttachment(0, 0);
    fdDecisionTableComp.top = new FormAttachment(0, 0);
    fdDecisionTableComp.right = new FormAttachment(100, 0);
    fdDecisionTableComp.bottom = new FormAttachment(100, 0);
    wDecisionTableComp.setLayoutData(fdDecisionTableComp);

    wDecisionTableComp.layout();
    wDecisionTableTab.setControl(wDecisionTableComp);
  }

  private void addFieldMappingTab(CTabFolder wTabFolder, int margin) {
    CTabItem wFieldMappingTab = new CTabItem(wTabFolder, SWT.NONE);
    wFieldMappingTab.setFont(GuiResource.getInstance().getFontDefault());
    wFieldMappingTab.setText(BaseMessages.getString(PKG, "DecisionTable.Tabs.FieldMapping"));

    Composite wFieldMappingComp = new Composite(wTabFolder, SWT.NONE);
    PropsUi.setLook(wFieldMappingComp);

    FormLayout fieldMappingLayout = new FormLayout();
    fieldMappingLayout.marginWidth = 3;
    fieldMappingLayout.marginHeight = 3;
    wFieldMappingComp.setLayout(fieldMappingLayout);

    int nrRows = (input.getFieldMappings() != null ? input.getFieldMappings().size() : 1);

    ColumnInfo[] ciFieldMappings =
        new ColumnInfo[] {
          new ColumnInfo(
              BaseMessages.getString(PKG, "DecisionTable.FieldMapping.InputField"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false,
              false),
          new ColumnInfo(
              BaseMessages.getString(PKG, "DecisionTable.FieldMapping.JavaField"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false,
              false),
          new ColumnInfo(
              BaseMessages.getString(PKG, "DecisionTable.FieldMapping.JavaType"),
              ColumnInfo.COLUMN_TYPE_CCOMBO,
              new String[] {"String", "Integer", "Double", "Boolean", "Long", "java.util.Date"}),
          new ColumnInfo(
              BaseMessages.getString(PKG, "DecisionTable.FieldMapping.MappingType"),
              ColumnInfo.COLUMN_TYPE_CCOMBO,
              new String[] {"auto", "manual"}),
        };

    wFieldMappings =
        new TableView(
            variables,
            wFieldMappingComp,
            SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
            ciFieldMappings,
            nrRows,
            false,
            null,
            props,
            false);

    FormData fdFieldMappings = new FormData();
    fdFieldMappings.left = new FormAttachment(0, 0);
    fdFieldMappings.top = new FormAttachment(0, margin);
    fdFieldMappings.right = new FormAttachment(100, 0);
    fdFieldMappings.bottom = new FormAttachment(100, -margin);
    wFieldMappings.setLayoutData(fdFieldMappings);
    wFieldMappings.getTable().addListener(SWT.Resize, new ColumnsResizer(0, 25, 25, 25, 25));

    // Add "Regenerate Mappings" button
    Button wRegenerateMappings = new Button(wFieldMappingComp, SWT.PUSH);
    wRegenerateMappings.setText(
        BaseMessages.getString(PKG, "DecisionTable.RegenerateMappings.Label"));
    PropsUi.setLook(wRegenerateMappings);
    FormData fdRegenerateMappings = new FormData();
    fdRegenerateMappings.right = new FormAttachment(100, 0);
    fdRegenerateMappings.bottom = new FormAttachment(100, -margin);
    wRegenerateMappings.setLayoutData(fdRegenerateMappings);

    wRegenerateMappings.addListener(SWT.Selection, e -> regenerateFieldMappings());

    // Adjust field mappings table to leave space for the button
    fdFieldMappings.bottom = new FormAttachment(wRegenerateMappings, -margin);

    FormData fdFieldMappingComp = new FormData();
    fdFieldMappingComp.left = new FormAttachment(0, 0);
    fdFieldMappingComp.top = new FormAttachment(0, 0);
    fdFieldMappingComp.right = new FormAttachment(100, 0);
    fdFieldMappingComp.bottom = new FormAttachment(100, 0);
    wFieldMappingComp.setLayoutData(fdFieldMappingComp);

    wFieldMappingComp.layout();
    wFieldMappingTab.setControl(wFieldMappingComp);
  }

  private void addOutputTab(CTabFolder wTabFolder, int margin) {
    CTabItem wOutputTab = new CTabItem(wTabFolder, SWT.NONE);
    wOutputTab.setFont(GuiResource.getInstance().getFontDefault());
    wOutputTab.setText(BaseMessages.getString(PKG, "DecisionTable.Tabs.Output"));

    Composite wOutputComp = new Composite(wTabFolder, SWT.NONE);
    PropsUi.setLook(wOutputComp);

    FormLayout outputLayout = new FormLayout();
    outputLayout.marginWidth = 3;
    outputLayout.marginHeight = 3;
    wOutputComp.setLayout(outputLayout);

    int nrRows = (input.getOutputFields() != null ? input.getOutputFields().size() : 1);

    ColumnInfo[] ciOutputFields =
        new ColumnInfo[] {
          new ColumnInfo(
              BaseMessages.getString(PKG, "DecisionTable.Output.FieldName"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false,
              false),
          new ColumnInfo(
              BaseMessages.getString(PKG, "DecisionTable.Output.FieldType"),
              ColumnInfo.COLUMN_TYPE_CCOMBO,
              ValueMetaFactory.getValueMetaNames()),
        };

    wOutputFields =
        new TableView(
            variables,
            wOutputComp,
            SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
            ciOutputFields,
            nrRows,
            false,
            null,
            props,
            false);

    FormData fdOutputFields = new FormData();
    fdOutputFields.left = new FormAttachment(0, 0);
    fdOutputFields.top = new FormAttachment(0, margin);
    fdOutputFields.right = new FormAttachment(100, 0);
    fdOutputFields.bottom = new FormAttachment(100, -margin);
    wOutputFields.setLayoutData(fdOutputFields);
    wOutputFields.getTable().addListener(SWT.Resize, new ColumnsResizer(0, 50, 50));

    FormData fdOutputComp = new FormData();
    fdOutputComp.left = new FormAttachment(0, 0);
    fdOutputComp.top = new FormAttachment(0, 0);
    fdOutputComp.right = new FormAttachment(100, 0);
    fdOutputComp.bottom = new FormAttachment(100, 0);
    wOutputComp.setLayoutData(fdOutputComp);

    wOutputComp.layout();
    wOutputTab.setControl(wOutputComp);
  }

  private void ok() {
    if (Utils.isEmpty(wTransformName.getText())) {
      return;
    }

    input.setDecisionTableFile(wDecisionTableFile.getText());
    input.setJavaClassName(wJavaClassName.getText());
    input.setWorksheetName(wWorksheetName.getText());
    input.setAutoGenerateMappings(wAutoGenerateMappings.getSelection());
    input.setKeepInputFields(wKeepInputFields.getSelection());

    // Field mappings
    input.getFieldMappings().clear();
    for (int i = 0; i < wFieldMappings.nrNonEmpty(); i++) {
      TableItem item = wFieldMappings.getNonEmpty(i);
      if (!Utils.isEmpty(item.getText(1))) {
        FieldMapping mapping =
            new FieldMapping(
                item.getText(1), // input field
                item.getText(2), // java field
                item.getText(3), // java type
                item.getText(4) // mapping type
                );
        input.getFieldMappings().add(mapping);
      }
    }

    // Output fields
    input.getOutputFields().clear();
    for (int i = 0; i < wOutputFields.nrNonEmpty(); i++) {
      TableItem item = wOutputFields.getNonEmpty(i);
      if (!Utils.isEmpty(item.getText(1))) {
        input.getOutputFields().add(new RuleResultItem(item.getText(1), item.getText(2)));
      }
    }

    transformName = wTransformName.getText();
    input.setChanged();
    dispose();
  }

  private void cancel() {
    transformName = null;
    input.setChanged(false);
    dispose();
  }

  /** 重新生成字段映射 */
  private void regenerateFieldMappings() {
    try {
      // 清空现有映射
      wFieldMappings.clearAll(false);

      // 获取输入字段信息
      if (pipelineMeta != null && transformMeta != null) {
        IRowMeta inputRowMeta = pipelineMeta.getPrevTransformFields(variables, transformMeta);
        String[] inputFields = inputRowMeta.getFieldNames();

        // 为每个输入字段生成映射
        for (int i = 0; i < inputFields.length; i++) {
          TableItem item = new TableItem(wFieldMappings.table, SWT.NONE);
          String inputField = inputFields[i];
          String javaField = FieldMapping.autoGenerateJavaFieldName(inputField);

          // 获取字段的Hop类型并转换为Java类型
          IValueMeta valueMeta = inputRowMeta.getValueMeta(inputRowMeta.indexOfValue(inputField));
          String javaType = FieldMapping.hopTypeToJavaType(valueMeta.getTypeDesc());

          item.setText(1, inputField); // Input Field
          item.setText(2, javaField); // Java Field
          item.setText(3, javaType); // Java Type (根据实际类型生成)
          item.setText(4, "auto"); // Mapping Type
        }

        wFieldMappings.removeEmptyRows();
        wFieldMappings.setRowNums();
        wFieldMappings.optWidth(true);

        // 标记为已修改
        input.setChanged();
      }
    } catch (Exception e) {
      // 记录错误但不中断操作
      logError("Error regenerating field mappings: " + e.getMessage());
    }
  }

  public void getData() {
    if (input.getDecisionTableFile() != null) {
      wDecisionTableFile.setText(input.getDecisionTableFile());
    }
    if (input.getJavaClassName() != null) {
      wJavaClassName.setText(input.getJavaClassName());
    }
    if (input.getWorksheetName() != null) {
      wWorksheetName.setText(input.getWorksheetName());
    }
    wAutoGenerateMappings.setSelection(input.isAutoGenerateMappings());
    wKeepInputFields.setSelection(input.isKeepInputFields());

    // Field mappings
    if (input.getFieldMappings() != null) {
      for (int i = 0; i < input.getFieldMappings().size(); i++) {
        TableItem ti = wFieldMappings.table.getItem(i);
        FieldMapping mapping = input.getFieldMappings().get(i);
        ti.setText(1, mapping.getInputFieldName() != null ? mapping.getInputFieldName() : "");
        ti.setText(2, mapping.getJavaFieldName() != null ? mapping.getJavaFieldName() : "");
        ti.setText(3, mapping.getJavaType() != null ? mapping.getJavaType() : "");
        ti.setText(4, mapping.getMappingType() != null ? mapping.getMappingType() : "");
      }
    }

    // Output fields
    if (input.getOutputFields() != null) {
      for (int i = 0; i < input.getOutputFields().size(); i++) {
        TableItem ti = wOutputFields.table.getItem(i);
        RuleResultItem outputField = input.getOutputFields().get(i);
        ti.setText(1, outputField.getName() != null ? outputField.getName() : "");
        ti.setText(2, outputField.getType() != null ? outputField.getType() : "");
      }
    }

    wFieldMappings.optWidth(true);
    wOutputFields.optWidth(true);

    wTransformName.selectAll();
    wTransformName.setFocus();
  }
}
