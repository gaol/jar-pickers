/**
 * 
 */
package org.jboss.eap.trackers.model;

/**
 * @author lgao
 *
 */
public enum CVEStatus {

   NEW("New"), // new, waiting fix from upstream, or waiting for patch
   NEED_BREW_BUILD("Need Brew Build"), // patch is ready, needs to be built
   BUILD_READY("Build Ready"), // built ready, waiting for merge in pv and release
   COMPLETED("Completed"); // included in a pv release
   
   
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
