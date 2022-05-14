package idea.irpc.framework.core.common.cache;

import idea.irpc.framework.core.common.ServerServiceSemaphoreWrapper;
import idea.irpc.framework.core.common.config.ServerConfig;
import idea.irpc.framework.core.dispatch.ServerChannelDispatcher;
import idea.irpc.framework.core.filter.server.ServerAfterFilterChain;
import idea.irpc.framework.core.filter.server.ServerBeforeFilterChain;
import idea.irpc.framework.core.filter.server.ServerFilterChain;
import idea.irpc.framework.core.registy.RegistryService;
import idea.irpc.framework.core.registy.URL;
import idea.irpc.framework.core.serialize.SerializeFactory;
import idea.irpc.framework.core.server.ServiceWrapper;
import io.netty.util.internal.ConcurrentSet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/3 19:06
 */
public class CommonServerCache {

    public static final Map<String,Object> PROVIDER_CLASS_MAP = new ConcurrentHashMap<>();
    public static final Set<URL> PROVIDER_URL_SET = new ConcurrentSet<>();
    public static SerializeFactory SERVER_SERIALIZE_FACTORY;
    public static ServerConfig SERVER_CONFIG;
    public static ServerBeforeFilterChain SERVER_BEFORE_FILTER_CHAIN;
    public static ServerAfterFilterChain SERVER_AFTER_FILTER_CHAIN;
    public static RegistryService REGISTRY_SERVICE;
    public static final Map<String, ServiceWrapper> PROVIDER_SERVICE_WRAPPER_MAP = new ConcurrentHashMap<>();
    public static Boolean IS_STARTED = false;
    public static ServerChannelDispatcher SERVER_CHANNEL_DISPATCHER = new ServerChannelDispatcher();
    public static final Map<String, ServerServiceSemaphoreWrapper> SERVER_SERVICE_SEMAPHORE_WRAPPER_MAP = new ConcurrentHashMap<>(64);

}
