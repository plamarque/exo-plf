package org.exoplatform.webui.organization.account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormPopupWindow;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@gmail.com
 * Dec 11, 2007  
 * Modified: dang.tung
 *           tungcnw@gmail.com 
 * Nov 22, 2008           
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/organization/webui/component/UISelectUserForm.gtmpl",
    events = {
      @EventConfig(listeners = UISelectUserForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISelectUserForm.AddUserActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISelectUserForm.SearchActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISelectUserForm.SearchGroupActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISelectUserForm.SelectGroupActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISelectUserForm.ShowPageActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISelectUserForm.CloseActionListener.class, phase = Phase.DECODE)
    }
)

public class UISelectUserForm extends UIForm implements UIPopupComponent { 
  final public static String FIELD_KEYWORD = "keyWord".intern() ;
  final public static String FIELD_FILTER = "filter".intern() ;
  final public static String FIELD_GROUP = "group".intern() ;
  public static String USER_NAME = "userName";
  public static String LAST_NAME = "lastName";
  public static String FIRST_NAME = "firstName";
  public static String EMAIL = "email";

  protected Map<String, User> userData_ = new HashMap<String, User>() ;
  private boolean isShowSearch_ = false ;
  protected String groupId_ = null ;
  protected Collection<String> pars_ ;
  public UIPageIterator uiIterator_ ;

  private String selectedUsers;
  private boolean multi = true;
  
  public UISelectUserForm() throws Exception {  
    addUIFormInput(new UIFormStringInput(FIELD_KEYWORD, FIELD_KEYWORD, null)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_FILTER, FIELD_FILTER, getFilters())) ;
    addUIFormInput(new UIFormStringInput(FIELD_GROUP, FIELD_GROUP, null)) ;
    isShowSearch_ = true ;
    OrganizationService service = getApplicationComponent(OrganizationService.class) ;
    ObjectPageList objPageList = new ObjectPageList(service.getUserHandler().findUsers(new Query()).currentPage(), 10) ;
    uiIterator_ = new UIPageIterator() ;
    uiIterator_.setPageList(objPageList) ;
    uiIterator_.setId("UISelectUserPage") ;
    
    // create group selector
    UIFormPopupWindow uiPopup = addChild(UIFormPopupWindow.class, null, "PopupGroupSelector");
    uiPopup.setWindowSize(540, 0);
    UISelectGroupForm uiGroup = createUIComponent(UISelectGroupForm.class, null, null);
    uiPopup.setUIComponent(uiGroup);
    uiGroup.setId("GroupSelector");
    uiGroup.getChild(UITree.class).setId("TreeGroupSelector");
    uiGroup.getChild(UIBreadcumbs.class).setId("BreadcumbsGroupSelector");
  }
  
  @SuppressWarnings("unchecked")
  public List<User> getData() throws Exception {
    if(getMulti()) {
      for(Object obj : uiIterator_.getCurrentPageData()){
        User user = (User)obj ;
        if(getUIFormCheckBoxInput(user.getUserName()) == null)
          addUIFormInput(new UIFormCheckBoxInput<Boolean>(user.getUserName(),user.getUserName(), false)) ;
      }
    }
    return  new ArrayList<User>(uiIterator_.getCurrentPageData());
  }
  
  public String getSelectedUsers() { return selectedUsers ; }
  
  public void setSelectedUsers(String selectedUsers) { this.selectedUsers = selectedUsers ;} 
  
  public void setMulti(boolean multi) { this.multi = multi; }
  
  public boolean getMulti() { return multi; }
  
  public UIPageIterator  getUIPageIterator() {  return uiIterator_ ; }

  public long getAvailablePage(){ return uiIterator_.getAvailablePage() ;}
  
  public long getCurrentPage() { return uiIterator_.getCurrentPage();}
  
  // update data, review later
