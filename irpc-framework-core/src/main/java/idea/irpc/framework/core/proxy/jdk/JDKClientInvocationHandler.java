package idea.irpc.framework.core.proxy.jdk;

import idea.irpc.framework.core.client.RpcReferenceWrapper;
import idea.irpc.framework.core.common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static idea.irpc.framework.core.common.cache.CommonClientCache.RESP_MAP;
import static idea.irpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;
import static idea.irpc.framework.core.common.constants.RpcConstants.DEFAULT_TIMEOUT;

/**
 * 各种代理工厂统一使用这个InvocationHandler
 *
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/5 12:55
 */
public class JDKClientInvocationHandler implements InvocationHandler {
    private final static Object OBJECT = new Object();

    private RpcReferenceWrapper rpcReferenceWrapper;

    private int timeOut = DEFAULT_TIMEOUT;

    public JDKClientInvocationHandler(RpcReferenceWrapper rpcReferenceWrapper) {
        this.rpcReferenceWrapper = rpcReferenceWrapper;
        timeOut = Integer.parseInt(String.valueOf(rpcReferenceWrapper.getAttachments().get("timeOut")));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetMethod(method.getName());
        rpcInvocation.setTargetServiceName(rpcReferenceWrapper.getAimClass().getName());
        //注入一个uuid，对每一次请求做单独区分
        rpcInvocation.setUuid(UUID.randomUUID().toString());
        rpcInvocation.setAttachments(rpcReferenceWrapper.getAttachments());
        RESP_MAP.put(rpcInvocation.getUuid(),OBJECT);
        SEND_QUEUE.add(rpcInvocation);
        if(rpcReferenceWrapper.isAsync()){
            return null;
        }
        long beginTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - beginTime < timeOut){
            Object object = RESP_MAP.get(rpcInvocation.getUuid());
            if(object instanceof RpcInvocation){
                return ((RpcInvocation) object).getResponse();
            }
        }
        throw new TimeoutException("wait for response from server on client " + timeOut + "ms!");

    }
}
