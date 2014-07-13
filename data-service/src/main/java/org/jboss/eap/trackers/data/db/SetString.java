/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data.db;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ext.Provider;

/**
 * @author lgao
 *
 */
@Provider
public class SetString implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final static String SEP = ",";
	
	private final String str;
	public SetString(String str) {
		this.str = str;
	}
	
	public Set<String> asSet() {
		if (str == null) {
		      return Collections.emptySet();
		    }
		Set<String> set = new HashSet<String>();
		String[] strArray = str.split(SEP);
		for (String s: strArray) {
			set.add(s);
		}
		return set;
	}

	@Override
	public String toString() {
		return "SetString" + this.str;
	}

}
