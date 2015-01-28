/**
 * 
 */
package org.jboss.eap.trackers.data;

import java.util.regex.Pattern;

/**
 * @author lgao
 *
 */
public interface Constants
{
   /** split char **/
   String SPLITTER = ":";
   
   String JAVA_ARTI_PREFIX = "java";
   
   String NATIVE_ARTI_PREFIX = "native";
   
   Pattern JAVA_ARTI_PATTERN = Pattern.compile("java:([^\n|^:]+):([^\n|^:]+)");
   
   Pattern NATIVE_ARTI_PATTERN = Pattern.compile("native:([^\n|^:]+)");
}
