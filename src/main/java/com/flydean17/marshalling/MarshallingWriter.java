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

import org.jboss.marshalling.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

public class MarshallingWriter {

    public void marshallingWrite(String fileName, Object obj) throws IOException {
        // 使用river作为marshalling的方式
        MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("river");
        // 创建marshalling的配置
        MarshallingConfiguration configuration = new MarshallingConfiguration();
        // 使用版本号4
        configuration.setVersion(4);
//        configuration.setClassCount(10);
//        configuration.setBufferSize(8096);
//        configuration.setInstanceCount(100);
//        configuration.setExceptionListener(new MarshallingException());
//        configuration.setClassResolver(new SimpleClassResolver(getClass().getClassLoader()));
//        configuration.setObjectPreResolver(new ChainingObjectResolver(Collections.singletonList(new HibernateDetachResolver())));
            final Marshaller marshaller = marshallerFactory.createMarshaller(configuration);
            try(FileOutputStream os = new FileOutputStream(fileName)){
                marshaller.start(Marshalling.createByteOutput(os));
                marshaller.writeObject(obj);
                marshaller.finish();
            }
    }

    public static void main(String[] args) throws IOException {
        MarshallingWriter writer = new MarshallingWriter();
        Student student= new Student("jack", 18, "first grade");
        writer.marshallingWrite("/tmp/marshall.txt",student);
    }

}
