package idea.irpc.framework.core.common;

import java.util.concurrent.atomic.AtomicLong;

import static idea.irpc.framework.core.common.cache.CommonClientCache.SERVICE_ROUTER_MAP;

/**
 * @author £ºMr.Zhang
 * @date £ºCreated in 2022/3/19 16:38
 */
public class ChannelFuturePollingRef {
    private AtomicLong referenceTimes = new AtomicLong(0);

    public ChannelFutureWrapper getChannelFutureWrapper(String serviceName){
        ChannelFutureWrapper[] arr = SERVICE_ROUTER_MAP.get(serviceName);
        long i = referenceTimes.getAndIncrement();
        int index = (int) (i % arr.length);
        return arr[index];
    }
}
