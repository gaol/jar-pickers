/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.logging.Logger;

/**
 * The version string defined by victims-cve-db. See: https://github.com/victims/victims-cve-db#version-string-common
 * 
 * When define the version scopes, please define one scope for one <major:minor> pair.
 * 
 * @author lgao
 *
 */
public class VersionScopes implements Serializable {
    /**
     * default serial version uid
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(VersionScopes.class);

    // no comma (,) or space ( ) is allowed in the version string.
    // Examples: >=2.6.6,2.6, ==2.6.7, <=3.2.4,3.2
    private static final String VERSION_SCOPE_REGEXP = "^([><=]=)([^, ]+)(,([^, ]+)){0,1}$";
    public static final Pattern VERSION_SCOPE_PATTERN = Pattern.compile(VERSION_SCOPE_REGEXP);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");

    // legal version characters: '-' '_' '.' '[a-z]' '[A-Z]' '[0-9]'
    // starts with digital, can't ends with '-' '_' '.'
    private static final String VERSION_REGEXP = "(\\d+)\\.(\\d+)\\.(\\d+)?([\\-\\.\\_\\w]+[a-zA-Z0-9]$)?";
    public static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_REGEXP);

    private static final String SCOPE_SEPERATOR = "::";

    private static final String COND_GE = ">=";
    private static final String COND_LE = "<=";
    private static final String COND_EQ = "==";

    private final List<VersionScope> versionScopes = new ArrayList<VersionScopes.VersionScope>(2);

    public VersionScopes(String versionScopes) {
        super();
        if (versionScopes == null || versionScopes.length() == 0) {
            throw new IllegalArgumentException(versionScopes + " can't be null");
        }
        StringTokenizer tokenizer = new StringTokenizer(versionScopes, SCOPE_SEPERATOR);
        while (tokenizer.hasMoreTokens()) {
            String verScope = tokenizer.nextToken().trim();
            Matcher matcher = VERSION_SCOPE_PATTERN.matcher(verScope);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(versionScopes + " is not a valid version scope definition.");
            }
            String condition = matcher.group(1);
            String version = matcher.group(2);
            String series = matcher.group(3);
            if (series != null && series.startsWith(",")) {
                series = series.substring(1);
            }
            VersionScope scope = new VersionScope(condition, version, series);
            this.versionScopes.add(scope);
        }
    }

    private static class VersionScope implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String condition;
        private final String series;

        private final Version version;

        VersionScope(String condition, String version, String series) {
            super();
            this.condition = condition;
            if (!COND_EQ.equals(condition) && !COND_LE.equals(condition) && !COND_GE.equals(condition)) {
                throw new IllegalArgumentException("Illegal Condition: " + condition + ", must be one of: >=, ==, <=");
            }
            this.series = series;
            if (COND_EQ.equals(this.condition) && this.series != null) {
                throw new IllegalArgumentException("Series must be null when using '==' condition.");
            }
            if (this.series != null && !version.startsWith(series)) {
                throw new IllegalArgumentException("Series must align with the version.");
            }
            this.version = new Version(version);

            LOG.debug(String.format("Condition: %s, Version: {major: %d, minor: %d, patch: %d, preRelease: %s}, Series: %s",
                    condition, this.version.major, this.version.minor, this.version.patch, this.version.preRelease, series));
        }

        public boolean isCaptured(Version ver) {
            if (COND_GE.equals(this.condition)) { // >=
                if (this.version.compareTo(ver) <= 0) {
                    if (this.series != null) {
                        return ver.verStr.startsWith(this.series);
                    } else {
                        return true;
                    }
                }
            }
            if (COND_LE.equals(this.condition)) { // <=
                if (this.version.compareTo(ver) >= 0) {
                    if (this.series != null) {
                        return ver.verStr.startsWith(this.series);
                    } else {
                        return true;
                    }
                }
            }
            if (COND_EQ.equals(this.condition)) {
                return this.version.compareTo(ver) == 0;
            }
            return false;
        }
    }

    private static class Version implements Serializable, Comparable<Version> {
        private static final long serialVersionUID = 1L;
        private final int major;
        private final int minor;
        private final Integer patch;
        private final String preRelease;
        private final String verStr;

        Version(String version) {
            Matcher matcher = VERSION_PATTERN.matcher(version);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(version + " is not a valid version.");
            }
            this.major = Integer.valueOf(matcher.group(1));
            this.minor = Integer.valueOf(matcher.group(2));
            String patchGrp = matcher.group(3);
            if (patchGrp != null) {
                this.patch = Integer.valueOf(patchGrp);
            } else {
                this.patch = null;
            }
            this.preRelease = matcher.group(4);
            this.verStr = version;
        }

        @Override
        public int compareTo(Version v) {
            if (v.verStr.equals("1.0.27.Final-redhat-10")) {
                System.out.println("wait..");
            }
            if (v == null)
                return 1;
            if (this.major - v.major > 0) {
                return 1;
            }
            if (this.major - v.major < 0) {
                return -1;
            }
            // major is same, test minor
            if (this.minor - v.minor > 0) {
                return 1;
            }
            if (this.minor - v.minor < 0) {
                return -1;
            }
            // major and minor are the same, test patch if it is available
            if (this.patch != null && v.patch == null) {
                return 1;
            }
            if (this.patch == null && v.patch != null) {
                return -1;
            }
            if (this.patch != null && v.patch != null) {
                if (this.patch != v.patch) {
                    return this.patch - v.patch;
                }
            }
            // test preRelease
            if (this.preRelease != null && v.preRelease == null) {
                return -1;
            }
            if (this.preRelease == null && v.preRelease != null) {
                return 1;
            }
            if (this.preRelease != null && v.preRelease != null) {
                // try number first, then compare as character
                Matcher m = NUMBER_PATTERN.matcher(this.preRelease);
                if (m.find()) {
                    int release = Integer.valueOf(m.group(1));
                    m = NUMBER_PATTERN.matcher(v.preRelease);
                    if (m.find()) {
                        int anotherRel = Integer.valueOf(m.group(1));
                        return release - anotherRel;
                    }
                }
                return this.preRelease.compareTo(v.preRelease);
            }
            return 0;
        }
    }

    /**
     * Whether the version is within the version scope definition.
     * 
     * @param version the candidate version
     * @return true if the version is within the version scope.
     */
    public boolean isCaptured(String version) {
        if (version == null || version.length() == 0) {
            return false;
        }
        Version ver = new Version(version);
        for (VersionScope scope : this.versionScopes) {
            if (COND_EQ.equals(scope.condition)) {
                return ver.compareTo(scope.version) == 0;
            }
            // if one of the scopes contains it, then return true.
            if (scope.isCaptured(ver)) {
                return true;
            }
        }
        return false;
    }
}
