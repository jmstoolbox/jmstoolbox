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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.EditingSupport;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
import org.titou10.jtb.script.gen.GlobalVariable;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Step;
import org.titou10.jtb.script.gen.StepKind;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.variable.gen.Variable;

/**
 * Manage the Script Editor
 * 
 * @author Denis Forveille
 *
 */
public class ScriptEditViewPart {

   private static final Logger log = LoggerFactory.getLogger(ScriptEditViewPart.class);

   private static final String PROPERTY_ALREADY_EXIST = "Variable '%s' is already in the list";

   @Inject
   private EPartService partService;

   @Inject
   private ConfigManager cm;

   private Variable selectedVariable;

   // private Button btnPromptVariables;

   // Business data
   private Script script;

   // JFaces components
   private Composite   stepsComposite;
   private Composite   gvComposite;
   private TableViewer tvSteps;
   private TableViewer tvGlobalVariables;

   @Inject
   @Optional
   public void getNotified(MPart part, @UIEventTopic(Constants.EVENT_REFRESH_SCRIPT_EDIT) Script script) {
      log.debug("refresh with {}", script);

      this.script = script;

      // if (script.isPromptVariables() != null) {
      // btnPromptVariables.setSelection(script.isPromptVariables());
      // }

      // Refresh Steps and Global Variables
      tvSteps.setInput(script.getStep());
      tvGlobalVariables.setInput(script.getGlobalVariable());

      stepsComposite.layout();
      gvComposite.layout();

      // Change Part name
      part.setLabel("Script '" + script.getName() + "'");

      partService.activate(part);
   }

