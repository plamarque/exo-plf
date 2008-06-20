/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.applicationregistry.webui.component;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.application.newregistry.Application;
import org.exoplatform.application.newregistry.ApplicationCategory;
import org.exoplatform.application.newregistry.ApplicationRegistryService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hoa Nguyen
 *          hoa.nguyen@exoplatform.com
 * Jun 23, 2006
 * 10:07:15 AM
 */
@ComponentConfig(    
    template = "app:/groovy/applicationregistry/webui/component/ApplicationRegistryWorkingArea.gtmpl",
    events = {
        @EventConfig(listeners = ApplicationRegistryWorkingArea.AddPortletActionListener.class),
        @EventConfig(listeners = ApplicationRegistryWorkingArea.DeletePortletActionListener.class, confirm = "ApplicationRegistryWorkingAreaNew.deleteApplication"),
        @EventConfig(listeners = ApplicationRegistryWorkingArea.EditPermissionActionListener.class),
        @EventConfig(listeners = ApplicationRegistryWorkingArea.EditPortletActionListener.class)
    }
)
public class ApplicationRegistryWorkingArea extends UIContainer {
   
  private List<Application> portlets_  = new ArrayList<Application>();
  private Application select_;
  
  public ApplicationRegistryWorkingArea() throws Exception {
//    UIPopupWindow addCategoryPopup = addChild(UIPopupWindow.class, null, "WorkingPopup");
//    addCategoryPopup.setWindowSize(660, 0); 
  }  
    
  public List<Application> getPortlets() {return portlets_;}
  public void setPortlets(List<Application> p) { portlets_ = p; }
  
  public Application getPortlet() {return select_;}
  public void setPortlet(Application s) { select_ = s; }
  
  public void processRender(WebuiRequestContext context) throws Exception {
    super.processRender(context);
    renderChildren(context) ;
  }

  public Application getSelectApplication() { return select_; }
  
  public boolean showAddButton(){
    UIApplicationRegistryPortlet registryPortlet = getParent();
    ApplicationRegistryControlArea controlArea = registryPortlet.getChild(ApplicationRegistryControlArea.class);
    List<ApplicationCategory> categories = controlArea.getPortletCategory();
    if (categories == null || categories.size() < 1) return false;
    return true;
  }
  
  public void setSeletcApplication(Application select){ select_ = select; }
  
  public void setSeletcApplication(String appName){
    for(Application app: portlets_){
      if(app.getApplicationName().equals(appName)){
        select_ = app;
      }
    }
  }
  static public class AddPortletActionListener extends EventListener<ApplicationRegistryWorkingArea> {
    public void execute(Event<ApplicationRegistryWorkingArea> event) throws Exception {
      ApplicationRegistryWorkingArea uiWorkingArea = event.getSource();
      UIApplicationRegistryPortlet uiParent = uiWorkingArea.getParent();
      ApplicationRegistryControlArea uiControlArea = uiParent.getChild(ApplicationRegistryControlArea.class);
      String msg = "ApplicationRegistryWorkingArea.msg.selectCategory";
      if(uiControlArea.getPortletCategory() == null || uiControlArea.getPortletCategory().size() < 1) {
        msg = "ApplicationRegistryWorkingArea.msg.nullCategory";
      }
      
      if(uiControlArea.getSelectedPortletCategory() == null ) {
        UIApplication uiApp = Util.getPortalRequestContext().getUIApplication() ;
        uiApp.addMessage(new ApplicationMessage(msg, null)) ;
        Util.getPortalRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages() );
        return;
      }
      UIPopupContainer popupContainer = uiParent.getChild(UIPopupContainer.class);
      UIAvailablePortletForm uiAvailablePortletForm = popupContainer.activate(UIAvailablePortletForm.class, 660) ;
      uiAvailablePortletForm.setValue();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }

  static public class EditPortletActionListener extends EventListener<ApplicationRegistryWorkingArea> {
    public void execute(Event<ApplicationRegistryWorkingArea> event) throws Exception {
      String appName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      ApplicationRegistryWorkingArea uiWorkingArea = event.getSource();
      uiWorkingArea.setSeletcApplication(appName);
      UIPopupContainer popupWindow = uiWorkingArea.getAncestorOfType(UIApplicationRegistryPortlet.class).getChild(UIPopupContainer.class) ;
      UIInfoPortletForm uiAvailablePortletForm= popupWindow.activate(UIInfoPortletForm.class, 660) ;
      uiAvailablePortletForm.setName("UIInfoPortletForm");
      uiAvailablePortletForm.setValues(uiWorkingArea.getSelectApplication());
      event.getRequestContext().addUIComponentToUpdateByAjax(popupWindow) ;
    }
  }

  static public class EditPermissionActionListener extends EventListener<ApplicationRegistryWorkingArea> {
    public void execute(Event<ApplicationRegistryWorkingArea> event) throws Exception {
      String appName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      ApplicationRegistryWorkingArea workingArea = event.getSource();
      workingArea.setSeletcApplication(appName);
      UIApplicationRegistryPortlet uiParent = workingArea.getAncestorOfType(UIApplicationRegistryPortlet.class) ;
      UIPopupContainer popupContainer = uiParent.getChild(UIPopupContainer.class) ;
      UIPermissionForm accessGroupForm = popupContainer.activate(UIPermissionForm.class, 660) ;
      //UIPermissionForm accessGroupForm= workingArea.createUIComponent(UIPermissionForm.class, null, null);
      accessGroupForm.setValue(workingArea.getSelectApplication()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }


  static public class DeletePortletActionListener extends EventListener<ApplicationRegistryWorkingArea> {
    public void execute(Event<ApplicationRegistryWorkingArea> event) throws Exception {
      String categoryName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      ApplicationRegistryWorkingArea workingArea = event.getSource();
      workingArea.setSeletcApplication(categoryName);
      Application selectedPortlet = workingArea.getSelectApplication() ;
      if(selectedPortlet == null) return ;

      ApplicationRegistryService service = workingArea.getApplicationComponent(ApplicationRegistryService.class) ;
      UIPopupContainer uiPopup = workingArea.getAncestorOfType(UIApplicationRegistryPortlet.class).getChild(UIPopupContainer.class) ;
      UIComponent uiComponent = uiPopup.getChild(UIPopupWindow.class).getUIComponent();
      if(uiComponent != null && uiComponent instanceof UIInfoPortletForm) {
        UIInfoPortletForm uiInfoForm = (UIInfoPortletForm)uiComponent ;
        Application existingApp = uiInfoForm.getPortlet() ;
        if(existingApp != null && existingApp.getId().equals(selectedPortlet.getId())) {
          UIApplication uiApp = event.getRequestContext().getUIApplication() ;
          uiApp.addMessage(new ApplicationMessage("ApplicationRegistryWorkingArea.msg.AppExist", new String[]{existingApp.getApplicationName()})) ;
          return ;
        }
      }
        
      service.remove(selectedPortlet) ;  
      workingArea.getPortlets().remove(selectedPortlet) ;
      workingArea.setSeletcApplication((Application)null);
    }
  }

}

