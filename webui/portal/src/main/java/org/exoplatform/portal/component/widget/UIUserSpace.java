/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.portal.component.widget;

import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Le bien thuy, 
 *          lebienthuy@gmail.com
 * Jul 11, 2006  
 */
@ComponentConfig(
  template = "system:/groovy/portal/webui/component/widget/UIUserSpace.gtmpl" 
)
public class UIUserSpace extends UIContainer {
  
  public UIUserSpace() throws Exception{
    addChild(UILoggedInfo.class, null, null);
    addChild(UIWidgets.class, null, null);
  }
}
