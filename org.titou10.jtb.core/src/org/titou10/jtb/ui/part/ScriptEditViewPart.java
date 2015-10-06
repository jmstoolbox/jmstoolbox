/*
 * Copyright (C) 2015 Denis Forveille titou10.titou10@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.titou10.jtb.ui.part;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.script.ScriptsUtils;
import org.titou10.jtb.script.gen.DataFile;
import org.titou10.jtb.script.gen.GlobalVariable;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Step;
import org.titou10.jtb.script.gen.StepKind;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.DNDData;
import org.titou10.jtb.util.DNDData.DNDElement;
import org.titou10.jtb.variable.gen.Variable;

/**
 * Manage the Script Editor
 * 
 * @author Denis Forveille
 *
 */
@SuppressWarnings("restriction")
public class ScriptEditViewPart {

   private static final Logger log                    = LoggerFactory.getLogger(ScriptEditViewPart.class);

   private static final String VARIABLE_ALREADY_EXIST = "Variable '%s' is already in the list";

   @Inject
   private ECommandService     commandService;

   @Inject
   private EHandlerService     handlerService;

   @Inject
   private ESelectionService   selectionService;

   @Inject
   private EMenuService        menuService;

   @Inject
   private MDirtyable          dirty;

   @Inject
   private ConfigManager       cm;

   private Variable            selectedVariable;

   // Business data
   private Script              workingScript;

   // JFaces components
   private Composite           stepsComposite;
   private Composite           gvComposite;
   private Composite           dfComposite;
   private TableViewer         tvSteps;
   private TableViewer         tvGlobalVariables;
   private TableViewer         tvDataFiles;

   @Inject
   @Optional
   public void refreshScript(Shell shell,
                             MWindow window,
                             MPart part,
                             @UIEventTopic(Constants.EVENT_REFRESH_SCRIPT_EDIT) String noUse) {
      log.debug("refresh with {}", workingScript);

      // Refresh Steps and Global Variables
      tvSteps.setInput(workingScript.getStep());
      tvGlobalVariables.setInput(workingScript.getGlobalVariable());
      tvDataFiles.setInput(workingScript.getDataFile());

      tvSteps.refresh();
      tvGlobalVariables.refresh();
      tvDataFiles.refresh();

      stepsComposite.layout();
      gvComposite.layout();
      dfComposite.layout();

      // Utils.resizeTableViewer(tvSteps);
      // Utils.resizeTableViewer(tvGlobalVariables);
      // Utils.resizeTableViewer(tvDataFiles);

   }

   @Persist
   public void persist(MDirtyable dirty) {
      log.debug("Save script on window close and user choose to save the editor");
      saveScript();
   }

