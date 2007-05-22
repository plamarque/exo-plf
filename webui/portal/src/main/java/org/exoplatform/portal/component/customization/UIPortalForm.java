/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.portal.component.customization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.organization.webui.component.UIListPermissionSelector;
import org.exoplatform.organization.webui.component.UIPermissionSelector;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.component.UIPortalApplication;
import org.exoplatform.portal.component.UIWorkspace;
import org.exoplatform.portal.component.control.UIMaskWorkspace;
import org.exoplatform.portal.component.model.PortalTemplateConfigOption;
import org.exoplatform.portal.component.view.PortalDataModelUtil;
import org.exoplatform.portal.component.view.UIPortal;
import org.exoplatform.portal.component.view.Util;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.component.UIFormInputItemSelector;
import org.exoplatform.webui.component.UIFormInputSet;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTabPane;
import org.exoplatform.webui.component.UIGrid;
import org.exoplatform.webui.component.UIPageIterator;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemCategory;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.component.validator.EmptyFieldValidator;
import org.exoplatform.webui.component.validator.NameValidator;
import org.exoplatform.webui.config.Component;
import org.exoplatform.webui.config.InitParams;
import org.exoplatform.webui.config.Param;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.config.annotation.ParamConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
@ComponentConfigs({
  @ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/component/UIFormTabPane.gtmpl",     
    events = {
      @EventConfig(listeners = UIPortalForm.SaveActionListener.class),
      @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class, phase = Phase.DECODE)
    }
  ),
  @ComponentConfig(
    id = "CreatePortal",
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/component/UIFormTabPane.gtmpl",
    initParams = @ParamConfig(
        name = "PortalTemplateConfigOption", 
        value = "app:/WEB-INF/conf/uiconf/portal/webui/component/customization/PortalTemplateConfigOption.groovy"
    ),
    events = {
      @EventConfig(name  = "Save", listeners = UIPortalForm.CreateActionListener.class),
      @EventConfig(listeners = UIPortalForm.SelectItemOptionActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class, phase = Phase.DECODE)
    }
  ),
  @ComponentConfig(
      type = UIFormInputSet.class,
      id = "PermissionSetting",
      template = "system:/groovy/webui/component/UITabSelector.gtmpl"
  )
})
public class UIPortalForm extends UIFormTabPane {

  private static final String SKIN = "skin";
  private PortalConfig portalConfig_;
  private List<SelectItemOption<String>> languages = new ArrayList<SelectItemOption<String>>() ;
  
  private static String DEFAULT_FACTORY_ID = "default";
  
  @SuppressWarnings("unchecked")
  public UIPortalForm(InitParams initParams) throws Exception {
    super("UIPortalForm");
    
    UIFormInputItemSelector uiTemplateInput = new  UIFormInputItemSelector("PortalTemplate", null);
    uiTemplateInput.setRendered(true) ;
    addUIFormInput(uiTemplateInput) ;
    
    createDefaultItem();
    
    UIFormInputSet uiPortalSetting = this.<UIFormInputSet>getChildById("PortalSetting");
    UIFormStringInput uiNameInput = uiPortalSetting.getUIStringInput("name");
    uiNameInput.setEditable(true);
    
    setActions(new String[]{"Save", "Close"});
    
    if(initParams == null) return;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Param param = initParams.getParam("PortalTemplateConfigOption");
    List<SelectItemCategory> portalTemplates = (List<SelectItemCategory>)param.getMapGroovyObject(context);
    for(SelectItemCategory itemCategory: portalTemplates){
      uiTemplateInput.getItemCategories().add(itemCategory);
    }
    if(uiTemplateInput.getSelectedItemOption() == null) {
      uiTemplateInput.getItemCategories().get(0).setSelected(true);
    }
  }

  
  public UIPortalForm() throws Exception {
    super("UIPortalForm");
    createDefaultItem();
    
    WebuiRequestContext currReqContext = RequestContext.getCurrentInstance() ;
    WebuiApplication app = (WebuiApplication)currReqContext.getApplication() ;
    List<Component> configs = app.getConfigurationManager().getComponentConfig(UIPortalApplication.class);
    List<SelectItemCategory>  itemCategories = new ArrayList<SelectItemCategory>();
    for(Component ele : configs) {
      String id =  ele.getId();
      if(id == null) id = DEFAULT_FACTORY_ID;
      StringBuilder builder = new StringBuilder(id);
      builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));
      String upId = builder.toString();
      
