/*
 * Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBTopic;
import org.titou10.jtb.jms.util.JTBDeliveryMode;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.dnd.DNDData;
import org.titou10.jtb.ui.dnd.TransferJTBMessage;
import org.titou10.jtb.ui.dnd.TransferTemplate;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * 
 * Dynamically created Part to handle Session Content, ie one tab to show messages from a Queue or a Topic
 * 
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
   private String               currentDestinationName;

   private Map<String, TabData> mapTabData;

   private CTabFolder           tabFolder;

   private Integer              nbMessage             = 0;

   private IPreferenceStore     ps;

   private IEclipseContext      windowContext;

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
            windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, null);
         }
      });

      // Intercept focus changes on CTabItems
      tabFolder.addSelectionListener(new SelectionListener() {

         @Override
         public void widgetSelected(SelectionEvent event) {
            if (event.item instanceof CTabItem) {
               CTabItem tabItem = (CTabItem) event.item;
               TabData td = (TabData) tabItem.getData();
               td.tableViewer.getTable().setFocus();

               JTBDestination jtbDestination = td.jtbDestination;
               currentDestinationName = jtbDestination.getName();
               windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, jtbDestination);
            }
         }

         @Override
         public void widgetDefaultSelected(SelectionEvent event) {
            // NOP
         }
      });

   }

   @Focus
   public void focus(MWindow window, MPart mpart) {
      log.debug("focus currentQueueName={}", currentDestinationName);

      TabData td = mapTabData.get(currentDestinationName);
      CTabItem tabItem = td.tabItem;
      tabFolder.setSelection(tabItem);
      td.tableViewer.getTable().setFocus();

      JTBDestination jtbDestination = td.jtbDestination;
      windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, jtbDestination);
   }

   // Called to update the search text when "Copy Property as Selector" has been used..
   @Inject
   @Optional
   private void addSelectorClause(@UIEventTopic(Constants.EVENT_ADD_SELECTOR_CLAUSE) List<Map.Entry<String, Object>> entry) {
      log.debug("addSelectorClause. entry={}", entry);

      TabData td = mapTabData.get(currentDestinationName);

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

   // Called whenever a new Queue is browsed or need to be refreshed
   @Inject
   @Optional
   private void refreshQueueMessageBrowser(@Active MPart part,
                                           final @UIEventTopic(Constants.EVENT_REFRESH_QUEUE_MESSAGES) JTBQueue jtbQueue) {
      // TODO weak? Replace with more specific event?
      if (!(jtbQueue.getJtbConnection().getSessionName().equals(mySessionName))) {
         log.trace("refreshQueueMessageBrowser. This notification is not for this part ({})...", mySessionName);
         return;
      }
      log.debug("create/refresh Queue Message Browser. part={} {}", part.getElementId(), jtbQueue);

      final String jtbQueueName = jtbQueue.getName();

      // Create one tab item per Q
      if (!mapTabData.containsKey(jtbQueueName)) {

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
               eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, (JTBQueue) td.jtbDestination);
            }
         });

         final Button clearButton = new Button(leftComposite, SWT.NONE);
         clearButton.setImage(Utils.getImage(this.getClass(), "icons/cross-script.png"));
         clearButton.setToolTipText("Clear search box");
         clearButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               searchTextCombo.setText("");
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
         });

         // Refresh Button
         final Button btnRefresh = new Button(leftComposite, SWT.NONE);
         btnRefresh.setImage(Utils.getImage(this.getClass(), "icons/arrow_refresh.png"));
         btnRefresh.setToolTipText("Refresh Messages (F5)");
         btnRefresh.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
         btnRefresh.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               CTabItem selectedTab = tabFolder.getSelection();
               if (selectedTab != null) {
                  // Send event to refresh list of messages
                  TabData td = (TabData) selectedTab.getData();
                  eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, (JTBQueue) td.jtbDestination);
               }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
               // NOP
            }
         });

         // Auto Refresh Button
         final Button btnAutoRefresh = new Button(leftComposite, SWT.TOGGLE);
         btnAutoRefresh.setImage(Utils.getImage(this.getClass(), "icons/time.png"));
         // btnAutoRefresh.setToolTipText("Activate Automatic Refresh");
         btnAutoRefresh.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
         btnAutoRefresh.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               final CTabItem selectedTab = tabFolder.getSelection();
               if (selectedTab != null) {

                  AutoRefreshJob job = td.autoRefreshJob;
                  log.debug("job state={}  auto refresh={}", job.getState(), td.autoRefreshActive);
                  if (job.getState() == Job.RUNNING) {
                     td.autoRefreshActive = false;
                     job.cancel();
                  } else {
                     td.autoRefreshActive = true;
                     if (event.data != null) {
                        job.setDelay(((Long) event.data).longValue());
                     }
                     job.schedule();
                  }
               }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
               // NOP
            }
         });
         new DelayedRefreshTooltip(btnAutoRefresh);

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
         // int operations = DND.DROP_MOVE | DND.DROP_COPY;
         int operations = DND.DROP_MOVE;
         Transfer[] transferTypesDrag = new Transfer[] { TransferJTBMessage.getInstance() };
         Transfer[] transferTypesDrop = new Transfer[] { TransferJTBMessage.getInstance(), TransferTemplate.getInstance() };
         tableViewer.addDragSupport(operations, transferTypesDrag, new JTBMessageDragListener(tableViewer));
         tableViewer.addDropSupport(operations, transferTypesDrop, new JTBMessageDropListener(tableViewer, jtbQueue));

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

         // Double clic listener to activate selection on enter
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

         // Remove a message from the queue
         table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
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
         AutoRefreshJob job = new AutoRefreshJob("Connect Job", jtbQueue, ps.getInt(Constants.PREF_AUTO_REFRESH_DELAY));
         job.setSystem(true);
         job.setName("Auto refresh " + jtbQueueName);

         // Intercept closing/hiding CTabItem : Remove the CTabItem for all the lists and cancel running job when closed
         tabItemQueue.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent event) {
               log.debug("dispose CTabItem for {}", jtbQueueName);
               Job job = td.autoRefreshJob;
               job.cancel();

               mapTabData.remove(jtbQueueName);
            }
         });

         // Kind of content
         tableViewer.setContentProvider(ArrayContentProvider.getInstance());

         Integer maxMessages = ps.getInt(Constants.PREF_MAX_MESSAGES);
         spinnerMaxMessages.setSelection(maxMessages);

         // Select Tab Item
         tabFolder.setSelection(tabItemQueue);
         windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, jtbQueue);

         // Store CTabItems in working tables
         currentDestinationName = jtbQueueName;

         td.tabItem = tabItemQueue;
         td.tableViewer = tableViewer;
         td.autoRefreshJob = job;
         td.autoRefreshActive = false; // Auto refresh = false on creation
         td.searchText = searchTextCombo;
         td.searchType = comboSearchType;
         td.searchItemsHistory = new ArrayList<String>();
         td.maxMessages = maxMessages;

         tabItemQueue.setData(td);

         mapTabData.put(currentDestinationName, td);
      }

      TabData td = mapTabData.get(jtbQueueName);

      // Load Content
      loadQueueContent(jtbQueue, td.tableViewer, td.searchText, td.searchType.getSelectionIndex(), td.searchItemsHistory);
   }

   // Select CTabItem for the jtbQueue
   @Inject
   @Optional
   private void setFocus(final @UIEventTopic(Constants.EVENT_FOCUS_CTABITEM) JTBDestination jtbDestination) {
      if (!(jtbDestination.getJtbConnection().getSessionName().equals(mySessionName))) {
         log.trace("setFocus. This notification is not for this part ({})...", mySessionName);
         return;
      }
      log.debug("setFocus {}", jtbDestination);

      currentDestinationName = jtbDestination.getName();
      TabData td = mapTabData.get(currentDestinationName);
      CTabItem tabItem = td.tabItem;
      if (tabItem != null) {
         // ?? It seems in some case, tabItem is null...
         tabFolder.setSelection(td.tabItem);
         windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, jtbDestination);
      }
   }

   // --------
   // Helpers
   // --------

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
            TabData td = mapTabData.get(jtbQueue.getName());
            int maxMessages = td.maxMessages;
            if (maxMessages == 0) {
               maxMessages = Integer.MAX_VALUE;
            }

            Integer depth = jtbQueue.getJtbConnection().getQm().getQueueDepth(jtbQueue.getName());
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
               // CTabItem tabItem = mapDestinationTabItem.get(jtbQueue.getName());
               CTabItem tabItem = td.tabItem;
               tabItem.setText(sb.toString());

               if (totalMessages >= maxMessages) {
                  tabItem.setImage(Utils.getImage(this.getClass(), "icons/error.png"));
               } else {
                  if (browseMode != BrowseMode.FULL) {
                     tabItem.setImage(Utils.getImage(this.getClass(), "icons/filter.png"));
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

   private List<JTBMessage> buildListJTBMessagesSelected(IStructuredSelection selection) {
      List<JTBMessage> jtbMessagesSelected = new ArrayList<JTBMessage>(selection.size());
      for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
         JTBMessage jtbMessage = (JTBMessage) iterator.next();
         jtbMessagesSelected.add(jtbMessage);
      }
      return jtbMessagesSelected;
   }

   private void createColumns(TableViewer tv, boolean showNb) {

      TableViewerColumn col;

      if (showNb) {
         col = createTableViewerColumn(tv, "#", 50);
         col.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
               nbMessage++;
               return nbMessage.toString();
            }
         });
      }

      col = createTableViewerColumn(tv, "JMS Timestamp", 180);
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

      col = createTableViewerColumn(tv, "ID", 200);
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

      col = createTableViewerColumn(tv, "JMS Correlation ID", 150);
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

      col = createTableViewerColumn(tv, "Type", 60);
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            JTBMessage jtbMessage = (JTBMessage) element;
            return jtbMessage.getJtbMessageType().name();
         }
      });

      col = createTableViewerColumn(tv, "JMS Type", 100);
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

      col = createTableViewerColumn(tv, "Priority", 60);
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

      col = createTableViewerColumn(tv, "Delivery Mode", 100);
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

   private TableViewerColumn createTableViewerColumn(TableViewer tableViewer, String title, int bound) {
      final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
      final TableColumn column = viewerColumn.getColumn();
      column.setText(title);
      column.setWidth(bound);
      column.setResizable(true);
      column.setMoveable(true);
      return viewerColumn;
   }

   // -----------------------
   // Context Menu
   // -----------------------
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
            if (currentDestinationName == null) {
               return;
            }

            TabData td = mapTabData.get(currentDestinationName);
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
            if (currentDestinationName == null) {
               return;
            }

            TabData td = mapTabData.get(currentDestinationName);
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

   // -----------------------
   // Providers and Listeners
   // -----------------------
   private final class JTBMessageDragListener extends DragSourceAdapter {
      private final TableViewer tableViewer;

      public JTBMessageDragListener(TableViewer tableViewer) {
         this.tableViewer = tableViewer;
      }

      @Override
      @SuppressWarnings("unchecked")
      public void dragStart(DragSourceEvent event) {
         log.debug("dragStart {}", event);

         IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
         switch (selection.size()) {
            case 0:
               event.doit = false;
               break;

            case 1:
               JTBMessage jtbMessage = (JTBMessage) selection.getFirstElement();
               if (jtbMessage.getJtbMessageType() == JTBMessageType.STREAM) {
                  log.warn("STREAM Messages can not be dragged to templates or another Queue");
                  event.doit = false;
                  return;
               }

               DNDData.dragJTBMessage(jtbMessage);
               break;

            default:
               List<JTBMessage> jtbMessages = (List<JTBMessage>) selection.toList();
               for (JTBMessage jtbMessage2 : jtbMessages) {
                  if (jtbMessage2.getJtbMessageType() == JTBMessageType.STREAM) {
                     log.warn("STREAM Messages can not be dragged to templates or another Queue");
                     event.doit = false;
                     return;
                  }
               }
               DNDData.dragJTBMessageMulti(jtbMessages);
               break;
         }
      }
   }

   public final class JTBMessageDropListener extends ViewerDropAdapter {

      private JTBDestination jtbDestination;

      public JTBMessageDropListener(TableViewer tableViewer, JTBDestination jtbDestination) {
         super(tableViewer);
         this.jtbDestination = jtbDestination;
         this.setFeedbackEnabled(false); // Disable "in between" visual clues
      }

      @Override
      public void drop(DropTargetEvent event) {

         // Store the JTBDestination where the drop occurred
         log.debug("The drop was done on element: {}", jtbDestination);

         DNDData.dropOnJTBDestination(jtbDestination);

         super.drop(event);
      }

      @Override
      public boolean performDrop(Object data) {
         log.debug("performDrop : {}", DNDData.getDrag());
         switch (DNDData.getDrag()) {
            case JTBMESSAGE:
            case JTBMESSAGE_MULTI:
            case TEMPLATE:
               Map<String, Object> parameters = new HashMap<>();
               parameters.put(Constants.COMMAND_CONTEXT_PARAM, Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP);

               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_MESSAGE_SEND_TEMPLATE, parameters);
               handlerService.executeHandler(myCommand);

               return true;

            default:
               return false;
         }
      }

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferData) {
         return ((TransferTemplate.getInstance().isSupportedType(transferData))
                 || (TransferJTBMessage.getInstance().isSupportedType(transferData)));
      }
   }

   // Called whenever a new Queue is browsed or need to be refreshed
   @Inject
   @Optional
   private void refreshTopicMessageBrowser(@Active MPart part,
                                           final @UIEventTopic(Constants.EVENT_REFRESH_TOPIC_MESSAGES) JTBTopic jtbTopic) {
      // TODO weak? Replace with more specific event?
      if (!(jtbTopic.getJtbConnection().getSessionName().equals(mySessionName))) {
         log.trace("refreshTopicMessageBrowser. This notification is not for this part ({})...", mySessionName);
         return;
      }
      log.debug("create/refresh Topic Message Browser. part={} {}", part.getElementId(), jtbTopic);

      final String jtbTopicName = jtbTopic.getName();

      // Create one tab item per Q
      if (!mapTabData.containsKey(jtbTopicName)) {

         final TabData td = new TabData(jtbTopic);

         final CTabItem tabItemTopic = new CTabItem(tabFolder, SWT.NONE);
         tabItemTopic.setShowClose(true);
         tabItemTopic.setText(jtbTopicName);

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
         searchTextCombo.setToolTipText("Search criteria, either text search string or selectors");
         searchTextCombo.addListener(SWT.DefaultSelection, new Listener() {
            public void handleEvent(Event e) {
               // Start Refresh on Enter
               CTabItem selectedTab = tabFolder.getSelection();
               TabData td = (TabData) selectedTab.getData();
               eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, (JTBTopic) td.jtbDestination);
            }
         });

         final Button clearButton = new Button(leftComposite, SWT.NONE);
         clearButton.setImage(Utils.getImage(this.getClass(), "icons/cross-script.png"));
         clearButton.setToolTipText("Clear search box");
         clearButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               searchTextCombo.setText("");
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
         });

         // Stop/Start Subscription
         final Button btnStopStartSub = new Button(leftComposite, SWT.TOGGLE);
         btnStopStartSub.setImage(Utils.getImage(this.getClass(), "icons/topics/resultset_next.png"));
         btnStopStartSub.setToolTipText("Subscription active...");
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
         tabItemTopic.setControl(composite);

         // Manage Content
         final Deque<JTBMessage> messages = new ArrayDeque<>();
         tableViewer.setContentProvider(ArrayContentProvider.getInstance());
         tableViewer.setInput(messages);

         // Drag and Drop
         // int operations = DND.DROP_MOVE | DND.DROP_COPY;
         int operations = DND.DROP_MOVE;
         Transfer[] transferTypesDrag = new Transfer[] { TransferJTBMessage.getInstance() };
         Transfer[] transferTypesDrop = new Transfer[] { TransferJTBMessage.getInstance(), TransferTemplate.getInstance() };
         tableViewer.addDragSupport(operations, transferTypesDrag, new JTBMessageDragListener(tableViewer));
         tableViewer.addDropSupport(operations, transferTypesDrop, new JTBMessageDropListener(tableViewer, jtbTopic));

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

         // Double clic listener to activate selection on enter
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

         // Remove a message from the queue
         table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
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

         // Intercept closing/hiding CTabItem : Remove the CTabItem for all the lists and cancel running job when closed
         tabItemTopic.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent event) {
               log.debug("dispose CTabItem for {}", jtbTopicName);

               mapTabData.remove(jtbTopicName);
            }
         });

         btnStopStartSub.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               final CTabItem selectedTab = tabFolder.getSelection();
               TabData td = (TabData) selectedTab.getData();

               tabItemTopic.setImage(null);

               try {
                  if (td.topicMessageConsumer == null) {
                     log.debug("Closing subscription");

                     String selector = searchTextCombo.getText().trim();
                     td.topicMessageConsumer = createTopicConsumer(jtbTopic,
                                                                   tableViewer,
                                                                   selector,
                                                                   messages,
                                                                   spinnerMaxMessages.getSelection());
                     btnStopStartSub.setImage(Utils.getImage(this.getClass(), "icons/topics/resultset_next.png"));
                     btnStopStartSub.setToolTipText("Subscription active...");
                     if (!selector.isEmpty()) {
                        tabItemTopic.setImage(Utils.getImage(this.getClass(), "icons/filter.png"));
                     }

                  } else {
                     td.topicMessageConsumer.close();
                     td.topicMessageConsumer = null;
                     btnStopStartSub.setImage(Utils.getImage(this.getClass(), "icons/topics/stop.png"));
                     btnStopStartSub.setToolTipText("Subscription stopped.");
                  }
               } catch (JMSException e1) {
                  // TODO Auto-generated catch block
                  e1.printStackTrace();
               }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
               // NOP
            }
         });

         Integer maxMessages = ps.getInt(Constants.PREF_MAX_MESSAGES);
         spinnerMaxMessages.setSelection(maxMessages);

         // Select Tab Item
         tabFolder.setSelection(tabItemTopic);
         windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, jtbTopic);

         // Create Subscriber
         try {
            td.topicMessageConsumer = createTopicConsumer(jtbTopic,
                                                          tableViewer,
                                                          searchTextCombo.getText().trim(),
                                                          messages,
                                                          spinnerMaxMessages.getSelection());

         } catch (JMSException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }

         // Store CTabItems in working tables
         currentDestinationName = jtbTopicName;

         td.tabItem = tabItemTopic;
         td.tableViewer = tableViewer;
         td.autoRefreshActive = false; // Auto refresh = false on creation
         td.searchText = searchTextCombo;
         td.searchItemsHistory = new ArrayList<String>();
         td.maxMessages = maxMessages;

         tabItemTopic.setData(td);

         mapTabData.put(currentDestinationName, td);
      }

      TabData td = mapTabData.get(jtbTopicName);
      td.tableViewer.refresh();

      // Load Content
      // loadQueueContent(jtbQueue, td.tableViewer, td.searchText, td.searchType.getSelectionIndex(), td.searchItemsHistory);
   }

   private MessageConsumer createTopicConsumer(JTBTopic jtbTopic,
                                               TableViewer tableViewer,
                                               String selector,
                                               Deque<JTBMessage> messages,
                                               int maxSize) throws JMSException {

      MessageListener ml = new JTBTopicMessageListener(jtbTopic, messages, tableViewer, maxSize);
      JTBConnection jtbConnection = jtbTopic.getJtbConnection();
      return jtbConnection.createTopicSubscriber(jtbTopic, ml, selector);
   }

   // --------------
   // Helper Classes
   // --------------

   /**
    * Hold all data related to a given tab
    */
   private class TabData {

      JTBDestination  jtbDestination;
      CTabItem        tabItem;
      TableViewer     tableViewer;
      AutoRefreshJob  autoRefreshJob;
      boolean         autoRefreshActive;
      Combo           searchText;
      Combo           searchType;
      List<String>    searchItemsHistory;
      int             maxMessages;
      MessageConsumer topicMessageConsumer;

      TabData(JTBDestination jtbDestination) {
         this.jtbDestination = jtbDestination;
      }
   }

   /**
    * Kind of Queue Message browsing
    */
   private enum BrowseMode {
                            FULL,
                            SEARCH,
                            SELECTOR
   }

   /**
    * Usage of the Search Text Box
    */
   private enum SearchType {
                            PAYLOAD("Payload"),
                            SELECTOR("Selector");
      private String label;

      private SearchType(String label) {
         this.label = label;
      }

      public String getLabel() {
         return this.label;
      }

   }

   /**
    * Job for auto refreshing
    * 
    * @author Denis Forveille
    *
    */
   private class AutoRefreshJob extends Job {
      private JTBQueue jtbQueue;
      private long     delaySeconds;
      boolean          run = true;

      public AutoRefreshJob(String name, JTBQueue jtbQueue, int delaySeconds) {
         super(name);
         this.jtbQueue = jtbQueue;
         this.delaySeconds = delaySeconds;
      }

      public void setDelay(long delaySeconds) {
         this.delaySeconds = delaySeconds;
      }

      @Override
      protected void canceling() {
         log.debug("Canceling Job '{}'", getName());
         super.canceling();
         run = false;
         done(Status.CANCEL_STATUS);
      }

      @Override
      public boolean shouldSchedule() {
         log.debug("Starting Job '{}' delaySeconds: {} ", getName(), delaySeconds);
         run = true;
         return super.shouldSchedule();
      }

      @Override
      protected IStatus run(IProgressMonitor monitor) {
         while (run) {
            sync.asyncExec(new Runnable() {
               @Override
               public void run() {
                  // Send event to refresh list of messages
                  eventBroker.post(Constants.EVENT_REFRESH_QUEUE_MESSAGES, jtbQueue);
               }
            });
            // TODO Put a loop per second and test cancel...
            try {
               TimeUnit.SECONDS.sleep(delaySeconds);
            } catch (InterruptedException e) {}
         }
         return Status.OK_STATUS;
      }
   }

   private class JTBTopicMessageListener implements MessageListener {

      final JTBTopic          jtbTopic;
      final Deque<JTBMessage> messages;
      final TableViewer       tableViewer;
      final int               maxSize;

      public JTBTopicMessageListener(JTBTopic jtbTopic, Deque<JTBMessage> messages, TableViewer tableViewer, int maxSize) {
         this.messages = messages;
         this.jtbTopic = jtbTopic;
         this.tableViewer = tableViewer;
         this.maxSize = maxSize;
      };

      @Override
      public void onMessage(Message jmsMessage) {
         try {
            log.debug("Topic {} : Add Message with id: {}", jtbTopic, jmsMessage.getJMSMessageID());
            messages.addFirst(new JTBMessage(jtbTopic, jmsMessage));
            if (messages.size() > maxSize) {
               messages.removeLast();
            }
            sync.asyncExec(new Runnable() {
               @Override
               public void run() {
                  // Send event to refresh list of messages
                  tableViewer.refresh();
               }
            });
         } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   };

   private class DelayedRefreshTooltip extends ToolTip {

      private Button btnAutoRefresh;
      private int    delay;

      public DelayedRefreshTooltip(Control btnAutoRefresh) {
         super(btnAutoRefresh);

         this.btnAutoRefresh = (Button) btnAutoRefresh;
         this.delay = ps.getInt(Constants.PREF_AUTO_REFRESH_DELAY);

         this.setPopupDelay(200);
         this.setHideDelay(0);
         this.setHideOnMouseDown(false);
         this.setShift(new org.eclipse.swt.graphics.Point(-210, 0));
      }

      @Override
      protected Composite createToolTipContentArea(Event event, Composite parent) {

         Color bc = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);

         int margin = 8;
         GridLayout gl = new GridLayout(3, false);
         gl.marginLeft = margin;
         gl.marginRight = margin;
         gl.marginTop = margin;
         gl.marginBottom = margin;
         gl.verticalSpacing = margin;

         Composite ttComposite = new Composite(parent, SWT.BORDER_SOLID);
         ttComposite.setLayout(gl);
         ttComposite.setBackground(bc);

         Label lbl1 = new Label(ttComposite, SWT.CENTER);
         lbl1.setText("Auto refresh every");
         lbl1.setBackground(bc);

         final Spinner spinnerAutoRefreshDelay = new Spinner(ttComposite, SWT.BORDER);
         spinnerAutoRefreshDelay.setMinimum(5);
         spinnerAutoRefreshDelay.setMaximum(600);
         spinnerAutoRefreshDelay.setIncrement(1);
         spinnerAutoRefreshDelay.setPageIncrement(5);
         spinnerAutoRefreshDelay.setTextLimit(3);
         spinnerAutoRefreshDelay.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
         spinnerAutoRefreshDelay.setSelection(delay);

         Label lbl2 = new Label(ttComposite, SWT.CENTER);
         lbl2.setText("seconds");
         lbl2.setBackground(bc);

         final DelayedRefreshTooltip ctt = this;

         final Button applyButton = new Button(ttComposite, SWT.PUSH);
         applyButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
         applyButton.setText("Start auto refresh");
         applyButton.setBackground(bc);
         applyButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               Event e = new Event();
               delay = spinnerAutoRefreshDelay.getSelection();
               e.data = Long.valueOf(delay);
               btnAutoRefresh.setSelection(true);
               btnAutoRefresh.notifyListeners(SWT.Selection, e);
               ctt.hide();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
         });

         return ttComposite;
      }

      @Override
      protected boolean shouldCreateToolTip(Event event) {
         // Display custom tooltip only when the refreshing is not running
         if (btnAutoRefresh.getSelection()) {
            btnAutoRefresh.setToolTipText("Refreshing every " + delay + " seconds");
            return false;
         } else {
            btnAutoRefresh.setToolTipText(null);
            return super.shouldCreateToolTip(event);
         }
      }

   }

}
