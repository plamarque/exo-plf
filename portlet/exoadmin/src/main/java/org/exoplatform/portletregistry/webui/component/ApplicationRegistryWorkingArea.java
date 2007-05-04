/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.portletregistry.webui.component;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Hoa Nguyen
 *          hoa.nguyen@exoplatform.com
 * Jun 23, 2006
 * 10:07:15 AM
 */
@ComponentConfig(    
    template = "app:/groovy/portletregistry/webui/component/ApplicationRegistryWorkingArea.gtmpl"
      
)
public class ApplicationRegistryWorkingArea extends UIContainer {
   
  public ApplicationRegistryWorkingArea() throws Exception {
//    addChild(UIDescription.class, null, "registryPortlet");   
//    addChild(UICategoryForm.class, null, null).setRendered(false);
//    addChild(UIInfoPortletForm.class, null, null).setRendered(false);
//    addChild(UIAvailablePortletForm.class, null, null).setRendered(false) ;
//    addChild(UIPermissionForm.class, null, null).setRendered(false);     
  }  
    
//  public void processRender(WebuiRequestContext context) throws Exception {
//    context.getWriter().append("<div id=\"").append(getId()).append("\">");
//    renderChildren(context) ;
//    context.getWriter().append("</div>");
//  }
    
}

