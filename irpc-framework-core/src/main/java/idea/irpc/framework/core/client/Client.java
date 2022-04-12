package idea.irpc.framework.core.client;

import idea.irpc.framework.core.common.RpcDecoder;
import idea.irpc.framework.core.common.RpcEncoder;
import idea.irpc.framework.core.common.RpcInvocation;
import idea.irpc.framework.core.common.RpcProtocol;
import idea.irpc.framework.core.common.config.ClientConfig;
import idea.irpc.framework.core.common.config.PropertiesBootstrap;
import idea.irpc.framework.core.common.event.IRpcListenerLoader;
import idea.irpc.framework.core.common.utils.CommonUtils;
import idea.irpc.framework.core.filter.client.ClientFilterChain;
import idea.irpc.framework.core.filter.IClientFilter;
import idea.irpc.framework.core.proxy.ProxyFactory;
import idea.irpc.framework.core.registy.RegistryService;
import idea.irpc.framework.core.registy.URL;
import idea.irpc.framework.core.registy.zookeeper.AbstractRegister;
import idea.irpc.framework.core.router.IRouter;
import idea.irpc.framework.core.serialize.SerializeFactory;
import idea.irpc.framework.interfaces.DataService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import com.alibaba.fastjson.JSON;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static idea.irpc.framework.core.common.cache.CommonClientCache.*;
import static idea.irpc.framework.core.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/3 19:36
 */

@Slf4j
public class Client {

    private Logger logger = LoggerFactory.getLogger(Client.class);

    public static EventLoopGroup clientGroup = new NioEventLoopGroup();

    private ClientConfig clientConfig;

    private IRpcListenerLoader iRpcListenerLoader;

    private Bootstrap bootstrap = new Bootstrap();

