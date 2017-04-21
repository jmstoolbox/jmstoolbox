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

import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.wb.swt.SWTResourceManager;
import org.titou10.jtb.util.Constants;

/**
 * Show the daleyed tooltip box for the auto refresh button
 * 
 * 
 * @author Denis Forveille
 * 
 */
@Deprecated
final class DelayedRefreshTooltip extends ToolTip {

   // Instantiated by:
   // new DelayedRefreshTooltip(ps.getInt(Constants.PREF_AUTO_REFRESH_DELAY), btnAutoRefresh);

   private Button btnAutoRefresh;
   private int    delay;

   DelayedRefreshTooltip(int delay, Control btnAutoRefresh) {
      super(btnAutoRefresh);

      this.delay = delay;
      this.btnAutoRefresh = (Button) btnAutoRefresh;

      this.setPopupDelay(200);
      this.setHideDelay(0);
      this.setHideOnMouseDown(false);
      this.setShift(new org.eclipse.swt.graphics.Point(-210, 0));
   }

   @Override
   protected Composite createToolTipContentArea(Event event, Composite parent) {

      Color bc = SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND);

      int margin = 8;
      GridLayout gl = new GridLayout(3, false);
      gl.marginLeft = margin;
      gl.marginRight = margin;
      gl.marginTop = margin;
      gl.marginBottom = margin;
      gl.verticalSpacing = margin;

      Composite ttComposite = new Composite(parent, SWT.BORDER_SOLID);
      ttComposite.setLayout(gl);
      ttComposite.setBackground(bc);

      Label lbl1 = new Label(ttComposite, SWT.CENTER);
      lbl1.setText("Auto refresh every");
      lbl1.setBackground(bc);

      final Spinner spinnerAutoRefreshDelay = new Spinner(ttComposite, SWT.BORDER);
      spinnerAutoRefreshDelay.setMinimum(Constants.MINIMUM_AUTO_REFRESH);
      spinnerAutoRefreshDelay.setMaximum(600);
      spinnerAutoRefreshDelay.setIncrement(1);
      spinnerAutoRefreshDelay.setPageIncrement(5);
      spinnerAutoRefreshDelay.setTextLimit(3);
      spinnerAutoRefreshDelay.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
      spinnerAutoRefreshDelay.setSelection(delay);

      Label lbl2 = new Label(ttComposite, SWT.CENTER);
      lbl2.setText("seconds");
      lbl2.setBackground(bc);

      final DelayedRefreshTooltip ctt = this;

      final Button applyButton = new Button(ttComposite, SWT.PUSH);
      applyButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
      applyButton.setText("Start auto refresh");
      applyButton.setBackground(bc);
      applyButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent event) {
            Event e = new Event();
            delay = spinnerAutoRefreshDelay.getSelection();
            e.data = Long.valueOf(delay);
            btnAutoRefresh.setSelection(true);
            btnAutoRefresh.notifyListeners(SWT.Selection, e);
            ctt.hide();
         }
      });

      return ttComposite;
   }

   @Override
   protected boolean shouldCreateToolTip(Event event) {
      // Display custom tooltip only when the refreshing is not running
      if (btnAutoRefresh.getSelection()) {
         btnAutoRefresh.setToolTipText("Refreshing every " + delay + " seconds");
         return false;
      } else {
         btnAutoRefresh.setToolTipText(null);
         return super.shouldCreateToolTip(event);
      }
   }
}
