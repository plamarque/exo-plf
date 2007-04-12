eXo.require('eXo.webui.UIPopupMenu');

function UIExoStartMenu() {
	this.buttonClicked = false ;
} ;

UIExoStartMenu.prototype.init = function(popupMenu, container, x, y) {
	var uiStart = eXo.portal.UIExoStartMenu;
	
	this.superClass = eXo.webui.UIPopupMenu;
	this.superClass.itemStyleClass = "MenuItem";
	this.superClass.itemOverStyleClass = "MenuItemOver";
	this.superClass.containerStyleClass = "MenuItemContainer";
	this.superClass.init(popupMenu, container.id, x, y) ;
	
	this.exoStartButton = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "ExoStartButton") ;
	this.exoStartButton.onmouseover = uiStart.startButtonOver ;
	this.exoStartButton.onmouseout = uiStart.startButtonOut ;
	
	this.superClass.buildMenu(popupMenu, uiStart.onMenuItemOver, uiStart.onMenuItemOut);
} ;

UIExoStartMenu.prototype.onLoad = function() {
	var uiStartContainer = document.getElementById("StartMenuContainer") ;
	var uiExoStart = document.getElementById("UIExoStart") ;
	eXo.portal.UIExoStartMenu.init(uiStartContainer, uiExoStart, 0, 0);
	eXo.webui.UIPopupMenu.hide(uiStartContainer);
	eXo.core.ExoDateTime.getTime() ;
};


UIExoStartMenu.prototype.startButtonOver = function() {
	if(!eXo.portal.UIExoStartMenu.buttonClicked) {
		this.className = "ExoStartButton ButtonOver" ;
	}
};

UIExoStartMenu.prototype.startButtonOut = function() {
	if(!eXo.portal.UIExoStartMenu.buttonClicked) {
		this.className = "ExoStartButton ButtonNormal" ;
	}
};

UIExoStartMenu.prototype.showStartMenu = function(e) {
	if(!e) var e = window.event;
	e.cancelBubble = true;
	
	var uiStartContainer = document.getElementById("StartMenuContainer") ;
	eXo.portal.UIExoStartMenu.exoStartButton.className = "ExoStartButton ButtonClicked" ;
	if(uiStartContainer.style.display == "block") {
		eXo.portal.UIExoStartMenu.hideUIStartMenu();
	} else {
		eXo.portal.UIExoStartMenu.buttonClicked = true ;
		this.superClass.show(uiStartContainer);
		var menuY = eXo.core.Browser.findPosY(eXo.portal.UIExoStartMenu.exoStartButton);
		var y = menuY - uiStartContainer.offsetHeight;
		uiStartContainer.style.width = "238px";
		this.superClass.setPosition(uiStartContainer, 0, y) ;
	}
	
	/*Hide eXoStartMenu whenever click on the UIApplication*/
	var uiPortalApplication = document.getElementById("UIPortalApplication") ;
	uiPortalApplication.onclick = eXo.portal.UIExoStartMenu.hideUIStartMenu ;
	
//	eXo.core.DOMUtil.listHideElements(uiStartContainer);
};

UIExoStartMenu.prototype.hideUIStartMenu = function() {
	var uiStartContainer = document.getElementById("StartMenuContainer") ;
	eXo.webui.UIPopupMenu.hide(uiStartContainer);
	eXo.portal.UIExoStartMenu.buttonClicked = false ;
	eXo.portal.UIExoStartMenu.exoStartButton.className = "ExoStartButton ButtonNormal" ;
};

UIExoStartMenu.prototype.onMenuItemOver = function(e) {
	eXo.webui.UIPopupMenu.onMenuItemOver(e);
	var menuItem = eXo.core.Browser.getEventSource(e);
	if (menuItem.className != eXo.portal.UIExoStartMenu.superClass.itemStyleClass) {
		 menuItem = eXo.core.DOMUtil.findAncestorByClass(menuItem, eXo.portal.UIExoStartMenu.superClass.itemStyleClass);
	}
	menuItem.className = eXo.portal.UIExoStartMenu.superClass.itemOverStyleClass ;
	var labelItem = eXo.core.DOMUtil.findFirstDescendantByClass(menuItem, "div", "LabelItem") ;
	// If the pointed menu item contains a link, sets the item clickable
	var link = eXo.core.DOMUtil.findDescendantsByTagName(labelItem, "a")[0];
	if (link && link.href) {
		window.status = link.href;
		menuItem.onclick = function() {
			if (link.href.substr(0, 7) == "http://") window.location.href = link.href;
			else eval(link.href);
			return false;
		}
	}
	// If the pointed menu item contains a submenu, resize it
	var menuItemContainer = eXo.core.DOMUtil.findFirstDescendantByClass(menuItem, "div", eXo.portal.UIExoStartMenu.superClass.containerStyleClass) ;
	if (menuItemContainer && eXo.core.Browser.getBrowserType() == "ie" && !menuItemContainer.resized) {
		// Resizes the container only once, the first time. After, container.resized is true so the condition is false
		var menuCenter = eXo.core.DOMUtil.findFirstDescendantByClass(menuItemContainer, "div", "StartMenuML");
		var menuTop = eXo.core.DOMUtil.findFirstDescendantByClass(menuItemContainer, "div", "StartMenuTL");
		var decorator = eXo.core.DOMUtil.findFirstDescendantByClass(menuTop, "div", "StartMenuTR");
		var menuBottom = menuTop.nextSibling;
		while (menuBottom.className != "StartMenuBL") menuBottom = menuBottom.nextSibling;
		var w = menuCenter.offsetWidth - decorator.offsetLeft;
		menuTop.style.width = w;
		menuBottom.style.width = w;
		menuCenter.style.width = w;
		menuItemContainer.resized = true;
	}
};

UIExoStartMenu.prototype.onMenuItemOut = function(e) {
	eXo.webui.UIPopupMenu.onMenuItemOut(e);
	window.status = "";
};

eXo.portal.UIExoStartMenu = new UIExoStartMenu() ;