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
package com.flydean34.http2images;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static io.netty.buffer.Unpooled.unreleasableBuffer;

/**
 * @author wayne
 * @version ImagePage,  2021/9/28
 */
@Slf4j
public class ImagePage {

    private static final int BLOCK_SIZE = 1024;

    private static final Map<String, ByteBuf> imageMap = new HashMap<>(200);

    public static String getName(int id) {
        return "img" + id + ".jpg";
    }

    public static ByteBuf getImage(int id) {
        if(imageMap.size()==0){
            init();
        }
        return imageMap.get(getName(id));
    }

    private static void init() {
        for (int id = 0; id < 6; id++) {
            try {
                String name = getName(id);
                log.info("image:{}",name);
                ByteBuf fileBytes = unreleasableBuffer(toByteBuf(ImagePage.class
                        .getResourceAsStream(name)).asReadOnly());
                imageMap.put(name, fileBytes);
            } catch (IOException e) {
                log.error(e.getMessage(),e);
            }
        }
    }


    /**
     * 将 InputStream 转换成为ByteBuf
     *
     */
    public static ByteBuf toByteBuf(InputStream input) throws IOException {
        ByteBuf buf = Unpooled.buffer();
        int n = 0;
        do {
            n = buf.writeBytes(input, BLOCK_SIZE);
        } while (n > 0);
        return buf;
    }


    public static ByteBuf getContent() throws IOException {
        URL url= ImagePage.class.getResource("image.html");
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


}
