// Created by Eric B. 30.01.2021 23:09
package de.ericzones.bungeesystem.collectives.database;

public enum SqlDataType {

    VARCHAR("VARCHAR(255)"),
    BOOLEAN("BOOLEAN"),
    INT("BIGINT UNSIGNED"),
    FLOAT("FLOAT(500,250)"),
    DOUBLE("DOUBLE(1500,500)"),
    TEXT("MEDIUMTEXT");

    private SqlDataType(String sqlTag){
        this.sqlTag = sqlTag;
    };

    private final String sqlTag;

    public String getSqlTag() {
        return sqlTag;
    }
}
