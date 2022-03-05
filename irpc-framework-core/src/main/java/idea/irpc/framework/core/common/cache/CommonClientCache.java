package idea.irpc.framework.core.common.cache;

import idea.irpc.framework.core.common.RpcInvocation;

import java.util.Map;
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
    public static Map<String,Object> RESP_MAP = new ConcurrentHashMap<>();
}
