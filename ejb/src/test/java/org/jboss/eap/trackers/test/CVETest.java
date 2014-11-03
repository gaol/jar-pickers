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

package org.jboss.eap.trackers.test;

import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.model.CVE;
import org.jboss.eap.trackers.model.ProductVersion;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author lgao
 *
 */
@RunWith(Arquillian.class)
public class CVETest extends AbstractTrackersTest {

    @Test
    public void testCVEs() throws Exception
    {
        String cveName = "CVE-2014-3547";
        Set<Artifact> artifacts = dataService.affectedArtifacts(cveName);
        Assert.assertNotNull(artifacts);
        Assert.assertFalse(artifacts.isEmpty());
        Assert.assertEquals(1, artifacts.size());
        
        Artifact arti = artifacts.iterator().next();
        
        Assert.assertEquals("org.jboss.ironjacamar", arti.getGroupId());
        Assert.assertEquals("ironjacamar-common-impl", arti.getArtifactId());
        Assert.assertEquals("1.0.2.Final", arti.getVersion());
        
        Set<ProductVersion> pvs = dataService.affectedProducts(cveName);
        Assert.assertNotNull(pvs);
        Assert.assertFalse(pvs.isEmpty());
        
        // eap 6.2.4
        Assert.assertEquals(1, pvs.size());
        
        ProductVersion pv = pvs.iterator().next();
        Assert.assertEquals("EAP", pv.getProduct().getName());
        Assert.assertEquals("6.2.4", pv.getVersion());
        
        // check cve list of a product version
        SortedSet<CVE> cves = dataService.productCVEs("EAP", "6.2.4");
        Assert.assertNotNull(cves);
        Assert.assertEquals(1, cves.size());
        CVE cve = cves.first();
        Assert.assertEquals("CVE-2014-3547", cve.getName());
        
        // check cve list of an artifact
        cves = dataService.artifactCVEs("org.jboss.ironjacamar", "ironjacamar-common-impl", "1.0.2.Final");
        Assert.assertNotNull(cves);
        Assert.assertEquals(1, cves.size());
        cve = cves.first();
        Assert.assertEquals("CVE-2014-3547", cve.getName());
        
    }
    
    @Test
    public void testUpdateCVEs() throws Exception
    {
        tracker.call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                
                CVE cve = dataService.newCVE("CVE-2012-1234");
                Assert.assertNotNull(cve);
                Assert.assertNull(cve.getNote());
                Assert.assertEquals("CVE-2012-1234", cve.getName());
                
                cve.setNote("Test Note");
                cve = dataService.updateCVE(cve);
                Assert.assertEquals("Test Note", cve.getNote());
                
                // this step only can be performed manually in real world
                cve = dataService.cveAffected("CVE-2012-0001", "org.jboss.ironjacamar", "ironjacamar-common-impl", "<=1.0.2.Final", false);
                Assert.assertEquals("CVE-2012-0001", cve.getName());
                
                Set<Artifact> artifacts = dataService.affectedArtifacts("CVE-2012-0001");
                Assert.assertNotNull(artifacts);
                Assert.assertEquals(1, artifacts.size());
                
                Artifact arti = artifacts.iterator().next();
                Assert.assertEquals("org.jboss.ironjacamar", arti.getGroupId());
                Assert.assertEquals("ironjacamar-common-impl", arti.getArtifactId());
                Assert.assertEquals("1.0.2.Final", arti.getVersion());
                
                Set<ProductVersion> pvs = dataService.affectedProducts("CVE-2012-0001");
                Assert.assertNotNull(pvs);
                Assert.assertFalse(pvs.isEmpty());
                
                // eap 6.2.4
                Assert.assertEquals(1, pvs.size());
                
                ProductVersion pv = pvs.iterator().next();
                Assert.assertEquals("EAP", pv.getProduct().getName());
                Assert.assertEquals("6.2.4", pv.getVersion());
                
                SortedSet<CVE> cves = dataService.productCVEs("EAP", "6.2.4");
                Assert.assertNotNull(cves);
                Assert.assertEquals(2, cves.size());
                CVE firstCVE = cves.first();
                Assert.assertEquals("CVE-2012-0001", firstCVE.getName());
                
                CVE lastCVE = cves.last();
                Assert.assertEquals("CVE-2014-3547", lastCVE.getName());
                
                // eap 6.2.0                     
                cves = dataService.productCVEs("EAP", "6.2.0");
                Assert.assertNotNull(cves);
                Assert.assertEquals(1, cves.size());
                cve = cves.first();
                Assert.assertEquals("CVE-2014-3566", cve.getName());
                
                
                return null;
            }
        });
    }
}
