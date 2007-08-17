/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.webui.form.validator;

import org.exoplatform.webui.form.UIFormInput;
/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minhdv81@yahoo.com
 * Jun 7, 2006
 * 
 * The base interface to create a validator
 */
public interface Validator {
  public void validate(UIFormInput uiInput) throws Exception ;
}
