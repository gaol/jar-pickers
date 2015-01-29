/**
 * 
 */
package org.jboss.eap.trackers.data.versioning;

/**
 * @author lgao
 *
 */
class InvalidVersionSpecificationException extends Exception
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public InvalidVersionSpecificationException( String message )
   {
       super( message );
   }
}
