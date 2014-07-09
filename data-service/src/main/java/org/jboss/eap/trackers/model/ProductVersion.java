/**
 * 
 */
package org.jboss.eap.trackers.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
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

/**
 * @author lgao
 *
 * This represents a product version
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "version"})})
@XmlRootElement
public class ProductVersion implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 574882341062177555L;

	public ProductVersion()
	{
		super();
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	private Product product;
	
	/**
	 * Version can be arbitrary string.
	 * 
	 * It also contains the milestone string here.
	 * like: 6.2.1.ER2
	 */
	@Column
	@NotNull(message = "Product version can't be empty.")
	private String version;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = Queries.SEQ_NAME)
	private Long id;
	
	@Column
	private String note;
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private List<Artifact> artifacts;

	/**
	 * @return the artifacts
	 */
	@XmlElementRef
	public List<Artifact> getArtifacts() {
		return artifacts;
	}

	/**
	 * @param artifacts the artifacts to set
	 */
	public void setArtifacts(List<Artifact> artifacts) {
		this.artifacts = artifacts;
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

	@XmlTransient
//	@jsonig
	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "ProductVersion [product=" + product + ", version=" + version
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((product == null) ? 0 : product.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductVersion other = (ProductVersion) obj;
		if (product == null) {
			if (other.product != null)
				return false;
		} else if (!product.equals(other.product))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	
}
