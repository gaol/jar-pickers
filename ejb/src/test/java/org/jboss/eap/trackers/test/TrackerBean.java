/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.test;

import java.util.concurrent.Callable;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * 
 * @author lgao
 *
 *	Test Bean which represents a 'tracker' role.
 */
@Stateless
@RunAs("tracker")
@PermitAll
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class TrackerBean {
	
	public void call(Callable<?> callable) throws Exception {
		callable.call();
	}
}
