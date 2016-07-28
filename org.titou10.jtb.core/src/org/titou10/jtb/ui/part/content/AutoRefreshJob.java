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
import org.titou10.jtb.util.Constants;

/**
 * Job for auto refreshing Message Browsing
 * 
 * @author Denis Forveille
 *
 */
final class AutoRefreshJob extends Job {

   private static final Logger log = LoggerFactory.getLogger(AutoRefreshJob.class);

   private UISynchronize       sync;
   private IEventBroker        eventBroker;

   private JTBQueue            jtbQueue;
   private long                delaySeconds;
   private boolean             run = true;

   AutoRefreshJob(UISynchronize sync, IEventBroker eventBroker, String name, JTBQueue jtbQueue, int delaySeconds) {
      super(name);
      this.sync = sync;
      this.eventBroker = eventBroker;
      this.jtbQueue = jtbQueue;
      this.delaySeconds = delaySeconds;
   }

   public void setDelay(long delaySeconds) {
      this.delaySeconds = delaySeconds;
   }

   @Override
   protected void canceling() {
      log.debug("Canceling Job '{}'", getName());
      run = false;
      super.canceling();
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
         long n = delaySeconds * 2; // Test every 1/2 second
         while (run && (n-- > 0)) {
            try {
               TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {}
         }
      }
      return Status.OK_STATUS;
   }
}
