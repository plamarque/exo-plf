/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.platform.cloud.services.rest;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class IntranetRESTOrganizationServiceImpl.
 */
@Path("/organization")
public class IntranetRESTOrganizationServiceImpl
{
   protected static final Logger LOG = LoggerFactory.getLogger(IntranetRESTOrganizationServiceImpl.class);
	 
   protected static final String ROOT_USER = "root";
   
   protected Format dateFormater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss SSS");
   
   protected final RepositoryService repositoryService;

   protected final OrganizationService organizationService;

   protected final String hostInfo;
   
   public IntranetRESTOrganizationServiceImpl(RepositoryService repositoryService,
      OrganizationService organizationService)
   {
      this.repositoryService = repositoryService;
      this.organizationService = organizationService;
      
      String hostname;
      try 
      {
        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        String allIfs = "";
        while (nis.hasMoreElements()) 
        {
          NetworkInterface ni = nis.nextElement();
          if (ni != null && !ni.isLoopback()) 
          {
            Enumeration<InetAddress> ia = ni.getInetAddresses();
            String allAddrs = "";
            while (ia.hasMoreElements()) 
            {
              InetAddress n = ia.nextElement();
              if (n != null && !n.isLoopbackAddress()) 
              {
                allAddrs += (allAddrs.length()>0 ? ", " : "") + n.getCanonicalHostName() + " (" + n.getHostAddress() + ")";
              }
            }
            allIfs += "[" + allAddrs + "]";
          }
        }
        
        if (allIfs.length()>0) 
        {
          hostname = allIfs;
        } 
        else 
        {
          InetAddress lo = InetAddress.getLocalHost();
          hostname = lo.getCanonicalHostName() + " (" + lo.getHostAddress() + ")";
        }
      } 
      catch(Throwable th) 
      {
        hostname = "UNKNOWN: " + th.getMessage();
      }
      
      this.hostInfo = hostname;
   }

   /**
    * Creates the user on given repository.
    *
    * @param tname the tname
    * @param baseURI the base uri
    * @param userName the user name
    * @param password the password
    * @param firstName the first name
    * @param lastName the last name
    * @param email the email
    * @return the response
    * @throws Exception the exception
    */
   @POST
   @Path("/adduser")
   @RolesAllowed("cloud-admin")
   public Response createUser(@FormParam("tname") String tname, @FormParam("URI") String baseURI,
      @FormParam("username") String userName, @FormParam("password") String password,
      @FormParam("first-name") String firstName, @FormParam("last-name") String lastName,
      @FormParam("email") String email) throws Exception
   {
      try
      {
         repositoryService.setCurrentRepositoryName(tname);
         UserHandler userHandler = organizationService.getUserHandler();
         User newUser = userHandler.createUserInstance(userName);
         newUser.setPassword(password);
         newUser.setFirstName(firstName);
         newUser.setLastName(lastName);
         newUser.setEmail(email);
         userHandler.createUser(newUser, true);

         // register user in groups '/platform/developers' and '/platform/users'
         GroupHandler groupHandler = organizationService.getGroupHandler();
         MembershipType membership = organizationService.getMembershipTypeHandler().findMembershipType("member");

         Group usersGroup = groupHandler.findGroupById("/platform/users");
         organizationService.getMembershipHandler().linkMembership(newUser, usersGroup, membership, true);
         return Response.status(HTTPStatus.CREATED).entity("Created").build();
      }
      catch (Exception e)
      {
         String err = "Unable to store user in tenant " + tname;
         LOG.error(err, e);
         throw new WebApplicationException(e, Response.status(HTTPStatus.INTERNAL_ERROR)
                                           .entity(errorMessage(err, e)).type("text/plain").build());
      }
   }

   /**
    * Creates the root user on given repository.
    *
    * @param tname the tname
    * @param password the password
    * @param firstName the first name
    * @param lastName the last name
    * @param email the email
    * @return the response
    * @throws Exception the exception
    */
   @POST
   @Path("/createroot")
   @RolesAllowed("cloud-admin")
   public Response createRoot(@FormParam("tname") String tname, @FormParam("password") String password,
      @FormParam("first-name") String firstName, @FormParam("last-name") String lastName,
      @FormParam("email") String email) throws Exception
   {
      try
      {
         repositoryService.setCurrentRepositoryName(tname);
         UserHandler userHandler = organizationService.getUserHandler();
         User rootUser = userHandler.findUserByName(ROOT_USER);
         rootUser.setPassword(password);
         rootUser.setFirstName(firstName);
         rootUser.setLastName(lastName);
         rootUser.setEmail(email);
         userHandler.saveUser(rootUser, true);//createUser(newUser, true);
         return Response.status(HTTPStatus.CREATED).entity("Created").build();
      }
      catch (Exception e)
      {
         String err = "Unable to store ROOT user in tenant " + tname;
    	   LOG.error(err, e);
    	   throw new WebApplicationException(e, Response.status(HTTPStatus.INTERNAL_ERROR)
    	                                     .entity(errorMessage(err, e)).type("text/plain").build());
      }
   }
   
   
   protected String errorMessage(String message, Exception err) {
     ByteArrayOutputStream baos = new ByteArrayOutputStream();
     PrintWriter wr = new PrintWriter(baos); 
     try 
     {
       err.printStackTrace(wr);
       wr.flush();
       
       StringBuilder str = new StringBuilder();
       str.append('[');
       str.append(dateFormater.format(new Date()));
       str.append(']');
       str.append(' ');
       str.append(hostInfo);
       str.append(':');
       str.append(message);
       str.append("\r\n");
       str.append(new String(baos.toByteArray()));
       return str.toString();
     } 
     catch (Throwable th) 
     {
       LOG.error("Cannot prepare error message:", th);
       return message + " (Error trace isn't available, see server logs (" + hostInfo + ") for details)";
     }
     finally 
     {
       wr.close();
     }
   }
}
