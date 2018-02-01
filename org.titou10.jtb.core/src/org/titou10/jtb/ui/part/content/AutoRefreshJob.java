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

import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UISynchronize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.util.Constants;

/**
 * Job for auto refreshing Message Browsing
 * 
 * @author Denis Forveille
 *
 */
final class AutoRefreshJob extends Job {

   private static final Logger log = LoggerFactory.getLogger(AutoRefreshJob.class);

   private final UISynchronize sync;
   private final IEventBroker  eventBroker;

   private long                delaySeconds;
   private boolean             run = true;

   private JTBQueue            jtbQueue;
   private JTBSession          jtbSession;

   // ------------
   // Constructors
   // ------------

   private AutoRefreshJob(UISynchronize sync, IEventBroker eventBroker, String name, int delaySeconds) {
      super(name);
      this.setSystem(true);
      this.sync = sync;
      this.eventBroker = eventBroker;
      this.delaySeconds = delaySeconds;

   }

   AutoRefreshJob(UISynchronize sync, IEventBroker eventBroker, String name, int delaySeconds, JTBQueue jtbQueue) {
      this(sync, eventBroker, name, delaySeconds);
      this.jtbQueue = jtbQueue;
   }

   AutoRefreshJob(UISynchronize sync, IEventBroker eventBroker, String name, int delaySeconds, JTBSession jtbSession) {
      this(sync, eventBroker, name, delaySeconds);
      this.jtbSession = jtbSession;
   }

   // ---------------
   // Getters/Setters
   // ---------------

   public void setDelay(long delaySeconds) {
      this.delaySeconds = delaySeconds;
   }

   // ------------------
   // Business Interface
   // ------------------

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
               // Send event to refresh list of messages or queue List
               if (jtbQueue != null) {
                  eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, jtbQueue);
               } else {
                  eventBroker.send(Constants.EVENT_REFRESH_SESSION_SYNTHETIC_VIEW, jtbSession);
               }
            }
         });
         long n = delaySeconds * 4; // Test every 1/4 second
         while (run && (n-- > 0)) {
            try {
               TimeUnit.MILLISECONDS.sleep(250);
            } catch (InterruptedException e) {}
         }
      }
      return Status.OK_STATUS;
   }

   @Override
   protected void canceling() {
      log.debug("Canceling '{}'", getName());
      run = false;
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
