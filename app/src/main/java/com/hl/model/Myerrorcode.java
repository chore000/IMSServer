package com.hl.model;

public class Myerrorcode {
    int stat = -1;
    String Codemsg = "";

    public Myerrorcode() {
    }

    public int getStat() {

        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public String getCodemsg() {
        return Codemsg;
    }

    public void setCodemsg(String codemsg) {
        Codemsg = codemsg;
    }
}
