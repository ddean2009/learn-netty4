package com.flydean49.extendEnum;

import lombok.Data;

public enum StatusEnum {
    START(1,"start"),
    INPROCESS(2,"inprocess"),
    END(3,"end");

    private int code;
    private String desc;

    StatusEnum(int code, String desc){
        this.code=code;
        this.desc=desc;
    }

    public static void main(String[] args) {
        StatusEnum start = START;
        System.out.println(start.name());
        System.out.println(start.ordinal());
        System.out.println(start.code);
        System.out.println(start.desc);
    }

}
