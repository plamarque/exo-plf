/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.portal.webui.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.exoplatform.Constants;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.application.UIPortletActionListener.ChangePortletModeActionListener;
import org.exoplatform.portal.webui.application.UIPortletActionListener.ChangeWindowStateActionListener;
import org.exoplatform.portal.webui.application.UIPortletActionListener.EditPortletActionListener;
import org.exoplatform.portal.webui.application.UIPortletActionListener.ProcessActionActionListener;
import org.exoplatform.portal.webui.application.UIPortletActionListener.ProcessEventsActionListener;
import org.exoplatform.portal.webui.application.UIPortletActionListener.RenderActionListener;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.DeleteComponentActionListener;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.portletcontainer.PortletContainerService;
import org.exoplatform.services.portletcontainer.pci.EventInput;
import org.exoplatform.services.portletcontainer.pci.EventOutput;
import org.exoplatform.services.portletcontainer.pci.ExoWindowID;
import org.exoplatform.services.portletcontainer.pci.PortletData;
import org.exoplatform.services.portletcontainer.pci.model.Supports;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.services.portletcontainer.plugins.pc.event.EventImpl;

/**
 * May 19, 2006
 */
@ComponentConfig(
    lifecycle = UIPortletLifecycle.class,    
    template = "system:/groovy/portal/webui/application/UIPortlet.gtmpl",
    events = {
      @EventConfig(listeners = RenderActionListener.class),
      @EventConfig(listeners = ChangePortletModeActionListener.class),
      @EventConfig(listeners = ChangeWindowStateActionListener.class),
      @EventConfig(listeners = DeleteComponentActionListener.class, confirm = "UIPortlet.deletePortlet"),
      @EventConfig(listeners = EditPortletActionListener.class),
      @EventConfig(phase = Phase.PROCESS, listeners = ProcessActionActionListener.class),
      @EventConfig(phase = Phase.PROCESS, listeners = ProcessEventsActionListener.class)      
    }    
)
public class UIPortlet extends UIApplication { 
  
  protected static Log log = ExoLogger.getLogger("portal:UIPortlet"); 
  
  private String windowId ;
  private String portletStyle ;

  private boolean  showPortletMode = true ;
  
  private Map renderParametersMap_ ;
  private ExoWindowID exoWindowId_ ;
  private PortletMode currentPortletMode_ = PortletMode.VIEW;
  private WindowState currentWindowState_ = WindowState.NORMAL;  
  
  private List<String> supportModes_ ;

  private List supportedProcessingEvents_;
  
  public String getId()  { return exoWindowId_.getUniqueID() ; }
  
  public String getWindowId() { return windowId ; }
  public void   setWindowId(String s) {
    windowId = s ;
    exoWindowId_ = new ExoWindowID(windowId) ;
  }
  
  public String getPortletStyle() {  return  portletStyle ; }
  public void   setPortletStyle(String s) { portletStyle = s ;}
  
  public boolean getShowPortletMode() { return showPortletMode ; }
  public void    setShowPortletMode(Boolean b) { showPortletMode = b ; }
  
  public ExoWindowID  getExoWindowID() { return exoWindowId_ ; }
  
  public  Map  getRenderParametersMap() { return renderParametersMap_ ;}
  public  void setRenderParametersMap(Map map) { renderParametersMap_ =  map ; } 
  
  public PortletMode getCurrentPortletMode() { return currentPortletMode_ ;}
  public void  setCurrentPortletMode(PortletMode mode) { currentPortletMode_ = mode;}
  
  public WindowState getCurrentWindowState() { return currentWindowState_ ;}
  public void  setCurrentWindowState(WindowState state) { currentWindowState_ = state;}

