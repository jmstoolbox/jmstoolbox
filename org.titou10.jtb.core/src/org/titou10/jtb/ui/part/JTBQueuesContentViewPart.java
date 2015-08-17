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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;

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
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.util.JMSDeliveryMode;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.DNDData;
import org.titou10.jtb.util.DNDData.DNDElement;
import org.titou10.jtb.util.Utils;

/**
 * 
 * Manage the View Part with the Message Lists
 * 
 * Dynamically created Part to handle Q Content for one Q Manager
 * 
 * @author Denis Forveille
 * 
 */
@SuppressWarnings("restriction")
public class JTBQueuesContentViewPart {

   private static final Logger log = LoggerFactory.getLogger(JTBQueuesContentViewPart.class);

   private static final String SEARCH_STRING         = "%s = '%s'";
   private static final String SEARCH_STRING_BOOLEAN = "%s = %s";
   private static final String SEARCH_NUMBER         = "%s = %d";
   private static final String SEARCH_BOOLEAN        = "%s = %b";

   @Inject
   private UISynchronize sync;

   @Inject
   private ESelectionService selectionService;

   @Inject
   private EMenuService menuService;

   @Inject
   private IEventBroker eventBroker;

   @Inject
   private ECommandService commandService;

   @Inject
   private EHandlerService handlerService;

   @Inject
   private ConfigManager cm;

   @Inject
   private JTBStatusReporter jtbStatusReporter;

   private Shell  shell;
   private String mySessionName;
   private String currentQueueName;

   private Map<String, CTabItem>    mapQueueTabItem;
   private Map<String, TableViewer> mapTableViewer;
   private Map<String, Job>         mapJobs;
   private Map<String, Boolean>     mapAutoRefresh;
   private Map<String, Text>        mapSearchText;

   private CTabFolder tabFolder;

   private Integer nbMessage = 0;

   private IPreferenceStore ps;

   private IEclipseContext windowContext;

