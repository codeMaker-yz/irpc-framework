package idea.irpc.framework.core.common.event;

/**
 * @author £ºMr.Zhang
 * @date £ºCreated in 2022/3/12 14:59
 */
public interface IRpcEvent {
    Object getData();

    IRpcEvent setData(Object data);
}
