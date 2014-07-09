package org.jboss.eap.trackers.data.db;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A class extending {@link Application} and annotated with @ApplicationPath is the Java EE 6
 * "no XML" approach to activating JAX-RS.
 * 
 * <p>
 * Resources are served relative to the servlet path specified in the {@link ApplicationPath}
 * annotation.
 * </p>
 */
@ApplicationPath("/api")
public class JaxRsActivator extends Application {
   /* class body intentionally left blank */
	
	public JaxRsActivator() {
		super();
		ResteasyProviderFactory.getInstance().addStringConverter(SetStringConvert.class);
	}
}