   @PostConstruct
   public void createControls(Shell shell, Composite parent, ConfigManager cm) {

      script = new Script();

      final Composite container = (Composite) new Composite(parent, SWT.NONE);
      container.setLayout(new FillLayout(SWT.HORIZONTAL));

      TabFolder tabFolder = new TabFolder(container, SWT.NONE);

      // ------------------
      // General Tab
      // ------------------
      TabItem tbtmGeneral = new TabItem(tabFolder, SWT.NONE);
      tbtmGeneral.setText("General");

      stepsComposite = new Composite(tabFolder, SWT.NONE);
      tbtmGeneral.setControl(stepsComposite);
      stepsComposite.setLayout(new GridLayout(1, false));

      createSteps(shell, stepsComposite);

      // --------------------
      // Global Variables Tab
      // --------------------

      TabItem tbtmGlobalVariables = new TabItem(tabFolder, SWT.NONE);
      tbtmGlobalVariables.setText("Global Variables");

      gvComposite = new Composite(tabFolder, SWT.NONE);
      tbtmGlobalVariables.setControl(gvComposite);
      gvComposite.setLayout(new GridLayout(1, false));

      createGlobalVariables(shell, gvComposite);

      // --------------------
      // Execution Tab
      // --------------------

      TabItem tbtmExecution = new TabItem(tabFolder, SWT.NONE);
      tbtmExecution.setText("Execution");

      Composite composite3 = new Composite(tabFolder, SWT.NONE);
      tbtmExecution.setControl(composite3);
      composite3.setLayout(new GridLayout(1, false));

      createExecution(composite3);

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
                  // buttonPressed(MessageEditDialog.BUTTON_SAVE_TEMPLATE);
                  // buttonPressed(IDialogConstants.OK_ID);
                  return;
               }
            }
         }
      });

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

   private void createSteps(final Shell shell, final Composite parentComposite) {

      // // Header lines
      // Composite compositeHeader = new Composite(parentComposite, SWT.NONE);
      // compositeHeader.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      // compositeHeader.setLayout(new GridLayout(3, false));
      //
      // Label lblNewLabel2 = new Label(compositeHeader, SWT.NONE);
      // lblNewLabel2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      // lblNewLabel2.setText("Prompt for variables?");
      //
      // btnPromptVariables = new Button(compositeHeader, SWT.CHECK);

      // Steps table
      Composite compositeSteps = new Composite(parentComposite, SWT.NONE);
      compositeSteps.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      TableColumnLayout tcl = new TableColumnLayout();
      compositeSteps.setLayout(tcl);

      final TableViewer stepTableViewer = new TableViewer(compositeSteps, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      final Table stepsTable = stepTableViewer.getTable();
      stepsTable.setHeaderVisible(true);
      stepsTable.setLinesVisible(true);

      TableViewerColumn stepTemplateNameColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      TableColumn stepTemplateNameHeader = stepTemplateNameColumn.getColumn();
      tcl.setColumnData(stepTemplateNameHeader, new ColumnWeightData(3, ColumnWeightData.MINIMUM_WIDTH, true));
      stepTemplateNameHeader.setAlignment(SWT.CENTER);
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
               cell.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
            }
         }
      });

      TableViewerColumn stepSessionNameColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      // stepSessionNameColumn.setEditingSupport(new ValueEditingSupport(stepTableViewer));
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
               cell.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
            }
         }
      });

      TableViewerColumn stepDestinationNameColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      // stepDestinationNameColumn.setEditingSupport(new ValueEditingSupport(tableViewer));
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
               cell.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
            }
         }

      });

      TableViewerColumn stepPauseSecsColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      // stepDestinationNameColumn.setEditingSupport(new ValueEditingSupport(tableViewer));
      TableColumn stepPauseSecsHeader = stepPauseSecsColumn.getColumn();
      tcl.setColumnData(stepPauseSecsHeader, new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH, true));
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

      TableViewerColumn stepIterationsColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      // stepDestinationNameColumn.setEditingSupport(new ValueEditingSupport(tableViewer));
      TableColumn stepIterationsHeader = stepIterationsColumn.getColumn();
      tcl.setColumnData(stepIterationsHeader, new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH, true));
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
               cell.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
            }
         }
      });

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
                  script.getStep().remove(s);
                  stepTableViewer.remove(s);
               }

               // propertyNameColumn.pack();
               // propertyValueColumn.pack();
               stepsTable.pack();

               parentComposite.layout();
            }
         }
      });

      stepTableViewer.setContentProvider(ArrayContentProvider.getInstance());
      tvSteps = stepTableViewer;
   }

   private void createGlobalVariables(final Shell shell, final Composite parentComposite) {

      // Header
      Composite compositeHeader = new Composite(parentComposite, SWT.NONE);
      compositeHeader.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      compositeHeader.setBounds(0, 0, 154, 33);
      compositeHeader.setLayout(new GridLayout(3, false));

      Label lblNewLabel = new Label(compositeHeader, SWT.NONE);
      lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      lblNewLabel.setAlignment(SWT.CENTER);
      lblNewLabel.setText("Variable");

      Label lblNewLabel2 = new Label(compositeHeader, SWT.NONE);
      lblNewLabel2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      lblNewLabel2.setAlignment(SWT.CENTER);
      lblNewLabel2.setText("Constant value (Optional)");

      Label lblNewLabel1 = new Label(compositeHeader, SWT.NONE);
      lblNewLabel1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));

      final ComboViewer cvGVName = new ComboViewer(compositeHeader, SWT.READ_ONLY);
      Combo combo = cvGVName.getCombo();
      combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
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

      Button btnAddProperty = new Button(compositeHeader, SWT.NONE);
      btnAddProperty.setText("Add");

      // Variables
      Composite compositeVariables = new Composite(parentComposite, SWT.NONE);
      compositeVariables.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      // compositeVariables.setBounds(0, 0, 64, 64);
      TableColumnLayout tcl = new TableColumnLayout();
      compositeVariables.setLayout(tcl);

      final TableViewer tableViewer = new TableViewer(compositeVariables, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      final Table gvTable = tableViewer.getTable();
      gvTable.setHeaderVisible(true);
      gvTable.setLinesVisible(true);

      TableViewerColumn gvNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn gvNameHeader = gvNameColumn.getColumn();
      tcl.setColumnData(gvNameHeader, new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH, true));
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
      gvValueColumn.setEditingSupport(new ValueEditingSupport(tableViewer));
      TableColumn gvValueHeader = gvValueColumn.getColumn();
      tcl.setColumnData(gvValueHeader, new ColumnWeightData(3, ColumnWeightData.MINIMUM_WIDTH, true));
      gvValueHeader.setText("Constant value");
      gvValueColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            GlobalVariable gv = (GlobalVariable) element;
            return gv.getConstantValue();
         }
      });

      tableViewer.setContentProvider(ArrayContentProvider.getInstance());

      // Add a new Property
      btnAddProperty.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {

            String name = selectedVariable.getName();

            // Validate that a property with the same name does not exit
            for (GlobalVariable gv : script.getGlobalVariable()) {
               if (gv.getName().equals(name)) {
                  MessageDialog.openError(shell, "Validation error", String.format(PROPERTY_ALREADY_EXIST, name));
                  return;
               }
            }

            // TODO Validate that the default value is compatible with the variable

            String value = txtGVValue.getText().trim();
            if (value.isEmpty()) {
               value = null;
            }

            GlobalVariable gv = new GlobalVariable();
            gv.setName(name);
            gv.setConstantValue(value);

            script.getGlobalVariable().add(gv);
            tableViewer.add(gv);
            parentComposite.layout();

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
                  script.getGlobalVariable().remove(gv);
                  tableViewer.remove(gv);
               }

               // propertyNameColumn.pack();
               // propertyValueColumn.pack();
               gvTable.pack();

               parentComposite.layout();
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

      tvGlobalVariables = tableViewer;

   }

   private void createExecution(final Composite parentComposite) {

      // Header
      Composite compositeHeader = new Composite(parentComposite, SWT.NONE);
      compositeHeader.setLayout(new RowLayout(SWT.HORIZONTAL));
      compositeHeader.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
      compositeHeader.setBounds(0, 0, 154, 33);

      Button btnSimulate = new Button(compositeHeader, SWT.NONE);
      btnSimulate.setText("Simulate");

      Button btnStep = new Button(compositeHeader, SWT.NONE);
      btnStep.setText("Step Execute");

      Button btnExecute = new Button(compositeHeader, SWT.NONE);
      btnExecute.setText("Execute");

   }

   // --------------
   // Helper Classes
   // --------------

   private static final class ValueEditingSupport extends EditingSupport {

      private final TableViewer viewer;
      private final CellEditor  editor;

      public ValueEditingSupport(TableViewer viewer) {
         super(viewer);
         this.viewer = viewer;
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
         gv.setConstantValue(String.valueOf(userInputValue));
         viewer.update(element, null);
      }
   }

}
