package com.noolitef.settings;

public final class Settings {
    private static String ip = "";
    private static String dns = "";
    private static String address = "192.168.100.170"; // http://134.17.24.191/
    private static boolean useDNS;
    private static int connectTimeout = 6000; //ms
    private static int switchTimeout = 250; //ms
    private static String login = "admin";
    private static String password = "admin";
    private static boolean useAuthorization;
    private static boolean developerMode;
    private static boolean nightMode;

    public static String getIP() {
        return ip;
    }

    public static void setIP(String ip) {
        Settings.ip = ip;
    }

    public static String getDNS() {
        return dns;
    }

    public static void setDNS(String dns) {
        Settings.dns = dns;
    }

    public static boolean useDNS() {
        return useDNS;
    }

    public static void useDNS(boolean use) {
        useDNS = use;
        if (useDNS) {
            address = dns;
        } else {
            address = ip;
        }
    }

    public static void setAddress(String ip) {
        address = ip;
    }

    public static String getAddress() {
        return address;
    }

    public static String URL() {
        return String.format("http://%s/", address);
    }

    public static int connectTimeout() {
        return connectTimeout;
    }

    public static int switchTimeout() {
        return switchTimeout;
    }

    public static String login() {
        return login;
    }

    public static void setLogin(String login) {
        Settings.login = login;
    }

    public static String password() {
        return password;
    }

    public static void setPassword(String password) {
        Settings.password = password;
    }

    public static boolean isAuthorization() {
        return useAuthorization;
    }

    public static void useAuthorization(boolean use) {
        useAuthorization = use;
    }

    public static boolean isDeveloperMode() {
        return developerMode;
    }

    public static void setDeveloperMode(boolean developerMode) {
        Settings.developerMode = developerMode;
    }

    public static boolean isNightMode() {
        return nightMode;
    }

    public static void setNightMode(boolean nightMode) {
        Settings.nightMode = nightMode;
    }
}
