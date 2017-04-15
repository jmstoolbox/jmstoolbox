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

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class exposed to external Scripts used by Visualizers
 * 
 * 
 * @author Denis Forveille
 *
 */
public class VisualizerScriptsHook {

   private static final Logger log = LoggerFactory.getLogger(VisualizersManager.class);

   private VisualizersManager  visualizersManager;

   VisualizerScriptsHook(VisualizersManager visualizersManager) {
      this.visualizersManager = visualizersManager;
   }

   public void showContent(String extension, String text) throws IOException {
      log.debug("showContent - String");
      try {
         visualizersManager.launchExternalExtension(extension, text);
      } catch (IOException e) {
         log.error("Exception occured when trying to call external visualizer", e);
         throw e;
      }
   }

   public void showContent(String extension, byte[] bytes) throws IOException {
      log.debug("showText - Bytes");
      try {
         visualizersManager.launchExternalExtension(extension, bytes);
      } catch (IOException e) {
         log.error("Exception occured when trying to call external visualizer", e);
         throw e;
      }
   }

   public void showContent(String extension, Map<String, Object> map) throws IOException {
      log.debug("showText - Map");
      try {
         visualizersManager.launchExternalExtension(extension, map);
      } catch (IOException e) {
         log.error("Exception occured when trying to call external visualizer", e);
         throw e;
      }
   }
}
