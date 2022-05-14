package idea.irpc.framework.core.filter.server;

import idea.irpc.framework.core.annotations.SPI;
import idea.irpc.framework.core.common.RpcInvocation;
import idea.irpc.framework.core.filter.IServerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端日志过滤器
 * @author ：Mr.Zhang
 * @date ：Created in 2022/4/1 13:14
 */
@SPI("before")
public class ServerLogFilterImpl implements IServerFilter {
    private static Logger logger = LoggerFactory.getLogger(ServerLogFilterImpl.class);
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        logger.debug(rpcInvocation.getAttachments().get("c_app_name") + "do invoke ----->" + rpcInvocation.getTargetServiceName() + "#" + rpcInvocation.getTargetMethod());
    }
}
