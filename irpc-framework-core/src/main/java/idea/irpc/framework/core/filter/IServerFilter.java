package idea.irpc.framework.core.filter;

import idea.irpc.framework.core.common.RpcInvocation;

/**
 * ����˵Ĺ�����
 *
 * @author ��Mr.Zhang
 * @date ��Created in 2022/4/1 12:41
 */
public interface IServerFilter extends IFilter{

    /**
     * ִ�к��Ĺ����߼�
     *
     * @param rpcInvocation
     */
    void doFilter(RpcInvocation rpcInvocation);
}
