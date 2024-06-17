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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.JTBPreferenceStore;
import org.titou10.jtb.cs.ColumnSystemHeader;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageType;
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

   private static final Logger  log        = LoggerFactory.getLogger(JTBMessageViewPart.class);

   // RegEx pattern to find carriage return line endings
   private static final Pattern CR_PATTERN = Pattern.compile("\r\n?");
   private static final String  CR         = "\n";

   private JTBStatusReporter    jtbStatusReporter;

   private Composite            tableJMSHeadersComposite;
   private Composite            tablePropertiesViewerComposite;

   private TabFolder            tabFolder;
   private TableViewer          tableJMSHeadersViewer;
   private TableViewer          tablePropertiesViewer;
   private TableColumn          colHeader;
   private TableColumn          colValue;
   private TableColumn          colHeader2;
   private TableColumn          colValue2;

   private Text                 txtToString;
   private Text                 txtPayloadText;
   private Text                 txtPayloadXML;
   private Text                 txtPayloadJSON;
   private HexViewer            hvPayLoadHex;
   private TableViewer          tvPayloadMap;
   private Table                tableProperties;
   private Table                tableJMSHeaders;

   private TabItem              tabToString;
   private TabItem              tabJMSHeaders;
   private TabItem              tabProperties;
   private TabItem              tabPayloadText;
   private TabItem              tabPayloadXML;
   private TabItem              tabPayloadJSON;
   private TabItem              tabPayloadHex;
   private TabItem              tabPayloadMap;

   @Inject
   private JTBPreferenceStore   ps;

   private JTBMessage           currentJtbMessage;

   private MessageTab           currentMessageTab;

   @SuppressWarnings("unchecked")
   @PostConstruct
   public void postConstruct(final Composite parent, EMenuService menuService, final ESelectionService selectionService) {
      parent.setLayout(new FillLayout(SWT.HORIZONTAL));

      tabFolder = new TabFolder(parent, SWT.NONE);

      // toString()
      tabToString = new TabItem(tabFolder, SWT.NONE);
      tabToString.setText("toString");

      var composite = new Composite(tabFolder, SWT.NONE);
      tabToString.setControl(composite);
      composite.setLayout(new FillLayout(SWT.HORIZONTAL));

      txtToString = new Text(composite, SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
      txtToString.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
      txtToString.setFont(SWTResourceManager.getFont("Courier New", 9, SWT.NORMAL));

      // JMS Headers
      tabJMSHeaders = new TabItem(tabFolder, SWT.NONE);
      tabJMSHeaders.setText("JMS Headers");

      tableJMSHeadersComposite = new Composite(tabFolder, SWT.NONE);
      tabJMSHeaders.setControl(tableJMSHeadersComposite);
      var tclComposite2 = new TableColumnLayout();
      tableJMSHeadersComposite.setLayout(tclComposite2);

      tableJMSHeadersViewer = new TableViewer(tableJMSHeadersComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      tableJMSHeaders = tableJMSHeadersViewer.getTable();
      tableJMSHeaders.setLinesVisible(true);
      tableJMSHeaders.setHeaderVisible(true);

      var colHeaderViewer = new TableViewerColumn(tableJMSHeadersViewer, SWT.NONE);
      tclComposite2.setColumnData(colHeaderViewer.getColumn(), new ColumnWeightData(2, 150, true));
      colHeader = colHeaderViewer.getColumn();
      colHeader.setText("Header");

      var colValueViewer = new TableViewerColumn(tableJMSHeadersViewer, SWT.NONE);
      tclComposite2.setColumnData(colValueViewer.getColumn(), new ColumnWeightData(3, 150, true));
      colValue = colValueViewer.getColumn();
      colValue.setText("Value");

      // Properties
      tabProperties = new TabItem(tabFolder, SWT.NONE);
      tabProperties.setText("Properties");

      tablePropertiesViewerComposite = new Composite(tabFolder, SWT.NONE);
      tabProperties.setControl(tablePropertiesViewerComposite);
      var tclComposite4 = new TableColumnLayout();
      tablePropertiesViewerComposite.setLayout(tclComposite4);

      tablePropertiesViewer = new TableViewer(tablePropertiesViewerComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      tableProperties = tablePropertiesViewer.getTable();
      tableProperties.setLinesVisible(true);
      tableProperties.setHeaderVisible(true);

      var colHeader2Viewer = new TableViewerColumn(tablePropertiesViewer, SWT.NONE);
      tclComposite4.setColumnData(colHeader2Viewer.getColumn(), new ColumnWeightData(2, 150, true));
      colHeader2 = colHeader2Viewer.getColumn();
      colHeader2.setText("Header");

      var colValue2Viewer = new TableViewerColumn(tablePropertiesViewer, SWT.NONE);
      tclComposite4.setColumnData(colValue2Viewer.getColumn(), new ColumnWeightData(3, 150, true));
      colValue2 = colValue2Viewer.getColumn();
      colValue2.setText("Value");

      // Manage selections
      if (selectionService != null) {
         tableJMSHeadersViewer.addSelectionChangedListener(event -> {
            var sel = (IStructuredSelection) event.getSelection();
            List<Map.Entry<String, ColumnSystemHeader>> xx = sel.toList();
            var headers = xx.stream()
                     .map(x -> new AbstractMap.SimpleEntry<>(x
                              .getValue(), x.getValue().getColumnSystemValue(currentJtbMessage.getJmsMessage(), true, false)))
                     .collect(Collectors.toList());
            selectionService.setSelection(headers);
         });
         tablePropertiesViewer.addSelectionChangedListener(event -> {
            var sel = (IStructuredSelection) event.getSelection();
            selectionService.setSelection(sel.toList());
         });

         // Attach the Popup Menus
         menuService.registerContextMenu(tableJMSHeadersViewer.getTable(), Constants.MESSAGE_VIEW_POPUP_MENU);
         menuService.registerContextMenu(tablePropertiesViewer.getTable(), Constants.MESSAGE_VIEW_POPUP_MENU);
      }

      tableJMSHeaders.addKeyListener(KeyListener.keyReleasedAdapter(e -> {

         // Select all JMS Headers
         if ((e.stateMask == SWT.MOD1) && (e.keyCode == 'a')) {
            var t = (Table) e.widget;
            t.selectAll();
            t.notifyListeners(SWT.Selection, null);
            return;
         }

         // Copy Map to Clipboard (CTRL+C)
         if (((e.stateMask & SWT.MOD1) != 0) && (e.keyCode == 'c')) {
            var selection = (IStructuredSelection) tableJMSHeadersViewer.getSelection();
            if (selection.isEmpty()) {
               return;
            }
            var sb = new StringBuilder(256);
            for (Object sel : selection.toList()) {
               var en = (Map.Entry<String, ColumnSystemHeader>) sel;
               sb.append(en.getKey());
               sb.append("=");
               sb.append(en.getValue().getColumnSystemValue(currentJtbMessage.getJmsMessage()));
               sb.append("\r");
            }
            var cb = new Clipboard(Display.getDefault());
            var textTransfer = TextTransfer.getInstance();
            cb.setContents(new Object[] { sb.toString() }, new Transfer[] { textTransfer });
            return;
         }
      }));

      tableProperties.addKeyListener(KeyListener.keyReleasedAdapter(e -> {

         // Select all Properties
         if ((e.stateMask == SWT.MOD1) && (e.keyCode == 'a')) {
            var t = (Table) e.widget;
            t.selectAll();
            t.notifyListeners(SWT.Selection, null);
            return;
         }

         // Copy Map to Clipboard (CTRL+C)
         if (((e.stateMask & SWT.MOD1) != 0) && (e.keyCode == 'c')) {
            var selection = (IStructuredSelection) tablePropertiesViewer.getSelection();
            if (selection.isEmpty()) {
               return;
            }
            var sb = new StringBuilder(256);
            for (Object sel : selection.toList()) {
               var en = (Map.Entry<String, Object>) sel;
               sb.append(en.getKey());
               sb.append("=");
               sb.append(en.getValue());
               sb.append("\r");
            }
            var cb = new Clipboard(Display.getDefault());
            var textTransfer = TextTransfer.getInstance();
            cb.setContents(new Object[] { sb.toString() }, new Transfer[] { textTransfer });
            return;
         }
      }));

      tabFolder.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         log.debug("TabItem selected : {}", e.item);
         var tabItem = (TabItem) e.item;
         var mt = MessageTab.fromText(tabItem.getText());
         currentMessageTab = mt == null ? MessageTab.PAYLOAD : mt;
      }));

      // Label/Content providers
      tableJMSHeadersViewer.setLabelProvider(new ColumnSystemHeaderTableLabelProvider());
      tableJMSHeadersViewer.setContentProvider(ArrayContentProvider.getInstance());

      tablePropertiesViewer.setLabelProvider(new UserPropertyTableLabelProvider());
      tablePropertiesViewer.setContentProvider(ArrayContentProvider.getInstance());

   }

   @Inject
   @Optional
   public void refreshMessage(@UIEventTopic(Constants.EVENT_JTBMESSAGE_PART_REFRESH) JTBMessage jtbMessage) {

      // Same message being displayed, fast exit
      if (this.currentJtbMessage == jtbMessage) {
         return;
      }

      log.debug("JTBMessageViewPart refresh for {}", jtbMessage);

      this.currentJtbMessage = jtbMessage;

      // Message is null, clear the part and exit
      if (jtbMessage == null) {

         cleanTabs(true, true, true, true, true);

         tableJMSHeadersViewer.setInput(null);
         Utils.resizeTableViewer(tableJMSHeadersViewer);

         tablePropertiesViewer.setInput(null);
         Utils.resizeTableViewer(tablePropertiesViewer);

         return;
      }

      // OK, time to populate the part

      ((ColumnSystemHeaderTableLabelProvider) tableJMSHeadersViewer.getLabelProvider()).setJmsMessage(jtbMessage.getJmsMessage());

      try {
         populateFields(jtbMessage);

         tableJMSHeadersViewer.getTable().deselectAll();
         tablePropertiesViewer.getTable().deselectAll();

      } catch (JMSException e) {
         jtbStatusReporter.showError("Problem while showing Message", e, "");
      }

   }

   // -------
   // Helpers
   // -------

   private void populateFields(JTBMessage jtbMessage) throws JMSException {

      // Populate fields
      var m = jtbMessage.getJmsMessage();

      // Properties
      Map<String, Object> properties = new TreeMap<>();
      Enumeration<?> e = m.getPropertyNames();
      while (e.hasMoreElements()) {
         var cle = (String) e.nextElement();
         properties.put(cle, m.getObjectProperty(cle));
      }

      // toString
      txtToString.setText(m.toString());

      // Payload tabs
      switch (jtbMessage.getJtbMessageType()) {
         case TEXT:
            cleanTabs(false, false, false, false, true);

            if (tabPayloadText == null) {
               tabPayloadText = new TabItem(tabFolder, SWT.NONE, 3);

               var composite3 = new Composite(tabFolder, SWT.NONE);
               tabPayloadText.setControl(composite3);
               composite3.setLayout(new FillLayout(SWT.HORIZONTAL));

               // DF SWT.WRAP slows down A LOT UI for long text Messages (> 1K)
               // txtPayloadRaw = new Text(composite3, SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
               txtPayloadText = new Text(composite3, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
               txtPayloadText.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));

               // Set monospaced font if required
               if (ps.getBoolean(Constants.PREF_MESSAGE_TEXT_MONOSPACED)) {
                  txtPayloadText.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL));
               }

               // Add key binding for CTRL-a -> select all
               txtPayloadText.addListener(SWT.KeyUp, new Listener() {
                  @Override
                  public void handleEvent(Event event) {
                     if ((event.stateMask == SWT.MOD1) && (event.keyCode == 'a')) {
                        ((Text) event.widget).selectAll();
                     }
                  }
               });
            }

            if (tabPayloadXML == null) {
               tabPayloadXML = new TabItem(tabFolder, SWT.NONE, 4);
               tabPayloadXML.setText(MessageTab.PAYLOAD_XML.getText());

               var composite1 = new Composite(tabFolder, SWT.NONE);
               tabPayloadXML.setControl(composite1);
               composite1.setLayout(new FillLayout(SWT.HORIZONTAL));

               // DF SWT.WRAP slows down A LOT UI for long text Messages (> 1K)
               // txtPayloadXML = new Text(composite_1, SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
               txtPayloadXML = new Text(composite1, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
               txtPayloadXML.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));

               // Add key binding for CTRL-a -> select all
               txtPayloadXML.addListener(SWT.KeyUp, new Listener() {

                  @Override
                  public void handleEvent(Event event) {
                     if ((event.stateMask == SWT.MOD1) && (event.keyCode == 'a')) {
                        ((Text) event.widget).selectAll();
                     }
                  }
               });
            }

            if (tabPayloadJSON == null) {
               tabPayloadJSON = new TabItem(tabFolder, SWT.NONE, 5);
               tabPayloadJSON.setText(MessageTab.PAYLOAD_JSON.getText());

               var composite1 = new Composite(tabFolder, SWT.NONE);
               tabPayloadJSON.setControl(composite1);
               composite1.setLayout(new FillLayout(SWT.HORIZONTAL));

               // DF SWT.WRAP slows down A LOT UI for long text Messages (> 1K)
               // txtPayloadXML = new Text(composite_1, SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
               txtPayloadJSON = new Text(composite1, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
               txtPayloadJSON.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));

               // Add key binding for CTRL-a -> select all
               txtPayloadJSON.addListener(SWT.KeyUp, new Listener() {

                  @Override
                  public void handleEvent(Event event) {
                     if ((event.stateMask == SWT.MOD1) && (event.keyCode == 'a')) {
                        ((Text) event.widget).selectAll();
                     }
                  }
               });
            }

            if (tabPayloadHex == null) {
               tabPayloadHex = new TabItem(tabFolder, SWT.NONE, 6);

               var composite1 = new Composite(tabFolder, SWT.NONE);
               tabPayloadHex.setControl(composite1);
               composite1.setLayout(new FillLayout(SWT.HORIZONTAL));

               hvPayLoadHex = new HexViewer(composite1, SWT.READ_ONLY, null, 16);
               hvPayLoadHex.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
               // hvPayLoadHex.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
            }

            // Populate Fields
            var tm = (TextMessage) m;
            var txt = tm.getText() == null ? "" : tm.getText();
            var bytes = txt.getBytes();

            tabPayloadText.setText(String.format(Constants.PAYLOAD_TEXT_TITLE, txt.length()));
            tabPayloadHex.setText(MessageTab.PAYLOAD_BYTES.getText());
            // tabPayloadHex.setText(String.format(Constants.PAYLOAD_BYTES_TITLE, bytes.length));

            txtPayloadText.setText(CR_PATTERN.matcher(txt).replaceAll(CR));
            txtPayloadXML.setText(FormatUtils.xmlPrettyFormat(ps, txt, false));
            txtPayloadJSON.setText(FormatUtils.jsonPrettyFormat(txt, false));
            hvPayLoadHex.setDataProvider(new BytesDataProvider(bytes));

            break;

         case BYTES:

            cleanTabs(true, true, true, false, true);

            final var bm = (BytesMessage) m;

            if (tabPayloadHex == null) {
               tabPayloadHex = new TabItem(tabFolder, SWT.NONE);

               var composite51 = new Composite(tabFolder, SWT.NONE);
               composite51.setLayout(new FillLayout(SWT.HORIZONTAL));
               tabPayloadHex.setControl(composite51);

               hvPayLoadHex = new HexViewer(composite51, SWT.READ_ONLY, null, 16);
               hvPayLoadHex.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
               // hvPayLoadHex.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
            }

            var payloadBytes = new byte[(int) bm.getBodyLength()];
            bm.reset();
            bm.readBytes(payloadBytes);
            IDataProvider idp = new BytesDataProvider(payloadBytes);
            hvPayLoadHex.setDataProvider(idp);

            tabPayloadHex.setText(String.format(Constants.PAYLOAD_BYTES_TITLE, payloadBytes.length));

            break;

         case MAP:

            cleanTabs(true, true, true, true, false);

            if (tabPayloadMap == null) {
               tabPayloadMap = new TabItem(tabFolder, SWT.NONE);
               tabPayloadMap.setText("Payload (Map)");

               var composite6 = new Composite(tabFolder, SWT.NONE);
               composite6.setLayout(new GridLayout());
               tabPayloadMap.setControl(composite6);

               var mapPayloadComposite = new Composite(composite6, SWT.BORDER_SOLID);
               mapPayloadComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
               mapPayloadComposite.setLayout(new GridLayout(1, false));

               createMapPayload(mapPayloadComposite);

            }

            Map<String, Object> payloadMap = new HashMap<>();
            var mm = (MapMessage) m;
            var mapNames = mm.getMapNames();
            while (mapNames.hasMoreElements()) {
               var key = (String) mapNames.nextElement();
               payloadMap.put(key, mm.getObject(key));
            }

            tvPayloadMap.setInput(payloadMap);
            Utils.resizeTableViewer(tvPayloadMap);

            break;

         case OBJECT:

            cleanTabs(false, true, true, true, true);

            if (tabPayloadText == null) {
               tabPayloadText = new TabItem(tabFolder, SWT.NONE);
               tabPayloadText.setText("Payload (Raw)");

               var composite3 = new Composite(tabFolder, SWT.NONE);
               tabPayloadText.setControl(composite3);
               composite3.setLayout(new FillLayout(SWT.HORIZONTAL));

               // DF SWT.WRAP slows down A LOT UI for long text Messages (> 1K)
               // txtPayloadRaw = new Text(composite3, SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);

               txtPayloadText = new Text(composite3, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
               txtPayloadText.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));

               // Add key binding for CTRL-a -> select all
               txtPayloadText.addListener(SWT.KeyUp, new Listener() {
                  @Override
                  public void handleEvent(Event event) {
                     if ((event.stateMask == SWT.MOD1) && (event.keyCode == 'a')) {
                        ((Text) event.widget).selectAll();
                     }
                  }
               });

            }

            // Populate Fields
            var sb = new StringBuilder(512);
            try {
               var om = (ObjectMessage) m;
               var payloadObject = om.getObject();
               if (payloadObject != null) {
                  sb.append("'toString()' representation of the Object of class '");
                  sb.append(payloadObject.getClass().getName());
                  sb.append("' stored in the ObjectMessage:");
                  sb.append(CR).append(CR);
                  sb.append(payloadObject.toString());
               } else {
                  sb.append("(This ObjectMessage does not have a payload. Nothing to display)");
               }

            } catch (Throwable e1) {
               // DF: Catching JMSException is not sufficient for classes that are used by the class of the Object Body..
               // } catch (JMSException e1) {
               log.error("A JMSException occurred when reading Object Payload: {}", e1);

               var sw = new StringWriter();
               e1.printStackTrace(new PrintWriter(sw));

               sb.append("An exception occured while reading the ObjectMessage payload.");
               sb.append(CR).append(CR);
               sb.append("In order to see the ObjectMessage payload, ");
               sb.append("consider adding the implementation class of the Object stored in the ObjectMessage to the Q Manager configuration jars.");
               sb.append(CR);
               sb.append("JMSToolBox will use the toString() method of this class to display a string representation of the object.");
               sb.append(CR).append(CR);
               sb.append("Cause: ").append(Utils.getCause(e1));
               sb.append(CR).append(CR);
               sb.append(sw);
            }

            txtPayloadText.setText(sb.toString());
            break;

         case MESSAGE:
         case STREAM:
            cleanTabs(true, true, true, true, true);
            break;
      }

      setTabSelection(jtbMessage.getJtbMessageType());

      // Set Content
      tableJMSHeadersViewer.setInput(ColumnSystemHeader.getJMSColumnSystemHeader().entrySet());
      tableJMSHeadersComposite.layout();
      Utils.resizeTableViewer(tableJMSHeadersViewer);

      tablePropertiesViewer.setInput(properties.entrySet());
      tablePropertiesViewerComposite.layout();
      Utils.resizeTableViewer(tablePropertiesViewer);
   }

   private void setTabSelection(JTBMessageType jtbMessageType) {

      if (currentMessageTab == null) {
         currentMessageTab = MessageTab.valueOf(ps.getString(Constants.PREF_MESSAGE_TAB_DISPLAY));
      }

      switch (currentMessageTab) {
         case TO_STRING:
            tabFolder.setSelection(tabToString);
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
                  tabFolder.setSelection(tabToString);
                  break;
            }
            break;
         case PAYLOAD_XML:
            // Payload (XML) is only valid for TextMessage. Use normal Payload for others
            switch (jtbMessageType) {
               case TEXT:
                  tabFolder.setSelection(tabPayloadXML);
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
                  tabFolder.setSelection(tabToString);
                  break;
            }
            break;
         case PAYLOAD_JSON:
            // Payload (JSON) is only valid for TextMessage. Use normal Payload for others
            switch (jtbMessageType) {
               case TEXT:
                  tabFolder.setSelection(tabPayloadJSON);
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
                  tabFolder.setSelection(tabToString);
                  break;
            }
            break;
         case PAYLOAD_BYTES:
            // Payload (bytes) is valid for TextMessage and ByteMessage
            switch (jtbMessageType) {
               case TEXT:
                  tabFolder.setSelection(tabPayloadHex);
                  break;
               case BYTES:
                  tabFolder.setSelection(tabPayloadHex);
                  break;
               case MAP:
                  tabFolder.setSelection(tabPayloadMap);
                  break;
               case OBJECT:
               case MESSAGE:
               case STREAM:
                  tabFolder.setSelection(tabToString);
                  break;
            }
            break;

         default:
            tabFolder.setSelection(tabToString);
            break;
      }
   }

   private void cleanTabs(boolean cleanText, boolean cleanXML, boolean cleanJSON, boolean cleanHex, boolean cleanMap) {
      var savedMessageTab = currentMessageTab;
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
      if (cleanJSON) {
         if (tabPayloadJSON != null) {
            tabPayloadJSON.dispose();
            tabPayloadJSON = null;
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
      currentMessageTab = savedMessageTab;
   }

   // MapMessage
   @SuppressWarnings("unchecked")
   private void createMapPayload(Composite parentComposite) {

      final var composite4 = new Composite(parentComposite, SWT.NONE);
      composite4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      var tclComposite4 = new TableColumnLayout();
      composite4.setLayout(tclComposite4);

      tvPayloadMap = new TableViewer(composite4, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      final var mapPropertyTable = tvPayloadMap.getTable();
      mapPropertyTable.setHeaderVisible(true);
      mapPropertyTable.setLinesVisible(true);

      var propertyNameColumn = new TableViewerColumn(tvPayloadMap, SWT.NONE);
      var propertyNameHeader = propertyNameColumn.getColumn();
      tclComposite4.setColumnData(propertyNameHeader, new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH, true));
      propertyNameHeader.setAlignment(SWT.CENTER);
      propertyNameHeader.setText("Name");
      propertyNameColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            var e = (Map.Entry<String, Object>) element;
            return e.getKey();
         }
      });

      var propertyValueColumn = new TableViewerColumn(tvPayloadMap, SWT.NONE);
      var propertyValueHeader = propertyValueColumn.getColumn();
      tclComposite4.setColumnData(propertyValueHeader, new ColumnWeightData(3, ColumnWeightData.MINIMUM_WIDTH, true));
      propertyValueHeader.setText("Value");
      propertyValueColumn.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            var e = (Map.Entry<String, Object>) element;
            return e.getValue().toString();
         }
      });

      mapPropertyTable.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
         // Select all
         if ((e.stateMask == SWT.MOD1) && (e.keyCode == 'a')) {
            ((Table) e.widget).selectAll();
            return;
         }

         // Copy Map to Clipboard (CTRL+C)
         if (((e.stateMask & SWT.MOD1) != 0) && (e.keyCode == 'c')) {
            var selection = (IStructuredSelection) tvPayloadMap.getSelection();
            if (selection.isEmpty()) {
               return;
            }
            var sb = new StringBuilder(256);
            for (Object sel : selection.toList()) {
               var en = (Map.Entry<String, Object>) sel;
               sb.append(en.getKey());
               sb.append("=");
               sb.append(en.getValue());
               sb.append("\r");
            }
            var cb = new Clipboard(Display.getDefault());
            var textTransfer = TextTransfer.getInstance();
            cb.setContents(new Object[] { sb.toString() }, new Transfer[] { textTransfer });
            return;
         }
      }));

      // tableViewer.setContentProvider(ArrayContentProvider.getInstance());
      tvPayloadMap.setContentProvider(new IStructuredContentProvider() {

         @Override
         public Object[] getElements(Object inputElement) {
            var m = (Map<String, Object>) inputElement;
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

}
