/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.test.capedwarf.testsuite.init.support;

import java.util.Collections;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.apphosting.api.ApiProxy;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class InitTestListener implements ServletContextListener, ServletRequestListener {
    private static final String KEY = "__KEY";
    @SuppressWarnings("UnusedDeclaration")
    private static final DatastoreService service;

    static {
        service = DatastoreServiceFactory.getDatastoreService();
    }

    public void contextInitialized(ServletContextEvent sce) {
        service.get(Collections.singleton(KeyFactory.createKey("NO_SUCH_KIND", 1)));
    }

    public void requestInitialized(ServletRequestEvent sre) {
        ApiProxy.getCurrentEnvironment().getAttributes().put(KEY, KEY);
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        Object value = ApiProxy.getCurrentEnvironment().getAttributes().get(KEY);
        if (value == null) {
            throw new RuntimeException("Missing KEY!");
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
