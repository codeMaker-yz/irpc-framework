package idea.irpc.framework.core.proxy;


import idea.irpc.framework.core.client.RpcReferenceWrapper;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/5 12:51
 */
public interface ProxyFactory {

    <T> T getProxy(RpcReferenceWrapper rpcReferenceWrapper) throws Throwable;

}
