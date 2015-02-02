/**
 * 
 */
package org.jboss.eap.trackers.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jboss.eap.trackers.data.Constants;

/**
 * 
 * The association of which product version has which CVE
 * 
 * @author lgao
 *
 */
@Entity
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"cve_name", "name", "version", "component", "bugzilla"})})
public class ProductCVE implements Serializable, Constants
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   
   @ManyToOne(fetch = FetchType.EAGER)
   private CVE cve;
   
   private String name;
   
   private String version;
   
   private String component;
   
   private String bugzilla; // bug id, like: 1182872, etc
   
   private String errata; // errata id, like: 
   
   private String build;
   
   private String note;
   
   /**
    * @return the name
    */
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
    * @return the version
    */
   public String getVersion()
   {
      return version;
   }

   /**
    * @param version the version to set
    */
   public void setVersion(String version)
   {
      this.version = version;
   }

   /**
    * @return the component
    */
   public String getComponent()
   {
      return component;
   }

   /**
    * @param component the component to set
    */
   public void setComponent(String component)
   {
      this.component = component;
   }

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
    * @return the bugzilla
    */
   public String getBugzilla()
   {
      return bugzilla;
   }

   /**
    * @param bugzilla the bugzilla to set
    */
   public void setBugzilla(String bugzilla)
   {
      this.bugzilla = bugzilla;
   }

   /**
    * @return the errata
    */
   public String getErrata()
   {
      return errata;
   }

   /**
    * @param errata the errata to set
    */
   public void setErrata(String errata)
   {
      this.errata = errata;
   }

   /**
    * @return the build
    */
   public String getBuild()
   {
      return build;
   }

   /**
    * @param build the build to set
    */
   public void setBuild(String build)
   {
      this.build = build;
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
   

}