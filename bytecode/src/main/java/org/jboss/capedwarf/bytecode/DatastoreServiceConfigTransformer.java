/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.bytecode;

import java.lang.reflect.Modifier;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DatastoreServiceConfigTransformer extends RewriteTransformer {
    private final static String DEFAULT = "getCallbacksConfigInputStream";
    private final static String INTERNAL = DEFAULT + "Internal";

    protected void transformInternal(CtClass clazz) throws Exception {
        // current
        CtMethod method = clazz.getDeclaredMethod(DEFAULT);

        // create new
        CtMethod newMethod = CtNewMethod.copy(method, INTERNAL, clazz, null);
        clazz.addMethod(newMethod);

        String body = "{" +
                "java.io.InputStream is = " + INTERNAL + "();" +
                "if (is == null) {" +
                "   is = Thread.currentThread().getContextClassLoader().getResourceAsStream(\"/META-INF/datastorecallbacks.xml\");" +
                "}" +
                "return is;" +
                "}";
        method.setBody(body);

        // remove final
        CtField callbacksField = clazz.getDeclaredField("instanceDatastoreCallbacks");
        callbacksField.setModifiers(Modifier.PRIVATE | Modifier.VOLATILE);

        // use instance callbacks
        CtMethod callbacks = clazz.getDeclaredMethod("getDatastoreCallbacks");
        callbacks.setBody("{" +
                "if (instanceDatastoreCallbacks == null) {" +
                "   java.io.InputStream is = getCallbacksConfigInputStream();" +
                "   if (is == null) {" +
                "       instanceDatastoreCallbacks = com.google.appengine.api.datastore.DatastoreCallbacks.NoOpDatastoreCallbacks.INSTANCE;" +
                "   } else {" +
                "       instanceDatastoreCallbacks = new com.google.appengine.api.datastore.DatastoreCallbacksImpl(is, false);" +
                "   }" +
                "}" +
                "return instanceDatastoreCallbacks;" +
                "}");

    }

    protected boolean doCheck(CtClass clazz) throws NotFoundException {
        return clazz.getDeclaredMethod(INTERNAL) != null;
    }
}
