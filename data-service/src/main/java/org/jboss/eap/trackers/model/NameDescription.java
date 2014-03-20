/**
 * 
 */
package org.jboss.eap.trackers.model;

import java.io.Serializable;


/**
 * @author lgao
 *
 *  This is an abstract class which has common instance variables: name and description
 */
public abstract class NameDescription implements Serializable  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4471205061515353147L;

	private String name;
	
	private String description;
	
	public NameDescription()
	{
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "NameDescription [name=" + name + ", description=" + description
				+ "]";
	}
	
}
