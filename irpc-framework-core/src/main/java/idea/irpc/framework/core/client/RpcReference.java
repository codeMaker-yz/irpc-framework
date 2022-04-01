package idea.irpc.framework.core.client;

import idea.irpc.framework.core.proxy.ProxyFactory;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/5 12:39
 */
public class RpcReference {
    public ProxyFactory proxyFactory;

    public RpcReference(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    /**
     * 根据接口类型获取代理对象
     *
     * @param rpcReferenceWrapper
     * @param <T>
     * @return
     * @throws Throwable
     */
    public <T> T get(RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable{
        return proxyFactory.getProxy(rpcReferenceWrapper);
    }
}
