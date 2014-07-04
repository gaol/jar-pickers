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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 * @author lgao
 *
 * This represents a Product.
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class Product implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 574882341062177555L;

	public Product()
	{
		super();
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = Queries.SEQ_NAME)
	private Long id;
	
	/**
	 * Full name of the product, like: JBoss Enterprise Application Platform
	 */
	@Column
	private String fullName;
	
	@Column
	@NotNull(message = "Product Name can't be empty.")
	private String name;
	
	@Column
	private String description;
	

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
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}


	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	/**
	 * Product Versions of this product.
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "product", cascade = {CascadeType.ALL})
	private List<ProductVersion> versions;
	
	public List<ProductVersion> getVersions() {
		return versions;
	}

	public void setVersions(List<ProductVersion> productVersions) {
		this.versions = productVersions;
		if (this.versions != null)
		{
			for (ProductVersion pv: this.versions)
			{
				pv.setProduct(this);
			}
		}
	}

	@Override
	public String toString() {
		return "Product [fullName=" + fullName + ", name="
				+ getName() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fullName == null) ? 0 : fullName.hashCode());
		result = prime * result
				+ ((getName() == null) ? 0 : getName().hashCode());
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
		Product other = (Product) obj;
		if (fullName == null) {
			if (other.fullName != null)
				return false;
		} else if (!fullName.equals(other.fullName))
			return false;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

}
