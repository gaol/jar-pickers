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

import org.jboss.eap.trackers.model.ProductVersion;

/**
 * @author lgao
 *
 */
public class ProductsOfCVEAnswer extends AbstractAnswer {

    /**
     * products of CVE-xxxx-xxxx
     */
    @SuppressWarnings("unchecked")
    @Override
    public Answer answer() throws Exception {
        Matcher matcher = getPattern().matcher(getQuestion());
        if (matcher.matches()) {
            String cve = matcher.group(1);
            Set<ProductVersion> pvs = RestAPInvoker.getRestEntity(getRestAPIBase() + "/cves/p/" + cve, null, Set.class);
            Answer answer = new Answer();
            answer.setAnswered(true);
            if (pvs != null && pvs.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (ProductVersion pv: pvs) {
                    sb.append("[" + pv.getProduct().getName().toUpperCase() + ":" + pv.getVersion() + "] ");
                }
                answer.setAnswer(sb.toString());
            } else {
                answer.setAnswer("Not Found.");
            }
            return answer;
        }
        return null;
    }
}