      SelectItemCategory category = new SelectItemCategory(upId);
      itemCategories.add(category);
      List<SelectItemOption<String>> items = new ArrayList<SelectItemOption<String>>();
      category.setSelectItemOptions(items);
      SelectItemOption<String> item = new SelectItemOption<String>(id, id, "Portal"+upId);
      items.add(item);
    } 
    
    UIFormInputSet uiPermissionSetting = createUIComponent(UIFormInputSet.class, "PermissionSetting", null);
    uiPermissionSetting.setRendered(false);
    addUIComponentInput(uiPermissionSetting);
    
    UIListPermissionSelector uiListPermissionSelector = createUIComponent(UIListPermissionSelector.class, null, null);
    uiListPermissionSelector.configure("UIListPermissionSelector", "accessPermissions");
    uiPermissionSetting.addChild(uiListPermissionSelector);
    
    UIPermissionSelector uiEditPermission = createUIComponent(UIPermissionSelector.class, null, null);
    uiEditPermission.setRendered(false) ;
    uiEditPermission.configure("UIPermissionSelector", "editPermission");
    uiPermissionSetting.addChild(uiEditPermission);
        
    UIFormInputItemSelector uiFactoryId = new UIFormInputItemSelector("FactoryId", "factoryId");
    uiFactoryId.setItemCategories(itemCategories);
    uiFactoryId.setRendered(false);
    addUIFormInput(uiFactoryId);
    
    this.<UIFormInputSet>getChildById("PortalSetting").setRendered(true);
  }
  
  private void createDefaultItem() throws Exception {
    LocaleConfigService localeConfigService  = getApplicationComponent(LocaleConfigService.class) ;
    Collection listLocaleConfig = localeConfigService.getLocalConfigs() ;

    Iterator iterator = listLocaleConfig.iterator() ;
    while(iterator.hasNext()) {
      LocaleConfig localeConfig = (LocaleConfig) iterator.next() ;
      languages.add(new SelectItemOption<String>(localeConfig.getLanguage(), localeConfig.getLanguage())) ;
    }

    UIFormInputSet uiSettingSet = new UIFormInputSet("PortalSetting") ;
    uiSettingSet.addUIFormInput(new UIFormStringInput("name", "name", null).
                                addValidator(EmptyFieldValidator.class).
                                addValidator(NameValidator.class).
                                setEditable(false)).
                 addUIFormInput(new UIFormSelectBox("locale", "locale", languages).
                                addValidator(EmptyFieldValidator.class));
    
    List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
    ls.add(new SelectItemOption<String>("Default", "Default")) ;
    ls.add(new SelectItemOption<String>("Mac", "Mac")) ;
    ls.add(new SelectItemOption<String>("Vista", "Vista")) ;
    UIFormSelectBox uiSelectBox = new UIFormSelectBox(SKIN, SKIN, ls) ;
    UIPortal uiPortal = Util.getUIPortal();
    uiPortal.getLocale();
    uiSelectBox.setValue(uiPortal.getSkin());
    uiSelectBox.setEditable(false);
    uiSettingSet.addUIFormInput(uiSelectBox);
    addUIFormInput(uiSettingSet);
    uiSettingSet.setRendered(false);
  }
  
  public PortalConfig getPortalConfig() { return portalConfig_; }
  
  public void setValues(PortalConfig uiPortal) throws Exception {
    portalConfig_ = uiPortal;
    if(portalConfig_.getFactoryId() == null) portalConfig_.setFactoryId(DEFAULT_FACTORY_ID);    
    invokeGetBindingBean(portalConfig_) ;
  }

  static public class SaveActionListener  extends EventListener<UIPortalForm> {
    public void execute(Event<UIPortalForm> event) throws Exception {
      UIPortalForm uiForm  =  event.getSource();
      String locale = uiForm.getUIStringInput("locale").getValue() ;
      LocaleConfigService localeConfigService  = uiForm.getApplicationComponent(LocaleConfigService.class) ;
      LocaleConfig localeConfig = localeConfigService.getLocaleConfig(locale);
      UIPortalApplication uiApp = uiForm.getAncestorOfType(UIPortalApplication.class);
      PortalConfig portalConfig  = uiForm.getPortalConfig();
      uiForm.invokeSetBindingBean(portalConfig);
      
      if(portalConfig.getFactoryId().equals(UIPortalForm.DEFAULT_FACTORY_ID)) portalConfig.setFactoryId(null);      
      if(localeConfig == null) localeConfig = localeConfigService.getDefaultLocaleConfig();
      uiApp.setLocale(localeConfig.getLocale());
      
      UIPortal uiPortal = Util.getUIPortal();
      uiPortal.getChildren().clear();
      UserPortalConfig userPortalConfig = uiPortal.getUserPortalConfig();
      userPortalConfig.setPortal(portalConfig);
      PortalDataModelUtil.toUIPortal(uiPortal, userPortalConfig);
      
      UIPortalApplication uiPortalApp = event.getSource().getAncestorOfType(UIPortalApplication.class);
      UIMaskWorkspace uiMaskWorkspace = uiForm.getParent();
      uiMaskWorkspace.setUIComponent(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWorkspace);
      UIPortalBrowser uiBrowser = uiPortalApp.findFirstComponentOfType(UIPortalBrowser.class);
      if(uiBrowser != null) {
        UIPageIterator  iterator = uiBrowser.getChild(UIGrid.class).getUIPageIterator();
        int currentPage = iterator.getCurrentPage();
        uiBrowser.loadPortalConfigs();
        if(currentPage > iterator.getAvailablePage()) currentPage = iterator.getAvailablePage();
        iterator.setCurrentPage(currentPage);
        UIWorkspace uiWorkingWS = uiPortalApp.findComponentById(UIPortalApplication.UI_WORKING_WS_ID);    
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingWS) ;
      }
    }
  }
  
  static public class CreateActionListener  extends EventListener<UIPortalForm> {
    public void execute(Event<UIPortalForm> event) throws Exception {
      UIPortalForm uiForm = event.getSource();
      String template = uiForm.getChild(UIFormInputItemSelector.class).getSelectedItemOption().getValue().toString();
      String portalName = uiForm.getUIStringInput("name").getValue();
      
      UserPortalConfigService service = uiForm.getApplicationComponent(UserPortalConfigService.class);
      UserPortalConfig userPortalConfig = service.createUserPortalConfig(portalName, template);
      PortalConfig pconfig = userPortalConfig.getPortalConfig();
      uiForm.invokeSetBindingBean(pconfig);
      
      PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();
      pconfig.setCreator(pcontext.getRemoteUser());
      service.update(pconfig);
      
      UIPortalApplication uiPortalApp = event.getSource().getAncestorOfType(UIPortalApplication.class);
      UIMaskWorkspace uiMaskWS = uiPortalApp.getChildById(UIPortalApplication.UI_MASK_WS_ID) ;
      uiMaskWS.setUIComponent(null);
      pcontext.addUIComponentToUpdateByAjax(uiMaskWS) ;
      
      UIPortalBrowser uiPortalBrowser = uiPortalApp.findFirstComponentOfType(UIPortalBrowser.class);
      uiPortalBrowser.loadPortalConfigs();
      pcontext.addUIComponentToUpdateByAjax(uiPortalBrowser);
    }
  }
  
  static  public class SelectItemOptionActionListener extends EventListener<UIPortalForm> {
    public void execute(Event<UIPortalForm> event) throws Exception {
      UIPortalForm uiForm = event.getSource();
      UIFormInputItemSelector templateInput = uiForm.getChild(UIFormInputItemSelector.class);
      PortalTemplateConfigOption selectItem = 
        (PortalTemplateConfigOption)templateInput.getSelectedCategory().getSelectItemOptions().get(0);
      List<String> groupIds = selectItem.getGroups();
      Group [] groups = new Group[groupIds.size()];
      OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class) ;
      for(int i = 0; i < groupIds.size(); i++) {
        groups[i] = service.getGroupHandler().findGroupById(groupIds.get(i));
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

}
