package com.hl.model;

import com.jfinal.plugin.activerecord.Model;

public class Tasktype extends Model<Tasktype> {
    public static final Tasktype dao = new Tasktype().dao();
}
