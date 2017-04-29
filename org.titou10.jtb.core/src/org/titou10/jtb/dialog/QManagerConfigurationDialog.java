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
package org.titou10.jtb.dialog;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.MetaQManager;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * 
 * Configure a Q Manager
 * 
 * @author Denis Forveille
 *
 */
public class QManagerConfigurationDialog extends Dialog {

   private static final Logger log     = LoggerFactory.getLogger(QManagerConfigurationDialog.class);

   private String              qManagerName;
   private String              helpText;

   private Table               jarsTable;

   private Text                newJarName;
   private SortedSet<String>   jarNames;

   private Map<Object, Button> buttons = new HashMap<>();

   public QManagerConfigurationDialog(Shell parentShell, MetaQManager metaQManager) {
      super(parentShell);

      this.qManagerName = metaQManager.getDisplayName();
      this.jarNames = new TreeSet<String>(metaQManager.getqManagerDef().getJar());

      if (metaQManager.getQmanager() != null) {
         helpText = metaQManager.getQmanager().getHelpText();
      }

      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setSize(new Point(700, 500));
      newShell.setText("Configure Q Manager '" + qManagerName + "'");
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.OK_ID, "Save", true);
      createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
   }

   @Override
   protected Control createButtonBar(final Composite parent) {
      Composite buttonBar = new Composite(parent, SWT.NONE);

      GridLayout layout = new GridLayout(2, false);
      layout.marginRight = -7; // DF: Magic number?
      layout.marginLeft = 0;

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
            QManagerHelpDialog helpDialog = new QManagerHelpDialog(getShell(), helpText);
            helpDialog.open();
         }
      });
      if (helpText == null) {
         help.setEnabled(false);
      }

      final GridData leftButtonData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
      leftButtonData.grabExcessHorizontalSpace = true;
      leftButtonData.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
      help.setLayoutData(leftButtonData);

      // Other buttons on the right
      final Control buttonControl = super.createButtonBar(buttonBar);
      buttonControl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

      return buttonBar;
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(4, false));

      Label lblJars = new Label(container, SWT.NONE);
      lblJars.setText("Jar Name:");

      newJarName = new Text(container, SWT.BORDER);
      newJarName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnAdd = new Button(container, SWT.NONE);
      btnAdd.setText("Add");

      Button btnBrowse = new Button(container, SWT.NONE);
      btnBrowse.setText("Browse and Add...");

      Label lblJars_1 = new Label(container, SWT.NONE);
      lblJars_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
      lblJars_1.setText("Extra jars :");

      // Table with Jars

      Composite compositeList = new Composite(container, SWT.NONE);
      compositeList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
      TableColumnLayout tcListComposite = new TableColumnLayout();
      compositeList.setLayout(tcListComposite);

      final TableViewer jarsTableViewer = new TableViewer(compositeList, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      jarsTable = jarsTableViewer.getTable();
      jarsTable.setHeaderVisible(true);
      jarsTable.setLinesVisible(true);

      TableViewerColumn systemViewerColumn = new TableViewerColumn(jarsTableViewer, SWT.NONE);
      TableColumn systemColumn = systemViewerColumn.getColumn();
      systemColumn.setAlignment(SWT.CENTER);
      tcListComposite.setColumnData(systemColumn, new ColumnWeightData(1, 16, false));
      systemViewerColumn.setLabelProvider(new ColumnLabelProvider() {
         // Manage the remove icon
         @Override
         public void update(ViewerCell cell) {
            String jarName = (String) cell.getElement();

            // Do not recreate buttons if already built
            if (buttons.containsKey(jarName) && !buttons.get(jarName).isDisposed()) {
               log.debug("jar {} found in cache", jarName);
               super.update(cell);
               return;
            }

            Composite parentComposite = (Composite) cell.getViewerRow().getControl();
            Color cellColor = cell.getBackground();
            Image image = SWTResourceManager.getImage(this.getClass(), "icons/delete.png");

            Button btnRemove = new Button(parentComposite, SWT.NONE);
            btnRemove.addSelectionListener(new SelectionAdapter() {
               @Override
               public void widgetSelected(SelectionEvent event) {
                  log.debug("Remove jar '{}'", jarName);
                  jarNames.remove(jarName);
                  clearButtonCache();
                  jarsTableViewer.refresh();
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

            buttons.put(jarName, btnRemove);
         }
      });

      TableViewerColumn nameViewerColumn = new TableViewerColumn(jarsTableViewer, SWT.NONE);
      TableColumn nameColumn = nameViewerColumn.getColumn();
      tcListComposite.setColumnData(nameColumn, new ColumnWeightData(4, 100, true));
      nameColumn.setText("Name");
      nameViewerColumn.setLabelProvider(new ColumnLabelProvider() {
      });

      // --------
      // Behavior
      // --------

      btnAdd.addSelectionListener(new SelectionAdapter() {
         // Add file name to the list of Jars
         @Override
         public void widgetSelected(SelectionEvent e) {
            String jarName = newJarName.getText();
            if (Utils.isNotEmpty(jarName)) {
               if (!(jarName.endsWith(".jar"))) {
                  MessageDialog.openError(getShell(), "Error", "The file name must hava a '.jar' extension");
                  return;
               }
               log.debug("Adding file {} to the list", jarName);
               if (!(jarNames.contains(jarName))) {
                  jarNames.add(jarName);
                  clearButtonCache();
                  jarsTableViewer.refresh();
               }
            }
         }
      });

      btnBrowse.addSelectionListener(new SelectionAdapter() {
         // FileDialog to chose a jar file
         @Override
         public void widgetSelected(SelectionEvent e) {
            FileDialog fileDialog = new FileDialog(getParentShell(), SWT.OPEN | SWT.MULTI);
            fileDialog.setText("Select jar file");
            fileDialog.setFilterExtensions(Constants.JAR_FILE_EXTENSION_FILTER);
            String sel = fileDialog.open();
            if (sel != null) {
               String path = fileDialog.getFilterPath();
               String[] fileNames = fileDialog.getFileNames();
               for (int i = 0; i < fileNames.length; i++) {
                  String jarName = path + File.separator + fileNames[i];
                  if (!(jarNames.contains(jarName))) {
                     jarNames.add(jarName);
                     clearButtonCache();
                     jarsTableViewer.refresh();
                  }
               }
            }
         }
      });

      jarsTable.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.keyCode == SWT.DEL) {
               IStructuredSelection selection = (IStructuredSelection) jarsTableViewer.getSelection();
               if (selection.isEmpty()) {
                  return;
               }
               for (Object item : selection.toList()) {
                  log.debug("Remove {} from the list", item);
                  jarNames.remove(item);
               }
               clearButtonCache();
               jarsTableViewer.refresh();
            }
         }
      });

      // --------------
      // Populate fields
      // --------------
      jarsTableViewer.setContentProvider(ArrayContentProvider.getInstance());
      jarsTableViewer.setInput(jarNames);

      Utils.resizeTableViewer(jarsTableViewer);

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
   public SortedSet<String> getJarNames() {
      return jarNames;
   }
}
