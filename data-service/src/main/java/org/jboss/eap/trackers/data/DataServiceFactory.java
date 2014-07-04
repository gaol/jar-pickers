/**
 * 
 */
package org.jboss.eap.trackers.data;

import org.jboss.eap.trackers.data.db.DBDataService;

/**
 * @author lgao
 *
 * The factory to create DataService
 * 
 */
public final class DataServiceFactory {
	
	private DataServiceFactory() {}
	
	/**
	 * Creates Database based DataService instance.
	 * 
	 */
	public static DataService createDBDataService()
	{
		return new DBDataService();
	}

}
