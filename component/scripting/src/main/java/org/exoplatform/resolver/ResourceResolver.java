/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.resolver ;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Mar 15, 2006
 */
abstract public class ResourceResolver {
  
  abstract public URL getResource(String url) throws Exception ;
  abstract public InputStream getInputStream(String url) throws Exception  ;
  
  abstract public List<URL> getResources(String url) throws Exception ;
  abstract public List<InputStream> getInputStreams(String url) throws Exception  ;
  
  @SuppressWarnings("unused")
  public String getWebAccessPath(String url) {
    throw new RuntimeException("This method is not supported") ;
  }
  
  abstract public String getResourceScheme() ;
  
  @SuppressWarnings("unused")
  public String getRealPath(String url) {
    throw new RuntimeException("unsupported method") ;
  }
  
  public String createResourceId(String url) {  return hashCode() + ":" +  url ; }
  
  public boolean isResolvable(String url) {
    return url.startsWith(getResourceScheme()) ;
  }
  
  public byte[] getResourceContentAsBytes(String url) throws Exception {
    InputStream is = getInputStream(url) ;
    BufferedInputStream buffer = new BufferedInputStream(is);    
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    byte[] data  = new byte[buffer.available()];      
    int available = -1;
    while( (available = buffer.read(data)) > -1){
      output.write(data, 0, available);
    }   
    return output.toByteArray();
  }
  
  abstract public boolean isModified(String url, long lastAccess) ;
  
  protected String removeScheme(String url) {
    String scheme = getResourceScheme() ;
    if(url.startsWith(scheme)) {
      return url.substring(scheme.length()) ; 
    }
    return url ;
  }
}