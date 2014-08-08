/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data.db;

import java.io.InputStream;

import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.DataServiceException;

/**
 * @author lgao
 *
 */
public interface DataServiceLocal extends DataService {

	/**
	 * Imports artifacts from an Inputstream
	 */
	void importArtifactsFromInput(InputStream input) throws DataServiceException;
	
}
