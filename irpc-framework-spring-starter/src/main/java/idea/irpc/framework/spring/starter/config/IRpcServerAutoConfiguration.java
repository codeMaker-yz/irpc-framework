package idea.irpc.framework.spring.starter.config;

import idea.irpc.framework.core.common.event.IRpcListenerLoader;
import idea.irpc.framework.core.server.ApplicationShutdownHook;
import idea.irpc.framework.core.server.Server;
import idea.irpc.framework.core.server.ServiceWrapper;
import idea.irpc.framework.spring.starter.common.IRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * @author £ºMr.Zhang
 * @date £ºCreated in 2022/4/13 10:31
 */
public class IRpcServerAutoConfiguration implements InitializingBean, ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(IRpcServerAutoConfiguration.class);

    private ApplicationContext applicationContext;
    @Override
    public void afterPropertiesSet() throws Exception {
        Server server = null;
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(IRpcService.class);
        if(beanMap.size() == 0){
            return;
        }
        printBanner();
        long begin = System.currentTimeMillis();
        server = new Server();
        server.initServerConfig();
        IRpcListenerLoader iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();
        for (String beanName : beanMap.keySet()){
            Object bean = beanMap.get(beanName);
            IRpcService iRpcService = bean.getClass().getAnnotation(IRpcService.class);
            ServiceWrapper dataServiceWrapper = new ServiceWrapper(bean, iRpcService.group());
            dataServiceWrapper.setServiceToken(iRpcService.serviceToken());
            dataServiceWrapper.setLimit(iRpcService.limit());
            server.exportService(dataServiceWrapper);
            LOGGER.info(">>>>>>>>>>>>>>> [irpc] {} export success! >>>>>>>>>>>>>>> ",beanName);
        }
        long end = System.currentTimeMillis();
        ApplicationShutdownHook.registryShutdownHook();
        server.startApplication();
        LOGGER.info(" ================== [{}] started success in {}s ================== ",server.getServerConfig().getApplicationName(),((double)end-(double)begin)/1000);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void printBanner(){
        System.out.println();
        System.out.println("==============================================");
        System.out.println("|||---------- IRpc Starting Now! ----------|||");
        System.out.println("==============================================");
        System.out.println("GO!GO!GO!");
        System.out.println("version: 1.0.0");
        System.out.println();
    }
}
