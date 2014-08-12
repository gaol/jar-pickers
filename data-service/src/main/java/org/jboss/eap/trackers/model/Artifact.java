/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @author lgao
 * 
 * Artifact is smallest unit of a product
 *
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"groupId", "artifactId", "version"})})
@XmlRootElement
public class Artifact implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -342959117290260121L;
	
	/**
	 * Build information about where to produce this component.
	 */
	@Column
	private String buildInfo;

	/**
	 * Version of the component
	 */
	@Column
	@NotNull(message = "Artifact version can't be empty.")
	private String version;
	
	/**
	 * groupId of the jar
	 */
	@Column
	@NotNull(message = "GroupId can't be empty.")
	private String groupId;
	
	@Column
	private String checksum;
	
	/**
	 * artifactId of the jar
	 */
	@Column
	@NotNull(message = "ArtifactId can't be empty.")
	private String artifactId;
	
	@Column(columnDefinition = " varchar(50) DEFAULT 'jar'")
	private String type;
	
	/**
	 * An Artifact belongs to a Component
	 */
	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	private Component component;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = Queries.SEQ_NAME)
	private Long id;
	
	@Column
	private String note;
	
	@ManyToMany(mappedBy = "artifacts", fetch = FetchType.LAZY)
	private List<ProductVersion> pvs;
	
	/**
	 * @return the pvs
	 */
	@XmlTransient
	@JsonIgnore
	public List<ProductVersion> getPvs() {
		return pvs;
	}

	/**
	 * @param pvs the pvs to set
	 */
	public void setPvs(List<ProductVersion> pvs) {
		this.pvs = pvs;
	}

	/**
	 * @return the checksum
	 */
	public String getChecksum() {
		return checksum;
	}

	/**
	 * @param checksum the checksum to set
	 */
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	
	/**
	 * @return the id
	 */
	@XmlAttribute
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * @return the buildInfo
	 */
	public String getBuildInfo() {
		return buildInfo;
	}

	/**
	 * @param buildInfo the buildInfo to set
	 */
	public void setBuildInfo(String buildInfo) {
		this.buildInfo = buildInfo;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the artifactId
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * @param artifactId the artifactId to set
	 */
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	/**
	 * @return the component
	 */
	@XmlElementRef
	public Component getComponent() {
		return component;
	}

	/**
	 * @param component the component to set
	 */
	public void setComponent(Component component) {
		this.component = component;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Artifact [version=" + version + ", groupId=" + groupId
				+ ", artifactId=" + artifactId + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Artifact other = (Artifact) obj;
		if (artifactId == null) {
			if (other.artifactId != null)
				return false;
		} else if (!artifactId.equals(other.artifactId))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

}
