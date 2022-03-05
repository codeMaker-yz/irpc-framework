package idea.irpc.framework.core.client;


import idea.irpc.framework.core.common.RpcDecoder;
import idea.irpc.framework.core.common.RpcEncoder;
import idea.irpc.framework.core.common.RpcInvocation;
import idea.irpc.framework.core.common.RpcProtocol;
import idea.irpc.framework.core.common.config.ClientConfig;
import idea.irpc.framework.core.proxy.jdk.JDKProxyFactory;
import idea.irpc.framework.interfaces.DataService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import com.alibaba.fastjson.JSON;

import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static idea.irpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/3 19:36
 */
public class Client {

    private Logger logger = LoggerFactory.getLogger(Client.class);

    public static EventLoopGroup clientGroup = new NioEventLoopGroup();

    private ClientConfig clientConfig;

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public RpcReference startClientApplication() throws InterruptedException {
        EventLoopGroup clientGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup);

        //客户端channel实现
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(
                //连接建立后，被调用
                new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RpcEncoder());
                        ch.pipeline().addLast(new RpcDecoder());
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });

        //连接到服务器，sync()是个阻塞方法，直到连接建立
        ChannelFuture channelFuture = bootstrap.connect(clientConfig.getServerAddr(), clientConfig.getPort()).sync();
        logger.info("===========服务启动==================");
        this.startClient(channelFuture);
        RpcReference rpcReference = new RpcReference(new JDKProxyFactory());
        return rpcReference;
    }

    public static void main(String[] args) throws Throwable {
        Client client = new Client();
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setPort(9090);
        clientConfig.setServerAddr("localhost");
        client.setClientConfig(clientConfig);
        RpcReference rpcReference = client.startClientApplication();
        DataService dataService = rpcReference.get(DataService.class);
        for (int i = 0; i < 100; i++) {
            String result = dataService.sendData("test");
            System.out.println(result);
        }

    }

    /**
     * 开启发送线程
     *
     * @param channelFuture
     */
    private void startClient(ChannelFuture channelFuture) {
        Thread asyncSendJob = new Thread(new AsyncSendJob(channelFuture));
        asyncSendJob.start();
    }

    class AsyncSendJob implements Runnable {

        private ChannelFuture channelFuture;

        public AsyncSendJob(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    //阻塞模式
                    RpcInvocation data = SEND_QUEUE.take();
                    String json = JSON.toJSONString(data);
                    RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes());
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
