/*
 * Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.config.gen.Properties.Property;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.ui.UIProperty;

/**
 * Dialog for creating or updating a new JTBSession
 * 
 * @author Denis Forveille
 *
 */
public class SessionAddOrEditDialog extends Dialog {

   private ConfigManager          cm;
   private List<QManager>         queueManagers;
   private JTBSession             jtbSession;

   private QManager               queueManagerSelected;

   // Session data
   private String                 name;
   private Integer                port;
   private String                 host;
   private String                 userId;
   private String                 password;
   private String                 folder;

   final private List<UIProperty> properties = new ArrayList<>();

   // Widgets
   private Text                   txtName;
   private Text                   txtPort;
   private Text                   txtHost;
   private Text                   txtUserId;
   private Text                   txtPassword;
   private Text                   txtFolder;

   // JFace objects
   private TabFolder              tabFolder;
   private TabItem                tabSession;
   private TabItem                tabProperties;

   private Table                  propertyTable;
   private TableColumn            propertyNameColumn;
   private TableColumn            propertyValueColumn;

   /**
    * @wbp.parser.constructor
    */
   // @Inject
   // public SessionAddOrEditDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell parentShell, ConfigManager cm) {
   public SessionAddOrEditDialog(Shell parentShell, ConfigManager cm) {
      this(parentShell, cm, null);
   }