    public Bootstrap getBootstrap(){
        return bootstrap;
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public RpcReference initClientApplication() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        EventLoopGroup clientGroup = new NioEventLoopGroup();

        bootstrap.group(clientGroup);

        //客户端channel实现
        bootstrap.channel(NioSocketChannel.class);

        bootstrap.handler(
                //连接建立后，被调用
                new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        log.info("client Init provider........." + System.currentTimeMillis());
                        ch.pipeline().addLast(new RpcEncoder());
                        ch.pipeline().addLast(new RpcDecoder());
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });
        iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();
        this.clientConfig = PropertiesBootstrap.loadClientConfigFromLocal();
        CLIENT_CONFIG = this.clientConfig;
        //spi扩展的加载部分
        this.initClientConfig();
        EXTENSION_LOADER.loadExtension(ProxyFactory.class);
        String proxyType = clientConfig.getProxyType();
        LinkedHashMap<String, Class> classMap = EXTENSION_LOADER_CLASS_CACHE.get(ProxyFactory.class.getName());
        Class proxyClassType = classMap.get(proxyType);
        ProxyFactory proxyFactory = (ProxyFactory)proxyClassType.newInstance();
        return new RpcReference(proxyFactory);
    }

    /**
     * 启动服务前需要预先订阅对应的dubbo服务
     * @param serviceBean
     */
    public void doSubscribeService(Class serviceBean){
        if(ABSTRACT_REGISTER == null){
            try {
                EXTENSION_LOADER.loadExtension(RegistryService.class);
                Map<String,Class> registerMap = EXTENSION_LOADER_CLASS_CACHE.get(RegistryService.class.getName());
                Class registerClass =  registerMap.get(clientConfig.getRegisterType());
                ABSTRACT_REGISTER = (AbstractRegister) registerClass.newInstance();
            } catch (Exception e){
                throw new RuntimeException("registryServiceType unKnow,error is ", e);
            }
        }
        URL url = new URL();
        url.setApplicationName(clientConfig.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameter("host", CommonUtils.getIpAddress());
        Map<String, String> result = ABSTRACT_REGISTER.getServiceWeightMap(serviceBean.getName());
        URL_MAP.put(serviceBean.getName(),result);
        ABSTRACT_REGISTER.subscribe(url);
    }

    /**
     * 开始和各个provider建立连接
     */
    public void doConnectServer() {
        for (URL providerURL : SUBSCRIBE_SERVICE_LIST) {
            List<String> providerIps = ABSTRACT_REGISTER.getProviderIps(providerURL.getServiceName());
            for (String providerIp : providerIps) {
                try {
                    ConnectionHandler.connect(providerURL.getServiceName(), providerIp);
                } catch (InterruptedException e) {
                    logger.error("[doConnectServer] connect fail ", e);
                }
            }
            URL url = new URL();
            url.addParameter("servicePath", providerURL.getServiceName()+"/provider");
            url.addParameter("providerIps", JSON.toJSONString(providerIps));
            //客户端在此新增一个订阅的功能
            ABSTRACT_REGISTER.doAfterSubscribe(url);
        }
    }

    /**
     * 开启发送线程
     *
     * @param
     */
    public void startClient() {
        Thread asyncSendJob = new Thread(new AsyncSendJob());
        asyncSendJob.start();
    }

    class AsyncSendJob implements Runnable {

        public AsyncSendJob() {
        }

        @Override
        public void run() {
            while (true) {
                try {
                    //阻塞模式
                    RpcInvocation rpcInvocation = SEND_QUEUE.take();
                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(rpcInvocation);
                    if(channelFuture != null){
                        RpcProtocol rpcProtocol = new RpcProtocol(CLIENT_SERIALIZE_FACTORY.serialize(rpcInvocation));
                        channelFuture.channel().writeAndFlush(rpcProtocol);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 使用spi机制，更新代码
     */
    private void initClientConfig() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        //初始化路由策略
        EXTENSION_LOADER.loadExtension(IRouter.class);
        String routerStrategy = clientConfig.getRouterStrategy();
        LinkedHashMap<String, Class> iRouterMap = EXTENSION_LOADER_CLASS_CACHE.get(IRouter.class.getName());
        Class iRouterClass = iRouterMap.get(routerStrategy);
        if(iRouterClass == null){
            throw new RuntimeException("no match routerStrategy for " + routerStrategy);
        }
        IROUTER = (IRouter)iRouterClass.newInstance();

        //初始化序列化框架
        EXTENSION_LOADER.loadExtension(SerializeFactory.class);
        String clientSerialize = clientConfig.getClientSerialize();
        LinkedHashMap<String, Class> serializeMap = EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        Class clientSerializeClass = serializeMap.get(clientSerialize);
        if(clientSerializeClass == null){
            throw new RuntimeException("no match serialize type for " + clientSerialize);
        }
        CLIENT_SERIALIZE_FACTORY = (SerializeFactory)clientSerializeClass.newInstance();

        //初始化客户端过滤链路
        EXTENSION_LOADER.loadExtension(IClientFilter.class);
        ClientFilterChain clientFilterChain = new ClientFilterChain();
        LinkedHashMap<String, Class> iClientMap = EXTENSION_LOADER_CLASS_CACHE.get(IClientFilter.class.getName());
        for(String implClassName : iClientMap.keySet()){
            Class iClientFilterClass = iClientMap.get(implClassName);
            if(iClientFilterClass == null){
                throw new RuntimeException("no match iClientFilter for " + iClientFilterClass);
            }
            clientFilterChain.addClientFilter((IClientFilter) iClientFilterClass.newInstance());
        }
        CLIENT_FILTER_CHAIN = clientFilterChain;

    }

    public static void main(String[] args) throws Throwable {
        Client client = new Client();
        RpcReference rpcReference = client.initClientApplication();
        RpcReferenceWrapper<DataService> rpcReferenceWrapper = new RpcReferenceWrapper<>();
        rpcReferenceWrapper.setAimClass(DataService.class);
        rpcReferenceWrapper.setGroup("dev");
        rpcReferenceWrapper.setServiceToken("token-a");
//        rpcReferenceWrapper.setUrl("localhost:9093");
        DataService dataService = rpcReference.get(rpcReferenceWrapper);
        client.doSubscribeService(DataService.class);
        ConnectionHandler.setBootstrap(client.getBootstrap());
        client.doConnectServer();
        client.startClient();
        for (int i = 0; i < 5; i++) {
            try {
                String result = dataService.sendData("test");
                System.out.println(result);
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
