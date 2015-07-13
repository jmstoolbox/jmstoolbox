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
package org.titou10.jtb.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * ITreeContentProvider for trees that show Templates
 * 
 * @author Denis Forveille
 *
 */
public final class TemplateTreeContentProvider implements ITreeContentProvider {

   private static final Logger log = LoggerFactory.getLogger(TemplateTreeContentProvider.class);

   private boolean showFoldersOnly;

   public TemplateTreeContentProvider(boolean showFoldersOnly) {
      this.showFoldersOnly = showFoldersOnly;
   }

   public Object[] getElements(Object inputElement) {
      return (Object[]) inputElement;
   }

   public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof IFolder) {
         IFolder ifolder = (IFolder) parentElement;
         try {
            IResource[] res = ifolder.members();

            if (showFoldersOnly) {
               List<IResource> list = new ArrayList<IResource>(res.length);
               for (IResource iResource : res) {
                  if (iResource.getType() == IResource.FOLDER) {
                     list.add(iResource);
                  }
               }
               res = list.toArray(new IResource[0]);
            }

            Arrays.sort(res, new TemplateComparator());
            return res;
         } catch (CoreException e) {
            log.error("Exception occurred when retrieving templates files", e);
         }
      }

      return null;
   }

   public Object getParent(Object element) {
      if (element instanceof IFolder) {
         IFolder folder = (IFolder) element;
         return folder.getParent();
      }
      if (element instanceof IFile) {
         IFile file = (IFile) element;
         return file.getParent();
      }
      return null;
   }

   public boolean hasChildren(Object element) {
      if (element instanceof IFolder) {
         IFolder folder = (IFolder) element;
         try {
            return folder.members().length > 0;
         } catch (CoreException e) {
            log.error("Exception occurred when checking folder children existence", e);
         }
      }
      return false;
   }

   @Override
   public void dispose() {
   }

   @Override
   public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
   }

   /**
    * Sort templates first on folders, then on file
    * 
    * @author Denis Forveille
    *
    */
   private final class TemplateComparator implements Comparator<IResource> {

      @Override
      public int compare(IResource o1, IResource o2) {
         if (o1 instanceof IFolder) {
            if (o2 instanceof IFolder) {
               return o1.getName().compareTo(o2.getName());
            } else {
               return -1;
            }
         } else {
            if (o2 instanceof IFolder) {
               return 1;
            } else {
               return o1.getName().compareTo(o2.getName());
            }
         }
      }
   }
}
