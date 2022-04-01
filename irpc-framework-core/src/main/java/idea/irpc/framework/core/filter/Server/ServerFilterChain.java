package idea.irpc.framework.core.filter.Server;

import idea.irpc.framework.core.common.RpcInvocation;
import idea.irpc.framework.core.filter.IServerFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author £ºMr.Zhang
 * @date £ºCreated in 2022/4/1 12:42
 */
public class ServerFilterChain {
    private static List<IServerFilter> iServerFilters = new ArrayList<>();

    public void addServerFilter(IServerFilter iServerFilter){
        iServerFilters.add(iServerFilter);
    }

    public void doFilter(RpcInvocation rpcInvocation){
        for (IServerFilter filter :
                iServerFilters) {
            filter.doFilter(rpcInvocation);
        }
    }
}
