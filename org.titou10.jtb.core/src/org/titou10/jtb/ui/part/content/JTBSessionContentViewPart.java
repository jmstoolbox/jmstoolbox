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
package org.titou10.jtb.ui.part.content;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBSessionClientType;
import org.titou10.jtb.jms.model.JTBTopic;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.util.JTBDeliveryMode;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.dnd.TransferJTBMessage;
import org.titou10.jtb.ui.dnd.TransferTemplate;
import org.titou10.jtb.ui.part.content.TabData.TabDataType;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * 
 * Dynamically created Part to handle Session Content, ie one tab to show messages from a Queue or a Topic or the SYnthetic View
 * 
 * @author Denis Forveille
 * 
 */
@SuppressWarnings("restriction")
public class JTBSessionContentViewPart {

   private static final Logger  log                   = LoggerFactory.getLogger(JTBSessionContentViewPart.class);

   private static final String  SEARCH_STRING         = "%s = '%s'";
   private static final String  SEARCH_STRING_BOOLEAN = "%s = %s";
   private static final String  SEARCH_NUMBER         = "%s = %d";
   private static final String  SEARCH_BOOLEAN        = "%s = %b";
   private static final String  SEARCH_NULL           = "%s is null";

   @Inject
   private UISynchronize        sync;

   @Inject
   private ESelectionService    selectionService;

   @Inject
   private EMenuService         menuService;

   @Inject
   private IEventBroker         eventBroker;

   @Inject
   private ECommandService      commandService;

   @Inject
   private EHandlerService      handlerService;

   @Inject
   private ConfigManager        cm;

   @Inject
   private JTBStatusReporter    jtbStatusReporter;

   private String               mySessionName;
   private String               currentCTabItemName;

   private Map<String, TabData> mapTabData;

   private CTabFolder           tabFolder;

   private Integer              nbMessage             = 0;

   private IPreferenceStore     ps;

   private IEclipseContext      windowContext;

