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
package org.exoplatform.portal.gadget.core;

import org.apache.shindig.common.JsonContainerConfig;
import org.apache.shindig.common.ContainerConfigException;
import org.apache.shindig.auth.BlobCrypterSecurityTokenDecoder;
import org.apache.commons.logging.Log;
import org.exoplatform.container.monitor.jvm.J2EEServerInfo;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.commons.utils.Safe;
import com.google.inject.name.Named;
import com.google.inject.Inject;

import java.io.File;
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;

/**
 * <p>The goal of the container config subclass is to integrate security key file along
 * with exo configuration.</p>
 *
 * <p>The implementation first determine the most relevant directory for performing the key lookup.
 * It will look for a <i>gadgets</i> directory under the configuration directory returned by the
 * {@link org.exoplatform.container.monitor.jvm.J2EEServerInfo#getExoConfigurationDirectory()}
 * method. If no such valid directory can be found then the implementation use the current execution directory
 * (which should be /bin in tomcat and jboss).</p>
 *
 * <p>When the lookup directory is determined, the implementation looks for a file named key.txt.
 * If no such file is found, then it will attempt to create it with a base 64 value encoded from
 * a 32 bytes random sequence generated by {@link SecureRandom} seeded by the current time. If the
 * file exist already but is a directory then no acton is done.<p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ExoContainerConfig extends JsonContainerConfig {

  /** . */
  private Log log = ExoLogger.getLogger(ExoContainerConfig.class);

  /** . */
  private static volatile String _keyPath;

  @Inject
  public ExoContainerConfig(@Named("shindig.containers.default") String s) throws ContainerConfigException {
    super(s);

    //
    J2EEServerInfo info = new J2EEServerInfo();

    //
    String confPath = info.getExoConfigurationDirectory();

    File keyFile = null;
    if (confPath != null) {
      File confDir = new File(confPath);
      if (!confDir.exists()) {
        log.debug("Exo conf directory (" + confPath + ") does not exist");
      } else {
        if (!confDir.isDirectory()) {
          log.debug("Exo conf directory (" + confPath + ") is not a directory");
        } else {
          keyFile = new File(confDir, "gadgets/key.txt");
        }
      }
    }

    //
    if (keyFile == null) {
      keyFile = new File("key.txt");
    }

    //
    String keyPath = keyFile.getAbsolutePath();
    if (!keyFile.exists()) {
      log.debug("No key file found at path " + keyPath + " generating a new key and saving it");
      String key = generateKey();
      Writer out = null;
      try {
        out = new FileWriter(keyFile);
        out.write(key);
        out.write('\n');
        log.info("Generated key file " + keyPath + " for eXo Gadgets");
        setKeyPath(keyPath);
      } catch (IOException e) {
        log.error("Coult not create key file " + keyPath, e);
      } finally {
        Safe.close(out);
      }
    }  else if (!keyFile.isFile()) {
      log.debug("Found key file " + keyPath + " but it's not a file");
    }  else {
      log.info("Found key file " + keyPath + " for gadgets security");
      setKeyPath(keyPath);
    }
  }

  private void setKeyPath(String keyPath) {
    // _keyPath is volatile so no concurrent writes and read are safe
    synchronized (ExoContainerConfig.class) {
      if (_keyPath != null && !_keyPath.equals(keyPath)) {
        throw new IllegalStateException("There is already a configured key path old=" + _keyPath + " new=" + keyPath);
      }
      _keyPath = keyPath;
    }
  }


  @Override
  public Object getJson(String container, String parameter) {
    if (parameter.equals(BlobCrypterSecurityTokenDecoder.SECURITY_TOKEN_KEY_FILE) && _keyPath != null) {
      return _keyPath;
    }
    return super.getJson(container, parameter);
  }

  /**
   * It's not public as we don't want to expose it to the outter world. The fact that this class
   * is instantiated by Guice and the ExoDefaultSecurityTokenGenerator is managed by exo kernel
   * force us to use static reference to share the keyPath value.
   *
   * @return the key path
   */
  static String getKeyPath() {
    return _keyPath;
  }

  /**
   * Generate a key of 32 bytes encoded in base64. The generation is based on
   * {@link SecureRandom} seeded with the current time.
   *
   * @return the key
   */
  private static String generateKey() {
    try {
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
      random.setSeed(System.currentTimeMillis());
      byte bytes[] = new byte[32];
      random.nextBytes(bytes);
      BASE64Encoder encoder = new BASE64Encoder();
      return encoder.encode(bytes);
    }
    catch (NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
  }
}
