package com.hl.model;

import com.jfinal.plugin.activerecord.Model;

public class BpModel extends Model<BpModel> {
    public static final BpModel dao = new BpModel().dao();
}
