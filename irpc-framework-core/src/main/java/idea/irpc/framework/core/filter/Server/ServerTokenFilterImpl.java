package idea.irpc.framework.core.filter.Server;

import idea.irpc.framework.core.common.RpcInvocation;
import idea.irpc.framework.core.common.utils.CommonUtils;
import idea.irpc.framework.core.filter.IServerFilter;
import idea.irpc.framework.core.server.ServiceWrapper;

import static idea.irpc.framework.core.common.cache.CommonServerCache.PROVIDER_SERVICE_WRAPPER_MAP;

/**
 * 简单的token校验
 * @author ：Mr.Zhang
 * @date ：Created in 2022/4/1 13:17
 */
public class ServerTokenFilterImpl implements IServerFilter {
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String token = String.valueOf(rpcInvocation.getAttachments().get("serviceToken"));
        ServiceWrapper serviceWrapper = PROVIDER_SERVICE_WRAPPER_MAP.get(rpcInvocation.getTargetServiceName());
        String matchToken = String.valueOf(serviceWrapper.getServiceToken());
        if(CommonUtils.isEmpty(matchToken)){
            return;
        }
        if(!CommonUtils.isEmpty(matchToken) && token.equals(matchToken)){
            return;
        }
        throw new RuntimeException("token is " + token + " , verify result is false!");
    }
}
