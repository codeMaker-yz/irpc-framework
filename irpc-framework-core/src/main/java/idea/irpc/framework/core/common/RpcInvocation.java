package idea.irpc.framework.core.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/3 19:00
 */
public class RpcInvocation implements Serializable {

    private static final long serialVersionUID = -3611379458492006176L;
    private String targetMethod;

    private String targetServiceName;

    private Object[] args;

    private String uuid;

    private Object response;

    private Map<String,Object> attachments = new HashMap<>();

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String getTargetServiceName() {
        return targetServiceName;
    }

    public void setTargetServiceName(String targetServiceName) {
        this.targetServiceName = targetServiceName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Map<String, Object> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, Object> attachments) {
        this.attachments = attachments;
    }

    @Override
    public String toString() {
        return "RpcInvocation{" +
                "targetMethod='" + targetMethod + '\'' +
                ", targetServiceName='" + targetServiceName + '\'' +
                ", args=" + Arrays.toString(args) +
                ", uuid='" + uuid + '\'' +
                ", response=" + response +
                '}';
    }
}
