package com.kaicomsol.kpos.models;

public class Error {

    public long id = 0;
    public String group;
    public String type;
    public String time;


    public Error(String group, String type, String time) {
        this.group = group;
        this.type = type;
        this.time = time;
    }

    public String getGroup() {
        return group;
    }

    public String getType() {
        return type;
    }

    public String getTime() {
        return time;
    }
}
