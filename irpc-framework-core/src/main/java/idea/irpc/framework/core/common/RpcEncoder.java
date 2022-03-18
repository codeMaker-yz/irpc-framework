package idea.irpc.framework.core.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/3 19:07
 */
@Slf4j
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcProtocol msg, ByteBuf byteBuf) throws Exception {

        log.info("encoding..........");
        byteBuf.writeShort(msg.getMagicNumber());
        byteBuf.writeInt(msg.getContentLength());
        byteBuf.writeBytes(msg.getContent());
    }
}
