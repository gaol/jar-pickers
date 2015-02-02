/**
 * 
 */
package org.jboss.eap.trackers.data.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author lgao
 *
 */
@XmlRootElement
public class CVEReport implements Serializable
{

   public static CVEReport EMPTY = new CVEReport();
   
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   private Date latestModified;
   
   private List<CVEReportSection> sections = new ArrayList<CVEReport.CVEReportSection>();

   /**
    * @return the latestModified
    */
   @JsonSerialize(using=JsonDateSerializer.class)
   public Date getLatestModified()
   {
      return latestModified;
   }

   /**
    * @param latestModified the latestModified to set
    */
   public void setLatestModified(Date latestModified)
   {
      this.latestModified = latestModified;
   }


   /**
    * @return the sections
    */
   @XmlElement(name = "reports")
   public List<CVEReportSection> getSections()
   {
      return sections;
   }


   /**
    * @param sections the sections to set
    */
   public void setSections(List<CVEReportSection> sections)
   {
      this.sections = sections;
   }


   @XmlRootElement(name = "section")
   public static class CVEReportSection implements Serializable
   {

      /**
       * 
       */
      private static final long serialVersionUID = 1L;
      
      private String cve;
      
      private List<CVEReportElement> elements = new ArrayList<CVEReport.CVEReportElement>();
      
      /**
       * @return the cve
       */
      public String getCve()
      {
         return cve;
      }
      
      /**
       * @param cve the cve to set
       */
      public void setCve(String cve)
      {
         this.cve = cve;
      }

      /**
       * @return the elements
       */
      public List<CVEReportElement> getElements()
      {
         return elements;
      }

      /**
       * @param elements the elements to set
       */
      public void setElements(List<CVEReportElement> elements)
      {
         this.elements = elements;
      }
      
   }
   
   public static class CVEReportElement implements Serializable
   {

      /**
       * 
       */
      private static final long serialVersionUID = 1L;
      
      private String name;
      
      private String version;
      
      private String component;
      
      private String bugzilla;
      
      private String errata;
      
      private String build;
      
      private String note;


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
}
