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
package org.exoplatform.portal.webui.application;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletMode;
import javax.portlet.ResourceURL;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.exoplatform.Constants;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.portletcontainer.PortletContainerService;
import org.exoplatform.services.portletcontainer.pci.ActionInput;
import org.exoplatform.services.portletcontainer.pci.ActionOutput;
import org.exoplatform.services.portletcontainer.pci.EventInput;
import org.exoplatform.services.portletcontainer.pci.EventOutput;
import org.exoplatform.services.portletcontainer.pci.ResourceInput;
import org.exoplatform.services.portletcontainer.pci.ResourceOutput;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * May 29, 2006
 */
public class UIPortletActionListener {

  public static final String PORTLET_EVENTS = "PortletEvents";

  protected static Log log = ExoLogger
      .getLogger("portal:UIPortletActionListener");

  /**
   * The process action listener is called when an ActionURL generated by the
   * portlet container has been invoked by the client
   *
   * The call is delegated to the portlet container iteself using the method
   * portletContainer.processAction(...). It returns an object of type
   * ActionOutput that contains several information such as the next window
   * state and portlet modes (if they have to change) as well as a list of
   * Events to be broadcasted to the other portlets located in the same portal
   * page
   */
  static public class ProcessActionActionListener extends
      EventListener<UIPortlet> {
    public void execute(Event<UIPortlet> event) throws Exception {
      UIPortlet uiPortlet = event.getSource();
      PortalRequestContext prcontext = (PortalRequestContext) event
          .getRequestContext();
      ExoContainer container = event.getRequestContext().getApplication()
          .getApplicationServiceContainer();
      UIPortalApplication uiPortalApp = uiPortlet
          .getAncestorOfType(UIPortalApplication.class);
      PortletContainerService portletContainer = (PortletContainerService) container
          .getComponentInstanceOfType(PortletContainerService.class);
      ActionInput actionInput = new ActionInput();
      OrganizationService service = uiPortlet
          .getApplicationComponent(OrganizationService.class);
      UserProfile userProfile = service.getUserProfileHandler()
          .findUserProfileByName(uiPortalApp.getOwner());
      actionInput.setInternalWindowID(uiPortlet.getExoWindowID());
      if (userProfile != null)
        actionInput.setUserAttributes(userProfile.getUserInfoMap());
      else
        actionInput.setUserAttributes(new HashMap());
      HashMap allParams = new HashMap();
      allParams.putAll(prcontext.getRequest().getParameterMap());
      actionInput.setRenderParameters(allParams);
      actionInput.setPortletMode(uiPortlet.getCurrentPortletMode());
      actionInput.setWindowState(uiPortlet.getCurrentWindowState());
      actionInput.setMarkup("text/html");
      actionInput.setStateChangeAuthorized(true);
      ActionOutput output = portletContainer.processAction(prcontext
          .getRequest(), prcontext.getResponse(), actionInput);

      /*
       * Update the portlet window state according to the action output
       * information
       *
       * If the current node is displaying a usual layout page, also tells the
       * page which portlet to render or not when the state is maximized
       */
      setNextState(uiPortlet, output.getNextState());

      // update the portlet with the next mode to display
      setNextMode(uiPortlet, output.getNextMode());

      // set the public params
      HttpServletRequest request = event.getRequestContext().getRequest();
      setupPublicRenderParams(uiPortlet, request.getParameterMap());

      /*
       * Cache the render parameters in the UI portlet component to handle the
       * navigational state. Each time a portlet is rendered (except using
       * directly a RenderURL) those parameters are added to the portlet request
       * to preserve the portlet state among all the portal clicks
       */
      uiPortlet.setRenderParametersMap(output.getRenderParameters());

      /*
       * Handle the events returned by the action output and broadcast a new UI
       * event to the ProcessEventsActionListener that will then target the
       * portlet container service directly
       */
      List<javax.portlet.Event> events = output.getEvents();
      if (events != null) {
        prcontext.setAttribute(PORTLET_EVENTS, new EventsWrapper(events));
        uiPortlet.createEvent("ProcessEvents", Phase.PROCESS, prcontext)
            .broadcast();
      }
    }
  }

