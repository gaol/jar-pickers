/**
 * 
 */
package org.jboss.eap.trackers.data;

import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;
import org.jboss.eap.trackers.pojo2js.POJO2JS;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author lgao
 *
 */
public class Pojo2JSTest {
	
	@Test
	public void testPOJO2JS() {
		POJO2JS pojo2js = new POJO2JS();
		
		pojo2js.addClass(Product.class);
		pojo2js.addClass(ProductVersion.class);
		pojo2js.addClass(Component.class);
		pojo2js.addClass(Artifact.class);
		
		String js = pojo2js.toJS();
		
		String expected = "function Product(){}Product.prototype={id:'',fullName:'',name:'',description:'',versions:[]}\n" +
				"function ProductVersion(){}ProductVersion.prototype={product:'',parent:'',nativeComps:[],version:'',id:'',note:'',artifacts:[]}\n" +
				"function Component(){}Component.prototype={version:'',scm:'',id:'',name:'',description:'',groupId:'',pvs:[],artis:[]}\n" +
				"function Artifact(){}Artifact.prototype={buildInfo:'',version:'',groupId:'',checksum:'',artifactId:'',type:'',component:'',id:'',note:'',pvs:[]}\n";
		
		Assert.assertEquals(expected, js);
	}

}
