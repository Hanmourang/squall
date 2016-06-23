/*
 * Copyright (c) 2011-2015 EPFL DATA Laboratory
 * Copyright (c) 2014-2015 The Squall Collaboration (see NOTICE)
 *
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.epfl.data.squall.utilities;

import backtype.storm.serialization.HeronPluggableSerializerDelegate;

import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.apache.log4j.Logger;


/*
 * Used in local mode to deserialize for the REPL and DBToaster
 */
public class SquallSerializationDelegate extends HeronPluggableSerializerDelegate {
  private static Logger LOG = Logger.getLogger(SquallSerializationDelegate.class);

  private URL classdir;

  @Override
  public void initialize(Map<String,Object> stormConf) {
    super.initialize(stormConf);

    LOG.info("Setting up SquallSerializationDelegate");
    LOG.info(stormConf.toString());
    try {
      String classdirPath = (String)stormConf.get("squall.classdir");
      if (classdirPath != null) {
        classdir = new File(classdirPath).toURL();
        LOG.info("Adding '" + classdir + "' as search path for deserializing");
      } else {
        throw new RuntimeException("Squall Serialization delegate was set, but option squall.classdir is empty");
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object deserialize(byte[] bytes) {
    try {
      return super.deserialize(bytes);
    } catch (RuntimeException e) {
      try {
        if (classdir == null) throw e;

        URLClassLoader classloader = new URLClassLoader(new URL[]{classdir},
                                                        Thread.currentThread().getContextClassLoader());

        // Read the object
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ClassLoaderObjectInputStream(classloader, bis);
        Object ret = ois.readObject();
        ois.close();
        return ret;
      } catch (ClassNotFoundException error) {
        throw new RuntimeException(error);
      } catch (IOException error) {
        throw new RuntimeException(error);
      }
    }
  }
}
