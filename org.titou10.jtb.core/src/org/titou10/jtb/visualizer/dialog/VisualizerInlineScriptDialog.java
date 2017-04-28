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
package org.titou10.jtb.visualizer.dialog;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.titou10.jtb.visualizer.VisualizersManager;
import org.titou10.jtb.visualizer.gen.Visualizer;
import org.titou10.jtb.visualizer.gen.VisualizerMessageType;

/**
 * 
 * Ask for a new Visualizer of kind "INLINE_SCRIPT"
 * 
 * @author Denis Forveille
 *
 */
public class VisualizerInlineScriptDialog extends Dialog {

   private Visualizer                  visualizer;
   private VisualizersManager          visualizersManager;

   private boolean                     showScriptLogs;
   private String                      source;
   private List<VisualizerMessageType> listMessageType;

   private Text                        textSource;
   private Button                      btnText;
   private Button                      btnBytes;
   private Button                      btnMap;

   private Button                      btnShowScriptLogs;

   public VisualizerInlineScriptDialog(Shell parentShell, Visualizer visualizer, VisualizersManager visualizersManager) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);

      this.visualizer = visualizer;
      this.visualizersManager = visualizersManager;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Add/Edit an 'Inline Script' visualizer");
   }

   protected Point getInitialSize() {
      return new Point(600, 600);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(2, false));

      Label lblNewLabel1 = new Label(container, SWT.NONE);
      lblNewLabel1.setText("Target JMS Messages: ");

      Composite compositeKind = new Composite(container, SWT.NONE);
      compositeKind.setLayout(new RowLayout(SWT.HORIZONTAL));

      btnText = new Button(compositeKind, SWT.CHECK);
      btnText.setText("TextMessage");

      btnBytes = new Button(compositeKind, SWT.CHECK);
      btnBytes.setText("BytesMessage");

      btnMap = new Button(compositeKind, SWT.CHECK);
      btnMap.setText("MapMessage");

      // Show Logs

      btnShowScriptLogs = new Button(container, SWT.CHECK);
      btnShowScriptLogs.setText("Show logs on execution? ");
      btnShowScriptLogs.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));

      // Language

      Composite compositeLanguage = new Composite(container, SWT.NONE);
      GridLayout gl = new GridLayout(2, false);
      gl.marginLeft = -5;
      compositeLanguage.setLayout(gl);
      compositeLanguage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

      Label lblNewLabel2 = new Label(compositeLanguage, SWT.NONE);
      lblNewLabel2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel2.setText("Language:");

      Label lblNewLabel3 = new Label(compositeLanguage, SWT.NONE);
      lblNewLabel3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel3.setText("JavaScript / nashorn");

      // Source

      Label lblNewLabel4 = new Label(container, SWT.NONE);
      lblNewLabel4.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      lblNewLabel4.setText("Script source code:");

      textSource = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
      textSource.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

      // Add key binding for CTRL-a -> select all
      textSource.addListener(SWT.KeyUp, new Listener() {
         public void handleEvent(Event event) {
            if (event.stateMask == SWT.MOD1 && event.keyCode == 'a') {
               ((Text) event.widget).selectAll();
            }
         }
      });

      if (visualizer != null) {
         for (VisualizerMessageType visualizerMessageType : visualizer.getTargetMsgType()) {
            switch (visualizerMessageType) {
               case BYTES:
                  btnBytes.setSelection(true);
                  break;
               case MAP:
                  btnMap.setSelection(true);
                  break;
               case TEXT:
                  btnText.setSelection(true);
                  break;
            }
         }
         btnShowScriptLogs.setSelection(visualizer.isShowScriptLogs());
         textSource.setText(visualizer.getSource());
      }

      return container;
   }

   @Override
   protected void okPressed() {

      listMessageType = new ArrayList<>();
      if (btnBytes.getSelection()) {
         listMessageType.add(VisualizerMessageType.BYTES);
      }
      if (btnMap.getSelection()) {
         listMessageType.add(VisualizerMessageType.MAP);
      }
      if (btnText.getSelection()) {
         listMessageType.add(VisualizerMessageType.TEXT);
      }
      if (listMessageType.isEmpty()) {
         btnBytes.setFocus();
         MessageDialog.openError(getShell(), "Error", "A visualizer must be associated to at least one JMS Message Type");
         return;
      }

      source = textSource.getText().trim();
      if (source.isEmpty()) {
         textSource.setFocus();
         MessageDialog.openError(getShell(), "Error", "Please enter the source code");
         return;
      }

      try {
         visualizersManager.compileScript(source);
      } catch (ScriptException e) {
         MessageDialog.openError(getShell(), "Invalid Script", e.getMessage());
         return;
      }

      showScriptLogs = btnShowScriptLogs.getSelection();

      super.okPressed();
   }

   @Override
   protected Control createButtonBar(final Composite parent) {
      Composite buttonBar = new Composite(parent, SWT.NONE);

      GridLayout layout = new GridLayout(3, false);
      layout.marginRight = -7; // DF: Magic number?
      buttonBar.setLayout(layout);

      GridData data = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
      buttonBar.setLayoutData(data);
      buttonBar.setFont(parent.getFont());

      // Help Button
      Button help = new Button(buttonBar, SWT.PUSH);
      help.setImage(SWTResourceManager.getImage(this.getClass(), "icons/help.png"));
      help.setToolTipText("Help");
      help.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent event) {
            VisualizerInlineScriptHelpDialog helpDialog = new VisualizerInlineScriptHelpDialog(getShell());
            helpDialog.open();
         }
      });

      // Other buttons on the right
      final Control buttonControl = super.createButtonBar(buttonBar);
      buttonControl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

      return buttonBar;
   }

   // ----------------
   // Standard Getters
   // ----------------
   public String getSource() {
      return source;
   }

   public List<VisualizerMessageType> getListMessageType() {
      return listMessageType;
   }

   public boolean getShowScriptLogs() {
      return showScriptLogs;
   }

}
