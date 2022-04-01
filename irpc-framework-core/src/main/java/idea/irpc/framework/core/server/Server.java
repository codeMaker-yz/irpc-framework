package idea.irpc.framework.core.server;

import idea.irpc.framework.core.common.RpcDecoder;
import idea.irpc.framework.core.common.RpcEncoder;
import idea.irpc.framework.core.common.config.PropertiesBootstrap;
import idea.irpc.framework.core.common.config.ServerConfig;
import idea.irpc.framework.core.common.event.IRpcListenerLoader;
import idea.irpc.framework.core.common.utils.CommonUtils;
import idea.irpc.framework.core.filter.Server.ServerFilterChain;
import idea.irpc.framework.core.filter.Server.ServerLogFilterImpl;
import idea.irpc.framework.core.filter.Server.ServerTokenFilterImpl;
import idea.irpc.framework.core.registy.RegistryService;
import idea.irpc.framework.core.registy.URL;
import idea.irpc.framework.core.registy.zookeeper.ZookeeperRegister;
import idea.irpc.framework.core.serialize.fastjson.FastJsonSerializeFactory;
import idea.irpc.framework.core.serialize.hessian.HessianSerializeFactory;
import idea.irpc.framework.core.serialize.jdk.JdkSerializeFactory;
import idea.irpc.framework.core.serialize.kryo.KryoSerializeFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import static idea.irpc.framework.core.common.cache.CommonServerCache.*;
import static idea.irpc.framework.core.common.constants.RpcConstants.*;
import static idea.irpc.framework.core.common.constants.RpcConstants.KRYO_SERIALIZE_TYPE;


public class Server {
    private static EventLoopGroup bossGroup = null;

    private static EventLoopGroup workerGroup = null;

    private ServerConfig serverConfig;

    private static IRpcListenerLoader iRpcListenerLoader;


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
                        System.out.println("server Init provider........."+ System.currentTimeMillis());
                        socketChannel.pipeline().addLast(new RpcEncoder());
                        socketChannel.pipeline().addLast(new RpcDecoder());
                        socketChannel.pipeline().addLast(new ServerHandler());
                    }
                });
        //5.绑定监听端口
        this.batchExportUrl();
        bootstrap.bind(serverConfig.getServerPort()).sync();

    }

    public void initServerConfig(){
        ServerConfig serverConfig = PropertiesBootstrap.loadServerConfigFromLocal();
        this.setServerConfig(serverConfig);
        String serverSerialize = serverConfig.getServerSerialize();
        switch (serverSerialize){
            case JDK_SERIALIZE_TYPE:
                SERVER_SERIALIZE_FACTORY = new JdkSerializeFactory();
                break;
            case FAST_JSON_SERIALIZE_TYPE:
                SERVER_SERIALIZE_FACTORY = new FastJsonSerializeFactory();
                break;
            case HESSIAN2_SERIALIZE_TYPE:
                SERVER_SERIALIZE_FACTORY = new HessianSerializeFactory();
                break;
            case KRYO_SERIALIZE_TYPE:
                SERVER_SERIALIZE_FACTORY = new KryoSerializeFactory();
                break;
            default:
                throw new RuntimeException("no match serialize type for " + serverSerialize);
        }
        ServerFilterChain serverFilterChain = new ServerFilterChain();
        serverFilterChain.addServerFilter(new ServerLogFilterImpl());
        serverFilterChain.addServerFilter(new ServerTokenFilterImpl());
        SERVER_FILTER_CHAIN = serverFilterChain;
    }

    public void exportService(ServiceWrapper serviceWrapper){
        Object serviceBean = serviceWrapper.getServiceObj();
        if(serviceBean.getClass().getInterfaces().length == 0){
            throw new RuntimeException("service must had interfaces");
        }
        Class[] classes = serviceBean.getClass().getInterfaces();
        if(classes.length > 1){
            throw new RuntimeException("service must only had one interfaces");
        }
        if(REGISTRY_SERVICE == null){
            REGISTRY_SERVICE = new ZookeeperRegister(serverConfig.getRegisterAddr());
        }
        //默认选择该对象的第一个实现接口
        Class interfaceClass = classes[0];
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
        URL url = new URL();
        url.setServiceName(interfaceClass.getName());
        url.setApplicationName(serverConfig.getApplicationName());
        url.addParameter("host", CommonUtils.getIpAddress());
        url.addParameter("port", String.valueOf(serverConfig.getServerPort()));
        url.addParameter("group",String.valueOf(serviceWrapper.getGroup()));
        PROVIDER_URL_SET.add(url);

    }

    public void batchExportUrl(){
        Thread task = new Thread(() -> {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (URL url : PROVIDER_URL_SET) {
                REGISTRY_SERVICE.register(url);
            }
        });
        task.start();
    }


    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        server.initServerConfig();
        iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();
        server.exportService(new ServiceWrapper(new DataServiceImpl()));
        server.exportService(new ServiceWrapper(new UserServiceImpl()));
        ApplicationShutdownHook.registryShutdownHook();
        server.startApplication();
    }
}