   @PostConstruct
   public void postConstruct(Shell shell, MWindow mw, final @Active MPart part, Composite parent) {

      this.shell = shell;
      this.mySessionName = part.getLabel();
      this.ps = cm.getPreferenceStore();
      this.windowContext = mw.getContext();

      parent.setLayout(new FillLayout(SWT.HORIZONTAL));

      mapQueueTabItem = new HashMap<>();
      mapTableViewer = new HashMap<>();
      mapJobs = new HashMap<>();
      mapAutoRefresh = new HashMap<>();
      mapSearchText = new HashMap<>();

      tabFolder = new CTabFolder(parent, SWT.BORDER);
      tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

      addContextMenu();

      // Dispose Listener
      tabFolder.addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(DisposeEvent disposeEvent) {
            log.debug("tabFolder disposed {}", disposeEvent);
            windowContext.set(Constants.CURRENT_TAB_JTBQUEUE, null);
         }
      });

      // Intercept focus changes on CTabItems
      tabFolder.addSelectionListener(new SelectionListener() {

         @Override
         public void widgetSelected(SelectionEvent event) {
            if (event.item instanceof CTabItem) {
               CTabItem i = (CTabItem) event.item;
               JTBQueue jtbQueue = (JTBQueue) i.getData();
               currentQueueName = jtbQueue.getName();
               windowContext.set(Constants.CURRENT_TAB_JTBQUEUE, jtbQueue);
               // manageRunningJobs((CTabItem) event.item, tiAutoRefresh);
            }
         }

         @Override
         public void widgetDefaultSelected(SelectionEvent event) {
            // NOP
         }
      });

   }

   // Called to update the search text when "Copy Property as Selector" has been used..
   @Inject
   @Optional
   private void addSearchString(@UIEventTopic(Constants.EVENT_ADD_SEARCH_STRING) Map.Entry<String, Object> entry) {
      log.debug("entry={}", entry);

      Text t = mapSearchText.get(currentQueueName);
      if (!(t.getText().trim().isEmpty())) {
         t.append(" AND ");
      }

      String key = entry.getKey();
      Object value = entry.getValue();

      if (value instanceof Number) {
         t.append(String.format(SEARCH_NUMBER, key, value));
         return;
      }
      if (value instanceof Boolean) {
         t.append(String.format(SEARCH_BOOLEAN, key, value));
         return;
      }
      String val = value.toString();
      if ((val.equalsIgnoreCase("true")) || (val.equalsIgnoreCase("false"))) {
         t.append(String.format(SEARCH_STRING_BOOLEAN, key, value));
         return;
      }
      t.append(String.format(SEARCH_STRING, key, value));
   }

   // Called whenever a new Queue is browsed or need to be refreshed
   @Inject
   @Optional
   private void getNotified(@Active MPart part, final @UIEventTopic(Constants.EVENT_REFRESH_MESSAGES) JTBQueue jtbQueue) {
      // TODO weak? Replace with more specific event?
      if (!(jtbQueue.getJtbSession().getName().equals(mySessionName))) {
         log.trace("This notification is not for this part...");
         return;
      }
      log.debug("create/refresh Message Browser. part={} {}", part.getElementId(), jtbQueue);

      final String jtbQueueName = jtbQueue.getName();

      // Create one tab item per Q
      CTabItem tabItemQueue = mapQueueTabItem.get(jtbQueueName);
      if (tabItemQueue == null) {
         tabItemQueue = new CTabItem(tabFolder, SWT.NONE);
         tabItemQueue.setShowClose(true);
         tabItemQueue.setText(jtbQueueName);
         tabItemQueue.setData(jtbQueue); // Store JTBQueue in tab Data

         Composite composite = new Composite(tabFolder, SWT.NONE);
         composite.setLayout(new GridLayout(3, false));

         // -----------
         // Search Line
         // -----------
         Composite searchComposite = new Composite(composite, SWT.NONE);
         searchComposite.setLayout(new GridLayout(4, false));
         searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

         final Text searchText = new Text(searchComposite, SWT.BORDER);
         searchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

         final Button clearButton = new Button(searchComposite, SWT.NONE);
         clearButton.setImage(Utils.getImage(this.getClass(), "icons/cross-script.png"));
         clearButton.setToolTipText("Clear search box");
         clearButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               searchText.setText("");
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
         });

         final Button searchButton = new Button(searchComposite, SWT.NONE);
         searchButton.setImage(Utils.getImage(this.getClass(), "icons/magnifier.png"));
         searchButton.setToolTipText("Search text in Text Messages");
         searchButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               if (searchText.getText().trim().isEmpty()) {
                  return;
               }
               loadContent(BrowseMode.SEARCH, jtbQueue, mapTableViewer.get(jtbQueueName), searchText.getText());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
         });

         final Button searchSelectorButton = new Button(searchComposite, SWT.NONE);
         searchSelectorButton.setImage(Utils.getImage(this.getClass(), "icons/magnifier_zoom_in.png"));
         searchSelectorButton.setToolTipText("Search Messages with selectors");
         searchSelectorButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
               if (searchText.getText().trim().isEmpty()) {
                  return;
               }
               if (searchText.getText().trim().isEmpty()) {
                  return;
               }
               loadContent(BrowseMode.SELECTOR, jtbQueue, mapTableViewer.get(jtbQueueName), searchText.getText());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
         });

         // Separator
         Composite separatorComposite = new Composite(composite, SWT.NONE);
         separatorComposite.setLayout(new RowLayout());
         Label separator = new Label(separatorComposite, SWT.SEPARATOR | SWT.VERTICAL);
         RowData layoutData = new RowData();
         layoutData.height = searchComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
         separator.setLayoutData(layoutData);

         // Refresh Buttons
         Composite refreshComposite = new Composite(composite, SWT.NONE);
         refreshComposite.setLayout(new GridLayout(2, false));

         final Button btnAutoRefresh = new Button(refreshComposite, SWT.TOGGLE);
         btnAutoRefresh.setImage(Utils.getImage(this.getClass(), "icons/time.png"));
         btnAutoRefresh.setToolTipText("Activate Automatic Refresh");
         btnAutoRefresh.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
         btnAutoRefresh.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               final CTabItem selectedTab = tabFolder.getSelection();
               if (selectedTab != null) {

                  Job job = mapJobs.get(currentQueueName);
                  log.debug("job state={}  auto refresh={}", job.getState(), mapAutoRefresh.get(currentQueueName));
                  if (job.getState() == Job.RUNNING) {
                     mapAutoRefresh.put(currentQueueName, false);
                     job.cancel();
                  } else {
                     mapAutoRefresh.put(currentQueueName, true);
                     job.schedule();
                  }
               }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
               // NOP
            }
         });

         final Button btnRefresh = new Button(refreshComposite, SWT.NONE);
         btnRefresh.setImage(Utils.getImage(this.getClass(), "icons/arrow_refresh.png"));
         btnRefresh.setToolTipText("Refresh Messages (F5)");
         btnRefresh.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
         btnRefresh.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
               CTabItem selectedTab = tabFolder.getSelection();
               if (selectedTab != null) {
                  // Send event to refresh list of messages
                  eventBroker.send(Constants.EVENT_REFRESH_MESSAGES, (JTBQueue) selectedTab.getData());
               }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
               // NOP
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
         int operations = DND.DROP_MOVE | DND.DROP_COPY;
         Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };
         tableViewer.addDragSupport(operations, transferTypes, new JTBMessageDragListener(tableViewer));
         tableViewer.addDropSupport(operations, transferTypes, new JTBMessageDropListener(tableViewer, jtbQueue));

         // Create Columns
         createColumns(tableViewer);

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
         Job job = new AutoRefreshJob("Connect Job", jtbQueue, ps.getInt(Constants.PREF_AUTO_REFRESH_DELAY));
         job.setSystem(true);
         job.setName("Auto refresh " + jtbQueueName);

         // Intercept closing/hiding CTabItem : Remove the CTabItem for all the lists and cancel running job when closed
         tabItemQueue.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent event) {
               log.debug("dispose CTabItem for {}", jtbQueueName);
               Job job = mapJobs.get(jtbQueueName);
               job.cancel();

               mapQueueTabItem.remove(jtbQueueName);
               mapTableViewer.remove(jtbQueueName);
               mapJobs.remove(jtbQueueName);
               mapAutoRefresh.remove(jtbQueueName);
               mapSearchText.remove(jtbQueueName);
            }
         });

         // Kind of content
         tableViewer.setContentProvider(ArrayContentProvider.getInstance());

         // Store CTabItems in working tables
         currentQueueName = jtbQueueName;
         mapQueueTabItem.put(jtbQueueName, tabItemQueue);
         mapTableViewer.put(jtbQueueName, tableViewer);
         mapJobs.put(jtbQueueName, job);
         mapAutoRefresh.put(jtbQueueName, false); // Auto refresh = false on creation
         mapSearchText.put(jtbQueueName, searchText);

         // Pause other auto refresh jobs
         // manageRunningJobs(tabItemQueue);

         // Select Tab Item
         tabFolder.setSelection(tabItemQueue);
         windowContext.set(Constants.CURRENT_TAB_JTBQUEUE, jtbQueue);
      }

      // Load Content
      loadContent(BrowseMode.FULL, jtbQueue, mapTableViewer.get(jtbQueue.getName()), null);
   }

   // Select CTabItem for the jtbQueue
   @Inject
   @Optional
   private void setFocus(final @UIEventTopic(Constants.EVENT_FOCUS_CTABITEM) JTBQueue jtbQueue) {
      log.debug("setFocus {}", jtbQueue);
      currentQueueName = jtbQueue.getName();
      CTabItem tabItem = mapQueueTabItem.get(currentQueueName);
      if (tabItem != null) {
         // ?? It seems in some case, tabItem is null...
         tabFolder.setSelection(mapQueueTabItem.get(currentQueueName));
         windowContext.set(Constants.CURRENT_TAB_JTBQUEUE, jtbQueue);
      }
   }

   // --------
   // Helpers
   // --------
   private void loadContent(final BrowseMode browseMode,
                            final JTBQueue jtbQueue,
                            final TableViewer tableViewer,
                            final String searchText) {
      // Set Content
      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            Integer depth = jtbQueue.getJtbSession().getQm().getQueueDepth(jtbQueue.getName());
            nbMessage = 0;
            int maxMessages = ps.getInt(Constants.PREF_MAX_MESSAGES);
            try {
               List<JTBMessage> messages = new ArrayList<>();
               switch (browseMode) {
                  case FULL:
                     messages = jtbQueue.getJtbSession().browseQueue(jtbQueue, maxMessages);
                     break;
                  case SEARCH:
                     messages = jtbQueue.getJtbSession().searchQueue(jtbQueue, searchText.trim(), maxMessages);
                     if (messages.isEmpty()) {
                        MessageDialog.openInformation(shell, "No result", "No message match the criterias");
                        return;
                     }
                     break;
                  case SELECTOR:
                     messages = jtbQueue.getJtbSession().browseQueueWithSelector(jtbQueue, searchText.trim(), maxMessages);
                     if (messages.isEmpty()) {
                        MessageDialog.openInformation(shell, "No result", "No message match the criterias");
                        return;
                     }
                     break;
               }

               // Display # messages in tab title

               Integer max = ConfigManager.getPreferenceStore2().getInt(Constants.PREF_MAX_MESSAGES);
               if (max == 0) {
                  max = Integer.MAX_VALUE;
               }

               Integer totalMessages = messages.size();
               log.debug("Nb messages to display : {}", totalMessages);

               StringBuilder sb = new StringBuilder(64);
               sb.append(jtbQueue.getName());
               sb.append(" (");
               sb.append(totalMessages);
               if (totalMessages >= max) {
                  if (depth != null) {
                     sb.append(" / ");
                     sb.append(depth);
                  } else {
                     sb.append("+");
                  }
               }
               sb.append(")");
               CTabItem tabItem = mapQueueTabItem.get(jtbQueue.getName());
               tabItem.setText(sb.toString());

               if (totalMessages >= max) {
                  tabItem.setImage(Utils.getImage(this.getClass(), "icons/error.png"));
               } else {
                  tabItem.setImage(null);
               }

               tableViewer.setInput(messages);

            } catch (Throwable e) {
               jtbStatusReporter.showError("Problem while browsing queue", e, "");
               return;
            }
         }
      });
   }

   // // Stop the running Job not associated with the current CTabItem
   // private void manageRunningJobs(CTabItem selectedItem) {
   //
   // // Get Queue Name
   // String queueName = null;
   // for (Entry<String, CTabItem> cTabItemEntry : mapQueueTabItem.entrySet()) {
   // if (cTabItemEntry.getValue().equals(selectedItem)) {
   // queueName = cTabItemEntry.getKey();
   // break;
   // }
   // }
   // if (queueName == null) {
   // log.warn("???? queueName not found");
   // return;
   // }
   //
   // // Cancel all other running jobs (Should only be one..)
   // for (Job job : mapJobs.values()) {
   // log.debug("job '{}' state={} (0=None 1=Sleep 2=Wait 4=Run)", job.getName(), job.getState());
   // if (job.getState() == Job.RUNNING) {
   // log.debug("job '{}' running . cancel it", job.getName());
   // job.cancel();
   // }
   // }
   //
   // // Start the associated Job if auto refresh is on..
   // Job jobCurrentQueue = mapJobs.get(queueName);
   // log.debug("Selected job '{}' job state={} auto refresh={}",
   // jobCurrentQueue.getName(),
   // jobCurrentQueue.getState(),
   // mapAutoRefresh.get(queueName));
   // if (mapAutoRefresh.get(queueName)) {
   // if (jobCurrentQueue.getState() != Job.RUNNING) {
   // log.info("Start Job '{}'", jobCurrentQueue.getName());
   // jobCurrentQueue.schedule();
   // }
   // }
   //
   // }

   private List<JTBMessage> buildListJTBMessagesSelected(IStructuredSelection selection) {
      List<JTBMessage> jtbMessagesSelected = new ArrayList<JTBMessage>(selection.size());
      for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
         JTBMessage jtbMessage = (JTBMessage) iterator.next();
         jtbMessagesSelected.add(jtbMessage);
      }
      return jtbMessagesSelected;
   }

   private void createColumns(TableViewer tv) {

      TableViewerColumn col = createTableViewerColumn(tv, "#", 50);
      col.setLabelProvider(new ColumnLabelProvider() {

         @Override
         public String getText(Object element) {
            nbMessage++;
            return nbMessage.toString();
         }
      });

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
               JMSDeliveryMode jmd = JMSDeliveryMode.fromValue(m.getJMSDeliveryMode());
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

   // --------------
   // Helper Classes
   // --------------

   private enum BrowseMode {
                            FULL,
                            SEARCH,
                            SELECTOR
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
                  eventBroker.post(Constants.EVENT_REFRESH_MESSAGES, jtbQueue);
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
            if (currentQueueName == null) {
               return;
            }

            CTabItem sel = mapQueueTabItem.get(currentQueueName);
            sel.dispose();
         }
      });

      MenuItem closeOthers = new MenuItem(contextMenu, SWT.NONE);
      closeOthers.setText("Close Others");
      closeOthers.addListener(SWT.Selection, new Listener() {
         @Override
         public void handleEvent(Event event) {
            log.debug("Close Others");
            if (currentQueueName == null) {
               return;
            }

            CTabItem sel = mapQueueTabItem.get(currentQueueName);
            for (CTabItem c : new ArrayList<>(mapQueueTabItem.values())) {
               if (c != sel) {
                  c.dispose();
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

            for (CTabItem c : new ArrayList<>(mapQueueTabItem.values())) {
               c.dispose();
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
      public void dragStart(DragSourceEvent event) {
         log.debug("dragStart {}", event);

         // Only allow one message at a time (for now...)
         IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
         if ((selection == null) || (selection.size() != 1)) {
            event.doit = false;
            return;
         }

         JTBMessage jtbMessage = (JTBMessage) selection.getFirstElement();
         if (jtbMessage.getJtbMessageType() == JTBMessageType.STREAM) {
            log.warn("STREAM Messages can not be dragged to templates or another Queue");
            event.doit = false;
            return;
         }

         super.dragStart(event);
      }

      @Override
      public void dragSetData(DragSourceEvent event) {
         if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
            event.data = "unused";

            // Get the selected JTBMessage
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            JTBMessage jtbMessage = (JTBMessage) selection.getFirstElement();

            // Store selected JTBMessage
            DNDData.setDrag(DNDElement.JTBMESSAGE);
            DNDData.setSourceJTBMessage(jtbMessage);
         }
      }

   }

   public final class JTBMessageDropListener extends ViewerDropAdapter {

      private JTBQueue jtbQueue;

      public JTBMessageDropListener(TableViewer tableViewer, JTBQueue jtbQueue) {
         super(tableViewer);
         this.jtbQueue = jtbQueue;
         this.setFeedbackEnabled(false); // Disable "in between" visual clues
      }

      @Override
      public void drop(DropTargetEvent event) {

         // Store the JTBDestination where the drop occurred
         log.debug("The drop was done on element: {}", jtbQueue);

         DNDData.setDrop(DNDElement.JTBDESTINATION);
         DNDData.setTargetJTBDestination(jtbQueue);

         super.drop(event);
      }

      @Override
      public boolean performDrop(Object data) {
         log.debug("performDrop : {}", DNDData.getDrag());

         ParameterizedCommand myCommand;
         Map<String, Object> parameters = new HashMap<>();
         parameters.put(Constants.COMMAND_CONTEXT_PARAM, Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP);
         myCommand = commandService.createCommand(Constants.COMMAND_MESSAGE_SEND_TEMPLATE, parameters);
         handlerService.executeHandler(myCommand);

         return true;
      }

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferType) {
         return true;
      }
   }

}