   // Create the TabFolder
   @PostConstruct
   public void postConstruct(MWindow mw, final @Active MPart part, Composite parent) {

      this.mySessionName = part.getLabel();
      this.ps = cm.getPreferenceStore();
      this.windowContext = mw.getContext();
      this.mapTabData = new HashMap<>();

      parent.setLayout(new FillLayout(SWT.HORIZONTAL));

      this.tabFolder = new CTabFolder(parent, SWT.BORDER);
      this.tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

      addContextMenu();

      // Dispose Listener
      tabFolder.addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(DisposeEvent disposeEvent) {
            log.debug("tabFolder disposed {}", disposeEvent);
            windowContext.remove(Constants.CURRENT_TAB_JTBDESTINATION);
            windowContext.remove(Constants.CURRENT_TAB_JTBSESSION);
         }
      });

      // Intercept focus changes on CTabItems
      tabFolder.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent event) {
            if (event.item instanceof CTabItem) {
               CTabItem tabItem = (CTabItem) event.item;
               TabData td = (TabData) tabItem.getData();
               td.tableViewer.getTable().setFocus();

               if (td.type == TabDataType.JTBDESTINATION) {
                  currentCTabItemName = computeCTabItemName(td.jtbDestination);
                  windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, td.jtbDestination);
                  windowContext.remove(Constants.CURRENT_TAB_JTBSESSION);
               } else {
                  currentCTabItemName = computeCTabItemName(td.jtbSession);
                  windowContext.remove(Constants.CURRENT_TAB_JTBDESTINATION);
                  windowContext.set(Constants.CURRENT_TAB_JTBSESSION, td.jtbSession);
               }
            }
         }
      });
   }

   private void addContextMenu() {

      Menu contextMenu = new Menu(tabFolder);
      MenuItem close = new MenuItem(contextMenu, SWT.NONE);
      close.setText("Close");
      // close.setText("Close\tCtrl+W");
      close.setAccelerator(SWT.MOD1 + 'E');
      close.addListener(SWT.Selection, new Listener() {
         @Override
         public void handleEvent(Event event) {
            log.debug("Close");
            if (currentCTabItemName == null) {
               return;
            }

            TabData td = mapTabData.get(currentCTabItemName);
            CTabItem sel = td.tabItem;
            sel.dispose();
         }
      });

      MenuItem closeOthers = new MenuItem(contextMenu, SWT.NONE);
      closeOthers.setText("Close Others");
      closeOthers.addListener(SWT.Selection, new Listener() {
         @Override
         public void handleEvent(Event event) {
            log.debug("Close Others");
            if (currentCTabItemName == null) {
               return;
            }

            TabData td = mapTabData.get(currentCTabItemName);
            CTabItem sel = td.tabItem;
            for (TabData t : new ArrayList<>(mapTabData.values())) {
               if (t.tabItem != sel) {
                  t.tabItem.dispose();
               }
            }
         }
      });

      MenuItem closeAll = new MenuItem(contextMenu, SWT.NONE);
      closeAll.setText("Close All");
      // closeAll.setAccelerator(SWT.CTRL + SWT.SHIFT + 'W'); does not work
      closeAll.addListener(SWT.Selection, new Listener() {
         @Override
         public void handleEvent(Event event) {
            log.debug("Close All");

            for (TabData t : new ArrayList<>(mapTabData.values())) {
               t.tabItem.dispose();
            }
         }
      });

      tabFolder.setMenu(contextMenu);
   }

   @Focus
   public void focus(MWindow window, MPart mpart) {
      log.debug("focus currentCTabItemName={}", currentCTabItemName);

      TabData td = mapTabData.get(currentCTabItemName);
      CTabItem tabItem = td.tabItem;
      tabFolder.setSelection(tabItem);
      td.tableViewer.getTable().setFocus();

      if (td.type == TabDataType.JTBDESTINATION) {
         currentCTabItemName = computeCTabItemName(td.jtbDestination);
         windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, td.jtbDestination);
         windowContext.remove(Constants.CURRENT_TAB_JTBSESSION);
      } else {
         currentCTabItemName = computeCTabItemName(td.jtbSession);
         windowContext.remove(Constants.CURRENT_TAB_JTBDESTINATION);
         windowContext.set(Constants.CURRENT_TAB_JTBSESSION, td.jtbSession);
      }
   }

   // Set focus on the CTabItem for the destination
   @Inject
   @Optional
   private void setFocusCTabItemDestination(final @UIEventTopic(Constants.EVENT_FOCUS_CTABITEM) JTBDestination jtbDestination) {
      if (!(jtbDestination.getJtbConnection().getSessionName().equals(mySessionName))) {
         log.trace("setFocusCTabItemDestination. This notification is not for this part ({})...", mySessionName);
         return;
      }
      log.debug("setFocusCTabItemDestination {}", jtbDestination);

      currentCTabItemName = computeCTabItemName(jtbDestination);
      TabData td = mapTabData.get(currentCTabItemName);
      if (td.tabItem != null) {
         // ?? It seems in some case, tabItem is null...
         tabFolder.setSelection(td.tabItem);
         windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, jtbDestination);
         windowContext.remove(Constants.CURRENT_TAB_JTBSESSION);
      }
   }

   // Set focus on the CTabItem for the Session
   @Inject
   @Optional
   private void setFocusCTabItemSession(final @UIEventTopic(Constants.EVENT_FOCUS_SYNTHETIC) JTBSession jtbSession) {
      if (!(jtbSession.getName().equals(mySessionName))) {
         log.trace("setFocusCTabItemSession. This notification is not for this part ({})...", mySessionName);
         return;
      }
      log.debug("setFocusCTabItemSession {}", jtbSession);

      currentCTabItemName = computeCTabItemName(jtbSession);
      TabData td = mapTabData.get(currentCTabItemName);
      tabFolder.setSelection(td.tabItem);
      windowContext.remove(Constants.CURRENT_TAB_JTBDESTINATION);
      windowContext.set(Constants.CURRENT_TAB_JTBSESSION, jtbSession);
   }

   // Called to update the search text when "Copy Property as Selector" has been used..
   @Inject
   @Optional
   private void addSelectorClause(@UIEventTopic(Constants.EVENT_ADD_SELECTOR_CLAUSE) List<Map.Entry<String, Object>> entry) {
      log.debug("addSelectorClause. entry={}", entry);

      TabData td = mapTabData.get(currentCTabItemName);

      // Select "Selector" as search type
      Combo searchTypeCombo = td.searchType;
      if (searchTypeCombo != null) {
         // No searchTypeCombo for Topics
         searchTypeCombo.select(SearchType.SELECTOR.ordinal());
      }

      Combo c = td.searchText;

      StringBuilder sb = new StringBuilder(128);
      sb.append(c.getText());
      for (Map.Entry<String, Object> e : entry) {

         if (!(c.getText().trim().isEmpty())) {
            sb.append(" AND ");
         }

         String key = e.getKey();
         Object value = e.getValue();

         if (value == null) {
            sb.append(String.format(SEARCH_NULL, key));
            continue;
         }

         if (value instanceof Number) {
            sb.append(String.format(SEARCH_NUMBER, key, value));
            continue;
         }

         if (value instanceof Boolean) {
            sb.append(String.format(SEARCH_BOOLEAN, key, value));
            continue;
         }

         String val = value.toString();
         if ((val.equalsIgnoreCase("true")) || (val.equalsIgnoreCase("false"))) {
            sb.append(String.format(SEARCH_STRING_BOOLEAN, key, value));
            continue;
         }

         sb.append(String.format(SEARCH_STRING, key, value));
      }
      c.setText(sb.toString());

   }

   // --------------
   // Queue Handling
   // --------------

   // Called whenever a new Queue is browsed or need to be refreshed
   @Inject
   @Optional
   private void refreshQueueMessageBrowser(final @UIEventTopic(Constants.EVENT_REFRESH_QUEUE_MESSAGES) JTBQueue jtbQueue) {
      // TODO weak? Replace with more specific event?
      if (!(jtbQueue.getJtbConnection().getSessionName().equals(mySessionName))) {
         log.trace("refreshQueueMessageBrowser. This notification is not for this part ({})...", mySessionName);
         return;
      }
      log.debug("refreshQueueMessageBrowser: {}", jtbQueue);

      final String jtbQueueName = jtbQueue.getName();

      // Create one tab item per Q
      if (!mapTabData.containsKey(computeCTabItemName(jtbQueue))) {

         final TabData td = new TabData(jtbQueue);

         CTabItem tabItemQueue = new CTabItem(tabFolder, SWT.NONE);
         tabItemQueue.setShowClose(true);
         tabItemQueue.setText(jtbQueueName);

         Composite composite = new Composite(tabFolder, SWT.NONE);
         composite.setLayout(new GridLayout(3, false));

         // -----------
         // Search Line
         // -----------
         GridLayout glSearch = new GridLayout(5, false);
         glSearch.marginWidth = 0;

         Composite leftComposite = new Composite(composite, SWT.NONE);
         leftComposite.setLayout(glSearch);
         leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

         // Search Type
         final Combo comboSearchType = new Combo(leftComposite, SWT.READ_ONLY);
         String[] labels = new String[SearchType.values().length];
         for (SearchType searchType : SearchType.values()) {
            labels[searchType.ordinal()] = searchType.getLabel();
         }
         comboSearchType.setItems(labels);
         comboSearchType.setToolTipText("Search/Refresh Mode");
         comboSearchType.select(SearchType.PAYLOAD.ordinal());

         // Search Text
         final Combo searchTextCombo = new Combo(leftComposite, SWT.BORDER);
         searchTextCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
         searchTextCombo.setToolTipText("Search criteria, either text search string or selectors");
         searchTextCombo.addListener(SWT.DefaultSelection, new Listener() {
            public void handleEvent(Event e) {
               // Start Refresh on Enter
               CTabItem selectedTab = tabFolder.getSelection();
               TabData td = (TabData) selectedTab.getData();
               eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, td.jtbDestination.getAsJTBQueue());
            }
         });

         final Button clearButton = new Button(leftComposite, SWT.NONE);
         clearButton.setImage(SWTResourceManager.getImage(this.getClass(), "icons/cross-script.png"));
         clearButton.setToolTipText("Clear search box");
         clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               searchTextCombo.setText("");
            }
         });

         // Refresh Button
         final Button btnRefresh = new Button(leftComposite, SWT.NONE);
         btnRefresh.setImage(SWTResourceManager.getImage(this.getClass(), "icons/arrow_refresh.png"));
         btnRefresh.setToolTipText("Refresh Messages (F5)");
         btnRefresh.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
         btnRefresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               CTabItem selectedTab = tabFolder.getSelection();
               if (selectedTab != null) {
                  // Send event to refresh list of messages
                  TabData td = (TabData) selectedTab.getData();
                  eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, td.jtbDestination.getAsJTBQueue());
               }
            }
         });

         // Auto Refresh Button
         final Button btnAutoRefresh = new Button(leftComposite, SWT.TOGGLE);
         btnAutoRefresh.setImage(SWTResourceManager.getImage(this.getClass(), "icons/time.png"));
         btnAutoRefresh.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
         btnAutoRefresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               final CTabItem selectedTab = tabFolder.getSelection();
               if (selectedTab != null) {

                  AutoRefreshJob job = td.autoRefreshJob;
                  log.debug("job name={} state={}  auto refresh={}", job.getName(), job.getState(), td.autoRefreshActive);
                  if (job.getState() == Job.RUNNING) {
                     job.cancel();
                     try {
                        if (!job.cancel()) {
                           job.join();
                        }
                     } catch (InterruptedException e) {
                        log.warn("InterruptedException occurred", e);
                     }
                     td.autoRefreshActive = false;
                  } else {
                     if (event.data != null) {
                        job.setDelay(((Long) event.data).longValue());
                     }
                     td.autoRefreshActive = true;
                     job.schedule();
                  }
               }
            }
         });
         new DelayedRefreshTooltip(ps.getInt(Constants.PREF_AUTO_REFRESH_DELAY), btnAutoRefresh);

         // Separator
         Composite separatorComposite = new Composite(composite, SWT.NONE);
         separatorComposite.setLayout(new RowLayout());
         Label separator = new Label(separatorComposite, SWT.SEPARATOR | SWT.VERTICAL);
         RowData layoutData = new RowData();
         layoutData.height = leftComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
         separator.setLayoutData(layoutData);

         // Right Composite
         GridLayout glRefresh = new GridLayout(1, false);
         glRefresh.marginWidth = 0;

         Composite rightComposite = new Composite(composite, SWT.NONE);
         rightComposite.setLayout(glRefresh);

         final Spinner spinnerMaxMessages = new Spinner(rightComposite, SWT.BORDER | SWT.RIGHT);
         spinnerMaxMessages.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
         spinnerMaxMessages.setToolTipText("Max number of messages displayed.\n0=no limit");
         spinnerMaxMessages.setMinimum(0);
         spinnerMaxMessages.setMaximum(9999);
         spinnerMaxMessages.setIncrement(1);
         spinnerMaxMessages.setPageIncrement(50);
         spinnerMaxMessages.setTextLimit(4);
         spinnerMaxMessages.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
               td.maxMessages = spinnerMaxMessages.getSelection();
            }
         });
         // -------------------
         // Table with Messages
         // -------------------
         final TableViewer tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
         Table table = tableViewer.getTable();
         table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
         table.setHeaderVisible(true);
         table.setLinesVisible(true);
         tabItemQueue.setControl(composite);

         // Drag and Drop
         int operations = DND.DROP_MOVE;
         Transfer[] transferTypesDrag = new Transfer[] { TransferJTBMessage.getInstance(), FileTransfer.getInstance() };
         Transfer[] transferTypesDrop = new Transfer[] { TransferJTBMessage.getInstance(), TransferTemplate.getInstance(),
                                                         FileTransfer.getInstance() };
         tableViewer.addDragSupport(operations, transferTypesDrag, new MessageDragListener(tableViewer));
         tableViewer.addDropSupport(operations,
                                    transferTypesDrop,
                                    new MessageDropListener(commandService, handlerService, tableViewer, jtbQueue));

         // Create Columns
         createColumns(tableViewer, true);

         // Manage selections
         tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {

               // Store selected Message
               List<JTBMessage> jtbMessagesSelected = buildListJTBMessagesSelected((IStructuredSelection) event.getSelection());
               selectionService.setSelection(jtbMessagesSelected);

               // Refresh Message Viewer
               if ((jtbMessagesSelected != null) && (jtbMessagesSelected.size() > 0)) {
                  eventBroker.send(Constants.EVENT_REFRESH_JTBMESSAGE_PART, jtbMessagesSelected.get(0));
               }
            }
         });

         // Double click listener to activate selection on enter
         tableViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
               // Call the View Message Command
               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_MESSAGE_VIEW, null);
               handlerService.executeHandler(myCommand);
            }
         });

         // Attach the Popup Menu
         menuService.registerContextMenu(table, Constants.QUEUE_CONTENT_POPUP_MENU);

         // Handle Keyboard Shortcuts
         table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
               if (e.keyCode == SWT.F5) {

                  // Send event to refresh list of queues
                  CTabItem selectedTab = tabFolder.getSelection();
                  TabData td = (TabData) selectedTab.getData();
                  eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, td.jtbDestination);
               }

               if (e.keyCode == 'a' && (e.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL) {
                  @SuppressWarnings("unchecked")
                  List<JTBMessage> messages = (List<JTBMessage>) tableViewer.getInput();
                  IStructuredSelection selection = new StructuredSelection(messages);
                  tableViewer.setSelection(selection);
                  return;
               }

               if (e.keyCode == SWT.DEL) {
                  IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                  if (selection.isEmpty()) {
                     return;
                  }

                  // Call the Remove command
                  ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_MESSAGE_REMOVE, null);
                  handlerService.executeHandler(myCommand);
               }
            }
         });

         // Create periodic refresh Job
         AutoRefreshJob job = new AutoRefreshJob(sync,
                                                 eventBroker,
                                                 "Connect Job",
                                                 ps.getInt(Constants.PREF_AUTO_REFRESH_DELAY),
                                                 jtbQueue);
         job.setSystem(true);
         job.setName("Auto refresh " + jtbQueueName);

         // Intercept closing/hiding CTabItem : Remove the CTabItem for all the lists and cancel running job when closed
         tabItemQueue.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent event) {
               log.debug("dispose CTabItem for Queue '{}'", jtbQueueName);
               Job job = td.autoRefreshJob;
               job.cancel();

               mapTabData.remove(computeCTabItemName(jtbQueue));
            }
         });

         // Kind of content
         tableViewer.setContentProvider(ArrayContentProvider.getInstance());

         Integer maxMessages = ps.getInt(Constants.PREF_MAX_MESSAGES);
         spinnerMaxMessages.setSelection(maxMessages);

         // Select Tab Item
         tabFolder.setSelection(tabItemQueue);
         windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, jtbQueue);
         windowContext.remove(Constants.CURRENT_TAB_JTBSESSION);

         // Store data into TabData
         currentCTabItemName = computeCTabItemName(jtbQueue);

         td.tabItem = tabItemQueue;
         td.tableViewer = tableViewer;
         td.autoRefreshJob = job;
         td.autoRefreshActive = false; // Auto refresh = false on creation
         td.searchText = searchTextCombo;
         td.searchType = comboSearchType;
         td.searchItemsHistory = new ArrayList<String>();
         td.maxMessages = maxMessages;

         tabItemQueue.setData(td);
         mapTabData.put(currentCTabItemName, td);
      }

      TabData td = mapTabData.get(computeCTabItemName(jtbQueue));

      // Load Content
      loadQueueContent(jtbQueue, td.tableViewer, td.searchText, td.searchType.getSelectionIndex(), td.searchItemsHistory);
   }

   private void loadQueueContent(final JTBQueue jtbQueue,
                                 final TableViewer tableViewer,
                                 final Combo searchTextCombo,
                                 final int selectionIndex,
                                 final List<String> oldSearchItems) {

      // Determine browsing mode
      final String searchText = searchTextCombo.getText().trim();
      BrowseMode bm;
      if (searchText.isEmpty()) {
         bm = BrowseMode.FULL;
      } else {
         if (selectionIndex == SearchType.PAYLOAD.ordinal()) {
            bm = BrowseMode.SEARCH;
         } else {
            bm = BrowseMode.SELECTOR;
         }
         String firstElement = oldSearchItems.isEmpty() ? "" : oldSearchItems.get(0);
         if (!(firstElement.equals(searchText))) {
            oldSearchItems.remove(searchText);
            oldSearchItems.add(0, searchText);
            searchTextCombo.setItems(oldSearchItems.toArray(new String[oldSearchItems.size()]));
            searchTextCombo.select(0);
         }
      }
      final BrowseMode browseMode = bm;

      // Set Content
      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            TabData td = mapTabData.get(computeCTabItemName(jtbQueue));
            int maxMessages = td.maxMessages;
            if (maxMessages == 0) {
               maxMessages = Integer.MAX_VALUE;
            }

            JTBConnection jtbConnection = jtbQueue.getJtbConnection();
            Integer depth = jtbConnection.getQm().getQueueDepth(jtbConnection.getJmsConnection(), jtbQueue.getName());
            nbMessage = 0;

            try {
               List<JTBMessage> messages = new ArrayList<>(256);
               switch (browseMode) {
                  case FULL:
                     messages = jtbQueue.getJtbConnection().browseQueue(jtbQueue, maxMessages);
                     break;
                  case SEARCH:
                     messages = jtbQueue.getJtbConnection().searchQueue(jtbQueue, searchText, maxMessages);
                     break;
                  case SELECTOR:
                     messages = jtbQueue.getJtbConnection().browseQueueWithSelector(jtbQueue, searchText, maxMessages);
                     break;
               }

               // Display # messages in tab title

               Integer totalMessages = messages.size();
               log.debug("Q Depth : {} Max : {} Nb msg to display : {}", depth, maxMessages, totalMessages);

               StringBuilder sb = new StringBuilder(64);
               sb.append(jtbQueue.getName());
               sb.append(" (");
               sb.append(totalMessages);
               if (totalMessages >= maxMessages) {
                  if (depth != null) {
                     sb.append(" / ");
                     sb.append(depth);
                  } else {
                     sb.append("+");
                  }
               }
               sb.append(")");
               CTabItem tabItem = td.tabItem;
               tabItem.setText(sb.toString());

               if (totalMessages >= maxMessages) {
                  tabItem.setImage(SWTResourceManager.getImage(this.getClass(), "icons/error.png"));
               } else {
                  if (browseMode != BrowseMode.FULL) {
                     tabItem.setImage(SWTResourceManager.getImage(this.getClass(), "icons/filter.png"));
                  } else {
                     tabItem.setImage(null);
                  }
               }

               tableViewer.setInput(messages);

            } catch (Throwable e) {
               jtbStatusReporter.showError("Problem while browsing queue", Utils.getCause(e), "");
               return;
            }
         }
      });
   }

   @SuppressWarnings("unchecked")
   private List<JTBMessage> buildListJTBMessagesSelected(IStructuredSelection selection) {
      return new ArrayList<JTBMessage>(selection.toList());
   }

   // --------------
   // Topic Handling
   // --------------

   // Called when the "Clear Topic Message" command is called
   @Inject
   @Optional
   private void clearTopicMessages(final @UIEventTopic(Constants.EVENT_TOPIC_CLEAR_MESSAGES) JTBTopic jtbTopic) {
      // TODO weak? Replace with more specific event?
      if (!(jtbTopic.getJtbConnection().getSessionName().equals(mySessionName))) {
         log.trace("clearTopicMessages. This notification is not for this part ({})...", mySessionName);
         return;
      }
      log.debug("clear captured messages. topic={}", jtbTopic);

      TabData td = mapTabData.get(computeCTabItemName(jtbTopic));
      td.topicMessages.clear();
      td.tableViewer.refresh();
   }

   // Called whenever a Topic is browsed
   @Inject
   @Optional
   private void refreshTopicMessageBrowser(final @UIEventTopic(Constants.EVENT_REFRESH_TOPIC_SHOW_MESSAGES) JTBTopic jtbTopic) {
      // TODO weak? Replace with more specific event?
      if (!(jtbTopic.getJtbConnection().getSessionName().equals(mySessionName))) {
         log.trace("refreshTopicMessageBrowser. This notification is not for this part ({})...", mySessionName);
         return;
      }
      log.debug("create/refresh Topic Message Browser. jtbTopic={}", jtbTopic);

      final String jtbTopicName = jtbTopic.getName();

      // Get teh current tab associated with the topic, create the tab is needed
      if (!mapTabData.containsKey(computeCTabItemName(jtbTopic))) {

         final TabData td = new TabData(jtbTopic);

         final CTabItem tabItemTopic = new CTabItem(tabFolder, SWT.NONE);
         tabItemTopic.setShowClose(true);
         tabItemTopic.setText(jtbTopicName);
         tabItemTopic.setImage(SWTResourceManager.getImage(this.getClass(), "icons/topics/play-2-16.png"));

         Composite composite = new Composite(tabFolder, SWT.NONE);
         composite.setLayout(new GridLayout(3, false));

         // -----------
         // Search Line
         // -----------
         GridLayout glSearch = new GridLayout(5, false);
         glSearch.marginWidth = 0;

         Composite leftComposite = new Composite(composite, SWT.NONE);
         leftComposite.setLayout(glSearch);
         leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

         // Search Type
         final Label labelSearchType = new Label(leftComposite, SWT.NONE);
         labelSearchType.setText("Selector: ");
         labelSearchType.setToolTipText("Topic Filtering");

         // Search Text
         final Combo searchTextCombo = new Combo(leftComposite, SWT.BORDER);
         searchTextCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
         searchTextCombo.setToolTipText("Search selector that will be used by the JMS MessageListener to filter messages");
         searchTextCombo.addListener(SWT.DefaultSelection, new Listener() {
            public void handleEvent(Event e) {
               // Start Refresh on Enter
               CTabItem selectedTab = tabFolder.getSelection();
               TabData td = (TabData) selectedTab.getData();
               eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, td.jtbDestination.getAsJTBTopic());
            }
         });

         final Button clearButton = new Button(leftComposite, SWT.NONE);
         clearButton.setImage(SWTResourceManager.getImage(this.getClass(), "icons/cross-script.png"));
         clearButton.setToolTipText("Clear search box");
         clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               searchTextCombo.setText("");
            }
         });

         // Stop/Start Subscription
         final Button btnStopStartSub = new Button(leftComposite, SWT.TOGGLE);
         btnStopStartSub.setImage(SWTResourceManager.getImage(this.getClass(), "icons/topics/pause-16.png"));
         btnStopStartSub.setToolTipText("Stop Subscription");
         btnStopStartSub.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
         btnStopStartSub.setSelection(true);

         // Separator
         Composite separatorComposite = new Composite(composite, SWT.NONE);
         separatorComposite.setLayout(new RowLayout());
         Label separator = new Label(separatorComposite, SWT.SEPARATOR | SWT.VERTICAL);
         RowData layoutData = new RowData();
         layoutData.height = leftComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
         separator.setLayoutData(layoutData);

         // Right Composite
         GridLayout glRefresh = new GridLayout(1, false);
         glRefresh.marginWidth = 0;

         Composite rightComposite = new Composite(composite, SWT.NONE);
         rightComposite.setLayout(glRefresh);

         final Spinner spinnerMaxMessages = new Spinner(rightComposite, SWT.BORDER | SWT.RIGHT);
         spinnerMaxMessages.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
         spinnerMaxMessages.setToolTipText("Maximum number of messages captured and displayed.\n0=no limit");
         spinnerMaxMessages.setMinimum(0);
         spinnerMaxMessages.setMaximum(9999);
         spinnerMaxMessages.setIncrement(1);
         spinnerMaxMessages.setPageIncrement(50);
         spinnerMaxMessages.setTextLimit(4);
         spinnerMaxMessages.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
               td.maxMessages = spinnerMaxMessages.getSelection();
            }
         });
         // -------------------
         // Table with Messages
         // -------------------
         final TableViewer tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
         Table table = tableViewer.getTable();
         table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
         table.setHeaderVisible(true);
         table.setLinesVisible(true);
         tabItemTopic.setControl(composite);

         // Manage Content
         final Deque<JTBMessage> messages = new ArrayDeque<>();
         tableViewer.setContentProvider(ArrayContentProvider.getInstance());
         tableViewer.setInput(messages);

         // Drag and Drop
         int operations = DND.DROP_MOVE;
         Transfer[] transferTypesDrag = new Transfer[] { TransferJTBMessage.getInstance(), FileTransfer.getInstance() };
         Transfer[] transferTypesDrop = new Transfer[] { TransferJTBMessage.getInstance(), TransferTemplate.getInstance(),
                                                         FileTransfer.getInstance() };
         tableViewer.addDragSupport(operations, transferTypesDrag, new MessageDragListener(tableViewer));
         tableViewer.addDropSupport(operations,
                                    transferTypesDrop,
                                    new MessageDropListener(commandService, handlerService, tableViewer, jtbTopic));

         // Create Columns
         createColumns(tableViewer, false);

         // Manage selections
         tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {

               // Store selected Message
               List<JTBMessage> jtbMessagesSelected = buildListJTBMessagesSelected((IStructuredSelection) event.getSelection());
               selectionService.setSelection(jtbMessagesSelected);

               // Refresh Message Viewer
               if ((jtbMessagesSelected != null) && (jtbMessagesSelected.size() > 0)) {
                  eventBroker.send(Constants.EVENT_REFRESH_JTBMESSAGE_PART, jtbMessagesSelected.get(0));
               }
            }
         });

         // Double click listener to activate selection on enter
         tableViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
               // Call the View Message Command
               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_MESSAGE_VIEW, null);
               handlerService.executeHandler(myCommand);
            }
         });

         // Attach the Popup Menu
         menuService.registerContextMenu(table, Constants.QUEUE_CONTENT_POPUP_MENU);

         // Keyboard Shortcuts on the Message table
         table.addKeyListener(new KeyAdapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void keyPressed(KeyEvent e) {
               if (e.keyCode == 'a' && (e.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL) {
                  // Selection MUST be a List<>
                  IStructuredSelection selection = new StructuredSelection(new ArrayList<JTBMessage>(td.topicMessages));
                  tableViewer.setSelection(selection);
                  return;
               }

               if (e.keyCode == SWT.DEL) {
                  IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                  if (selection.isEmpty()) {
                     return;
                  }

                  for (JTBMessage m : (List<JTBMessage>) selection.toList()) {
                     td.topicMessages.remove(m);
                  }
                  return;
               }
            }
         });

         // Intercept closing/hiding CTabItem : Remove the CTabItem for all the lists and stop the MessageListener
         tabItemTopic.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent event) {
               log.debug("dispose CTabItem for Topic '{}'", jtbTopicName);

               // Close subscription
               TabData td = mapTabData.get(computeCTabItemName(jtbTopic));
               try {
                  if (td.topicMessageConsumer != null) {
                     td.topicMessageConsumer.close();
                     td.topicMessageConsumer = null;
                  }
               } catch (JMSException e) {
                  log.error("Exception when closing subscription", e);
               }
               mapTabData.remove(computeCTabItemName(jtbTopic));
            }
         });

         // Manage the behavior of the Stop/Start button
         btnStopStartSub.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               TabData td = (TabData) tabFolder.getSelection().getData();

               try {
                  if (td.topicMessageConsumer == null) {
                     // Listener is stopped, create a new one
                     log.debug("Starting subscription");

                     String selector = searchTextCombo.getText().trim();
                     td.topicMessageConsumer = createTopicConsumer(jtbTopic,
                                                                   tableViewer,
                                                                   tabItemTopic,
                                                                   selector,
                                                                   messages,
                                                                   spinnerMaxMessages.getSelection());
                     btnStopStartSub.setImage(SWTResourceManager.getImage(this.getClass(), "icons/topics/pause-16.png"));
                     btnStopStartSub.setToolTipText("Stop Subscription");
                     if (!selector.isEmpty()) {
                        tabItemTopic.setImage(SWTResourceManager.getImage(this.getClass(), "icons/filter.png"));
                     } else {
                        tabItemTopic.setImage(SWTResourceManager.getImage(this.getClass(), "icons/topics/play-2-16.png"));
                     }

                  } else {
                     // Listener is running, stop it
                     td.topicMessageConsumer.close();
                     td.topicMessageConsumer = null;
                     btnStopStartSub.setImage(SWTResourceManager.getImage(this.getClass(), "icons/topics/play-2-16.png"));
                     btnStopStartSub.setToolTipText("Start Subscription");
                     tabItemTopic.setImage(SWTResourceManager.getImage(this.getClass(), "icons/topics/pause-16.png"));
                  }
               } catch (JMSException e1) {
                  String msg = "An Exception occured when stopping/starting subscription";
                  log.error(msg, e1);
                  jtbStatusReporter.showError(msg, Utils.getCause(e1), e1.getMessage());
               }
            }
         });

         // Set Data
         // --------
         Integer maxMessages = ps.getInt(Constants.PREF_MAX_MESSAGES_TOPIC);
         spinnerMaxMessages.setSelection(maxMessages);

         // Select Tab Item
         tabFolder.setSelection(tabItemTopic);
         windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, jtbTopic);
         windowContext.remove(Constants.CURRENT_TAB_JTBSESSION);

         // Store data in TabData and CTabItem
         currentCTabItemName = computeCTabItemName(jtbTopic);

         td.tabItem = tabItemTopic;
         td.tableViewer = tableViewer;
         td.autoRefreshActive = false; // Auto refresh = false on creation
         td.searchText = searchTextCombo;
         td.searchItemsHistory = new ArrayList<String>();
         td.maxMessages = maxMessages;
         td.topicMessages = messages;

         tabItemTopic.setData(td);
         mapTabData.put(currentCTabItemName, td);

         // Create Subscriber
         try {
            td.topicMessageConsumer = createTopicConsumer(jtbTopic,
                                                          tableViewer,
                                                          tabItemTopic,
                                                          searchTextCombo.getText().trim(),
                                                          messages,
                                                          spinnerMaxMessages.getSelection());

         } catch (JMSException e1) {
            String msg = "An Exception occured when initially starting the subscription";
            log.error(msg, e1);
            jtbStatusReporter.showError(msg, Utils.getCause(e1), e1.getMessage());
            return;
         }
      }

      TabData td = mapTabData.get(computeCTabItemName(jtbTopic));
      td.tableViewer.refresh();
   }

   private MessageConsumer createTopicConsumer(JTBTopic jtbTopic,
                                               TableViewer tableViewer,
                                               CTabItem tabItemTopic,
                                               String selector,
                                               Deque<JTBMessage> messages,
                                               int maxSize) throws JMSException {

      MessageListener ml = new TopicListener(sync, jtbTopic, messages, tableViewer, tabItemTopic, maxSize);
      JTBConnection jtbConnection = jtbTopic.getJtbConnection();
      return jtbConnection.createTopicSubscriber(jtbTopic, ml, selector);
   }

   // -----------------------
   // Synthetic View Handling
   // -----------------------

   // Called whenever a new Queue is browsed or need to be refreshed
   @Inject
   @Optional
   private void refreshSyntheticView(final @UIEventTopic(Constants.EVENT_REFRESH_SESSION_SYNTHETIC_VIEW) JTBSession jtbSession) {
      // TODO weak? Replace with more specific event?
      if (!(jtbSession.getJTBConnection(JTBSessionClientType.GUI).getSessionName().equals(mySessionName))) {
         log.trace("refreshSyntheticView. This notification is not for this part ({})...", mySessionName);
         return;
      }
      log.debug("create/refresh Synthetic view. jtbSession={}", jtbSession);

      final String jtbSessionName = jtbSession.getName();

      // Create one tab item per Session
      if (!mapTabData.containsKey(computeCTabItemName(jtbSession))) {

         final TabData td = new TabData(jtbSession);

         CTabItem tabItemSynthetic = new CTabItem(tabFolder, SWT.NONE);
         tabItemSynthetic.setShowClose(true);
         tabItemSynthetic.setText("Queues depth");

         Composite composite = new Composite(tabFolder, SWT.NONE);
         composite.setLayout(new GridLayout(1, false));

         // -----------
         // Search Line
         // -----------
         GridLayout glSearch = new GridLayout(6, false);
         glSearch.marginWidth = 0;

         Composite leftComposite = new Composite(composite, SWT.NONE);
         leftComposite.setLayout(glSearch);
         leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

         final Label labelFilter1 = new Label(leftComposite, SWT.NONE);
         labelFilter1.setText("Filter Queues: ");

         // Search Text
         final Text filterText = new Text(leftComposite, SWT.BORDER);
         filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
         filterText.addListener(SWT.Traverse, new Listener() {
            @Override
            public void handleEvent(Event event) {
               if (event.detail == SWT.TRAVERSE_RETURN) {
                  // User pressed Enter
                  // Send event to refresh list of queues
                  CTabItem selectedTab = tabFolder.getSelection();
                  if (selectedTab != null) {
                     TabData td = (TabData) selectedTab.getData();
                     eventBroker.send(Constants.EVENT_REFRESH_SESSION_SYNTHETIC_VIEW, td.jtbSession);
                  }
               }
            }
         });

         final Label labelFilter2 = new Label(leftComposite, SWT.NONE);
         labelFilter2.setText("(Use '*' or '?' as wildcards)");

         // Clear Text
         final Button clearButton = new Button(leftComposite, SWT.NONE);
         clearButton.setImage(SWTResourceManager.getImage(this.getClass(), "icons/cross-script.png"));
         clearButton.setToolTipText("Clear search box");
         clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               filterText.setText("");
            }
         });

         // Refresh Button
         final Button btnRefresh = new Button(leftComposite, SWT.NONE);
         btnRefresh.setImage(SWTResourceManager.getImage(this.getClass(), "icons/arrow_refresh.png"));
         btnRefresh.setToolTipText("Refresh Messages (F5)");
         btnRefresh.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
         btnRefresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               CTabItem selectedTab = tabFolder.getSelection();
               if (selectedTab != null) {
                  // Send event to refresh list of queues
                  TabData td = (TabData) selectedTab.getData();
                  eventBroker.send(Constants.EVENT_REFRESH_SESSION_SYNTHETIC_VIEW, td.jtbSession);
               }
            }
         });

         // Auto Refresh Button
         final Button btnAutoRefresh = new Button(leftComposite, SWT.TOGGLE);
         btnAutoRefresh.setImage(SWTResourceManager.getImage(this.getClass(), "icons/time.png"));
         btnAutoRefresh.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
         btnAutoRefresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               final CTabItem selectedTab = tabFolder.getSelection();
               if (selectedTab != null) {

                  AutoRefreshJob job = td.autoRefreshJob;
                  log.debug("job name={} state={}  auto refresh={}", job.getName(), job.getState(), td.autoRefreshActive);
                  if (job.getState() == Job.RUNNING) {
                     job.cancel();
                     try {
                        if (!job.cancel()) {
                           job.join();
                        }
                     } catch (InterruptedException e) {
                        log.warn("InterruptedException occurred", e);
                     }
                     td.autoRefreshActive = false;
                  } else {
                     if (event.data != null) {
                        job.setDelay(((Long) event.data).longValue());
                     }
                     td.autoRefreshActive = true;
                     job.schedule();
                  }
               }
            }
         });
         new DelayedRefreshTooltip(ps.getInt(Constants.PREF_AUTO_REFRESH_DELAY), btnAutoRefresh);

         // ---------------------------------------
         // Table with Queue Depths + JMS Timestamp
         // ---------------------------------------
         final TableViewer tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
         Table table = tableViewer.getTable();
         table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
         table.setHeaderVisible(true);
         table.setLinesVisible(true);
         tabItemSynthetic.setControl(composite);

         // Create Columns

         TableViewerColumn col = createTableViewerColumn(tableViewer, "Queue Name", 250, SWT.NONE);
         col.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
               QueueWithDepth qwd = (QueueWithDepth) element;
               return qwd.jtbQueue.getName();
            }

            @Override
            public Color getBackground(Object element) {
               // Put a gray backgound on non browsable queues
               QueueWithDepth qwd = (QueueWithDepth) element;
               if (!(qwd.jtbQueue.isBrowsable())) {
                  return SWTResourceManager.getColor(SWT.COLOR_GRAY);
               }
               return null;
            }
         });

         col = createTableViewerColumn(tableViewer, "Depth", 20, SWT.RIGHT);
         col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
               QueueWithDepth p = (QueueWithDepth) element;
               return p.depth == null ? "N/A" : p.depth.toString();
            }
         });

         col = createTableViewerColumn(tableViewer, "JMS Timestamp of 1st Message", 140, SWT.CENTER);
         col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
               QueueWithDepth p = (QueueWithDepth) element;
               return p.firstMessageTimestamp == null ? "-" : Constants.JMS_TIMESTAMP_SDF.format(p.firstMessageTimestamp);
            }
         });

         // Manage selections
         tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {

               IStructuredSelection sel = (IStructuredSelection) event.getSelection();
               if ((sel == null) || (sel.isEmpty())) {
                  return;
               }

               QueueWithDepth qwd = (QueueWithDepth) sel.getFirstElement();
               windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, qwd.jtbQueue);
            }
         });

         // Double click listener to activate selection on enter
         tableViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {

               // Call Browse Queue Command
               Map<String, Object> parameters = new HashMap<>();
               parameters.put(Constants.COMMAND_CONTEXT_PARAM, Constants.COMMAND_CONTEXT_PARAM_SYNTHETIC);
               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_QUEUE_BROWSE, parameters);
               handlerService.executeHandler(myCommand);
            }
         });

         // Handle Keyboard Shortcuts
         table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
               if (e.keyCode == SWT.F5) {

                  // Send event to refresh list of queues
                  CTabItem selectedTab = tabFolder.getSelection();
                  TabData td = (TabData) selectedTab.getData();
                  eventBroker.send(Constants.EVENT_REFRESH_SESSION_SYNTHETIC_VIEW, td.jtbSession);
               }
            }
         });

         // Attach the Popup Menu
         menuService.registerContextMenu(table, Constants.SYNTHETIC_VIEW_POPUP_MENU);

         // Create periodic refresh Job
         AutoRefreshJob job = new AutoRefreshJob(sync,
                                                 eventBroker,
                                                 "Connect Job",
                                                 ps.getInt(Constants.PREF_AUTO_REFRESH_DELAY),
                                                 jtbSession);
         job.setSystem(true);
         job.setName("Auto refresh " + jtbSessionName);

         // Intercept closing/hiding CTabItem : Remove the CTabItem for all the lists and cancel running job when closed
         tabItemSynthetic.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent event) {
               log.debug("dispose CTabItem for Queue '{}'", jtbSessionName);
               Job job = td.autoRefreshJob;
               job.cancel();

               mapTabData.remove(computeCTabItemName(jtbSession));
            }
         });

         // Kind of content
         tableViewer.setContentProvider(ArrayContentProvider.getInstance());

         // Select Tab Item
         tabFolder.setSelection(tabItemSynthetic);
         windowContext.remove(Constants.CURRENT_TAB_JTBDESTINATION);
         windowContext.set(Constants.CURRENT_TAB_JTBSESSION, jtbSession);

         // Store data into TabData
         currentCTabItemName = computeCTabItemName(jtbSession);

         td.tabItem = tabItemSynthetic;
         td.tableViewer = tableViewer;
         td.autoRefreshJob = job;
         td.autoRefreshActive = false; // Auto refresh = false on creation
         td.filterText = filterText;

         tabItemSynthetic.setData(td);
         mapTabData.put(currentCTabItemName, td);
      }

      TabData td = mapTabData.get(computeCTabItemName(jtbSession));

      // Set Content
      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            JTBConnection jtbConnection = jtbSession.getJTBConnection(JTBSessionClientType.GUI);
            QManager qm = jtbConnection.getQm();

            // Get Queues based on Tree Browser filter
            List<QueueWithDepth> list = new ArrayList<QueueWithDepth>(jtbConnection.getJtbQueues().size());
            SortedSet<JTBQueue> baseQueues;
            if (jtbConnection.isFilterApplied()) {
               baseQueues = jtbConnection.getJtbQueuesFiltered();
            } else {
               baseQueues = jtbConnection.getJtbQueues();
            }

            // Filter Queue names based on local filter
            SortedSet<JTBQueue> jtbQueuesFiltered = new TreeSet<>(baseQueues);
            String filter = td.filterText.getText().trim();
            if (!(filter.isEmpty())) {
               String filterRegexPattern = filter.replaceAll("\\.", "\\\\.").replaceAll("\\?", ".").replaceAll("\\*", ".*");
               jtbQueuesFiltered = jtbQueuesFiltered.stream().filter(q -> q.getName().matches(filterRegexPattern))
                        .collect(Collectors.toCollection(() -> new TreeSet<>()));

            }

            // Hide non browsable Queue if set in preference
            if (!(ps.getBoolean(Constants.PREF_SHOW_NON_BROWSABLE_Q))) {
               jtbQueuesFiltered = jtbQueuesFiltered.stream().filter(q -> q.isBrowsable())
                        .collect(Collectors.toCollection(() -> new TreeSet<>()));
            }

            for (JTBQueue jtbQueue : jtbQueuesFiltered) {
               Date firstMessageTimestamp = null;
               try {
                  firstMessageTimestamp = jtbConnection.getFirstMessageTimestamp(jtbQueue);
               } catch (JMSException e) {
                  log.error("JMSException occurred when calling jtbConnection.getFirstMessageTimestamp", e);
               }
               list.add(new QueueWithDepth(jtbQueue,
                                           qm.getQueueDepth(jtbConnection.getJmsConnection(), jtbQueue.getName()),
                                           firstMessageTimestamp));
            }

            td.tableViewer.setInput(list);
         }
      });

      Utils.resizeTableViewer(td.tableViewer);
   }

   // --------
   // Helpers
   // --------

   private String computeCTabItemName(JTBDestination jtbDestination) {
      if (jtbDestination.isJTBQueue()) {
         return "Q:" + jtbDestination.getName();
      } else {
         return "T:" + jtbDestination.getName();
      }
   }

   private String computeCTabItemName(JTBSession jtbSession) {
      return "S:" + jtbSession.getName();
   }

   private void createColumns(TableViewer tv, boolean showNb) {

      TableViewerColumn col;

      if (showNb) {
         col = createTableViewerColumn(tv, "#", 25, SWT.RIGHT);
         col.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
               nbMessage++;
               return nbMessage.toString();
            }
         });
      }

      col = createTableViewerColumn(tv, "JMS Timestamp", 140, SWT.NONE);
      col.setLabelProvider(new ColumnLabelProvider() {

         @Override
         public String getText(Object element) {
            JTBMessage jtbMessage = (JTBMessage) element;
            Message m = jtbMessage.getJmsMessage();
            try {
               if (m.getJMSTimestamp() == 0) {
                  return "";
               } else {
                  Date d = new Date(m.getJMSTimestamp());
                  return Constants.JMS_TIMESTAMP_SDF.format(d);
               }
            } catch (JMSException e) {
               log.warn("JMSException occured when reading JMSTimestamp : {}", e.getMessage());
               return "";
            }
         }
      });

      col = createTableViewerColumn(tv, "ID", 200, SWT.NONE);
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            JTBMessage jtbMessage = (JTBMessage) element;
            Message m = jtbMessage.getJmsMessage();
            try {
               if (m.getJMSMessageID() == null) {
                  return "";
               } else {
                  return m.getJMSMessageID();
               }
            } catch (JMSException e) {
               log.warn("JMSException occured when reading JMSMessageID : {}", e.getMessage());
               return "";
            }
         }
      });

      col = createTableViewerColumn(tv, "JMS Correlation ID", 150, SWT.NONE);
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            JTBMessage jtbMessage = (JTBMessage) element;
            Message m = jtbMessage.getJmsMessage();
            try {
               return m.getJMSCorrelationID();
            } catch (JMSException e) {
               log.warn("JMSException occured when reading JMSCorrelationID : {}", e.getMessage());
               return "";
            }
         }
      });

      col = createTableViewerColumn(tv, "Type", 60, SWT.NONE);
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            JTBMessage jtbMessage = (JTBMessage) element;
            return jtbMessage.getJtbMessageType().name();
         }
      });

      col = createTableViewerColumn(tv, "JMS Type", 100, SWT.NONE);
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            JTBMessage jtbMessage = (JTBMessage) element;
            Message m = jtbMessage.getJmsMessage();
            try {
               return m.getJMSType();
            } catch (JMSException e) {
               log.warn("JMSException occured when reading Message : {}", e.getMessage());
               return "";
            }
         }
      });

      col = createTableViewerColumn(tv, "Priority", 60, SWT.NONE);
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            JTBMessage jtbMessage = (JTBMessage) element;
            Message m = jtbMessage.getJmsMessage();
            try {
               return String.valueOf(m.getJMSPriority());
            } catch (JMSException e) {
               log.warn("JMSException occured when reading Message : {}", e.getMessage());
               return "";
            }
         }
      });

      col = createTableViewerColumn(tv, "Delivery Mode", 100, SWT.NONE);
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            JTBMessage jtbMessage = (JTBMessage) element;
            Message m = jtbMessage.getJmsMessage();
            try {
               JTBDeliveryMode jmd = JTBDeliveryMode.fromValue(m.getJMSDeliveryMode());
               return jmd.name();
            } catch (JMSException e) {
               log.warn("JMSException occured when reading JMSDeliveryMode : {}", e.getMessage());
               return "";
            }
         }
      });

   }

   private TableViewerColumn createTableViewerColumn(TableViewer tableViewer, String title, int bound, int style) {
      final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, style);
      final TableColumn column = viewerColumn.getColumn();
      column.setText(title);
      column.setWidth(bound);
      column.setResizable(true);
      column.setMoveable(true);
      return viewerColumn;
   }

}
