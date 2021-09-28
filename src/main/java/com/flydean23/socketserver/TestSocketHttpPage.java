
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
