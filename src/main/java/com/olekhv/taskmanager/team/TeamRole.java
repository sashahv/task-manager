package com.olekhv.taskmanager.team;

public enum TeamRole {
    ADMIN("Admin"),
    MEMBER("Member");

    private String name;

    TeamRole(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
