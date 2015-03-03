package org.jboss.eap.trackers.data.db;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CVEInfo {
	public String cve_name;
	public String product_name;
	public String product_version;
	public String bugzilla;
	public String bugzilla_status;
	public String errata;
	public String build_nvr;
	public String fixed_in_version;
	public String note;

	public String getCve_name() {
		return cve_name;
	}
	public void setCve_name(String cve_name) {
		this.cve_name = cve_name;
	}
	public String getProduct_name() {
		return product_name;
	}
	public void setProduct_name(String product_name) {
		this.product_name = product_name;
	}
	public String getProduct_version() {
		return product_version;
	}
	public void setProduct_version(String product_version) {
		this.product_version = product_version;
	}
	public String getBugzilla() {
		return bugzilla;
	}
	public void setBugzilla(String bugzilla) {
		this.bugzilla = bugzilla;
	}
	public String getBugzilla_status() {
		return bugzilla_status;
	}
	public void setBugzilla_status(String bugzilla_status) {
		this.bugzilla_status = bugzilla_status;
	}
	public String getErrata() {
		return errata;
	}
	public void setErrata(String errata) {
		this.errata = errata;
	}
	public String getBuild_nvr() {
        return build_nvr;
    }
    public void setBuild_nvr(String build_nvr) {
        this.build_nvr = build_nvr;
    }
    public String getFixed_in_version() {
        return fixed_in_version;
    }
    public void setFixed_in_version(String fixed_in_version) {
        this.fixed_in_version = fixed_in_version;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
}
