/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.portal.webui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkspace;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@exoplatform.com
 * Jul 11, 2006  
 */
@ComponentConfig(  
  lifecycle = UIFormLifecycle.class,
  template = "system:/groovy/portal/webui/UILoginForm.gtmpl" ,
  events = {
    @EventConfig(listeners = UILoginForm.SigninActionListener.class),
    @EventConfig(phase = Phase.DECODE, listeners = UILoginForm.SignupActionListener.class),
    @EventConfig(phase = Phase.DECODE, listeners = UIMaskWorkspace.CloseActionListener.class)
  }
)
public class UILoginForm extends UIForm {
  
  public UILoginForm() throws Exception{    
    addUIFormInput(new UIFormStringInput("username", "username", null)).
    addUIFormInput(new UIFormStringInput("password", "password", null).
                   setType(UIFormStringInput.PASSWORD_TYPE)).
    addUIFormInput(new UIFormCheckBoxInput<Boolean>("remember", "remember", null));
//    PortalRequestContext prContext = Util.getPortalRequestContext();
//    HttpServletRequest request = prContext.getRequest();
//    Cookie userName = loadCookie(request, "authentication.username", null, false);
//    Cookie pass = loadCookie(request, "authentication.password", null, false);
//    if( userName == null || pass == null) return;
//    getUIStringInput("username").setValue(userName.getValue());
//    getUIStringInput("password").setValue(pass.getValue());
    /*
         addUIFormInput(new UIFormStringInput("username", "username", null).
                   addValidator(NameValidator.class)).
    addUIFormInput(new UIFormStringInput("password", "password", null).
                   setType(UIFormStringInput.PASSWORD_TYPE).
                   addValidator(EmptyFieldValidator.class)).
    addUIFormInput(new UIFormCheckBoxInput<Boolean>("remember", "remember", null));
     */
  }
  
//  protected Cookie loadCookie(HttpServletRequest request, String name, String value, boolean autoCreate){
//    Cookie[] cookies = request.getCookies();
//    Cookie cookie = null;
//    if (cookies != null) {
//      for (Cookie ele : cookies) {
//        if(ele.getName().equals(name)) cookie = ele;
//      }
//    }
//    if(cookie == null && autoCreate) cookie = new Cookie(name, value);
//    if(cookie == null) return null;
//    cookie.setDomain(request.getRemoteHost());
//    cookie.setSecure(true);
//    return cookie;
//  }


  static public class SigninActionListener  extends EventListener<UILoginForm> {
    
    public void execute(Event<UILoginForm> event) throws Exception {
      UILoginForm uiForm = event.getSource();
      String username = uiForm.getUIStringInput("username").getValue();
      String password = uiForm.getUIStringInput("password").getValue();
      
//      boolean remember = uiForm.<UIFormCheckBoxInput >getUIInput("remember").isChecked();
      
      OrganizationService orgService = uiForm.getApplicationComponent(OrganizationService.class);
      boolean authentication = orgService.getUserHandler().authenticate(username, password);
      if(!authentication){
        throw new MessageException(new ApplicationMessage("UILoginForm.msg.Invalid-account", null));
      }
        
      PortalRequestContext prContext = Util.getPortalRequestContext();
      HttpServletRequest request = prContext.getRequest();
      request.getSession().invalidate();
      HttpSession session = request.getSession();
      session.setAttribute("authentication.username", username);
      session.setAttribute("authentication.password", password);
      
      /*if(remember && authentication){
        HttpServletResponse response = prContext.getResponse();
        response.addCookie(loadCookie(request, "authentication.username", username));
        response.addCookie(loadCookie(request, "authentication.password", password));
      }*/
      
      prContext.setResponseComplete(true);  
      //String redirect = request.getContextPath() + "/private/site:/";
      UIPortal uiCurrentPortal = Util.getUIPortal() ;
      String portalName = uiCurrentPortal.getName() ;
      String redirect = request.getContextPath() + "/private/" + portalName + "/";
      prContext.getResponse().sendRedirect(redirect);      
    }   
    
   /* protected Cookie loadCookie(HttpServletRequest request, String name, String value){
      Cookie[] cookies = request.getCookies();
      Cookie cookie = null;
      if (cookies != null) {
        for (Cookie ele : cookies) {
          if(ele.getName().equals(name)) cookie = ele;
        }
      }
      if(cookie == null) cookie = new Cookie(name, value);
      cookie.setDomain(request.getRemoteHost());
      cookie.setSecure(true);
      return cookie;
    }*/
  }
  
  static public class SignupActionListener  extends EventListener<UILoginForm> {
    public void execute(Event<UILoginForm> event) throws Exception {
      UIPortal uiPortal  = Util.getUIPortal() ;
      UIPageBody uiPageBody = uiPortal.findFirstComponentOfType(UIPageBody.class);          
      uiPageBody.setPage("portal::site::register", uiPortal);
      
      UIPortalApplication uiPortalApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
      UIWorkspace uiWorkingWS = uiPortalApp.findComponentById(UIPortalApplication.UI_WORKING_WS_ID);
      PortalRequestContext pcontext = Util.getPortalRequestContext();     
      pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);      
      uiPortal.setRenderSibbling(UIPortal.class);
      pcontext.setFullRender(true);
      
      UIMaskWorkspace  uiMaskWorkspace = event.getSource().getAncestorOfType(UIMaskWorkspace.class);
      if(uiMaskWorkspace == null || !uiMaskWorkspace.isShow()) return;
      uiMaskWorkspace.setUIComponent(null);
      uiMaskWorkspace.setShow(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWorkspace) ;
    }
  }
}
