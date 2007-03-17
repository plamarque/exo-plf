/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.organization.webui.component;

import org.exoplatform.webui.application.RequestContext;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : chungnv
 *          nguyenchung136@yahoo.com
 * Jun 23, 2006
 * 10:07:15 AM
 */
@ComponentConfig ()
public class UIGroupDetail extends UIContainer {
  
  public UIGroupDetail() throws Exception {
    addChild(UIGroupInfo.class, null, null) ;
    addChild(UIGroupForm.class, null, null).setRendered(false) ;
  }  
  
  public void processRender(RequestContext context) throws Exception {
    renderChildren(context) ;
  }
  
}

