package idea.irpc.framework.core.router;

import idea.irpc.framework.core.common.ChannelFutureWrapper;
import idea.irpc.framework.core.registy.URL;
import jdk.nashorn.internal.ir.CallNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static idea.irpc.framework.core.common.cache.CommonClientCache.*;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/19 16:11
 */
public class RandomRouterImpl implements IRouter{
    @Override
    public void refreshRouterArr(Selector selector) {
        //获取服务提供者的数目
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(selector.getProviderServiceName());
        ChannelFutureWrapper[] arr = new ChannelFutureWrapper[channelFutureWrappers.size()];
        //提前生成调用先后顺序的随机数组
        int[] result = createRandomIndex(arr.length);
        //生成对应服务集群的每台机器调用顺序
        for (int i = 0; i < result.length; i++) {
            arr[i] = channelFutureWrappers.get(i);
        }
        SERVICE_ROUTER_MAP.put(selector.getProviderServiceName(), arr);

    }

    @Override
    public ChannelFutureWrapper select(Selector selector) {
        return CHANNEL_FUTURE_POLLING_REF.getChannelFutureWrapper(selector.getProviderServiceName());
    }

    @Override
    public void updateWeight(URL url) {
        //服务节点权重
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(url.getServiceName());
        Integer[] weightArr = createWeightArr(channelFutureWrappers);
        Integer[] finalArr = createRandomArr(weightArr);
        ChannelFutureWrapper[] finalChannelFutureWrappers = new ChannelFutureWrapper[finalArr.length];
        for (int i = 0; i < finalArr.length; i++) {
            finalChannelFutureWrappers[i] = channelFutureWrappers.get(i);
        }
        SERVICE_ROUTER_MAP.put(url.getServiceName(),finalChannelFutureWrappers);
    }

    /**
     *
     * weight指权重，权重约定好是100的整数倍
     * @param channelFutureWrappers
     * @return
     */
    private static Integer[] createWeightArr(List<ChannelFutureWrapper> channelFutureWrappers){
        ArrayList<Integer> weightArr = new ArrayList<>();
        for (int i = 0; i < channelFutureWrappers.size(); i++) {
            Integer weight = channelFutureWrappers.get(i).getWeight();
            int c = weight / 100;
            for (int j = 0; j < c; j++) {
                weightArr.add(i);
            }
        }
        Integer[] arr = new Integer[weightArr.size()];
        return weightArr.toArray(arr);
    }

    /**
     * 测试代码片段
     */
    public static void main(String[] args) {
        ArrayList<ChannelFutureWrapper> channelFutureWrappers = new ArrayList<>();
        channelFutureWrappers.add(new ChannelFutureWrapper(null,null,100));
        channelFutureWrappers.add(new ChannelFutureWrapper(null,null,200));
        channelFutureWrappers.add(new ChannelFutureWrapper(null,null,9300));
        channelFutureWrappers.add(new ChannelFutureWrapper(null,null,400));
        channelFutureWrappers.add(new ChannelFutureWrapper(null,null,100));
        Integer[] r = createWeightArr(channelFutureWrappers);
        System.out.println(Arrays.toString(r));
        System.out.println(r.length);
    }

    /**
     * 创建随机乱序数组
     *
     * @param arr
     * @return
     */
    private static Integer[] createRandomArr(Integer[] arr) {
        int total = arr.length;
        Random ra = new Random();
        for (int i = 0; i < total; i++) {
            int j = ra.nextInt(total);
            if (i == j) {
                continue;
            }
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        return arr;
    }

    private int[] createRandomIndex(int len) {
        int[] arrInt = new int[len];
        Random ra = new Random();
        for (int i = 0; i < arrInt.length; i++) {
            arrInt[i] = -1;
        }
        int index = 0;
        while (index < arrInt.length) {
            int num = ra.nextInt(len);
            //如果数组中不包含这个元素则赋值给数组
            if (!contains(arrInt, num)) {
                arrInt[index++] = num;
            }
        }
        return arrInt;
    }

    public boolean contains(int[] arr, int key) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == key) {
                return true;
            }
        }
        return false;
    }
}
