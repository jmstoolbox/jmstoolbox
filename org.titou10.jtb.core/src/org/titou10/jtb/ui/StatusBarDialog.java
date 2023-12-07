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

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bottom status bar progress report
 * 
 * @author Denis Forveille
 *
 */
public class StatusBarDialog implements IProgressMonitor {

   private static final Logger log = LoggerFactory.getLogger(StatusBarDialog.class);

   private Label               lblStatusText;
   private ProgressBar         progressBar;

   @Inject
   private UISynchronize       sync;

   @PostConstruct
   public void createControls(Composite parent) {
      GridLayout gl_parent = new GridLayout(1, false);
      gl_parent.marginWidth = 2;
      gl_parent.marginHeight = 2;
      parent.setLayout(gl_parent);

      Composite composite = new Composite(parent, SWT.NONE);
      GridLayout gl_composite = new GridLayout(2, false);
      gl_composite.horizontalSpacing = 0;
      gl_composite.verticalSpacing = 0;
      gl_composite.marginWidth = 0;
      gl_composite.marginHeight = 0;
      composite.setLayout(gl_composite);
      composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));

      lblStatusText = new Label(composite, SWT.NONE);
      GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
      gd_lblNewLabel.widthHint = 150;
      lblStatusText.setLayoutData(gd_lblNewLabel);
      lblStatusText.setText("");

      progressBar = new ProgressBar(composite, SWT.SMOOTH);
   }

   @Override
   public void beginTask(final String name, final int totalWork) {
      sync.syncExec(new Runnable() {
         @Override
         public void run() {
            progressBar.setMaximum(totalWork);
            progressBar.setToolTipText(name);
            lblStatusText.setText(name);
         }
      });
      log.debug("Starting");
   }

   @Override
   public void worked(final int work) {
      sync.syncExec(new Runnable() {
         @Override
         public void run() {
            log.debug("Worked");
            progressBar.setSelection(progressBar.getSelection() + work);
         }
      });
   }

   @Override
   public void done() {
      sync.syncExec(new Runnable() {
         @Override
         public void run() {
            lblStatusText.setText("");
            progressBar.setSelection(0);
         }
      });
      log.debug("Done");
   }

   @Override
   public void internalWorked(double arg0) {
   }

   @Override
   public boolean isCanceled() {
      return false;
   }

   @Override
   public void setCanceled(boolean arg0) {
   }

   @Override
   public void setTaskName(String arg0) {
   }

   @Override
   public void subTask(String arg0) {
   }
}
