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
package org.titou10.jtb.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.script.ScriptsUtils;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Step;
import org.titou10.jtb.ui.UINameValue;
import org.titou10.jtb.util.Utils;

/**
 * Dialog for creating or editing a script
 * 
 * @author Denis Forveille
 *
 */
public class ScriptsAddOrEditDialog extends Dialog {

   private static final Logger log = LoggerFactory.getLogger(MessageDialogAbstract.class);

   private static final String PROPERTY_NAME_INVALID  = "Property '%s' is not a valid JMS property identifier";
   private static final String PROPERTY_ALREADY_EXIST = "A property with name '%s' is already defined";

   // Business data
   private ConfigManager cm;
   private Script        script;
   private String        scriptName;

   // JTBMessage data
   private List<UINameValue> userProperties;

   // Message common Widgets
   private Text   txtScriptName;
   private Button btnPromptVariables;

   // TableViewers
   private TableViewer tvSteps;
   private TableViewer tvGlobalVariables;

   // ------------
   // Constructor
   // ------------

   public ScriptsAddOrEditDialog(Shell parentShell, ConfigManager cm, Script script) {
      super(parentShell);
      this.script = script;
      this.scriptName = script.getName();
   }

   // -----------
   // Dialog stuff
   // -----------

   @Override
   protected Control createDialogArea(Composite parent) {
      final Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new FillLayout(SWT.HORIZONTAL));

      TabFolder tabFolder = new TabFolder(container, SWT.NONE);

      // ------------------
      // General Tab
      // ------------------
      TabItem tbtmGeneral = new TabItem(tabFolder, SWT.NONE);
      tbtmGeneral.setText("General");

      Composite composite1 = new Composite(tabFolder, SWT.NONE);
      tbtmGeneral.setControl(composite1);
      composite1.setLayout(new GridLayout(1, false));

      createSteps(composite1);

      // --------------------
      // Global Variables Tab
      // --------------------

      TabItem tbtmGlobalVariables = new TabItem(tabFolder, SWT.NONE);
      tbtmGlobalVariables.setText("Global Variables");

      Composite composite2 = new Composite(tabFolder, SWT.NONE);
      tbtmGlobalVariables.setControl(composite2);
      composite2.setLayout(new GridLayout(1, false));

      createGlobalVariables(composite2);

      // --------------------
      // Execution Tab
      // --------------------

      TabItem tbtmExecution = new TabItem(tabFolder, SWT.NONE);
      tbtmExecution.setText("Execution");

      Composite composite3 = new Composite(tabFolder, SWT.NONE);
      tbtmExecution.setControl(composite3);
      composite3.setLayout(new GridLayout(1, false));

      createGlobalVariables(composite3);

      // ---------------
      // Dialog Shortcuts
      // ----------------

      getShell().getDisplay().addFilter(SWT.KeyUp, new Listener() {
         @Override
         public void handleEvent(Event e) {
            // Fast fail
            if (e.keyCode != 's') {
               return;
            }
            if (e.widget instanceof Control && isChild(container, (Control) e.widget)) {
               if ((e.stateMask & SWT.CTRL) != 0) {
                  log.debug("CTRL-S pressed");
                  buttonPressed(MessageEditDialog.BUTTON_SAVE_TEMPLATE);
                  buttonPressed(IDialogConstants.OK_ID);
                  return;
               }
            }
         }
      });

      // --------------
      // Initialize data
      // --------------

      populateFields();
      // enableDisableControls();

