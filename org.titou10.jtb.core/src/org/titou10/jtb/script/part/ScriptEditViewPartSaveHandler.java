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
package org.titou10.jtb.script.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.PartServiceSaveHandler;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

//@formatter:off
/**
 * Manage the Script Editor
 * 
 * Mix of :
 * - org.eclipse.e4.ui.internal.workbench.PartServiceSaveHandler
 * - org.eclipse.e4.ui.workbench.renderers.swt.WBWRenderer
 * 
 * @author Denis Forveille
 *
 */
//@formatter:on
@SuppressWarnings("restriction")
public class ScriptEditViewPartSaveHandler extends PartServiceSaveHandler {

   private IEclipseContext context;

   public ScriptEditViewPartSaveHandler(IEclipseContext context) {
      this.context = context;
   }

   @Override
   public Save promptToSave(MPart dirtyPart) {
      Shell shell = (Shell) context.get(IServiceConstants.ACTIVE_SHELL);
      Object[] elements = promptForSave(shell, Collections.singleton(dirtyPart));
      if (elements == null) {
         return Save.CANCEL;
      }

      // DF
      dirtyPart.setDirty(false);
      // DF

      return elements.length == 0 ? Save.NO : Save.YES;
   }

   @Override
   public Save[] promptToSave(Collection<MPart> dirtyParts) {
      List<MPart> parts = new ArrayList<MPart>(dirtyParts);
      Shell shell = (Shell) context.get(IServiceConstants.ACTIVE_SHELL);
      Save[] response = new Save[dirtyParts.size()];
      Object[] elements = promptForSave(shell, parts);
      if (elements == null) {
         Arrays.fill(response, Save.CANCEL);
      } else {
         Arrays.fill(response, Save.NO);
         for (int i = 0; i < elements.length; i++) {
            response[parts.indexOf(elements[i])] = Save.YES;
         }
         for (MPart object : dirtyParts) {
            // DF
            object.setDirty(false);
            // DF
         }
      }
      return response;
   }

   private Object[] promptForSave(Shell parentShell, Collection<MPart> saveableParts) {
      SaveablePartPromptDialog dialog = new SaveablePartPromptDialog(context, parentShell, saveableParts);
      if (dialog.open() == Window.CANCEL) {
         return null;
      }

      return dialog.getCheckedElements();
   }

   private class SaveablePartPromptDialog extends Dialog {

      private IEclipseContext     context;
      private Collection<MPart>   collection;

      private CheckboxTableViewer tableViewer;

      private Object[]            checkedElements = new Object[0];

      public SaveablePartPromptDialog(IEclipseContext context, Shell shell, Collection<MPart> collection) {
         super(shell);
         this.context = context;
         this.collection = collection;
      }

      @Override
      protected void configureShell(Shell newShell) {
         super.configureShell(newShell);
         newShell.setText("Chose scripts to save");
      }

      @Override
      protected Control createDialogArea(Composite parent) {
         parent = (Composite) super.createDialogArea(parent);

         Label label = new Label(parent, SWT.LEAD);
         label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
         label.setText("Chose scripts to save");

         tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.SINGLE | SWT.BORDER);
         GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
         data.heightHint = 250;
         data.widthHint = 300;
         tableViewer.getControl().setLayoutData(data);
         tableViewer.setLabelProvider(LabelProvider.createTextProvider(element -> ((MPart) element).getLocalizedLabel()));
         tableViewer.setContentProvider(ArrayContentProvider.getInstance());
         tableViewer.setInput(collection);
         tableViewer.setAllChecked(true);

         return parent;
      }

      @Override
      public void create() {
         super.create();
         applyDialogStyles(getShell());
      }

      @Override
      protected void okPressed() {
         checkedElements = tableViewer.getCheckedElements();
         super.okPressed();
      }

      public Object[] getCheckedElements() {
         return checkedElements;
      }

      @Override
      protected boolean isResizable() {
         return true;
      }

      private void applyDialogStyles(Control control) {
         IStylingEngine engine = (IStylingEngine) context.get(IStylingEngine.class.getName());
         if (engine != null) {
            Shell shell = control.getShell();
            if (shell.getBackgroundMode() == SWT.INHERIT_NONE) {
               shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
            }
            engine.style(shell);
         }
      }
   }

}
