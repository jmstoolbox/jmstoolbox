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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.cs.ColumnsSetsManager;
import org.titou10.jtb.cs.gen.ColumnsSet;
import org.titou10.jtb.util.Utils;

/**
 *
 * Manage the Columns Sets
 *
 * @author Denis Forveille
 *
 */
public class ColumnsSetsManagerDialog extends Dialog {

   private static final Logger log       = LoggerFactory.getLogger(ColumnsSetsManagerDialog.class);

   private static final Image  ICON_DEL  = SWTResourceManager.getImage(ColumnsSetsManagerDialog.class, "icons/delete.png");

   private ColumnsSetsManager  csManager;

   private Text                newName;
   private Table               columnsSetsTable;

   private List<ColumnsSet>    columnsSets;

   private Map<Object, Label>  delLabels = new HashMap<>();

   public ColumnsSetsManagerDialog(Shell parentShell, ColumnsSetsManager csManager) {
      super(parentShell);

      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);

      this.csManager = csManager;
      this.columnsSets = csManager.getColumnsSets();
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setSize(900, 600);
      newShell.setText("Manage Columns Sets");
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.OK_ID, "Save", true);
      createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(1, false));

      Composite addComposite = new Composite(container, SWT.NONE);
      addComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      GridLayout glAdd = new GridLayout(3, false);
      glAdd.marginWidth = 0;
      addComposite.setLayout(glAdd);

      Label lblNewLabel = new Label(addComposite, SWT.NONE);
      lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
      lblNewLabel.setAlignment(SWT.CENTER);
      lblNewLabel.setText("Name");

      newName = new Text(addComposite, SWT.BORDER);
      newName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      newName.setToolTipText("Name of a new Columns Set");
      newName.setTextLimit(10); // Name 10 chars max

      Button btnAddColumnsSet = new Button(addComposite, SWT.NONE);
      btnAddColumnsSet.setToolTipText("Show the dialog to add a new Column Set");
      btnAddColumnsSet.setText("Add...");

      // Table with columns sets

      Composite compositeList = new Composite(container, SWT.NONE);
      compositeList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      TableColumnLayout tcListComposite = new TableColumnLayout();
      compositeList.setLayout(tcListComposite);

      final TableViewer columnsSetsTableViewer = new TableViewer(compositeList, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      columnsSetsTable = columnsSetsTableViewer.getTable();
      columnsSetsTable.setHeaderVisible(true);
      columnsSetsTable.setLinesVisible(true);

      TableViewerColumn systemViewerColumn = new TableViewerColumn(columnsSetsTableViewer, SWT.CENTER);
      TableColumn systemColumn = systemViewerColumn.getColumn();
      tcListComposite.setColumnData(systemColumn, new ColumnPixelData(16, false, true));
      systemColumn.setResizable(false); // resizable attribute of ColumnPixelData is not functionnal...
      systemViewerColumn.setLabelProvider(new ColumnLabelProvider() {

         @Override
         public String getText(Object element) {
            return "";
         }

         // Manage the remove icon
         @Override
         public void update(ViewerCell cell) {
            ColumnsSet cs = (ColumnsSet) cell.getElement();
            if (cs.isSystem()) {
               super.update(cell);
               return;
            }

            cell.setImage(null);
            cell.setBackground(null);
            cell.setForeground(null);
            cell.setFont(null);

            // Do not recreate the label if already built
            if (delLabels.containsKey(cs)) {
               log.debug("del icon already build for {}", cs);
               if (!delLabels.get(cs).isDisposed()) {
                  return;
               } else {
                  delLabels.remove(cs);
               }
            }

            Composite parentComposite = (Composite) cell.getViewerRow().getControl();
            Color cellColor = cell.getBackground();

            Label delLabel = new Label(parentComposite, SWT.CENTER);
            delLabel.setImage(ICON_DEL);
            delLabel.setBackground(cellColor);
            delLabel.addMouseListener(new MouseAdapter() {
               @Override
               public void mouseDown(MouseEvent e) {
                  log.debug("Remove columns set '{}'", cs.getName());
                  columnsSets.remove(cs);
                  clearDelLabelsCache();
                  columnsSetsTableViewer.refresh();
               }
            });

            TableItem item = (TableItem) cell.getItem();

            TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            editor.setEditor(delLabel, item, cell.getColumnIndex());
            editor.layout();

            delLabels.put(cs, delLabel);
         }
      });

      TableViewerColumn nameViewerColumn = new TableViewerColumn(columnsSetsTableViewer, SWT.LEFT);
      TableColumn nameColumn = nameViewerColumn.getColumn();
      tcListComposite.setColumnData(nameColumn, new ColumnWeightData(4, 100, true));
      nameColumn.setText("Name");
      nameViewerColumn.setLabelProvider(ColumnLabelProvider.createTextProvider(cs -> ((ColumnsSet) cs).getName()));

      TableViewerColumn definitionViewerColumn = new TableViewerColumn(columnsSetsTableViewer, SWT.LEFT);
      TableColumn definitionColumn = definitionViewerColumn.getColumn();
      tcListComposite.setColumnData(definitionColumn, new ColumnWeightData(12, 100, true));
      definitionColumn.setText("Columns");
      definitionViewerColumn
               .setLabelProvider(ColumnLabelProvider.createTextProvider(cs -> csManager.buildDescription((ColumnsSet) cs)));

      // Add a Double Click Listener
      columnsSetsTableViewer.addDoubleClickListener(event -> {
         IStructuredSelection sel = (IStructuredSelection) event.getSelection();
         ColumnsSet cs = (ColumnsSet) sel.getFirstElement();

         // System visualizers can not be edited
         if (cs.isSystem()) {
            return;
         }

         showAddEditDialog(columnsSetsTableViewer, cs);
      });

      columnsSetsTableViewer.setContentProvider(ArrayContentProvider.getInstance());
      columnsSetsTableViewer.setInput(columnsSets);

      // ----------
      // Behavior
      // ----------

      btnAddColumnsSet.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         log.debug("Add selected");
         String name = newName.getText().trim();
         if (name.isEmpty()) {
            MessageDialog.openInformation(getShell(), "Missing Name", "Please first enter a name for the colums set");
            return;
         }

         // Check for duplicates
         for (ColumnsSet cs : columnsSets) {
            if (cs.getName().equalsIgnoreCase(name)) {
               MessageDialog.openError(getShell(), "Duplicate Name", "A columns set with this name already exist");
               return;
            }

         }

         showAddEditDialog(columnsSetsTableViewer, null);
      }));

      columnsSetsTable.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
         if (e.keyCode == SWT.DEL) {
            IStructuredSelection selection = (IStructuredSelection) columnsSetsTableViewer.getSelection();
            if (selection.isEmpty()) {
               return;
            }
            for (Object sel2 : selection.toList()) {
               ColumnsSet cs = (ColumnsSet) sel2;
               if (cs.isSystem()) {
                  continue;
               }
               log.debug("Remove columns set '{}'", cs.getName());
               columnsSets.remove(cs);
            }
            clearDelLabelsCache();
            columnsSetsTableViewer.refresh();
            compositeList.layout();
            Utils.resizeTableViewer(columnsSetsTableViewer);
         }
      }));

      compositeList.layout();
      Utils.resizeTableViewer(columnsSetsTableViewer);

      return container;
   }

   private void clearDelLabelsCache() {
      for (Label b : delLabels.values()) {
         b.dispose();
      }
      delLabels.clear();
   }

   private void showAddEditDialog(TableViewer columnsSetsTableViewer, ColumnsSet oldColumnsSet) {

      ColumnsSetDialog d1 = new ColumnsSetDialog(getShell(), csManager, oldColumnsSet);
      if (d1.open() != Window.OK) {
         return;
      }

      if (oldColumnsSet == null) {

         ColumnsSet newColumnsSet = new ColumnsSet();
         newColumnsSet.setName(newName.getText().trim());
         newColumnsSet.setSystem(false);
         newColumnsSet.getColumn().addAll(d1.getColumns());

         columnsSets.add(newColumnsSet);
         Collections.sort(columnsSets, ColumnsSetsManager.COLUMNSSETS_COMPARATOR);
      }

      clearDelLabelsCache();
      Utils.resizeTableViewer(columnsSetsTableViewer);
      columnsSetsTableViewer.refresh();
   }

}
