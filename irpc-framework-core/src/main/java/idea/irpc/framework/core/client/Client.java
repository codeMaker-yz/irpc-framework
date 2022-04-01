package idea.irpc.framework.core.client;


import idea.irpc.framework.core.common.RpcDecoder;
import idea.irpc.framework.core.common.RpcEncoder;
import idea.irpc.framework.core.common.RpcInvocation;
import idea.irpc.framework.core.common.RpcProtocol;
import idea.irpc.framework.core.common.config.ClientConfig;
import idea.irpc.framework.core.common.config.PropertiesBootstrap;
import idea.irpc.framework.core.common.event.IRpcListenerLoader;
import idea.irpc.framework.core.common.utils.CommonUtils;
import idea.irpc.framework.core.filter.Client.ClientFilterChain;
import idea.irpc.framework.core.filter.Client.ClientLogFilterImpl;
import idea.irpc.framework.core.filter.Client.DirectInvokeFilterImpl;
import idea.irpc.framework.core.filter.Client.GroupFilterImpl;
import idea.irpc.framework.core.proxy.jdk.JDKProxyFactory;
import idea.irpc.framework.core.registy.URL;
import idea.irpc.framework.core.registy.zookeeper.AbstractRegister;
import idea.irpc.framework.core.registy.zookeeper.ZookeeperRegister;
import idea.irpc.framework.core.router.RandomRouterImpl;
import idea.irpc.framework.core.router.RotateRouterImpl;
import idea.irpc.framework.core.serialize.fastjson.FastJsonSerializeFactory;
import idea.irpc.framework.core.serialize.hessian.HessianSerializeFactory;
import idea.irpc.framework.core.serialize.jdk.JdkSerializeFactory;
import idea.irpc.framework.core.serialize.kryo.KryoSerializeFactory;
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

import java.util.List;
import java.util.Map;

import static idea.irpc.framework.core.common.cache.CommonClientCache.*;
import static idea.irpc.framework.core.common.constants.RpcConstants.*;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/3 19:36
 */

@Slf4j
public class Client {

    private Logger logger = LoggerFactory.getLogger(Client.class);

    public static EventLoopGroup clientGroup = new NioEventLoopGroup();

    private ClientConfig clientConfig;

    private AbstractRegister abstractRegister;

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

    public RpcReference initClientApplication(){
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
        RpcReference rpcReference = new RpcReference(new JDKProxyFactory());
        return rpcReference;
    }

    /**
     * 启动服务前需要预先订阅对应的dubbo服务
     * @param serviceBean
     */
    public void doSubscribeService(Class serviceBean){
        if(abstractRegister == null){
            abstractRegister = new ZookeeperRegister(clientConfig.getRegisterAddr());
        }
        URL url = new URL();
        url.setApplicationName(clientConfig.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameter("host", CommonUtils.getIpAddress());
        Map<String, String> result = abstractRegister.getServiceWeightMap(serviceBean.getName());
        URL_MAP.put(serviceBean.getName(),result);
        abstractRegister.subscribe(url);
    }

    /**
     * 开始和各个provider建立连接
     */
    public void doConnectServer() {
        for (URL providerURL : SUBSCRIBE_SERVICE_LIST) {
            List<String> providerIps = abstractRegister.getProviderIps(providerURL.getServiceName());
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
            abstractRegister.doAfterSubscribe(url);
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
     * todo
     * 后续可以考虑加入spi
     */
    private void initClientConfig() {
        //初始化路由策略
        String routerStrategy = clientConfig.getRouterStrategy();
        switch (routerStrategy){
            case RANDOM_ROUTER_TYPE:
                IROUTER = new RandomRouterImpl();
                break;
            case ROTATE_ROUTER_TYPE:
                IROUTER = new RotateRouterImpl();
                break;
            default:
                throw new RuntimeException("no match routeStrategy for " + routerStrategy);
        }
        String clientSerialize = clientConfig.getClientSerialize();
        switch (clientSerialize){
            case JDK_SERIALIZE_TYPE:
                CLIENT_SERIALIZE_FACTORY = new JdkSerializeFactory();
                break;
            case FAST_JSON_SERIALIZE_TYPE:
                CLIENT_SERIALIZE_FACTORY = new FastJsonSerializeFactory();
                break;
            case HESSIAN2_SERIALIZE_TYPE:
                CLIENT_SERIALIZE_FACTORY = new HessianSerializeFactory();
                break;
            case KRYO_SERIALIZE_TYPE:
                CLIENT_SERIALIZE_FACTORY = new KryoSerializeFactory();
                break;
            default:
                throw new RuntimeException("no match serialize type for " + clientSerialize);
        }
        //todo 初始化过滤链 指定过滤顺序
        ClientFilterChain clientFilterChain = new ClientFilterChain();
        clientFilterChain.addClientFilter(new DirectInvokeFilterImpl());
        clientFilterChain.addClientFilter(new GroupFilterImpl());
        clientFilterChain.addClientFilter(new ClientLogFilterImpl());
        CLIENT_FILTER_CHAIN = clientFilterChain;

    }

    public static void main(String[] args) throws Throwable {
        Client client = new Client();
        RpcReference rpcReference = client.initClientApplication();
        client.initClientConfig();
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
