package idea.irpc.framework.core.common.cache;

import idea.irpc.framework.core.common.ChannelFuturePollingRef;
import idea.irpc.framework.core.common.ChannelFutureWrapper;
import idea.irpc.framework.core.common.RpcInvocation;
import idea.irpc.framework.core.common.config.ClientConfig;
import idea.irpc.framework.core.filter.Client.ClientFilterChain;
import idea.irpc.framework.core.registy.URL;
import idea.irpc.framework.core.router.IRouter;
import idea.irpc.framework.core.serialize.SerializeFactory;


import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 公用缓存，存储请求队列等公共信息
 *
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/5 10:49
 */
public class CommonClientCache {
    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue(100);
    public static Map<String, Object> RESP_MAP = new ConcurrentHashMap<>();
    public static ClientConfig CLIENT_CONFIG;
    //provider名称 --> 该服务有哪些集群URL
    public static List<URL> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();

    public static Map<String, Map<String,String>> URL_MAP = new ConcurrentHashMap<>();
    public static Set<String> SERVER_ADDRESS = new HashSet<>();
    //每次进行远程调用的时候都是从这里面去选择服务提供者
    public static Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();
    //随机请求的map
    public static Map<String, ChannelFutureWrapper[]> SERVICE_ROUTER_MAP = new ConcurrentHashMap<>();
    public static ChannelFuturePollingRef CHANNEL_FUTURE_POLLING_REF = new ChannelFuturePollingRef();
    public static IRouter IROUTER;
    public static SerializeFactory CLIENT_SERIALIZE_FACTORY;
    public static ClientFilterChain CLIENT_FILTER_CHAIN;
}
