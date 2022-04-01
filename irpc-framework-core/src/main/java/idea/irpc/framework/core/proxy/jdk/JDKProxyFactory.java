package idea.irpc.framework.core.proxy.jdk;

import idea.irpc.framework.core.client.RpcReferenceWrapper;
import idea.irpc.framework.core.proxy.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/5 12:50
 */
public class JDKProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(RpcReferenceWrapper rpcReferenceWrapper) throws Throwable {
        return (T) Proxy.newProxyInstance(rpcReferenceWrapper.getAimClass().getClassLoader(), new Class[]{rpcReferenceWrapper.getAimClass()},
                new JDKClientInvocationHandler(rpcReferenceWrapper));

    }
}
