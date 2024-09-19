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
package org.titou10.jtb.ui.part.content;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import jakarta.xml.bind.JAXBException;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.JTBPreferenceStore;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.cs.ColumnSystemHeader;
import org.titou10.jtb.cs.ColumnsSetsManager;
import org.titou10.jtb.cs.gen.Column;
import org.titou10.jtb.cs.gen.ColumnKind;
import org.titou10.jtb.cs.gen.ColumnsSet;
import org.titou10.jtb.cs.gen.UserProperty;
import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBSessionClientType;
import org.titou10.jtb.jms.model.JTBTopic;
import org.titou10.jtb.sessiontype.SessionTypeManager;
import org.titou10.jtb.template.TemplatesManager;
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
public class JTBSessionContentViewPart {

   private static final Logger  log                      = LoggerFactory.getLogger(JTBSessionContentViewPart.class);

   private static final int     DECORATOR_WIDTH          = 6;
   private static final int     DECORATOR_HEIGHT         = 16;
   private static final int     CLEAR_BUTTON_SIZE        = 28;
   private static final String  PAYLOAD_SEARCH_TOOLTIP   = "Filter messages with payload containing this text";
   private static final String  SELECTORS_SEARCH_TOOLTIP = "Filter messages with JMS selectors";

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
   private JTBStatusReporter    jtbStatusReporter;

   @Inject
   private JTBPreferenceStore   ps;

   @Inject
   private TemplatesManager     templatesManager;

   @Inject
   private ColumnsSetsManager   csManager;

   @Inject
   private SessionTypeManager   sessionTypeManager;

   private String               mySessionName;
   private String               currentCTabItemName;
   private SessionDef           sessionDef;

   private Map<String, TabData> mapTabData;

   private CTabFolder           tabFolder;

   private Integer              nbMessage                = 0;

   private IEclipseContext      windowContext;

