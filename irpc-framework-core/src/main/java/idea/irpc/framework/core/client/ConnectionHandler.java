package idea.irpc.framework.core.client;

import idea.irpc.framework.core.common.ChannelFutureWrapper;
import idea.irpc.framework.core.common.utils.CommonUtils;
import idea.irpc.framework.core.router.Selector;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static idea.irpc.framework.core.common.cache.CommonClientCache.*;

/**
 * @author ��Mr.Zhang
 * @date ��Created in 2022/3/12 15:28
 */
@Slf4j
public class ConnectionHandler {
    /**
     * ���ĵ����Ӵ�����
     * ר�����ڸ���ͷ���˹�������ͨ��
     */
    private static Bootstrap bootstrap;

    public static void setBootstrap(Bootstrap bootstrap) {
        ConnectionHandler.bootstrap = bootstrap;
    }


    /**
     * ������������ͨ�� Ԫ��������Ҫ�������ӣ���Ҫͳһ�����ӽ����ڴ�洢����
     *
     * @param providerIp
     * @return
     * @throws InterruptedException
     */
    public static void connect(String providerServiceName, String providerIp) throws InterruptedException {
        if (bootstrap == null) {
            throw new RuntimeException("bootstrap can not be null");
        }
        //��ʽ�������͵���Ϣ
        if(!providerIp.contains(":")){
            return;
        }
        String[] providerAddress = providerIp.split(":");
        String ip = providerAddress[0];
        Integer port = Integer.parseInt(providerAddress[1]);
        //�������channelFuture������ʲô
        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
        String providerURLInfo = URL_MAP.get(providerServiceName).get(providerIp);
        log.info("providerURLInfo: " + providerURLInfo);
        ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
        channelFutureWrapper.setChannelFuture(channelFuture);
        channelFutureWrapper.setHost(ip);
        channelFutureWrapper.setPort(port);
        channelFutureWrapper.setWeight(Integer.valueOf(providerURLInfo.substring(providerURLInfo.lastIndexOf(";")+1)));
        SERVER_ADDRESS.add(providerIp);
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isEmptyList(channelFutureWrappers)) {
            channelFutureWrappers = new ArrayList<>();
        }
        channelFutureWrappers.add(channelFutureWrapper);
        CONNECT_MAP.put(providerServiceName, channelFutureWrappers);
        Selector selector = new Selector();
        selector.setProviderServiceName(providerServiceName);
        IROUTER.refreshRouterArr(selector);

    }

    /**
     * ����ChannelFuture
     * @param ip
     * @param port
     * @return
     * @throws InterruptedException
     */
    public static ChannelFuture createChannelFuture(String ip,Integer port) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
        return channelFuture;
    }

    /**
     * �Ͽ�����
     *
     * @param providerServiceName
     * @param providerIp
     */
    public static void disConnect(String providerServiceName, String providerIp) {
        SERVER_ADDRESS.remove(providerIp);
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isNotEmptyList(channelFutureWrappers)) {
            Iterator<ChannelFutureWrapper> iterator = channelFutureWrappers.iterator();
            while (iterator.hasNext()) {
                ChannelFutureWrapper channelFutureWrapper = iterator.next();
                if (providerIp.equals(channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort())) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Ĭ����������Ի�ȡChannelFuture
     *
     * @param providerServiceName
     * @return
     */
    public static ChannelFuture getChannelFuture(String providerServiceName) {
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isEmptyList(channelFutureWrappers)) {
            throw new RuntimeException("no provider exist for " + providerServiceName);
        }
        ChannelFuture channelFuture = channelFutureWrappers.get(new Random().nextInt(channelFutureWrappers.size())).getChannelFuture();
        return channelFuture;
    }

}