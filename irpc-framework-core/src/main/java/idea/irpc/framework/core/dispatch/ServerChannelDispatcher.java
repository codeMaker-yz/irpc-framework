package idea.irpc.framework.core.dispatch;

import idea.irpc.framework.core.common.RpcInvocation;
import idea.irpc.framework.core.common.RpcProtocol;
import idea.irpc.framework.core.common.exception.IRpcException;
import idea.irpc.framework.core.server.ServerChannelReadData;

import java.lang.reflect.Method;
import java.util.concurrent.*;

import static idea.irpc.framework.core.common.cache.CommonServerCache.*;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/4/12 16:06
 */
public class ServerChannelDispatcher {
    private BlockingQueue<ServerChannelReadData> RPC_DATA_QUEUE;

    private ExecutorService executorService;

    public ServerChannelDispatcher() {
    }

    public void init(int queueSize, int bizThreadNums){
        RPC_DATA_QUEUE = new ArrayBlockingQueue<>(queueSize);
        executorService = new ThreadPoolExecutor(0,Integer.MAX_VALUE,
                60L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>());

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
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                                RpcProtocol rpcProtocol = serverChannelReadData.getRpcProtocol();
                                RpcInvocation rpcInvocation = SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);
                                //执行过滤链路
                                try {
                                    //前置过滤器
                                    SERVER_BEFORE_FILTER_CHAIN.doFilter(rpcInvocation);
                                } catch (Exception cause){
                                    //针对自定义异常进行捕获，并且返回异常信息给客户端
                                    if(cause instanceof IRpcException){
                                        IRpcException rpcException = ((IRpcException) cause);
                                        RpcInvocation reqParam = rpcException.getRpcInvocation();
                                        rpcInvocation.setE(rpcException);
                                        byte[] body = SERVER_SERIALIZE_FACTORY.serialize(reqParam);
                                        RpcProtocol respRpcProtocol = new RpcProtocol(body);
                                        serverChannelReadData.getChannelHandlerContext().writeAndFlush(respRpcProtocol);
                                        return;
                                    }
                                }
                                Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
                                Method[] methods = aimObject.getClass().getDeclaredMethods();
                                Object result = null;
                                for(Method method : methods){
                                    if(method.getName().equals(rpcInvocation.getTargetMethod())){
                                        if(method.getReturnType().equals(Void.TYPE)){
                                            try {
                                                method.invoke(aimObject, rpcInvocation.getArgs());
                                            } catch (Exception e){
                                                rpcInvocation.setE(e);
                                            }
                                        } else {
                                            try {
                                                result = method.invoke(aimObject,rpcInvocation.getArgs());
                                            } catch (Exception e){
                                                rpcInvocation.setE(e);
                                            }
                                        }
                                        break;
                                    }
                                }
                                rpcInvocation.setResponse(result);
                                //后置过滤器
                                SERVER_AFTER_FILTER_CHAIN.doFilter(rpcInvocation);
                                RpcProtocol respRpcProtocol = new RpcProtocol(SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
                                serverChannelReadData.getChannelHandlerContext().writeAndFlush(respRpcProtocol);
                        }
                    });
                } catch (Exception e){
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
