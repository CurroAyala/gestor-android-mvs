package com.curro.gestormvs.domain.models;

public class Host {

    private String name;
    private String user;
    private String ip;
    private Integer port;

    public Host(String name, String user, String ip, Integer port) {
        this.name = name;
        this.user = user;
        this.ip = ip;
        this.port = port;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