   private void saveScript() {

      // Call ScriptSave Command
      ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_SCRIPT_SAVE, null);
      handlerService.executeHandler(myCommand);

   }

   @Focus
   public void focus(MWindow window, MPart mpart) {
      log.debug("Focus set on '{}'", mpart.getElementId());

      // When focus changes, change the "active" script
      window.getContext().set(Constants.CURRENT_WORKING_SCRIPT, this.workingScript);

      // Mandatory. if not there, double clicks on script browser are broken...
      stepsComposite.setFocus();
   }

   @PostConstruct
   public void createControls(final Shell shell,
                              MWindow window,
                              Composite parent,
                              ConfigManager cm,
                              @Named(Constants.CURRENT_WORKING_SCRIPT) Script workingScript) {

      final Composite container = (Composite) new Composite(parent, SWT.NONE);
      container.setLayout(new FillLayout(SWT.HORIZONTAL));

      TabFolder tabFolder = new TabFolder(container, SWT.NONE);

      // ------------------
      // General Tab
      // ------------------
      TabItem tbtmGeneral = new TabItem(tabFolder, SWT.NONE);
      tbtmGeneral.setText("Steps");

      stepsComposite = new Composite(tabFolder, SWT.NONE);
      tbtmGeneral.setControl(stepsComposite);
      stepsComposite.setLayout(new GridLayout(1, false));

      tvSteps = createSteps(shell, stepsComposite);

      // --------------------
      // Global Variables Tab
      // --------------------

      TabItem tbtmGlobalVariables = new TabItem(tabFolder, SWT.NONE);
      tbtmGlobalVariables.setText("Global Variables");

      gvComposite = new Composite(tabFolder, SWT.NONE);
      tbtmGlobalVariables.setControl(gvComposite);
      gvComposite.setLayout(new GridLayout(1, false));

      tvGlobalVariables = createGlobalVariables(shell, gvComposite);

      // --------------------
      // Data Files Tab
      // --------------------

      TabItem tbtmDataFiles = new TabItem(tabFolder, SWT.NONE);
      tbtmDataFiles.setText("Data Files");

      dfComposite = new Composite(tabFolder, SWT.NONE);
      tbtmDataFiles.setControl(dfComposite);
      dfComposite.setLayout(new GridLayout(1, false));

      tvDataFiles = createDataFiles(shell, dfComposite);

      // // Save Handler
      // window.getContext().set(ISaveHandler.class, new PartServiceSaveHandler());

      // ---------------
      // Dialog Shortcuts
      // ----------------

      shell.getDisplay().addFilter(SWT.KeyUp, new Listener() {
         @Override
         public void handleEvent(Event e) {
            // Fast fail
            if (e.keyCode != 's') {
               return;
            }
            if (e.widget instanceof Control && isChild(container, (Control) e.widget)) {
               if ((e.stateMask & SWT.CTRL) != 0) {
                  log.debug("CTRL-S pressed");
                  saveScript();
                  return;
               }
            }
         }
      });

      // Set Content
      this.workingScript = workingScript;
      tvSteps.setInput(workingScript.getStep());
      tvGlobalVariables.setInput(workingScript.getGlobalVariable());
      tvDataFiles.setInput(workingScript.getDataFile());
   }

   // -------
   // Helpers
   // -------

   private static boolean isChild(Control parent, Control child) {
      if (child.equals(parent)) {
         return true;
      }

      Composite p = child.getParent();
      if (p == null) {
         return false;
      }

      return isChild(parent, p);
   }

   private TableViewer createSteps(final Shell shell, final Composite parentComposite) {

      // Steps table
      Composite compositeSteps = new Composite(parentComposite, SWT.NONE);
      compositeSteps.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      TableColumnLayout tcl = new TableColumnLayout();
      compositeSteps.setLayout(tcl);

      final TableViewer stepTableViewer = new TableViewer(compositeSteps, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      final Table stepsTable = stepTableViewer.getTable();
      stepsTable.setHeaderVisible(true);
      stepsTable.setLinesVisible(true);

      TableViewerColumn stepTemplateNameColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      TableColumn stepTemplateNameHeader = stepTemplateNameColumn.getColumn();
      tcl.setColumnData(stepTemplateNameHeader, new ColumnWeightData(3, ColumnWeightData.MINIMUM_WIDTH, true));
      stepTemplateNameHeader.setText("Template");
      stepTemplateNameColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            Step s = (Step) element;
            return s.getTemplateName();
         }

         @Override
         public void update(ViewerCell cell) {
            super.update(cell);
            Step s = (Step) cell.getElement();
            if (s.getKind() == StepKind.PAUSE) {
               cell.setBackground(SWTResourceManager.getColor(222, 222, 222));
            }
         }
      });

      TableViewerColumn stepSessionNameColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      TableColumn stepSessionNameHeader = stepSessionNameColumn.getColumn();
      tcl.setColumnData(stepSessionNameHeader, new ColumnWeightData(3, ColumnWeightData.MINIMUM_WIDTH, true));
      stepSessionNameHeader.setText("Session");
      stepSessionNameColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            Step s = (Step) element;
            return s.getSessionName();
         }

         @Override
         public void update(ViewerCell cell) {
            super.update(cell);
            Step s = (Step) cell.getElement();
            if (s.getKind() == StepKind.PAUSE) {
               cell.setBackground(SWTResourceManager.getColor(222, 222, 222));
            }
         }
      });

      TableViewerColumn stepDestinationNameColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      TableColumn stepDestinationNameHeader = stepDestinationNameColumn.getColumn();
      tcl.setColumnData(stepDestinationNameHeader, new ColumnWeightData(3, ColumnWeightData.MINIMUM_WIDTH, true));
      stepDestinationNameHeader.setText("Destination");
      stepDestinationNameColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            Step s = (Step) element;
            return s.getDestinationName();
         }

         @Override
         public void update(ViewerCell cell) {
            super.update(cell);
            Step s = (Step) cell.getElement();
            if (s.getKind() == StepKind.PAUSE) {
               cell.setBackground(SWTResourceManager.getColor(222, 222, 222));
            }
         }

      });

      TableViewerColumn stepDataFileNameColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      TableColumn stepDataFileNameHeader = stepDataFileNameColumn.getColumn();
      tcl.setColumnData(stepDataFileNameHeader, new ColumnWeightData(3, ColumnWeightData.MINIMUM_WIDTH, true));
      stepDataFileNameHeader.setText("Data File");
      stepDataFileNameColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            Step s = (Step) element;
            if (s.getVariablePrefix() != null) {
               DataFile dataFile = ScriptsUtils.findDataFileByVariablePrefix(workingScript, s.getVariablePrefix());
               return ScriptsUtils.buildDataFileDislayName(dataFile);
            } else {
               return "";
            }
         }

         @Override
         public void update(ViewerCell cell) {
            super.update(cell);
            Step s = (Step) cell.getElement();
            if (s.getKind() == StepKind.PAUSE) {
               cell.setBackground(SWTResourceManager.getColor(222, 222, 222));
            }
         }

      });

      TableViewerColumn stepIterationsColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      TableColumn stepIterationsHeader = stepIterationsColumn.getColumn();
      stepIterationsHeader.setAlignment(SWT.CENTER);
      tcl.setColumnData(stepIterationsHeader, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, false));
      stepIterationsHeader.setText("Iterations");
      stepIterationsColumn.setLabelProvider(new ColumnLabelProvider() {

         @Override
         public String getText(Object element) {
            Step s = (Step) element;
            if (s.getKind() == StepKind.REGULAR) {
               return String.valueOf(s.getIterations());
            } else {
               return "";
            }
         }

         @Override
         public void update(ViewerCell cell) {
            super.update(cell);
            Step s = (Step) cell.getElement();
            if (s.getKind() == StepKind.PAUSE) {
               cell.setBackground(SWTResourceManager.getColor(222, 222, 222));
            }
         }
      });

      TableViewerColumn stepPauseSecsColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      TableColumn stepPauseSecsHeader = stepPauseSecsColumn.getColumn();
      stepPauseSecsHeader.setAlignment(SWT.CENTER);
      tcl.setColumnData(stepPauseSecsHeader, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, false));
      stepPauseSecsHeader.setText("Pause (s)");
      stepPauseSecsColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            Step s = (Step) element;
            if (s.getPauseSecsAfter() != null) {
               return s.getPauseSecsAfter().toString();
            }
            return "";
         }

      });

      // Attach the Popup Menu
      menuService.registerContextMenu(stepsTable, Constants.SCRIPT_POPUP_MENU);

      // Remove a step from the list
      stepsTable.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.keyCode == SWT.DEL) {
               IStructuredSelection selection = (IStructuredSelection) stepTableViewer.getSelection();
               if (selection.isEmpty()) {
                  return;
               }
               for (Object sel : selection.toList()) {
                  Step s = (Step) sel;
                  log.debug("Remove {} from the list", s);
                  workingScript.getStep().remove(s);
                  stepTableViewer.remove(s);
               }

               dirty.setDirty(true);

               parentComposite.layout();
               // Utils.resizeTableViewer(tvSteps);
            }
         }
      });

      // Manage selections
      stepTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            selectionService.setSelection(selection.getFirstElement());
         }
      });

      // Double Click: edit Step
      stepTableViewer.addDoubleClickListener(new IDoubleClickListener() {

         @Override
         public void doubleClick(DoubleClickEvent event) {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            selectionService.setSelection(selection.getFirstElement());

            // Call Step Add or Edit Command
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(Constants.COMMAND_SCRIPT_NEWSTEP_PARAM, Constants.COMMAND_SCRIPT_NEWSTEP_EDIT);
            ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_SCRIPT_NEWSTEP, parameters);
            handlerService.executeHandler(myCommand);
         }
      });

      // Drag and Drop: "URLTransfer" are restricted to Steps Drag & Drop = reordering..
      int operations = DND.DROP_MOVE;
      Transfer[] transferTypes = new Transfer[] { URLTransfer.getInstance() };
      stepTableViewer.addDragSupport(operations, transferTypes, new StepDragListener(stepTableViewer));
      stepTableViewer.addDropSupport(operations, transferTypes, new StepDropListener(shell, stepTableViewer));

      stepTableViewer.setContentProvider(ArrayContentProvider.getInstance());
      return stepTableViewer;
   }

   private TableViewer createGlobalVariables(final Shell shell, final Composite parentComposite) {

      // Header
      Composite compositeHeader = new Composite(parentComposite, SWT.NONE);
      compositeHeader.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      compositeHeader.setBounds(0, 0, 154, 33);
      GridLayout glCompositeHeader = new GridLayout(3, false);
      glCompositeHeader.marginWidth = 0;
      compositeHeader.setLayout(glCompositeHeader);

      Label lblNewLabel = new Label(compositeHeader, SWT.NONE);
      lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
      lblNewLabel.setAlignment(SWT.CENTER);
      lblNewLabel.setText("Variable Name");

      Label lblNewLabel2 = new Label(compositeHeader, SWT.NONE);
      lblNewLabel2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      lblNewLabel2.setAlignment(SWT.CENTER);
      lblNewLabel2.setText("Constant value (Optional)");

      Label lblNewLabel1 = new Label(compositeHeader, SWT.NONE);
      lblNewLabel1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));

      final ComboViewer cvGVName = new ComboViewer(compositeHeader, SWT.READ_ONLY);
      cvGVName.setContentProvider(ArrayContentProvider.getInstance());
      cvGVName.setLabelProvider(new LabelProvider() {
         @Override
         public String getText(Object element) {
            return ((Variable) element).getName();
         }
      });
      cvGVName.setInput(cm.getVariables());

      final Text txtGVValue = new Text(compositeHeader, SWT.BORDER);
      txtGVValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnAddVariable = new Button(compositeHeader, SWT.NONE);
      btnAddVariable.setText("Add");

      // Variables
      Composite compositeVariables = new Composite(parentComposite, SWT.NONE);
      compositeVariables.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      // compositeVariables.setBounds(0, 0, 64, 64);
      TableColumnLayout tcl = new TableColumnLayout();
      compositeVariables.setLayout(tcl);

      final TableViewer tableViewer = new TableViewer(compositeVariables, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      final Table gvTable = tableViewer.getTable();
      gvTable.setHeaderVisible(true);
      gvTable.setLinesVisible(true);

      TableViewerColumn gvNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn gvNameHeader = gvNameColumn.getColumn();
      tcl.setColumnData(gvNameHeader, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
      gvNameHeader.setAlignment(SWT.CENTER);
      gvNameHeader.setText("Name");
      gvNameColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            GlobalVariable gv = (GlobalVariable) element;
            return gv.getName();
         }
      });

      TableViewerColumn gvValueColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      gvValueColumn.setEditingSupport(new ValueEditingSupport(tableViewer, dirty));
      TableColumn gvValueHeader = gvValueColumn.getColumn();
      tcl.setColumnData(gvValueHeader, new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH, true));
      gvValueHeader.setText("Constant value");
      gvValueColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            GlobalVariable gv = (GlobalVariable) element;
            return gv.getConstantValue();
         }
      });

      tableViewer.setContentProvider(ArrayContentProvider.getInstance());

      // Add a new Variable
      btnAddVariable.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {

            String name = selectedVariable.getName();

            // Validate that a variable with the same name does not exit
            for (GlobalVariable gv : workingScript.getGlobalVariable()) {
               if (gv.getName().equals(name)) {
                  MessageDialog.openError(shell, "Validation error", String.format(VARIABLE_ALREADY_EXIST, name));
                  return;
               }
            }

            String value = txtGVValue.getText().trim();
            if (value.isEmpty()) {
               value = null;
            }

            GlobalVariable gv = new GlobalVariable();
            gv.setName(name);
            gv.setConstantValue(value);

            workingScript.getGlobalVariable().add(gv);
            tableViewer.add(gv);

            dirty.setDirty(true);

            parentComposite.layout();
            // Utils.resizeTableViewer(tvGlobalVariables);
         }
      });

      // Remove a Global Variable from the list
      gvTable.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.keyCode == SWT.DEL) {
               IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
               if (selection.isEmpty()) {
                  return;
               }
               for (Object sel : selection.toList()) {
                  GlobalVariable gv = (GlobalVariable) sel;
                  log.debug("Remove Global Variable '{}' from the list", gv.getName());
                  workingScript.getGlobalVariable().remove(gv);
                  tableViewer.remove(gv);

                  dirty.setDirty(true);
               }

               parentComposite.layout();
               // Utils.resizeTableViewer(tvGlobalVariables);
            }
         }
      });

      // Select the first variable in the combo box
      selectedVariable = cm.getVariables().get(0);
      ISelection selection = new StructuredSelection(selectedVariable);
      cvGVName.setSelection(selection);

      // Save the variable selected in the combo box
      cvGVName.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            selectedVariable = (Variable) sel.getFirstElement();
         }
      });

      return tableViewer;

   }

   private TableViewer createDataFiles(final Shell shell, final Composite parentComposite) {

      // Data Files table
      Composite compositeSteps = new Composite(parentComposite, SWT.NONE);
      compositeSteps.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      TableColumnLayout tcl = new TableColumnLayout();
      compositeSteps.setLayout(tcl);

      final TableViewer tableViewer = new TableViewer(compositeSteps, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      final Table table = tableViewer.getTable();
      table.setHeaderVisible(true);
      table.setLinesVisible(true);

      TableViewerColumn varPrefixColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn varPrefixHeader = varPrefixColumn.getColumn();
      tcl.setColumnData(varPrefixHeader, new ColumnWeightData(3, ColumnWeightData.MINIMUM_WIDTH, true));
      varPrefixHeader.setText("Var. Prefix");
      varPrefixColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            DataFile df = (DataFile) element;
            return df.getVariablePrefix();
         }
      });

      TableViewerColumn delimiterColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn delimiterHeader = delimiterColumn.getColumn();
      tcl.setColumnData(delimiterHeader, new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH, true));
      delimiterHeader.setText("Delimiter");
      delimiterColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            DataFile df = (DataFile) element;
            return df.getDelimiter();
         }
      });

      TableViewerColumn variableNamesColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn variableHeader = variableNamesColumn.getColumn();
      tcl.setColumnData(variableHeader, new ColumnWeightData(6, ColumnWeightData.MINIMUM_WIDTH, true));
      variableHeader.setText("Variable Names");
      variableNamesColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            DataFile df = (DataFile) element;
            return df.getVariableNames();
         }
      });

      TableViewerColumn fileNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn fileNameHeader = fileNameColumn.getColumn();
      tcl.setColumnData(fileNameHeader, new ColumnWeightData(10, ColumnWeightData.MINIMUM_WIDTH, false));
      fileNameHeader.setText("File Name");
      fileNameColumn.setLabelProvider(new ColumnLabelProvider() {

         @Override
         public String getText(Object element) {
            DataFile df = (DataFile) element;
            return df.getFileName();
         }
      });

      // Attach the Popup Menu
      menuService.registerContextMenu(table, Constants.SCRIPT_DATAFILE_POPUP_MENU);

      // Remove a data file from the list
      table.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.keyCode == SWT.DEL) {
               IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
               if (selection.isEmpty()) {
                  return;
               }
               for (Object sel : selection.toList()) {
                  DataFile df = (DataFile) sel;
                  log.debug("Remove {} from the list", df);
                  workingScript.getDataFile().remove(df);
                  tableViewer.remove(df);
               }

               dirty.setDirty(true);

               parentComposite.layout();
               // Utils.resizeTableViewer(tvDataFiles);
            }
         }
      });

      // Manage selections
      tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            selectionService.setSelection(selection.getFirstElement());
         }
      });

      // Double Click: edit Data File
      tableViewer.addDoubleClickListener(new IDoubleClickListener() {

         @Override
         public void doubleClick(DoubleClickEvent event) {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            selectionService.setSelection(selection.getFirstElement());

            // Call Data File Add or Edit Command
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(Constants.COMMAND_SCRIPT_NEWDF_PARAM, Constants.COMMAND_SCRIPT_NEWDF_EDIT);
            ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_SCRIPT_NEWDF, parameters);
            handlerService.executeHandler(myCommand);
         }
      });

      tableViewer.setContentProvider(ArrayContentProvider.getInstance());
      return tableViewer;

   }

   // --------------
   // Helper Classes
   // --------------

   private class PartServiceSaveHandler implements ISaveHandler {

      public boolean save(MPart dirtyPart, boolean confirm) {

         if (confirm) {
            switch (promptToSave(dirtyPart)) {
               case NO:
                  return true;

               case CANCEL:
                  return false;

               case YES:
                  break;
            }
         }

         Object client = dirtyPart.getObject();

         try {
            ContextInjectionFactory.invoke(client, Persist.class, dirtyPart.getContext());
         } catch (InjectionException e) {
            log.error("Failed to persist contents of part ({0})", dirtyPart.getElementId(), e);
            return false;
         } catch (RuntimeException e) {
            log.error("Failed to persist contents of part ({0}) via DI", dirtyPart.getElementId(), e);
            return false;
         }

         return true;
      }

      public boolean saveParts(Collection<MPart> dirtyParts, boolean confirm) {

         if (confirm) {
            List<MPart> dirtyPartsList = Collections.unmodifiableList(new ArrayList<MPart>(dirtyParts));

            Save[] decisions = promptToSave(dirtyPartsList);

            for (Save decision : decisions) {
               if (decision == Save.CANCEL) {
                  return false;
               }
            }

            for (int i = 0; i < decisions.length; i++) {
               if (decisions[i] == Save.YES) {
                  if (!save(dirtyPartsList.get(i), false)) {
                     return false;
                  }
               }
            }
            return true;
         }

         for (MPart dirtyPart : dirtyParts) {
            if (!save(dirtyPart, false)) {
               return false;
            }
         }

         return true;
      }

      public Save promptToSave(MPart dirtyPart) {
         return Save.YES;

      }

      public Save[] promptToSave(Collection<MPart> dirtyParts) {
         Save[] rc = new Save[dirtyParts.size()];
         for (int i = 0; i < rc.length; i++) {
            rc[i] = Save.YES;
         }
         return rc;
      }

   }

   private static final class ValueEditingSupport extends EditingSupport {

      private final TableViewer viewer;
      private final CellEditor  editor;
      private MDirtyable        dirty;

      public ValueEditingSupport(TableViewer viewer, MDirtyable dirty) {
         super(viewer);
         this.viewer = viewer;
         this.dirty = dirty;
         this.editor = new TextCellEditor(viewer.getTable());
      }

      @Override
      protected CellEditor getCellEditor(Object element) {
         return editor;
      }

      @Override
      protected boolean canEdit(Object element) {
         return true;
      }

      @Override
      protected Object getValue(Object element) {
         GlobalVariable gv = (GlobalVariable) element;
         String s = gv.getConstantValue();
         if (s == null) {
            return "";
         } else {
            return s;
         }
      }

      @Override
      protected void setValue(Object element, Object userInputValue) {
         GlobalVariable gv = (GlobalVariable) element;
         String s = String.valueOf(userInputValue);
         if (s.trim().isEmpty()) {
            s = null;
         }
         gv.setConstantValue(s);
         viewer.update(element, null);

         dirty.setDirty(true);
      }
   }

   // -----------------------
   // Providers and Listeners
   // -----------------------

   private class StepDragListener extends DragSourceAdapter {
      private TableViewer tableViewer;
      private Step        sourceStep;

      public StepDragListener(TableViewer tableViewer) {
         this.tableViewer = tableViewer;
      }

      @Override
      public void dragStart(DragSourceEvent event) {
         log.debug("Start Drag");

         // Only allow one step at a time (for now...)
         IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
         if ((selection == null) || (selection.size() != 1)) {
            event.doit = false;
            return;
         }

         sourceStep = (Step) selection.getFirstElement();
      }

      public void dragSetData(DragSourceEvent event) {
         if (URLTransfer.getInstance().isSupportedType(event.dataType)) {
            event.data = "unused";

            DNDData.setDrag(DNDElement.STEP);
            DNDData.setSourceStep(sourceStep);
         }
      }
   }

   private class StepDropListener extends ViewerDropAdapter {
      private TableViewer tableViewer;
      private Step        target;

      public StepDropListener(Shell shell, TableViewer tableViewer) {
         super(tableViewer);
         this.tableViewer = tableViewer;
      }

      @Override
      public void drop(DropTargetEvent event) {
         target = (Step) determineTarget(event);
         DNDData.setTargetStep(target);
         super.drop(event);
      }

      @Override
      public boolean performDrop(Object data) {

         List<Step> steps = workingScript.getStep();
         Step source = DNDData.getSourceStep();
         Step currentTarget = (Step) getCurrentTarget();

         int n;
         if (currentTarget != null) {
            n = steps.indexOf(currentTarget);
            int loc = getCurrentLocation();
            if (loc == LOCATION_BEFORE) {
               n--;
            }
         } else {
            n = steps.indexOf(target);
         }

         if ((n < 0) || (n > steps.size())) {
            return false;
         }

         steps.remove(source);
         steps.add(n, source);

         // Refresh TableViewer
         tableViewer.refresh();

         // Script is now dirty
         dirty.setDirty(true);

         return true;
      }

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferData) {
         return URLTransfer.getInstance().isSupportedType(transferData);
      }
   }
}
