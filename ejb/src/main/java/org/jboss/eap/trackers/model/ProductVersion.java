/**
 * 
 */
package org.jboss.eap.trackers.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author lgao
 *
 * This represents a product version
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "version"})})
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
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
	
	/** This is the based product version, for example, the layered products based on EAP
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	private ProductVersion parent;
	
	/**
	 * Components here is convenient way to collect, but it is not reliable, except for the native components.
	 */
	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private List<Component> comps;
	
	/**
	 * Version can be arbitrary string.
	 * 
	 * It also contains the milestone string here.
	 * like: 6.2.1.ER2
	 */
	@Column
	@NotNull(message = "Product version can't be empty.")
	private String version;
	
	/**
	 * If it is one-off release, the parent MUST NOT be null.
	 * in this case, version is normally the bugzilla id or erratum or CVE number.
	 */
	@Column(columnDefinition = "boolean DEFAULT false")
    private boolean isOneOff;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = Queries.SEQ_NAME)
	private Long id;
	
	@Column
	private String note;
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private List<Artifact> artifacts;
	
	@Transient
	private transient String name;
	
   /**
    * @param name the prdName to set
    */
   public void setName(String name)
   {
      this.name = name;
   }
   
   @XmlElement(name = "name")
   @JsonProperty("name")
   public String getName() {
       return this.name;
   }

   /**
     * @return the isOneOff
     */
	@XmlAttribute
    public boolean isOneOff() {
        return isOneOff;
    }

    /**
     * @param isOneOff the isOneOff to set
     */
    public void setOneOff(boolean isOneOff) {
        this.isOneOff = isOneOff;
    }

    /**
	 * @return the parent
	 */
	@XmlTransient
	@JsonIgnore
	public ProductVersion getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(ProductVersion parent) {
		this.parent = parent;
	}
	
	@XmlElement(name = "parent")
	@JsonProperty("parent")
	public String getParentPV() {
	    if (this.parent == null) {
	        return "";
	    }
	    return parent.getName() + ":" + parent.getVersion();
	}

	/**
	 * @return the nativeComps
	 */
	@XmlTransient
    @JsonIgnore
	public List<Component> getNativeComps() {
	    if (this.comps == null || this.comps.isEmpty()) {
	        return Collections.emptyList();
	    }
	    List<Component> nativeComps = new ArrayList<Component>();
	    for (Component c: this.comps) {
	        if (c.isNative()) {
	            nativeComps.add(c);
	        }
	    }
		return nativeComps;
	}
	
	/**
     * @return the comps
     */
	@XmlTransient
    @JsonIgnore
    public List<Component> getComps() {
        return comps;
    }

    /**
     * @param comps the comps to set
     */
    public void setComps(List<Component> comps) {
        this.comps = comps;
    }

    /**
	 * @return the artifacts
	 */
	@XmlTransient
	@JsonIgnore
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
	@JsonIgnore
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
