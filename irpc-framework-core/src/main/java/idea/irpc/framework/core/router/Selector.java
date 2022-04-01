package idea.irpc.framework.core.router;

import idea.irpc.framework.core.common.ChannelFutureWrapper;

/**
 * @author ��Mr.Zhang
 * @date ��Created in 2022/3/19 16:07
 */
public class Selector {
    /**
     * ��������
     * eg:com.test.DataService
     */
    private String providerServiceName;

    /**
     * ��������ɸѡ֮���future����
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
