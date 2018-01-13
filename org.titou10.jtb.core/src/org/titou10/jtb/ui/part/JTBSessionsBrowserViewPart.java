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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.config.JTBPreferenceStore;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBSessionClientType;
import org.titou10.jtb.sessiontype.SessionTypeManager;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.ui.dnd.TransferJTBMessage;
import org.titou10.jtb.ui.dnd.TransferTemplate;
import org.titou10.jtb.ui.navigator.NodeAbstract;
import org.titou10.jtb.ui.navigator.NodeFolder;
import org.titou10.jtb.ui.navigator.NodeJTBQueue;
import org.titou10.jtb.ui.navigator.NodeJTBSession;
import org.titou10.jtb.ui.navigator.NodeJTBSessionProvider;
import org.titou10.jtb.ui.navigator.NodeJTBTopic;
import org.titou10.jtb.ui.navigator.NodeTreeLabelProvider;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the View Part with JTBSessions
 * 
 * @author Denis Forveille
 * 
 */
@SuppressWarnings("restriction")
public class JTBSessionsBrowserViewPart {

   private static final Logger log = LoggerFactory.getLogger(JTBSessionsBrowserViewPart.class);

   @Inject
   private ECommandService     commandService;

   @Inject
   private EHandlerService     handlerService;

   @Inject
   private ESelectionService   selectionService;

   @Inject
   private EMenuService        menuService;

   @Inject
   private ConfigManager       cm;

   @Inject
   private JTBPreferenceStore  ps;

   @Inject
   private TemplatesManager    templatesManager;

   @Inject
   private SessionTypeManager  sessionTypeManager;

   private TreeViewer          treeViewer;

   @PostConstruct
   public void createControls(Composite parent) {
      log.debug("createControls");

      parent.setLayout(new GridLayout(1, false));

      SortedSet<NodeAbstract> listNodesSession = buildSessionList();

      // Build navigator
      treeViewer = new TreeViewer(parent, SWT.BORDER);
      treeViewer.setContentProvider(new NodeJTBSessionProvider());
      treeViewer.setLabelProvider(new NodeTreeLabelProvider(sessionTypeManager, JTBSessionClientType.GUI));
      treeViewer.setInput(listNodesSession);

      // Drag and Drop
      int operations = DND.DROP_MOVE;
      Transfer[] transferTypesDrop = new Transfer[] { TransferJTBMessage.getInstance(), TransferTemplate.getInstance(),
                                                      FileTransfer.getInstance() };

      treeViewer.addDropSupport(operations,
                                transferTypesDrop,
                                new JTBSessionBrowserDropListener(commandService, handlerService, templatesManager, treeViewer));

      Tree tree = treeViewer.getTree();
      tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

      // Add a Double Clic Listener on navigator
      treeViewer.addDoubleClickListener(new IDoubleClickListener() {

         @Override
         public void doubleClick(DoubleClickEvent event) {
            ITreeSelection sel = (ITreeSelection) event.getSelection();
            NodeAbstract selected = (NodeAbstract) sel.getFirstElement();

            // Double clic on JTBSession: Connect or Disconnect
            if (selected instanceof NodeJTBSession) {
               NodeJTBSession s = (NodeJTBSession) selected;
               JTBSession j = (JTBSession) s.getBusinessObject();

               if (j.getJTBConnection(JTBSessionClientType.GUI).isConnected()) {
                  // Call Session Disconnect Command
                  ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_SESSION_DISCONNECT, null);
                  handlerService.executeHandler(myCommand);
               } else {
                  // Call Session Connect Command
                  ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_SESSION_CONNECT, null);
                  handlerService.executeHandler(myCommand);
               }

               return;
            }

            // Double clic on JTBQueue: Display Message on the right
            if (selected instanceof NodeJTBQueue) {
               // Call Browse Queue Command
               Map<String, Object> parameters = new HashMap<>();
               parameters.put(Constants.COMMAND_CONTEXT_PARAM, Constants.COMMAND_CONTEXT_PARAM_QUEUE);
               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_QUEUE_BROWSE, parameters);
               handlerService.executeHandler(myCommand);
               return;
            }

            // Double clic on JTBTopic: Display Messages on the right
            if (selected instanceof NodeJTBTopic) {
               // Call Subscribe to Topic Command
               Map<String, Object> parameters = new HashMap<>();
               parameters.put(Constants.COMMAND_TOPIC_SUBSCRIBE_PARAM, Constants.COMMAND_TOPIC_SUBSCRIBE_PARAM_TOPIC);
               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_TOPIC_SUBSCRIBE, parameters);
               handlerService.executeHandler(myCommand);
               return;
            }

