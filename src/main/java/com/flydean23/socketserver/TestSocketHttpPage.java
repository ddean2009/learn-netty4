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
package com.flydean23.socketserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;

/**
 * 读取html页面的数据，展示给用户
 */
@Slf4j
public final class TestSocketHttpPage {

    public static ByteBuf getContent() throws IOException {
        URL url= TestSocketHttpPage.class.getClassLoader().getResource("socket.html");
        log.info("url: {}",url);
        String filePath = url.getFile();
        File file = new File(filePath);
        log.info(file.getCanonicalPath());
        FileReader fileReader = new FileReader(file);
        BufferedReader reader  = new BufferedReader(fileReader);
        StringBuilder builder= new StringBuilder();
        reader.lines().forEach(builder::append);
        reader.close();
        fileReader.close();
        return Unpooled.copiedBuffer(builder.toString(), CharsetUtil.UTF_8);
    }

    private TestSocketHttpPage() {
    }
}
