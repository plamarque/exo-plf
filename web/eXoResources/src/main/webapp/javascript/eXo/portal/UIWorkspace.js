function UIWorkspace(id) {
  this.id = id ;
  this.showControlWorkspace = false ;
  this.isFirstTime = true ;
}

if(eXo.portal.Workspace == undefined) {
  eXo.portal.Workspace = new UIWorkspace("UIWorkspace") ;
}
if(eXo.portal.UIControlWorkspace == undefined) {
  eXo.portal.UIControlWorkspace = new UIWorkspace("UIControlWorkspace") ;
}

eXo.portal.UIControlWorkspace.onResize = function(width, height) {
	this.width = width ;
	this.height = height ;
	var uiWorkspace = document.getElementById(this.id) ;
	var uiWorkspaceContainer = document.getElementById("UIWorkspaceContainer") ;
	this.uiWorkspaceControl = document.getElementById("UIWorkspaceControl") ;
	var uiWorkspacePanel = document.getElementById("UIWorkspacePanel") ;
	
	uiWorkspace.style.width = width + "px" ;
	uiWorkspace.style.height = height + "px" ;
		
	/*	In case uiWorkspaceContainer is setted display to none, uiWorkspaceControl.offsetHeight equal 0 
	 * 	23 is the height of User Workspace Title.
	 * */
	
	if(eXo.portal.UIControlWorkspace.showControlWorkspace == true) {
		uiWorkspaceContainer.style.display = "block" ;
		uiWorkspaceContainer.style.width = eXo.portal.UIControlWorkspace.defaultWidth + "px" ;
	}
	uiWorkspacePanel.style.height = (height - this.uiWorkspaceControl.offsetHeight - 23) + "px" ;
	
	
	/*Fix Bug on IE*/
	eXo.portal.UIControlWorkspace.slidebar.style.height = height + "px" ;
	uiWorkspace.style.top = document.documentElement.scrollTop + "px" ;
} ;

eXo.portal.UIControlWorkspace.onResizeDefault = function() {
	var cws = eXo.portal.UIControlWorkspace ;
	cws.defaultWidth = 250 ;
	cws.slidebarDefaultWidth = 6;
	cws.slidebar = document.getElementById("ControlWorkspaceSlidebar") ;
	
	if(this.isFirstTime) {
		cws.originalSlidebarWidth = cws.slidebar.offsetWidth ;
		this.isFirstTime = false ;
	}
	
	if(cws.showControlWorkspace) {
		cws.onResize(cws.defaultWidth, eXo.core.Browser.getBrowserHeight()) ;
	} else {
		cws.onResize(cws.slidebar.offsetWidth, eXo.core.Browser.getBrowserHeight()) ;
	}
		
};
   	
eXo.portal.UIControlWorkspace.showWorkspace = function() {
	var cws = eXo.portal.UIControlWorkspace ;
	var uiWorkspace = document.getElementById(this.id) ;
	var uiWorkspaceContainer = document.getElementById("UIWorkspaceContainer") ;
	var uiWorkspacePanel = document.getElementById("UIWorkspacePanel") ;
	var slidebar = document.getElementById("ControlWorkspaceSlidebar") ;
	slidebar.childrens = slidebar.getElementsByTagName("div") ;
	
	if(cws.showControlWorkspace == false) {
		cws.showControlWorkspace = true ;
	
		slidebar.style.display = "none" ;
		uiWorkspace.style.width = cws.defaultWidth + "px" ;
		uiWorkspaceContainer.style.width = cws.defaultWidth + "px" ;

		uiWorkspaceContainer.style.display = "block" ;
		
		uiWorkspacePanel.style.height = (eXo.portal.UIControlWorkspace.height - 
																		 eXo.portal.UIControlWorkspace.uiWorkspaceControl.offsetHeight - 23) + "px" ;
		/*23 is height of User Workspace Title*/
		eXo.portal.UIControlWorkspace.width = uiWorkspace.offsetWidth;
		eXo.portal.UIWorkingWorkspace.onResize(null, null) ;
	} else {
		cws.showControlWorkspace = false ;
		uiWorkspaceContainer.style.display = "none" ;
		slidebar.style.display = "block" ;
		uiWorkspace.style.width = slidebar.offsetWidth + "px";
		eXo.portal.UIControlWorkspace.width = eXo.portal.UIControlWorkspace.slidebar.offsetWidth ;
		eXo.portal.UIWorkingWorkspace.onResize(null, null) ;
	}
	/* Reorganize opened windows */
	eXo.portal.UIWorkingWorkspace.reorganizeWindows(this.showControlWorkspace);
	/*Resize Dockbar*/
	var uiPageDesktop = document.getElementById("UIPageDesktop") ;
	if(uiPageDesktop != null) eXo.desktop.UIDockbar.resizeDockBar() ;
};

