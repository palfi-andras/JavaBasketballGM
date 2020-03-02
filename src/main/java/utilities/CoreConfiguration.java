package utilities;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * MET CS 622
 *
 * @author apalfi
 * @version 1.0
 * <p>
 * The CoreConfiguration class is meant to provide a set of static functions to read from the config file of this program
 */
public class CoreConfiguration {

    private static final String configFilePath = "./resources/config.properties";
    private static CoreConfiguration instance = null;
    private CompositeConfiguration config;

    private CoreConfiguration() {
        try {
            config = new CompositeConfiguration();
            config.addConfiguration(new PropertiesConfiguration(configFilePath));
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static CoreConfiguration getInstance() {
        if (instance == null)
            instance = new CoreConfiguration();
        return instance;
    }

    public int getIntProperty(String property) {
        return config.getInt(property);
    }

    public String getStringProperty(String property) {
        return config.getString(property);
    }

    public double getDoubleProperty(String property) {
        return config.getDouble(property);
    }


}
