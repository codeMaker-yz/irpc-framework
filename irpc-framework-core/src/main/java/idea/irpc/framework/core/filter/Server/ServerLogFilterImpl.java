package idea.irpc.framework.core.filter.Server;

import idea.irpc.framework.core.common.RpcInvocation;
import idea.irpc.framework.core.filter.Client.ClientLogFilterImpl;
import idea.irpc.framework.core.filter.IServerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static idea.irpc.framework.core.common.cache.CommonClientCache.CLIENT_CONFIG;

/**
 * 服务端日志过滤器
 * @author ：Mr.Zhang
 * @date ：Created in 2022/4/1 13:14
 */
public class ServerLogFilterImpl implements IServerFilter {
    private static Logger logger = LoggerFactory.getLogger(ServerLogFilterImpl.class);
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        logger.info(rpcInvocation.getAttachments().get("c_app_name") + "do invoke ----->" + rpcInvocation.getTargetServiceName() + "#" + rpcInvocation.getTargetMethod());
    }
}