//  public void init(Collection<String> pars) throws Exception{
//    OrganizationService service = getApplicationComponent(OrganizationService.class) ;
//    ObjectPageList objPageList = new ObjectPageList(service.getUserHandler().getUserPageList(0).getAll(), 10) ;
//    uiIterator_.setPageList(objPageList) ;
//    pars_ = pars ;
//  }
  
  private List<SelectItemOption<String>> getFilters() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(USER_NAME, USER_NAME)) ;
    options.add(new SelectItemOption<String>(LAST_NAME, LAST_NAME)) ;
    options.add(new SelectItemOption<String>(FIRST_NAME, FIRST_NAME)) ;
    options.add(new SelectItemOption<String>(EMAIL, EMAIL)) ;
    return options;
  }

  public String[] getActions() { return new String[]{"Add", "Close"}; }
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {} 
  
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
  }
  public void setShowSearch(boolean isShowSearch) {
    this.isShowSearch_ = isShowSearch;
  }
  public boolean isShowSearch() {
    return isShowSearch_;
  }
  public String getSelectedGroup() {
    return getUIStringInput(FIELD_GROUP).getValue();
  }
  public void setSelectedGroup(String selectedGroup) {
    getUIStringInput(FIELD_GROUP).setValue(selectedGroup);
  }
  static  public class AddActionListener extends EventListener<UISelectUserForm> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiForm = event.getSource();
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      StringBuilder sb = new StringBuilder() ;
      int count = 0;
      for(Object o : uiForm.uiIterator_.getCurrentPageData()) {
        User u = (User)o ;
        UIFormCheckBoxInput input = uiForm.getUIFormCheckBoxInput(u.getUserName()) ;
        if(input != null && input.isChecked()) {
          count++ ;
          if(sb.toString() != null && sb.toString().trim().length() != 0) sb.append(",") ;
          sb.append(u.getUserName()) ;
        }
      }
      if(count == 0) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UISelectUserForm.msg.user-required",null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiForm.setSelectedUsers(sb.toString());
      System.out.println("\n\n\n user added: " + uiForm.getSelectedUsers());
      if(uiContainer !=null) uiContainer.cancelPopupAction();
    }  
  }
  
  
  static  public class AddUserActionListener extends EventListener<UISelectUserForm> {
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiForm = event.getSource();
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);
      uiForm.setSelectedUsers(userName);
      System.out.println("\n\n\n user added: " + uiForm.getSelectedUsers());
      if(uiContainer !=null) uiContainer.cancelPopupAction();
    }  
  }

  protected void updateCurrentPage(int page) throws Exception{
    uiIterator_.setCurrentPage(page) ;
  }
  public void setKeyword(String value) {
    getUIStringInput(FIELD_KEYWORD).setValue(value) ;
  }
  
  static  public class SelectGroupActionListener extends EventListener<UISelectGroupForm> {   
    public void execute(Event<UISelectGroupForm> event) throws Exception {
      UISelectGroupForm uiSelectGroupForm = event.getSource();
      UISelectUserForm uiSelectUserForm = uiSelectGroupForm.<UIComponent>getParent().getParent(); 
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiSelectUserForm.setSelectedGroup(groupId);
      OrganizationService service = uiSelectGroupForm.getApplicationComponent(OrganizationService.class);
      uiSelectUserForm.uiIterator_.setPageList(service.getUserHandler().findUsersByGroup(groupId));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSelectUserForm) ;
    }
  }
  
  @SuppressWarnings("unchecked")
  static  public class SearchActionListener extends EventListener<UISelectUserForm> {
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiForm = event.getSource() ;
      OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class) ;
      String keyword = uiForm.getUIStringInput(FIELD_KEYWORD).getValue();
      String filter = uiForm.getUIFormSelectBox(FIELD_FILTER).getValue();
      if(filter == null || filter.trim().length() == 0) return;
      Query q = new Query() ;
      keyword = "*" + keyword + "*" ;
      if(USER_NAME.equals(filter)) {
        q.setUserName(keyword) ;
      } 
      if(LAST_NAME.equals(filter)) {
        q.setLastName(keyword) ;
      }
      if(FIRST_NAME.equals(filter)) {
        q.setFirstName(keyword) ;
      }
      if(EMAIL.equals(filter)) {
        q.setEmail(keyword) ;
      }
      List results = new ArrayList() ;
      results.addAll(service.getUserHandler().findUsers(q).getAll()) ;
      ObjectPageList objPageList = new ObjectPageList(results, 10) ;
      uiForm.uiIterator_.setPageList(objPageList);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }

  static  public class CloseActionListener extends EventListener<UISelectUserForm> {
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiForm = event.getSource();  
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      if(uiContainer !=null) uiContainer.cancelPopupAction();
    }
  }
  
  static  public class SearchGroupActionListener extends EventListener<UISelectUserForm> {
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiForm = event.getSource();
      uiForm.getChild(UIPopupWindow.class).setShow(true);
    }
  }
  static  public class ShowPageActionListener extends EventListener<UISelectUserForm> {
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiSelectUserForm = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiSelectUserForm.updateCurrentPage(page) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSelectUserForm);           
    }
  }
}
