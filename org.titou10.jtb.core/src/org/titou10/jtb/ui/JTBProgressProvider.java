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
package org.titou10.jtb.ui;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.titou10.jtb.util.Constants;

/**
 * Progress Monitor for the application
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class JTBProgressProvider extends ProgressProvider {

   @Inject
   private MApplication  application;

   @Inject
   private EModelService service;

   @Override
   public IProgressMonitor createMonitor(Job arg0) {
      MToolControl element = (MToolControl) service.find(Constants.TOOLCONTROL_STATUS_CONTROL, application);
      return (IProgressMonitor) element.getObject();
   }

}
