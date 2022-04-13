package idea.irpc.framework.spring.starter.config;

import idea.irpc.framework.core.client.Client;
import idea.irpc.framework.core.client.ConnectionHandler;
import idea.irpc.framework.core.client.RpcReference;
import idea.irpc.framework.core.client.RpcReferenceWrapper;
import idea.irpc.framework.spring.starter.common.IRpcReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.lang.reflect.Field;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/4/13 10:58
 */
public class IRpcClientAutoConfiguration implements BeanPostProcessor, ApplicationListener<ApplicationReadyEvent> {
    private static RpcReference rpcReference = null;
    private static Client client = null;
    private volatile boolean needInitClient = false;
    private volatile boolean hasInitClientConfig = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(IRpcClientAutoConfiguration.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields){
            if(field.isAnnotationPresent(IRpcReference.class)){
                if(!hasInitClientConfig){
                    //初始化客户端的配置
                    client = new Client();
                    try {
                        rpcReference = client.initClientApplication();
                    } catch (Exception e){
                        LOGGER.error("[IRpcClientAutoConfiguration] postProcessAfterInitialization has error ",e);
                        throw new RuntimeException(e);
                    }
                    hasInitClientConfig = true;
                }
                needInitClient = true;
                IRpcReference iRpcReference = field.getAnnotation(IRpcReference.class);
                try {
                    field.setAccessible(true);
                    Object refObj = field.get(bean);
                    RpcReferenceWrapper rpcReferenceWrapper = new RpcReferenceWrapper();
                    rpcReferenceWrapper.setAimClass(field.getType());
                    rpcReferenceWrapper.setGroup(iRpcReference.group());
                    rpcReferenceWrapper.setServiceToken(iRpcReference.serviceToken());
                    rpcReferenceWrapper.setUrl(iRpcReference.url());
                    rpcReferenceWrapper.setTimeOut(iRpcReference.timeOut());
                    rpcReferenceWrapper.setAsync(iRpcReference.async());
                    refObj = rpcReference.get(rpcReferenceWrapper);
                    field.set(bean, refObj);
                    client.doSubscribeService(field.getType());
                } catch (IllegalAccessException e){
                    e.printStackTrace();
                } catch (Throwable e){
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if (needInitClient && client!=null) {
            LOGGER.info(" ================== [{}] started success ================== ",client.getClientConfig().getApplicationName());
            ConnectionHandler.setBootstrap(client.getBootstrap());
            client.doConnectServer();
            client.startClient();
        }
    }
}
