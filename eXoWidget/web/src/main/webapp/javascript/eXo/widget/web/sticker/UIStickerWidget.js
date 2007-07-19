eXo.require('eXo.widget.UIExoWidget');

UIStickerWidget.prototype = eXo.widget.UIExoWidget;
UIStickerWidget.prototype.constructor = UIStickerWidget;

function UIStickerWidget() {
	this.init("UIStickerWidget", "sticker");
}

UIStickerWidget.prototype.sendContent = function(object) {	

	if (object.value == "")	return ;
	var DOMUtil = eXo.core.DOMUtil ;
	var uiWidgetContainer = DOMUtil.findAncestorByClass(object, "WidgetApplication") ;
	containerBlockId = uiWidgetContainer.id;
	var parent = uiWidgetContainer.parentNode ;
  alert(object.value + " TO--> " +  parent.id);
	var params = [
  	{name: "objectId", value : object.id} ,
  	{name: "stickerContent", value : object.value}
  ] ;
  var url = eXo.env.server.context + "/command?" ;
  url += "type=org.exoplatform.web.command.handler.WidgetHandler&action=saveContent&componentId="+parent.id+"&content="+object.value ;
  ajaxAsyncGetRequest(url, false);
} ;

if(eXo.widget.web == null) eXo.widget.web = {} ;
if(eXo.widget.web.sticker == null) eXo.widget.web.sticker = {};
eXo.widget.web.sticker.UIStickerWidget = new UIStickerWidget()  ;
