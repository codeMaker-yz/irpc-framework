package idea.irpc.framework.core.server;

import idea.irpc.framework.core.common.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author £ºMr.Zhang
 * @date £ºCreated in 2022/4/12 16:10
 */
public class ServerChannelReadData {

    private ChannelHandlerContext channelHandlerContext;

    private RpcProtocol rpcProtocol;

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

    public RpcProtocol getRpcProtocol() {
        return rpcProtocol;
    }

    public void setRpcProtocol(RpcProtocol rpcProtocol) {
        this.rpcProtocol = rpcProtocol;
    }
}
