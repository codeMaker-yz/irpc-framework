package idea.irpc.framework.core.dispatch;

import idea.irpc.framework.core.common.RpcInvocation;
import idea.irpc.framework.core.common.RpcProtocol;
import idea.irpc.framework.core.server.ServerChannelReadData;

import java.lang.reflect.Method;
import java.util.concurrent.*;

import static idea.irpc.framework.core.common.cache.CommonServerCache.*;

/**
 * @author £ºMr.Zhang
 * @date £ºCreated in 2022/4/12 16:06
 */
public class ServerChannelDispatcher {
    private BlockingQueue<ServerChannelReadData> RPC_DATA_QUEUE;

    private ExecutorService executorService;

    public ServerChannelDispatcher() {
    }

    public void init(int queueSize, int bizThreadNums){
        RPC_DATA_QUEUE = new ArrayBlockingQueue<>(queueSize);
        executorService = new ThreadPoolExecutor(bizThreadNums,bizThreadNums,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(512));

    }

    public void add(ServerChannelReadData serverChannelReadData){
        RPC_DATA_QUEUE.add(serverChannelReadData);
    }

    public void startDataConsume(){
        Thread thread = new Thread(new ServerJobCoreHandle());
        thread.start();
    }
    class ServerJobCoreHandle implements Runnable{
        @Override
        public void run() {
            while (true){
                try {
                    ServerChannelReadData serverChannelReadData = RPC_DATA_QUEUE.take();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                RpcProtocol rpcProtocol = serverChannelReadData.getRpcProtocol();
                                RpcInvocation rpcInvocation = SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);
                                //Ö´ÐÐ¹ýÂËÁ´Â·
                                SERVER_FILTER_CHAIN.doFilter(rpcInvocation);
                                Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
                                Method[] methods = aimObject.getClass().getDeclaredMethods();
                                Object result = null;
                                for(Method method : methods){
                                    if(method.getName().equals(rpcInvocation.getTargetMethod())){
                                        if(method.getReturnType().equals(Void.TYPE)){
                                            method.invoke(aimObject, rpcInvocation.getArgs());
                                        } else {
                                            result = method.invoke(aimObject,rpcInvocation.getArgs());
                                        }
                                        break;
                                    }
                                }
                                rpcInvocation.setResponse(result);
                                RpcProtocol respRpcProtocol = new RpcProtocol(SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
                                serverChannelReadData.getChannelHandlerContext().writeAndFlush(respRpcProtocol);
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
