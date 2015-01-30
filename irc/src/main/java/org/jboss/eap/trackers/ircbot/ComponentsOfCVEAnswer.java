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

import java.util.Set;
import java.util.regex.Matcher;

import javax.ws.rs.core.GenericType;

import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.model.Component;

/**
 * @author lgao
 *
 */
public class ComponentsOfCVEAnswer extends AbstractAnswer {

    /**
     * components of CVE-xxxx-xxxx
     */
    @Override
    public Answer answer() throws Exception {
        Matcher matcher = getPattern().matcher(getQuestion());
        if (matcher.matches()) {
            String cve = matcher.group(1);
            Set<Component> nativeComps = RestAPInvoker.getRestEntity(getRestAPIBase() + "/cves/c/" + cve, null, new GenericType<Set<Component>>(){});
            Answer answer = new Answer();
            answer.setAnswered(true);
            StringBuilder sb = new StringBuilder();
            if (nativeComps != null && nativeComps.size() > 0) {
                // native components
                sb.append("Native Components: ");
                for (Component comp: nativeComps) {
                    sb.append("[" + comp.getName() + ":" + comp.getVersion() + "] ");
                }
                answer.setAnswer(sb.toString());
            }
            Set<Artifact> artifacts = RestAPInvoker.getRestEntity(getRestAPIBase() + "/cves/a/" + cve, null, new GenericType<Set<Artifact>>(){});
            if (artifacts != null && artifacts.size() > 0) {
                // normal components
                boolean first = true;
                for (Artifact arti: artifacts) {
                    Component comp = arti.getComponent();
                    if (comp != null) {
                        if (first) {
                            first = false;
                            sb.append("\nComponents: ");
                        }
                        sb.append("[" + comp.getName() + ":" + comp.getVersion() + "] ");
                    }
                }
            }
            if ((nativeComps == null || nativeComps.size() == 0) && (artifacts == null || artifacts.size() == 0)) {
                answer.setAnswer("Not Found.");
            }
            return answer;
        }
        return null;
    }
}
