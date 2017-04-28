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
package org.titou10.jtb.visualizer.dialog;

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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
import org.titou10.jtb.visualizer.VisualizersManager;
import org.titou10.jtb.visualizer.gen.Visualizer;
import org.titou10.jtb.visualizer.gen.VisualizerKind;
import org.titou10.jtb.visualizer.gen.VisualizerMessageType;

/**
 * 
 * Manage the Visualizers
 * 
 * @author Denis Forveille
 *
 */
public class VisualizersManageDialog extends Dialog {

   private static final Logger log     = LoggerFactory.getLogger(VisualizersManageDialog.class);

   private VisualizersManager  visualizersManager;
   private Text                newName;
   private Table               visualizerTable;

   private List<Visualizer>    visualizers;
   private VisualizerKind      visualizerKindSelected;

   private Map<Object, Button> buttons = new HashMap<>();

   public VisualizersManageDialog(Shell parentShell, VisualizersManager visualizersManager) {
      super(parentShell);

      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);

      this.visualizersManager = visualizersManager;
      this.visualizers = visualizersManager.getVisualisers();
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setSize(900, 600);
      newShell.setText("Manage Visualizers");
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
      lblNewLabel.setBounds(0, 0, 49, 13);
      lblNewLabel.setText("Name");

      Label lbl1 = new Label(addComposite, SWT.NONE);
      lbl1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
      lbl1.setAlignment(SWT.CENTER);
      lbl1.setBounds(0, 0, 49, 13);
      lbl1.setText("Kind");
      new Label(addComposite, SWT.NONE);

      newName = new Text(addComposite, SWT.BORDER);
      newName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      newName.setBounds(0, 0, 76, 19);

      final Combo newKindCombo = new Combo(addComposite, SWT.READ_ONLY);

      Button btnAddVisualizer = new Button(addComposite, SWT.NONE);
      btnAddVisualizer.setText("Add...");

      // Table with visualizers

      Composite compositeList = new Composite(container, SWT.NONE);
      compositeList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      TableColumnLayout tcListComposite = new TableColumnLayout();
      compositeList.setLayout(tcListComposite);

      final TableViewer visualizerTableViewer = new TableViewer(compositeList, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      visualizerTable = visualizerTableViewer.getTable();
      visualizerTable.setHeaderVisible(true);
      visualizerTable.setLinesVisible(true);

      TableViewerColumn systemViewerColumn = new TableViewerColumn(visualizerTableViewer, SWT.NONE);
      TableColumn systemColumn = systemViewerColumn.getColumn();
      systemColumn.setAlignment(SWT.CENTER);
      tcListComposite.setColumnData(systemColumn, new ColumnPixelData(15, false, true));
      systemViewerColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            Visualizer v = (Visualizer) element;
            return v.isSystem() ? "  *" : null;
         }

