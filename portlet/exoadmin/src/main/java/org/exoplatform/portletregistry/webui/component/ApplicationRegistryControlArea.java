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
package org.exoplatform.portletregistry.webui.component;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : chungnv
 *          nguyenchung136@yahoo.com
 * Jun 23, 2006
 * 10:07:15 AM
 */
@ComponentConfig(    
    template = "app:/groovy/portletregistry/webui/component/ApplicationRegistryControlArea.gtmpl",
    events = {
        @EventConfig(listeners = ApplicationRegistryControlArea.AddCategoryActionListener.class),
        @EventConfig(listeners = ApplicationRegistryControlArea.EditCategoryActionListener.class),
        @EventConfig(listeners = ApplicationRegistryControlArea.ImportPortletActionListener.class),
        @EventConfig(listeners = ApplicationRegistryControlArea.ImportExoWidgetActionListener.class),
        @EventConfig(listeners = ApplicationRegistryControlArea.DeleteCategoryActionListener.class, confirm = "ApplicationRegistryControlArea.deleteCategory"),
        @EventConfig(listeners = ApplicationRegistryControlArea.DeleteAllCategoryActionListener.class, confirm = "ApplicationRegistryControlArea.deleteAllCategory"),
        @EventConfig(listeners = ApplicationRegistryControlArea.ShowCategoryActionListener.class)
    }
)
public class ApplicationRegistryControlArea extends UIContainer {

  private ApplicationCategory selectedCategory ;  
  private List<ApplicationCategory> portletCategories ;
  private Application selectedPortlet = null ;
  private List<Application> portlets ;

  public ApplicationRegistryControlArea() throws Exception {
    UIPopupWindow addCategoryPopup = addChild(UIPopupWindow.class, null, "AddCategory");
    addCategoryPopup.setWindowSize(640, 0);  
    UICategoryForm uiCategoryForm = createUIComponent(UICategoryForm.class, null, null);
    addCategoryPopup.setUIComponent(uiCategoryForm);
    uiCategoryForm.setValue(null);
  }  

  @SuppressWarnings("unchecked")
  public void initApplicationCategories() throws Exception {
    ApplicationRegistryService service = getApplicationComponent(ApplicationRegistryService.class);
    String accessUser = Util.getPortalRequestContext().getRemoteUser() ;
    portletCategories = service.getApplicationCategories(accessUser, new String[] {});
    if(portletCategories == null) portletCategories = new ArrayList<ApplicationCategory>(0);
    if(portletCategories.size() > 0) {
      setSelectedCategory(portletCategories.get(0));
      return;
    }
    setSelectedCategory((ApplicationCategory)null);
  }

  public ApplicationCategory getSelectedPortletCategory() { return selectedCategory; }

  @SuppressWarnings("unchecked")
  public void setSelectedCategory(Object category) throws Exception {
    selectedCategory = null;
    selectedPortlet = null;
    portlets = new ArrayList<Application>(0); 
    if(category instanceof ApplicationCategory) {
      selectedCategory = (ApplicationCategory)category;
    }else {
      for(ApplicationCategory portletCategory : portletCategories) {
        if(portletCategory.getName().equals(category)) {
          selectedCategory = portletCategory ;
          break ;
        }
      }
    }
    UIPortletRegistryPortlet parent = getParent();
    ApplicationRegistryWorkingArea workingArea = parent.getChild(ApplicationRegistryWorkingArea.class);
    if(selectedCategory == null){
      if(workingArea != null)  workingArea.setPortlets(portlets);
      return;
    }
    ApplicationRegistryService service = getApplicationComponent(ApplicationRegistryService.class) ;
    portlets = service.getApplications(selectedCategory) ;
    if(workingArea != null)  workingArea.setPortlets(portlets);
  }
  
  public ApplicationCategory getCategory(String name) {
    for(ApplicationCategory category: portletCategories){
      if(category.getName().equals(name))return category;
    }
    return null;
  }  
  
  public void processRender(WebuiRequestContext context) throws Exception {
    super.processRender(context);
    renderChildren();
  }

  public List<ApplicationCategory> getPortletCategory() { return portletCategories ;  }

  public Application getSelectedPortlet() { return selectedPortlet ; }
  public void setSelectedPortlet(String portletId) { 
    selectedPortlet = null;
    for(Application portlet : portlets) {
      if(portlet.getId().equals(portletId)) {
        selectedPortlet = portlet ;
        break ;
      }
    }
  }

  public List<Application> getPortlets() { return portlets ;  }  
  public void setPortlets(List <Application> portlets) { this.portlets = portlets ; }
  
  //TODO: Tung.Pham added
  public boolean isInUse(ApplicationCategory category) {
    UIPopupWindow uiPopup = getChild(UIPopupWindow.class) ;
    UIComponent uiComponent = uiPopup.getUIComponent() ;
    if(uiComponent != null && uiComponent instanceof UICategoryForm) {
      UICategoryForm uiForm = (UICategoryForm) uiComponent ;
      ApplicationCategory existingCategory = uiForm.getCategory() ;
      return (existingCategory != null && existingCategory.getName().equals(category.getName())) ;      
    }
    return false ;
  }
  
