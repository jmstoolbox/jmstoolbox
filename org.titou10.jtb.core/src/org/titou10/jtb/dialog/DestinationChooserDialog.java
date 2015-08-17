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

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBTopic;

/**
 * 
 * Dialog to choose a Destination
 * 
 * @author Denis Forveille
 *
 */
public class DestinationChooserDialog extends Dialog {

   SortedSet<JTBDestination> jtbDestinations;

   private JTBDestination selectedJTBDestination;

   private Table table;

   public DestinationChooserDialog(Shell parentShell, JTBSession jtbSession) {
      super(parentShell);
      setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.PRIMARY_MODAL);

      // Build list of destinations

      jtbDestinations = new TreeSet<>(new Comparator<JTBDestination>() {
         @Override
         public int compare(JTBDestination o1, JTBDestination o2) {
            if (o1 instanceof JTBQueue) {
               // Queues First
               if (o2 instanceof JTBTopic) {
                  return -1;
               } else {
                  return o1.getName().compareTo(o2.getName());
               }
            } else {
               // Queues First
               if (o2 instanceof JTBQueue) {
                  return 1;
               } else {
                  return o1.getName().compareTo(o2.getName());
               }
            }
         }
      });
      jtbDestinations.addAll(jtbSession.getJtbQueues());
      jtbDestinations.addAll(jtbSession.getJtbTopics());
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Select a Destination");
   }

   @Override
   protected Point getInitialSize() {
      return new Point(600, 400);
   }

   @Override
   protected Control createDialogArea(Composite parent) {

      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

      TableColumnLayout tcl = new TableColumnLayout();
      container.setLayout(tcl);

      TableViewer tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
      table = tableViewer.getTable();
      table.setHeaderVisible(true);
      table.setLinesVisible(true);

      TableViewerColumn sessionNameColumnViewer = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn sessionNameColumn = sessionNameColumnViewer.getColumn();
      tcl.setColumnData(sessionNameColumn, new ColumnWeightData(1, 50, true));
      sessionNameColumn.setAlignment(SWT.LEFT);
      sessionNameColumnViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            JTBDestination d = (JTBDestination) element;
            if (d instanceof JTBQueue) {
               return "[Q] " + d.getName();
            } else {
               return "[T] " + d.getName();
            }
         }
      });

      tableViewer.setContentProvider(new ArrayContentProvider());
      tableViewer.setInput(jtbDestinations);

      // Manage selections
      tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            selectedJTBDestination = (JTBDestination) sel.getFirstElement();
         }
      });

      // Add a Double Click Listener
      tableViewer.addDoubleClickListener(new IDoubleClickListener() {

         @Override
         public void doubleClick(DoubleClickEvent event) {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            selectedJTBDestination = (JTBDestination) sel.getFirstElement();
            okPressed();
         }
      });

      return container;

   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public JTBDestination getSelectedJTBDestination() {
      return selectedJTBDestination;
   }

}
