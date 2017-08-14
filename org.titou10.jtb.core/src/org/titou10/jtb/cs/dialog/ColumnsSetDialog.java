/*
 * Copyright (C) 2015-2017 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.cs.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.cs.ColumnSystemHeader;
import org.titou10.jtb.cs.ColumnsSetsManager;
import org.titou10.jtb.cs.gen.Column;
import org.titou10.jtb.cs.gen.ColumnKind;
import org.titou10.jtb.cs.gen.ColumnsSet;
import org.titou10.jtb.cs.gen.UserProperty;
import org.titou10.jtb.cs.gen.UserPropertyType;
import org.titou10.jtb.ui.dnd.DNDData;
import org.titou10.jtb.ui.dnd.TransferColumn;
import org.titou10.jtb.util.Utils;

/**
 * 
 * Manage the columns of a Columns Set
 * 
 * @author Denis Forveille
 *
 */
public class ColumnsSetDialog extends Dialog {

   private static final Logger  log           = LoggerFactory.getLogger(ColumnsSetDialog.class);

   private static final String  DUPLICATE_MSG = "A column with the same name is already present";

   private Shell                shell;
   private ColumnsSetsManager   csManager;
   private ColumnsSet           columnsSet;

   private Table                table;
   private List<Column>         columns       = new ArrayList<>();

   private Map<Integer, Button> buttons       = new HashMap<>();

