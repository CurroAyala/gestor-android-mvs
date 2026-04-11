package com.curro.gestormvs.domain.models;

public class Snapshot {

    private String name;
    private String description;
    private String state;

    public Snapshot(String name, String description, String state) {
        this.name = name;
        this.description = description;
        this.state = state;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
