/*
 * Copyright (C) 2018 Denis Forveille titou10.titou10@gmail.com
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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.sessiontype.SessionType;
import org.titou10.jtb.sessiontype.SessionTypeManager;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Utils;

/**
 * "General" page in the preference dialog
 * 
 * @author Denis Forveille
 *
 */
final class PageSessionType extends PreferencePage {

   private static final Logger log = LoggerFactory.getLogger(PageSessionType.class);

   private static int          N   = 1;

   private JTBStatusReporter   jtbStatusReporter;
   private SessionTypeManager  sessionTypeManager;

   private List<SessionType>   sessionTypes;

   private TableViewer         stTableViewer;

   private SessionType         currentSessionType;
   private Text                txtCurrentName;
   private ColorSelector       btnColorSelector;

   public PageSessionType(JTBStatusReporter jtbStatusReporter, SessionTypeManager sessionTypeManager) {
      super("Session Types");
      this.jtbStatusReporter = jtbStatusReporter;
      this.sessionTypeManager = sessionTypeManager;
   }

   @Override
   protected Control createContents(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      composite.setLayout(new GridLayout(1, false));

      sessionTypes = new ArrayList<>(sessionTypeManager.getSessionTypes());

      // Session Types

      Group gSessionTypes = new Group(composite, SWT.SHADOW_ETCHED_IN);
      gSessionTypes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      gSessionTypes.setText("Session Types");
      gSessionTypes.setLayout(new GridLayout(3, false));

      // Table with Session Types

      Composite compositeList = new Composite(gSessionTypes, SWT.NONE);
      compositeList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      TableColumnLayout tcListComposite = new TableColumnLayout();
      compositeList.setLayout(tcListComposite);

      stTableViewer = new TableViewer(compositeList, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      Table stTable = stTableViewer.getTable();
      stTable.setBackgroundMode(SWT.INHERIT_FORCE);
      stTable.setHeaderVisible(true);
      stTable.setLinesVisible(true);

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

         @Override
         public Color getBackground(Object element) {
            SessionType st = (SessionType) element;
            return st.getColor();
         }

      });

      // Add a Selection Listener on list items
      stTableViewer.addSelectionChangedListener((event) -> {
         IStructuredSelection selection = (IStructuredSelection) event.getSelection();
         if (selection.isEmpty()) {
            currentSessionType = null;
            txtCurrentName.setText("");
            btnColorSelector.setColorValue(SessionTypeManager.DEFAULT_COLOR.getRGB());
         } else {
            currentSessionType = (SessionType) selection.getFirstElement();
            txtCurrentName.setText(currentSessionType.getName());
            btnColorSelector.setColorValue(currentSessionType.getColor().getRGB());
         }
      });

      // Capture the delete key

      stTable.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
         if (e.keyCode == SWT.DEL) {
            IStructuredSelection selection = (IStructuredSelection) stTableViewer.getSelection();
            if (selection.isEmpty()) {
               return;
            }
            for (Object sel2 : selection.toList()) {
               SessionType st = (SessionType) sel2;
               log.debug("Remove Session Type '{}'", st.getName());
               sessionTypes.remove(st);
            }
            currentSessionType = null;
            txtCurrentName.setText("");
            btnColorSelector.setColorValue(SessionTypeManager.DEFAULT_COLOR.getRGB());
            stTableViewer.refresh();
         }
      }));

      // Add

      Button btnAdd = new Button(composite, SWT.NONE);
      btnAdd.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
      btnAdd.setText("Add new session type");
      btnAdd.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         log.debug("Add new ST selected");

         Color newColor = SessionTypeManager.DEFAULT_COLOR;

         String baseName = "Type " + N++;

         currentSessionType = new SessionType(baseName, newColor);
         sessionTypes.add(currentSessionType);
         stTableViewer.refresh();

         txtCurrentName.setText(currentSessionType.getName());
         btnColorSelector.setColorValue(currentSessionType.getColor().getRGB());
      }));

      // Update type

      Group gAdd = new Group(composite, SWT.SHADOW_ETCHED_IN);
      gAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      gAdd.setText("Session Type");
      gAdd.setLayout(new GridLayout(3, false));

      Label lblName = new Label(gAdd, SWT.NONE);
      lblName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
      lblName.setText("Name");
      txtCurrentName = new Text(gAdd, SWT.BORDER);
      txtCurrentName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
      txtCurrentName.addModifyListener(new ModifyListener() {
         @Override
         public void modifyText(ModifyEvent e) {
            if (currentSessionType != null) {
               currentSessionType.setName(txtCurrentName.getText());
               stTableViewer.refresh();
            }
         }
      });

      Label lblColor = new Label(gAdd, SWT.NONE);
      lblColor.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
      lblColor.setText("Color");

      btnColorSelector = new ColorSelector(gAdd);
      btnColorSelector.addListener(new IPropertyChangeListener() {
         @Override
         public void propertyChange(PropertyChangeEvent event) {
            if (currentSessionType != null) {
               currentSessionType.setColor(SWTResourceManager.getColor(btnColorSelector.getColorValue()));
               stTableViewer.refresh();
            }
         }
      });

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
      sessionTypes = new ArrayList<>(sessionTypeManager.getStandardSessionTypes());
      stTableViewer.setInput(sessionTypes);
      stTableViewer.refresh();
   }

   // -------
   // Helpers
   // -------
   private void saveValues() {
      // Page is lazily loaded, so components may be null if the page has not been visited
      if (txtCurrentName == null) {
         return;
      }

      try {
         sessionTypeManager.saveValues(sessionTypes);
      } catch (IOException e) {
         String msg = "Exception occurred when saving preferences";
         log.error(msg, e);
         jtbStatusReporter.showError(msg, Utils.getCause(e), e.getMessage());
      }
   }
}
