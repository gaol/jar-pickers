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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * One CVE may affect multiple artifacts.
 * 
 * @author lgao
 *
 */
@Entity
@XmlRootElement
public class CVEAffected implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
    private String cveName;

    /**
     * This will be the text representation of the affect of the CVE and it's status of the fix.
     */
    @Column(columnDefinition = "TEXT")
    private String cveAffect;

   /**
    * @return the cveName
    */
   public String getCveName()
   {
      return cveName;
   }

   /**
    * @param cveName the cveName to set
    */
   public void setCveName(String cveName)
   {
      this.cveName = cveName;
   }

   /**
    * @return the cveAffect
    */
   public String getCveAffect()
   {
      return cveAffect;
   }

   /**
    * @param cveAffect the cveAffect to set
    */
   public void setCveAffect(String cveAffect)
   {
      this.cveAffect = cveAffect;
   }
    
}