   public ColumnsSetDialog(Shell parentShell, ColumnsSetsManager csManager, ColumnsSet columnsSet) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);

      this.shell = parentShell;
      this.csManager = csManager;
      this.columnsSet = columnsSet;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Manage Columns Set columns");
   }

   @Override
   protected Point getInitialSize() {
      return new Point(600, 600);
   }

   @Override
   protected Control createDialogArea(Composite parent) {

      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(3, false));

      // JMS Headers
      Group gSystemHeader = new Group(container, SWT.SHADOW_ETCHED_IN);
      gSystemHeader.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 3, 1));
      gSystemHeader.setText("JMS/System Headers:");
      gSystemHeader.setLayout(new GridLayout(3, false));

      Label label1 = new Label(gSystemHeader, SWT.RIGHT);
      label1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      label1.setText("JMS/System Headers:");

      final ComboViewer comboCS = new ComboViewer(gSystemHeader, SWT.READ_ONLY);
      comboCS.setContentProvider(ArrayContentProvider.getInstance());
      comboCS.getCombo().setToolTipText("JMS System Header");
      comboCS.setLabelProvider(new LabelProvider() {
         @Override
         public String getText(Object element) {
            ColumnSystemHeader csh = (ColumnSystemHeader) element;
            return csh.getHeaderName();
         }
      });

      Button btnAddSystem = new Button(gSystemHeader, SWT.NONE);
      btnAddSystem.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      btnAddSystem.setText("Add");

      // User Property

      Group gUserProperty = new Group(container, SWT.SHADOW_ETCHED_IN);
      gUserProperty.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
      gUserProperty.setText("User Property");
      gUserProperty.setLayout(new GridLayout(2, false));

      Label label2 = new Label(gUserProperty, SWT.RIGHT);
      label2.setText("Property Name: ");

      Text newUserPropertyName = new Text(gUserProperty, SWT.BORDER);
      newUserPropertyName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      newUserPropertyName.setToolTipText("Name of the JMS Messae property");

      Label label3 = new Label(gUserProperty, SWT.RIGHT);
      label3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
      label3.setText("Display Text: ");

      Text newUserPropertyDisplay = new Text(gUserProperty, SWT.BORDER);
      newUserPropertyDisplay.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      newUserPropertyDisplay.setToolTipText("Column header text");

      Label label4 = new Label(gUserProperty, SWT.RIGHT);
      label4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
      label4.setText("Display width: ");

      Spinner displayWidth = new Spinner(gUserProperty, SWT.BORDER);
      displayWidth.setMinimum(20);
      displayWidth.setMaximum(400);
      displayWidth.setIncrement(5);
      displayWidth.setPageIncrement(10);
      displayWidth.setTextLimit(3);
      displayWidth.setSelection(100);
      displayWidth.setToolTipText("Width in pixel of the column");

      Label label5 = new Label(gUserProperty, SWT.RIGHT);
      label5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
      label5.setText("Type: ");

      final ComboViewer comboUserPropertyType = new ComboViewer(gUserProperty, SWT.READ_ONLY);
      comboUserPropertyType.getCombo().setLayoutData(new GridData(SWT.LEFT, SWT.RIGHT, false, false, 1, 1));
      comboUserPropertyType.setContentProvider(ArrayContentProvider.getInstance());
      comboUserPropertyType.getCombo().setToolTipText("How to display the value");
      comboUserPropertyType.setLabelProvider(new LabelProvider() {
         @Override
         public String getText(Object element) {
            UserPropertyType upt = (UserPropertyType) element;
            return upt.name();
         }
      });
      comboUserPropertyType.getCombo().setToolTipText("Value conversion (long to Timestamp, long to Date...");

      Button btnAddUser = new Button(gUserProperty, SWT.NONE);
      btnAddUser.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
      btnAddUser.setText("Add / Modify");

      // Table with Values

      Label lblValues = new Label(container, SWT.NONE);
      lblValues.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
      lblValues.setText("Columns:");

      Composite compositeList = new Composite(container, SWT.NONE);
      compositeList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
      TableColumnLayout tcListComposite = new TableColumnLayout();
      compositeList.setLayout(tcListComposite);

      final TableViewer tableViewer = new TableViewer(compositeList, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            Column c = (Column) event.getStructuredSelection().getFirstElement();
            if ((c != null) && (c.getColumnKind() == ColumnKind.USER_PROPERTY)) {
               UserProperty up = c.getUserProperty();
               newUserPropertyName.setText(up.getUserPropertyName());
               newUserPropertyDisplay.setText(up.getDisplayName());
               displayWidth.setSelection(up.getDisplayWidth());
               comboUserPropertyType.setSelection(new StructuredSelection(up.getType()));
            }
         }
      });
      table = tableViewer.getTable();
      table.setHeaderVisible(true);
      table.setLinesVisible(true);

      int operations = DND.DROP_MOVE;
      Transfer[] transferTypes = new Transfer[] { TransferColumn.getInstance() };
      tableViewer.addDragSupport(operations, transferTypes, new ColumnDragListener(tableViewer));
      tableViewer.addDropSupport(operations, transferTypes, new ColumnDropListener(shell, tableViewer));

      TableViewerColumn systemViewerColumn = new TableViewerColumn(tableViewer, SWT.CENTER | SWT.LEAD);
      TableColumn systemColumn = systemViewerColumn.getColumn();
      tcListComposite.setColumnData(systemColumn, new ColumnPixelData(16, false));
      systemColumn.setResizable(false); // resizable attribute of ColumnPixelData is not functionnal...
      systemViewerColumn.setLabelProvider(new ColumnLabelProvider() {
         // Manage the remove icon
         @Override
         public void update(ViewerCell cell) {
            Column value = (Column) cell.getElement();

            // Do not recreate buttons if already built
            if (buttons.containsKey(value.hashCode()) && !buttons.get(value.hashCode()).isDisposed()) {
               return;
            }

            Composite parentComposite = (Composite) cell.getViewerRow().getControl();
            Color cellColor = cell.getBackground();
            Image image = SWTResourceManager.getImage(this.getClass(), "icons/delete.png");

            Button btnRemove = new Button(parentComposite, SWT.NONE);
            btnRemove.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
               log.debug("Remove value '{}'", value);
               columns.remove(value);
               clearButtonCache();
               tableViewer.refresh();
            }));

            btnRemove.addPaintListener(event -> SWTResourceManager.drawCenteredImage(event, cellColor, image));

            TableItem item = (TableItem) cell.getItem();

            TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            editor.setEditor(btnRemove, item, cell.getColumnIndex());
            editor.layout();

            buttons.put(value.hashCode(), btnRemove);
         }
      });

      TableViewerColumn nameViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
      TableColumn nameColumn = nameViewerColumn.getColumn();
      tcListComposite.setColumnData(nameColumn, new ColumnWeightData(4, 100, true));
      nameColumn.setText("Name");
      nameViewerColumn.setLabelProvider(new ColumnLabelProvider() {

         @Override
         public String getText(Object element) {
            Column c = (Column) element;
            return c.getColumnKind() == ColumnKind.SYSTEM_HEADER ? c.getSystemHeaderName()
                     : c.getUserProperty().getUserPropertyName();
         }
      });

      TableViewerColumn displayViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
      TableColumn displayColumn = displayViewerColumn.getColumn();
      tcListComposite.setColumnData(displayColumn, new ColumnWeightData(4, 100, true));
      displayColumn.setText("Display");
      displayViewerColumn.setLabelProvider(new ColumnLabelProvider() {

         @Override
         public String getText(Object element) {
            Column c = (Column) element;
            return c.getColumnKind() == ColumnKind.SYSTEM_HEADER
                     ? ColumnSystemHeader.fromHeaderName(c.getSystemHeaderName()).getDisplayName()
                     : c.getUserProperty().getDisplayName();
         }
      });

      TableViewerColumn widthViewerColumn = new TableViewerColumn(tableViewer, SWT.RIGHT);
      TableColumn widthColumn = widthViewerColumn.getColumn();
      tcListComposite.setColumnData(widthColumn, new ColumnWeightData(1, 30, true));
      widthColumn.setText("Width");
      widthViewerColumn.setLabelProvider(new ColumnLabelProvider() {

         @Override
         public String getText(Object element) {
            Column c = (Column) element;
            int w;
            if (c.getColumnKind() == ColumnKind.SYSTEM_HEADER) {
               w = ColumnSystemHeader.fromHeaderName(c.getSystemHeaderName()).getDisplayWidth();
            } else {
               w = c.getUserProperty().getDisplayWidth();
            }
            return String.valueOf(w);
         }
      });

      TableViewerColumn kindViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
      TableColumn kindColumn = kindViewerColumn.getColumn();
      tcListComposite.setColumnData(kindColumn, new ColumnWeightData(1, 30, true));
      kindColumn.setText("Kind");
      kindViewerColumn.setLabelProvider(new ColumnLabelProvider() {

         @Override
         public String getText(Object element) {
            Column c = (Column) element;
            if (c.getColumnKind() == ColumnKind.SYSTEM_HEADER) {
               return "JMS";
            } else {
               return "User (" + c.getUserProperty().getType().name() + ")";
            }
         }
      });

      // Add a System Header to the list of values
      btnAddSystem.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         ColumnSystemHeader csh = (ColumnSystemHeader) comboCS.getStructuredSelection().getFirstElement();
         log.debug("Adding ColumnSystemHeader {} to the list", csh);

         // Check if this System Header is already present in the Columns Set
         for (Column c : columns) {
            if (c.getColumnKind() == ColumnKind.SYSTEM_HEADER) {
               if (c.getSystemHeaderName().equals(csh.getHeaderName())) {
                  MessageDialog.openError(getShell(), "Error", DUPLICATE_MSG);
                  return;
               }
            }
         }

         columns.add(csManager.buildSystemColumn(csh));

         clearButtonCache();
         tableViewer.refresh();
         compositeList.layout();
         Utils.resizeTableViewer(tableViewer);
      }));

      // Add a User Property to the list of values
      btnAddUser.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         String userPropertyName = newUserPropertyName.getText();

         int indexOldColumn = -1;

         for (Column c : columns) {
            if (c.getColumnKind() == ColumnKind.USER_PROPERTY) {
               if (c.getUserProperty().getUserPropertyName().equals(userPropertyName)) {
                  indexOldColumn = columns.indexOf(c);
               }
            }
         }

         String userPropertyDisplay = newUserPropertyDisplay.getText();
         int width = displayWidth.getSelection();
         UserPropertyType upt = (UserPropertyType) comboUserPropertyType.getStructuredSelection().getFirstElement();

         if (Utils.isEmpty(userPropertyName)) {
            MessageDialog.openError(getShell(), "Error", "The property name is required");
            return;
         }
         if (Utils.isEmpty(userPropertyDisplay)) {
            MessageDialog.openError(getShell(), "Error", "The property display name is required");
            return;
         }

         log.debug("Adding User Property '{}' to the list", userPropertyName);

         Column newColumn = csManager.buildUserPropertyColumn(userPropertyName, userPropertyDisplay, width, upt);
         if (indexOldColumn == -1) {
            columns.add(newColumn);
         } else {
            columns.set(indexOldColumn, newColumn);
         }

         clearButtonCache();
         tableViewer.refresh();
         compositeList.layout();
         Utils.resizeTableViewer(tableViewer);
      }));

      table.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
         if (e.keyCode == SWT.DEL) {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            if (selection.isEmpty()) {
               return;
            }
            for (Object item : selection.toList()) {
               log.debug("Remove {} from the list", item);
               columns.remove(item);
            }
            clearButtonCache();
            tableViewer.refresh();
            compositeList.layout();
            Utils.resizeTableViewer(tableViewer);
         }
      }));

      if (columnsSet != null) {
         columns = columnsSet.getColumn();
      }

      // Populate fields
      comboCS.setInput(ColumnSystemHeader.values());
      comboCS.setSelection(new StructuredSelection(ColumnSystemHeader.JMS_CORRELATION_ID), true);
      comboUserPropertyType.setInput(UserPropertyType.values());
      comboUserPropertyType.setSelection(new StructuredSelection(UserPropertyType.STRING), true);
      tableViewer.setContentProvider(ArrayContentProvider.getInstance());
      tableViewer.setInput(columns);

      compositeList.layout();
      Utils.resizeTableViewer(tableViewer);

      return container;
   }

   @Override
   protected void okPressed() {

      if (columns.isEmpty()) {
         MessageDialog.openError(getShell(), "Error", "A Columns Set requires at least one column");
         return;
      }

      super.okPressed();
   }

   private void clearButtonCache() {
      for (Button b : buttons.values()) {
         b.dispose();
      }
      buttons.clear();
   }

   // -----------------------
   // Providers and Listeners
   // -----------------------

   private class ColumnDragListener extends DragSourceAdapter {
      private TableViewer tableViewer;

      public ColumnDragListener(TableViewer tableViewer) {
         this.tableViewer = tableViewer;
      }

      @Override
      public void dragStart(DragSourceEvent event) {
         log.debug("Start Drag");

         // Only allow one column at a time (for now...)
         IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
         if ((selection == null) || (selection.size() != 1)) {
            event.doit = false;
            return;
         }

         DNDData.dragColumn((Column) selection.getFirstElement());
      }
   }

   private class ColumnDropListener extends ViewerDropAdapter {

      public ColumnDropListener(Shell shell, TableViewer tableViewer) {
         super(tableViewer);
      }

      @Override
      public void drop(DropTargetEvent event) {
         DNDData.dropOnColumn((Column) determineTarget(event));
         super.drop(event);
      }

      @Override
      public boolean performDrop(Object data) {

         Column target = DNDData.getTargetColumn();

         Column source = DNDData.getSourceColumn();
         Column currentTarget = (Column) getCurrentTarget();

         int n;
         if (currentTarget != null) {
            n = columns.indexOf(currentTarget);
            int loc = getCurrentLocation();
            if (loc == LOCATION_BEFORE) {
               n--;
            }
         } else {
            n = columns.indexOf(target);
         }

         if ((n < 0) || (n > columns.size())) {
            return false;
         }

         columns.remove(source);
         columns.add(n, source);

         // Refresh TableViewer
         getViewer().refresh();

         return true;
      }

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferData) {
         return TransferColumn.getInstance().isSupportedType(transferData);
      }
   }

   // ----------------
   // Standard Getters
   // ----------------

   public List<Column> getColumns() {
      return columns;
   }

}
