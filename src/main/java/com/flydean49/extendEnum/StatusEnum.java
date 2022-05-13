/*
 * Copyright 2022 learn-netty4 Project
 *
 * The learn-netty4 Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
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
