package idea.irpc.framework.core.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import static idea.irpc.framework.core.common.constants.RpcConstants.MAGIC_NUMBER;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/3 19:07
 */
@Slf4j
public class RpcDecoder extends ByteToMessageDecoder {

    /**
     * 协议开头部分的标准长度
     */
    public final int BASE_LENGTH = 2 + 4;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //log.info("decoding..........");
        if(byteBuf.readableBytes() >= BASE_LENGTH){
            if(!(byteBuf.readShort() == MAGIC_NUMBER)){
                channelHandlerContext.close();
                return;
            }
            int length = byteBuf.readInt();
            if(byteBuf.readableBytes() < length){
                channelHandlerContext.close();
                return;
            }
            byte[] body = new byte[length];
            byteBuf.readBytes(body);
            RpcProtocol rpcProtocol = new RpcProtocol(body);
            list.add(rpcProtocol);
        }
    }
}
