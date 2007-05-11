/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.portletregistry.webui.component;

import java.util.Calendar;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTextAreaInput;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.validator.EmptyFieldValidator;
import org.exoplatform.webui.component.validator.NameValidator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Viet Chung
 *          nguyenchung136@yahoo.com
 * Jul 28, 2006  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/component/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(phase = Phase.DECODE, listeners = UIInfoPortletForm.BackActionListener.class),
      @EventConfig(listeners = UIInfoPortletForm.SaveActionListener.class)
    }
)
public class UIInfoPortletForm extends UIForm {  

  private Application portlet_ ;
  
  private String name_;
  
  public UIInfoPortletForm() throws Exception {
    addUIFormInput(new UIFormStringInput("applicationName", "applicationName", null).
                   addValidator(EmptyFieldValidator.class).addValidator(NameValidator.class)) ;
    
    addUIFormInput(new UIFormStringInput("displayName", "displayName", null));
    addUIFormInput(new UIFormTextAreaInput("description", "description", null));
  }
  
  public String getName() {return name_;}
  public void setName(String name) {name_ = name;}

  public void setValues(Application portlet) throws Exception {
    portlet_ = portlet;
    getUIStringInput("applicationName").setEditable(false);
    invokeGetBindingBean(portlet) ;
  }

  public Application getPortlet() { return portlet_ ; }

  static public class SaveActionListener extends EventListener<UIInfoPortletForm> {
    public void execute(Event<UIInfoPortletForm> event) throws Exception{
      UIInfoPortletForm uiForm = event.getSource() ;
      UIPopupWindow parent = uiForm.getParent();
      parent.setShow(false);
      ApplicationRegistryService service = uiForm.getApplicationComponent(ApplicationRegistryService.class) ;
      Application portlet = uiForm.getPortlet() ;
      uiForm.invokeSetBindingBean(portlet);
      portlet.setModifiedDate(Calendar.getInstance().getTime());
      service.update(portlet) ;
    }
  }

  static public class BackActionListener extends EventListener<UIInfoPortletForm>{
    public void execute(Event<UIInfoPortletForm> event) throws Exception{
      UIInfoPortletForm uiForm = event.getSource() ;
      UIPopupWindow uiParent = uiForm.getParent();
      uiParent.setShow(false);
    }
  }

}


