/* Copyright (C) 2015-2017 Denis Forveille titou10.titou10@gmail.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>. */
package org.titou10.jtb.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.titou10.jtb.util.Constants;

/**
 * About Dialog
 * 
 * @author Denis Forveille
 *
 */
public class AboutDialog extends Dialog {

   // private static final Logger log = LoggerFactory.getLogger(AboutDialog.class);

   private static final String TITLE           = "Universal JMS Browser";
   private static final String AUTHOR_1        = "Author: ";
   private static final String AUTHOR_2        = "Denis Forveille";
   private static final String CONTRIBUTORS_1  = "Contributors:";
   private static final String CONTRIBUTORS_2  = "Yannick Beaudoin, Ralf Lehmann";
   private static final String CONTRIBUTORS_3  = "Raymond Meester (SonicMQ plugin), Monica Zhang (Solace plugin)";
   private static final String VERSION_1       = "v%s (%s)";
   private static final String VERSION_2       = "%s-%s-%s %s:%s";
   private static final String EMAIL           = "titou10.titou10@gmail.com";
   private static final String EMAIL_MAILTO    = "mailto:" + EMAIL;
   private static final String EMAIL_LINK      = "<a href=\"" + EMAIL_MAILTO + "\">" + EMAIL + "</a>";
   private static final String WEB             = "https://github.com/jmstoolbox/jmstoolbox/wiki";
   private static final String WEB_LINK        = "<a href=\"" + WEB + "\">" + WEB + "</a>";
   private static final String WEB_WIKI        = WEB + "/wiki/Home/";
   private static final String WEB_WIKI_LINK   = "<a href=\"" + WEB_WIKI + "\">" + WEB_WIKI + "</a>";

   private static final String LOGO            = "icons/branding/logo-jmstoolbox_400.jpg";

