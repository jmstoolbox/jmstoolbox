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
package org.titou10.jtb.visualizer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.titou10.jtb.visualizer.gen.Visualizer;
import org.titou10.jtb.visualizer.gen.VisualizerSourceKind;

/**
 * Utility class to manage "Visualizers"
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class VisualizersUtils {

   public static List<Visualizer> getSystemVisualizers() {
      List<Visualizer> list = new ArrayList<>(6);

      list.add(buildVisualizerBuiltInExternal(true, "PDF", "pdf"));
      list.add(buildVisualizerBuiltInExternal(true, "ZIP", "zip"));
      list.add(buildVisualizerBuiltInExternal(true, "Excel", "xls"));

      return list;
   }

   public static Visualizer buildVisualizerBuiltInExternal(boolean system, String name, String extension) {
      Visualizer v = new Visualizer();
      v.setSourceKind(VisualizerSourceKind.BUILTIN_EXTERNAL);
      v.setSystem(system);
      v.setName(name);
      v.setExtension(extension);

      return v;
   }
}
