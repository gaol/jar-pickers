/**
 * 
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @author lgao
 *
 * This represents a component in a specific product version.
 * 
 * A component can be a Jar artifact, or a package like: tomcat 7.0.35, etc.
 * 
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "version"})})
@XmlRootElement
public class Component implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 574882341062177555L;

	/**
	 * Version of the component
	 */
	@Column
	@NotNull(message = "Component version can't be empty.")
	private String version;
	
	/**
	 * scm url for this version of the component.
	 */
	@Column
	private String scm;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = Queries.SEQ_NAME)
	private Long id;
	
	@Column
	@NotNull(message = "Component Name can't be empty.")
	private String name;
	
	@Column
	private String description;
	
	@Column
	private String groupId;
	
	/** In case it is a native components, it might belong to some PVs **/
	@ManyToMany(mappedBy = "nativeComps", fetch = FetchType.LAZY)
	private List<ProductVersion> pvs;
	
	@OneToMany(mappedBy = "component", fetch = FetchType.EAGER)
	private List<Artifact> artis;

	/**
     * @return the artis
     */
    public List<Artifact> getArtis() {
        return artis;
    }

    /**
     * @param artis the artis to set
     */
    public void setArtis(List<Artifact> artis) {
        this.artis = artis;
    }

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
	 * @return the scm
	 */
	public String getScm() {
		return scm;
	}

	/**
	 * @param scm the scm to set
	 */
	public void setScm(String scm) {
		this.scm = scm;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Component [version=" + version + ", name=" + name + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Component other = (Component) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

}
