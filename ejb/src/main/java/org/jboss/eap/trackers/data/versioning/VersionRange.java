/**
 * 
 */
package org.jboss.eap.trackers.data.versioning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author lgao
 *
 */
class VersionRange implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private final ArtifactVersion recommendedVersion;

   private final List<Restriction> restrictions;

   private VersionRange( ArtifactVersion recommendedVersion,
                         List<Restriction> restrictions )
   {
       this.recommendedVersion = recommendedVersion;
       this.restrictions = restrictions;
   }

   /**
    * Create a version range from a string representation
    * <p/>
    * Some spec examples are
    * <ul>
    * <li><code>1.0</code> Version 1.0</li>
    * <li><code>[1.0,2.0)</code> Versions 1.0 (included) to 2.0 (not included)</li>
    * <li><code>[1.0,2.0]</code> Versions 1.0 to 2.0 (both included)</li>
    * <li><code>[1.5,)</code> Versions 1.5 and higher</li>
    * <li><code>(,1.0],[1.2,)</code> Versions up to 1.0 (included) and 1.2 or higher</li>
    * </ul>
    *
    * @param spec string representation of a version or version range
    * @return a new {@link VersionRange} object that represents the spec
    * @throws InvalidVersionSpecificationException
    *
    */
   public static VersionRange createFromVersionSpec( String spec )
       throws InvalidVersionSpecificationException
   {
       if ( spec == null )
       {
           return null;
       }

       List<Restriction> restrictions = new ArrayList<Restriction>();
       String process = spec;
       ArtifactVersion version = null;
       ArtifactVersion upperBound = null;
       ArtifactVersion lowerBound = null;

       while ( process.startsWith( "[" ) || process.startsWith( "(" ) )
       {
           int index1 = process.indexOf( ")" );
           int index2 = process.indexOf( "]" );

           int index = index2;
           if ( index2 < 0 || index1 < index2 )
           {
               if ( index1 >= 0 )
               {
                   index = index1;
               }
           }

           if ( index < 0 )
           {
               throw new InvalidVersionSpecificationException( "Unbounded range: " + spec );
           }

           Restriction restriction = parseRestriction( process.substring( 0, index + 1 ) );
           if ( lowerBound == null )
           {
               lowerBound = restriction.getLowerBound();
           }
           if ( upperBound != null )
           {
               if ( restriction.getLowerBound() == null || restriction.getLowerBound().compareTo( upperBound ) < 0 )
               {
                   throw new InvalidVersionSpecificationException( "Ranges overlap: " + spec );
               }
           }
           restrictions.add( restriction );
           upperBound = restriction.getUpperBound();

           process = process.substring( index + 1 ).trim();

           if ( process.length() > 0 && process.startsWith( "," ) )
           {
               process = process.substring( 1 ).trim();
           }
       }

       if ( process.length() > 0 )
       {
           if ( restrictions.size() > 0 )
           {
               throw new InvalidVersionSpecificationException(
                   "Only fully-qualified sets allowed in multiple set scenario: " + spec );
           }
           else
           {
               version = new ArtifactVersion( process );
               restrictions.add( new Restriction(version) );
           }
       }

       return new VersionRange( version, restrictions );
   }

   private static Restriction parseRestriction( String spec )
       throws InvalidVersionSpecificationException
   {
       boolean lowerBoundInclusive = spec.startsWith( "[" );
       boolean upperBoundInclusive = spec.endsWith( "]" );

       String process = spec.substring( 1, spec.length() - 1 ).trim();

       Restriction restriction;

       int index = process.indexOf( "," );

       if ( index < 0 )
       {
           if ( !lowerBoundInclusive || !upperBoundInclusive )
           {
               throw new InvalidVersionSpecificationException( "Single version must be surrounded by []: " + spec );
           }

           ArtifactVersion version = new ArtifactVersion( process );

           restriction = new Restriction( version, lowerBoundInclusive, version, upperBoundInclusive );
       }
       else
       {
           String lowerBound = process.substring( 0, index ).trim();
           String upperBound = process.substring( index + 1 ).trim();
           if ( lowerBound.equals( upperBound ) )
           {
               throw new InvalidVersionSpecificationException( "Range cannot have identical boundaries: " + spec );
           }

           ArtifactVersion lowerVersion = null;
           if ( lowerBound.length() > 0 )
           {
               lowerVersion = new ArtifactVersion( lowerBound );
           }
           ArtifactVersion upperVersion = null;
           if ( upperBound.length() > 0 )
           {
               upperVersion = new ArtifactVersion( upperBound );
           }

           if ( upperVersion != null && lowerVersion != null && upperVersion.compareTo( lowerVersion ) < 0 )
           {
               throw new InvalidVersionSpecificationException( "Range defies version ordering: " + spec );
           }

           restriction = new Restriction( lowerVersion, lowerBoundInclusive, upperVersion, upperBoundInclusive );
       }

       return restriction;
   }

   public String toString()
   {
       if ( recommendedVersion != null )
       {
           return recommendedVersion.toString();
       }
       else
       {
           StringBuilder buf = new StringBuilder();
           for ( Iterator<Restriction> i = restrictions.iterator(); i.hasNext(); )
           {
               Restriction r = i.next();

               buf.append( r.toString() );

               if ( i.hasNext() )
               {
                   buf.append( ',' );
               }
           }
           return buf.toString();
       }
   }

   public boolean containsVersion( ArtifactVersion version )
   {
       for ( Restriction restriction : restrictions )
       {
           if ( restriction.containsVersion( version ) )
           {
               return true;
           }
       }
       return false;
   }

   public boolean equals( Object obj )
   {
       if ( this == obj )
       {
           return true;
       }
       if ( !( obj instanceof VersionRange ) )
       {
           return false;
       }
       VersionRange other = (VersionRange) obj;

       boolean equals =
           recommendedVersion == other.recommendedVersion
               || ( ( recommendedVersion != null ) && recommendedVersion.equals( other.recommendedVersion ) );
       equals &=
           restrictions == other.restrictions
               || ( ( restrictions != null ) && restrictions.equals( other.restrictions ) );
       return equals;
   }

   public int hashCode()
   {
       int hash = 7;
       hash = 31 * hash + ( recommendedVersion == null ? 0 : recommendedVersion.hashCode() );
       hash = 31 * hash + ( restrictions == null ? 0 : restrictions.hashCode() );
       return hash;
   }
}
