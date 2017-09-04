package com.qzsang.mycacheutil;

import java.io.Serializable;

/**
 * Created by qzsang on 2017/9/4.
 */

public class StudentBean implements Serializable {
    private static final long serialVersionUID = 1427236929209072540L;
    private String name;
    private int age;

    public StudentBean() {
    }

    public StudentBean(String name, int age) {
        this.name = name;
        this.age = age;
    }


    @Override
    public String toString() {
        return "StudentBean{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
