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
import java.util.SortedSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.jboss.eap.trackers.data.VersionScopes;

/**
 * 
 * One CVE may affect multiple artifacts.
 * 
 * @author lgao
 *
 */
@Entity
@XmlRootElement
public class AffectedArtifact implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = Queries.SEQ_NAME)
    private Long id;
    
    /*
     * version scopes on the affected artifacts.
     * 
     */
    @Column(length = 512)
    @Type(type = "org.jboss.eap.trackers.data.db.VersionScopeUserType")
    private VersionScopes versionScopes;
    
    @Column
    private String artiGrpId;
    
    @Column
    private String artiId;
  
    @Sort(type = SortType.NATURAL)
    @ManyToMany(mappedBy = "affectedArtis", fetch = FetchType.EAGER)
    private SortedSet<CVE> cves;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the versionScopes
     */
    public VersionScopes getVersionScopes() {
        return versionScopes;
    }

    /**
     * @param versionScopes the versionScopes to set
     */
    public void setVersionScopes(VersionScopes versionScopes) {
        this.versionScopes = versionScopes;
    }

    /**
     * @return the artiGrpId
     */
    public String getArtiGrpId() {
        return artiGrpId;
    }

    /**
     * @param artiGrpId the artiGrpId to set
     */
    public void setArtiGrpId(String artiGrpId) {
        this.artiGrpId = artiGrpId;
    }

    /**
     * @return the artiId
     */
    public String getArtiId() {
        return artiId;
    }

    /**
     * @param artiId the artiId to set
     */
    public void setArtiId(String artiId) {
        this.artiId = artiId;
    }

    /**
     * @return the cves
     */
    public SortedSet<CVE> getCves() {
        return cves;
    }

    /**
     * @param cves the cves to set
     */
    public void setCves(SortedSet<CVE> cves) {
        this.cves = cves;
    }
    
}
