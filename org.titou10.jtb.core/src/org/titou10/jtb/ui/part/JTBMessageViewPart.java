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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.FormatUtils;
import org.titou10.jtb.util.Utils;

/**
 * Display the content of the first selected Message
 * 
 * @author Denis Forveille
 * 
 */
public class JTBMessageViewPart {

   private static final Logger log = LoggerFactory.getLogger(JTBMessageViewPart.class);

   private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

   private Shell             shell;
   private JTBStatusReporter jtbStatusReporter;

   private TabFolder   tabFolder;
   private TableViewer tableJMSHeadersViewer;
   private TableViewer tablePropertiesViewer;
   private TableColumn colHeader;
   private TableColumn colValue;
   private TableColumn colHeader2;
   private TableColumn colValue2;

   private Text  txtToString;
   private Text  txtPayloadRaw;
   private Text  txtPayloadXML;
   private Table tableProperties;
   private Table tableJMSHeaders;

   private TabItem tabPayloadRaw;
   private TabItem tabPayloadXML;
   private TabItem tabPayloadBinary;

   @PostConstruct
   public void postConstruct(final Composite parent,
                             EMenuService menuService,
                             final ESelectionService selectionService,
                             JTBStatusReporter jtbStatusReporter) {
      this.shell = parent.getShell();

      parent.setLayout(new FillLayout(SWT.HORIZONTAL));

      tabFolder = new TabFolder(parent, SWT.NONE);

      // Overview
      TabItem tabToString = new TabItem(tabFolder, SWT.NONE);
      tabToString.setText("Overview");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      tabToString.setControl(composite);
      composite.setLayout(new FillLayout(SWT.HORIZONTAL));

      txtToString = new Text(composite, SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
      txtToString.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
      txtToString.setFont(SWTResourceManager.getFont("Courier New", 9, SWT.NORMAL));

      // JMS Headers
      TabItem tabJMSHeaders = new TabItem(tabFolder, SWT.NONE);
      tabJMSHeaders.setText("JMS Headers");

      Composite composite_2 = new Composite(tabFolder, SWT.NONE);
      tabJMSHeaders.setControl(composite_2);
      composite_2.setLayout(new FillLayout(SWT.HORIZONTAL));

      tableJMSHeadersViewer = new TableViewer(composite_2, SWT.BORDER | SWT.FULL_SELECTION);
      tableJMSHeaders = tableJMSHeadersViewer.getTable();
      tableJMSHeaders.setLinesVisible(true);
      tableJMSHeaders.setHeaderVisible(true);

      TableViewerColumn colHeaderViewer = new TableViewerColumn(tableJMSHeadersViewer, SWT.NONE);
      colHeader = colHeaderViewer.getColumn();
      colHeader.setWidth(100);
      colHeader.setText("Header");

      TableViewerColumn colValueViewer = new TableViewerColumn(tableJMSHeadersViewer, SWT.NONE);
      colValue = colValueViewer.getColumn();
      colValue.setWidth(100);
      colValue.setText("Value");

      // Properties
      TabItem tabProperties = new TabItem(tabFolder, SWT.NONE);
      tabProperties.setText("Properties");

      Composite composite_4 = new Composite(tabFolder, SWT.NONE);
      tabProperties.setControl(composite_4);
      composite_4.setLayout(new FillLayout(SWT.HORIZONTAL));

      tablePropertiesViewer = new TableViewer(composite_4, SWT.BORDER | SWT.FULL_SELECTION);
      tableProperties = tablePropertiesViewer.getTable();
      tableProperties.setLinesVisible(true);
      tableProperties.setHeaderVisible(true);

      TableViewerColumn colHeader2Viewer = new TableViewerColumn(tablePropertiesViewer, SWT.NONE);
      colHeader2 = colHeader2Viewer.getColumn();
      colHeader2.setWidth(100);
      colHeader2.setText("Header");

      TableViewerColumn colValue2Viewer = new TableViewerColumn(tablePropertiesViewer, SWT.NONE);
      colValue2 = colValue2Viewer.getColumn();
      colValue2.setWidth(100);
      colValue2.setText("Value");

      // Manage selections
      if (selectionService != null) {
         tableJMSHeadersViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
               IStructuredSelection sel = (IStructuredSelection) event.getSelection();
               selectionService.setSelection(sel.getFirstElement());
            }
         });
         tablePropertiesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
               IStructuredSelection sel = (IStructuredSelection) event.getSelection();
               selectionService.setSelection(sel.getFirstElement());
            }
         });

         // Attach the Popup Menus
         menuService.registerContextMenu(tableJMSHeadersViewer.getTable(), Constants.MESSAGE_VIEW_POPUP_MENU);
         menuService.registerContextMenu(tablePropertiesViewer.getTable(), Constants.MESSAGE_VIEW_POPUP_MENU);
      }
   }

   @Inject
   @Optional
   public void getNotified(@UIEventTopic(Constants.EVENT_REFRESH_JTBMESSAGE_PART) JTBMessage jtbMessage) {

      log.debug("JTBMessageViewPart refresh for {}", jtbMessage);

      try {
         populateFields(shell, jtbMessage);
      } catch (JMSException e) {
         jtbStatusReporter.showError("Problem while showing Message", e, "");
         return;
      }
   }

   // -------
   // Helpers
   // -------
   private void populateFields(final Shell shell, JTBMessage jtbMessage) throws JMSException {

      // Populate fields
      Message m = jtbMessage.getJmsMessage();

      Map<String, Object> headers = new LinkedHashMap<>();
      Map<String, Object> properties = new TreeMap<>();

      String jmsTimestamp;
      if (m.getJMSTimestamp() == 0) {
         jmsTimestamp = "";
      } else {
         Date x = new Date(m.getJMSTimestamp());
         jmsTimestamp = SDF.format(x);
      }

      // JMS Headers
      headers.put("JMSCorrelationID", m.getJMSCorrelationID());
      headers.put("JMSMessageID", m.getJMSMessageID());
      headers.put("JMSType", m.getJMSType());
      headers.put("JMSDeliveryMode", m.getJMSDeliveryMode());
      headers.put("JMSDestination", m.getJMSDestination());
      headers.put("JMSExpiration", m.getJMSExpiration());
      headers.put("JMSPriority", m.getJMSPriority());
      headers.put("JMSRedelivered", m.getJMSRedelivered());
      headers.put("JMSReplyTo", m.getJMSReplyTo());
      headers.put("JMSTimestamp", jmsTimestamp);

      // Properties
      Enumeration<?> e = m.getPropertyNames();
      while (e.hasMoreElements()) {
         String cle = (String) e.nextElement();
         properties.put(cle, String.valueOf(m.getObjectProperty(cle)));
      }

      // toString
      txtToString.setText(m.toString());

      // Payload tabs
      switch (jtbMessage.getJtbMessageType()) {
         case TEXT:
            if (tabPayloadRaw == null) {
               tabPayloadRaw = new TabItem(tabFolder, SWT.NONE);
               tabPayloadRaw.setText("Payload (Raw)");

               Composite composite_3 = new Composite(tabFolder, SWT.NONE);
               tabPayloadRaw.setControl(composite_3);
               composite_3.setLayout(new FillLayout(SWT.HORIZONTAL));

               txtPayloadRaw = new Text(composite_3, SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
               txtPayloadRaw.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            }

            if (tabPayloadXML == null) {
               tabPayloadXML = new TabItem(tabFolder, SWT.NONE);
               tabPayloadXML.setText("Payload (XML)");

               Composite composite_1 = new Composite(tabFolder, SWT.NONE);
               tabPayloadXML.setControl(composite_1);
               composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));

               txtPayloadXML = new Text(composite_1, SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
               txtPayloadXML.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            }
            if (tabPayloadBinary != null) {
               tabPayloadBinary.dispose();
               tabPayloadBinary = null;
            }

            // Populate Fields
            TextMessage tm = (TextMessage) m;
            String txt = tm.getText();
            if (tm.getText() != null) {
               txtPayloadRaw.setText(txt);
               txtPayloadXML.setText(FormatUtils.xmlPrettyFormat(txt, false));
            }
            break;

         case BYTES:
            final BytesMessage bm = (BytesMessage) m;

            // Construct proposed file name for the payload

            final String finalDestName = jtbMessage.getJtbDestination().getName();

            if (tabPayloadRaw != null) {
               tabPayloadRaw.dispose();
               tabPayloadRaw = null;
            }
            if (tabPayloadXML != null) {
               tabPayloadXML.dispose();
               tabPayloadXML = null;
            }
            if (tabPayloadBinary != null) {
               tabPayloadBinary.dispose();
            }

            tabPayloadBinary = new TabItem(tabFolder, SWT.NONE);
            tabPayloadBinary.setText("Payload (Binary)");

            Composite composite_5 = new Composite(tabFolder, SWT.NONE);
            composite_5.setLayout(new RowLayout());
            composite_5.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            tabPayloadBinary.setControl(composite_5);

            Button btnSaveBinary = new Button(composite_5, SWT.NONE);
            btnSaveBinary.setText("Save payload as...");
            btnSaveBinary.addSelectionListener(new SelectionAdapter() {
               @Override
               public void widgetSelected(SelectionEvent e) {

                  log.debug("zipFileName={}", finalDestName);

                  try {
                     Utils.exportPayload(shell, finalDestName, bm);
                  } catch (IOException | JMSException e1) {
                     jtbStatusReporter.showError("An exception occurred while exporting payload", e1, "");
                     return;
                  }
               }
            });
            break;

         case MAP:
         case MESSAGE:
         case OBJECT:
         case STREAM:
            if (tabPayloadRaw != null) {
               tabPayloadRaw.dispose();
               tabPayloadRaw = null;
            }
            if (tabPayloadXML != null) {
               tabPayloadXML.dispose();
               tabPayloadXML = null;
            }
            if (tabPayloadBinary != null) {
               tabPayloadBinary.dispose();
            }
            break;
      }

      // Set Content
      tableJMSHeadersViewer.setLabelProvider(new MyTableLabelProvider());
      tableJMSHeadersViewer.setContentProvider(ArrayContentProvider.getInstance());
      tableJMSHeadersViewer.setInput(headers.entrySet());

      tablePropertiesViewer.setLabelProvider(new MyTableLabelProvider());
      tablePropertiesViewer.setContentProvider(ArrayContentProvider.getInstance());
      tablePropertiesViewer.setInput(properties.entrySet());

      colHeader.pack();
      colHeader2.pack();
      colValue.pack();
      colValue2.pack();

   }

   private class MyTableLabelProvider implements ITableLabelProvider {

      @Override
      @SuppressWarnings("unchecked")
      public String getColumnText(Object element, int columnIndex) {
         Map.Entry<String, Object> e = (Map.Entry<String, Object>) element;
         if (columnIndex == 0) {
            return e.getKey();
         } else {
            if (e.getValue() != null) {
               return e.getValue().toString();
            } else {
               return null;
            }
         }
      }

      @Override
      public Image getColumnImage(Object arg0, int arg1) {
         return null;
      }

      @Override
      public void addListener(ILabelProviderListener arg0) {
         // NOP
      }

      @Override
      public void dispose() {
         // NOP
      }

      @Override
      public boolean isLabelProperty(Object arg0, String arg1) {
         return false;
      }

      @Override
      public void removeListener(ILabelProviderListener arg0) {
         // NOP
      }
   }
}
