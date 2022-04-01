package idea.irpc.framework.core.filter.Client;

import idea.irpc.framework.core.common.ChannelFutureWrapper;
import idea.irpc.framework.core.common.RpcInvocation;
import idea.irpc.framework.core.filter.IClientFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author £ºMr.Zhang
 * @date £ºCreated in 2022/4/1 12:45
 */
public class ClientFilterChain {
    private static List<IClientFilter> iClientFilters = new ArrayList<>();

    public void addClientFilter(IClientFilter iClientFilter){
        iClientFilters.add(iClientFilter);
    }

    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation){
        for (IClientFilter filter :
                iClientFilters) {
            filter.doFilter(src, rpcInvocation);
        }
    }
}
