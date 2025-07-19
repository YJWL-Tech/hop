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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class DecisionTableDialog extends BaseTransformDialog {

  private static final Class<?> PKG = Rules.class;

  private DecisionTableMeta input;

  private TextVar wDecisionTableFile;
  private Combo wJavaClassName;
  private Combo wWorksheetName;
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
    wDecisionTableFile.addModifyListener(
        e -> {
          input.setChanged();
          // 当文件路径改变时，自动更新worksheet name列表
          updateWorksheetNameList();
        });
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

    wJavaClassName = new Combo(wDecisionTableComp, SWT.BORDER | SWT.DROP_DOWN);
    PropsUi.setLook(wJavaClassName);
    wJavaClassName.addModifyListener(lsMod);
    FormData fdJavaClassName = new FormData();
    fdJavaClassName.left = new FormAttachment(50, 0);
    fdJavaClassName.top = new FormAttachment(wDecisionTableFile, margin);
    fdJavaClassName.right = new FormAttachment(100, 0);
    wJavaClassName.setLayoutData(fdJavaClassName);

    // 加载可用的POJO类
    loadAvailablePojoClasses();

    // Worksheet name
    Label wlWorksheetName = new Label(wDecisionTableComp, SWT.RIGHT);
    wlWorksheetName.setText(BaseMessages.getString(PKG, "DecisionTable.WorksheetName.Label"));
    PropsUi.setLook(wlWorksheetName);
    FormData fdlWorksheetName = new FormData();
    fdlWorksheetName.left = new FormAttachment(0, 0);
    fdlWorksheetName.top = new FormAttachment(wJavaClassName, margin);
    fdlWorksheetName.right = new FormAttachment(50, -margin);
    wlWorksheetName.setLayoutData(fdlWorksheetName);

    wWorksheetName = new Combo(wDecisionTableComp, SWT.BORDER | SWT.DROP_DOWN);
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

  /** 加载可用的POJO类 */
  private void loadAvailablePojoClasses() {
    try {
      logBasic("开始扫描可用的POJO类...");
      List<String> pojoClasses = scanForPojoClasses();

      logBasic("找到 " + pojoClasses.size() + " 个符合条件的POJO类");
      for (String className : pojoClasses) {
        logDetailed("发现POJO类: " + className);
      }

      // 清空现有选项
      wJavaClassName.removeAll();

      // 添加扫描到的POJO类到下拉列表
      for (String className : pojoClasses) {
        wJavaClassName.add(className);
      }

      // 如果列表不为空，设置第一个作为默认选择
      if (!pojoClasses.isEmpty()) {
        wJavaClassName.select(0);
        logBasic("默认选择第一个POJO类: " + pojoClasses.get(0));
      } else {
        logBasic("警告：未找到任何符合 com.leapfuture.*.pojo.* 模式的类");
      }

    } catch (Exception e) {
      // 记录错误但不中断操作
      logError("Error loading available POJO classes: " + e.getMessage(), e);
    }
  }

  /** 扫描classpath中的com.leapfuture.*.pojo.*类 */
  private List<String> scanForPojoClasses() {
    List<String> classes = new ArrayList<>();

    try {
      // 1. 首先使用插件的 ClassLoader (更重要)
      ClassLoader pluginClassLoader = getClass().getClassLoader();
      scanWithClassLoader(pluginClassLoader, classes);

      // 2. 然后使用当前线程的 ClassLoader
      ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
      if (threadClassLoader != pluginClassLoader) {
        scanWithClassLoader(threadClassLoader, classes);
      }

      // 3. 扫描插件目录下的 lib 文件夹
      scanPluginLibDirectory(classes);

      // 4. 扫描系统 classpath (作为后备)
      scanSystemClasspath(classes);

    } catch (Exception e) {
      logError("Error scanning for POJO classes: " + e.getMessage());
    }

    // 使用LinkedHashSet去重并保持顺序，然后排序
    java.util.Set<String> uniqueClassSet = new java.util.LinkedHashSet<>(classes);
    List<String> uniqueClasses = new ArrayList<>(uniqueClassSet);
    Collections.sort(uniqueClasses);
    return uniqueClasses;
  }

  /** 使用指定的 ClassLoader 进行扫描 */
  private void scanWithClassLoader(ClassLoader classLoader, List<String> classes) {
    try {
      if (classLoader instanceof URLClassLoader) {
        URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
        URL[] urls = urlClassLoader.getURLs();

        for (URL url : urls) {
          scanUrl(url, classes);
        }
      }
    } catch (Exception e) {
      logError("Error scanning with ClassLoader: " + e.getMessage());
    }
  }

  /** 扫描插件 lib 目录 */
  private void scanPluginLibDirectory(List<String> classes) {
    try {
      // 获取插件目录路径
      File pluginJar =
          new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
      File pluginDir = pluginJar.getParentFile();

      logDetailed("插件JAR路径: " + pluginJar.getAbsolutePath());
      logDetailed("插件目录路径: " + pluginDir.getAbsolutePath());

      // 查找 lib 目录
      File libDir = new File(pluginDir, "lib");
      logDetailed("lib目录路径: " + libDir.getAbsolutePath());

      if (libDir.exists() && libDir.isDirectory()) {
        logBasic("正在扫描插件lib目录: " + libDir.getAbsolutePath());
        File[] jarFiles = libDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (jarFiles != null) {
          logBasic("找到 " + jarFiles.length + " 个JAR文件");
          for (File jarFile : jarFiles) {
            logDetailed("扫描JAR文件: " + jarFile.getName());
            int beforeCount = classes.size();
            scanJarFile(jarFile, classes);
            int afterCount = classes.size();
            if (afterCount > beforeCount) {
              logDetailed(
                  "从 " + jarFile.getName() + " 中找到 " + (afterCount - beforeCount) + " 个POJO类");
            }
          }
        }
      } else {
        logBasic("lib目录不存在: " + libDir.getAbsolutePath());

        // 尝试扫描插件目录本身的JAR文件
        File[] dirJarFiles =
            pluginDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (dirJarFiles != null && dirJarFiles.length > 0) {
          logBasic("扫描插件目录中的JAR文件...");
          for (File jarFile : dirJarFiles) {
            logDetailed("扫描插件JAR文件: " + jarFile.getName());
            scanJarFile(jarFile, classes);
          }
        }
      }
    } catch (Exception e) {
      logError("Error scanning plugin lib directory: " + e.getMessage(), e);
    }
  }

  /** 扫描系统 classpath */
  private void scanSystemClasspath(List<String> classes) {
    try {
      String classPath = System.getProperty("java.class.path");
      if (classPath != null) {
        String[] paths = classPath.split(System.getProperty("path.separator"));
        for (String path : paths) {
          try {
            File file = new File(path);
            if (file.exists()) {
              scanUrl(file.toURI().toURL(), classes);
            }
          } catch (Exception e) {
            // 忽略单个路径的错误
          }
        }
      }
    } catch (Exception e) {
      logError("Error scanning system classpath: " + e.getMessage());
    }
  }

  /** 扫描单个URL/路径中的类 */
  private void scanUrl(URL url, List<String> classes) {
    try {
      String path = url.getFile();
      File file = new File(path);

      if (file.isDirectory()) {
        scanDirectory(file, "", classes);
      } else if (path.endsWith(".jar")) {
        scanJarFile(file, classes);
      }
    } catch (Exception e) {
      // 忽略单个URL的错误
    }
  }

  /** 扫描目录中的类文件 */
  private void scanDirectory(File directory, String packageName, List<String> classes) {
    File[] files = directory.listFiles();
    if (files == null) return;

    for (File file : files) {
      if (file.isDirectory()) {
        String subPackage =
            packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
        scanDirectory(file, subPackage, classes);
      } else if (file.getName().endsWith(".class")) {
        String className = packageName + "." + file.getName().replace(".class", "");
        if (isPojoClass(className)) {
          classes.add(className);
        }
      }
    }
  }

  /** 扫描JAR文件中的类 */
  private void scanJarFile(File jarFile, List<String> classes) {
    try (JarFile jar = new JarFile(jarFile)) {
      Enumeration<JarEntry> entries = jar.entries();

      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name = entry.getName();

        if (name.endsWith(".class")) {
          String className = name.replace("/", ".").replace(".class", "");
          if (isPojoClass(className)) {
            classes.add(className);
          }
        }
      }
    } catch (Exception e) {
      // 忽略JAR文件错误
    }
  }

  /** 判断是否为符合条件的POJO类 */
  private boolean isPojoClass(String className) {
    return className.startsWith("com.leapfuture.") && className.contains(".pojo.");
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

  /** 从Excel文件中读取所有sheet名称 */
  private List<String> getExcelSheetNames(String fileName) {
    List<String> sheetNames = new ArrayList<>();

    if (Utils.isEmpty(fileName)) {
      return sheetNames;
    }

    // 解析变量
    String resolvedFileName = variables.resolve(fileName);
    java.io.File file = new java.io.File(resolvedFileName);

    if (!file.exists()) {
      logDetailed("Excel文件不存在: " + resolvedFileName);
      return sheetNames;
    }

    try {
      // 使用Apache POI读取Excel文件的sheet名称
      org.apache.poi.ss.usermodel.Workbook workbook = null;

      if (resolvedFileName.toLowerCase().endsWith(".xlsx")) {
        workbook =
            new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.FileInputStream(file));
      } else if (resolvedFileName.toLowerCase().endsWith(".xls")) {
        workbook =
            new org.apache.poi.hssf.usermodel.HSSFWorkbook(new java.io.FileInputStream(file));
      } else {
        logDetailed("不支持的文件格式: " + resolvedFileName);
        return sheetNames;
      }

      // 获取所有sheet名称
      int numberOfSheets = workbook.getNumberOfSheets();
      for (int i = 0; i < numberOfSheets; i++) {
        String sheetName = workbook.getSheetName(i);
        sheetNames.add(sheetName);
        logDetailed("发现sheet: " + sheetName);
      }

      workbook.close();

    } catch (Exception e) {
      logError("读取Excel文件sheet列表时出错: " + e.getMessage());
      // 即使出错也返回空列表，而不是抛出异常
    }

    return sheetNames;
  }

  /** 更新worksheet name下拉列表 */
  private void updateWorksheetNameList() {
    String fileName = wDecisionTableFile.getText();

    // 保存当前选择的值
    String currentSelection = wWorksheetName.getText();

    // 清空现有选项
    wWorksheetName.removeAll();

    // 检查文件路径是否包含变量
    if (containsVariables(fileName)) {
      // 如果包含变量，尝试在设计时resolve（可能有默认值）
      try {
        String resolvedFileName = variables.resolve(fileName);
        logDetailed("尝试解析变量: " + fileName + " -> " + resolvedFileName);

        if (!Utils.isEmpty(resolvedFileName) && !resolvedFileName.equals(fileName)) {
          // 成功解析且不同于原始值，尝试读取sheet
          List<String> sheetNames = getExcelSheetNames(resolvedFileName);
          if (!sheetNames.isEmpty()) {
            // 成功读取到sheet，正常处理
            for (String sheetName : sheetNames) {
              wWorksheetName.add(sheetName);
            }

            if (!Utils.isEmpty(currentSelection) && sheetNames.contains(currentSelection)) {
              wWorksheetName.setText(currentSelection);
            } else {
              wWorksheetName.select(0);
            }

            logBasic("通过变量解析成功加载 " + sheetNames.size() + " 个sheet");
            return;
          }
        }
      } catch (Exception e) {
        logDetailed("变量解析失败: " + e.getMessage());
      }

      // 解析失败或文件不存在，允许手动输入
      logDetailed("检测到变量，在设计时可能无法读取sheet列表: " + fileName);
      wWorksheetName.add(""); // 添加空选项

      // 恢复之前的选择（可能是手动输入的）
      if (!Utils.isEmpty(currentSelection)) {
        wWorksheetName.setText(currentSelection);
        logDetailed("恢复之前的worksheet选择: " + currentSelection);
      }

      logBasic("文件路径包含变量，运行时将自动解析。当前允许手动输入worksheet名称。");
      return;
    }

    // 获取sheet列表
    List<String> sheetNames = getExcelSheetNames(fileName);

    if (sheetNames.isEmpty()) {
      // 如果没有找到sheet，允许用户手动输入
      wWorksheetName.add("");
      logDetailed("未找到Excel sheet，允许手动输入");
    } else {
      // 添加所有sheet到下拉列表
      for (String sheetName : sheetNames) {
        wWorksheetName.add(sheetName);
      }

      // 尝试恢复之前的选择
      if (!Utils.isEmpty(currentSelection) && sheetNames.contains(currentSelection)) {
        wWorksheetName.setText(currentSelection);
        logDetailed("恢复之前选择的sheet: " + currentSelection);
      } else if (!sheetNames.isEmpty()) {
        // 默认选择第一个sheet
        wWorksheetName.select(0);
        logDetailed("默认选择第一个sheet: " + sheetNames.get(0));
      }

      logBasic("已加载 " + sheetNames.size() + " 个Excel sheet到下拉列表");
    }
  }

  /** 检查字符串是否包含Hop变量 */
  private boolean containsVariables(String text) {
    if (Utils.isEmpty(text)) {
      return false;
    }
    // 检查是否包含 ${...} 或 %%...%% 格式的变量
    return text.contains("${") || text.contains("%%");
  }
}
