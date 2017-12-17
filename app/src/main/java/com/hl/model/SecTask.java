package com.hl.model;

import com.jfinal.plugin.activerecord.Model;

public class SecTask extends Model<SecTask> {
    public static final SecTask dao = new SecTask().dao();
}
