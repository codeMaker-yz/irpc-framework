package idea.irpc.framework.core.common.cache;

import idea.irpc.framework.core.registy.URL;
import idea.irpc.framework.core.serialize.SerializeFactory;
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
}