  static public class AddCategoryActionListener extends EventListener<ApplicationRegistryControlArea>{
    public void execute(Event<ApplicationRegistryControlArea> event) throws Exception{
      ApplicationRegistryControlArea uiRegistryCategory = event.getSource();
      UIPopupWindow popupWindow = uiRegistryCategory.getChild(UIPopupWindow.class);
      popupWindow.setId("AddCategory");
      UICategoryForm categoryForm = (UICategoryForm) popupWindow.getUIComponent();
      categoryForm.setValue((ApplicationCategory)null);
      popupWindow.setShow(true);
    }
  }

  static public class EditCategoryActionListener extends EventListener<ApplicationRegistryControlArea>{
    public void execute(Event<ApplicationRegistryControlArea> event) throws Exception{
      ApplicationRegistryControlArea uiRegistryCategory = event.getSource();
      UIPopupWindow uiPopupWindow = uiRegistryCategory.getChild(UIPopupWindow.class);
      uiPopupWindow.setId("EditCategory");
      UICategoryForm uiCategoryForm = (UICategoryForm) uiPopupWindow.getUIComponent();
      uiCategoryForm.setValue(uiRegistryCategory.getSelectedPortletCategory());
      uiPopupWindow.setShow(true)
      ;
    }
  }

  static public class ImportPortletActionListener extends EventListener<ApplicationRegistryControlArea> {
    public void execute(Event<ApplicationRegistryControlArea> event) throws Exception {
      ApplicationRegistryControlArea uiSource = event.getSource();
      ApplicationRegistryService service = uiSource.getApplicationComponent(ApplicationRegistryService.class) ;
      service.importJSR168Portlets() ;       
      uiSource.initApplicationCategories() ;
    }
  }
  
  static public class ImportExoWidgetActionListener extends EventListener<ApplicationRegistryControlArea> {
    public void execute(Event<ApplicationRegistryControlArea> event) throws Exception {
      ApplicationRegistryControlArea uiSource = event.getSource();
      ApplicationRegistryService service = uiSource.getApplicationComponent(ApplicationRegistryService.class) ;
      service.importExoWidgets();
      uiSource.initApplicationCategories();
    }
  }
  
  static public class DeleteAllCategoryActionListener extends EventListener<ApplicationRegistryControlArea> {
    public void execute(Event<ApplicationRegistryControlArea> event) throws Exception{
      ApplicationRegistryControlArea uiSource = event.getSource();
      ApplicationRegistryService service = uiSource.getApplicationComponent(ApplicationRegistryService.class);
      List<ApplicationCategory> list = uiSource.getPortletCategory();
      for(ApplicationCategory ele : list) {
        //TODO: Tung.Pham modified
        //--------------------------------------
        //service.remove(ele) ;
        if(uiSource.isInUse(ele)){
          UIApplication uiApp = event.getRequestContext().getUIApplication() ;
          uiApp.addMessage(new ApplicationMessage("ApplicationRegistryControlArea.msg.CategoryExist", new String[]{ele.getName()})) ;
          continue ;
        }
        service.remove(ele) ;
        //--------------------------------------
      }
      uiSource.initApplicationCategories();
      UIPortletRegistryPortlet parent = uiSource.getParent();
      ApplicationRegistryWorkingArea workingArea = parent.getChild(ApplicationRegistryWorkingArea.class);
      workingArea.setPortlets(new ArrayList<Application>());
      event.getRequestContext().addUIComponentToUpdateByAjax(parent);
    }
  }

  static public class DeleteCategoryActionListener extends EventListener<ApplicationRegistryControlArea> {
    public void execute(Event<ApplicationRegistryControlArea> event) throws Exception {
      ApplicationRegistryControlArea uiComp = event.getSource();
      ApplicationRegistryService service = uiComp.getApplicationComponent(ApplicationRegistryService.class);            
      ApplicationCategory selectedCategory = uiComp.getSelectedPortletCategory();
      if(selectedCategory == null) return;
      //TODO: Tung.Pham added
      //------------------------------------------------
      if(uiComp.isInUse(selectedCategory)) {
        UIApplication uiApp = event.getRequestContext().getUIApplication() ;
        uiApp.addMessage(new ApplicationMessage("ApplicationRegistryControlArea.msg.CategoryExist", new String[]{selectedCategory.getName()})) ;
        return ;
      }
      //------------------------------------------------
      service.remove(selectedCategory) ; 
      uiComp.initApplicationCategories();
    }
  }

  static public class ShowCategoryActionListener extends EventListener<ApplicationRegistryControlArea>{
    public void execute(Event<ApplicationRegistryControlArea> event) throws Exception{
      ApplicationRegistryControlArea uiComp = event.getSource();
      UIPortletRegistryPortlet parent = uiComp.getParent();
      ApplicationRegistryWorkingArea workingArea = parent.getChild(ApplicationRegistryWorkingArea.class);
      String categoryName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      ApplicationCategory selectedCategory = uiComp.getSelectedPortletCategory();
      if(selectedCategory.getName().equals(categoryName))  return;
      uiComp.setSelectedCategory(categoryName);      
      ApplicationRegistryService service = uiComp.getApplicationComponent(ApplicationRegistryService.class) ;
      List<Application> portlets = service.getApplications(uiComp.getSelectedPortletCategory());
      workingArea.setPortlets(portlets);
      event.getRequestContext().addUIComponentToUpdateByAjax(parent);
    }
  }

}