      return container;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText(getDialogTitle());
   }

   @Override
   protected Point getInitialSize() {
      return new Point(800, 600);
   }

   @Override
   protected void okPressed() {
      // updateTemplate();
      super.okPressed();
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

   private void populateFields() {

      if (script.getName() != null) {
         txtScriptName.setText(script.getName());
      }

      if (script.isPromptVariables() != null) {
         btnPromptVariables.setSelection(script.isPromptVariables());
      }

      tvSteps.setInput(script.getStep());
   }

   private void createSteps(final Composite parentComposite) {

      // Header lines
      Composite compositeHeader = new Composite(parentComposite, SWT.NONE);
      compositeHeader.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      compositeHeader.setLayout(new GridLayout(2, false));

      Label lblNewLabel1 = new Label(compositeHeader, SWT.NONE);
      lblNewLabel1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel1.setText("Name");

      txtScriptName = new Text(compositeHeader, SWT.BORDER);
      txtScriptName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Label lblNewLabel2 = new Label(compositeHeader, SWT.NONE);
      lblNewLabel2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel2.setText("Prompt for variables?");

      btnPromptVariables = new Button(compositeHeader, SWT.CHECK);

      Composite composite = new Composite(compositeHeader, SWT.NONE);
      composite.setLayout(new RowLayout(SWT.HORIZONTAL));
      composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));

      Button btnAddStep = new Button(composite, SWT.NONE);
      btnAddStep.setText("Add a new Step");
      btnAddStep.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            ScriptsNewStepDialog d1 = new ScriptsNewStepDialog(getShell());
            if (d1.open() != Window.OK) {
               return;
            }
            Step s = ScriptsUtils.buildStep(d1.getTemplateName(),
                                            d1.getSessionName(),
                                            d1.getDestinationName(),
                                            d1.getDelay(),
                                            d1.getIterations());
            script.getStep().add(s);
            tvSteps.add(s);
            parentComposite.layout();
         }
      });

      Button btnAddPause = new Button(composite, SWT.NONE);
      btnAddPause.setText("Add a Pause");
      btnAddPause.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            ScriptsNewPauseDialog d1 = new ScriptsNewPauseDialog(getShell());
            if (d1.open() != Window.OK) {
               return;
            }
            log.debug("delay : {} seconds", d1.getDelay());
            Step s = ScriptsUtils.buildPauseStep(d1.getDelay());
            script.getStep().add(s);
            tvSteps.add(s);
            parentComposite.layout();
         }
      });

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
      });

      TableViewerColumn stepSessionNameColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      // stepSessionNameColumn.setEditingSupport(new
      // ValueEditingSupport(tableViewer));
      TableColumn stepSessionNameHeader = stepSessionNameColumn.getColumn();
      tcl.setColumnData(stepSessionNameHeader, new ColumnWeightData(3, ColumnWeightData.MINIMUM_WIDTH, true));
      stepSessionNameHeader.setText("Session");
      stepSessionNameColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            Step s = (Step) element;
            return s.getSessionName();
         }
      });

      TableViewerColumn stepDestinationNameColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      // stepDestinationNameColumn.setEditingSupport(new
      // ValueEditingSupport(tableViewer));
      TableColumn stepDestinationNameHeader = stepDestinationNameColumn.getColumn();
      tcl.setColumnData(stepDestinationNameHeader, new ColumnWeightData(3, ColumnWeightData.MINIMUM_WIDTH, true));
      stepDestinationNameHeader.setText("Destination");
      stepDestinationNameColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            Step s = (Step) element;
            return s.getDestinationName();
         }
      });

      TableViewerColumn stepPauseSecsColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      // stepDestinationNameColumn.setEditingSupport(new
      // ValueEditingSupport(tableViewer));
      TableColumn stepPauseSecsHeader = stepPauseSecsColumn.getColumn();
      tcl.setColumnData(stepPauseSecsHeader, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
      stepPauseSecsHeader.setText("Pause (s)");
      stepPauseSecsColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            Step s = (Step) element;
            if (s.getPauseSecsAfter() != null) {
               return s.getPauseSecsAfter().toString();
            } else {
               return null;
            }
         }
      });

      TableViewerColumn stepIterationsColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      // stepDestinationNameColumn.setEditingSupport(new
      // ValueEditingSupport(tableViewer));
      TableColumn stepIterationsHeader = stepIterationsColumn.getColumn();
      tcl.setColumnData(stepIterationsHeader, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
      stepIterationsHeader.setText("Iterations");
      stepIterationsColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            Step s = (Step) element;
            return String.valueOf(s.getIterations());
         }
      });

      TableViewerColumn actionsColumn = new TableViewerColumn(stepTableViewer, SWT.NONE);
      TableColumn actionsHeader = actionsColumn.getColumn();
      tcl.setColumnData(actionsHeader, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
      actionsHeader.setText("<->");
      actionsColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            return "";
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

   private void createGlobalVariables(final Composite parentComposite) {

      // Header
      Composite compositeHeader = new Composite(parentComposite, SWT.NONE);
      compositeHeader.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      compositeHeader.setBounds(0, 0, 154, 33);
      compositeHeader.setLayout(new GridLayout(3, false));

      Label lblNewLabel = new Label(compositeHeader, SWT.NONE);
      lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      lblNewLabel.setAlignment(SWT.CENTER);
      lblNewLabel.setText("Name");

      Label lblNewLabel2 = new Label(compositeHeader, SWT.NONE);
      lblNewLabel2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      lblNewLabel2.setAlignment(SWT.CENTER);
      lblNewLabel2.setText("Value");

      Label lblNewLabel1 = new Label(compositeHeader, SWT.NONE);
      lblNewLabel1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));

      final Text newPropertyName = new Text(compositeHeader, SWT.BORDER);
      newPropertyName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      final Text newPropertyValue = new Text(compositeHeader, SWT.BORDER);
      newPropertyValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnAddProperty = new Button(compositeHeader, SWT.NONE);
      btnAddProperty.setText("Add");

      // Variables
      Composite compositeVariables = new Composite(parentComposite, SWT.NONE);
      compositeVariables.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      compositeVariables.setBounds(0, 0, 64, 64);
      TableColumnLayout tcl_composite_4 = new TableColumnLayout();
      compositeVariables.setLayout(tcl_composite_4);

      final TableViewer tableViewer = new TableViewer(compositeVariables, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      final Table propertyTable = tableViewer.getTable();
      propertyTable.setHeaderVisible(true);
      propertyTable.setLinesVisible(true);

      TableViewerColumn propertyNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn propertyNameHeader = propertyNameColumn.getColumn();
      tcl_composite_4.setColumnData(propertyNameHeader, new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH, true));
      propertyNameHeader.setAlignment(SWT.CENTER);
      propertyNameHeader.setText("Name");
      propertyNameColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            UINameValue u = (UINameValue) element;
            return u.getName();
         }
      });

      TableViewerColumn propertyValueColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      propertyValueColumn.setEditingSupport(new ValueEditingSupport(tableViewer));
      TableColumn propertyValueHeader = propertyValueColumn.getColumn();
      tcl_composite_4.setColumnData(propertyValueHeader, new ColumnWeightData(3, ColumnWeightData.MINIMUM_WIDTH, true));
      propertyValueHeader.setText("Value");
      propertyValueColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            UINameValue u = (UINameValue) element;
            return u.getValue();
         }
      });

      tableViewer.setContentProvider(ArrayContentProvider.getInstance());

      // Add a new Property
      btnAddProperty.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            String name = newPropertyName.getText().trim();
            if (name.length() == 0) {
               return;
            }
            if (newPropertyValue.getText().trim().length() == 0) {
               return;
            }

            // Validate that a property with the same name does not exit
            for (UINameValue unv : userProperties) {
               if (unv.getName().equals(name)) {
                  MessageDialog.openError(getShell(), "Validation error", String.format(PROPERTY_ALREADY_EXIST, name));
                  return;
               }
            }

            // Validate that the property name is a valid JMS property name
            if (Utils.isValidJMSPropertyName(name)) {
               UINameValue h = new UINameValue(name, newPropertyValue.getText().trim());
               userProperties.add(h);
               tableViewer.add(h);
               parentComposite.layout();
            } else {
               MessageDialog.openError(getShell(), "Validation error", String.format(PROPERTY_NAME_INVALID, name));
               return;
            }
         }
      });

      // Remove a property from the list
      propertyTable.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.keyCode == SWT.DEL) {
               IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
               if (selection.isEmpty()) {
                  return;
               }
               for (Object sel : selection.toList()) {
                  UINameValue h = (UINameValue) sel;
                  log.debug("Remove {} from the list", h);
                  userProperties.remove(h);
                  tableViewer.remove(h);
               }

               // propertyNameColumn.pack();
               // propertyValueColumn.pack();
               propertyTable.pack();

               parentComposite.layout();
            }
         }
      });

      tvGlobalVariables = tableViewer;

   }

   // ----------------
   // Business Methods
   // ----------------

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.OK_ID, "Execute", false);
      createButton(parent, IDialogConstants.OK_ID, "Save Script", false);
      createButton(parent, IDialogConstants.CANCEL_ID, "Close", true);
   }

   public String getDialogTitle() {
      if (scriptName == null) {
         return "Add a new Script";
      } else {
         return "Edit Script : '" + scriptName + "'";
      }
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
         String s = ((UINameValue) element).getValue();
         if (s == null) {
            return "";
         } else {
            return s;
         }
      }

      @Override
      protected void setValue(Object element, Object userInputValue) {
         ((UINameValue) element).setValue(String.valueOf(userInputValue));
         viewer.update(element, null);
      }
   }

}
