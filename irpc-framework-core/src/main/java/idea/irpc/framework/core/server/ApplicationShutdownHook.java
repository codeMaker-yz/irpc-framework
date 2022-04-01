package idea.irpc.framework.core.server;

import idea.irpc.framework.core.common.event.IRpcDestroyEvent;
import idea.irpc.framework.core.common.event.IRpcListenerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/4/1 13:58
 */
public class ApplicationShutdownHook {
    public static Logger LOGGER = LoggerFactory.getLogger(ApplicationShutdownHook.class);

    /**
     * 注册一个shutdownHook的钩子，当jvm进程关闭时触发
     */
    public static void registryShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("[registryShutdownHook] ==== ");
                IRpcListenerLoader.sendSyncEvent(new IRpcDestroyEvent("destroy"));
                System.out.println("destroy");;
            }
        }));
    }
}
