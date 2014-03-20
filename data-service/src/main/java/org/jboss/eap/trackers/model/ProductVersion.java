/**
 * 
 */
package org.jboss.eap.trackers.model;

/**
 * @author lgao
 *
 * This represents a product version
 */
public class ProductVersion extends Noteable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 574882341062177555L;

	public ProductVersion()
	{
		super();
	}
	
	private Product product;
	
	/**
	 * Version can be arbitrary string.
	 * 
	 * It also contains the milestone string here.
	 * like: 6.2.1.ER2
	 */
	private String version;

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
