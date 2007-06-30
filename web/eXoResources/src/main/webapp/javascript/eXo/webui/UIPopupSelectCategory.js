function UIPopupSelectCategory() {
} ;

UIPopupSelectCategory.prototype.show = function(selectedElement, width, e) {
	if(!e) e = window.event;
	e.cancelBubble = true;
	if(e.stopPropagation) e.stopPropagation();
	var DOMUtil = eXo.core.DOMUtil;
	var ancestorPopupCategory = DOMUtil.findAncestorByClass(selectedElement, "AncestorPopupCategory") ;
	var categoryDetectPosition = DOMUtil.findAncestorByClass(selectedElement, "CategoryDetectPosition") ;
	var controlCategory = DOMUtil.findFirstDescendantByClass(ancestorPopupCategory, "div", "ControlIcon") ;
  var uiPopupCategory = DOMUtil.findFirstDescendantByClass(ancestorPopupCategory, "div", "UIPopupCategory") ;
	if(uiPopupCategory == null) return;
	/**Repaired: by Vu Duy Tu 30/06/07
	 * TODO: check for IE7 
	 **/ 
	if(uiPopupCategory.style.display != "block") {
		uiPopupCategory.style.position = "absolute" ;
		uiPopupCategory.style.display = "block";
		uiPopupCategory.style.width = width + "px";
		
		var posLeft = eXo.core.Browser.findPosX(categoryDetectPosition) - width + controlCategory.offsetWidth + 28;
		/* 28 is distance between arrow and PopupCategoryRight */
		  posLeft -= ancestorPopupCategory.offsetLeft;
		if (eXo.portal.UIControlWorkspace.showControlWorkspace) {
			 posLeft -= eXo.portal.UIControlWorkspace.defaultWidth ;
		} else {
			posLeft -= 5;
			/* SlidebarButton Width */
		}
		uiPopupCategory.style.left = posLeft + "px";
	} else {
		uiPopupCategory.style.display = "none";
	}
	
	/*Add uiPopupCategory to the list element will be display to "none" when click on document*/
	eXo.core.DOMUtil.listHideElements(uiPopupCategory);
} ;

UIPopupSelectCategory.prototype.selectedCategoryIndex = function(selectedElement) {
	var parentNode = selectedElement.parentNode ;
	var categoryItems = eXo.core.DOMUtil.findChildrenByClass(parentNode, "div", "CategoryItem") ;
	for(var i = 0; i < categoryItems.length; i++) {
		if(categoryItems[i] == selectedElement) return i ;
	}
} ;

UIPopupSelectCategory.prototype.selectdCategory = function(selectedElement) {
	selectedElement.className = "CategoryItem" ;
	var ancestorPopupCategory = eXo.core.DOMUtil.findAncestorByClass(selectedElement, "AncestorPopupCategory") ;
	var categoryItems = eXo.core.DOMUtil.findDescendantsByClass(ancestorPopupCategory, "div", "CategoryContainer") ;
	var selectedIndex = eXo.webui.UIPopupSelectCategory.selectedCategoryIndex(selectedElement);
	
	for(var i = 0 ;i < categoryItems.length; i++) {
		if(i != selectedIndex ) categoryItems[i].style.display = "none" ;
		else categoryItems[i].style.display = "block" ;
	}
	
	var uiPopupCategory = eXo.core.DOMUtil.findAncestorByClass(selectedElement, "UIPopupCategory") ;
	ancestorPopupCategory.style.position = "static" ;
	uiPopupCategory.style.display = "none" ;
} ;

UIPopupSelectCategory.prototype.onMouseOver = function(selectedElement, over) {
	if(over) {
		selectedElement.onmouseup = function() {
			var uiPopupCategory = eXo.core.DOMUtil.findAncestorByClass(selectedElement, "UIPopupCategory") ;
			uiPopupCategory.style.display = "none" ;
		};
		selectedElement.className = "CategoryItem OverCategoryItem" ;
	} else {
		selectedElement.className = "CategoryItem" ;
	}
} ;

UIPopupSelectCategory.prototype.setDefault = function(selectedElement) {
	var ancestorPopupCategory = eXo.core.DOMUtil.findAncestorByClass(selectedElement, "AncestorPopupCategory") ;
	var uiPopupCategory = eXo.core.DOMUtil.findFirstDescendantByClass(ancestorPopupCategory, "div", "UIPopupCategory") ;

	uiPopupCategory.style.top = "22px" ;		
	ancestorPopupCategory.style.position = "absolute" ;
};

eXo.webui.UIPopupSelectCategory = new UIPopupSelectCategory() ;
