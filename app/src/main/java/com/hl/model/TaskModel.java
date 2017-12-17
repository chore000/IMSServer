package com.hl.model;

import com.jfinal.plugin.activerecord.Model;

public class TaskModel extends Model<TaskModel> {
    public static final TaskModel dao = new TaskModel().dao();
}
