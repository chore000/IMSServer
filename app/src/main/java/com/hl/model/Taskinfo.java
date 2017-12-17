package com.hl.model;

import com.jfinal.plugin.activerecord.Model;

public class Taskinfo extends Model<Taskinfo> {
    public static final Taskinfo dao = new Taskinfo().dao();
}
