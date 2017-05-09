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
package org.titou10.jtb.template;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * 
 * ITreeContentProvider for trees that show Templates
 * 
 * @author Denis Forveille
 *
 */
public final class TemplateTreeContentProvider2 implements ITreeContentProvider {

   // private static final Logger log = LoggerFactory.getLogger(TemplateTreeContentProvider2.class);

   private boolean showFoldersOnly;

   // -----------
   // Constructor
   // -----------
   public TemplateTreeContentProvider2(boolean showFoldersOnly) {
      this.showFoldersOnly = showFoldersOnly;
   }

   // -----------
   // Interface
   // -----------

   @Override
   public boolean hasChildren(Object element) {
      IFileStore file = (IFileStore) element;
      return file.fetchInfo().isDirectory();
   }

   @Override
   public Object getParent(Object element) {
      IFileStore file = (IFileStore) element;
      return file.getParent();
   }

   @Override
   public Object[] getElements(Object inputElement) {
      return (Object[]) inputElement;

   }

   @Override
   public Object[] getChildren(Object parentElement) {
      // if (showFoldersOnly) {
      IFileStore file = (IFileStore) parentElement;
      try {
         return file.childStores(EFS.NONE, new NullProgressMonitor());
      } catch (CoreException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return new Object[0];
   }

   @Override
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
   }

   @Override
   public void dispose() {
   }
}
