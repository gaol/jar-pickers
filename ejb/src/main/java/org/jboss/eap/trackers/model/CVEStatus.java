/**
 * 
 */
package org.jboss.eap.trackers.model;

/**
 * @author lgao
 *
 */
public enum CVEStatus {

   NEW("New"),
   NEED_BREW_BUILD("Need Brew Build"),
   COMPLETED("Completed");
   
   
   private String name;
   
   CVEStatus(String name) {
      this.name = name;
   }
   
   @Override
   public String toString()
   {
      return this.name;
   }
   
}