   private static final Font   TAHOMA_9        = SWTResourceManager.getFont("Tahoma", 9, SWT.NORMAL);
   private static final Font   TAHOMA_9_BOLD   = SWTResourceManager.getFont("Tahoma", 9, SWT.BOLD);
   private static final Font   TAHOMA_10       = SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL);
   private static final Font   TAHOMA_12       = SWTResourceManager.getFont("Tahoma", 12, SWT.NORMAL);
   private static final Font   VERDANA_20_BOLD = SWTResourceManager.getFont("Verdana", 20, SWT.BOLD);

   public AboutDialog(Shell parentShell) {
      super(parentShell);
      setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.PRIMARY_MODAL);
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("About");
   }

   @Override
   protected Control createDialogArea(Composite parent) {

      // Version v = FrameworkUtil.getBundle(AboutDialog.class).getVersion();
      // String version = String.format(VERSION, v.getMajor(), v.getMinor(), v.getMicro());

      String bundleVersion = org.eclipse.core.runtime.Platform.getBundle(Constants.BASE_CORE).getHeaders().get("Bundle-Version");
      int i = bundleVersion.lastIndexOf('.');
      String v1 = bundleVersion.substring(0, i);
      String v2 = bundleVersion.substring(i + 1, bundleVersion.length());// 201707100133
      // log.debug("v1='{}' v2='{}'", v1, v2);
      String d;
      if (v2.length() == 12) {
         d = String.format(VERSION_2,
                           v2.substring(0, 4),
                           v2.substring(4, 6),
                           v2.substring(6, 8),
                           v2.substring(8, 10),
                           v2.substring(10, 12));
      } else {
         d = v2;
      }
      String version = String.format(VERSION_1, v1, d);

      Composite container = (Composite) super.createDialogArea(parent);
      container.setFont(TAHOMA_12);
      GridLayout gl_container = new GridLayout(1, false);
      gl_container.marginWidth = 20;
      gl_container.verticalSpacing = 10;
      container.setLayout(gl_container);

      Label lblTitle = new Label(container, SWT.NONE);
      GridData gd_lblTitle = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
      gd_lblTitle.verticalIndent = 20;
      lblTitle.setLayoutData(gd_lblTitle);
      lblTitle.setFont(VERDANA_20_BOLD);
      lblTitle.setAlignment(SWT.CENTER);
      lblTitle.setText(TITLE);

      Label lblImage = new Label(container, SWT.NONE);
      GridData gd_lblImage = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
      gd_lblImage.verticalIndent = 20;
      lblImage.setLayoutData(gd_lblImage);
      lblImage.setAlignment(SWT.CENTER);
      lblImage.setImage(SWTResourceManager.getImage(this.getClass(), LOGO));

      Label lblVersion = new Label(container, SWT.NONE);
      lblVersion.setFont(TAHOMA_10);
      lblVersion.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
      lblVersion.setAlignment(SWT.CENTER);
      lblVersion.setText(version);

      Composite webContainer = new Composite(container, SWT.NONE);
      webContainer.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
      webContainer.setLayout(new GridLayout(2, false));

      Label lblWeb = new Label(webContainer, SWT.NONE);
      lblWeb.setText("Web Site:");

      Link webLink = new Link(webContainer, SWT.NONE);
      webLink.setFont(TAHOMA_9);
      webLink.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
      webLink.setText(WEB_LINK);
      webLink.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         Program.launch(WEB);
      }));

      Composite wikiComposite = new Composite(container, SWT.NONE);
      wikiComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
      wikiComposite.setLayout(new GridLayout(2, false));

      Label lblWikiLink = new Label(wikiComposite, SWT.NONE);
      lblWikiLink.setText("Online Manual:");

      Link webWikiLink = new Link(wikiComposite, SWT.NONE);
      webWikiLink.setFont(TAHOMA_9);
      webWikiLink.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
      webWikiLink.setText(WEB_WIKI_LINK);
      webWikiLink.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         Program.launch(WEB_WIKI);
      }));

      Composite authorComposite = new Composite(container, SWT.NONE);
      authorComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
      authorComposite.setLayout(new GridLayout(3, false));

      Label lblAuthor1 = new Label(authorComposite, SWT.NONE);
      lblAuthor1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
      lblAuthor1.setFont(TAHOMA_9_BOLD);
      lblAuthor1.setText(AUTHOR_1);
      Label lblAuthor2 = new Label(authorComposite, SWT.NONE);
      lblAuthor2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
      lblAuthor2.setFont(TAHOMA_9);
      lblAuthor2.setText(AUTHOR_2);

      Link emailLink = new Link(authorComposite, SWT.NONE);
      emailLink.setFont(TAHOMA_9);
      emailLink.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
      emailLink.setText(EMAIL_LINK);
      emailLink.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         Program.launch(EMAIL_MAILTO);
      }));

      Label lblHelper1 = new Label(container, SWT.NONE);
      lblHelper1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
      lblHelper1.setFont(TAHOMA_9_BOLD);
      lblHelper1.setText(CONTRIBUTORS_1);

      Label lblHelper2 = new Label(container, SWT.NONE);
      lblHelper2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
      lblHelper2.setFont(TAHOMA_9);
      lblHelper2.setText(CONTRIBUTORS_2);

      Label lblHelper3 = new Label(container, SWT.NONE);
      lblHelper3.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
      lblHelper3.setFont(TAHOMA_9);
      lblHelper3.setText(CONTRIBUTORS_3);

      webWikiLink.setFocus();

      return container;
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.OK_ID, "OK", true);
   }

   @Override
   protected Control createButtonBar(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);

      // create a layout with spacing and margins appropriate for the font size.
      GridLayout layout = new GridLayout();
      layout.numColumns = 0; // this is incremented by createButton
      layout.makeColumnsEqualWidth = true;
      layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
      layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
      layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
      layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
      composite.setLayout(layout);

      // GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
      GridData data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_CENTER);
      composite.setLayoutData(data);
      composite.setFont(parent.getFont());

      // Add the buttons to the button bar.
      createButtonsForButtonBar(composite);
      return composite;
   }

}
