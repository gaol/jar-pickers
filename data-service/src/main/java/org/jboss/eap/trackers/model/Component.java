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
	
}
