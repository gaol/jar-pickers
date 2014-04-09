/**
 * 
 */
package org.jboss.eap.trackers.model;

import java.io.Serializable;

/**
 * @author lgao
 *
 * This represents a component in a specific product version.
 * 
 * A component can be a Jar artifact, or a package like: tomcat 7.0.35, etc.
 * 
 */
public class Component extends NameDescription implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 574882341062177555L;

	public Component()
	{
		super();
	}
	
	/**
	 * Build information about where to produce this component.
	 */
	private String buildInfo;

	/**
	 * Version of the component
	 */
	private String version;
	
	/**
	 * groupId if this is a Jar artifact.
	 * (Optional)
	 */
	private String groupId;
	
	/**
	 * scm url for this version of the component.
	 */
	private String scm;
	
	/**
	 * Which package does this component belongs to.
	 * The concept 'package' here is the package in dist-git for brew system. 
	 */
	private String pkg;

	
	public String getScm() {
		return scm;
	}

	public void setScm(String scm) {
		this.scm = scm;
	}

	public String getBuildInfo() {
		return buildInfo;
	}

	public void setBuildInfo(String buildInfo) {
		this.buildInfo = buildInfo;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getPkg() {
		return pkg;
	}

	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

	@Override
	public String toString() {
		return "Component [name =" + getName() + ", version=" + version
				+ ", groupId=" + groupId + ", pkg=" + pkg + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Component other = (Component) obj;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	
}
