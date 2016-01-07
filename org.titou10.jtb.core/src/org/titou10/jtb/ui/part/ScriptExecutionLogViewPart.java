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
package org.titou10.jtb.ui.part;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.script.ScriptStepResult;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.DNDData;

/**
 * Display the execution log of a Script
 * 
 * @author Denis Forveille
 * 
 */
@SuppressWarnings("restriction")
public class ScriptExecutionLogViewPart {

   private static final Logger           log     = LoggerFactory.getLogger(ScriptExecutionLogViewPart.class);

   private static final SimpleDateFormat SDF     = new SimpleDateFormat("HH:mm:ss.SSS");

   @Inject
   private ECommandService               commandService;

   @Inject
   private EHandlerService               handlerService;

   @Inject
   private EMenuService                  menuService;

   // JFaces components
   private Composite                     compositeLog;
   private TableViewer                   tableViewer;
   private Table                         logTable;

   // Business Data
   private List<ScriptStepResult>        logExecution;

   private Map<Object, Button>           buttons = new HashMap<Object, Button>();

   @Inject
   @Optional
   public void getNotified(@UIEventTopic(Constants.EVENT_REFRESH_EXECUTION_LOG) ScriptStepResult ssr) {
      log.debug("ScriptExecutionLogViewPart refresh");

      logExecution.add(ssr);
      tableViewer.refresh();
      compositeLog.layout();
   }

   @Inject
   @Optional
   public void clearLogs(@UIEventTopic(Constants.EVENT_CLEAR_EXECUTION_LOG) String noUse) {
      log.debug("clearLogs");

      logExecution.clear();

      // Dispose buttons
      if ((logTable != null) && (logTable.getChildren() != null)) {
         for (Control item : logTable.getChildren()) {
            if ((item != null) && (!item.isDisposed())) {
               item.dispose();
            }
         }
      }
      buttons = new HashMap<Object, Button>();

      tableViewer.refresh();
      compositeLog.layout();

   }

   @PostConstruct
   public void postConstruct(Shell shell, final Composite parent) {

      // Log
      compositeLog = new Composite(parent, SWT.NONE);
      compositeLog.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      TableColumnLayout tcl = new TableColumnLayout();
      compositeLog.setLayout(tcl);

      tableViewer = new TableViewer(compositeLog, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      logTable = tableViewer.getTable();
      logTable.setHeaderVisible(true);
      logTable.setLinesVisible(true);

      TableViewerColumn logTSColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn logTSHeader = logTSColumn.getColumn();
      tcl.setColumnData(logTSHeader, new ColumnPixelData(100, true, true));
      logTSHeader.setAlignment(SWT.CENTER);
      logTSHeader.setText("Time");
      logTSColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            ScriptStepResult r = (ScriptStepResult) element;
            return SDF.format(r.getTs().getTime());
         }
      });

      TableViewerColumn logActionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn logActionHeader = logActionColumn.getColumn();
      tcl.setColumnData(logActionHeader, new ColumnPixelData(100, true, true));
      logActionHeader.setText("Source");
      logActionColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            ScriptStepResult r = (ScriptStepResult) element;
            return r.getAction().name();
         }
      });

      TableViewerColumn resultColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn resultHeader = resultColumn.getColumn();
      tcl.setColumnData(resultHeader, new ColumnPixelData(100, true, true));
      resultHeader.setText("Result");
      resultColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            ScriptStepResult r = (ScriptStepResult) element;
            return r.getReturnCode().name();
         }

         @Override
         public void update(ViewerCell cell) {
            super.update(cell);
            ScriptStepResult r = (ScriptStepResult) cell.getElement();
            switch (r.getReturnCode()) {
               case SUCCESS:
                  cell.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
                  break;

               case FAILED:
                  cell.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
                  break;

               case CANCELLED:
                  cell.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
                  break;

               default:
                  break;
            }
         }
      });

      TableViewerColumn dataColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn dataHeader = dataColumn.getColumn();
      tcl.setColumnData(dataHeader, new ColumnWeightData(4, ColumnWeightData.MINIMUM_WIDTH, true));
      dataHeader.setText("Information");
      dataColumn.setLabelProvider(new LogDataColumnProvider());

      // Attach Popup Menu
      menuService.registerContextMenu(logTable, Constants.EXECUTION_LOG_POPUP_MENU);

      logExecution = new ArrayList<>();
      tableViewer.setContentProvider(ArrayContentProvider.getInstance());
      tableViewer.setInput(logExecution);
   }

   // ------
   // Helper
   // ------
   private class LogDataColumnProvider extends ColumnLabelProvider {

      @Override
      public String getText(Object element) {
         ScriptStepResult r = (ScriptStepResult) element;

         if (r.getData() != null) {
            if (r.getData() instanceof JTBMessageTemplate) {
               return "";
            } else {
               return r.getData().toString();
            }
         }

         return "";
      }

      @Override
      public void update(ViewerCell cell) {

         ScriptStepResult r = (ScriptStepResult) cell.getElement();
         if ((r.getData() == null) || (!(r.getData() instanceof JTBMessageTemplate))) {
            super.update(cell);
            return;
         }

         // This a JTBMessageTemplate ..

         // If the button has already been created, exit
         Object key = cell.getElement();
         if (buttons.containsKey(key)) {
            return;
         }

         // Create the view button
         final JTBMessageTemplate jtbMessageTemplate = (JTBMessageTemplate) r.getData();

         Composite parentComposite = (Composite) cell.getViewerRow().getControl();
         Color parentColor = parentComposite.getBackground();

         RowLayout rl = new RowLayout(SWT.HORIZONTAL);
         rl.wrap = false;
         rl.marginBottom = 0;
         rl.marginLeft = 0;
         rl.marginTop = 0;
         rl.spacing = 5;

         Composite c = new Composite(parentComposite, SWT.NONE);
         c.setLayout(rl);
         c.setBackground(parentColor);

         Button btnViewMessage = new Button(c, SWT.NONE);
         btnViewMessage.setText("View Message");
         btnViewMessage.setLayoutData(new RowData(SWT.DEFAULT, 20)); // TODO Hard Coded...
         btnViewMessage.pack();
         btnViewMessage.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {

               // Set "Active" selection
               DNDData.setSelectedJTBMessageTemplate(jtbMessageTemplate);

               // Call Template "Add or Edit" Command
               Map<String, Object> parameters = new HashMap<>();
               parameters.put(Constants.COMMAND_TEMPLATE_ADDEDIT_PARAM, Constants.COMMAND_TEMPLATE_ADDEDIT_EDIT_SCRIPT);
               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_TEMPLATE_ADDEDIT, parameters);
               handlerService.executeHandler(myCommand);
            }
         });

         buttons.put(cell.getElement(), btnViewMessage);

         Label l = new Label(c, SWT.NONE);
         l.setBackground(parentColor);
         if (r.getTemplateName() != null) {
            StringBuilder sb = new StringBuilder(64);
            sb.append(" Generated from template '");
            sb.append(r.getTemplateName());
            sb.append("'");
            l.setText(sb.toString());
         }

         c.pack();

         TableItem item = (TableItem) cell.getItem();
         TableEditor editor = new TableEditor(item.getParent());
         editor.horizontalAlignment = SWT.LEFT;
         editor.grabHorizontal = true;

         editor.setEditor(c, item, cell.getColumnIndex());

      }
   }
}
