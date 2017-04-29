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
package org.titou10.jtb.variable.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
import org.titou10.jtb.util.Utils;
import org.titou10.jtb.variable.gen.Variable;

/**
 * 
 * Ask for a new Variable of kind "List"
 * 
 * @author Denis Forveille
 *
 */
public class VariablesListDialog extends Dialog {

   private static final Logger log     = LoggerFactory.getLogger(VariablesListDialog.class);

   private Variable            variable;

   private Table               table;

   private List<String>        values  = new ArrayList<>();

   private List<Button>        buttons = new ArrayList<>();

   public VariablesListDialog(Shell parentShell, Variable variable) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);

      this.variable = variable;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Add a new 'List' variable");
   }

   @Override
   protected Point getInitialSize() {
      return new Point(600, 600);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(3, false));

      Label lblNewLabel = new Label(container, SWT.NONE);
      lblNewLabel.setText("Value:");

      final Text newValue = new Text(container, SWT.BORDER);
      newValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnAdd = new Button(container, SWT.NONE);
      btnAdd.setText("Add");

      Label lblValues = new Label(container, SWT.NONE);
      lblValues.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
      lblValues.setText("Values:");

      // Table with Values

      Composite compositeList = new Composite(container, SWT.NONE);
      compositeList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
      TableColumnLayout tcListComposite = new TableColumnLayout();
      compositeList.setLayout(tcListComposite);

      final TableViewer tableViewer = new TableViewer(compositeList, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      table = tableViewer.getTable();
      table.setHeaderVisible(true);
      table.setLinesVisible(true);

      TableViewerColumn systemViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn systemColumn = systemViewerColumn.getColumn();
      systemColumn.setAlignment(SWT.CENTER);
      tcListComposite.setColumnData(systemColumn, new ColumnWeightData(1, 16, false));
      systemViewerColumn.setLabelProvider(new ColumnLabelProvider() {
         // Manage the remove icon
         @Override
         public void update(ViewerCell cell) {
            String value = (String) cell.getElement();

            Composite parentComposite = (Composite) cell.getViewerRow().getControl();
            Color cellColor = cell.getBackground();
            Image image = SWTResourceManager.getImage(this.getClass(), "icons/delete.png");

            Button btnRemove = new Button(parentComposite, SWT.NONE);
            btnRemove.addSelectionListener(new SelectionAdapter() {
               @Override
               public void widgetSelected(SelectionEvent event) {
                  log.debug("Remove value '{}'", value);
                  values.remove(value);
                  clearButtonCache();
                  tableViewer.refresh();
               }
            });
            btnRemove.addPaintListener(new PaintListener() {
               @Override
               public void paintControl(PaintEvent event) {
                  SWTResourceManager.drawCenteredImage(event, cellColor, image);
               }
            });

            TableItem item = (TableItem) cell.getItem();

            TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            editor.setEditor(btnRemove, item, cell.getColumnIndex());
            editor.layout();

            buttons.add(btnRemove);
         }
      });

      TableViewerColumn nameViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn nameColumn = nameViewerColumn.getColumn();
      tcListComposite.setColumnData(nameColumn, new ColumnWeightData(4, 100, true));
      nameColumn.setText("Name");
      nameViewerColumn.setLabelProvider(new ColumnLabelProvider() {
      });

      btnAdd.addSelectionListener(new SelectionAdapter() {
         // Add value the list of values
         @Override
         public void widgetSelected(SelectionEvent e) {
            String value = newValue.getText();
            if ((value != null) && (value.length() > 0)) {
               log.debug("Adding value {} to the list", value);
               values.add(value);
               tableViewer.refresh();
               clearButtonCache();
               tableViewer.refresh();
            }
         }
      });

      table.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.keyCode == SWT.DEL) {
               IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
               if (selection.isEmpty()) {
                  return;
               }
               for (Object item : selection.toList()) {
                  log.debug("Remove {} from the list", item);
                  values.remove(item);
               }
               clearButtonCache();
               tableViewer.refresh();
            }
         }
      });

      if (variable != null) {
         values = variable.getListValue();
      }

      // Populate fields
      tableViewer.setContentProvider(ArrayContentProvider.getInstance());
      tableViewer.setInput(values);

      Utils.resizeTableViewer(tableViewer);

      return container;
   }

   private void clearButtonCache() {
      for (Button b : buttons) {
         b.dispose();
      }
      buttons.clear();
   }

   // ----------------
   // Standard Getters
   // ----------------

   public List<String> getValues() {
      return values;
   }

}
