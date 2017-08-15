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
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
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

   private static final Logger log     = LoggerFactory.getLogger(ColumnsSetsManagerDialog.class);

   private ColumnsSetsManager  csManager;

   private Text                newName;
   private Table               columnsSetsTable;

   private List<ColumnsSet>    columnsSets;

   private Map<Object, Button> buttons = new HashMap<>();

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
      // systemViewerColumn.setLabelProvider(new ColumnLabelProvider() {
      systemViewerColumn.setLabelProvider(new OwnerDrawLabelProvider() {

         @Override
         protected void measure(Event event, Object element) {

            // ColumnsSet cs = (ColumnsSet) element;
            // // Do not recreate buttons if already built
            // if (buttons.containsKey(cs) && !buttons.get(cs).isDisposed()) {
            // log.debug("Columns Set {} found in cache", cs.getName());
            // return;
            // }
            //
            // Button btnRemove = new Button(parentComposite, SWT.NONE);
            // btnRemove.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            // log.debug("Remove columns set '{}'", cs.getName());
            // columnsSets.remove(cs);
            // clearButtonCache();
            // columnsSetsTableViewer.refresh();
            // }));
            // Rectangle rectangle = ICON.getBounds();
            // event.
            // setBounds(new Rectangle(
            // event.x,
            // event.y,
            // rectangle.width + 200 ,
            // rectangle.height));
            //
         }

         @Override
         protected void paint(Event event, Object element) {
            ColumnsSet cs = (ColumnsSet) element;
            Image image = SWTResourceManager.getImage(this.getClass(), "icons/delete.png");

            Rectangle bounds = event.getBounds();
            event.gc.drawText("Hello", bounds.x, bounds.y);
            Point point = event.gc.stringExtent("Hello");
            event.gc.drawImage(image, bounds.x + 5 + point.x, bounds.y);
         }

         // @Override
         // public String getText(Object element) {
         // ColumnsSet cs = (ColumnsSet) element;
         // return Utils.getStar(cs.isSystem());
         // }

         // // Manage the remove icon
         // @Override
         // public void update(ViewerCell cell) {
         // ColumnsSet cs = (ColumnsSet) cell.getElement();
         // if (cs.isSystem()) {
         // super.update(cell);
         // return;
         // }
         //
         // // Do not recreate buttons if already built
         // if (buttons.containsKey(cs) && !buttons.get(cs).isDisposed()) {
         // log.debug("Columns Set {} found in cache", cs.getName());
         // return;
         // }
         // Composite parentComposite = (Composite) cell.getViewerRow().getControl();
         // Color cellColor = cell.getBackground();
         // Image image = SWTResourceManager.getImage(this.getClass(), "icons/delete.png");
         //
         // Button btnRemove = new Button(parentComposite, SWT.NONE);
         // btnRemove.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         // log.debug("Remove columns set '{}'", cs.getName());
         // columnsSets.remove(cs);
         // clearButtonCache();
         // columnsSetsTableViewer.refresh();
         // }));
         //
         // btnRemove.addPaintListener(event -> SWTResourceManager.drawCenteredImage(event, cellColor, image));
         //
         // TableItem item = (TableItem) cell.getItem();
         //
         // TableEditor editor = new TableEditor(item.getParent());
         // editor.grabHorizontal = true;
         // editor.grabVertical = true;
         // editor.setEditor(btnRemove, item, cell.getColumnIndex());
         // editor.layout();
         //
         // buttons.put(cs, btnRemove);
         // }
      });

      TableViewerColumn nameViewerColumn = new TableViewerColumn(columnsSetsTableViewer, SWT.LEFT);
      TableColumn nameColumn = nameViewerColumn.getColumn();
      tcListComposite.setColumnData(nameColumn, new ColumnWeightData(4, 100, true));
      nameColumn.setText("Name");
      nameViewerColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            ColumnsSet cs = (ColumnsSet) element;
            return cs.getName();
         }
      });

      TableViewerColumn definitionViewerColumn = new TableViewerColumn(columnsSetsTableViewer, SWT.LEFT);
      TableColumn definitionColumn = definitionViewerColumn.getColumn();
      tcListComposite.setColumnData(definitionColumn, new ColumnWeightData(12, 100, true));
      definitionColumn.setText("Columns");
      definitionViewerColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            ColumnsSet cs = (ColumnsSet) element;
            return csManager.buildDescription(cs);
         }
      });

      // Add a Double Click Listener
      columnsSetsTableViewer.addDoubleClickListener((event) -> {
         IStructuredSelection sel = (IStructuredSelection) event.getSelection();
         ColumnsSet cs = (ColumnsSet) sel.getFirstElement();

         // System visualizers can not be edited
         if (cs.isSystem()) {
            return;
         }

         showAddEditDialog(columnsSetsTableViewer, cs.getName(), cs);
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

         showAddEditDialog(columnsSetsTableViewer, name, null);

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
            clearButtonCache();
            columnsSetsTableViewer.refresh();
            compositeList.layout();
            Utils.resizeTableViewer(columnsSetsTableViewer);
         }
      }));

      compositeList.layout();
      Utils.resizeTableViewer(columnsSetsTableViewer);

      return container;
   }

   private void clearButtonCache() {
      for (Button b : buttons.values()) {
         b.dispose();
      }
      buttons.clear();
   }

   private void showAddEditDialog(TableViewer columnsSetsTableViewer, String columnsSetName, ColumnsSet oldColumnsSet) {

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

      clearButtonCache();
      columnsSetsTableViewer.refresh();
      Utils.resizeTableViewer(columnsSetsTableViewer);
   }

}
