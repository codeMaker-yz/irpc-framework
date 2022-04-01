package idea.irpc.framework.core.filter;

import idea.irpc.framework.core.common.ChannelFutureWrapper;
import idea.irpc.framework.core.common.RpcInvocation;

import java.util.List;

/**
 * 客户端的过滤器
 *
 * @author ：Mr.Zhang
 * @date ：Created in 2022/4/1 12:38
 */
public interface IClientFilter extends IFilter{
    /**
     * 执行过滤链
     * @param src
     * @param rpcInvocation
     */
    void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation);
}
