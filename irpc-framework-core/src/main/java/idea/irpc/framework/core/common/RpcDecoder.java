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
        log.info("decoding..........");
        if(byteBuf.readableBytes() >= BASE_LENGTH){
            // 防止数据包过大
            if(byteBuf.readableBytes() > 4056){
                byteBuf.skipBytes(byteBuf.readableBytes());
            }
            int beginReader;
            while (true){
                beginReader = byteBuf.readerIndex();
                byteBuf.markReaderIndex();
                if(byteBuf.readShort() == MAGIC_NUMBER){
                    break;
                }else{
                    channelHandlerContext.close();
                    return;
                }
            }
            //对应RpcProtocol对象的contentLength字段
            int length = byteBuf.readInt();
            //如果数据包不是完整的，需要重置下读索引
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
