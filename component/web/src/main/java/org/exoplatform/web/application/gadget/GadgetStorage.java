/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.web.application.gadget;



/**
 * Created by The eXo Platform SAS
 * @author Pham Thanh Tung
 *          thanhtungty@gmail.com</br>
 * Jul 28, 2008</br>
 * This class is used to add and get gadget application. 
 */
public interface GadgetStorage {
  
  /**
   * Gets the gadget application from portal by name
   * @param name the name of gadget application
   * @return the gadget application or null if not found
   * @throws Exception
   */
  public GadgetApplication getGadget(String name) throws Exception ;
  
  /**
   * Adds the gadget application to portal
   * @param app the gadget application
   * @throws Exception
   */
  public void addGadget(GadgetApplication app) throws Exception ;

}
