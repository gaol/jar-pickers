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

package org.jboss.eap.trackers.model;

import java.io.Serializable;
import java.util.regex.Matcher;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.eap.trackers.data.Constants;
import org.jboss.eap.trackers.data.VersionScopes;

/**
 * 
 * The association of which artifact has which CVEs
 * 
 * @author lgao
 *
 */
@Entity
@XmlRootElement
public class ArtifactCVEs implements Serializable, Constants {
    
    /**
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = Queries.SEQ_NAME)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    private CVE cve;
    
    /*
     * version scopes on the affected artifacts.
     * 
     */
    @Column(length = 512)
    private String versions;
    
    @Column(length = 512)
    private String fixedVersions;
    
    /**
     * name can be an Maven groupId:artifactId or a component name.
     * 
     * <p>
     * Format:
     *   <li>java:org.jgroup:jgroup</li>
     *   <li>native:openssl</li>
     */
    @Column(name = "identifier")
    private String identifier;
    
    /**
     * Brew builds information about the CVE fixing.
     */
    @Column(length = 512)
    private String brewBuilds;
    
    /**
     * Bugzilla references about the affection.
     */
    @Column(length = 512)
    private String bugzillas;
    
    /**
     * Erratas to which the CVE will be released
     */
    @Column(length = 512)
    private String erratas;
    
    /**
     * Status of CVE
     */
    @Enumerated(EnumType.STRING)
    @Column
    private CVEStatus status;

   /**
    * @return the id
    */
   public Long getId()
   {
      return id;
   }

   /**
    * @param id the id to set
    */
   public void setId(Long id)
   {
      this.id = id;
   }

   /**
    * @return the cve
    */
   public CVE getCve()
   {
      return cve;
   }

   /**
    * @param cve the cve to set
    */
   public void setCve(CVE cve)
   {
      this.cve = cve;
   }

   /**
    * @return the versions
    */
   public String getVersions()
   {
      return versions;
   }
   
   public VersionScopes getVersionScopes() {
      if (this.versions == null || this.versions.length() == 0) {
         return null;
      }
      return new VersionScopes(this.versions);
   }
   
   public VersionScopes getFixedVersionScopes() {
      if (this.fixedVersions == null || this.fixedVersions.length() == 0) {
         return null;
      }
      return new VersionScopes(this.fixedVersions);
   }

   /**
    * @param versions the versions to set
    */
   public void setVersions(String versions)
   {
      this.versions = versions;
   }

   /**
    * @return the fixedVersions
    */
   public String getFixedVersions()
   {
      return fixedVersions;
   }

   /**
    * @param fixedVersions the fixedVersions to set
    */
   public void setFixedVersions(String fixedVersions)
   {
      this.fixedVersions = fixedVersions;
   }

   /**
    * @return the identifier
    */
   public String getIdentifier()
   {
      return identifier;
   }
   
   public boolean isJavaArtifact() {
      if (null == identifier) return false;
      return JAVA_ARTI_PATTERN.matcher(this.identifier).matches();
   }
   
   public String getJavaGroupId() {
      if (null == identifier) {
         return null;
      }
      Matcher matcher = JAVA_ARTI_PATTERN.matcher(this.identifier);
      if (matcher.matches()) {
         return matcher.group(1);
      }
      return null;
   }
   
   public String getJavaArtifactId() {
      if (null == identifier) {
         return null;
      }
      Matcher matcher = JAVA_ARTI_PATTERN.matcher(this.identifier);
      if (matcher.matches()) {
         return matcher.group(2);
      }
      return null;
   }
   
   public String getNativeName() {
      if (null == identifier) {
         return null;
      }
      Matcher matcher = NATIVE_ARTI_PATTERN.matcher(this.identifier);
      if (matcher.matches()) {
         return matcher.group(1);
      }
      return null;
   }

   /**
    * @param identifier the identifier to set
    */
   public void setIdentifier(String identifier)
   {
      this.identifier = identifier;
   }

   /**
    * @return the brewBuilds
    */
   public String getBrewBuilds()
   {
      return brewBuilds;
   }

   /**
    * @param brewBuilds the brewBuilds to set
    */
   public void setBrewBuilds(String brewBuilds)
   {
      this.brewBuilds = brewBuilds;
   }

   /**
    * @return the bugzillas
    */
   public String getBugzillas()
   {
      return bugzillas;
   }

   /**
    * @param bugzillas the bugzillas to set
    */
   public void setBugzillas(String bugzillas)
   {
      this.bugzillas = bugzillas;
   }

   /**
    * @return the erratas
    */
   public String getErratas()
   {
      return erratas;
   }

   /**
    * @param erratas the erratas to set
    */
   public void setErratas(String erratas)
   {
      this.erratas = erratas;
   }

   /**
    * @return the status
    */
   public CVEStatus getStatus()
   {
      return status;
   }

   /**
    * @param status the status to set
    */
   public void setStatus(CVEStatus status)
   {
      this.status = status;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((cve == null) ? 0 : cve.hashCode());
      result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
      return result;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ArtifactCVEs other = (ArtifactCVEs) obj;
      if (cve == null)
      {
         if (other.cve != null)
            return false;
      }
      else if (!cve.equals(other.cve))
         return false;
      if (identifier == null)
      {
         if (other.identifier != null)
            return false;
      }
      else if (!identifier.equals(other.identifier))
         return false;
      return true;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "ArtifactCVEs [cve=" + cve + ", versions=" + versions + ", identifier=" + identifier + ", status=" + status + "]";
   }
   
}
