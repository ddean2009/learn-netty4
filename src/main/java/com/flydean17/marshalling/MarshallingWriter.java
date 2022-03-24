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
