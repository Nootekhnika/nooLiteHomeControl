package com.noolitef;

import java.util.ArrayList;

public class Home {
    private String name;
    private String ip;
    private String dns;
    private boolean useDNS;
    private String login;
    private String password;
    private boolean useAuthorization;

    private ArrayList<Room> rooms;

    public Home(String name, String ip, String dns, boolean useDNS, String login, String password, boolean useAuthorization, ArrayList<Room> rooms) {
        this.name = name;
        this.ip = ip;
        this.dns = dns;
        this.useDNS = useDNS;
        this.login = login;
        this.password = password;
        this.useAuthorization = useAuthorization;
        this.rooms = rooms;
    }

    public Home(String homeString) {
        String[] strings = homeString.split("||");
        if (strings.length < 7) return;
        this.name = strings[strings.length - 7];
        this.ip = strings[strings.length - 6];
        this.dns = strings[strings.length - 5];
        this.useDNS = strings[strings.length - 4].equals("true");
        this.login = strings[strings.length - 3];
        this.password = strings[strings.length - 2];
        this.useAuthorization = strings[strings.length - 1].equals("true");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIP() {
        return ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    public String getDNS() {
        return dns;
    }

    public void setDNS(String dns) {
        this.dns = dns;
    }

    public boolean useDNS() {
        return useDNS;
    }

    public void useDNS(boolean useDNS) {
        this.useDNS = useDNS;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAuthorization() {
        return useAuthorization;
    }

    public void useAuthorization(boolean useAuthorization) {
        this.useAuthorization = useAuthorization;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public void setRooms(ArrayList<Room> rooms) {
        this.rooms = rooms;
    }


    public String getString() {
        return String.format("%s||%s||%s||%s||%s||%s", name, ip, dns, useDNS, login, password, useAuthorization);
    }
}
