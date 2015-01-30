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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jboss.eap.trackers.data.Constants;
import org.jboss.eap.trackers.data.DataService;

/**
 * @author lgao
 *
 */
@Entity
@Table
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CVE implements Serializable, Comparable<CVE>, Constants
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * name like: CVE-2012-3356
    */
   @Id
   @Column(length = 50)
   private String name;

   @Column(length = 256)
   private String alias;

   @Column(length = 512)
   private String title;

   @Column(columnDefinition = "Boolean DEFAULT FALSE")
   private boolean embargoed;

   @Column(columnDefinition = "DATE")
   private Date embargoDate;

   @Column(length = 512)
   private String note;

   @Column(columnDefinition = "TEXT")
   private String description;

   @Column
   private String cvss;

   @OneToMany(mappedBy = "cve", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE})
   private Set<ArtifactCVEs> affectedArtis;

   /**
    * @return the cvss
    */
   public String getCvss()
   {
      return cvss;
   }

   /**
    * @param cvss the cvss to set
    */
   public void setCvss(String cvss)
   {
      this.cvss = cvss;
   }

   /**
    * @return the description
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * @param description the description to set
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * @return the alias
    */
   public String getAlias()
   {
      return alias;
   }

   /**
    * @param alias the alias to set
    */
   public void setAlias(String alias)
   {
      this.alias = alias;
   }

   /**
    * @return the title
    */
   public String getTitle()
   {
      return title;
   }

   /**
    * @param title the title to set
    */
   public void setTitle(String title)
   {
      this.title = title;
   }

   /**
     * @return the affectedArtis
     */
   public Set<ArtifactCVEs> getAffectedArtis()
   {
      return affectedArtis;
   }

   /**
    * @return the affectedArtis
    */
   public Set<ArtifactCVEs> getAffectedJavaArtis()
   {
      if (null == affectedArtis)
      {
         return null;
      }
      Set<ArtifactCVEs> javaArtis = new HashSet<ArtifactCVEs>();
      for (ArtifactCVEs artiCVEs: this.affectedArtis) {
         if (artiCVEs.getIdentifier().startsWith(JAVA_ARTI_PREFIX)) {
            javaArtis.add(artiCVEs);
         }
      }
      return javaArtis;
   }

   /**
    * @return the affectedArtis
    */
   public Set<ArtifactCVEs> getAffectedNativeComps()
   {
      if (null == affectedArtis)
      {
         return null;
      }
      Set<ArtifactCVEs> nativeArtis = new HashSet<ArtifactCVEs>();
      for (ArtifactCVEs artiCVEs: this.affectedArtis) {
         if (artiCVEs.getIdentifier().startsWith(NATIVE_ARTI_PREFIX)) {
            nativeArtis.add(artiCVEs);
         }
      }
      return nativeArtis;
   }

   /**
    * @param affectedArtis the affectedArtis to set
    */
   public void setAffectedArtis(Set<ArtifactCVEs> affectedArtis)
   {
      this.affectedArtis = affectedArtis;
   }

   /**
   * @return the embargoed
   */
   public boolean getEmbargoed()
   {
      return embargoed;
   }

   public String getEmbargoedHint()
   {
      return embargoed ? "Embargoed" : "Public";
   }

   /**
    * @return the name
    */
   @XmlAttribute
   public String getName()
   {
      return name;
   }

   /**
    * @param name the name to set
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * @return the note
    */
   public String getNote()
   {
      return note;
   }

   /**
    * @param note the note to set
    */
   public void setNote(String note)
   {
      this.note = note;
   }

   /**
    * @param embargoed the embargoed to set
    */
   public void setEmbargoed(boolean embargoed)
   {
      this.embargoed = embargoed;
   }

   /**
    * @return the embargoDate
    */
   public Date getEmbargoDate()
   {
      return embargoDate;
   }

   /**
    * @param embargoDate the embargoDate to set
    */
   public void setEmbargoDate(Date embargoDate)
   {
      this.embargoDate = embargoDate;
   }

   @Override
   public String toString()
   {
      return this.name;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
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
      CVE other = (CVE) obj;
      if (name == null)
      {
         if (other.name != null)
            return false;
      }
      else if (!name.equals(other.name))
         return false;
      return true;
   }

   @Override
   public int compareTo(CVE c)
   {
      if (c == null)
      {
         return 1;
      }
      // compare cve name only
      if (this.name != null && c.name == null)
      {
         return 1;
      }
      if (this.name == null && c.name != null)
      {
         return -1;
      }
      if (this.name != null && c.name != null)
      {
         Matcher m = DataService.CVE_NAME_PATTERN.matcher(this.name);
         if (m.matches())
         {
            int year = Integer.valueOf(m.group(1));
            int number = Integer.valueOf(m.group(2));
            m = DataService.CVE_NAME_PATTERN.matcher(c.name);
            if (m.matches())
            {
               int year2 = Integer.valueOf(m.group(1));
               int number2 = Integer.valueOf(m.group(2));
               if (year - year2 != 0)
               {
                  return year - year2;
               }
               return number - number2;
            }
         }
      }
      return 0;
   }

}