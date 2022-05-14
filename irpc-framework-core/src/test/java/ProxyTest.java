import idea.irpc.framework.core.proxy.ProxyFactory;
import idea.irpc.framework.core.proxy.jdk.JDKProxyFactory;
import idea.irpc.framework.core.server.DataServiceImpl;
import idea.irpc.framework.interfaces.DataService;
import io.netty.util.internal.SystemPropertyUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author £ºMr.Zhang
 * @date £ºCreated in 2022/3/18 16:19
 */
public class ProxyTest {
    @Test
    public void test() throws Throwable {
        System.out.println(SystemPropertyUtil.getInt("io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));

    }

}
