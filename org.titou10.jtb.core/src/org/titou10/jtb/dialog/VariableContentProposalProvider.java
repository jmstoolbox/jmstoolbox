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
package org.titou10.jtb.dialog;

import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.titou10.jtb.variable.VariablesUtils;
import org.titou10.jtb.variable.gen.Variable;

/**
 * Build the list of variables for the content assist popup
 * 
 * @author Denis Forveille
 *
 */
public class VariableContentProposalProvider implements IContentProposalProvider {

   private IContentProposal[] icp;

   public VariableContentProposalProvider(List<Variable> variables) {
      icp = new IContentProposal[variables.size()];
      int i = 0;
      for (Variable v : variables) {
         icp[i++] = new ContentProposal(VariablesUtils.buildVariableDisplayName(v), VariablesUtils.buildDescription(v));
      }
   }

   @Override
   public IContentProposal[] getProposals(String contents, int position) {
      return icp;
   }

}
