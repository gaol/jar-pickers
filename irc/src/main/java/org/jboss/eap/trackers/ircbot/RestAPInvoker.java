/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

package org.jboss.eap.trackers.ircbot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author lgao
 *
 */
public final class RestAPInvoker {

    private RestAPInvoker(){}
    
    public static <T> T getRestEntity(String apiURL, HashMap<String, String> data, Class<T> entityType) throws IOException {
        ClientBuilder builder = ClientBuilder.newBuilder();
        Client client = builder.build();
        
        WebTarget target = client.target(apiURL);
        if (data != null && data.size() > 0) {
            for (Map.Entry<String, String> entry: data.entrySet()) {
                target.queryParam(entry.getKey(), entry.getValue());
            }
        }
        Response resp = target.request().buildGet().invoke();
        int status = resp.getStatus();
        if (status == 200) {
            MediaType mediaType = resp.getMediaType();
            if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
                GenericType<T> type = new GenericType<T>(){};
                return resp.readEntity(type);
            }
            return null;
        } else if (status == 404) {
            return null;
        }
        throw new IOException("Wrong response: " + resp.getStatus());
    }
}
