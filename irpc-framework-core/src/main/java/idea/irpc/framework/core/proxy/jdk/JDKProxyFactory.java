package idea.irpc.framework.core.proxy.jdk;

import idea.irpc.framework.core.proxy.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/5 12:50
 */
public class JDKProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(Class clazz) throws Throwable {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                new JDKClientInvocationHandler(clazz));

    }
}
