package idea.irpc.framework.core.client;

import com.alibaba.fastjson.JSON;
import idea.irpc.framework.core.common.RpcInvocation;
import idea.irpc.framework.core.common.RpcProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import static idea.irpc.framework.core.common.cache.CommonClientCache.RESP_MAP;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/5 12:41
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcProtocol rpcProtocol = (RpcProtocol) msg;
        byte[] reqContent = rpcProtocol.getContent();
        String json = new String(reqContent,0,reqContent.length);
        RpcInvocation rpcInvocation = JSON.parseObject(json, RpcInvocation.class);
        //通过之前发送的uuid来注入匹配的响应数值
        if(!RESP_MAP.containsKey(rpcInvocation.getUuid())){
            throw new IllegalArgumentException("server response is error!");
        }
        RESP_MAP.put(rpcInvocation.getUuid(),rpcInvocation);
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if(channel.isActive()){
            ctx.close();
        }
    }
}