         // Manage the remove icon
         @Override
         public void update(ViewerCell cell) {
            Visualizer v = (Visualizer) cell.getElement();
            if (v.isSystem()) {
               super.update(cell);
               return;
            }

            // Do not recreate buttons if already built
            if (buttons.containsKey(v) && !buttons.get(v).isDisposed()) {
               log.debug("visualizer {} found in cache", v.getName());
               super.update(cell);
               return;
            }

            Composite parentComposite = (Composite) cell.getViewerRow().getControl();
            Color parentColor = parentComposite.getBackground();
            Image image = SWTResourceManager.getImage(this.getClass(), "icons/delete.png");

            Button btnRemove = new Button(parentComposite, SWT.NONE);
            btnRemove.addSelectionListener(new SelectionAdapter() {
               @Override
               public void widgetSelected(SelectionEvent event) {
                  log.debug("Remove visualizer '{}'", v.getName());
                  visualizers.remove(v);
                  clearButtonCache();
                  visualizerTableViewer.refresh();
               }
            });
            btnRemove.addPaintListener(new PaintListener() {
               @Override
               public void paintControl(PaintEvent event) {
                  SWTResourceManager.drawCenteredImage(event, parentColor, image);
               }
            });

            TableItem item = (TableItem) cell.getItem();

            TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            editor.setEditor(btnRemove, item, cell.getColumnIndex());
            editor.layout();

            buttons.put(cell.getElement(), btnRemove);
         }
      });

      TableViewerColumn nameViewerColumn = new TableViewerColumn(visualizerTableViewer, SWT.NONE);
      TableColumn nameColumn = nameViewerColumn.getColumn();
      tcListComposite.setColumnData(nameColumn, new ColumnWeightData(4, 100, true));
      nameColumn.setText("Name");
      nameViewerColumn.setLabelProvider(new ColumnLabelProvider() {

         @Override
         public String getText(Object element) {
            Visualizer v = (Visualizer) element;
            return v.getName();
         }
      });

      TableViewerColumn kindViewerColumn = new TableViewerColumn(visualizerTableViewer, SWT.NONE);
      TableColumn kindColumn = kindViewerColumn.getColumn();
      tcListComposite.setColumnData(kindColumn, new ColumnWeightData(1, 100, true));
      kindColumn.setText("Kind");
      kindViewerColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            Visualizer v = (Visualizer) element;
            return v.getKind().name();
         }
      });

      TableViewerColumn targetViewerColumn = new TableViewerColumn(visualizerTableViewer, SWT.NONE);
      TableColumn targetColumn = targetViewerColumn.getColumn();
      tcListComposite.setColumnData(targetColumn, new ColumnWeightData(1, 100, true));
      targetColumn.setText("Targets JMS Messages");
      targetViewerColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            Visualizer v = (Visualizer) element;
            StringBuilder sb = new StringBuilder(64);
            sb.append("[ ");
            for (VisualizerMessageType vmt : v.getTargetMsgType()) {
               sb.append(vmt);
               sb.append(" ");
            }
            sb.append("]");
            return sb.toString();
         }
      });

      TableViewerColumn definitionViewerColumn = new TableViewerColumn(visualizerTableViewer, SWT.NONE);
      TableColumn definitionColumn = definitionViewerColumn.getColumn();
      tcListComposite.setColumnData(definitionColumn, new ColumnWeightData(12, 100, true));
      definitionColumn.setText("Definition");
      definitionViewerColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            Visualizer v = (Visualizer) element;
            return visualizersManager.buildDescription(v);
         }
      });

      // Add a Double Click Listener
      visualizerTableViewer.addDoubleClickListener((event) -> {
         IStructuredSelection sel = (IStructuredSelection) event.getSelection();
         Visualizer v = (Visualizer) sel.getFirstElement();

         // System visualizers can not be edited
         if (v.isSystem()) {
            return;
         }

         showAddEditDialog(visualizerTableViewer, v.getKind(), v.getName(), v);
      });

      // ----------
      // Set values
      // ----------

      String[] vkNames = visualizersManager.getVisualizerKindsBuildable();
      newKindCombo.setItems(vkNames);
      int sel = 0;
      newKindCombo.select(sel);
      visualizerKindSelected = VisualizerKind.valueOf(vkNames[sel]);

      visualizerTableViewer.setContentProvider(ArrayContentProvider.getInstance());
      visualizerTableViewer.setInput(visualizers);

      // ----------
      // Behavior
      // ----------

      btnAddVisualizer.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            log.debug("Add selected");
            String name = newName.getText().trim();
            if (name.isEmpty()) {
               MessageDialog.openInformation(getShell(), "Missing Name", "Please first enter a name for the visualizer");
               return;
            }

            // Check for duplicates
            for (Visualizer v : visualizers) {
               if (v.getName().equalsIgnoreCase(name)) {
                  MessageDialog.openError(getShell(), "Duplicate Name", "A visualizer with this name already exist");
                  return;
               }

            }

            showAddEditDialog(visualizerTableViewer, visualizerKindSelected, name, null);
         }
      });

      visualizerTable.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.keyCode == SWT.DEL) {
               IStructuredSelection selection = (IStructuredSelection) visualizerTableViewer.getSelection();
               if (selection.isEmpty()) {
                  return;
               }
               for (Object sel : selection.toList()) {
                  Visualizer v = (Visualizer) sel;
                  if (v.isSystem()) {
                     continue;
                  }
                  log.debug("Remove visualizer '{}'", v.getName());
                  visualizers.remove(v);
               }
               clearButtonCache();
               visualizerTableViewer.refresh();
            }
         }
      });

      // Save the selected property Kind
      newKindCombo.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent arg0) {
            String sel = newKindCombo.getItem(newKindCombo.getSelectionIndex());
            visualizerKindSelected = VisualizerKind.valueOf(sel);
         }
      });

      Utils.resizeTableViewer(visualizerTableViewer);

      return container;
   }

   private void showAddEditDialog(TableViewer visualizerTableViewer,
                                  VisualizerKind kind,
                                  String visualizerName,
                                  Visualizer visualizer) {

      switch (kind) {

         case OS_EXTENSION:
            VisualizerOSExtensionDialog d2 = new VisualizerOSExtensionDialog(getShell(), visualizer);
            if (d2.open() != Window.OK) {
               return;
            }

            String extension = d2.getExtension();
            List<VisualizerMessageType> listMessageType2 = d2.getListMessageType();
            Visualizer newVisualizer2 = visualizersManager.buildOSExtension(false, visualizerName, extension, listMessageType2);

            addOrReplaceVisualizer(visualizerTableViewer, visualizer, newVisualizer2);
            break;

         case EXTERNAL_SCRIPT:
            VisualizerExternalScriptDialog d3 = new VisualizerExternalScriptDialog(getShell(), visualizer);
            if (d3.open() != Window.OK) {
               return;
            }

            String fileName = d3.getFileName();
            boolean showScriptLogs3 = d3.getShowScriptLogs();
            List<VisualizerMessageType> listMessageType3 = d3.getListMessageType();

            Visualizer newVisualizer3 = visualizersManager
                     .buildExternalScript(false, showScriptLogs3, visualizerName, fileName, listMessageType3);

            addOrReplaceVisualizer(visualizerTableViewer, visualizer, newVisualizer3);

            break;

         case INLINE_SCRIPT:
            VisualizerInlineScriptDialog d4 = new VisualizerInlineScriptDialog(getShell(), visualizer, visualizersManager);
            if (d4.open() != Window.OK) {
               return;
            }

            String source = d4.getSource();
            boolean showScriptLogs4 = d4.getShowScriptLogs();
            List<VisualizerMessageType> listMessageType4 = d4.getListMessageType();

            Visualizer newVisualizer4 = visualizersManager
                     .buildInlineScript(false, showScriptLogs4, visualizerName, source, listMessageType4);
            addOrReplaceVisualizer(visualizerTableViewer, visualizer, newVisualizer4);
            break;

         case EXTERNAL_COMMAND:
            VisualizerExternalCommandDialog d5 = new VisualizerExternalCommandDialog(getShell(), visualizer);
            if (d5.open() != Window.OK) {
               return;
            }

            String commandName = d5.getCommandName();
            List<VisualizerMessageType> listMessageType5 = d5.getListMessageType();

            Visualizer newVisualizer5 = visualizersManager.buildExternalCommand(false,
                                                                                visualizerName,
                                                                                commandName,
                                                                                listMessageType5);

            addOrReplaceVisualizer(visualizerTableViewer, visualizer, newVisualizer5);
            break;

         default:
            // Ignore other kinds
            break;
      }
   }

   private void addOrReplaceVisualizer(TableViewer visualizerTableViewer, Visualizer oldVisualizer, Visualizer newVisualizer) {
      if (oldVisualizer != null) {
         visualizers.set(visualizers.indexOf(oldVisualizer), newVisualizer);
      } else {
         visualizers.add(newVisualizer);
         Collections.sort(visualizers, VisualizersManager.VISUALIZER_COMPARATOR);
      }
      clearButtonCache();
      visualizerTableViewer.refresh();
   }

   private void clearButtonCache() {
      for (Button b : buttons.values()) {
         b.dispose();
      }
      buttons.clear();
   }
}
