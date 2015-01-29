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

package org.jboss.eap.trackers.test.brew;

import java.io.File;
import java.util.concurrent.Callable;

import javax.ejb.EJB;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.db.DBDataService;
import org.jboss.eap.trackers.data.db.DataServiceLocal;
import org.jboss.eap.trackers.data.db.mdb.BrewBuildCollector;
import org.jboss.eap.trackers.data.versioning.VersionRanges;
import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.test.AbstractTrackersTest;
import org.jboss.eap.trackers.test.TrackerBean;
import org.jboss.eap.trackers.utils.ArtifactsUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author lgao
 *
 */
@RunWith(Arquillian.class)
public class BrewBuildCollectTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        File[] libs = Maven.resolver().loadPomFromFile(new File("pom.xml"))
                .importRuntimeAndTestDependencies()
                .resolve()
                .withMavenCentralRepo(true)
                .withClassPathResolution(true)
                .withMavenCentralRepo(true)
                .withTransitivity().asFile();
        
        WebArchive res = ShrinkWrap
                .create(WebArchive.class, "brew-test.war")
                .addAsResource("META-INF/test-persistence.xml",
                        "META-INF/persistence.xml")
                .addAsResource("META-INF/orm.xml", "META-INF/orm.xml")
                .addAsResource("artis.txt", "artis.txt")
                .addAsResource("comps.txt", "comps.txt")
                .addAsResource("import.sql", "import.sql")
                .addPackage(DataService.class.getPackage())
                .addPackage(DBDataService.class.getPackage())
                .addPackage(Product.class.getPackage())
                .addPackage(VersionRanges.class.getPackage())
                .addPackage(AbstractTrackersTest.class.getPackage())
                .addClass(ArtifactsUtil.class)
                .addClass(BrewBuildCollector.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                ;
        
        for (File lib: libs) {
            res.addAsLibraries(lib);
        }
        return res;
    }

    @EJB
    DataServiceLocal dataService;
    
    @EJB(name = "TrackerBean")
    TrackerBean tracker;
    
    @Inject
    private BrewBuildCollector controller;
    
    @Test
    public void testMeadBuildCollect() throws Exception
    {
        tracker.call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTest();
                return null;
            }
        });
    }

    protected void doTest() throws Exception {
        
        // mead build only collects artifacts.
        String buildId = "org.apache.httpcomponents-project-6_redhat_2-1"; // 378841 org.apache.httpcomponents-project-6_redhat_2-1
        Artifact arti = this.dataService.getArtifact("org.apache.httpcomponents", "httpcore", "4.2.1.redhat-2");
        Assert.assertNull(arti);
        
        controller.collectBrewBuild(buildId);
        
        // after collection.
        arti = this.dataService.getArtifact("org.apache.httpcomponents", "httpcore", "4.2.1.redhat-2");
        Assert.assertNotNull(arti);
        
        // why choose this wield package for testing ? version differs from its artifacts.
        Component meadComp = dataService.getComponent("org.apache.httpcomponents-project", "6-redhat-2");
        Assert.assertNotNull(meadComp);
        Assert.assertEquals("org.apache.httpcomponents", meadComp.getGroupId());
        Assert.assertEquals(11, meadComp.getArtis().size());
        
        // wrapperRPM build collects both component and artifacts
        buildId = "jbossws-spi-2.3.1-1.Final_redhat_1.1.ep6.el6"; // 398060 jbossws-spi-2.3.1-1.Final_redhat_1.1.ep6.el6
        Component comp = dataService.getComponent("jbossws-spi", "2.3.1.Final-redhat-1");
        Assert.assertNull(comp);
        
        controller.collectBrewBuild(buildId);
        comp = dataService.getComponent("jbossws-spi", "2.3.1.Final-redhat-1");
        Assert.assertNotNull(comp);
        Assert.assertEquals("org.jboss.ws", comp.getGroupId());
        Assert.assertNotNull(comp.getArtis());
        Assert.assertEquals(1, comp.getArtis().size());
        Assert.assertEquals("jbossws-spi", comp.getArtis().get(0).getArtifactId());
        
        // native build
        buildId = "tomcat-native-1.1.32-1.redhat_1.ep6.el6"; // 396415 tomcat-native-1.1.32-1.redhat_1.ep6.el6
        comp = dataService.getComponent("tomcat-native", "1.1.32");
        Assert.assertNull(comp);
        controller.collectBrewBuild(buildId);
        comp = dataService.getComponent("tomcat-native", "1.1.32");
        Assert.assertNotNull(comp);
        Assert.assertNull(comp.getGroupId());
        Assert.assertTrue(comp.isNative());
        
    }
}
