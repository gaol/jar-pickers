/**
 * 
 */
package org.jboss.eap.trackers.data.file;

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
		
		String js = pojo2js.toJS();
		
		System.out.println(js);
		
		String expected = "function Product(){}Product.prototype={fullName:'',versions:'',name:'',description:'',}\n" +
				"function ProductVersion(){}ProductVersion.prototype={product:'',version:'',note:'',}\n" +
				"function Component(){}Component.prototype={buildInfo:'',version:'',groupId:'',scm:'',pkg:'',name:'',description:'',}\n";
		
		Assert.assertEquals(expected, js);
	}

}
