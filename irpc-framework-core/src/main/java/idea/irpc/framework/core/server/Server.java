package idea.irpc.framework.core.server;

import idea.irpc.framework.core.common.RpcDecoder;
import idea.irpc.framework.core.common.RpcEncoder;
import idea.irpc.framework.core.common.config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import static idea.irpc.framework.core.common.cache.CommonServerCache.PROVIDER_CLASS_MAP;


public class Server {
    private static EventLoopGroup bossGroup = null;

    private static EventLoopGroup workerGroup = null;

    private ServerConfig serverConfig;

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }


    public void startApplication() throws InterruptedException {

        //3.boos负责处理连接 worker负责处理读写
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        //1.启动器，负责组装netty组件，启动服务器
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);

        //2.选择服务器的ServerSocketChannel实现
        bootstrap.channel(NioServerSocketChannel.class);

        bootstrap.option(ChannelOption.TCP_NODELAY, true);

        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);

        bootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.SO_KEEPALIVE, true);

        bootstrap.childHandler(
                //4.channel代表和客户端进行读写的通道Initializer初始化
                new ChannelInitializer<SocketChannel>() {
                    @Override
                    //连接建立后，调用初始化方法
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        System.out.println("初始化provider过程");
                        socketChannel.pipeline().addLast(new RpcEncoder());
                        socketChannel.pipeline().addLast(new RpcDecoder());
                        socketChannel.pipeline().addLast(new ServerHandler());
                    }
                });
        //5.绑定监听端口
        bootstrap.bind(serverConfig.getPort()).sync();

    }

    public void registerService(Object serviceBean) {
        if (serviceBean.getClass().getInterfaces().length == 0) {
            throw new RuntimeException("service must had interfaces!");
        }
        Class[] classes = serviceBean.getClass().getInterfaces();
        if (classes.length > 1) {
            throw new RuntimeException("service must only had one interfaces!");
        }
        Class interfaceClass = classes[0];
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
    }


    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(9090);
        server.setServerConfig(serverConfig);
        server.registerService(new DataServiceImpl());
        server.startApplication();
    }
}