  public  List<String> getSupportModes() { 
    if (supportModes_ != null) return supportModes_;
    PortletContainerService portletContainer =  getApplicationComponent(PortletContainerService.class);
    String  portletId = exoWindowId_.getPortletApplicationName() + Constants.PORTLET_META_DATA_ENCODER + exoWindowId_.getPortletName();   
    PortletData portletData = (PortletData) portletContainer.getAllPortletMetaData().get(portletId);
    if(portletData == null) return null;
    List supportsList = portletData.getSupports() ;
    List<String> supportModes = new ArrayList<String>() ;
    for (int i = 0; i < supportsList.size(); i++) {
      Supports supports = (Supports) supportsList.get(i) ;
      String mimeType = supports.getMimeType() ;
      if ("text/html".equals(mimeType)) {
        List modes = supports.getPortletMode() ;
        for (int j =0 ; j < modes.size() ; j++) {
          String mode =(String)modes.get(j) ;
          mode = mode.toLowerCase() ;
          //check role admin
          if("config".equals(mode)) { 
            //if(adminRole) 
            supportModes.add(mode) ;
          } else {
            supportModes.add(mode) ;
          }
        }
        break ;
      }
    }
    if(supportModes.size() > 1) supportModes.remove("view");
    setSupportModes(supportModes);
    return supportModes;
  }
  public void setSupportModes(List<String> supportModes) { supportModes_ = supportModes; }

  
  /**
   * Tells, according to the info located in portlet.xml, wether this portlet can handle
   * a portlet event with the QName given as the method argument
   */
  public boolean supportsProcessingEvent(QName name) {
	if(supportedProcessingEvents_ == null) {
      PortletContainerService portletContainer =  getApplicationComponent(PortletContainerService.class);
      String  portletId = exoWindowId_.getPortletApplicationName() + Constants.PORTLET_META_DATA_ENCODER + exoWindowId_.getPortletName();   
      PortletData portletData = (PortletData) portletContainer.getAllPortletMetaData().get(portletId);
      supportedProcessingEvents_ = portletData.getSupportedProcessingEvent();
	}
	if(supportedProcessingEvents_ == null) return false;
	for (Iterator iter = supportedProcessingEvents_.iterator(); iter.hasNext();) {
	  QName eventName = (QName) iter.next();
	  if(eventName.equals(name)) {
		log.info("The Portlet " + windowId + " supports the event : " + name);
		return true;
	  }
	}
	return false;
  }
  
  public void setSupportedProcessingEvents(List supportedProcessingEvents) {
	supportedProcessingEvents_ = supportedProcessingEvents;
  }
  public List getSupportedProcessingEvents() {
	return supportedProcessingEvents_;
  }

  /**
   * This method is called when the javax.portlet.Event is supported by the current portlet
   * stored in the Portlet Caontainer
   * 
   * The processEvent() method can also generates IPC events and hence the portal itself will
   * call the ProcessEventsActionListener once again
   */
  public Map processEvent(QName eventName, Object eventValue) {
	log.info("Process Event: " + eventName + " for portlet: " + windowId);
    try {
  	  PortletContainerService service =  getApplicationComponent(PortletContainerService.class);
   	  PortalRequestContext context = (PortalRequestContext) WebuiRequestContext.getCurrentInstance() ;
      EventInput input = new EventInput();
      String baseUrl = new StringBuilder(context.getNodeURI()).append("?" + PortalRequestContext.UI_COMPONENT_ID).append(
      	"=").append(this.getId()).toString()  ;
      input.setBaseURL(baseUrl);  
      UIPortal uiPortal = Util.getUIPortal();
      OrganizationService organizationService = getApplicationComponent(OrganizationService.class);
      UserProfile userProfile = organizationService.getUserProfileHandler().findUserProfileByName(uiPortal.getOwner()) ;    
      if(userProfile != null) input.setUserAttributes(userProfile.getUserInfoMap()) ;
      else input.setUserAttributes(new HashMap());
      input.setPortletMode(getCurrentPortletMode());
      input.setWindowState(getCurrentWindowState());
      input.setWindowID(getExoWindowID());    
      input.setMarkup("text/html");
      input.setEvent(new EventImpl(eventName, eventValue));  
	  EventOutput output = service.processEvent((HttpServletRequest)context.getRequest(), 
		  (HttpServletResponse) context.getResponse(), input);
	  return output.getEvents();
	} catch (Exception e) {
	  log.error("Problem while processesing event for the portlet: " + windowId, e);
	}
	return null;
  }
  
}
