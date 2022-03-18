import idea.irpc.framework.core.client.ClientHandler;
import idea.irpc.framework.core.common.RpcDecoder;
import idea.irpc.framework.core.common.RpcEncoder;
import idea.irpc.framework.core.common.RpcProtocol;
import idea.irpc.framework.core.registy.zookeeper.ZookeeperRegister;
import io.netty.channel.embedded.EmbeddedChannel;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

/**
 * @author £ºMr.Zhang
 * @date £ºCreated in 2022/3/9 15:56
 */
public class TestRpcProtocol {

    @Test
    public void testRpc(){
        EmbeddedChannel channel = new EmbeddedChannel(
                new RpcEncoder(),
                new RpcDecoder(),
                new ClientHandler()
        );
        // encode
        RpcProtocol rpcProtocol = new RpcProtocol(new byte[]{65,66,67,68,69,70,71});
        boolean b = channel.writeOutbound(rpcProtocol);
        System.out.println(b);

        // decode
        channel.writeInbound();

    }

    @Test
    public void testZK() throws Exception {

    }


}
