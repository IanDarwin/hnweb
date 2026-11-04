package model;

import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

public class Config implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Properties props;

	protected static final String CONFIG_PROPERTIES_NAME = "/config.properties";

	static {
		props = new Properties();
		loadProps();
	}

	private static void loadProps() {
		try (InputStream is = 
			Config.class.getResourceAsStream(CONFIG_PROPERTIES_NAME)) {
			if (is == null) {
				throw new IllegalArgumentException(
					"File " + CONFIG_PROPERTIES_NAME + " not on CLASSPATH.");
			}
			props.load(is);
			props.store(System.out, "Dump of config properties");
		} catch (IOException e) {
			throw new ExceptionInInitializerError(
				"CANTHAPPEN: IOException in close of resource " + CONFIG_PROPERTIES_NAME);
		}
	}

	/** Only public for use by Testing! */
	public static void reload() {
		props.clear();
		loadProps();
	}

	public static String getProperty(String key) {
		return props.getProperty(key);
	}

	public static void setProperty(String k, String v) {
		props.setProperty(k, v);
	}

	/** Optimization */
	public static String orgName() {
		return props.getProperty("orgName", "UNNAMED - CLUBLIST");
	}

	public static int getIntProperty(String key) {
		return getIntProperty(key, null);
	}

	public static int getIntProperty(String key, Integer dflt) {
		var s = props.getProperty(key);
		if (s == null) {
			if (dflt != null) {
				return dflt;
			} else {
				throw new IllegalStateException(key + " not found in properties and no default");
			}
		} else {
			return Integer.parseInt(s);
		}
	}

	public static double getDoubleProperty(String key, Double dflt) {
		var s = props.getProperty(key);
		if (s == null) {
			if (dflt != null) {
				return dflt;
			} else {
				throw new IllegalStateException(key + " not found in properties and no default");
			}
		} else {
			return Integer.parseInt(s);
		}
	}

	// Most PROPTNAME_xxx are defined in the class that uses them.
	// Only props used in multiple classes should be defined here.

	/**
	 * HN Membership year runs from Sept 1 to Aug 31. All memberships
	 * are due to renew starting in this month.
	 */
	public final static String PROPNAME_RENEWAL_MONTH = "datetime.renewalmonth";


	final static int RPROPNAME_ENEWAL_FREE_YEAR_MONTH = 3;

}
