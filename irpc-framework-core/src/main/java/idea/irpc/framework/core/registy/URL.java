package idea.irpc.framework.core.registy;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ��Mr.Zhang
 * @date ��Created in 2022/3/9 18:52
 */
public class URL {

    /**
     * ����Ӧ������
     */
    private String applicationName;


    /**
     * ע�ᵽ�ڵ�ķ������ƣ����磺com.test.UserService
     */
    private String serviceName;

    /**
     * �Զ�����չ
     * ����
     * Ȩ��
     * �����ṩ�ߵĵ�ַ
     * �����ṩ�ߵĶ˿�
     */
    private Map<String,String> parameters = new HashMap<>();

    public void addParameter(String key,String value){
        this.parameters.putIfAbsent(key,value);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }


    /**
     * ��URLת��Ϊд��zk��provider�ڵ��µ�һ���ַ���
     *
     * @param url
     * @return
     */
    public static String buildProviderUrlStr(URL url){
        String host = url.getParameters().get("host");
        String port = url.getParameters().get("port");
        return new String((url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ":" + port + ";" + System.currentTimeMillis()).getBytes(), StandardCharsets.UTF_8);
    }

    /**
     * ��URLת��Ϊд��zk��consumer�ڵ��µ�һ���ַ���
     *
     * @param url
     * @return
     */
    public static String buildConsumerUrlStr(URL url) {
        String host = url.getParameters().get("host");
        return new String((url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ";" + System.currentTimeMillis()).getBytes(), StandardCharsets.UTF_8);
    }

    /**
     * ��ĳ���ڵ��µ���Ϣת��Ϊһ��Provider�ڵ����
     *
     * @param providerNodeStr
     * @return
     */

}
