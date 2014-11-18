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
     * This can collects maven|build|wrapperRPM tasks.
     * 
     * @param build brew build id, or nvr
     * @throws Exception any Exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void collectBrewBuild(final String build) throws Exception {
        if (build == null) {
            LOGGER.warn("Null build, ignore.");
            return;
        }
        
        // check builds first
        Map buildInfo = getBuildInfo(build);
        if ((buildInfo == null) || buildInfo.isEmpty()) {
            LOGGER.warn("Build: " + build + " is not found.");
            return;
        }
        
        String buildId = buildInfo.get("id").toString();
        String taskId = buildInfo.get("task_id").toString();
        
        Map taskInfo = getTaskInfo(taskId);
        if ((taskInfo == null) || taskInfo.isEmpty()) {
            throw new RuntimeException("Task: " + taskId + " is not found after build: " + build + " has been found.");
        }
        
        String buildMethod = (String)taskInfo.get("method");
        if (buildMethod.contains("maven")) {
            collectsMaven(buildInfo);
            return;
        } else if (buildMethod.contains("build")) {
            // buildArch, native components, no artifacts collected.
            collectsbuildArch(buildInfo);
            return;
        } else if (buildMethod.contains("wrapperRPM")) {
            // wrapperRPM, wrapper on top of mead build
            collectsWrapperRPM(buildInfo, taskInfo);
            return;
        } else {
            LOGGER.info("Build: " + buildId + " [" + buildMethod + "] not interested.");
            return;
        }
    }
    
    @SuppressWarnings({ "rawtypes" })
    private void collectsMaven(Map<Object, Object> buildInfo) throws DataServiceException, XmlRpcException {
        String nvr = buildInfo.get("nvr").toString();
        String build = buildInfo.get("id").toString();
        
        // mead maven build
        String version = buildInfo.get("version").toString();
        Component comp = new Component();
        comp.setVersion(version);
        
        Map mavenBuildInfo = getMavenBuildInfo(build);
        if (mavenBuildInfo == null || mavenBuildInfo.isEmpty()) {
            throw new RuntimeException("Maven build of: " + build + " is not found after it has been identified.");
        }
        String groupId = mavenBuildInfo.get("group_id").toString();
        comp.setGroupId(groupId);
        
        String compName = mavenBuildInfo.get("artifact_id").toString();
        comp.setName(compName);
        this.dataService.saveComponent(comp);
        
        collectAllMavenJarArtifacts(buildInfo, nvr, comp);
    }
    
    @SuppressWarnings("rawtypes")
    private void collectsWrapperRPM(Map<Object, Object> buildInfo, Map<Object, Object> taskInfo) throws DataServiceException, XmlRpcException { 
        String packageName = buildInfo.get("package_name").toString(); // this is the real dist-git package name.
        String version = buildInfo.get("version").toString();
        String taskId = buildInfo.get("task_id").toString();
        String build = buildInfo.get("id").toString();
        
        Component comp = new Component();
        comp.setName(packageName);
        comp.setVersion(version);
        
        // getTaskRequest
        Object[] taskRequestResult = getTaskRequest(taskId);
        if (taskRequestResult == null || taskRequestResult.length == 0) {
            throw new RuntimeException("No task request found: " + taskId);
        }
        for (Object r: taskRequestResult) {
            if (r instanceof Map) {
                Object meadBuildObj = ((Map)r).get("nvr");
                if (meadBuildObj != null) { // is the map which has the mead task id information
                    String meadBuildNVR = meadBuildObj.toString();
                    Map meadBuildInfo = getBuildInfo(meadBuildNVR);
                    if ((meadBuildInfo == null) || meadBuildInfo.isEmpty()) {
                        throw new RuntimeException("Mead Build: " + meadBuildNVR + " is not found after wrapperRPM build: " + build + " has been found.");
                    }
                    String meadTaskId = meadBuildInfo.get("task_id").toString();
                    Map meadTaskInfo = getTaskInfo(meadTaskId);
                    if ("maven".equals(meadTaskInfo.get("method"))) {
                        // OK, it is really a maven build, instead of another wrapperRPM build
                        Map meadMavenBuildInfo = getMavenBuildInfo(meadBuildNVR);
                        String groupId = meadMavenBuildInfo.get("group_id").toString();
                        comp.setGroupId(groupId);
                        this.dataService.saveComponent(comp);
                        
                        // maven artifacts collection.
                        collectAllMavenJarArtifacts(meadBuildInfo, meadBuildNVR, comp);
                    }
                    break;
                }
            }
        }
        
    }

    @SuppressWarnings("rawtypes")
    private void collectAllMavenJarArtifacts(Map meadBuildInfo, String meadBuildNVR, Component comp) throws XmlRpcException, DataServiceException {
        // maven artifacts:  listArchives
        Object[] mavenArchives = listMavenBuildArchives(meadBuildNVR);
        if (mavenArchives == null || mavenArchives.length == 0) {
            LOGGER.warn("No Maven Artifacts output of build: " + meadBuildNVR);
            return;
        }
        for (Object mavenArchive: mavenArchives) {
            Map mavenArchiveMap = (Map)mavenArchive;
            String typeName = mavenArchiveMap.get("type_name").toString();
            String fileName = mavenArchiveMap.get("filename").toString().trim().toLowerCase();
            if (typeName.equals("jar") && !fileName.endsWith("-sources.jar") && !fileName.endsWith("-javadoc.jar")) {  // no sources nor javadoc jars
                // only collect jar
                String id = mavenArchiveMap.get("id").toString();
                String checksum = mavenArchiveMap.get("checksum").toString();
                
                // getMavenArchive
                Map mavenArchiveInfo = getMavenArchives(id);
                if (mavenArchiveInfo == null || mavenArchiveInfo.isEmpty()) {
                    LOGGER.warn("No Maven Artifact Infomation found: " + id);
                    break;
                }
                String artiGrpId = mavenArchiveInfo.get("group_id").toString();
                String artiId = mavenArchiveInfo.get("artifact_id").toString();
                String version = mavenArchiveInfo.get("version").toString();
                
                Artifact arti = new Artifact();
                arti.setArtifactId(artiId);
                String buildInfoLink = "[" + meadBuildNVR + "](https://brewweb.devel.redhat.com/buildinfo?buildID=" + meadBuildInfo.get("id") + ")";
                arti.setBuildInfo(buildInfoLink);
                arti.setChecksum(checksum);
                arti.setComponent(comp);
                arti.setGroupId(artiGrpId);
                arti.setType(typeName);
                arti.setVersion(version);
                this.dataService.addArtifact(arti);
            }
        }
    }

    private void collectsbuildArch(Map<Object, Object> buildInfo) throws DataServiceException {
        assert buildInfo != null : "BuildInfo can't be null";
        String packageName = buildInfo.get("package_name").toString();
        String version = buildInfo.get("version").toString();
        Component nativeComp = new Component();
        nativeComp.setName(packageName);
        nativeComp.setVersion(version);
        nativeComp.setNative(true);
        this.dataService.saveComponent(nativeComp);
    }

    @SuppressWarnings("rawtypes")
    private Map getTaskInfo(String taskId) throws XmlRpcException {
        Object[] taskInfoParams = new Object[] {taskId, true};
        Map taskInfo = (Map)brewXmlRpcCall("getTaskInfo", taskInfoParams);
        return taskInfo;
    }
    
    @SuppressWarnings("rawtypes")
    private Map getMavenArchives(String archiveId) throws XmlRpcException {
        Object[] archiveParams = new Object[] {archiveId, true};
        Map mavenArchives = (Map)brewXmlRpcCall("getMavenArchive", archiveParams);
        return mavenArchives;
    }
    
    private Object[] getTaskRequest(String taskId) throws XmlRpcException {
        Object[] taskInfoParams = new Object[] {taskId};
        return (Object[])brewXmlRpcCall("getTaskRequest", taskInfoParams);
    }
    
    private Object[] listMavenBuildArchives(String meadBuildId) throws XmlRpcException {
        Object[] meadBuildParams = new Object[] {meadBuildId};
        return (Object[])brewXmlRpcCall("listArchives", meadBuildParams);
    }
    
    @SuppressWarnings("rawtypes")
    private Map getBuildInfo(String buildId) throws XmlRpcException {
        Object[] buildInfoParams = new Object[] {buildId, true};
        Map buildInfo = (Map)brewXmlRpcCall("getBuild", buildInfoParams);
        return buildInfo;
    }
    
    @SuppressWarnings("rawtypes")
    private Map getMavenBuildInfo(String buildId) throws XmlRpcException {
        Object[] buildInfoParams = new Object[] {buildId, true};
        Map buildInfo = (Map)brewXmlRpcCall("getMavenBuild", buildInfoParams);
        return buildInfo;
    }
    
    private Object brewXmlRpcCall(String methodName, Object[] params) throws XmlRpcException {
        return client.execute(methodName, params);
    }

}
