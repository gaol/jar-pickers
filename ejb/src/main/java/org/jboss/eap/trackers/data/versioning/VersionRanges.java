/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data.versioning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The VersionRanges definition.
 * 
 * <ul>
 *   <li>x, means v == x
 *   <li>[x,y], means v >= x, and v <= y
 *   <li>[,y] or (,y], means v <= y and v >= 0
 *   <li>[x,], means v >= x
 *   <li>(x,], means v > x
 *   <li>(x,y], means v > x, and v <= y
 *   <li>(x,y), means v > x, and v < y
 *   <li>[x,y), means v >= x, and v < y
 * </ul>
 * <p>
 * Each Version Range splits with char: ','
 * <p>
 * <p>
 * For Example:
 * <pre>
 *   <li>(,2.3.4] : (2.3.6, 3.4.5)
 *   <li>[1.1.1,]
 * </pre>
 * @author lgao
 *
 */
public class VersionRanges implements Serializable, Cloneable
{
   /**
    * default serial version uid
    */
   private static final long serialVersionUID = 1L;

   private static final String SCOPE_SEPERATOR = ":";

   private final List<VersionRange> verRanges = new ArrayList<VersionRange>();

   private final String rawStrToClone;

   public VersionRanges(String versionSpecs)
   {
      super();
      if (versionSpecs == null || versionSpecs.length() == 0)
      {
         throw new IllegalArgumentException("VersionSpec can't be null");
      }
      StringTokenizer tokenizer = new StringTokenizer(versionSpecs, SCOPE_SEPERATOR);
      while (tokenizer.hasMoreTokens())
      {
         String verSpec = tokenizer.nextToken().trim();
         try
         {
            this.verRanges.add(VersionRange.createFromVersionSpec(verSpec));
         }
         catch (InvalidVersionSpecificationException e)
         {
            throw new IllegalArgumentException("Can't construct VersionRage: " + verSpec, e);
         }
      }
      this.rawStrToClone = versionSpecs;
   }

   public VersionRanges clone()
   {
      return new VersionRanges(this.rawStrToClone);
   }

   @Override
   public String toString()
   {
      return this.rawStrToClone;
   }

   /**
    * Whether the version is within the version scope definition.
    * 
    * @param version the candidate version
    * @return true if the version is within the version scope.
    */
   public boolean isCaptured(String version)
   {
      if (version == null || version.length() == 0)
      {
         return false;
      }
      ArtifactVersion ver = new ArtifactVersion(version);
      for (VersionRange range : this.verRanges)
      {
         if (range.containsVersion(ver))
         {
            return true;
         }
      }
      return false;
   }
}
