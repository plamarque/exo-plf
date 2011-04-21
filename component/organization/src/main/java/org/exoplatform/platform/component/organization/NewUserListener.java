/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.platform.component.organization;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * This Listener is invoked when a User is updated/added. Its purpose is to
 * ensure that OrganizationServiceIntegration don't apply Organization
 * Model Data listeners twice.
 * 
 * @author Boubaker KHANFIR
 */
public class NewUserListener extends UserEventListener {

  private RepositoryService repositoryService;

  public NewUserListener(RepositoryService repositoryService) throws Exception {
    this.repositoryService = repositoryService;
  }

  /**
   * {@inheritDoc}
   */
  public void postSave(User user, boolean isNew) throws Exception {
    if (!Util.hasUserFolder(repositoryService, user)) {
      Util.createUserFolder(repositoryService, user);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void postDelete(User user) throws Exception {
    if (Util.hasUserFolder(repositoryService, user)) {
      Util.deleteUserFolder(repositoryService, user);
    }
  }
}