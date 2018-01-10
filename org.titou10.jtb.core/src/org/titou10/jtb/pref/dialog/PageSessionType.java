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
package org.titou10.jtb.pref.dialog;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.JTBPreferenceStore;
import org.titou10.jtb.sessiontype.SessionType;
import org.titou10.jtb.sessiontype.SessionTypeManager;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * "General" page in the preference dialog
 * 
 * @author Denis Forveille
 *
 */
final class PageSessionType extends PreferencePage {

   private static final Logger log     = LoggerFactory.getLogger(PageSessionType.class);

   private JTBStatusReporter   jtbStatusReporter;
   private SessionTypeManager  sessionTypeManager;
   private IPreferenceStore    ps;

   private List<SessionType>   sessionTypes;

   private Button              showSystemObject;
   private Table               stTable;

   private Text                newName;
   private Text                newDescription;
   private Color               newColor;

   private Map<Object, Button> buttons = new HashMap<>();

   public PageSessionType(JTBStatusReporter jtbStatusReporter, SessionTypeManager sessionTypeManager, IPreferenceStore ps) {
      super("Session Types");
      this.jtbStatusReporter = jtbStatusReporter;
      this.sessionTypeManager = sessionTypeManager;
      this.ps = ps;
   }

   @Override
   protected Control createContents(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      composite.setLayout(new GridLayout(1, false));

      sessionTypes = sessionTypeManager.getSessionTypes();

      // Session Types

      Group gSessionTypes = new Group(composite, SWT.SHADOW_ETCHED_IN);
      gSessionTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      gSessionTypes.setText("Session Types");
      gSessionTypes.setLayout(new GridLayout(3, false));

      showSystemObject = new Button(gSessionTypes, SWT.CHECK);
      showSystemObject.setText("Show system destinations (Also show 'temporary' destinations for some Q Providers)");
      showSystemObject.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

      // Table with Session TYpes

      Composite compositeList = new Composite(gSessionTypes, SWT.NONE);
      compositeList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      TableColumnLayout tcListComposite = new TableColumnLayout();
      compositeList.setLayout(tcListComposite);

      final TableViewer stTableViewer = new TableViewer(compositeList, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      stTable = stTableViewer.getTable();
      stTable.setBackgroundMode(SWT.INHERIT_FORCE);
      stTable.setHeaderVisible(true);
      stTable.setLinesVisible(true);

      TableViewerColumn systemViewerColumn = new TableViewerColumn(stTableViewer, SWT.CENTER);
      TableColumn systemColumn = systemViewerColumn.getColumn();
      tcListComposite.setColumnData(systemColumn, new ColumnPixelData(16, false, true));
      systemColumn.setResizable(false); // resizable attribute of ColumnPixelData is not functionnal...
      systemViewerColumn.setLabelProvider(new ColumnLabelProvider() {

         @Override
         public String getText(Object element) {
            SessionType st = (SessionType) element;
            return Utils.getStar(st.isSystem());
         }

         // Manage the remove icon
         @Override
         public void update(ViewerCell cell) {
            SessionType st = (SessionType) cell.getElement();
            if (st.isSystem()) {
               super.update(cell);
               return;
            }

            // Do not recreate buttons if already built
            if (buttons.containsKey(st)) {
               log.debug("session type {} found in cache", st.getName());
               if (!buttons.get(st).isDisposed()) {
                  return;
               } else {
                  buttons.remove(st);
               }
            }

            Composite parentComposite = (Composite) cell.getViewerRow().getControl();
            Color cellColor = cell.getBackground();
            Image image = SWTResourceManager.getImage(this.getClass(), "icons/delete.png");

            Button btnRemove = new Button(parentComposite, SWT.NONE);
            btnRemove.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
               log.debug("Remove session type '{}'", st.getName());
               sessionTypes.remove(st);
               clearButtonCache();
               stTableViewer.refresh();
            }));

            btnRemove.addPaintListener(event -> SWTResourceManager.drawCenteredImage(event, cellColor, image));

            TableItem item = (TableItem) cell.getItem();

            TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            editor.setEditor(btnRemove, item, cell.getColumnIndex());
            editor.layout();

            buttons.put(st, btnRemove);
         }
      });

      TableViewerColumn nameViewerColumn = new TableViewerColumn(stTableViewer, SWT.LEFT);
      TableColumn nameColumn = nameViewerColumn.getColumn();
      tcListComposite.setColumnData(nameColumn, new ColumnWeightData(1, 25, true));
      nameColumn.setText("Name");
      nameViewerColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            SessionType st = (SessionType) element;
            return st.getName();
         }
      });

      TableViewerColumn descriptionViewerColumn = new TableViewerColumn(stTableViewer, SWT.LEFT);
      TableColumn descriptionColumn = descriptionViewerColumn.getColumn();
      tcListComposite.setColumnData(descriptionColumn, new ColumnWeightData(3, 100, true));
      descriptionColumn.setText("Description");
      descriptionViewerColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            SessionType st = (SessionType) element;
            return st.getDescription();
         }
      });

      // Add new type

      newColor = SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND);

      Group gAdd = new Group(composite, SWT.SHADOW_ETCHED_IN);
      gAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      gAdd.setText("Session Type");
      gAdd.setLayout(new GridLayout(3, false));

      Label lblNew = new Label(gAdd, SWT.NONE);
      lblNew.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
      lblNew.setText("Name");
      newName = new Text(gAdd, SWT.BORDER);
      newName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

      Label lblDescription = new Label(gAdd, SWT.NONE);
      lblDescription.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
      lblDescription.setText("Description");
      newDescription = new Text(gAdd, SWT.BORDER);
      newDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

      Label lblColor = new Label(gAdd, SWT.NONE);
      lblColor.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
      lblColor.setText("Color");

      Label colorLabel = new Label(gAdd, SWT.BORDER);
      colorLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
      colorLabel.setText("                              ");
      colorLabel.setBackground(newColor);

      Button btnColor = new Button(gAdd, SWT.NONE);
      btnColor.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
      btnColor.setText("Color...");
      btnColor.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         ColorDialog dlg = new ColorDialog(getShell());
         dlg.setRGB(colorLabel.getBackground().getRGB());
         dlg.setText("Choose a Color");
         RGB rgb = dlg.open();
         if (rgb != null) {
            newColor = SWTResourceManager.getColor(rgb);
            colorLabel.setBackground(newColor);
         }
      }));

      Button btnAdd = new Button(gAdd, SWT.NONE);
      btnAdd.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
      btnAdd.setText("Add");
      btnAdd.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         log.debug("Add selected");
         String name = newName.getText().trim();
         if (name.isEmpty()) {
            MessageDialog.openInformation(getShell(), "Missing Name", "Please first enter a name for the session type");
            return;
         }

         // Check for duplicates
         for (SessionType st : sessionTypes) {
            if (st.getName().equalsIgnoreCase(name)) {
               MessageDialog.openError(getShell(), "Duplicate Name", "A session type with this name already exist");
               return;
            }

         }

         // showAddEditDialog(variableTableViewer, variableKindSelected, name, null);

      }));

      stTableViewer.setContentProvider(ArrayContentProvider.getInstance());
      stTableViewer.setInput(sessionTypes);

      return composite;

   }

   @Override
   public boolean performOk() {
      saveValues();
      return true;
   }

   @Override
   protected void performApply() {
      saveValues();
   }

   @Override
   protected void performDefaults() {
      showSystemObject.setSelection(ps.getDefaultBoolean(Constants.PREF_SHOW_SYSTEM_OBJECTS));
   }

   // -------
   // Helpers
   // -------
   private void saveValues() {
      ps.setValue(Constants.PREF_SHOW_SYSTEM_OBJECTS, showSystemObject.getSelection());

      // Save the preferences
      try {
         ((JTBPreferenceStore) getPreferenceStore()).save();
      } catch (IOException e) {
         String msg = "Exception occurred when saving preferences";
         log.error(msg, e);
         jtbStatusReporter.showError(msg, Utils.getCause(e), e.getMessage());
      }
   }

   private void clearButtonCache() {
      for (Button b : buttons.values()) {
         b.dispose();
      }
      buttons.clear();
   }
}