  /**
   * This method is used to set the next portlet window state if this one needs to
   * be modified because of the incoming request
   */
  public static void setNextState(UIPortlet uiPortlet, WindowState state) {
    if (state != null) {
      UIPage uiPage = uiPortlet.getAncestorOfType(UIPage.class);
      if (state == WindowState.MAXIMIZED) {
        uiPortlet.setCurrentWindowState(WindowState.MAXIMIZED);
        if (uiPage != null)
          uiPage.setMaximizedUIPortlet(uiPortlet);
      } else if (state == WindowState.MINIMIZED) {
        uiPortlet.setCurrentWindowState(WindowState.MINIMIZED);
        if (uiPage != null)
          uiPage.setMaximizedUIPortlet(null);
      } else {
        uiPortlet.setCurrentWindowState(WindowState.NORMAL);
        if (uiPage != null)
          uiPage.setMaximizedUIPortlet(null);
      }
    }
  }

  /**
   * This method is used to set the next portlet mode if this one needs to
   * be modified because of the incoming request
   */
  public static void setNextMode(UIPortlet uiPortlet, PortletMode portletMode) {
    if (portletMode != null) {
      if (portletMode.equals(PortletMode.HELP)) {
        uiPortlet.setCurrentPortletMode(PortletMode.HELP);
      } else if (portletMode.equals(PortletMode.EDIT)) {
        uiPortlet.setCurrentPortletMode(PortletMode.EDIT);
      } else {
        uiPortlet.setCurrentPortletMode(PortletMode.VIEW);
      }
    }
  }

  /**
   * The serveResource() method defined in the JSR 286 specs has several goals:
   *   - provide binary output like images to be displayed in the portlet (in the previous
   *     spec - JSR 168 - a servlet was needed)
   *   - provide text output that does not impact the entire portal rendering, it is for
   *     instance usefull when dealing with Javascript to return some JSON structures
   *
   *  The method delegates the call to the portlet container serverResource method after
   *  filling the ResourceInput object with the current request state.
   *
   *  This returns a ResourceOutput object that can content binary or text contentType
   *
   *  Finally the content is set in the portal response writer or outputstream depending
   *  on the type; the processRender() method of the portal is not called as we set the
   *  response as complete
   *
   */
  static public class ServeResourceActionListener extends
      EventListener<UIPortlet> {
    public void execute(Event<UIPortlet> event) throws Exception {
      UIPortlet uiPortlet = event.getSource();
      log.info("Serve Resource for portlet: " + uiPortlet.getWindowId());
      try {
        ExoContainer container = event.getRequestContext().getApplication()
            .getApplicationServiceContainer();
        UIPortalApplication uiPortalApp = uiPortlet
            .getAncestorOfType(UIPortalApplication.class);
        PortletContainerService portletContainer = (PortletContainerService) container
            .getComponentInstanceOfType(PortletContainerService.class);
        ResourceInput input = new ResourceInput();
        OrganizationService service = uiPortlet
            .getApplicationComponent(OrganizationService.class);
        UserProfile userProfile = service.getUserProfileHandler()
            .findUserProfileByName(uiPortalApp.getOwner());
        input.setInternalWindowID(uiPortlet.getExoWindowID());
        if (userProfile != null)
          input.setUserAttributes(userProfile.getUserInfoMap());
        else
          input.setUserAttributes(new HashMap());
        input.setPortletMode(uiPortlet.getCurrentPortletMode());
        input.setWindowState(uiPortlet.getCurrentWindowState());

        input.setMarkup("text/html");
        PortalRequestContext context = (PortalRequestContext) event
            .getRequestContext();
        input.setRenderParameters(getResourceParameterMap(uiPortlet, context));
        input.setCacheability(context.getCacheLevel());
        String baseUrl = new StringBuilder(context.getRequestURI()).append(
            "?" + PortalRequestContext.UI_COMPONENT_ID).append("=").append(
            uiPortlet.getId()).toString();
        input.setBaseURL(baseUrl);
        HttpServletRequest request = (HttpServletRequest) context.getRequest();
        HttpServletResponse response = context.getResponse();
        String resourceId = request
            .getParameter(Constants.RESOURCE_ID_PARAMETER);
        if (resourceId != null) {
          input.setResourceID(resourceId);
        }

        ResourceOutput output = portletContainer.serveResource(request,
            (HttpServletResponse) context.getResponse(), input);

        //Manage headers
        context.setHeaders(output.getHeaderProperties());
        
        if (resourceId != null)
          return;
        
        String contentType = output.getContentType();
        log.info("Try to get a resource of type: " + contentType
            + " for the portlet: " + uiPortlet.getWindowId());
        if (contentType.startsWith("text")) {
          context.getWriter().write(output.getContent());
        } else {
          response.setContentType(contentType);
          OutputStream stream = response.getOutputStream();
          stream.write(output.getBinContent());
        }
        context.getResponse().flushBuffer();

      } catch (Exception e) {
        log.error("Problem while serving resource for the portlet: "
            + uiPortlet.getWindowId(), e);
      } finally {
        /**
         * The resource method does not need to go through the render phase
         */
        event.getRequestContext().setResponseComplete(true);
      }
    }
  }
  
