package com.dgregory.il2ssd.business.config;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 23/09/13 20:44
 * il2ssd
 */
public class Config {

    static PropertiesConfiguration defaultConfiguration = new PropertiesConfiguration();
    static HierarchicalINIConfiguration iniConfiguration = new HierarchicalINIConfiguration();
    static CompositeConfiguration compositeConfiguration = new CompositeConfiguration();

    public static void loadConfiguration() {

        defaultConfiguration.addProperty("Connection.IPAddress", "127.0.0.1");
        defaultConfiguration.addProperty("Connection.Port", "21003");
        defaultConfiguration.addProperty("Path.ServerPath", "");
        defaultConfiguration.addProperty("Path.MissionPath", "");
        defaultConfiguration.addProperty("Path.DCGPath", "");
        defaultConfiguration.addProperty("Mode.RemoteLoad", true);
        defaultConfiguration.addProperty("Mode.RemoteLoadPath", "");
        defaultConfiguration.addProperty("Mode.DCG", false);

        if (Files.exists(Paths.get("il2ssd.ini"))) {
            try {
                iniConfiguration.load("il2ssd.ini");
            } catch (ConfigurationException e) {
                System.out.println("Configuration file invalid.");
            }
        } else {
            System.out.println("Config file not found.");
        }

        compositeConfiguration.addConfiguration(iniConfiguration, true);
        compositeConfiguration.addConfiguration(defaultConfiguration);

    }

    public static void saveConfiguration() {
        try {
            iniConfiguration.save("il2ssd.ini");
        } catch (ConfigurationException e) {
            System.out.println("Couldn't save to config file.");
        }
    }

    public static String getIpAddress() {
        return compositeConfiguration.getString("Connection.IPAddress");
    }

    public static void setIpAddress(String ipEntry) {
        compositeConfiguration.setProperty("Connection.IPAddress", ipEntry);
    }

    public static String getPort() {
        return compositeConfiguration.getString("Connection.Port");
    }

    public static void setPort(String portEntry) {
        compositeConfiguration.setProperty("Connection.Port", portEntry);
    }

    public static String getServerPath() {
        return compositeConfiguration.getString("Path.ServerPath");
    }

    public static void setServerPath(String serverEntry) {
        compositeConfiguration.setProperty("Path.ServerPath", serverEntry);
    }

    public static String getMissionPath() {
        return compositeConfiguration.getString("Path.MissionPath");
    }

    public static void setMissionPath(String missionEntry) {
        compositeConfiguration.setProperty("Path.MissionPath", missionEntry);
    }

    public static String getDcgPath() {
        return compositeConfiguration.getString("Path.DCGPath");
    }

    public static void setDcgPath(String dcgEntry) {
        compositeConfiguration.setProperty("Path.DCGPath", dcgEntry);
    }

    public static Boolean getRemoteMode() {
        return compositeConfiguration.getBoolean("Mode.RemoteLoad");
    }

    public static void setRemoteMode(Boolean remoteEntry) {
        compositeConfiguration.setProperty("Mode.RemoteLoad", remoteEntry);
    }

    public static String getRemotePath() {
        return compositeConfiguration.getString("Mode.RemoteLoadPath");
    }

    public static void setRemotePath(String remotePathEntry) {
        compositeConfiguration.setProperty("Mode.RemoteLoadPath", remotePathEntry);
    }

    public static Boolean getDcgMode() {
        return compositeConfiguration.getBoolean("Mode.DCG");
    }

    public static void setDcgMode(Boolean dcgModeEntry) {
        compositeConfiguration.setProperty("Mode.DCG", dcgModeEntry);
    }
}

