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
package org.exoplatform.application.gadget.jcr;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.application.gadget.SourceStorage;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Aug 6, 2008  
 */
public class SourceStorageImpl implements SourceStorage {
  
  private RepositoryService repoService;
  private String repo;
  private String wsName;
  private String storePath;
  
  public SourceStorageImpl(InitParams params, RepositoryService service) throws Exception {
    PropertiesParam properties = params.getPropertiesParam("location");
    if(properties == null) throw new Exception("The 'location' properties parameter is expected.");
    repo = properties.getProperty("repository");
    wsName = properties.getProperty("workspace");
    storePath = properties.getProperty("store.path");
    repoService = service;
  }

  public String getSource(String name) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    Node homeNode = getHomeNode(sessionProvider) ;
    String source = homeNode.getNode(name + ".xml" + "/jcr:content").getProperty("jcr:data").getString() ;
    sessionProvider.close() ;
    return source ;
  }

  public void saveSource(String name, String source) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    Session session = sessionProvider.getSession(wsName, repoService.getRepository(repo)) ;
    Node homeNode = (Node) session.getItem(storePath);
    String fileName = name + ".xml" ;
    saveContent(homeNode, fileName, source);
//    if(!homeNode.hasNode(fileName)) {
//      Node fileNode = homeNode.addNode(fileName, "nt:file") ;
//      contentNode = fileNode.addNode("jcr:content", "nt:resource") ;
//    } else contentNode = homeNode.getNode(fileName + "/jcr:content") ;
//    contentNode.setProperty("jcr:data", source) ;
//    contentNode.setProperty("jcr:mimeType", "text/xml") ;
//    contentNode.setProperty("jcr:lastModified", Calendar.getInstance()) ;
    session.save() ;
    sessionProvider.close() ;
  }

  public void removeSource(String name) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    Session session = sessionProvider.getSession(wsName, repoService.getRepository(repo)) ;
    Node homeNode = (Node) session.getItem(storePath);
    Node sourceNode = homeNode.getNode(name + ".xml") ;
    sourceNode.remove() ;
    session.save() ;
    sessionProvider.close() ;
  }

  public String getSourcePath(String name) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    Node homeNode = getHomeNode(sessionProvider) ;
    StringBuilder link = new StringBuilder(30);
    link.append("rest/jcr/").append(repo).append("/")
    .append(wsName).append(homeNode.getNode(name + ".xml").getPath());
    sessionProvider.close() ;
    return link.toString() ;
  }

  private Node getHomeNode(SessionProvider sessionProvider) throws Exception {
    Session session = sessionProvider.getSession(wsName, repoService.getRepository(repo)) ;
    return (Node) session.getItem(storePath) ; 
  }

  public void addDependency(String name, String dependencyName,
                            String dependencySource) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    Session session = sessionProvider.getSession(wsName, repoService.getRepository(repo)) ;
    Node homeNode = (Node) session.getItem(storePath);
    Node parent;
    if(!homeNode.hasNode(name))  parent = homeNode.addNode(name, "nt:unstructured");
    else parent = homeNode.getNode(name);
    saveContent(parent, dependencyName, dependencySource);
    session.save();
    sessionProvider.close();
  }

  private void saveContent(Node parentNdoe, String name, String source) throws Exception {
    Node contentNode ;
    if(!parentNdoe.hasNode(name)) {
      Node fileNode = parentNdoe.addNode(name, "nt:file") ;
      contentNode = fileNode.addNode("jcr:content", "nt:resource") ;
    } else contentNode = parentNdoe.getNode(name + "/jcr:content") ;
    contentNode.setProperty("jcr:data", source) ;
    contentNode.setProperty("jcr:mimeType", "text/xml") ;
    contentNode.setProperty("jcr:lastModified", Calendar.getInstance()) ;
  }
  
}