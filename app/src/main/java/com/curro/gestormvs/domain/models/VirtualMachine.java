package com.curro.gestormvs.domain.models;

import java.io.Serializable;

public class VirtualMachine implements Serializable {

    private Integer id;
    private String name;
    private String state;

    public VirtualMachine(Integer id, String name, String state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
