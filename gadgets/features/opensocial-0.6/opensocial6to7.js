// If a gadget was written for the opensocial 0.6 apis and it wants to run in a
// container that only supports 0.7, including this file should allow the gadget
// to run without making any changes.

opensocial.requestNavigateTo = function() {
  return gadgets.views.requestNavigateTo();
};

// If using enums, gadgets are ok.
// TODO: Translate hardcoded string params to the new gadgets values
opensocial.makeRequest = function() {
  return gadgets.io.makeRequest();
};

opensocial.ContentRequestParameters = {
  METHOD : gadgets.io.RequestParameters.METHOD,
  CONTENT_TYPE : gadgets.io.RequestParameters.CONTENT_TYPE,
  AUTHENTICATION : gadgets.io.RequestParameters.AUTHORIZATION,
  NUM_ENTRIES : gadgets.io.RequestParameters.NUM_ENTRIES,
  GET_SUMMARIES : gadgets.io.RequestParameters.GET_SUMMARIES
};

opensocial.ContentRequestParameters.MethodType = {
  GET : gadgets.io.MethodType.GET,
  POST : gadgets.io.MethodType.POST
};

opensocial.ContentRequestParameters.ContentType = {
  HTML : gadgets.io.ContentType.TEXT,
  XML : gadgets.io.ContentType.DOM,
  FEED : gadgets.io.ContentType.JSON
};

opensocial.ContentRequestParameters.AuthenticationType = {
  NONE : gadgets.io.AuthorizationType.NONE,
  SIGNED : gadgets.io.AuthorizationType.SIGNED,
  AUTHENTICATED : gadgets.io.AuthorizationType.AUTHENTICATED
};


opensocial.Person.prototype.getFieldOld = opensocial.Person.prototype.getField;

opensocial.Person.prototype.getField = function(fieldname) {
  if (fieldname == opensocial.Person.Field.NAME) {
    return this.getFieldOld(opensocial.Person.Field.NAME)
        .getField(opensocial.Name.Field.UNSTRUCTURED);
  } else {
    return this.getFieldOld(fieldname);
  }
}

opensocial.Person.prototype.getDisplayNameOld
    = opensocial.Person.getDisplayName;
opensocial.Person.prototype.getDisplayName = function() {
  return this.getFieldOld(opensocial.Person.Field.NAME)
      .getField(opensocial.Name.Field.UNSTRUCTURED);
}

opensocial.newActivityOld = opensocial.newActivity;
opensocial.newActivity = function(title, opt_params) {
  opt_params['title'] = title;
  opensocial.newActivityOld(opt_params);
};

opensocial.DataRequest.prototype.newFetchGlobalAppDataRequest = function(keys) {
  // This should be a no-op. This call never fetched anything relavant because
  // you couldn't set any global app data.
  // However, we don't want containers to crash, so we will just fetch person
  // app data. This seems the best we can do for now.
  return this.newFetchPersonAppDataRequest(keys);
};

opensocial.DataRequest.prototype.newFetchInstanceAppDataRequest =
    function(keys) {
  var moduleId = new gadgets.Prefs().getModuleId();
  if (opensocial.Container.isArray(keys)) {
    for (var i = 0; i < keys.length; i++) {
      keys[i] = moduleId + keys[i];
    }
  } else {
    keys = moduleId + keys;
  }

  return this.newFetchPersonAppDataRequest('OWNER', keys);
};

opensocial.DataRequest.prototype.newUpdateInstanceAppDataRequest = function(key,
    value) {
  var moduleId = new gadgets.Prefs().getModuleId();
  return this.newUpdatePersonAppDataRequest('OWNER', moduleId + key);
};

// The surface object is gone, but the user never directly knew about the object
// (ie they couldn't construct it) so we will just make the methods look the
// same.
gadgets.views.View.prototype.isPrimaryContent = function() {
  return this.isOnlyVisibleGadget();
}

// Note: The names of views may have changed in a container between 0.6 and 0.7
// but that is container specific

opensocial.Environment.prototype.getSurface = function() {
  return gadgets.views.getCurrentView();
};
opensocial.Environment.prototype.getSupportedSurfaces = function() {
  return gadgets.views.getSupportedViews();
};
opensocial.Environment.prototype.getParams = function() {
  return gadgets.views.getParams();
};
opensocial.Environment.prototype.hasCapability = function() {
  return gadgets.util.hasFeature();
};
