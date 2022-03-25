package idea.irpc.framework.core.common.event;

/**
 * @author £ºMr.Zhang
 * @date £ºCreated in 2022/3/22 19:25
 */
public class IRpcNodeChangeEvent implements IRpcEvent{

    private Object data;

    public IRpcNodeChangeEvent(Object data) {
        this.data = data;
    }
    @Override
    public Object getData() {
        return data;
    }

    @Override
    public IRpcEvent setData(Object data) {
        this.data = data;
        return this;
    }
}
