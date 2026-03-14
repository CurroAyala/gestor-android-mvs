package com.curro.gestormvs.domain.models;

public class Host {

    private long id;
    private String name;
    private String user;
    private String ip;
    private Integer port;
    private String password;

    public Host(long id, String name, String user, String ip, Integer port, String password) {
        this.id = id;
        this.name = name;
        this.user = user;
        this.ip = ip;
        this.port = port;
        this.password = password;
    }


    // Getters and Setters

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

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

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }
}
