package idea.irpc.framework.core.common.config;

import idea.irpc.framework.core.common.utils.CommonUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author ��Mr.Zhang
 * @date ��Created in 2022/3/12 15:46
 */
public class PropertiesLoader {
    private static Properties properties;

    private static Map<String, String> propertiesMap = new HashMap<>();

    private static String DEFAULT_PROPERTIES_FILE = "D:\\rpc\\irpc-framework-core\\src\\main\\resources\\irpc.properties";

    //todo �������ֱ��ʹ��static�����Ƿ���ԣ�
    public static void loadConfiguration() throws IOException {
        if(properties!=null){
            return;
        }
        properties = new Properties();
        FileInputStream in = null;
        in = new FileInputStream(DEFAULT_PROPERTIES_FILE);
        properties.load(in);
    }

    /**
     * ���ݼ�ֵ��ȡ��������
     *
     * @param key
     * @return
     */
    public static String getPropertiesStr(String key) {
        if (properties == null) {
            return null;
        }
        if (CommonUtils.isEmpty(key)) {
            return null;
        }
        if (!propertiesMap.containsKey(key)) {
            String value = properties.getProperty(key);
            propertiesMap.put(key, value);
        }
        return String.valueOf(propertiesMap.get(key));
    }

    /**
     * ���ݼ�ֵ��ȡ��������
     *
     * @param key
     * @return
     */
    public static Integer getPropertiesInteger(String key) {
        if (properties == null) {
            return null;
        }
        if (CommonUtils.isEmpty(key)) {
            return null;
        }
        if (!propertiesMap.containsKey(key)) {
            String value = properties.getProperty(key);
            propertiesMap.put(key, value);
        }
        return Integer.valueOf(propertiesMap.get(key));
    }
}
