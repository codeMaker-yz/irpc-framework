package idea.irpc.framework.core.common.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author ��Mr.Zhang
 * @date ��Created in 2022/3/12 15:05
 */
public class CommonUtils {
    /**
     * ��ȡĿ������ʵ�ֽӿ�
     *
     * @param targetClass
     * @return
     */
    public static List<Class<?>> getAllInterfaces(Class targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("targetClass is null!");
        }
        Class[] clazz = targetClass.getInterfaces();
        if (clazz.length == 0) {
            return Collections.emptyList();
        }
        List<Class<?>> classes = new ArrayList<>(clazz.length);
        for (Class aClass : clazz) {
            classes.add(aClass);
        }
        return classes;
    }


    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("IP��ַ��ȡʧ��" + e.toString());
        }
        return "";
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isEmptyList(List list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(String str){
        return !isEmpty(str);
    }

    public static boolean isNotEmptyList(List list) {
        return !isEmptyList(list);
    }
}
