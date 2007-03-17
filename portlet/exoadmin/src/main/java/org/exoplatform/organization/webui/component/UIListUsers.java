/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.organization.webui.component;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.webui.application.RequestContext;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIFormInputSet;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIGrid;
import org.exoplatform.webui.component.UISearch;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.organization.webui.component.UIListUsers.*;

/**
 * Created by The eXo Platform SARL
 * Author : chungnv
 *          nguyenchung136@yahoo.com
 * Jun 23, 2006
 * 10:07:15 AM
 */
@ComponentConfig(
    events = {
      @EventConfig(listeners = ViewUserInfoActionListener.class),
      @EventConfig(listeners = DeleteUserActionListener.class)
    }
)
public class UIListUsers extends UISearch {
  
  private static String[] USER_BEAN_FIELD = {"userName", "lastName", "firstName", "email"} ;
  private static String[] USER_ACTION = {"ViewUserInfo", "DeleteUser"} ;  

  private static List<SelectItemOption<String>> OPTIONS_ = new ArrayList<SelectItemOption<String>>(4);
  static{
    OPTIONS_.add(new SelectItemOption<String>("Username", "userName"));
    OPTIONS_.add(new SelectItemOption<String>("First name", "firstName"));
    OPTIONS_.add(new SelectItemOption<String>("Last name", "lastName"));
    OPTIONS_.add(new SelectItemOption<String>("Email", "email"));
  }
  
  private Query lastQuery_ ;
  
	public UIListUsers() throws Exception {
		super(OPTIONS_) ;
    UIGrid uiGrid = addChild(UIGrid.class, null, "UIListUsers") ;
    uiGrid.configure("userName", USER_BEAN_FIELD, USER_ACTION) ;
		search(new Query()) ;
	}
	
	public void search(Query query) throws Exception {
    lastQuery_ = query ;
    UIGrid uiGrid = findFirstComponentOfType(UIGrid.class) ;
//    uiGrid.getBeans().s
    OrganizationService service = getApplicationComponent(OrganizationService.class) ;
    uiGrid.getUIPageIterator().setPageList(service.getUserHandler().findUsers(query)) ;
	}
  
  public void quickSearch(UIFormInputSet quickSearchInput) throws Exception {
    Query query = new Query();
    UIFormStringInput input = (UIFormStringInput) quickSearchInput.getChild(0);
    UIFormSelectBox select = (UIFormSelectBox) quickSearchInput.getChild(1);
    String name = input.getValue();
    if(name == null || name.equals("")) {
      search(new Query()) ;
      return ;
    }
    String selectBoxValue = select.getValue();
    if(selectBoxValue.equals("userName")) query.setUserName(name) ;
    if(selectBoxValue.equals("firstName")) query.setFirstName(name) ; 
    if(selectBoxValue.equals("lastName")) query.setLastName(name) ;
    if(selectBoxValue.equals("email")) query.setEmail(name) ;
    search(query);
  }

  @SuppressWarnings("unused")
  public void advancedSearch(UIFormInputSet advancedSearchInput) throws Exception {
  }
  
  public void processRender(RequestContext context) throws Exception {
    Writer w =  context.getWriter() ;
    w.write("<div class=\"UIListUsers\">");
    renderChildren();
    w.write("</div>");
  }
	
	static  public class ViewUserInfoActionListener extends EventListener<UIListUsers> {
    public void execute(Event<UIListUsers> event) throws Exception {
    	String username = event.getRequestContext().getRequestParameter(OBJECTID) ;
    	UIListUsers uiListUsers = event.getSource();
      OrganizationService service = uiListUsers.getApplicationComponent(OrganizationService.class);
      if(service.getUserHandler().findUserByName(username) == null ) {
        uiListUsers.search(new Query()) ;
        return ;
      }
      
    	uiListUsers.setRendered(false);
    	UIUserManagement uiUserManager = uiListUsers.getParent();
    	UIUserInfo uiUserInfo = uiUserManager.getChild(UIUserInfo.class);
      uiUserInfo.setUser(username);
    	uiUserInfo.setRendered(true);
      
    	UIComponent uiToUpdateAjax = uiListUsers.getAncestorOfType(UIUserManagement.class) ;
    	event.getRequestContext().addUIComponentToUpdateByAjax(uiToUpdateAjax) ;
    }
  }

	static  public class DeleteUserActionListener extends EventListener<UIListUsers> {
		public void execute(Event<UIListUsers> event) throws Exception {
			UIListUsers uiListUser = event.getSource() ;
			String userName = event.getRequestContext().getRequestParameter(OBJECTID) ;
			OrganizationService service = uiListUser.getApplicationComponent(OrganizationService.class) ;
			service.getUserHandler().removeUser(userName, true) ;
			uiListUser.search(uiListUser.lastQuery_);
      
      UIComponent uiToUpdateAjax = uiListUser.getAncestorOfType(UIUserManagement.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiToUpdateAjax) ;
    }
  }
  
}
