package ch.retorte.intervalmusiccompositor;

/**
 * @author nw
 */
public class Version implements Comparable<Version> {

  private Integer major = 0;
  private Integer minor = 0;
  private Integer revision = 0;
  private Integer build = 0;
	
	public Version(String version) {
		
		String[] parts = version.trim().split("\\.");
		
		try {
			major = Integer.valueOf(parts[0].trim());
		} 
		catch (NumberFormatException e) {
			// ignore, value is already set as zero
		}
		
		if(2 <= parts.length) {
			try {
				minor = Integer.valueOf(parts[1].trim());
			} 
			catch (NumberFormatException e) {
				// ignore, value is already set as zero
			}
		} 
		
		if(3 <= parts.length) {
			try {
				revision = Integer.valueOf(parts[2].trim());
			} 
			catch (NumberFormatException e) {
				// ignore, value is already set as zero
			}
		}
		
		if(4 <= parts.length) {
			try {
				build = Integer.valueOf(parts[3].trim());
			} 
			catch (NumberFormatException e) {
				// ignore, value is already set as zero
			}
		}
	}
	
	public Version(int major) {
		this.major = major;
	}
	
	public Version(int major, int minor) {
		this.major = major;
		this.minor = minor;
	}
	
	public Version(int major, int minor, int revision) {
		this.major = major;
		this.minor = minor;
		this.revision = revision;
	}
	
	public Version(int major, int minor, int revision, int build) {
		this.major = major;
		this.minor = minor;
		this.revision = revision;
		this.build = build;
	}

	@Override
	public int compareTo(Version o) {
		if(major != o.getMajor()) {
      return major.compareTo(o.getMajor());
			
		} else if(minor != o.getMinor()) {
      return minor.compareTo(o.getMinor());
			
		} else if(revision != o.getRevision()) {
      return revision.compareTo(o.getRevision());
			
		} else if(build != o.getBuild()) {
      return build.compareTo(o.getBuild());
			
		} else {
			return 0;
		}
	}
	
	@Override
	public String toString() {
		return major + "." + minor + "." + revision;
	}
	
	public int getMajor() {
		return major;
	}
	
	public int getMinor() {
		return minor;
	}
	
	public int getRevision() {
		return revision;
	}
	
	public int getBuild() {
		return build;
	}
	
}
