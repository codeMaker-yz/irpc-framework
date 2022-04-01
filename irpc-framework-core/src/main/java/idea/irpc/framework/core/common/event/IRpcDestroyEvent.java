package idea.irpc.framework.core.common.event;



/**
 * @author £ºMr.Zhang
 * @date £ºCreated in 2022/4/1 14:05
 */
public class IRpcDestroyEvent implements IRpcEvent{

    private Object data;

    public IRpcDestroyEvent(Object data){
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
