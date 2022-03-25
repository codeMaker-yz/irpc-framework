package idea.irpc.framework.core.router;

import idea.irpc.framework.core.common.ChannelFutureWrapper;
import idea.irpc.framework.core.registy.URL;

import java.util.List;

import static idea.irpc.framework.core.common.cache.CommonClientCache.*;

/**
 * @author £ºMr.Zhang
 * @date £ºCreated in 2022/3/22 19:31
 */
public class RotateRouterImpl implements IRouter{
    @Override
    public void refreshRouterArr(Selector selector) {
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(selector.getProviderServiceName());
        ChannelFutureWrapper[] arr = new ChannelFutureWrapper[channelFutureWrappers.size()];
        for (int i=0;i<channelFutureWrappers.size();i++) {
            arr[i]=channelFutureWrappers.get(i);
        }
        SERVICE_ROUTER_MAP.put(selector.getProviderServiceName(),arr);
    }

    @Override
    public ChannelFutureWrapper select(Selector selector) {
        return CHANNEL_FUTURE_POLLING_REF.getChannelFutureWrapper(selector.getProviderServiceName());
    }

    @Override
    public void updateWeight(URL url) {

    }
}
