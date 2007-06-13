/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.portal.webui.application;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkspace;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputIconSelector;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;
import org.exoplatform.webui.form.validator.NumberFormatValidator;
/**
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@yahoo.com
 * Jun 8, 2006
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UIPortletForm.SaveActionListener.class),
      @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class, phase = Phase.DECODE)
    }
)   
/*initParams = {
      @ParamConfig(
          name = "PortletDecorator", 
          value = "app:/WEB-INF/conf/uiconf/portal/webui/component/customization/PortletDecorator.groovy"
      ),          
      @ParamConfig(
          name = "PortletTemplate",
          value = "app:/WEB-INF/conf/uiconf/portal/webui/component/customization/PortletTemplate.groovy"
      ),
      @ParamConfig(
          name = "help.UIPortletFormQuickHelp",
          value = "app:/WEB-INF/conf/uiconf/portal/webui/component/customization/UIPortletFormQuickHelp.xhtml"
      )
    },*/
public class UIPortletForm extends UIFormTabPane {	
  
	private UIPortlet uiPortlet_ ;
  private UIComponent backComponent_ ;
  
  @SuppressWarnings("unchecked")
  public UIPortletForm() throws Exception {//InitParams initParams
  	super("UIPortletForm");
    UIFormInputSet uiSettingSet = new UIFormInputSet("PortletSetting") ;
  	uiSettingSet.
      addUIFormInput(new UIFormStringInput("id", "id", null).
                     addValidator(EmptyFieldValidator.class).setEditable(false)).
      addUIFormInput(new UIFormStringInput("factoryId", "factoryId", null)).
    	addUIFormInput(new UIFormStringInput("title", "title", null)).
  		addUIFormInput(new UIFormStringInput("width", "width", null).
                     addValidator(NumberFormatValidator.class)).
  		addUIFormInput(new UIFormStringInput("height", "height", null).
                     addValidator(NumberFormatValidator.class)).
  		addUIFormInput(new UIFormCheckBoxInput("showInfoBar", "showInfoBar", false)).
  		addUIFormInput(new UIFormCheckBoxInput("showPortletMode", "showPortletMode", false)).
    	addUIFormInput(new UIFormCheckBoxInput("showWindowState", "showWindowState", false)).
      addUIFormInput(new UIFormTextAreaInput("description", "description", null));
    addUIFormInput(uiSettingSet);    
    
//    UIFormInputDecoratorSelector uiDecorator = new UIFormInputDecoratorSelector("Decorator", "decorator");
//    uiDecorator.setRendered(false) ;
//  	addUIFormInput(uiDecorator);
    
    UIFormInputIconSelector uiIconSelector = new UIFormInputIconSelector("Icon", "icon") ;
    uiIconSelector.setRendered(false)  ;
    addUIFormInput(uiIconSelector) ;
    
   /* UIFormInputItemSelector uiTemplate = new UIFormInputItemSelector("Template", "template");
    uiTemplate.setTypeValue(String.class);
    uiTemplate.setRendered(false);
    addUIFormInput(uiTemplate);*/

//    if(initParams == null) return ;
//    UIFormInputDecoratorSelector uiDecoratorInput = getChild(UIFormInputDecoratorSelector.class);
//    RequestContext context = RequestContext.getCurrentInstance() ;
//    List<SelectItemOption> options = initParams.getParam("PortletDecorator").getMapGroovyObject(context) ;
//    uiDecoratorInput.setOptions(options);
  }
  
  public UIComponent getBackComponent() { return backComponent_ ; }
  public void setBackComponent(UIComponent uiComp) throws Exception {
    backComponent_ = uiComp;
  }
  
  public UIPortlet getUIPortlet() { return uiPortlet_; }
  
  @SuppressWarnings("unchecked")
  public void setValues(UIPortlet uiPortlet) throws Exception {
  	uiPortlet_ = uiPortlet;
    invokeGetBindingBean(uiPortlet_) ;
    String icon = uiPortlet.getIcon();
    if(icon != null && icon.length() > 0){
      getChild(UIFormInputIconSelector.class).setSelectedIcon(icon);
    }
    UIFormInputIconSelector uiIconSelector = getChild(UIFormInputIconSelector.class);
    uiIconSelector.setSelectedIcon(uiPortlet.getIcon());
  }
  
	static public class SaveActionListener extends EventListener<UIPortletForm> {
    public void execute(Event<UIPortletForm> event) throws Exception {      
      UIPortletForm uiPortletForm = event.getSource() ;
      UIPortlet uiPortlet = uiPortletForm.getUIPortlet() ;
      UIFormInputIconSelector uiIconSelector = uiPortletForm.getChild(UIFormInputIconSelector.class);
      uiPortlet.setIcon(uiIconSelector.getSelectedIcon());
      uiPortletForm.invokeSetBindingBean(uiPortlet) ;
      
      UIMaskWorkspace uiMaskWorkspace = uiPortletForm.getParent();
      uiMaskWorkspace.setUIComponent(null);
      
      PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();
      pcontext.addUIComponentToUpdateByAjax(uiMaskWorkspace);
      
      UIPortalApplication uiPortalApp = uiPortlet.getAncestorOfType(UIPortalApplication.class);
      UIWorkspace uiWorkingWS = uiPortalApp.findComponentById(UIPortalApplication.UI_WORKING_WS_ID);
      pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);
      pcontext.setFullRender(true);
      Util.showComponentLayoutMode(UIPortlet.class);  
    }
  }
  
}
