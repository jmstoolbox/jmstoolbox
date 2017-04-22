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
package org.titou10.jtb.ui.part;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.jms.util.JTBDeliveryMode;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.hex.BytesDataProvider;
import org.titou10.jtb.ui.hex.HexViewer;
import org.titou10.jtb.ui.hex.IDataProvider;
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

   private static final Logger           log = LoggerFactory.getLogger(JTBMessageViewPart.class);

   private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

   private static final String           CR  = "\n";

   private Shell                         shell;
   private JTBStatusReporter             jtbStatusReporter;

   private TabFolder                     tabFolder;
   private TableViewer                   tableJMSHeadersViewer;
   private TableViewer                   tablePropertiesViewer;
   private TableColumn                   colHeader;
   private TableColumn                   colValue;
   private TableColumn                   colHeader2;
   private TableColumn                   colValue2;

   private Text                          txtToString;
   private Text                          txtPayloadText;
   private Text                          txtPayloadXML;
   private HexViewer                     hvPayLoadHex;
   private TableViewer                   tvPayloadMap;
   private Table                         tableProperties;
   private Table                         tableJMSHeaders;

   private TabItem                       tabOverview;
   private TabItem                       tabJMSHeaders;
   private TabItem                       tabProperties;
   private TabItem                       tabPayloadText;
   private TabItem                       tabPayloadXML;
   private TabItem                       tabPayloadHex;
   private TabItem                       tabPayloadMap;

   @Inject
   private ConfigManager                 cm;

   private JTBMessage                    currentJtbMessage;

   @PostConstruct
   public void postConstruct(final Composite parent,
                             EMenuService menuService,
                             final ESelectionService selectionService,
                             JTBStatusReporter jtbStatusReporter) {
      this.shell = parent.getShell();

      parent.setLayout(new FillLayout(SWT.HORIZONTAL));

      tabFolder = new TabFolder(parent, SWT.NONE);

      // Overview
      tabOverview = new TabItem(tabFolder, SWT.NONE);
      tabOverview.setText("Overview");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      tabOverview.setControl(composite);
      composite.setLayout(new FillLayout(SWT.HORIZONTAL));

      txtToString = new Text(composite, SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
      txtToString.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
      txtToString.setFont(SWTResourceManager.getFont("Courier New", 9, SWT.NORMAL));

      // JMS Headers
      tabJMSHeaders = new TabItem(tabFolder, SWT.NONE);
      tabJMSHeaders.setText("JMS Headers");

      Composite composite2 = new Composite(tabFolder, SWT.NONE);
      tabJMSHeaders.setControl(composite2);
      TableColumnLayout tclComposite2 = new TableColumnLayout();
      composite2.setLayout(tclComposite2);

      tableJMSHeadersViewer = new TableViewer(composite2, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      tableJMSHeaders = tableJMSHeadersViewer.getTable();
      tableJMSHeaders.setLinesVisible(true);
      tableJMSHeaders.setHeaderVisible(true);

      TableViewerColumn colHeaderViewer = new TableViewerColumn(tableJMSHeadersViewer, SWT.NONE);
      tclComposite2.setColumnData(colHeaderViewer.getColumn(), new ColumnWeightData(2, 150, true));
      colHeader = colHeaderViewer.getColumn();
      colHeader.setText("Header");

      TableViewerColumn colValueViewer = new TableViewerColumn(tableJMSHeadersViewer, SWT.NONE);
      tclComposite2.setColumnData(colValueViewer.getColumn(), new ColumnWeightData(3, 150, true));
      colValue = colValueViewer.getColumn();
      colValue.setText("Value");

      // Properties
      tabProperties = new TabItem(tabFolder, SWT.NONE);
      tabProperties.setText("Properties");

      Composite composite4 = new Composite(tabFolder, SWT.NONE);
      tabProperties.setControl(composite4);
      TableColumnLayout tclComposite4 = new TableColumnLayout();
      composite4.setLayout(tclComposite4);

      tablePropertiesViewer = new TableViewer(composite4, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      tableProperties = tablePropertiesViewer.getTable();
      tableProperties.setLinesVisible(true);
      tableProperties.setHeaderVisible(true);

      TableViewerColumn colHeader2Viewer = new TableViewerColumn(tablePropertiesViewer, SWT.NONE);
      tclComposite4.setColumnData(colHeader2Viewer.getColumn(), new ColumnWeightData(2, 150, true));
      colHeader2 = colHeader2Viewer.getColumn();
      colHeader2.setText("Header");

      TableViewerColumn colValue2Viewer = new TableViewerColumn(tablePropertiesViewer, SWT.NONE);
      tclComposite4.setColumnData(colValue2Viewer.getColumn(), new ColumnWeightData(3, 150, true));
      colValue2 = colValue2Viewer.getColumn();
      colValue2.setText("Value");

      // Manage selections
      if (selectionService != null) {
         tableJMSHeadersViewer.addSelectionChangedListener((event) -> {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            selectionService.setSelection(sel.toList());
         });
         tablePropertiesViewer.addSelectionChangedListener((event) -> {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            selectionService.setSelection(sel.toList());
         });

         // Attach the Popup Menus
         menuService.registerContextMenu(tableJMSHeadersViewer.getTable(), Constants.MESSAGE_VIEW_POPUP_MENU);
         menuService.registerContextMenu(tablePropertiesViewer.getTable(), Constants.MESSAGE_VIEW_POPUP_MENU);
      }

      tableJMSHeaders.addKeyListener(new KeyAdapter() {

         @SuppressWarnings("unchecked")
         @Override
         public void keyPressed(KeyEvent e) {

            // Select all JMS Headers
            if ((e.stateMask == SWT.MOD1) && (e.keyCode == 'a')) {
               Table t = (Table) e.widget;
               t.selectAll();
               t.notifyListeners(SWT.Selection, null);
               return;
            }

            // Copy Map to Clipboard (CTRL+C)
            if (((e.stateMask & SWT.MOD1) != 0) && (e.keyCode == 'c')) {
               IStructuredSelection selection = (IStructuredSelection) tableJMSHeadersViewer.getSelection();
               if (selection.isEmpty()) {
                  return;
               }
               StringBuilder sb = new StringBuilder(256);
               for (Object sel : selection.toList()) {
                  Map.Entry<String, Object> en = (Map.Entry<String, Object>) sel;
                  sb.append(en.getKey());
                  sb.append("=");
                  sb.append(en.getValue());
                  sb.append("\r");
               }
               Clipboard cb = new Clipboard(Display.getDefault());
               TextTransfer textTransfer = TextTransfer.getInstance();
               cb.setContents(new Object[] { sb.toString() }, new Transfer[] { textTransfer });
               return;
            }
         }
      });

      tableProperties.addKeyListener(new KeyAdapter() {
         @SuppressWarnings("unchecked")
         @Override
         public void keyPressed(KeyEvent e) {

            // Select all Properties
            if ((e.stateMask == SWT.MOD1) && (e.keyCode == 'a')) {
               Table t = (Table) e.widget;
               t.selectAll();
               t.notifyListeners(SWT.Selection, null);
               return;
            }

            // Copy Map to Clipboard (CTRL+C)
            if (((e.stateMask & SWT.MOD1) != 0) && (e.keyCode == 'c')) {
               IStructuredSelection selection = (IStructuredSelection) tablePropertiesViewer.getSelection();
               if (selection.isEmpty()) {
                  return;
               }
               StringBuilder sb = new StringBuilder(256);
               for (Object sel : selection.toList()) {
                  Map.Entry<String, Object> en = (Map.Entry<String, Object>) sel;
                  sb.append(en.getKey());
                  sb.append("=");
                  sb.append(en.getValue());
                  sb.append("\r");
               }
               Clipboard cb = new Clipboard(Display.getDefault());
               TextTransfer textTransfer = TextTransfer.getInstance();
               cb.setContents(new Object[] { sb.toString() }, new Transfer[] { textTransfer });
               return;
            }
         }
      });

      // Label/Content providers
      tableJMSHeadersViewer.setLabelProvider(new MyTableLabelProvider());
      tableJMSHeadersViewer.setContentProvider(ArrayContentProvider.getInstance());

      tablePropertiesViewer.setLabelProvider(new MyTableLabelProvider());
      tablePropertiesViewer.setContentProvider(ArrayContentProvider.getInstance());

   }

   @Inject
   @Optional
   public void refreshMessage(@UIEventTopic(Constants.EVENT_JTBMESSAGE_PART_REFRESH) JTBMessage jtbMessage) {
      // log.debug("JTBMessageViewPart refresh for {}", jtbMessage);

      // Same message being displayed, fast exit
      if (this.currentJtbMessage == jtbMessage) {
         return;
      }

      this.currentJtbMessage = jtbMessage;

      // Message is null, clear the part and exit
      if (jtbMessage == null) {
         cleanTabs(true, true, true, true);
         tableJMSHeadersViewer.setInput(null);
         tablePropertiesViewer.setInput(null);
         return;
      }

      // OK, time to populate the part
      try {
         populateFields(shell, jtbMessage);
         tableJMSHeadersViewer.getTable().deselectAll();
         tablePropertiesViewer.getTable().deselectAll();
      } catch (JMSException e) {
         jtbStatusReporter.showError("Problem while showing Message", e, "");
      }

      Utils.resizeTableViewer(tableJMSHeadersViewer);
      Utils.resizeTableViewer(tablePropertiesViewer);
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

      String jmsDeliveryTime = null;
      try {
         if (m.getJMSDeliveryTime() == 0) {
            jmsDeliveryTime = "";
         } else {
            Date x = new Date(m.getJMSDeliveryTime());
            jmsDeliveryTime = SDF.format(x);
         }
      } catch (Throwable t) {
         // JMS 2.0+
      }

      String jmsExpiration;
      if (m.getJMSExpiration() == 0) {
         jmsExpiration = "";
      } else {
         Date x = new Date(m.getJMSExpiration());
         jmsExpiration = SDF.format(x);
      }

      StringBuilder deliveryMode = new StringBuilder(32);
      deliveryMode.append(JTBDeliveryMode.fromValue(m.getJMSDeliveryMode()).name());
      deliveryMode.append(" (");
      deliveryMode.append(m.getJMSDeliveryMode());
      deliveryMode.append(")");

      // JMS Headers
      headers.put("JMSCorrelationID", m.getJMSCorrelationID());
      headers.put("JMSMessageID", m.getJMSMessageID());
      headers.put("JMSType", m.getJMSType());
      headers.put("JMSDeliveryMode", deliveryMode.toString());
      headers.put("JMSDestination", m.getJMSDestination());
      if (jmsDeliveryTime != null) {
         headers.put("JMSDeliveryTime", jmsDeliveryTime);
      }
      headers.put("JMSExpiration", jmsExpiration);
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
            cleanTabs(false, false, true, true);

            if (tabPayloadText == null) {
               tabPayloadText = new TabItem(tabFolder, SWT.NONE);

               Composite composite3 = new Composite(tabFolder, SWT.NONE);
               tabPayloadText.setControl(composite3);
               composite3.setLayout(new FillLayout(SWT.HORIZONTAL));

               // DF SWT.WRAP slows down A LOT UI for long text Messages (> 1K)
               // txtPayloadRaw = new Text(composite3, SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
               txtPayloadText = new Text(composite3, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
               txtPayloadText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

               // Add key binding for CTRL-a -> select all
               txtPayloadText.addListener(SWT.KeyUp, new Listener() {
                  public void handleEvent(Event event) {
                     if (event.stateMask == SWT.MOD1 && event.keyCode == 'a') {
                        ((Text) event.widget).selectAll();
                     }
                  }
               });
            }

            if (tabPayloadXML == null) {
               tabPayloadXML = new TabItem(tabFolder, SWT.NONE);
               tabPayloadXML.setText("Payload (XML)");

               Composite composite1 = new Composite(tabFolder, SWT.NONE);
               tabPayloadXML.setControl(composite1);
               composite1.setLayout(new FillLayout(SWT.HORIZONTAL));

               // DF SWT.WRAP slows down A LOT UI for long text Messages (> 1K)
               // txtPayloadXML = new Text(composite_1, SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
               txtPayloadXML = new Text(composite1, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
               txtPayloadXML.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
               // Add key binding for CTRL-a -> select all
               txtPayloadXML.addListener(SWT.KeyUp, new Listener() {

                  public void handleEvent(Event event) {
                     if (event.stateMask == SWT.MOD1 && event.keyCode == 'a') {
                        ((Text) event.widget).selectAll();
                     }
                  }
               });
            }

            // Populate Fields
            TextMessage tm = (TextMessage) m;
            String txt = tm.getText();
            if (txt != null) {
               txtPayloadText.setText(txt);
               txtPayloadXML.setText(FormatUtils.xmlPrettyFormat(cm.getPreferenceStore(), txt, false));
               tabPayloadText.setText(String.format(Constants.PAYLOAD_TEXT_TITLE, txt.length()));
            } else {
               tabPayloadText.setText(Constants.PAYLOAD_TEXT_TITLE_NULL);
            }

            break;

         case BYTES:

            cleanTabs(true, true, false, true);

            final BytesMessage bm = (BytesMessage) m;

            if (tabPayloadHex == null) {
               tabPayloadHex = new TabItem(tabFolder, SWT.NONE);

               Composite composite51 = new Composite(tabFolder, SWT.NONE);
               composite51.setLayout(new FillLayout(SWT.HORIZONTAL));
               tabPayloadHex.setControl(composite51);

               hvPayLoadHex = new HexViewer(composite51, SWT.READ_ONLY, null, 16);
               hvPayLoadHex.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            }

            byte[] payloadBytes = new byte[(int) bm.getBodyLength()];
            bm.reset();
            bm.readBytes(payloadBytes);
            IDataProvider idp = new BytesDataProvider(payloadBytes);
            hvPayLoadHex.setDataProvider(idp);

            tabPayloadHex.setText(String.format(Constants.PAYLOAD_BYTES_TITLE, payloadBytes.length));

            break;

         case MAP:

            cleanTabs(true, true, true, false);

            if (tabPayloadMap == null) {
               tabPayloadMap = new TabItem(tabFolder, SWT.NONE);
               tabPayloadMap.setText("Payload (Map)");

               Composite composite6 = new Composite(tabFolder, SWT.NONE);
               composite6.setLayout(new GridLayout());
               tabPayloadMap.setControl(composite6);

               Composite mapPayloadComposite = new Composite(composite6, SWT.BORDER_SOLID);
               mapPayloadComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
               mapPayloadComposite.setLayout(new GridLayout(1, false));

               createMapPayload(mapPayloadComposite);

            }

            Map<String, Object> payloadMap = new HashMap<>();
            MapMessage mm = (MapMessage) m;
            @SuppressWarnings("rawtypes")
            Enumeration mapNames = mm.getMapNames();
            while (mapNames.hasMoreElements()) {
               String key = (String) mapNames.nextElement();
               payloadMap.put(key, mm.getObject(key));
            }

            tvPayloadMap.setInput(payloadMap);

            break;

         case OBJECT:

            cleanTabs(false, true, true, true);

            if (tabPayloadText == null) {
               tabPayloadText = new TabItem(tabFolder, SWT.NONE);
               tabPayloadText.setText("Payload (Raw)");

               Composite composite3 = new Composite(tabFolder, SWT.NONE);
               tabPayloadText.setControl(composite3);
               composite3.setLayout(new FillLayout(SWT.HORIZONTAL));

               // DF SWT.WRAP slows down A LOT UI for long text Messages (> 1K)
               // txtPayloadRaw = new Text(composite3, SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);

               txtPayloadText = new Text(composite3, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
               txtPayloadText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
               // Add key binding for CTRL-a -> select all
               txtPayloadText.addListener(SWT.KeyUp, new Listener() {
                  public void handleEvent(Event event) {
                     if (event.stateMask == SWT.MOD1 && event.keyCode == 'a') {
                        ((Text) event.widget).selectAll();
                     }
                  }
               });

            }

            // Populate Fields
            StringBuilder sb = new StringBuilder(512);
            try {
               ObjectMessage om = (ObjectMessage) m;
               Serializable payloadObject = om.getObject();
               if (payloadObject != null) {
                  sb.append("'toString()' representation of the Object of class '");
                  sb.append(payloadObject.getClass().getName());
                  sb.append("' stored in the ObjectMessage:");
                  sb.append(CR).append(CR);
                  sb.append(payloadObject.toString());
               } else {
                  sb.append("(This ObjectMessage does not have a payload. Nothing to display)");
               }
            } catch (JMSException e1) {
               log.error("A JMSException occurred when reading Object Payload: {}", e1.getMessage());

               sb.append("An exception occured while reading the ObjectMessage payload.");
               sb.append(CR).append(CR);
               sb.append("In order to see the ObjectMessage payload, ");
               sb.append("consider adding the implementation class of the Object stored in the ObjectMessage to the Q Manager configuration jars.");
               sb.append(CR);
               sb.append("JMSToolBox will use the toString() method of this class to display a string representation of the object.");
               sb.append(CR).append(CR);
               sb.append("Cause: ").append(Utils.getCause(e1));
               sb.append(CR).append(CR);
               sb.append(e1.getMessage());
            }

            txtPayloadText.setText(sb.toString());
            break;

         case MESSAGE:
         case STREAM:
            cleanTabs(true, true, true, true);
            break;
      }

      setTabSelection(jtbMessage.getJtbMessageType());

      // Set Content
      tableJMSHeadersViewer.setInput(headers.entrySet());
      tablePropertiesViewer.setInput(properties.entrySet());
   }

   private void setTabSelection(JTBMessageType jtbMessageType) {
      PreferenceStore ps = cm.getPreferenceStore();
      MessageTab messageTab = MessageTab.valueOf(ps.getString(Constants.PREF_MESSAGE_TAB_DISPLAY));
      switch (messageTab) {
         case OVERVIEW:
            tabFolder.setSelection(tabOverview);
            break;
         case JMS_HEADERS:
            tabFolder.setSelection(tabJMSHeaders);
            break;
         case PROPERTIES:
            tabFolder.setSelection(tabProperties);
            break;
         case PAYLOAD:
            switch (jtbMessageType) {
               case TEXT:
                  tabFolder.setSelection(tabPayloadText);
                  break;
               case BYTES:
                  tabFolder.setSelection(tabPayloadHex);
                  break;
               case MAP:
                  tabFolder.setSelection(tabPayloadMap);
                  break;
               case OBJECT:
                  tabFolder.setSelection(tabPayloadText);
                  break;
               case MESSAGE:
               case STREAM:
                  tabFolder.setSelection(tabOverview);
                  break;
            }
            break;
         default:
            tabFolder.setSelection(tabOverview);
            break;
      }
   }

   private void cleanTabs(boolean cleanText, boolean cleanXML, boolean cleanHex, boolean cleanMap) {
      if (cleanText) {
         if (tabPayloadText != null) {
            tabPayloadText.dispose();
            tabPayloadText = null;
         }
      }
      if (cleanXML) {
         if (tabPayloadXML != null) {
            tabPayloadXML.dispose();
            tabPayloadXML = null;
         }
      }
      if (cleanHex) {
         if (tabPayloadHex != null) {
            tabPayloadHex.dispose();
            tabPayloadHex = null;
         }
      }
      if (cleanMap) {
         if (tabPayloadMap != null) {
            tabPayloadMap.dispose();
            tabPayloadMap = null;
         }
      }
   }

   // MapMessage
   private void createMapPayload(Composite parentComposite) {

      final Composite composite4 = new Composite(parentComposite, SWT.NONE);
      composite4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      composite4.setBounds(0, 0, 64, 64);
      TableColumnLayout tclComposite4 = new TableColumnLayout();
      composite4.setLayout(tclComposite4);

      tvPayloadMap = new TableViewer(composite4, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      final Table mapPropertyTable = tvPayloadMap.getTable();
      mapPropertyTable.setHeaderVisible(true);
      mapPropertyTable.setLinesVisible(true);

      TableViewerColumn propertyNameColumn = new TableViewerColumn(tvPayloadMap, SWT.NONE);
      TableColumn propertyNameHeader = propertyNameColumn.getColumn();
      tclComposite4.setColumnData(propertyNameHeader, new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH, true));
      propertyNameHeader.setAlignment(SWT.CENTER);
      propertyNameHeader.setText("Name");
      propertyNameColumn.setLabelProvider(new ColumnLabelProvider() {
         @SuppressWarnings("unchecked")
         @Override
         public String getText(Object element) {
            Map.Entry<String, Object> e = (Map.Entry<String, Object>) element;
            return e.getKey();
         }
      });

      TableViewerColumn propertyValueColumn = new TableViewerColumn(tvPayloadMap, SWT.NONE);
      TableColumn propertyValueHeader = propertyValueColumn.getColumn();
      tclComposite4.setColumnData(propertyValueHeader, new ColumnWeightData(3, ColumnWeightData.MINIMUM_WIDTH, true));
      propertyValueHeader.setText("Value");
      propertyValueColumn.setLabelProvider(new ColumnLabelProvider() {
         @SuppressWarnings("unchecked")
         @Override
         public String getText(Object element) {
            Map.Entry<String, Object> e = (Map.Entry<String, Object>) element;
            return e.getValue().toString();
         }
      });

      mapPropertyTable.addKeyListener(new KeyAdapter() {
         @SuppressWarnings("unchecked")
         @Override
         public void keyPressed(KeyEvent e) {

            // Select all
            if ((e.stateMask == SWT.MOD1) && (e.keyCode == 'a')) {
               ((Table) e.widget).selectAll();
               return;
            }

            // Copy Map to Clipboard (CTRL+C)
            if (((e.stateMask & SWT.MOD1) != 0) && (e.keyCode == 'c')) {
               IStructuredSelection selection = (IStructuredSelection) tvPayloadMap.getSelection();
               if (selection.isEmpty()) {
                  return;
               }
               StringBuilder sb = new StringBuilder(256);
               for (Object sel : selection.toList()) {
                  Map.Entry<String, Object> en = (Map.Entry<String, Object>) sel;
                  sb.append(en.getKey());
                  sb.append("=");
                  sb.append(en.getValue());
                  sb.append("\r");
               }
               Clipboard cb = new Clipboard(Display.getDefault());
               TextTransfer textTransfer = TextTransfer.getInstance();
               cb.setContents(new Object[] { sb.toString() }, new Transfer[] { textTransfer });
               return;
            }
         }
      });

      // tableViewer.setContentProvider(ArrayContentProvider.getInstance());
      tvPayloadMap.setContentProvider(new IStructuredContentProvider() {

         @SuppressWarnings("unchecked")
         @Override
         public Object[] getElements(Object inputElement) {
            Map<String, Object> m = (Map<String, Object>) inputElement;
            return m.entrySet().toArray();
         }

         @Override
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // NOP
         }

         @Override
         public void dispose() {
            // NOP
         }

      });
   }

   private class MyTableLabelProvider implements ITableLabelProvider {

      @Override
      @SuppressWarnings("unchecked")
      public String getColumnText(Object element, int columnIndex) {
         Map.Entry<String, Object> e = (Map.Entry<String, Object>) element;
         if (columnIndex == 0) {
            return e.getKey();
         }
         return e.getValue() == null ? null : e.getValue().toString();
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
