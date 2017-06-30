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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import javax.jms.JMSException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.custom.CTabItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Job that asynchronously collect Queue Depth data
 * 
 * @author Denis Forveille
 *
 */
public class CollectQueueDepthJob extends Job {

   private static final Logger log = LoggerFactory.getLogger(CollectQueueDepthJob.class);

   private final UISynchronize sync;

   private final JTBConnection jtbConnection;
   private final QManager      qm;

   private final TableViewer   tableViewer;
   private final CTabItem      tabItem;
   private final String        title;

   private SortedSet<JTBQueue> jtbQueuesFiltered;

   // ------------
   // Constructors
   // ------------

   public CollectQueueDepthJob(UISynchronize sync,
                               String name,
                               JTBConnection jtbConnection,
                               TableViewer tableViewer,
                               CTabItem tabItem,
                               String title) {
      super(name);
      this.setSystem(true);
      this.sync = sync;

      this.jtbConnection = jtbConnection;
      this.qm = jtbConnection.getQm();

      this.tableViewer = tableViewer;
      this.tabItem = tabItem;
      this.title = title;
   }

   // ---------------
   // Getters/Setters
   // ---------------

   public void setJtbQueuesFiltered(SortedSet<JTBQueue> jtbQueuesFiltered) {
      this.jtbQueuesFiltered = jtbQueuesFiltered;
   }

   // ------------------
   // Business Interface
   // ------------------

   @Override
   protected IStatus run(IProgressMonitor monitor) {

      List<QueueWithDepth> list = new ArrayList<QueueWithDepth>(jtbConnection.getJtbQueues().size());

      Date firstMessageTimestamp;
      for (JTBQueue jtbQueue : jtbQueuesFiltered) {

         try {
            firstMessageTimestamp = jtbConnection.getFirstMessageTimestamp(jtbQueue);
         } catch (JMSException e) {
            firstMessageTimestamp = null;
            log.error("JMSException occurred when calling jtbConnection.getFirstMessageTimestamp", e);
         }

         list.add(new QueueWithDepth(jtbQueue,
                                     qm.getQueueDepth(jtbConnection.getJmsConnection(), jtbQueue.getName()),
                                     firstMessageTimestamp));
      }

      // Update UI
      sync.asyncExec(new Runnable() {
         @Override
         public void run() {
            if (tableViewer.getControl().isDisposed()) {
               cancel();
               return;
            }

            tableViewer.setInput(list);
            Utils.resizeTableViewer(tableViewer);
            tabItem.setText(title);
         }
      });

      return Status.OK_STATUS;
   }

   @Override
   protected void canceling() {
      log.debug("Canceling '{}'", getName());
      super.canceling();
   }

   @Override
   public boolean belongsTo(Object family) {
      if (family instanceof String) {
         return Constants.JTB_JOBS_FAMILY.equals(family);
      }
      return false;
   }

}
