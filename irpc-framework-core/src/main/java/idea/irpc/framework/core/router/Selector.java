package idea.irpc.framework.core.router;

import idea.irpc.framework.core.common.ChannelFutureWrapper;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/19 16:07
 */
public class Selector {
    /**
     * 服务命名
     * eg:com.test.DataService
     */
    private String providerServiceName;

    /**
     * 经过二次筛选之后的future集合
     */
    private ChannelFutureWrapper[] channelFutureWrappers;

    public String getProviderServiceName() {
        return providerServiceName;
    }

    public void setProviderServiceName(String providerServiceName) {
        this.providerServiceName = providerServiceName;
    }

    public ChannelFutureWrapper[] getChannelFutureWrappers() {
        return channelFutureWrappers;
    }

    public void setChannelFutureWrappers(ChannelFutureWrapper[] channelFutureWrappers) {
        this.channelFutureWrappers = channelFutureWrappers;
    }
}
