package idea.irpc.framework.core.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import static idea.irpc.framework.core.common.constants.RpcConstants.MAGIC_NUMBER;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/3 19:07
 */
public class RpcDecoder extends ByteToMessageDecoder {

    /**
     * 协议开头部分的标准长度
     */
    public final int BASE_LENGTH = 2 + 4;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if(byteBuf.readableBytes() >= BASE_LENGTH){
            if(byteBuf.readableBytes() > 4056){
                byteBuf.skipBytes(byteBuf.readableBytes());
            }
            int beginReader;
            while (true){
                beginReader = byteBuf.readerIndex();
                byteBuf.markReaderIndex();
                if(byteBuf.readShort() == MAGIC_NUMBER){
                    break;
                }
                byteBuf.resetReaderIndex();
                byteBuf.readByte();

                if(byteBuf.readInt() < BASE_LENGTH){
                    return;
                }
            }

            int length = byteBuf.readInt();
            if(byteBuf.readableBytes() < length){
                byteBuf.readerIndex(beginReader);
                return;
            }
            byte[] data = new byte[length];
            byteBuf.readBytes(data);
            RpcProtocol rpcProtocol = new RpcProtocol(data);
            list.add(rpcProtocol);
        }
    }
}
