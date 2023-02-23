package com.olekhv.taskmanager.team;

public enum TeamType {
    PUBLIC("Public"),
    PRIVATE("Private");

    private String name;

    TeamType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
