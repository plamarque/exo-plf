/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.portal.config.model;

import java.util.ArrayList;

/**
 * May 13, 2004
 * @author: Tuan Nguyen
 * @email:   tuan08@users.sourceforge.net
 * @version: $Id: Page.java,v 1.9 2004/11/03 01:23:55 tuan08 Exp $
 **/
public class Page extends Container {
  
  final static public String DESKTOP_PAGE = "Desktop";
  final static public String DEFAULT_PAGE = "Default";
  
  private transient String   pageId;
  
  private String   ownerType;
  private String   ownerId;
  
  private String[] accessPermissions ;
  
  private String editPermission;
  
  private boolean  showMaxWindow = false ;
  
  private String   creator ;
  private String   modifier ;
  
  private transient boolean modifiable ;
  
  public Page() {
  }
  
  public String getOwnerId() { return ownerId; }
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId; 
    this.pageId = null;
  }

  public String getOwnerType() { return ownerType; }
  public void setOwnerType(String ownerType) { 
    this.ownerType = ownerType;
    this.pageId = null;
  }
  
  public String[] getAccessPermissions() { return accessPermissions ; }
  public void     setAccessPermissions(String[] s) { accessPermissions = s; }
  
  public String getEditPermission() { return editPermission; }
  public void setEditPermission(String editPermission) { this.editPermission = editPermission; }
  
  public boolean isShowMaxWindow() { return showMaxWindow; }  
  public void setShowMaxWindow(Boolean showMaxWindow) {
    this.showMaxWindow = showMaxWindow.booleanValue(); 
  }
  
  public void setName(String name) {
    super.setName(name);
    this.pageId = null;
  }
  
  public String getPageId() {
    if(pageId == null) pageId = ownerType +"::"+ownerId+"::"+name;
    return pageId; 
  }
  
  public boolean isModifiable() { return modifiable ; }
  public void    setModifiable(boolean b) { modifiable = b ; }
  
  public String getCreator()  {  return creator ; }
  public void   setCreator(String s) { creator = s ; }
  
  public String getModifier() { return modifier ; }
  public void   setModifier(String s) { modifier = s ; }
  
  static public class PageSet {
    private ArrayList<Page> pages ;
    public PageSet() { pages = new ArrayList<Page>(); }
    public ArrayList<Page> getPages() { return pages ; }
    public void setPages(ArrayList<Page> list) { pages = list ; }
  }
  
}