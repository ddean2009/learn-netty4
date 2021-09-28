
package com.flydean24.socketserver2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

@Slf4j
public final class Server2HttpPage {

    public static ByteBuf getContent() throws IOException {
        URL url= Server2HttpPage.class.getClassLoader().getResource("socket2.html");
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

    private Server2HttpPage() {
    }
}
