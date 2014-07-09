/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data.db;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.resteasy.spi.StringConverter;

/**
 * @author lgao
 *
 */
public class SetStringConvert implements StringConverter<Collection<String>>{

	private final static String SEP = ",";
	
	@Override
	public Collection<String> fromString(String string) {
		if (string == null) {
		      return Collections.emptySet();
		    }
		Set<String> set = new HashSet<String>();
		String[] strArray = string.split(SEP);
		for (String s: strArray) {
			set.add(s);
		}
		return set;
	}

	@Override
	public String toString(Collection<String> set) {
		if (set == null || set.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String s: set) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(s);
		}
		return sb.toString();
	}

}
