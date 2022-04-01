package idea.irpc.framework.core.filter;

import idea.irpc.framework.core.common.RpcInvocation;

/**
 * 服务端的过滤器
 *
 * @author ：Mr.Zhang
 * @date ：Created in 2022/4/1 12:41
 */
public interface IServerFilter extends IFilter{

    /**
     * 执行核心过滤逻辑
     *
     * @param rpcInvocation
     */
    void doFilter(RpcInvocation rpcInvocation);
}