/*#############################-Working Workspace-##############################*/
if(eXo.portal.UIWorkingWorkspace == undefined) {
  eXo.portal.UIWorkingWorkspace = new UIWorkspace("UIWorkingWorkspace") ;
}

eXo.portal.UIWorkingWorkspace.onResize = function(width, height) {
	var uiWorkspace = document.getElementById(this.id) ;
	var uiControlWorkspace = document.getElementById("UIControlWorkspace") ;
  var controlWorkspaceWidth = eXo.portal.UIControlWorkspace.width ;
    
//  if(eXo.core.Browser.isIE6()) {
//  	this.slidebar = document.getElementById("ControlWorkspaceSlidebar") ;
//  	if(this.slidebar) {
//  		uiWorkspace.style.width = (eXo.core.Browser.getBrowserWidth() - controlWorkspaceWidth - this.slidebar.offsetWidth) + "px";
//  	}
//  }
 
  if(uiControlWorkspace) {
  	uiWorkspace.style.marginLeft = controlWorkspaceWidth + "px" ;
  } else {
  	uiWorkspace.style.marginLeft = "0px" ;
  }
};

eXo.portal.UIWorkingWorkspace.onResizeDefault = function(event) {
  eXo.portal.UIWorkingWorkspace.onResize(null, null) ;
};

eXo.portal.UIWorkingWorkspace.reorganizeWindows = function(showControlWorkspace) {
	var uiDesk = document.getElementById("UIPageDesktop");
	if (uiDesk != null) {
		var uiCtrl = document.getElementById("UIControlWorkspace");
		var uiWindows = eXo.core.DOMUtil.findDescendantsByClass(uiDesk, "div", "UIWindow");
		for (var k = 0; k < uiWindows.length; k++) {
			if (uiWindows[k].style.display != "none") {
				// We reorganize the opened windows (display != none) only
				uiWindow = uiWindows[k];
				if (showControlWorkspace) {
					// When the ControlWorkspace is shown
					uiWindow.oldW = uiWindow.offsetWidth;
					uiWindow.oldX = uiWindow.offsetLeft;
					if (uiWindow.offsetLeft > eXo.portal.UIControlWorkspace.defaultWidth) {
						uiWindow.style.left = 
						(uiWindow.offsetLeft-eXo.portal.UIControlWorkspace.defaultWidth+eXo.portal.UIControlWorkspace.slidebarDefaultWidth)+"px";
					}
					if ((uiWindow.offsetLeft+uiWindow.offsetWidth) > uiDesk.offsetWidth) {
						uiWindow.style.width = 
						(uiWindow.offsetWidth-eXo.portal.UIControlWorkspace.defaultWidth+eXo.portal.UIControlWorkspace.slidebarDefaultWidth)+"px";
					}
				} else {
					// When the ControlWorkspace is hidden
					if (uiWindow.maximized) {
						// If the window is maximized, we set the size to its maximum : the desktop size
						uiWindow.style.width = uiDesk.offsetWidth+"px";
						uiWindow.style.left = uiDesk.offsetLeft+"px";
					} else {
						uiWindow.style.width = uiWindow.oldW+"px";
						uiWindow.style.left = uiWindow.oldX+"px";
					}
				}
			}
		}
	}
};