package idea.irpc.framework.core.server;

import idea.irpc.framework.core.common.RpcDecoder;
import idea.irpc.framework.core.common.RpcEncoder;
import idea.irpc.framework.core.common.config.PropertiesBootstrap;
import idea.irpc.framework.core.common.config.ServerConfig;
import idea.irpc.framework.core.common.event.IRpcListenerLoader;
import idea.irpc.framework.core.common.utils.CommonUtils;
import idea.irpc.framework.core.filter.IServerFilter;
import idea.irpc.framework.core.filter.server.ServerFilterChain;
import idea.irpc.framework.core.registy.RegistryService;
import idea.irpc.framework.core.registy.URL;
import idea.irpc.framework.core.registy.zookeeper.AbstractRegister;
import idea.irpc.framework.core.serialize.SerializeFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static idea.irpc.framework.core.common.cache.CommonClientCache.EXTENSION_LOADER;
import static idea.irpc.framework.core.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;
import static idea.irpc.framework.core.common.cache.CommonServerCache.*;



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
        SERVER_CHANNEL_DISPATCHER.startDataConsume();
        bootstrap.bind(serverConfig.getServerPort()).sync();
        IS_STARTED = true;
    }

    public void initServerConfig() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        ServerConfig serverConfig = PropertiesBootstrap.loadServerConfigFromLocal();
        this.setServerConfig(serverConfig);
        SERVER_CONFIG = serverConfig;
        //初始化线程池和队列的配置
        SERVER_CHANNEL_DISPATCHER.init(SERVER_CONFIG.getServerQueueSize(),SERVER_CONFIG.getServerBizThreadNums());
        //序列化技术初始化
        String serverSerialize = serverConfig.getServerSerialize();
        EXTENSION_LOADER.loadExtension(SerializeFactory.class);
        LinkedHashMap<String, Class> serializeFactoryClassMap = EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        Class serializeFactoryClass = serializeFactoryClassMap.get(serverSerialize);
        if(serializeFactoryClass == null){
            throw new RuntimeException("no match serialize type for " + serverSerialize);
        }
        SERVER_SERIALIZE_FACTORY = (SerializeFactory)serializeFactoryClass.newInstance();

        //过滤链技术初始化
        EXTENSION_LOADER.loadExtension(IServerFilter.class);
        LinkedHashMap<String, Class> iServerFilterClassMap = EXTENSION_LOADER_CLASS_CACHE.get(IServerFilter.class.getName());
        ServerFilterChain serverFilterChain = new ServerFilterChain();
        for (String iServerFilterKey : iServerFilterClassMap.keySet()) {
            Class iServerFilterClass = iServerFilterClassMap.get(iServerFilterKey);
            if(iServerFilterClass==null){
                throw new RuntimeException("no match iServerFilter type for " + iServerFilterKey);
            }
            serverFilterChain.addServerFilter((IServerFilter) iServerFilterClass.newInstance());
        }
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
            try {
                EXTENSION_LOADER.loadExtension(RegistryService.class);
                Map<String, Class> registryClassMap = EXTENSION_LOADER_CLASS_CACHE.get(RegistryService.class.getName());
                Class registryClass = registryClassMap.get(serverConfig.getRegisterType());
                REGISTRY_SERVICE = (AbstractRegister) registryClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("registryServiceType unKnow,error is ", e);
            }
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
        url.addParameter("limit",String.valueOf(serviceWrapper.getLimit()));
        PROVIDER_URL_SET.add(url);
        if(CommonUtils.isNotEmpty(serviceWrapper.getServiceToken())){
            PROVIDER_SERVICE_WRAPPER_MAP.put(interfaceClass.getName(), serviceWrapper);
        }

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


    public static void main(String[] args) throws InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        Server server = new Server();
        server.initServerConfig();
        iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();
        ServiceWrapper dataServiceWrapper = new ServiceWrapper(new DataServiceImpl(), "dev");
        dataServiceWrapper.setServiceToken("token-a");
        dataServiceWrapper.setLimit(2);
        ServiceWrapper userServiceWrapper = new ServiceWrapper(new UserServiceImpl(), "dev");
        userServiceWrapper.setServiceToken("token-b");
        userServiceWrapper.setLimit(2);
        server.exportService(dataServiceWrapper);
        server.exportService(userServiceWrapper);
        ApplicationShutdownHook.registryShutdownHook();
        server.startApplication();
    }
}
