/**
 * 
 */
package org.jboss.eap.trackers.data.db;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

/**
 * @author lgao
 *
 */
public class JsonDateSerializer extends JsonSerializer<Date>
{

   // 2015-02-02 09:54:12 -0500
   
   private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
   
   @Override
   public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
         JsonProcessingException
   {
      String formattedDate = dateFormat.format(value);
      jgen.writeString(formattedDate);
   }

}
