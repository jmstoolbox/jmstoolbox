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
package org.titou10.jtb.config;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * 
 * Dynamic Splash Screen
 * 
 * @author Denis Forveille
 *
 */
public class SplashScreenDialog {

   private Shell       splashShell = null;
   private Label       textLabel   = null;
   private ProgressBar progressBar = null;

   public void open(int totalWork) {
      splashShell = createSplashShell(totalWork);
      splashShell.open();
   }

   public void setProgress(String labelText, int progress) {
      this.textLabel.setText(labelText);
      this.progressBar.setSelection(progress);
   }

   private Shell createSplashShell(int totalWork) {
      final Shell shell = new Shell(SWT.TOOL | SWT.NO_TRIM);

      Image image = SWTResourceManager.getImage(this.getClass(), "icons/splash.bmp");
      shell.setBackgroundImage(image);
      shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
      Rectangle imageBounds = image.getBounds();

      textLabel = new Label(shell, SWT.WRAP);
      Rectangle textRect = new Rectangle(4, imageBounds.height - 40, imageBounds.width - 40, 20);
      textLabel.setBounds(textRect);
      textLabel.setText("Initializing...");

      progressBar = new ProgressBar(shell, SWT.BORDER | SWT.SMOOTH | SWT.HORIZONTAL);
      progressBar.setMinimum(0);
      progressBar.setMaximum(totalWork);

      Rectangle progressRect = new Rectangle(0,
                                             imageBounds.height - 16 - progressBar.getBorderWidth(),
                                             imageBounds.width - progressBar.getBorderWidth(),
                                             16);
      progressBar.setBounds(progressRect);

      shell.setSize(imageBounds.width, imageBounds.height);
      shell.setLocation(getMonitorCenter(shell));
      return shell;
   }

   private Point getMonitorCenter(Shell shell) {
      Monitor primary = shell.getDisplay().getPrimaryMonitor();
      Rectangle bounds = primary.getBounds();
      Rectangle rect = shell.getBounds();
      int x = bounds.x + (bounds.width - rect.width) / 2;
      int y = bounds.y + (bounds.height - rect.height) / 2;
      return new Point(x, y);
   }

   public void close() {
      splashShell.close();
      textLabel = null;
      progressBar = null;
      splashShell.dispose();
      splashShell = null;
   }

}
