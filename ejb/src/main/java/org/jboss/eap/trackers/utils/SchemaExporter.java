/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.utils;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.model.ArtifactCVEs;
import org.jboss.eap.trackers.model.CVE;
import org.jboss.eap.trackers.model.CVELastUpdated;
import org.jboss.eap.trackers.model.CVEStatus;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductCVE;
import org.jboss.eap.trackers.model.ProductVersion;

/**
 * @author lgao
 *
 */
public class SchemaExporter {

	public static void main(String[] args) throws Exception {
		org.hibernate.cfg.Configuration cfg = new Configuration();
        
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        cfg.addAnnotatedClass(Product.class);
        cfg.addAnnotatedClass(ProductVersion.class);
        cfg.addAnnotatedClass(Component.class);
        cfg.addAnnotatedClass(Artifact.class);
        cfg.addAnnotatedClass(CVE.class);
        cfg.addAnnotatedClass(ArtifactCVEs.class);
        cfg.addAnnotatedClass(CVEStatus.class);
        cfg.addAnnotatedClass(ProductCVE.class);
        cfg.addAnnotatedClass(CVELastUpdated.class); 
        cfg.addURL(Artifact.class.getClassLoader().getResource("META-INF/orm.xml"));
        
        SchemaExport exporter = new SchemaExport(cfg);
        exporter.setFormat(true);
        exporter.setDelimiter(";");
        exporter.setOutputFile(System.getProperty("trackers.sql.file", System.getProperty("user.dir") + "/trackers.sql"));
       
        exporter.create(true, false);

	}
}
