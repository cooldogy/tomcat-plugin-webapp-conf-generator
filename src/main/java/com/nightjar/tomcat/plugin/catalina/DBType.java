package com.nightjar.tomcat.plugin.catalina;

public enum DBType {

    MYSQL(0),
    ORACLE(1),
    SQLSERVER(2);

    private final int value;

    DBType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
