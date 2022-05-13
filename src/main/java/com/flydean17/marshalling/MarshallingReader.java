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
package com.flydean17.marshalling;

import lombok.extern.slf4j.Slf4j;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.Unmarshaller;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
public class MarshallingReader {

    public void marshallingRead(String fileName) throws IOException, ClassNotFoundException {
        // 使用river协议创建MarshallerFactory
        MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("river");
        // 创建配置文件
        MarshallingConfiguration configuration = new MarshallingConfiguration();
        // 使用版本号4
        configuration.setVersion(4);
            final Unmarshaller unmarshaller = marshallerFactory.createUnmarshaller(configuration);
            try(FileInputStream is = new FileInputStream(fileName)){
                unmarshaller.start(Marshalling.createByteInput(is));
                Student student=(Student)unmarshaller.readObject();
                log.info("student:{}",student);
                unmarshaller.finish();
            }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        MarshallingReader reader= new MarshallingReader();
        reader.marshallingRead("/tmp/marshall.txt");
    }
}
