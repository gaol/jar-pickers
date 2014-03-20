/**
 * 
 */
package org.jboss.eap.trackers.data;

/**
 * @author lgao
 *
 */
public class DataServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3802697500410840909L;

	public DataServiceException()
	{
		super();
	}
	
	public DataServiceException(String msg)
	{
		super(msg);
	}
	
	public DataServiceException(Throwable cause)
	{
		super(cause);
	}
	
	public DataServiceException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
	
}
