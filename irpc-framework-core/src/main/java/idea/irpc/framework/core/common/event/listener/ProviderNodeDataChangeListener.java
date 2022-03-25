package idea.irpc.framework.core.common.event.listener;

import idea.irpc.framework.core.common.ChannelFutureWrapper;
import idea.irpc.framework.core.common.event.IRpcListener;
import idea.irpc.framework.core.common.event.IRpcNodeChangeEvent;
import idea.irpc.framework.core.registy.URL;
import idea.irpc.framework.core.registy.zookeeper.ProviderNodeInfo;

import java.util.List;

import static idea.irpc.framework.core.common.cache.CommonClientCache.CONNECT_MAP;
import static idea.irpc.framework.core.common.cache.CommonClientCache.IROUTER;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/22 19:28
 */
public class ProviderNodeDataChangeListener implements IRpcListener<IRpcNodeChangeEvent> {
    @Override
    public void callBack(Object t) {
        ProviderNodeInfo providerNodeInfo = ((ProviderNodeInfo) t);
        List<ChannelFutureWrapper> channelFutureWrappers =  CONNECT_MAP.get(providerNodeInfo.getServiceName());
        for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
            String address = channelFutureWrapper.getHost()+":"+channelFutureWrapper.getPort();
            if(address.equals(providerNodeInfo.getAddress())){
                //修改权重
                channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
                URL url = new URL();
                url.setServiceName(providerNodeInfo.getServiceName());
                //更新权重
                IROUTER.updateWeight(url);
                break;
            }
        }
    }
}
