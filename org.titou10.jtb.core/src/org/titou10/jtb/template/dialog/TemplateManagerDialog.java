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
package org.titou10.jtb.template.dialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.filesystem.URIUtil;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.template.gen.TemplateDirectory;
import org.titou10.jtb.util.Utils;

/**
 * 
 * Manage the Templates Directory
 * 
 * @author Denis Forveille
 *
 */
public class TemplateManagerDialog extends Dialog {

   private static final Logger     log     = LoggerFactory.getLogger(TemplateManagerDialog.class);

   @Inject
   private TemplatesManager        templatesManager;

   private Text                    newName;
   private Table                   tdTable;

   private List<TemplateDirectory> listTD;

   private Map<Object, Button>     buttons = new HashMap<>();

   public TemplateManagerDialog(Shell parentShell, TemplatesManager templatesManager) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);

      this.templatesManager = templatesManager;
      this.listTD = templatesManager.getTemplateRootDirs();
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setSize(new Point(700, 500));
      newShell.setText("Configure Templates Directories");
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

      // Headers

      Label lblNewLabel = new Label(addComposite, SWT.NONE);
      lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
      lblNewLabel.setAlignment(SWT.CENTER);
      lblNewLabel.setText("Directory Name: ");

      GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
      gd.widthHint = 160;
      newName = new Text(addComposite, SWT.BORDER);
      newName.setLayoutData(gd);
      newName.setTextLimit(16);

      Button btnBrowseAndAdd = new Button(addComposite, SWT.NONE);
      lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
      btnBrowseAndAdd.setText("Browse and add directory...");

      // Table with directories

      Composite compositeList = new Composite(container, SWT.NONE);
      compositeList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
      TableColumnLayout tcListComposite = new TableColumnLayout();
      compositeList.setLayout(tcListComposite);

      final TableViewer tdTableViewer = new TableViewer(compositeList, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      tdTable = tdTableViewer.getTable();
      tdTable.setHeaderVisible(true);
      tdTable.setLinesVisible(true);

      TableViewerColumn systemViewerColumn = new TableViewerColumn(tdTableViewer, SWT.CENTER);
      TableColumn systemColumn = systemViewerColumn.getColumn();
      tcListComposite.setColumnData(systemColumn, new ColumnPixelData(16, false, true));
      systemColumn.setResizable(false); // resizable attribute of ColumnPixelData is not functionnal...
      systemViewerColumn.setLabelProvider(new ColumnLabelProvider() {

         @Override
         public String getText(Object element) {
            TemplateDirectory td = (TemplateDirectory) element;
            return Utils.getStar(td.isSystem());
         }

         // Manage the remove icon
         @Override
         public void update(ViewerCell cell) {
            TemplateDirectory td = (TemplateDirectory) cell.getElement();
            if (td.isSystem()) {
               super.update(cell);
               return;
            }

            // Do not recreate buttons if already built
            if (buttons.containsKey(td) && !buttons.get(td).isDisposed()) {
               log.debug("Template Directory {} found in cache", td.getName());
               return;
            }
            Composite parentComposite = (Composite) cell.getViewerRow().getControl();
            Color cellColor = cell.getBackground();
            Image image = SWTResourceManager.getImage(this.getClass(), "icons/delete.png");

            Button btnRemove = new Button(parentComposite, SWT.NONE);
            btnRemove.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
               log.debug("Remove variable '{}'", td.getName());
               listTD.remove(td);
               clearButtonCache();
               tdTableViewer.refresh();
            }));

            btnRemove.addPaintListener(event -> SWTResourceManager.drawCenteredImage(event, cellColor, image));

            TableItem item = (TableItem) cell.getItem();

            TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            editor.setEditor(btnRemove, item, cell.getColumnIndex());
            editor.layout();

            // TODO DF: remove
            log.debug("Store button in cache for {}", td.getName());
            buttons.put(td, btnRemove);
         }
      });

      TableViewerColumn nameViewerColumn = new TableViewerColumn(tdTableViewer, SWT.LEFT);
      TableColumn nameColumn = nameViewerColumn.getColumn();
      tcListComposite.setColumnData(nameColumn, new ColumnWeightData(4, 100, true));
      nameColumn.setText("Name");
      nameViewerColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            TemplateDirectory td = (TemplateDirectory) element;
            return td.getName();
         }
      });

      TableViewerColumn kindViewerColumn = new TableViewerColumn(tdTableViewer, SWT.LEFT);
      TableColumn kindColumn = kindViewerColumn.getColumn();
      tcListComposite.setColumnData(kindColumn, new ColumnWeightData(1, 25, true));
      kindColumn.setText("Directory");
      kindViewerColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            TemplateDirectory td = (TemplateDirectory) element;
            return td.getDirectory();
         }
      });

      // ----------
      // Set values
      // ----------

      tdTableViewer.setContentProvider(ArrayContentProvider.getInstance());
      tdTableViewer.setInput(listTD);

      // ----------
      // Behavior
      // ----------

      // FileDialog to chose a directory
      btnBrowseAndAdd.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         String name = newName.getText();
         if (Utils.isEmpty(name)) {
            MessageDialog.openInformation(getShell(), "Missing Name", "Please first enter a name for the directory");
            return;
         }
         DirectoryDialog directoryDialog = new DirectoryDialog(getShell(), SWT.OPEN);
         directoryDialog.setText("Select directory");

         String selectedDirectoryName = directoryDialog.open();
         if (selectedDirectoryName == null) {
            return;
         }
         selectedDirectoryName = URIUtil.toPath(URIUtil.toURI(selectedDirectoryName)).toPortableString();

         listTD.add(templatesManager.buildTemplateDirectory(false, name, selectedDirectoryName));
         clearButtonCache();
         tdTableViewer.refresh();
      }));

      tdTable.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.keyCode == SWT.DEL) {
               IStructuredSelection selection = (IStructuredSelection) tdTableViewer.getSelection();
               if (selection.isEmpty()) {
                  return;
               }
               for (Object sel : selection.toList()) {
                  TemplateDirectory td = (TemplateDirectory) sel;
                  if (td.isSystem()) {
                     continue;
                  }
                  log.debug("Remove directory '{}'", td.getName());
                  listTD.remove(td);
               }
               clearButtonCache();
               tdTableViewer.refresh();
               compositeList.layout();
               Utils.resizeTableViewer(tdTableViewer);
            }
         }
      });

      compositeList.layout();
      Utils.resizeTableViewer(tdTableViewer);

      return container;
   }

   private void clearButtonCache() {
      for (Button b : buttons.values()) {
         b.dispose();
      }
      buttons.clear();
   }

   // ----------------
   // Standard Getters
   // ----------------

   public List<TemplateDirectory> getListTD() {
      return listTD;
   }
}
