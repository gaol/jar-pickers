/**
 * 
 */
package org.jboss.eap.trackers.data.db;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author lgao
 *
 */
public class Resources {

	@Produces
	@PersistenceContext
	private EntityManager em;
	
}
