/**
 * 
 */
package org.jboss.eap.trackers.model;

import java.util.List;

/**
 * @author lgao
 *
 * This represents a Product.
 */
public class Product extends NameDescription {

	/**
	 * 
	 */
	private static final long serialVersionUID = 574882341062177555L;

	public Product()
	{
		super();
	}
	
	/**
	 * Full name of the product, like: JBoss Enterprise Application Platform
	 */
	private String fullName;
	
	/**
	 * Product Versions of this product.
	 */
	private List<ProductVersion> versions;

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public String getShortName()
	{
		return super.getName();
	}
	
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
		return "Product [fullName=" + fullName + ", shortName="
				+ getShortName() + "]";
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
