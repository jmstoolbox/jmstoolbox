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
package org.titou10.jtb.dialog;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.ui.navigator.NodeAbstract;
import org.titou10.jtb.ui.navigator.NodeJTBQueue;
import org.titou10.jtb.ui.navigator.NodeJTBSession;
import org.titou10.jtb.ui.navigator.NodeJTBSessionProvider;
import org.titou10.jtb.ui.navigator.NodeJTBTopic;
import org.titou10.jtb.ui.navigator.TreeLabelProvider;

/**
 * 
 * Dialog to choose a Destination
 * 
 * @author Denis Forveille
 *
 */
public class DestinationChooserDialog extends Dialog {

   private SortedSet<NodeAbstract> listNodesSession;

   private JTBDestination          selectedJTBDestination;

   public DestinationChooserDialog(Shell parentShell, JTBSession jtbSession) {
      super(parentShell);
      setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.PRIMARY_MODAL);

      listNodesSession = new TreeSet<>();
      listNodesSession.add(new NodeJTBSession(jtbSession));
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Select a Destination");
   }

   @Override
   protected Point getInitialSize() {
      return new Point(800, 600);
   }

   @Override
   protected Control createDialogArea(Composite parent) {

      TreeViewer treeViewer = new TreeViewer(parent, SWT.BORDER);
      Tree tree = treeViewer.getTree();
      tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      treeViewer.setContentProvider(new NodeJTBSessionProvider());
      treeViewer.setLabelProvider(new TreeLabelProvider());
      treeViewer.setInput(listNodesSession);
      treeViewer.expandToLevel(3);

      // Manage selections
      treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            NodeAbstract selected = (NodeAbstract) sel.getFirstElement();
            if (selected instanceof NodeJTBQueue) {
               NodeJTBQueue nodeJTBQueue = (NodeJTBQueue) selected;
               selectedJTBDestination = (JTBDestination) nodeJTBQueue.getBusinessObject();
               return;
            }
            if (selected instanceof NodeJTBTopic) {
               NodeJTBTopic nodeJTBTopic = (NodeJTBTopic) selected;
               selectedJTBDestination = (JTBDestination) nodeJTBTopic.getBusinessObject();
            }
         }
      });

      // Add a Double Clic Listener on navigator
      treeViewer.addDoubleClickListener(new IDoubleClickListener() {

         @Override
         public void doubleClick(DoubleClickEvent event) {
            ITreeSelection sel = (ITreeSelection) event.getSelection();
            NodeAbstract selected = (NodeAbstract) sel.getFirstElement();
            if ((selected instanceof NodeJTBQueue) || (selected instanceof NodeJTBTopic)) {
               selectedJTBDestination = (JTBDestination) selected.getBusinessObject();
               okPressed();
            }
         }
      });

      return parent;
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public JTBDestination getSelectedJTBDestination() {
      return selectedJTBDestination;
   }

}
