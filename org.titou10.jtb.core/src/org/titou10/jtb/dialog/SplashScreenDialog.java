/*
 * Copyright (C) 2025 Denis Forveille titou10.titou10@gmail.com
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
public final class SplashScreenDialog {

   private static final int LABEL_HEIGHT       = 24;
   private static final int LABEL_MARGIN       = 2;
   private static final int PROGRESSBAR_HEIGHT = 16;

   private Shell            splashShell;
   private Label            textLabel;
   private ProgressBar      progressBar;

   private int              progress;
   private boolean          opened             = false;

   public void setProgress(String labelText) {
      if (opened) {
         splashShell.getDisplay().syncExec(() -> {
            textLabel.setText(labelText);
            progressBar.setSelection(progress++);
            splashShell.update();
         });
      }
   }

   public void open(int totalWork) {
      this.splashShell = createSplashShell(totalWork);
      this.splashShell.open();
      this.opened = true;
   }

   public void close() {
      if (opened) {
         splashShell.close();
         splashShell.dispose();
      }
   }

   // -------
   // Helpers
   // -------

   private Shell createSplashShell(int totalWork) {
      final Shell shell = new Shell(SWT.TOOL | SWT.NO_TRIM);

      Image image = SWTResourceManager.getImage(this.getClass(), "splash.bmp");
      shell.setBackgroundImage(image);
      shell.setBackgroundMode(SWT.INHERIT_FORCE);
      Rectangle imageBounds = image.getBounds();

      textLabel = new Label(shell, SWT.WRAP);
      Rectangle textRect = new Rectangle(LABEL_MARGIN,
                                         imageBounds.height - LABEL_HEIGHT - PROGRESSBAR_HEIGHT,
                                         imageBounds.width - 2 * LABEL_MARGIN,
                                         LABEL_HEIGHT);
      textLabel.setBounds(textRect);

      progressBar = new ProgressBar(shell, SWT.BORDER | SWT.SMOOTH | SWT.HORIZONTAL);
      progressBar.setMinimum(0);
      progressBar.setMaximum(totalWork);

      Rectangle progressRect = new Rectangle(0, imageBounds.height - PROGRESSBAR_HEIGHT, imageBounds.width, PROGRESSBAR_HEIGHT);
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

}
