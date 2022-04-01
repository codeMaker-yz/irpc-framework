package idea.irpc.framework.core.filter;

import idea.irpc.framework.core.common.ChannelFutureWrapper;
import idea.irpc.framework.core.common.RpcInvocation;

import java.util.List;

/**
 * �ͻ��˵Ĺ�����
 *
 * @author ��Mr.Zhang
 * @date ��Created in 2022/4/1 12:38
 */
public interface IClientFilter extends IFilter{
    /**
     * ִ�й�����
     * @param src
     * @param rpcInvocation
     */
    void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation);
}
