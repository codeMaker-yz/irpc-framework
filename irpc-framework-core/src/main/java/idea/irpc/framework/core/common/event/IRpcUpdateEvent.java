package idea.irpc.framework.core.common.event;

/**
 * @author £ºMr.Zhang
 * @date £ºCreated in 2022/3/12 15:00
 */
public class IRpcUpdateEvent implements IRpcEvent {
    private Object data;

    public IRpcUpdateEvent(Object data) {
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
