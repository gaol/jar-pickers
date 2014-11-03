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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.model.CVE;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.ProductVersion;

/**
 * @author lgao
 *
 */
public class CVEListAnswer extends AbstractAnswer {

    /**
     * cves_of EAP:6.2.4
     */
    @SuppressWarnings("unchecked")
    @Override
    public Answer answer() throws Exception {
        // first try pv, then component, then artifacts by artiId and version.
        Matcher matcher = getPattern().matcher(getQuestion());
        if (matcher.matches()) {
            String name = matcher.group(1);
            String version = matcher.group(2);
            Set<CVE> cves = new HashSet<CVE>();

            // test pv
            ProductVersion pv = RestAPInvoker.getRestEntity(getRestAPIBase() + "/pv/" + name.toUpperCase() + ":" + version, null,
                    ProductVersion.class);
            if (pv == null) {
                // test component
                Component comp = RestAPInvoker.getRestEntity(getRestAPIBase() + "/c/" + name + ":" + version, null,
                        Component.class);

                if (comp == null) {
                    // test artiId + version of Artifact
                    List<Artifact> artifacts = RestAPInvoker.getRestEntity(getRestAPIBase() + "/artis/" + name + ":" + version,
                            null, List.class);
                    if (artifacts != null && artifacts.size() > 0) {
                        for (Artifact arti : artifacts) {
                            cves.addAll(RestAPInvoker.getRestEntity(getRestAPIBase() + "/cves/a/" + arti.getGroupId() + ":"
                                    + arti.getArtifactId() + ":" + arti.getVersion(), null, Set.class));
                        }
                    }
                } else {
                    // cves of a component
                    List<Artifact> artifacts = comp.getArtis();
                    if (artifacts != null && artifacts.size() > 0) {
                        for (Artifact arti : artifacts) {
                            cves.addAll(RestAPInvoker.getRestEntity(getRestAPIBase() + "/cves/a/" + arti.getGroupId() + ":"
                                    + arti.getArtifactId() + ":" + arti.getVersion(), null, Set.class));
                        }
                    }
                }
            } else {
                // cves of pv
                cves = RestAPInvoker.getRestEntity(getRestAPIBase() + "/cves/p/" + name + ":" + version, null, Set.class);
            }
            Answer answer = new Answer();
            answer.setAnswered(true);
            if (cves != null && cves.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (CVE cve : cves) {
                    sb.append("[" + cve.getName() + "] ");
                }
                answer.setAnswer(sb.toString());
            } else {
                answer.setAnswer("Not found.");
            }
            return answer;
        }
        return null;
    }
}
