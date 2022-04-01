package idea.irpc.framework.core.common.event;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import idea.irpc.framework.core.common.event.listener.ProviderNodeDataChangeListener;
import idea.irpc.framework.core.common.utils.CommonUtils;
import idea.irpc.framework.core.common.event.listener.ServiceUpdateListener;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/12 15:02
 */
public class IRpcListenerLoader {
    private static List<IRpcListener> iRpcListenerList = new ArrayList<>();

    private static ExecutorService eventThreadPool = Executors.newFixedThreadPool(2);

    public static void registerListener(IRpcListener iRpcListener) {
        iRpcListenerList.add(iRpcListener);
    }

    public void init() {
        registerListener(new ServiceUpdateListener());
        registerListener(new ProviderNodeDataChangeListener());
    }

    /**
     * 获取接口上的泛型T
     *
     * @param o     接口
     */
    public static Class<?> getInterfaceT(Object o) {
        Type[] types = o.getClass().getGenericInterfaces();
        ParameterizedType parameterizedType = (ParameterizedType) types[0];
        Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        return null;
    }

    /**
     * 同步事件处理，可能会阻塞
     * @param iRpcEvent
     */
    public static void sendSyncEvent(IRpcEvent iRpcEvent){
        System.out.println(iRpcListenerList);
        if(CommonUtils.isEmptyList(iRpcListenerList)){
            return;
        }
        for (IRpcListener<?> iRpcListener : iRpcListenerList){
            Class<?> type = getInterfaceT(iRpcListener);
            if(type.equals(iRpcEvent.getClass())){
                try {
                    iRpcListener.callBack(iRpcEvent.getData());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void sendEvent(IRpcEvent iRpcEvent) {
        if(CommonUtils.isEmptyList(iRpcListenerList)){
            return;
        }
        for (IRpcListener<?> iRpcListener : iRpcListenerList) {
            Class<?> type = getInterfaceT(iRpcListener);
            if(type.equals(iRpcEvent.getClass())){
                eventThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            iRpcListener.callBack(iRpcEvent.getData());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

}
