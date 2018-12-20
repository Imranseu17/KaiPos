package com.kaicomsol.kpos.model;

public class History {

    public long id = 0;
    public String no;
    public String time;
    public String type;

    public History(String type, String time) {
        this.type = type;
        this.time = time;
    }

    public History(long id, String no, String time, String type) {
        this.id = id;
        this.no = no;
        this.time = time;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public String getNo() {
        return no;
    }

    public String getTime() {
        return time;
    }

    public String getType() {
        return type;
    }
}
