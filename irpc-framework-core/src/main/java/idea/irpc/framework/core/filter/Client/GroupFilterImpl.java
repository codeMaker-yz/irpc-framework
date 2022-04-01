package idea.irpc.framework.core.filter.Client;

import idea.irpc.framework.core.common.ChannelFutureWrapper;
import idea.irpc.framework.core.common.RpcInvocation;
import idea.irpc.framework.core.common.utils.CommonUtils;
import idea.irpc.framework.core.filter.IClientFilter;

import java.util.List;

/**
 * 服务分组过滤器
 * @author ：Mr.Zhang
 * @date ：Created in 2022/4/1 13:06
 */
public class GroupFilterImpl implements IClientFilter {
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        String group = String.valueOf(rpcInvocation.getAttachments().get("group"));
        for (ChannelFutureWrapper channelFutureWrapper : src){
            if(!channelFutureWrapper.getGroup().equals(group)){
                src.remove(channelFutureWrapper);
            }
        }
        if(CommonUtils.isEmptyList(src)){
            throw new RuntimeException("no provider match for group " + group);
        }
    }
}
