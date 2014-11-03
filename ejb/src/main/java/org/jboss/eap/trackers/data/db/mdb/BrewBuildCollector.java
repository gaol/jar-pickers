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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.jboss.eap.trackers.data.DataServiceException;
import org.jboss.eap.trackers.data.db.DataServiceLocal;
import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.model.Component;
import org.jboss.logging.Logger;

/**
 * 
 * @author lgao
 *
 */
@ApplicationScoped
@RunAs("tracker")
public class BrewBuildCollector {
    
    private static final String BREW_URL = "http://brewhub.devel.redhat.com/brewhub";
    
    private XmlRpcClient client;
    
    @EJB
    private DataServiceLocal dataService;
    
    private final static Logger LOGGER = Logger.getLogger(BrewBuildCollector.class);
    
    @PostConstruct
    public void setUp() {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        client = new XmlRpcClient();
        client.setConfig(config);
        try {
            config.setServerURL(new URL(BREW_URL));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e); 
        }
        
    }
    
    /**
     * Collects builds artifacts according to the build id or nvr.
     * 
     * @param build brew build id, or nvr
     * @throws Exception any Exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void collectBrewBuilds(String build) throws Exception {
        if (build == null) {
            LOGGER.warn("Null build, ignore.");
            return;
        }
        
        // check builds first
        Object[] buildInfoParams = new Object[] {build, true};
        Map buildInfo = (Map)brewXmlRpcCall("getBuild", buildInfoParams);
        if ((buildInfo == null) || buildInfo.isEmpty()) {
            LOGGER.warn("Build: " + build + " is not found.");
            return;
        }
        String nvr = buildInfo.get("nvr").toString();
        String buildId = buildInfo.get("id").toString();
        
        String taskId = buildInfo.get("task_id").toString();
        Object[] taskInfoParams = new Object[] {taskId, true};
        Map taskInfo = (Map)brewXmlRpcCall("getTaskInfo", taskInfoParams);
        if ((taskInfo == null) || taskInfo.isEmpty()) {
            LOGGER.error("Task: " + taskId + " is not found after build: " + build + " has been found.");
            return;
        }
        
        String buildMethod = (String)taskInfo.get("method");
        if (buildMethod.contains("maven")) {
            // mead maven build
            String packageName = buildInfo.get("package_name").toString();
            String version = buildInfo.get("version").toString();
            Component comp = new Component();
            comp.setName(packageName);
            comp.setVersion(version);
            Object[] mavenBuildParams = new Object[] {build, true};
            Map mavenBuildInfo = (Map)brewXmlRpcCall("getMavenBuild", mavenBuildParams);
            if (mavenBuildInfo == null || mavenBuildInfo.isEmpty()) {
                LOGGER.error("Maven build of: " + build + " is not found after it has been identified.");
                return;
            }
            String groupId = mavenBuildInfo.get("group_id").toString();
            comp.setGroupId(groupId);
            this.dataService.saveComponent(comp);
            
            // maven artifacts:  listArchives
            Object[] listArchivesParams = new Object[] {build};
            Object[] mavenArchives = (Object[]) brewXmlRpcCall("listArchives", listArchivesParams);
            if (mavenArchives == null || mavenArchives.length == 0) {
                LOGGER.warn("No Maven Artifacts output of build: " + build);
                return;
            }
            for (Object mavenArchive: mavenArchives) {
                Map mavenArchiveMap = (Map)mavenArchive;
                String typeName = mavenArchiveMap.get("type_name").toString();
                if (typeName.equals("jar") || typeName.equals("pom")) {
                    // only collect jar and poms
                    String id = mavenArchiveMap.get("id").toString();
                    String checksum = mavenArchiveMap.get("checksum").toString();
                    // getMavenArchive
                    Object[] mavenArchiveInfoParams = new Object[] {id, true};
                    Map mavenArchiveInfo = (Map) brewXmlRpcCall("getMavenArchive", mavenArchiveInfoParams);
                    if (mavenArchiveInfo == null || mavenArchiveInfo.isEmpty()) {
                        LOGGER.warn("No Maven Artifact Infomation found: " + id);
                        break;
                    }
                    String artiGrpId = mavenArchiveInfo.get("group_id").toString();
                    String artiId = mavenArchiveInfo.get("artifact_id").toString();
                    Artifact arti = new Artifact();
                    arti.setArtifactId(artiId);
                    String buildInfoLink = "[" + nvr + "](https://brewweb.devel.redhat.com/buildinfo?buildID=" + buildId + ")";
                    arti.setBuildInfo(buildInfoLink);
                    arti.setChecksum(checksum);
                    arti.setComponent(comp);
                    arti.setGroupId(artiGrpId);
                    arti.setType(typeName);
                    arti.setVersion(version);
                    this.dataService.addArtifact(arti);
                }
            }
        } else if (buildMethod.contains("build")) {
            // buildArch, native components, no artifacts collected.
            collectsNativeComponents(buildInfo);
            return;
        } else {
            LOGGER.info("Build: " + build + " not interested.");
            return;
        }
    }
    
    private void collectsNativeComponents(Map<Object, Object> buildInfo) throws DataServiceException {
        assert buildInfo != null : "BuildInfo can't be null";
        String packageName = buildInfo.get("package_name").toString();
        String version = buildInfo.get("version").toString();
        Component nativeComp = new Component();
        nativeComp.setName(packageName);
        nativeComp.setVersion(version);
        this.dataService.saveComponent(nativeComp);
    }

    private Object brewXmlRpcCall(String methodName, Object[] params) throws XmlRpcException {
        return client.execute(methodName, params);
    }
    
    

}