   // Create the TabFolder
   @PostConstruct
   public void postConstruct(MWindow mw, final @Active MPart part, Composite parent) {

      this.mySessionName = part.getLabel();
      this.windowContext = mw.getContext();
      this.mapTabData = new HashMap<>();

      parent.setLayout(new FillLayout(SWT.HORIZONTAL));

      this.tabFolder = new CTabFolder(parent, SWT.BORDER);
      this.tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

      // // Set background color based on sessionDef
      this.sessionDef = (SessionDef) part.getTransientData().get(Constants.SESSION_TYPE_SESSION_DEF);
      if (getBackGroundColor() != null) {
         part.getTransientData()
                  .put(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY,
                       SWTResourceManager.createImageSolidColor(getBackGroundColor(), DECORATOR_WIDTH, DECORATOR_HEIGHT));
      }

      addContextMenu();

      // Dispose Listener - Called when the View (=QM) is closed
      tabFolder.addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(DisposeEvent disposeEvent) {
            log.debug("tabFolder disposed {}", disposeEvent);
            windowContext.remove(Constants.CURRENT_TAB_JTBDESTINATION);
            windowContext.remove(Constants.CURRENT_TAB_JTBSESSION);
            windowContext.remove(Constants.CURRENT_COLUMNSSET);

            // Clear Message Data
            eventBroker.post(Constants.EVENT_JTBMESSAGE_PART_REFRESH, null);
         }
      });

      // Intercept focus changes on CTabItems
      tabFolder.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         if (e.item instanceof CTabItem) {
            CTabItem tabItem = (CTabItem) e.item;
            TabData td = (TabData) tabItem.getData();
            td.tableViewer.getTable().setFocus();

            log.debug("CTabItem got focus: {}", td.type);
            // log.debug("CTabItem got focus: {}",td..);

            if (td.type == TabDataType.JTBDESTINATION) {
               currentCTabItemName = computeCTabItemName(td.jtbDestination);
               windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, td.jtbDestination);
               windowContext.remove(Constants.CURRENT_TAB_JTBSESSION);
               windowContext.set(Constants.CURRENT_COLUMNSSET, td.columnsSet);

               // Select Destination in Session Browser
               eventBroker.post(Constants.EVENT_SELECT_OBJECT_SESSION_BROWSER, td.jtbDestination);

               // Refresh Message View Part with current selection
               eventBroker.post(Constants.EVENT_JTBMESSAGE_PART_REFRESH, td.selectedJTBMessage);

            } else {
               currentCTabItemName = computeCTabItemName(td.jtbSession);
               windowContext.remove(Constants.CURRENT_TAB_JTBDESTINATION);
               windowContext.set(Constants.CURRENT_TAB_JTBSESSION, td.jtbSession);
               windowContext.remove(Constants.CURRENT_COLUMNSSET);

               // Clear Message Data
               eventBroker.post(Constants.EVENT_JTBMESSAGE_PART_REFRESH, null);
            }
         }
      }));
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
            log.debug("Menu Close Tab");
            if (currentCTabItemName == null) {
               return;
            }

            TabData td = mapTabData.get(currentCTabItemName);
            CTabItem sel = td.tabItem;
            sel.dispose();
         }
      });

      MenuItem closeOthers = new MenuItem(contextMenu, SWT.NONE);
      closeOthers.setText("Close Other Tabs");
      closeOthers.addListener(SWT.Selection, new Listener() {
         @Override
         public void handleEvent(Event event) {
            log.debug("Menu Close Others");
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
            log.debug("Menu Close All Tabs");

            for (TabData t : new ArrayList<>(mapTabData.values())) {
               t.tabItem.dispose();
            }

            // Clear Message Data
            eventBroker.post(Constants.EVENT_JTBMESSAGE_PART_REFRESH, null);
         }
      });

      tabFolder.setMenu(contextMenu);
   }

   @Focus
   public void focus() {
      log.debug("focus currentCTabItemName={}", currentCTabItemName);

      TabData td = mapTabData.get(currentCTabItemName);
      CTabItem tabItem = td.tabItem;
      tabFolder.setSelection(tabItem);
      td.tableViewer.getTable().setFocus();

      if (td.type == TabDataType.JTBDESTINATION) {
         currentCTabItemName = computeCTabItemName(td.jtbDestination);
         windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, td.jtbDestination);
         windowContext.remove(Constants.CURRENT_TAB_JTBSESSION);
         windowContext.set(Constants.CURRENT_COLUMNSSET, td.columnsSet);

         // Select Destination in Session Browser
         eventBroker.post(Constants.EVENT_SELECT_OBJECT_SESSION_BROWSER, td.jtbDestination);

         // Refresh Message View Part with current selection
         eventBroker.post(Constants.EVENT_JTBMESSAGE_PART_REFRESH, td.selectedJTBMessage);

      } else {
         currentCTabItemName = computeCTabItemName(td.jtbSession);
         windowContext.remove(Constants.CURRENT_TAB_JTBDESTINATION);
         windowContext.set(Constants.CURRENT_TAB_JTBSESSION, td.jtbSession);
         windowContext.remove(Constants.CURRENT_COLUMNSSET);

         // Clear Message Data
         eventBroker.post(Constants.EVENT_JTBMESSAGE_PART_REFRESH, null);
      }
   }

   // Set focus on the CTabItem for the destination
   @Inject
   @Optional
   private void resetBackgroundColor(final @Active MPart part,
                                     final @UIEventTopic(Constants.EVENT_REFRESH_BACKGROUND_COLOR) String useless) {
      log.debug("resetBackgroundColor");

      if (getBackGroundColor() == null) {
         part.getTransientData().remove(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY);
      } else {
         part.getTransientData()
                  .put(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY,
                       SWTResourceManager.createImageSolidColor(getBackGroundColor(), DECORATOR_WIDTH, DECORATOR_HEIGHT));
      }

      for (CTabItem tabItem : tabFolder.getItems()) {
         Control c = tabItem.getControl();
         if ((c != null) && (!c.isDisposed())) {
            tabItem.getControl().setBackground(getBackGroundColor());
         }
      }

      tabFolder.update();
   }

   // Set focus on the CTabItem for the destination
   @Inject
   @Optional
   private void setFocusCTabItemDestination(final @UIEventTopic(Constants.EVENT_FOCUS_CTABITEM) JTBDestination jtbDestination) {
      if (!isThisEventForThisPart(jtbDestination)) {
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
         windowContext.set(Constants.CURRENT_COLUMNSSET, td.columnsSet);

         // Select Destination in Session Browser
         eventBroker.post(Constants.EVENT_SELECT_OBJECT_SESSION_BROWSER, td.jtbDestination);
      }
   }

   // Set focus on the CTabItem for the Session
   @Inject
   @Optional
   private void setFocusCTabItemSession(final @UIEventTopic(Constants.EVENT_FOCUS_SYNTHETIC) JTBSession jtbSession) {
      if (!isThisEventForThisPart(jtbSession)) {
         return;
      }

      log.debug("setFocusCTabItemSession {}", jtbSession);

      currentCTabItemName = computeCTabItemName(jtbSession);
      TabData td = mapTabData.get(currentCTabItemName);
      tabFolder.setSelection(td.tabItem);
      windowContext.remove(Constants.CURRENT_TAB_JTBDESTINATION);
      windowContext.set(Constants.CURRENT_TAB_JTBSESSION, jtbSession);
      windowContext.remove(Constants.CURRENT_COLUMNSSET);
   }

   // Called to update the search text when "Copy Property as Selector" has been used..
   @Inject
   @Optional
   private void rebuildViewNewColumsSet(@UIEventTopic(Constants.EVENT_REBUILD_VIEW_NEW_CS) String noUse,
                                        @Named(Constants.CURRENT_TAB_JTBDESTINATION) JTBDestination jtbDestination) {
      if (!isThisEventForThisPart(jtbDestination)) {
         return;
      }

      log.debug("rebuildViewNewColumsSet");

      TabData td = mapTabData.get(currentCTabItemName);
      applyNewColumnSet(td, td.columnsSet);
   }

   // Called to update the search text when "Copy Property as Selector" has been used..
   @Inject
   @Optional
   private void addSelectorClause(@UIEventTopic(Constants.EVENT_ADD_SELECTOR_CLAUSE) String selector,
                                  @Named(Constants.CURRENT_TAB_JTBDESTINATION) JTBDestination jtbDestination) {
      if (!isThisEventForThisPart(jtbDestination)) {
         return;
      }

      log.debug("addSelectorClause. selector={} for {}", selector, jtbDestination);

      TabData td = mapTabData.get(currentCTabItemName);

      StringBuilder sb = new StringBuilder(128);

      if (jtbDestination.isJTBQueue()) {
         Combo c = td.selectorsSearchTextCombo;
         sb.append(c.getText());
         if (!(c.getText().trim().isEmpty())) {
            sb.append(" AND ");
         }
         sb.append(selector);
         c.setText(sb.toString());
      } else {
         Text t = td.selectorsSearchTextTopic;
         sb.append(t.getText());
         if (!(t.getText().trim().isEmpty())) {
            sb.append(" AND ");
         }
         sb.append(selector);
         t.setText(sb.toString());

      }

   }

   private boolean isThisEventForThisPart(JTBDestination jtbDestination) {
      return isThisEventForThisPart(jtbDestination.getJtbConnection().getSessionName());
   }

   private boolean isThisEventForThisPart(JTBSession jtbSession) {
      return isThisEventForThisPart(jtbSession.getName());
   }

   private boolean isThisEventForThisPart(String sessionName) {
      if (!(sessionName.equals(mySessionName))) {
         log.debug("isThisEventForThisPart. This notification is not for this part ({})...", mySessionName);
         return false;
      }
      return true;
   }

   // --------------
   // Queue Handling
   // --------------

   // Called whenever a new Queue is browsed or need to be refreshed
   @Inject
   @Optional
   private void refreshQueueMessageBrowser(Shell shell,
                                           final @UIEventTopic(Constants.EVENT_REFRESH_QUEUE_MESSAGES) JTBQueue jtbQueue) {

      if (!isThisEventForThisPart(jtbQueue)) {
         return;
      }

      log.debug("refreshQueueMessageBrowser: {}", jtbQueue);

      final String jtbQueueName = jtbQueue.getName();

      // Create one tab item per Q
      if (!mapTabData.containsKey(computeCTabItemName(jtbQueue))) {

         final TabData td = new TabData(jtbQueue);

         CTabItem tabItemQueue = new CTabItem(tabFolder, SWT.NONE);
         tabItemQueue.setShowClose(true);

         Composite composite = new Composite(tabFolder, SWT.NONE);
         composite.setLayout(new GridLayout(3, false));
         composite.setBackground(getBackGroundColor());

         // -----------
         // Search Line
         // -----------
         GridLayout glSearch = new GridLayout(5, false);
         glSearch.marginWidth = 0;
         glSearch.marginHeight = 0;

         Composite leftComposite = new Composite(composite, SWT.NONE);
         leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
         leftComposite.setLayout(glSearch);

         // ------------
         // Search boxes
         // ------------

         GridLayout glSearchBoxes = new GridLayout(3, false);
         glSearchBoxes.marginWidth = 0;
         glSearchBoxes.marginHeight = 0;
         glSearchBoxes.verticalSpacing = 2;

         Composite searchBoxesComposite = new Composite(leftComposite, SWT.NONE);
         searchBoxesComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
         searchBoxesComposite.setLayout(glSearchBoxes);

         // Payload search box
         Label lblPayload = new Label(searchBoxesComposite, SWT.NONE);
         lblPayload.setText("Payload:");
         lblPayload.setToolTipText(PAYLOAD_SEARCH_TOOLTIP);

         final Combo payloadSearchTextCombo = new Combo(searchBoxesComposite, SWT.BORDER);
         payloadSearchTextCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
         payloadSearchTextCombo.setToolTipText(PAYLOAD_SEARCH_TOOLTIP);
         payloadSearchTextCombo.addListener(SWT.DefaultSelection, new Listener() {
            @Override
            public void handleEvent(Event e) {
               // Start Refresh on Enter
               CTabItem selectedTab = tabFolder.getSelection();
               TabData td = (TabData) selectedTab.getData();
               eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, td.jtbDestination.getAsJTBQueue());
            }
         });

         // Payload search box Clear Button
         final Button clearPayloadButton = new Button(searchBoxesComposite, SWT.NONE);
         clearPayloadButton.setLayoutData(new GridData(CLEAR_BUTTON_SIZE, CLEAR_BUTTON_SIZE));
         clearPayloadButton.setImage(SWTResourceManager.getImage(this.getClass(), "icons/cross-script.png"));
         clearPayloadButton.setToolTipText("Clear payload search box");
         clearPayloadButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            payloadSearchTextCombo.setText("");
         }));

         // Selectors search box
         Label lblSelectors = new Label(searchBoxesComposite, SWT.NONE);
         lblSelectors.setText("Selectors:");
         lblSelectors.setToolTipText(SELECTORS_SEARCH_TOOLTIP);

         final Combo selectorsSearchTextCombo = new Combo(searchBoxesComposite, SWT.BORDER);
         selectorsSearchTextCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
         selectorsSearchTextCombo.setToolTipText(SELECTORS_SEARCH_TOOLTIP);
         selectorsSearchTextCombo.addListener(SWT.DefaultSelection, new Listener() {
            @Override
            public void handleEvent(Event e) {
               // Start Refresh on Enter
               CTabItem selectedTab = tabFolder.getSelection();
               TabData td = (TabData) selectedTab.getData();
               eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, td.jtbDestination.getAsJTBQueue());
            }
         });

         // Selectors search box Clear Button
         final Button clearSelectorButton = new Button(searchBoxesComposite, SWT.NONE);
         clearSelectorButton.setLayoutData(new GridData(CLEAR_BUTTON_SIZE, CLEAR_BUTTON_SIZE));
         clearSelectorButton.setImage(SWTResourceManager.getImage(this.getClass(), "icons/cross-script.png"));
         clearSelectorButton.setToolTipText("Clear selectors search box");
         clearSelectorButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            selectorsSearchTextCombo.setText("");
         }));

         // Refresh Button
         final Button btnRefresh = new Button(leftComposite, SWT.NONE);
         btnRefresh.setImage(SWTResourceManager.getImage(this.getClass(), "icons/arrow_refresh.png"));
         btnRefresh.setToolTipText("Refresh Messages (F5)");
         btnRefresh.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
         btnRefresh.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            CTabItem selectedTab = tabFolder.getSelection();
            if (selectedTab != null) {
               // Send event to refresh list of messages
               TabData td2 = (TabData) selectedTab.getData();
               eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, td2.jtbDestination.getAsJTBQueue());
            }
         }));

         // Auto Refresh Button
         final Button btnAutoRefresh = new Button(leftComposite, SWT.TOGGLE);
         btnAutoRefresh.setImage(SWTResourceManager.getImage(this.getClass(), "icons/time.png"));
         btnAutoRefresh.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
         btnAutoRefresh.setToolTipText("Set auto refresh");
         btnAutoRefresh.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
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
                  } catch (InterruptedException ex) {
                     log.warn("InterruptedException occurred", ex);
                  }
                  td.autoRefreshActive = false;
                  btnAutoRefresh.setToolTipText("Set auto refresh");
                  btnAutoRefresh.setSelection(false);
               } else {
                  // Position popup windows below the button
                  Point btnPosition = btnAutoRefresh.toDisplay(0, 0);
                  Point btnSize = btnAutoRefresh.getSize();
                  Point position = new Point(btnPosition.x - 200, btnPosition.y + btnSize.y + 40);

                  AutoRefreshPopup popup = new AutoRefreshPopup(shell, position, ps.getInt(Constants.PREF_AUTO_REFRESH_DELAY));
                  if (popup.open() != Window.OK) {
                     btnAutoRefresh.setSelection(false);
                     return;
                  }
                  job.setDelay(popup.getDelay());
                  td.autoRefreshActive = true;
                  job.schedule();
                  btnAutoRefresh.setSelection(true);
                  btnAutoRefresh.setToolTipText("Refreshing every " + popup.getDelay() + " seconds");
               }
            }
         }));
         // new DelayedRefreshTooltip(ps.getInt(Constants.PREF_AUTO_REFRESH_DELAY), btnAutoRefresh);

         // Separator
         Composite separatorComposite = new Composite(composite, SWT.NONE);
         separatorComposite.setLayout(new RowLayout());
         Label separator = new Label(separatorComposite, SWT.SEPARATOR | SWT.VERTICAL);
         RowData layoutData = new RowData();
         layoutData.height = leftComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
         separator.setLayoutData(layoutData);

         // Right Composite
         GridLayout glRefresh = new GridLayout(2, false);
         glRefresh.marginWidth = 0;

         Composite rightComposite = new Composite(composite, SWT.NONE);
         rightComposite.setLayout(glRefresh);

         final Spinner spinnerMaxMessages = new Spinner(rightComposite, SWT.BORDER | SWT.RIGHT);
         spinnerMaxMessages.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
         spinnerMaxMessages.setToolTipText("Max number of messages displayed.\n0=no limit");
         spinnerMaxMessages.setMinimum(0);
         spinnerMaxMessages.setMaximum(99999);
         spinnerMaxMessages.setIncrement(1);
         spinnerMaxMessages.setPageIncrement(50);
         spinnerMaxMessages.setTextLimit(5);
         spinnerMaxMessages.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
               td.maxMessages = spinnerMaxMessages.getSelection();
            }
         });
         spinnerMaxMessages.addSelectionListener(SelectionListener.widgetDefaultSelectedAdapter(event -> {
            TabData td2 = (TabData) tabFolder.getSelection().getData();
            eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, td2.jtbDestination.getAsJTBQueue());
         }));

         // Columns Sets
         ColumnsSet cs = csManager.getDefaultColumnSet(jtbQueue).columnsSet;
         final ComboViewer comboCS = new ComboViewer(rightComposite, SWT.READ_ONLY);
         comboCS.getCombo().setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
         comboCS.getCombo().setToolTipText("Columns Sets");
         comboCS.setContentProvider(ArrayContentProvider.getInstance());
         comboCS.setLabelProvider(LabelProvider.createTextProvider(element -> ((ColumnsSet) element).getName()));
         comboCS.setInput(csManager.getColumnsSets());
         comboCS.setSelection(new StructuredSelection(cs), true);
         comboCS.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
               ColumnsSet cs = (ColumnsSet) comboCS.getStructuredSelection().getFirstElement();
               applyNewColumnSet(td, cs);
            }
         });

         // -------------------
         // Table with Messages
         // -------------------
         final TableViewer tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);

         // Create Columns
         List<TableViewerColumn> cols = createColumns(tableViewer, true, cs);

         Table table = tableViewer.getTable();
         table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
         table.setHeaderVisible(true);
         table.setLinesVisible(true);
         tabItemQueue.setControl(composite);

         // Add a focus manager to capture the content of the cell to build the contextual menu
         new TableViewerFocusCellManager(tableViewer, new JTBFocusCellHighlighter(tableViewer, windowContext));

         // Drag and Drop
         int operations = DND.DROP_MOVE | DND.DROP_COPY;
         Transfer[] transferTypesDrag = new Transfer[] { TransferJTBMessage.getInstance(), FileTransfer.getInstance() };
         Transfer[] transferTypesDrop = new Transfer[] { TransferJTBMessage.getInstance(), TransferTemplate.getInstance(),
                                                         FileTransfer.getInstance() };
         tableViewer.addDragSupport(operations, transferTypesDrag, new MessageDragListener(tableViewer));
         tableViewer
                  .addDropSupport(operations,
                                  transferTypesDrop,
                                  new MessageDropListener(commandService, handlerService, templatesManager, tableViewer, jtbQueue));

         // Manage selections
         tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {

               // Store selected Message
               List<JTBMessage> jtbMessagesSelected = buildListJTBMessagesSelected((IStructuredSelection) event.getSelection());
               selectionService.setSelection(jtbMessagesSelected);

               // Remember selection
               td.selectedJTBMessage = null;
               if ((jtbMessagesSelected != null) && (jtbMessagesSelected.size() > 0)) {
                  td.selectedJTBMessage = jtbMessagesSelected.get(0);
               }

               // Refresh Message Viewer
               eventBroker.post(Constants.EVENT_JTBMESSAGE_PART_REFRESH, td.selectedJTBMessage);
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
         table.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
            if (e.keyCode == SWT.F5) {

               // Send event to refresh list of queues
               CTabItem selectedTab = tabFolder.getSelection();
               TabData td2 = (TabData) selectedTab.getData();
               eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, td2.jtbDestination);
            }

            if ((e.keyCode == 'a') && ((e.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL)) {
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
         }));

         // Create periodic refresh Job
         AutoRefreshJob job = new AutoRefreshJob(sync,
                                                 eventBroker,
                                                 "Auto refresh job. Messages for " + jtbQueueName,
                                                 ps.getInt(Constants.PREF_AUTO_REFRESH_DELAY),
                                                 jtbQueue);

         // Intercept closing/hiding CTabItem : Remove the CTabItem for all the lists and cancel running job when closed
         tabItemQueue.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent event) {
               log.debug("dispose CTabItem for Queue '{}'", jtbQueueName);
               AutoRefreshJob job = td.autoRefreshJob;
               job.cancel();

               mapTabData.remove(computeCTabItemName(jtbQueue));
            }
         });

         // Kind of content
         tableViewer.setContentProvider(ArrayContentProvider.getInstance());

         int maxMessages = ps.getInt(Constants.PREF_MAX_MESSAGES);
         spinnerMaxMessages.setSelection(maxMessages);

         // Select Tab Item
         tabFolder.setSelection(tabItemQueue);
         windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, jtbQueue);
         windowContext.remove(Constants.CURRENT_TAB_JTBSESSION);
         windowContext.set(Constants.CURRENT_COLUMNSSET, cs);

         // Store data into TabData
         currentCTabItemName = computeCTabItemName(jtbQueue);

         td.tabItem = tabItemQueue;
         td.tableViewer = tableViewer;
         td.autoRefreshJob = job;
         td.autoRefreshActive = false; // Auto refresh = false on creation
         td.payloadSearchText = payloadSearchTextCombo;
         td.payloadSearchItemsHistory = new ArrayList<>();
         td.selectorsSearchTextCombo = selectorsSearchTextCombo;
         td.selectorsSearchItemsHistory = new ArrayList<>();
         td.maxMessages = maxMessages;
         td.tableViewerColumns = cols;
         td.columnsSet = cs;

         tabItemQueue.setData(td);
         mapTabData.put(currentCTabItemName, td);
      }

      TabData td = mapTabData.get(computeCTabItemName(jtbQueue));

      // Load Content
      loadQueueContent(jtbQueue,
                       td.tableViewer,
                       td.payloadSearchText,
                       td.payloadSearchItemsHistory,
                       td.selectorsSearchTextCombo,
                       td.selectorsSearchItemsHistory);

      if (ps.getBoolean(Constants.PREF_AUTO_RESIZE_COLS_BROWSER)) {
         Utils.resizeTableViewer(td.tableViewer);
      }
   }

   private void loadQueueContent(final JTBQueue jtbQueue,
                                 final TableViewer tableViewer,
                                 final Combo payloadSearchTextCombo,
                                 final List<String> payloadSearchItemsHistory,
                                 final Combo selectorsSearchTextCombo,
                                 final List<String> selectorsSearchItemsHistory) {

      // Payload search text exists?
      final String payloadSearchText = payloadSearchTextCombo.getText().trim();
      String firstElement = payloadSearchItemsHistory.isEmpty() ? "" : payloadSearchItemsHistory.get(0);
      if (!(firstElement.equals(payloadSearchText))) {
         payloadSearchItemsHistory.remove(payloadSearchText);
         payloadSearchItemsHistory.add(0, payloadSearchText);
         payloadSearchTextCombo.setItems(payloadSearchItemsHistory.toArray(new String[payloadSearchItemsHistory.size()]));
         payloadSearchTextCombo.select(0);
      }

      // Selectors exists?
      final String selectorsSearchText = selectorsSearchTextCombo.getText().trim();
      firstElement = selectorsSearchItemsHistory.isEmpty() ? "" : selectorsSearchItemsHistory.get(0);
      if (!(firstElement.equals(selectorsSearchText))) {
         selectorsSearchItemsHistory.remove(selectorsSearchText);
         selectorsSearchItemsHistory.add(0, selectorsSearchText);
         selectorsSearchTextCombo.setItems(selectorsSearchItemsHistory.toArray(new String[selectorsSearchItemsHistory.size()]));
         selectorsSearchTextCombo.select(0);
      }

      // G
      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
         @Override
         public void run() {
            TabData td = mapTabData.get(computeCTabItemName(jtbQueue));

            int maxMessages = td.maxMessages == 0 ? Integer.MAX_VALUE : td.maxMessages;

            JTBConnection jtbConnection = jtbQueue.getJtbConnection();
            Integer depth = jtbConnection.getQm().getQueueDepth(jtbConnection.getJmsConnection(), jtbQueue.getName());

            nbMessage = 0;

            try {
               List<JTBMessage> messages = new ArrayList<>(256);
               messages = jtbQueue.getJtbConnection().browseQueue(jtbQueue, maxMessages, payloadSearchText, selectorsSearchText);

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
                  if (payloadSearchText.isEmpty() && selectorsSearchText.isEmpty()) {
                     tabItem.setImage(null);
                  } else {
                     tabItem.setImage(SWTResourceManager.getImage(this.getClass(), "icons/filter.png"));
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
      if (!isThisEventForThisPart(jtbTopic)) {
         return;
      }
      log.debug("clear captured messages. topic={}", jtbTopic);

      TabData td = mapTabData.get(computeCTabItemName(jtbTopic));
      td.topicMessages.clear();
      td.tableViewer.refresh();
   }

   // Called when the "Remove Topic Message" command is called
   @Inject
   @Optional
   private void removeTopicMessages(final @UIEventTopic(Constants.EVENT_TOPIC_REMOVE_MESSAGES) List<JTBMessage> messages) {
      JTBTopic jtbTopic = messages.get(0).getJtbDestination().getAsJTBTopic();
      if (!isThisEventForThisPart(jtbTopic)) {
         return;
      }

      TabData td = mapTabData.get(computeCTabItemName(jtbTopic));

      for (JTBMessage jtbMessage : messages) {
         log.debug("remove captured message {}", jtbMessage);
         td.topicMessages.remove(jtbMessage);
      }

      td.tableViewer.refresh();
   }

   // Called whenever a Topic is browsed
   @Inject
   @Optional
   private void refreshTopicMessageBrowser(final @UIEventTopic(Constants.EVENT_REFRESH_TOPIC_SHOW_MESSAGES) JTBTopic jtbTopic) {
      if (!isThisEventForThisPart(jtbTopic)) {
         return;
      }
      log.debug("create/refresh Topic Message Browser. jtbTopic={}", jtbTopic);

      final String jtbTopicName = jtbTopic.getName();

      // Get the current tab associated with the topic, create the tab is needed
      if (!mapTabData.containsKey(computeCTabItemName(jtbTopic))) {

         final TabData td = new TabData(jtbTopic);

         final CTabItem tabItemTopic = new CTabItem(tabFolder, SWT.NONE);
         tabItemTopic.setShowClose(true);
         tabItemTopic.setText(jtbTopicName);
         tabItemTopic.setImage(SWTResourceManager.getImage(this.getClass(), "icons/topics/play-2-16.png"));

         Composite composite = new Composite(tabFolder, SWT.NONE);
         composite.setLayout(new GridLayout(3, false));
         composite.setBackground(getBackGroundColor());

         // -----------
         // Search Line
         // -----------
         GridLayout glSearch = new GridLayout(5, false);
         glSearch.marginWidth = 0;
         glSearch.marginHeight = 0;

         Composite leftComposite = new Composite(composite, SWT.NONE);
         leftComposite.setLayout(glSearch);
         leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

         // Search Text
         final Label labelSearchType = new Label(leftComposite, SWT.NONE);
         labelSearchType.setText("Selectors: ");
         labelSearchType.setToolTipText("Topic Filtering");

         // Search Text
         final Text selectorsSearchText = new Text(leftComposite, SWT.BORDER);
         selectorsSearchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
         selectorsSearchText.setToolTipText("Search selector that will be used by the JMS MessageListener to filter messages");

         final Button clearButton = new Button(leftComposite, SWT.NONE);
         clearButton.setImage(SWTResourceManager.getImage(this.getClass(), "icons/cross-script.png"));
         clearButton.setToolTipText("Clear search box");
         clearButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            selectorsSearchText.setText("");
         }));

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
         GridLayout glRefresh = new GridLayout(2, false);
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
               int max = spinnerMaxMessages.getSelection();
               td.maxMessages = max == 0 ? Integer.MAX_VALUE : max;

               // Set TopicListener max messages
               if (td.topicMessageConsumer != null) {
                  try {
                     TopicListener tl = (TopicListener) td.topicMessageConsumer.getMessageListener();
                     tl.setMaxSize(td.maxMessages);
                  } catch (JMSException e1) {
                     // DF what to do here? alert user??
                     log.error("Exception when getting back the TopicListener", e);
                  }
               }

               // On tab creation, td.xx objects do not exist yet...
               if (td.topicMessages != null) {
                  // Clean messages table
                  while (td.topicMessages.size() > td.maxMessages) {
                     td.topicMessages.pollLast();
                  }
                  td.tableViewer.refresh();
               }
            }
         });

         // Columns Sets
         ColumnsSet cs = csManager.getDefaultColumnSet(jtbTopic).columnsSet;
         final ComboViewer comboCS = new ComboViewer(rightComposite, SWT.READ_ONLY);
         comboCS.getCombo().setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
         comboCS.getCombo().setToolTipText("Columns Sets");
         comboCS.setContentProvider(ArrayContentProvider.getInstance());
         comboCS.setLabelProvider(LabelProvider.createTextProvider(element -> ((ColumnsSet) element).getName()));
         comboCS.setInput(csManager.getColumnsSets());
         comboCS.setSelection(new StructuredSelection(cs), true);
         comboCS.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
               ColumnsSet cs = (ColumnsSet) comboCS.getStructuredSelection().getFirstElement();
               applyNewColumnSet(td, cs);
            }
         });

         // -------------------
         // Table with Messages
         // -------------------
         final TableViewer tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);

         // Create Columns
         td.tableViewerColumns = createColumns(tableViewer, false, cs);

         Table table = tableViewer.getTable();
         table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
         table.setHeaderVisible(true);
         table.setLinesVisible(true);
         tabItemTopic.setControl(composite);

         // Manage Content
         tableViewer.setContentProvider(ArrayContentProvider.getInstance());
         final Deque<JTBMessage> messages = new ArrayDeque<>();
         tableViewer.setInput(messages);
         spinnerMaxMessages.setSelection(ps.getInt(Constants.PREF_MAX_MESSAGES_TOPIC));

         // Drag and Drop
         int operations = DND.DROP_MOVE;
         Transfer[] transferTypesDrag = new Transfer[] { TransferJTBMessage.getInstance(), FileTransfer.getInstance() };
         Transfer[] transferTypesDrop = new Transfer[] { TransferJTBMessage.getInstance(), TransferTemplate.getInstance(),
                                                         FileTransfer.getInstance() };
         tableViewer.addDragSupport(operations, transferTypesDrag, new MessageDragListener(tableViewer));
         tableViewer
                  .addDropSupport(operations,
                                  transferTypesDrop,
                                  new MessageDropListener(commandService, handlerService, templatesManager, tableViewer, jtbTopic));

         // Manage selections
         tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {

               // Store selected Message
               List<JTBMessage> jtbMessagesSelected = buildListJTBMessagesSelected((IStructuredSelection) event.getSelection());
               selectionService.setSelection(jtbMessagesSelected);

               // Remember selection
               td.selectedJTBMessage = null;
               if ((jtbMessagesSelected != null) && (jtbMessagesSelected.size() > 0)) {
                  td.selectedJTBMessage = jtbMessagesSelected.get(0);
               }

               // Refresh Message Viewer
               eventBroker.post(Constants.EVENT_JTBMESSAGE_PART_REFRESH, td.selectedJTBMessage);
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
         table.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
            if ((e.keyCode == 'a') && ((e.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL)) {
               // Selection MUST be a List<>
               IStructuredSelection selection = new StructuredSelection(new ArrayList<>(td.topicMessages));
               tableViewer.setSelection(selection);
               return;
            }

            if (e.keyCode == SWT.DEL) {
               IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
               if (selection.isEmpty()) {
                  return;
               }

               // Call the Remove command
               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_TOPIC_MESSAGE_REMOVE, null);
               handlerService.executeHandler(myCommand);
            }
         }));

         // Intercept closing/hiding CTabItem : Remove the CTabItem for all the lists and stop the MessageListener
         tabItemTopic.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent event) {
               log.debug("dispose CTabItem for Topic '{}'", jtbTopicName);

               // Close subscription
               TabData td = mapTabData.get(computeCTabItemName(jtbTopic));
               try {
                  if (td.topicMessageConsumer != null) {
                     JTBConnection jtbConnection = jtbTopic.getJtbConnection();
                     jtbConnection.closeTopicConsumer(jtbTopic, td.topicMessageConsumer);
                     td.topicMessageConsumer = null;
                  }
               } catch (JMSException e) {
                  log.error("Exception when closing subscription", e);
               }
               mapTabData.remove(computeCTabItemName(jtbTopic));
            }
         });

         // Manage the behavior of the Stop/Start button
         btnStopStartSub.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            TabData td2 = (TabData) tabFolder.getSelection().getData();

            try {
               if (td2.topicMessageConsumer == null) {
                  // Listener is stopped, create a new one
                  log.debug("Starting subscription");

                  String selector = selectorsSearchText.getText().trim();
                  td2.topicMessageConsumer = createTopicConsumer(jtbTopic,
                                                                 tableViewer,
                                                                 tabItemTopic,
                                                                 selector,
                                                                 messages,
                                                                 td.maxMessages);
                  btnStopStartSub.setImage(SWTResourceManager.getImage(this.getClass(), "icons/topics/pause-16.png"));
                  btnStopStartSub.setToolTipText("Stop Subscription");
                  if (!selector.isEmpty()) {
                     tabItemTopic.setImage(SWTResourceManager.getImage(this.getClass(), "icons/filter.png"));
                  } else {
                     tabItemTopic.setImage(SWTResourceManager.getImage(this.getClass(), "icons/topics/play-2-16.png"));
                  }

                  log.debug("Subscription started.");
               } else {
                  // Listener is running, stop it
                  log.debug("Stopping subscription");
                  JTBConnection jtbConnection = jtbTopic.getJtbConnection();
                  jtbConnection.closeTopicConsumer(jtbTopic, td2.topicMessageConsumer);
                  td2.topicMessageConsumer = null;
                  btnStopStartSub.setImage(SWTResourceManager.getImage(this.getClass(), "icons/topics/play-2-16.png"));
                  btnStopStartSub.setToolTipText("Start Subscription");
                  tabItemTopic.setImage(SWTResourceManager.getImage(this.getClass(), "icons/topics/pause-16.png"));
                  log.debug("Subscription stopped.");
               }
            } catch (JMSException e1) {
               String msg = "An Exception occured when stopping/starting subscription";
               log.error(msg, e1);
               jtbStatusReporter.showError(msg, Utils.getCause(e1), e1.getMessage());
            }
         }));

         // --------
         // Set Data
         // --------

         int max = spinnerMaxMessages.getSelection();
         int maxMessages = max == 0 ? Integer.MAX_VALUE : max;

         // Select Tab Item
         tabFolder.setSelection(tabItemTopic);
         windowContext.set(Constants.CURRENT_TAB_JTBDESTINATION, jtbTopic);
         windowContext.remove(Constants.CURRENT_TAB_JTBSESSION);
         windowContext.set(Constants.CURRENT_COLUMNSSET, td.columnsSet);

         // Store data in TabData and CTabItem
         currentCTabItemName = computeCTabItemName(jtbTopic);

         td.tabItem = tabItemTopic;
         td.tableViewer = tableViewer;
         td.autoRefreshActive = false; // Auto refresh = false on creation
         td.maxMessages = maxMessages;
         td.topicMessages = messages;
         td.columnsSet = cs;
         td.selectorsSearchTextTopic = selectorsSearchText;

         tabItemTopic.setData(td);
         mapTabData.put(currentCTabItemName, td);

         // Create Subscriber
         try {
            td.topicMessageConsumer = createTopicConsumer(jtbTopic,
                                                          tableViewer,
                                                          tabItemTopic,
                                                          selectorsSearchText.getText().trim(),
                                                          messages,
                                                          maxMessages);

         } catch (JMSException e1) {
            String msg = "An Exception occured when initially starting the subscription";
            log.error(msg, e1);
            jtbStatusReporter.showError(msg, Utils.getCause(e1), e1.getMessage());
            return;
         }
      }

      TabData td = mapTabData.get(computeCTabItemName(jtbTopic));
      td.tableViewer.refresh();

      if (ps.getBoolean(Constants.PREF_AUTO_RESIZE_COLS_BROWSER)) {
         Utils.resizeTableViewer(td.tableViewer);
      }
   }

   private MessageConsumer createTopicConsumer(JTBTopic jtbTopic,
                                               TableViewer tableViewer,
                                               CTabItem tabItemTopic,
                                               String selector,
                                               Deque<JTBMessage> messages,
                                               int maxMessages) throws JMSException {

      TopicListener tl = new TopicListener(sync,
                                           jtbTopic,
                                           messages,
                                           tableViewer,
                                           tabItemTopic,
                                           maxMessages,
                                           !Utils.isEmpty(selector));
      JTBConnection jtbConnection = jtbTopic.getJtbConnection();
      return jtbConnection.createTopicConsumer(jtbTopic, tl, selector);
   }

   // -----------------------
   // Synthetic View Handling
   // -----------------------

   // Called whenever a new Queue is browsed or need to be refreshed
   @Inject
   @Optional
   private void refreshSyntheticView(Shell shell,
                                     final @UIEventTopic(Constants.EVENT_REFRESH_SESSION_SYNTHETIC_VIEW) JTBSession jtbSession) {
      if (!isThisEventForThisPart(jtbSession)) {
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
         composite.setBackground(getBackGroundColor());

         // -----------
         // Search Line
         // -----------
         GridLayout glSearch = new GridLayout(6, false);
         glSearch.marginWidth = 0;
         glSearch.marginHeight = 0;

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
         // Set filter from preferences
         filterText.setText(ps.getString(ps.buildPreferenceKeyForQDepthFilter(jtbSessionName)));

         final Label labelFilter2 = new Label(leftComposite, SWT.NONE);
         labelFilter2.setText("(Use '*' or '?' as wildcards)");

         // Clear Text
         final Button clearButton = new Button(leftComposite, SWT.NONE);
         clearButton.setImage(SWTResourceManager.getImage(this.getClass(), "icons/cross-script.png"));
         clearButton.setToolTipText("Clear search box");
         clearButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            filterText.setText("");
         }));

         // Refresh Button
         final Button btnRefresh = new Button(leftComposite, SWT.NONE);
         btnRefresh.setImage(SWTResourceManager.getImage(this.getClass(), "icons/arrow_refresh.png"));
         btnRefresh.setToolTipText("Refresh Messages (F5)");
         btnRefresh.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
         btnRefresh.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            CTabItem selectedTab = tabFolder.getSelection();
            if (selectedTab != null) {
               // Send event to refresh list of queues
               TabData td2 = (TabData) selectedTab.getData();
               eventBroker.send(Constants.EVENT_REFRESH_SESSION_SYNTHETIC_VIEW, td2.jtbSession);
            }
         }));

         // Auto Refresh Button
         final Button btnAutoRefresh = new Button(leftComposite, SWT.TOGGLE);
         btnAutoRefresh.setImage(SWTResourceManager.getImage(this.getClass(), "icons/time.png"));
         btnAutoRefresh.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
         btnAutoRefresh.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
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
                  } catch (InterruptedException ex) {
                     log.warn("InterruptedException occurred", e);
                  }
                  td.autoRefreshActive = false;
                  btnAutoRefresh.setToolTipText("Set auto refresh");
                  btnAutoRefresh.setSelection(false);
               } else {
                  // Position popup windows below the button
                  Point btnPosition = btnAutoRefresh.toDisplay(0, 0);
                  Point btnSize = btnAutoRefresh.getSize();
                  Point position = new Point(btnPosition.x - 200, btnPosition.y + btnSize.y + 40);

                  AutoRefreshPopup popup = new AutoRefreshPopup(shell, position, ps.getInt(Constants.PREF_AUTO_REFRESH_DELAY));
                  if (popup.open() != Window.OK) {
                     btnAutoRefresh.setSelection(false);
                     return;
                  }
                  job.setDelay(popup.getDelay());
                  td.autoRefreshActive = true;
                  job.schedule();
                  btnAutoRefresh.setSelection(true);
                  btnAutoRefresh.setToolTipText("Refreshing every " + popup.getDelay() + " seconds");
               }
            }
         }));

         // ---------------------------------------
         // Table with Queue Depths + JMS Timestamp
         // ---------------------------------------
         final TableViewer tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
         Table table = tableViewer.getTable();
         table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
         table.setHeaderVisible(true);
         table.setLinesVisible(true);
         tabItemSynthetic.setControl(composite);

         QueueDepthViewerComparator viewerComparator = new QueueDepthViewerComparator();

         // Create Columns

         TableViewerColumn col = createTableViewerColumn(tableViewer, "Queue Name", 250, SWT.NONE);
         TableColumn tabCol = col.getColumn();
         tabCol.addSelectionListener(buildQueueDepthSelectionAdapter(tableViewer, viewerComparator, tabCol, 0));
         col.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
               QueueWithDepth qwd = (QueueWithDepth) element;
               return " " + qwd.jtbQueue.getName();
            }

            @Override
            public Image getImage(Object element) {
               QueueWithDepth qwd = (QueueWithDepth) element;
               if (qwd.jtbQueue.isBrowsable()) {
                  return SWTResourceManager.getImage(this.getClass(), "icons/queue/page_white_stack.png");
               } else {
                  return SWTResourceManager.getImage(this.getClass(), "icons/queue/page_white_link.png");
               }
            }
         });

         // Set Sorting order to Queue Name down...
         table.setSortDirection(SWT.UP);
         table.setSortColumn(tabCol);

         col = createTableViewerColumn(tableViewer, "Depth", 60, SWT.RIGHT);
         tabCol = col.getColumn();
         tabCol.addSelectionListener(buildQueueDepthSelectionAdapter(tableViewer, viewerComparator, tabCol, 1));
         col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
               QueueWithDepth p = (QueueWithDepth) element;
               return p.depth == null ? "N/A" : p.depth.toString();
            }
         });

         col = createTableViewerColumn(tableViewer, "JMS Timestamp of 1st Message", 160, SWT.LEFT);
         tabCol = col.getColumn();
         tabCol.addSelectionListener(buildQueueDepthSelectionAdapter(tableViewer, viewerComparator, tabCol, 2));
         col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
               QueueWithDepth p = (QueueWithDepth) element;
               return p.firstMessageTimestamp == null ? "-" : Utils.formatTimestamp(p.firstMessageTimestamp.getTime(), false);
            }
         });

         // Manage selections
         tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
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
         table.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
            if (e.keyCode == SWT.F5) {

               // Send event to refresh list of queues
               CTabItem selectedTab = tabFolder.getSelection();
               TabData td2 = (TabData) selectedTab.getData();
               eventBroker.send(Constants.EVENT_REFRESH_SESSION_SYNTHETIC_VIEW, td2.jtbSession);
            }
         }));

         // Attach the Popup Menu
         menuService.registerContextMenu(table, Constants.SYNTHETIC_VIEW_POPUP_MENU);

         // Create periodic refresh Job
         AutoRefreshJob job = new AutoRefreshJob(sync,
                                                 eventBroker,
                                                 "Auto refresh job. Queue Depth for " + jtbSessionName,
                                                 ps.getInt(Constants.PREF_AUTO_REFRESH_DELAY),
                                                 jtbSession);

         // Create Queue Depth collection Job
         CollectQueueDepthJob cqdj = new CollectQueueDepthJob(sync,
                                                              "Collect Queue Depth job for " + jtbSessionName,
                                                              jtbSession.getJTBConnection(JTBSessionClientType.GUI),
                                                              tableViewer,
                                                              tabItemSynthetic,
                                                              tabItemSynthetic.getText());

         // Intercept closing/hiding CTabItem : Remove the CTabItem for all the lists and cancel running job when closed
         tabItemSynthetic.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent event) {
               log.debug("dispose CTabItem for Synthetic View for Session '{}'", jtbSessionName);
               AutoRefreshJob job = td.autoRefreshJob;
               job.cancel();

               mapTabData.remove(computeCTabItemName(jtbSession));
            }
         });

         // Kind of content
         tableViewer.setContentProvider(ArrayContentProvider.getInstance());

         // Comprator for column sorting
         tableViewer.setComparator(viewerComparator);

         // Select Tab Item
         tabFolder.setSelection(tabItemSynthetic);
         windowContext.remove(Constants.CURRENT_TAB_JTBDESTINATION);
         windowContext.set(Constants.CURRENT_TAB_JTBSESSION, jtbSession);
         windowContext.remove(Constants.CURRENT_COLUMNSSET);

         // Store data into TabData
         currentCTabItemName = computeCTabItemName(jtbSession);

         td.tabItem = tabItemSynthetic;
         td.tableViewer = tableViewer;
         td.autoRefreshJob = job;
         td.autoRefreshActive = false; // Auto refresh = false on creation
         td.collectQueueDepthJob = cqdj;
         td.filterText = filterText;

         tabItemSynthetic.setData(td);
         mapTabData.put(currentCTabItemName, td);

      }

      TabData td = mapTabData.get(computeCTabItemName(jtbSession));

      // Set Content
      JTBConnection jtbConnection = jtbSession.getJTBConnection(JTBSessionClientType.GUI);

      // Get Queues based on Tree Browser filter
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
                  .collect(Collectors.toCollection(TreeSet::new));
      }

      // Save filter in preferences
      String prefKey = ps.buildPreferenceKeyForQDepthFilter(jtbSessionName);
      if (filter.isEmpty()) {
         ps.remove(prefKey);
      } else {
         ps.putValue(prefKey, filter);
      }
      try {
         ps.save();
      } catch (IOException e) {
         log.error("IOException when saving preferences", e);
      }

      // Hide non browsable Queue if set in preference
      if (!(ps.getBoolean(Constants.PREF_SHOW_NON_BROWSABLE_Q))) {
         jtbQueuesFiltered = jtbQueuesFiltered.stream().filter(JTBQueue::isBrowsable)
                  .collect(Collectors.toCollection(TreeSet::new));
      }

      // Collect data asynchronously
      CollectQueueDepthJob collectQueueDepthJob = td.collectQueueDepthJob;

      // Check if the data collecting job is still running in case the frequency is too short or the user pressed refresh..
      if (collectQueueDepthJob.getState() != Job.RUNNING) {
         td.tabItem.setText("(Refreshing..)");
         collectQueueDepthJob.setJtbQueuesFiltered(jtbQueuesFiltered);

         // Start the Job
         log.debug("Starting the Queue Depth data collection job.");
         collectQueueDepthJob.schedule();
      } else {
         log.debug("Queue Depth data collection Job is already running. Data collection can't keep up with auto refresh...");
      }

      if (ps.getBoolean(Constants.PREF_AUTO_RESIZE_COLS_BROWSER)) {
         Utils.resizeTableViewer(td.tableViewer);
      }
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

   private List<TableViewerColumn> createColumns(TableViewer tv, boolean showNb, ColumnsSet columnSet) {

      List<TableViewerColumn> tvcList = new ArrayList<>();

      TableViewerColumn col;

      if (showNb) {
         col = createTableViewerColumn(tv, "#", 30, SWT.RIGHT);
         tvcList.add(col);
         col.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
               nbMessage++;
               return nbMessage.toString();
            }
         });
      }

      for (Column c : columnSet.getColumn()) {
         if (c.getColumnKind().equals(ColumnKind.SYSTEM_HEADER)) {
            ColumnSystemHeader h = ColumnSystemHeader.fromHeaderName(c.getSystemHeaderName());
            col = createTableViewerColumn(tv, h.getDisplayName(), h.getDisplayWidth(), SWT.NONE);
            tvcList.add(col);

            col.getColumn().setData(Constants.COLUMN_TYPE_COLUMN_SYSTEM_HEADER, h);

            col.setLabelProvider(new ColumnLabelProvider() {
               @Override
               public String getText(Object element) {
                  JTBMessage jtbMessage = (JTBMessage) element;
                  Object o = h.getColumnSystemValue(jtbMessage.getJmsMessage(), false, false);
                  return o == null ? "" : o.toString();
               }
            });

         } else {

            UserProperty u = c.getUserProperty();
            col = createTableViewerColumn(tv, csManager.getUserPropertyDisplayName(u, true), u.getDisplayWidth(), SWT.NONE);
            tvcList.add(col);

            col.getColumn().setData(Constants.COLUMN_TYPE_USER_PROPERTY, u);

            col.setLabelProvider(new ColumnLabelProvider() {

               @Override
               public String getText(Object element) {
                  JTBMessage jtbMessage = (JTBMessage) element;
                  return csManager.getColumnUserPropertyValueAsString(jtbMessage.getJmsMessage(), u);
               }
            });

            col.getColumn().addControlListener(new ControlAdapter() {
               @Override
               public void controlResized(ControlEvent e) {
                  TableColumn tc = (TableColumn) e.getSource();
                  int width = tc.getWidth();

                  log.debug("controlResized width:{} {}", width, tc);
                  u.setDisplayWidth(width);
                  try {
                     csManager.saveConfig();
                  } catch (JAXBException | CoreException e1) {
                     log.error("Exception occurred when saving ColumnsSets", e1);
                  }
               }
            });
         }

      }

      return tvcList;

   }

   private TableViewerColumn createTableViewerColumn(final TableViewer tableViewer,
                                                     final String title,
                                                     final int width,
                                                     final int style) {
      final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, style);
      final TableColumn column = viewerColumn.getColumn();
      column.setText(title);
      column.setWidth(width);
      column.setResizable(true);
      column.setMoveable(true);

      return viewerColumn;
   }

   private void applyNewColumnSet(TabData td, ColumnsSet cs) {
      for (TableViewerColumn c : td.tableViewerColumns) {
         c.getColumn().dispose();
      }
      nbMessage = 0;
      td.columnsSet = cs;
      td.tableViewerColumns = createColumns(td.tableViewer, true, td.columnsSet);
      td.tableViewer.refresh();

      windowContext.set(Constants.CURRENT_COLUMNSSET, cs);

      // Reset Selection
      td.tableViewer.setSelection(null);

      if (ps.getBoolean(Constants.PREF_AUTO_RESIZE_COLS_BROWSER)) {
         Utils.resizeTableViewer(td.tableViewer);
      }

      // Clear Message part
      eventBroker.post(Constants.EVENT_JTBMESSAGE_PART_REFRESH, null);

   }

   private SelectionAdapter buildQueueDepthSelectionAdapter(final TableViewer tableViewer,
                                                            final QueueDepthViewerComparator viewerComparator,
                                                            final TableColumn column,
                                                            final int index) {
      SelectionAdapter selectionAdapter = new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            viewerComparator.setColumn(index);
            int dir = viewerComparator.getDirection();
            tableViewer.getTable().setSortDirection(dir);
            tableViewer.getTable().setSortColumn(column);
            tableViewer.refresh();
            Utils.resizeTableViewerAll(tableViewer);
         }
      };
      return selectionAdapter;
   }

   private Color getBackGroundColor() {
      return sessionTypeManager.getBackgroundColorForSessionTypeName(sessionDef.getSessionType());
   }
}
