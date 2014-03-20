package org.jboss.eap.trackers.cdi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;


/**
 * This class uses CDI to alias Java EE resources, such as the persistence context, to CDI beans
 * 
 * <p>
 * Example injection on a managed bean field:
 * </p>
 * 
 * <pre>
 * &#064;Inject
 * private EntityManager em;
 * </pre>
 */
public class Resources {
   
   @Inject
   private Logger logger;
   
   
   @Produces
   public Logger produceLog(InjectionPoint injectionPoint) {
      return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
   }
   
   @Produces @ConfigProperties
   public Properties produceConfigProperties(InjectionPoint injectionPoint) throws IOException {
      Properties props = new Properties();
      ConfigProperties configProp = injectionPoint.getAnnotated().getAnnotation(ConfigProperties.class);
      if (configProp != null)
      {
         String configURLString = configProp.value();
         if (configURLString != null && configURLString.length() > 0)
         {
            props = readPropertiesFromURL(configURLString);
         }
         else
         {
            String configURLProp = configProp.urlProp();
            if (configURLProp != null && configURLProp.length() > 0)
            {
               props = readPropertiesFromURL(System.getProperty(configURLProp));
            }
         }
      }
      return props;
   }
    
   private Properties readPropertiesFromURL(String configURLString) throws IOException
   {
      Properties props = new Properties();
      if (configURLString == null || configURLString.length() == 0)
      {
         return props;
      }
      
      InputStream input = null;
      input = getClass().getClassLoader().getResourceAsStream(configURLString);
      if (input == null)
      {
         try
         {
            URL url = new URL(configURLString);
            input = url.openStream();
         }
         catch (IOException e)
         {
            logger.info("Try to load the local file: " + configURLString);
            input = new FileInputStream(configURLString);
         }
      }
      if (input != null)
      {
         try
         {
            props.load(input);
         }
         finally
         {
            input.close();
         }
      }
      
      return props;
   }
}