  /**
   * This method returns all the parameters supported by the targeted portlets,
   * both the private and public ones
   */
  @SuppressWarnings( { "unchecked" })
  private static Map getResourceParameterMap(UIPortlet uiPortlet,
      PortalRequestContext prcontext) {
    
    Map portletParams = new HashMap(prcontext.getPortletParameters());
    
    /*
     * For serveResource requests the portlet must receive any resource parameters that 
     * were explicitly set on the ResourceURL that triggered the request. If 
     * the cacheability level of that resource URL (see PLT.13.7) was PORTLET or PAGE, 
     * the portlet must also receive the render parameters present in the request in 
     * which the URL was created. 
     * 
     * If a resource parameter is set that has the same name as a render parameter, 
     * the render parameter must be the last entry in the parameter value array.
     */
    String cacheLevel = prcontext.getCacheLevel();
    if(ResourceURL.PAGE.equals(cacheLevel) || ResourceURL.PORTLET.equals(cacheLevel)) {
      Map renderParams = uiPortlet.getRenderParametersMap();
      if(renderParams != null) {
        Set keys = renderParams.keySet();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
          String key = (String) iter.next();
          if(portletParams.containsKey(key)) {
            String[] renderValueArray = (String[]) renderParams.get(key);
            String[] portletValueArray = (String[]) portletParams.get(key);
            String[] resources = new String[renderValueArray.length + portletValueArray.length];
            System.arraycopy(portletValueArray, 0, resources, 0, portletValueArray.length);
            System.arraycopy(renderValueArray, 0, resources, portletValueArray.length, renderValueArray.length);
            portletParams.put(key, resources);
          } else {
            portletParams.put(key, renderParams.get(key));
          }
        }
        portletParams.putAll(renderParams);
      }
    }
    return portletParams;
  }  

  /**
   * Process Events sent by the portlet API during the processAction() and
   * serverResource() methods defined in Portlet API 2.0 (JSR 286)
   */
  static public class ProcessEventsActionListener extends
      EventListener<UIPortlet> {
    public void execute(Event<UIPortlet> event) throws Exception {
      UIPortlet uiPortlet = event.getSource();
      PortalRequestContext context = (PortalRequestContext) event
          .getRequestContext();
      List<UIPortlet> portletInstancesInPage = new ArrayList<UIPortlet>();
      UIPortalApplication uiPortal = uiPortlet.getAncestorOfType(UIPortalApplication.class);
      uiPortal.findComponentOfType(portletInstancesInPage, UIPortlet.class);
      EventsWrapper eventsWrapper = (EventsWrapper) event.getRequestContext()
          .getAttribute(PORTLET_EVENTS);
      List<javax.portlet.Event> events = eventsWrapper.getEvents(); 
      
      /*
       * Iterate over all the events that the processAction 
       * has generated. Check among all the portlet instances deployed in the
       * page (usual layout or webos) which instance can be targeted by the
       * event and then process the event on the associated UIPortlet component
       */
      for (Iterator<javax.portlet.Event> iter = events.iterator(); iter.hasNext();) {
        javax.portlet.Event nativeEvent = iter.next();
        QName eventName = nativeEvent.getQName();
        for (Iterator iterator = portletInstancesInPage.iterator(); iterator
            .hasNext();) {
          UIPortlet uiPortletInPage = (UIPortlet) iterator.next();
          if (uiPortletInPage.supportsProcessingEvent(eventName)
              && !eventsWrapper.isInvokedTooManyTimes(uiPortletInPage
                  .getWindowId())) {
            List<javax.portlet.Event> newEvents = processEvent(uiPortletInPage, nativeEvent);
            eventsWrapper.increaseCounter(uiPortletInPage.getWindowId());
            if (context.useAjax()) {
              log
                  .info("Events were generated inside the scope of an AJAX call, hence will only refresh the targeted portlets");
              event.getRequestContext().addUIComponentToUpdateByAjax(
                  uiPortletInPage);
            } else {
              log
                  .info("Events were generated outside the scope of an AJAX call, hence will make a full render of the page");
              context.setFullRender(true);
            }
            if (newEvents != null && !newEvents.isEmpty()) {
              log.info("The portlet: " + uiPortletInPage.getWindowId()
                  + " processEvent() method has generated new events itself");
              events.addAll(newEvents);
              uiPortlet.createEvent("ProcessEvents", Phase.PROCESS, context)
                  .broadcast();
            }
          }
        }
      }
    }
  }


  /**
   * This method is called when the javax.portlet.Event is supported by the
   * current portlet stored in the Portlet Caontainer
   *
   * The processEvent() method can also generates IPC events and hence the
   * portal itself will call the ProcessEventsActionListener once again
   */
  public static List<javax.portlet.Event> processEvent(UIPortlet uiPortlet, javax.portlet.Event event) {
    log.info("Process Event: " + event.getName() + " for portlet: "
        + uiPortlet.getWindowId());
    try {
      PortletContainerService service = uiPortlet
          .getApplicationComponent(PortletContainerService.class);
      PortalRequestContext context = (PortalRequestContext) WebuiRequestContext
          .getCurrentInstance();
      EventInput input = new EventInput();
      String baseUrl = new StringBuilder(context.getRequestURI()).append(
          "?" + PortalRequestContext.UI_COMPONENT_ID).append("=").append(
          uiPortlet.getId()).toString();
      input.setBaseURL(baseUrl);
      UIPortal uiPortal = Util.getUIPortal();
      OrganizationService organizationService = uiPortlet
          .getApplicationComponent(OrganizationService.class);
      UserProfile userProfile = organizationService.getUserProfileHandler()
          .findUserProfileByName(uiPortal.getOwner());
      if (userProfile != null)
        input.setUserAttributes(userProfile.getUserInfoMap());
      else
        input.setUserAttributes(new HashMap());
      input.setPortletMode(uiPortlet.getCurrentPortletMode());
      input.setWindowState(uiPortlet.getCurrentWindowState());
      input.setInternalWindowID(uiPortlet.getExoWindowID());
      input.setMarkup("text/html");
      input.setEvent(event);
      EventOutput output = service.processEvent((HttpServletRequest) context
          .getRequest(), (HttpServletResponse) context.getResponse(), input);
      /*
       * Update the portlet window state according to the action output
       * information
       *
       * If the current node is displaying a usual layout page, also tells the
       * page which portlet to render or not when the state is maximized
       */
      setNextState(uiPortlet, output.getNextState());

      // update the portlet with the next mode to display
      setNextMode(uiPortlet, output.getNextMode());      
      return output.getEvents();
    } catch (Exception e) {
      log.error("Problem while processesing event for the portlet: "
          + uiPortlet.getWindowId(), e);
    }
    return null;
  }

  /**
   * This listener is called when a RenderURL url has been generated by the
   * portlet container. In that case it means that the render() method of a
   * targeted portlet will be directly called and that the existing navigational
   * state will be reset by removing all the Render Parameters from the cache
   * map located in the UIPortlet
   */
  static public class RenderActionListener extends EventListener<UIPortlet> {
    public void execute(Event<UIPortlet> event) throws Exception {
      UIPortlet uiPortlet = event.getSource();
      uiPortlet.setRenderParametersMap(null);

      // set the public params
      HttpServletRequest request = event.getRequestContext().getRequest();
      setupPublicRenderParams(uiPortlet, request.getParameterMap());
      
      //set render params
      Map<String, String[]> renderParams = ((PortalRequestContext)event.getRequestContext()).getPortletParameters();
      uiPortlet.setRenderParametersMap(renderParams);
    }
  }


  /**
   * This method is called by the process action and render action listeners,
   * aka during the processDecode() phase of our UI framework
   *
   * It goes throughs all the request parameters and add to the public render
   * parameters Map the one that are supported by the targeted portlet
   */
  static public void setupPublicRenderParams(UIPortlet uiPortlet,
      Map<String, String[]> requestParams) {
    UIPortal uiPortal = Util.getUIPortal();
    Map<String, String[]> publicParams = uiPortal.getPublicParameters();

    Iterator<String> keys = requestParams.keySet().iterator();
    while (keys.hasNext()) {
      String key = keys.next();
      if (uiPortlet.supportsPublicParam(key))
        publicParams.put(key, requestParams.get(key));
    }

  }

  static public class ChangeWindowStateActionListener extends
      EventListener<UIPortlet> {
    public void execute(Event<UIPortlet> event) throws Exception {
      UIPortlet uiPortlet = event.getSource();

      UIPortalApplication uiPortalApp = uiPortlet
          .getAncestorOfType(UIPortalApplication.class);
      UIWorkingWorkspace uiWorkingWS = uiPortalApp
          .getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      PortalRequestContext pcontext = (PortalRequestContext) event
          .getRequestContext();
      pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);
      pcontext.setFullRender(true);

      String windowState = event.getRequestContext().getRequestParameter("portal:windowState") ;
      //TODO TrongTT: We should use only parameter for change WindowState
      if(windowState == null) windowState = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID).trim();
      UIPageBody uiPageBody = uiPortlet.getAncestorOfType(UIPageBody.class);
      UIPage uiPage = uiPortlet.getAncestorOfType(UIPage.class) ;
      if (windowState.equals(WindowState.MAXIMIZED.toString())) {
        if (uiPageBody != null) {
          uiPortlet.setCurrentWindowState(WindowState.MAXIMIZED);
          //TODO dang.tung: we have to set maximized portlet for page because in ShowMaxWindow case the PageBody isn't rendered
          //                reference: UIPortalLifecycle, UIPageLifecycle, renderChildren() in UIPageBody
          //---------------------------------------------------------
          if(uiPage != null && uiPage.isShowMaxWindow()) uiPage.setMaximizedUIPortlet(uiPortlet) ;
          //---------------------------------------------------------
          uiPageBody.setMaximizedUIComponent(uiPortlet);
        } else {
          uiPortlet.setCurrentWindowState(WindowState.NORMAL);
        }
        return;
      }
      if (uiPageBody != null) {
        UIPortlet maxPortlet = (UIPortlet) uiPageBody.getMaximizedUIComponent();
        if (maxPortlet == uiPortlet)
          uiPageBody.setMaximizedUIComponent(null);
      }
      //TODO dang.tung: for ShowMaxWindow situation
      //----------------------------------------------------------------
      if (uiPage != null) {
        UIPortlet maxPortlet = (UIPortlet) uiPage.getMaximizedUIPortlet();
        if (maxPortlet == uiPortlet)
          uiPage.setMaximizedUIPortlet(null);
      }
      //-----------------------------------------------------------------
      if (windowState.equals(WindowState.MINIMIZED.toString())) {
        uiPortlet.setCurrentWindowState(WindowState.MINIMIZED);
        return;
      }
      uiPortlet.setCurrentWindowState(WindowState.NORMAL);

    }
  }

  /**
   * This listener is called when the portlet mode of a portlet has to be
   * changed.
   */
  static public class ChangePortletModeActionListener extends
      EventListener<UIPortlet> {
    public void execute(Event<UIPortlet> event) throws Exception {
      UIPortlet uiPortlet = event.getSource();
      String portletMode = event.getRequestContext().getRequestParameter("portal:portletMode") ;
      //TODO TrongTT: We should use only parameter for change PortletMode
      if(portletMode == null) portletMode = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
      
      log.info("Change portlet mode of " + uiPortlet.getWindowId() + " to "
          + portletMode);
      if (portletMode.equals(PortletMode.HELP.toString())) {
        uiPortlet.setCurrentPortletMode(PortletMode.HELP);
      } else if (portletMode.equals(PortletMode.EDIT.toString())) {
        uiPortlet.setCurrentPortletMode(PortletMode.EDIT);
      } else {
        uiPortlet.setCurrentPortletMode(PortletMode.VIEW);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
    }
  }

  /**
   * This listener is called when the portlet edit form (which tells information
   * about the portlet width or height as well as if the info bar and its
   * content should be shown) is invoked.
   *
   * It places the form in the portal black mask
   */
  static public class EditPortletActionListener extends
      EventListener<UIPortlet> {
    public void execute(Event<UIPortlet> event) throws Exception {
      UIPortal uiPortal = Util.getUIPortal();
      UIPortalApplication uiApp = uiPortal
          .getAncestorOfType(UIPortalApplication.class);
      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
      uiMaskWS.setUpdated(true) ;
      UIPortlet uiPortlet = event.getSource();
      UIPortletForm uiPortletForm = uiMaskWS.createUIComponent(UIPortletForm.class, null, null);
      uiPortletForm.setValues(uiPortlet);
      uiMaskWS.setWindowSize(800, -1);
    }
  }

}
