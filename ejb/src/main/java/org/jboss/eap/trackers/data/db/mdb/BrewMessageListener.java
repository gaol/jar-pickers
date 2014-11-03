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

package org.jboss.eap.trackers.data.db.mdb;


import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.jboss.logging.Logger;

/**
 * MDB to listen on the Brew Build information.
 * 
 * @author lgao
 *
 */
@MessageDriven(name = "BrewBuildTopicMDB", description = "MDB to listen on the Brew Build information", 
     messageListenerInterface = MessageListener.class, activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "XXXX")
})
public class BrewMessageListener implements MessageListener {

    private final static Logger LOGGER = Logger.getLogger(BrewMessageListener.class);
    
    @Inject
    private BrewBuildCollector controller;
    
    @Override
    public void onMessage(Message message) {
        TextMessage txtMsg = null;
        try {
            if (message instanceof TextMessage) {
                txtMsg = (TextMessage)message;
                String txt = txtMsg.getText();
                LOGGER.info("Message: " + txt);
                String buildId = parseBuildId(txt);
                if (buildId != null) {
                    controller.collectBrewBuilds(buildId);
                }
            }
        } catch (JMSException e) {
            throw new RuntimeException("Can't get message.", e);
        } catch (Exception e) {
            throw new RuntimeException("Can't get Brew build information.", e);
        }
    }

    private String parseBuildId(String txt) {
        if (txt == null || txt.trim().length() == 0) {
            return null;
        }
        return txt.trim();
    }

}