   // Editing a JTBSession
   public SessionAddOrEditDialog(Shell parentShell, ConfigManager cm, JTBSession jtbSession) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);

      this.cm = cm;
      this.queueManagers = cm.getRunningQManagers();
      this.jtbSession = jtbSession;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      if (jtbSession == null) {
         newShell.setText("Add Session");
      } else {
         newShell.setText("Update Session");
      }
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new FillLayout(SWT.HORIZONTAL));

      tabFolder = new TabFolder(container, SWT.NONE);

      // -----------------------------
      // Session Basic Information Tab
      // -----------------------------
      tabSession = new TabItem(tabFolder, SWT.NONE);
      tabSession.setText("Connection");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      tabSession.setControl(composite);
      composite.setLayout(new GridLayout(2, false));

      Label lblNewLabel_3 = new Label(composite, SWT.NONE);
      lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_3.setText("Queue Manager");

      ComboViewer comboViewer = new ComboViewer(composite, SWT.READ_ONLY);
      comboViewer.setContentProvider(ArrayContentProvider.getInstance());
      comboViewer.setLabelProvider(new MyQueueManagerLabelProvider());

      new Label(composite, SWT.NONE);
      new Label(composite, SWT.NONE);

      Label lblNewLabel_16 = new Label(composite, SWT.NONE);
      lblNewLabel_16.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_16.setText("Folder");

      txtFolder = new Text(composite, SWT.BORDER);
      txtFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      new Label(composite, SWT.NONE);
      new Label(composite, SWT.NONE);

      Label lblNewLabel_6 = new Label(composite, SWT.NONE);
      lblNewLabel_6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_6.setText("Session Name");

      txtName = new Text(composite, SWT.BORDER);
      txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      new Label(composite, SWT.NONE);
      new Label(composite, SWT.NONE);

      Label lblNewLabel_8 = new Label(composite, SWT.NONE);
      lblNewLabel_8.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_8.setText("Host");

      txtHost = new Text(composite, SWT.BORDER);
      txtHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Label lblNewLabel_4 = new Label(composite, SWT.NONE);
      lblNewLabel_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_4.setText("Port");

      GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
      gd.widthHint = 35;
      txtPort = new Text(composite, SWT.BORDER);
      txtPort.setLayoutData(gd);
      txtPort.setTextLimit(5);
      final Text txtPortFinal = txtPort;
      txtPort.addVerifyListener(new VerifyListener() {
         @Override
         public void verifyText(VerifyEvent e) {
            final String oldS = txtPortFinal.getText();
            final String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
            if (!newS.isEmpty()) {
               try {
                  new Long(newS);
               } catch (final NumberFormatException nfe) {
                  e.doit = false;
               }
            }
         }
      });

      Label lblNewLabel_5 = new Label(composite, SWT.NONE);
      lblNewLabel_5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_5.setText("Userid");

      txtUserId = new Text(composite, SWT.BORDER);
      txtUserId.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

      Label lblNewLabel_2 = new Label(composite, SWT.NONE);
      lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_2.setText("Password");

      txtPassword = new Text(composite, SWT.BORDER | SWT.PASSWORD);
      txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      // --------------
      // Properties Tab
      // --------------

      tabProperties = new TabItem(tabFolder, SWT.NONE);
      tabProperties.setText("Properties");

      final Composite composite_1 = new Composite(tabFolder, SWT.NONE);
      tabProperties.setControl(composite_1);
      composite_1.setLayout(new GridLayout(1, false));

      // Properties TableViewer
      Composite composite_4 = new Composite(composite_1, SWT.NONE);
      composite_4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      composite_4.setBounds(0, 0, 64, 64);

      TableColumnLayout tcl_composite_4 = new TableColumnLayout();
      composite_4.setLayout(tcl_composite_4);

      final TableViewer tableViewer = new TableViewer(composite_4, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      propertyTable = tableViewer.getTable();
      propertyTable.setHeaderVisible(true);
      propertyTable.setLinesVisible(true);
      ColumnViewerToolTipSupport.enableFor(tableViewer);

      TableViewerColumn propertyRequiredColumnViewer = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn propertyRequiredColumn = propertyRequiredColumnViewer.getColumn();
      propertyRequiredColumn.setAlignment(SWT.CENTER);
      tcl_composite_4.setColumnData(propertyRequiredColumn, new ColumnPixelData(12, true, true));
      propertyRequiredColumnViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            UIProperty u = (UIProperty) element;
            return u.isRequired() ? "*" : "";
         }
      });

      TableViewerColumn propertyNameColumnViewer = new TableViewerColumn(tableViewer, SWT.NONE);
      propertyNameColumn = propertyNameColumnViewer.getColumn();
      propertyNameColumn.setAlignment(SWT.LEFT);
      tcl_composite_4.setColumnData(propertyNameColumn, new ColumnPixelData(150, true, true));
      propertyNameColumn.setText("Name");
      propertyNameColumnViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            UIProperty u = (UIProperty) element;
            return u.getName();
         }

         @Override
         public String getToolTipText(Object element) {
            UIProperty u = (UIProperty) element;
            return u.getToolTip();
         }
      });

      TableViewerColumn propertyKindColumnViewer = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn propertyKindColumn = propertyKindColumnViewer.getColumn();
      propertyKindColumn.setAlignment(SWT.LEFT);
      tcl_composite_4.setColumnData(propertyKindColumn, new ColumnPixelData(80, true, true));
      propertyKindColumn.setText("Kind");
      // propertyKindColumnViewer.setEditingSupport(new NameValueDeleteSupport(tableViewer, properties));
      propertyKindColumnViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            UIProperty u = (UIProperty) element;
            return u.getKind().name();
         }
      });

      TableViewerColumn propertyValueColumnViewer = new TableViewerColumn(tableViewer, SWT.NONE);
      propertyValueColumn = propertyValueColumnViewer.getColumn();
      propertyValueColumn.setAlignment(SWT.LEFT);
      tcl_composite_4.setColumnData(propertyValueColumn, new ColumnPixelData(500, true, true));
      propertyValueColumn.setText("Value");
      propertyValueColumnViewer.setEditingSupport(new ValueEditingSupport(tableViewer));
      propertyValueColumnViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            UIProperty u = (UIProperty) element;
            return u.getValue();
         }

         @Override
         public String getToolTipText(Object element) {
            UIProperty u = (UIProperty) element;
            return u.getToolTip();
         }
      });

      tableViewer.setContentProvider(ArrayContentProvider.getInstance());

      // ----------
      // Set values
      // ----------

      comboViewer.setInput(queueManagers);
      // newPropertyKindCombo.setItems(JMSPropertyKind.NAMES);

      if (jtbSession == null) {
         queueManagerSelected = queueManagers.get(0);
      } else {
         queueManagerSelected = jtbSession.getQm();

         SessionDef sessionDef = jtbSession.getSessionDef();
         txtHost.setText(sessionDef.getHost());
         txtName.setText(sessionDef.getName());
         txtPort.setText(String.valueOf(sessionDef.getPort()));
         if (sessionDef.getUserid() != null) {
            txtUserId.setText(sessionDef.getUserid());
         }
         if (sessionDef.getPassword() != null) {
            txtPassword.setText(sessionDef.getPassword());
         }
         if (sessionDef.getFolder() != null) {
            txtFolder.setText(sessionDef.getFolder());
         }
      }

      populateProperties();

      ISelection selection = new StructuredSelection(queueManagerSelected);
      comboViewer.setSelection(selection);

      tableViewer.setInput(properties);

      // --------
      // Behavior
      // --------

      // Save the selected QueueManager
      comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            queueManagerSelected = (QManager) sel.getFirstElement();

            populateProperties();
            tableViewer.setInput(properties);

            tableViewer.refresh();

            propertyNameColumn.pack();
            propertyValueColumn.pack();

         }
      });

      propertyNameColumn.pack();
      propertyValueColumn.pack();

      txtName.setFocus();

      return container;
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      if (jtbSession == null) {
         Button btnCancel = createButton(parent, IDialogConstants.OK_ID, "Create", true);
         btnCancel.setText("Create");
      } else {
         Button btnCancel = createButton(parent, IDialogConstants.OK_ID, "Update", true);
         btnCancel.setText("Update");
      }
      Button button = createButton(parent, IDialogConstants.CANCEL_ID, "Done", false);
      button.setText("Cancel");
   }

   @Override
   protected Control createButtonBar(final Composite parent) {
      Composite buttonBar = new Composite(parent, SWT.NONE);

      GridLayout layout = new GridLayout(3, false);
      layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
      layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
      layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
      layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
      buttonBar.setLayout(layout);

      GridData data = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
      buttonBar.setLayoutData(data);
      buttonBar.setFont(parent.getFont());

      // Help Button
      Button help = new Button(buttonBar, SWT.PUSH);
      help.setImage(SWTResourceManager.getImage(this.getClass(), "icons/help.png"));
      help.setToolTipText("Help");
      help.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent event) {
            QManagerHelpDialog helpDialog = new QManagerHelpDialog(getShell(), queueManagerSelected.getHelpText());
            helpDialog.open();
         }
      });

      final GridData leftButtonData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
      leftButtonData.grabExcessHorizontalSpace = true;
      leftButtonData.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
      help.setLayoutData(leftButtonData);
      if (queueManagerSelected.getHelpText() == null) {
         help.setEnabled(false);
      }

      // Other buttons on the right
      final Control buttonControl = super.createButtonBar(buttonBar);
      buttonControl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

      return buttonBar;
   }

   @Override
   protected Point getInitialSize() {
      return new Point(900, 600);
   }

   @Override
   protected void okPressed() {

      // Session Name
      if (txtName.getText().trim().isEmpty()) {
         tabFolder.setSelection(tabSession);
         txtName.setFocus();
         MessageDialog.openError(getShell(), "Validation error", "The session name is mandatory");
         return;
      } else {
         name = txtName.getText().trim();
      }

      // Check duplicate name when adding
      boolean duplicate = false;
      if (jtbSession == null) {
         // Adding a session
         if (cm.getSessionDefByName(name) != null) {
            duplicate = true;
         }
      } else {
         // Updating a session
         if (!(jtbSession.getName().equals(name))) {
            if (cm.getSessionDefByName(name) != null) {
               duplicate = true;
            }
         }
      }

      if (duplicate) {
         txtName.setFocus();
         MessageDialog.openError(getShell(), "Validation error", "A session with this name already exists");
         return;
      }

      // Folder
      if (!(txtFolder.getText().trim().isEmpty())) {
         folder = txtFolder.getText().trim();
      }

      // Host Name
      if (txtHost.getText().isEmpty()) {
         tabFolder.setSelection(tabSession);
         txtHost.setFocus();
         MessageDialog.openError(getShell(), "Validation error", "The host name is mandatory");
         return;
      } else {
         host = txtHost.getText().trim();
      }

      // Port
      if (txtPort.getText().isEmpty()) {
         tabFolder.setSelection(tabSession);
         txtPort.setFocus();
         MessageDialog.openError(getShell(), "Validation error", "The port is mandatory");
         return;
      } else {
         port = Integer.valueOf(txtPort.getText());
      }

      // UserId
      if (!(txtUserId.getText().trim().isEmpty())) {
         userId = txtUserId.getText().trim();
      }

      // Password
      if (!(txtPassword.getText().trim().isEmpty())) {
         password = txtPassword.getText().trim();
      }

      // Validate properties
      for (UIProperty property : properties) {

         String name = property.getName().trim();
         String value = property.getValue();

         // Mandatory parameters
         if ((property.isRequired()) && ((value == null) || (value.trim().isEmpty()))) {
            tabFolder.setSelection(tabProperties);
            // newPropertyName.setFocus();
            MessageDialog.openError(getShell(),
                                    "Validation error",
                                    "Property '" + property.getName() + "' is mandatory for this Queue manager");
            return;
         }

         // Check kind of parameter
         boolean ok = JMSPropertyKind.validateValue(property.getKind(), value);
         if (!ok) {
            tabFolder.setSelection(tabProperties);
            // newPropertyName.setFocus();
            MessageDialog.openError(getShell(),
                                    "Validation error",
                                    "Property '" + name + "' must be of kind '" + property.getKind() + "'");
            return;
         }
      }

      super.okPressed();
   }

   // -------
   // Helpers
   // -------

   private void populateProperties() {

      properties.clear();

      if (queueManagerSelected.getQManagerProperties() == null) {
         return;
      }

      for (QManagerProperty qmProperty : queueManagerSelected.getQManagerProperties()) {
         properties.add(new UIProperty(qmProperty));
      }

      if (jtbSession == null) {
         return;
      }
      SessionDef sessionDef = jtbSession.getSessionDef();
      if (sessionDef.getProperties() != null) {
         if (sessionDef.getProperties().getProperty() != null) {
            // For each property of the session
            for (Property property : sessionDef.getProperties().getProperty()) {
               // if it exist in the QM, add the value, drop the other
               for (UIProperty uiProperty : properties) {
                  if (uiProperty.getName().equals(property.getName())) {
                     uiProperty.setValue(property.getValue());
                     break;
                  }
               }
            }
         }
      }
      // Sort properties: required first, then populated
      Collections.sort(properties);
   }

   public class ValueEditingSupport extends EditingSupport {

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
         String s = ((UIProperty) element).getValue();
         if (s == null) {
            return "";
         } else {
            return s;
         }
      }

      @Override
      protected void setValue(Object element, Object userInputValue) {
         ((UIProperty) element).setValue(String.valueOf(userInputValue));
         viewer.update(element, null);
      }
   }

   private class MyQueueManagerLabelProvider extends LabelProvider {
      @Override
      public String getText(Object element) {
         return ((QManager) element).getName();
      }
   }

   // ----------------
   // Standard Getters
   // ----------------

   public String getName() {
      return name;
   }

   public String getFolder() {
      return folder;
   }

   public Integer getPort() {
      return port;
   }

   public String getHost() {
      return host;
   }

   public String getUserId() {
      return userId;
   }

   public String getPassword() {
      return password;
   }

   public QManager getQueueManagerSelected() {
      return queueManagerSelected;
   }

   public List<UIProperty> getProperties() {
      return properties;
   }

}