            // Double clic on Queue Folder: Display Queue Depth
            if (Utils.isQueueFolder(selected)) {
               // Call Show Synthetic View Command
               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_SESSION_SYNTHETIC_VIEW, null);
               handlerService.executeHandler(myCommand);
               return;
            }
         }
      });

      treeViewer.getControl().addKeyListener(KeyListener.keyReleasedAdapter(e -> {
         if (e.keyCode == SWT.DEL) {
            IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
            NodeAbstract selected = (NodeAbstract) selection.getFirstElement();
            if (selection.isEmpty()) {
               return;
            }
            if (selected instanceof NodeJTBSession) {
               NodeJTBSession s = (NodeJTBSession) selected;
               JTBSession j = (JTBSession) s.getBusinessObject();
               log.debug("Del key pressed to remove {} ", j);

               // Call Remove Queue Command
               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_SESSION_REMOVE, null);
               handlerService.executeHandler(myCommand);
            }
         }
      }));

      // Manage selections
      treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            selectionService.setSelection(selection.getFirstElement());
         }
      });

      // Attach Popup Menu
      menuService.registerContextMenu(tree, Constants.SESSION_POPUP_MENU);

   }

   @Inject
   @Optional
   public void refreshSessionBrowserReload(@UIEventTopic(Constants.EVENT_REFRESH_SESSION_BROWSER) Boolean reload) {
      log.debug("refreshSessionBrowserReload. Reload={}", reload);

      if (reload != null) {
         Object[] expandedElements = treeViewer.getExpandedElements();
         TreePath[] expandedTreePaths = treeViewer.getExpandedTreePaths();

         treeViewer.setInput(buildSessionList());

         treeViewer.setExpandedElements(expandedElements);
         treeViewer.setExpandedTreePaths(expandedTreePaths);
      }
   }

   @Inject
   @Optional
   public void refreshSessionBrowserForJTBSession(@UIEventTopic(Constants.EVENT_REFRESH_SESSION_BROWSER) NodeJTBSession nodeJTBSession) {
      log.debug("refreshSessionBrowserForJTBSession for {}", nodeJTBSession);

      // Toggle expand/collapse state of a node
      JTBSession jtBSession = (JTBSession) nodeJTBSession.getBusinessObject();
      if (jtBSession.getJTBConnection(JTBSessionClientType.GUI).isConnected()) {
         if (treeViewer.getExpandedState(nodeJTBSession)) {
            treeViewer.collapseToLevel(nodeJTBSession, AbstractTreeViewer.ALL_LEVELS);
         } else {
            treeViewer.expandToLevel(nodeJTBSession, AbstractTreeViewer.ALL_LEVELS);
         }
      } else {
         treeViewer.collapseToLevel(nodeJTBSession, AbstractTreeViewer.ALL_LEVELS);
      }
      treeViewer.refresh(nodeJTBSession);
   }

   @Inject
   @Optional
   @SuppressWarnings("unchecked")
   // Listen to Tab Selection in the Queue Message Browser and select the corresponding Destination Node
   public void selectNode(@UIEventTopic(Constants.EVENT_SELECT_OBJECT_SESSION_BROWSER) JTBDestination jtbDestination) {

      if (!(ps.getBoolean(Constants.PREF_SYNCHRONIZE_SESSIONS_MESSAGES))) {
         // Disabled by preference
         return;
      }

      log.debug("Synchronize Session TreeViewer with {}", jtbDestination);

      // Optimize if the jtbDestination is already selected
      ITreeSelection selection = treeViewer.getStructuredSelection();
      if (selection != null) {
         NodeAbstract n = (NodeAbstract) selection.getFirstElement();
         if (n.getBusinessObject() == jtbDestination) {
            // log.debug("same selection");
            return;
         }
      }

      NodeAbstract na = findNodeDestination((SortedSet<? extends NodeAbstract>) treeViewer.getInput(), jtbDestination);
      // log.debug("Found: {}", na);
      if (na != null) {
         treeViewer.reveal(na);
         treeViewer.setSelection(new StructuredSelection(na));
      }
   }

   // -------
   // Helpers
   // -------
   private SortedSet<NodeAbstract> buildSessionList() {
      log.debug("buildSessionList");

      SortedSet<NodeAbstract> listNodesSession = new TreeSet<>();

      // Build the list of JTBSessions
      for (JTBSession session : cm.getJtbSessions()) {
         String folderName = session.getSessionDef().getFolder();
         if (folderName == null) {
            // No folder, add the session as-is
            listNodesSession.add(new NodeJTBSession(session, JTBSessionClientType.GUI));
         } else {
            // Create or reuse folder
            NodeFolder<NodeJTBSession> folder = findExistingFolder(listNodesSession, folderName);
            if (folder == null) {
               // Folder does not exist yet, create a new Folder and add the session as child
               SortedSet<NodeJTBSession> xx = new TreeSet<>();
               xx.add(new NodeJTBSession(session, JTBSessionClientType.GUI));
               folder = new NodeFolder<NodeJTBSession>(folderName, null, xx);
               listNodesSession.add(folder);
            } else {
               // Folder exists, add the session as child
               folder.addChild(new NodeJTBSession(session, JTBSessionClientType.GUI));
            }
         }
      }
      return listNodesSession;
   }

   @SuppressWarnings("unchecked")
   private NodeFolder<NodeJTBSession> findExistingFolder(SortedSet<NodeAbstract> listNodesSession, String folderName) {
      for (NodeAbstract nodeAbstract : listNodesSession) {
         if (nodeAbstract instanceof NodeFolder) {
            if (nodeAbstract.getName().equals(folderName)) {
               return (NodeFolder<NodeJTBSession>) nodeAbstract;
            }
         }
      }
      return null;
   }

   // Find a Node given a JTBDestination or JTBSession
   private NodeAbstract findNodeDestination(SortedSet<? extends NodeAbstract> listNodesSession, JTBDestination jtbDestination) {
      for (NodeAbstract nodeAbstract : listNodesSession) {
         if (nodeAbstract.getChildren() != null) {
            if ((nodeAbstract instanceof NodeFolder) || (nodeAbstract instanceof NodeJTBSession)) {
               NodeAbstract na = findNodeDestination(nodeAbstract.getChildren(), jtbDestination);
               if (na != null) {
                  return na;
               }
            }
         }
         if (nodeAbstract.getBusinessObject() == jtbDestination) {
            return nodeAbstract;
         }
      }
      return null;
   }

}
