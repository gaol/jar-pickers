/**
 * 
 */
package org.jboss.eap.trackers.model;

import java.io.Serializable;


/**
 * @author lgao
 * 
 * The abstract class which can be noted.
 * 
 * Which means has an instance variable: note
 *
 */
public abstract class Noteable implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7553705542090972696L;
	
	private String note;
	
	public Noteable()
	{
		super();
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
	

}
