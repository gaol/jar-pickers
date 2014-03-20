/**
 * 
 */
package org.jboss.eap.trackers.data;

import org.jboss.eap.trackers.data.file.FileBasedDataService;

/**
 * @author lgao
 *
 * The factory to create DataService
 * 
 */
public final class DataServiceFactory {
	
	private DataServiceFactory() {}
	
	/**
	 * Creates DataService instance.
	 * 
	 */
	public static DataService createFileDataService()
	{
		return FileBasedDataService.instance();
	}

}
