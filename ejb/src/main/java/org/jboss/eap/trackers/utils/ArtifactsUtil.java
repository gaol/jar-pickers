/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jboss.eap.trackers.model.Artifact;

/**
 * @author lgao
 *
 */
public final class ArtifactsUtil {

	private ArtifactsUtil(){
		//ops
	}
	
	public static String guessComponentNameFromAritifact(String groupId, String artiId, String version) {
	    // currently only returns groupId
	    return groupId;
	}
	
	/**
	 * select distinct(artifactId) from the list
	 */
	public static List<Artifact> distinctArtifactIdArtis(List<Artifact> artis) {
		List<Artifact> result = new ArrayList<Artifact>();
		if (artis != null && artis.size() > 0) {
			for (Artifact arti: artis) {
				if (!artifactIdIn(result, arti.getArtifactId())) {
					result.add(arti);
				}
			}
		}
		return result;
	}

	public static boolean artifactIdIn(List<Artifact> artis, String artifactId) {
		for (Artifact arti: artis) {
			if (artifactId.equals(arti.getArtifactId())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * select distinct(groupId) from the list
	 */
	public static List<Artifact> distinctGroupIdArtis(List<Artifact> artis) {
		List<Artifact> result = new ArrayList<Artifact>();
		if (artis != null && artis.size() > 0) {
			for (Artifact arti: artis) {
				if (!groupIdInArtis(result, arti.getGroupId())) {
					result.add(arti);
				}
			}
		}
		return result;
	}

	public static boolean groupIdInArtis(List<Artifact> artis, String groupId) {
		for (Artifact arti: artis) {
			if (groupId.equals(arti.getGroupId())) {
				return true;
			}
		}
		return false;
	}
	
	   /**
     * This will close the input stream at last.
     */
    public static List<String> getMatchRegexLines(InputStream input, String regex) throws IOException{
        List<String> result = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                if (line.matches(regex)) {
                    result.add(line);
                }
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return result;
    }
}