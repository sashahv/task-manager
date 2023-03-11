package com.olekhv.taskmanager.task;

public enum TaskProgress {
    TODO("TODO"),
    PLANNING("Planning"),
    IN_PROCESS("In process"),
    FINISHED("Finished"),
    OVERDUE("Overdue"),
    CLOSED("Closed");

    private String name;

    TaskProgress(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
