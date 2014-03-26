/**
 * 
 */
package org.jboss.eap.trackers.data.file;

import java.io.Serializable;
import java.util.List;

import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.NameDescription;

/**
 * @author lgao
 *
 */
public class ProductVersionComponents extends NameDescription implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6498217571724769382L;
	
	private String version;
	
	private List<Component> components;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<Component> getComponents() {
		return components;
	}

	public void setComponents(List<Component> components) {
		this.components = components;
	}
	
}
