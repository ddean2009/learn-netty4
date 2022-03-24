package com.flydean17.marshalling;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Student implements Serializable {

    private String name;
    private int age;
    private String className;

}
