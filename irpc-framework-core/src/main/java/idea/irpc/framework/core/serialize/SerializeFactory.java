package idea.irpc.framework.core.serialize;

/**
 * @author ：Mr.Zhang
 * @date ：Created in 2022/3/29 14:22
 */
public interface SerializeFactory {

    /**
     * 序列化
     * @param t
     * @param <T>
     * @return
     */
    <T> byte[] serialize(T t);

    /**
     * 反序列化
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T deserialize(byte[] data, Class<T> clazz);
}
