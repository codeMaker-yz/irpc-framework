# irpc-framework
轻量级的rpc框架

客户端和服务器端的通信流程分析：

1. 客户端与服务器端连接建立后，执行客户端和服务器的initChannel(SocketChannel ch)方法
2. 客户端发送的信息经过自定义的encode方法进行编码
3. 服务器通过自定义的decode方法进行解码
4. 解码完成后，服务器通过ServerHandler进行数据处理，得到响应内容
5. 将响应内容通过encode方法编码
6. 客户端执行decode方法解码
7. 客户端执行ClientHandler方法获取响应内容

Netty定义了两个重要的ChannelHandler子接口：
- ChannelInboundHandler ———————————— 处理入站数据以及各种状态变化
- ChannelOutboundHandler ——————————— 处理出站数据并且允许拦截所有的操作

根据事件的起源，事件会被ChannelInboundHandler或者ChannelOutboundHandler处理。随后调用ChannelHandlerContext实现，将被转发给同一超类型的下一个ChannelHandler。
入站事件触发，会从ChannelPipeline的头部开始一直传播到ChannelPipeline的尾部
出站事件触发，会从ChannelPipeline的尾部开始一直传播到ChannelPipeline的头部

## 代理层开发

netty内部数据传输，要考虑拆包和粘包部分的逻辑。

解决手段：
- 固定长度文本传输
- 特殊分割字符传输
- 固定协议传输

项目使用自定义协议的方式，见RpcEncoder和RpcDecoder

客户端通过一个代理工厂获取被调用对象的代理对象，然后通过代理对象将数据放入发送队列，最后会有一个异步线程将发送队列内部的数据一个个地发送到服务端，并等待服务端响应对应的数据结果。

核心思想：将请求发送任务交给单独的IO线程去负责，实现异步化，提升发送性能。

代理工厂部分的设计：JDKProxyFactory
```java
public class JDKProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(final Class clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                new JDKClientInvocationHandler(clazz));
    }

}
```
主要是JDKClientInvocationHandler()的实现，核心任务是将需要调用的方法名称、服务名称，参数统统都封装好到RpcInvocation中，然后添加进一个阻塞队列，并等待服务端的数据返回。

## 注册中心的接入与实现

新增一个第三方平台，每个服务暴露的时候，将相关信息记录到中间平台。当有调用方订阅服务的时候，预先到中间平台进行登记。当服务提供者下线的时候，需要到该平台去将之前的记录移除，然后由平台通知给服务调用方。


引入事件的设计思路，主要目的是解耦。
当监听到某个节点数据发生更新之后，会发生一个节点更新事件，然后在事件的监听端对不同的行为做不同的事件处理。

事件监听机制设计代码：

定义一个抽象的事件，该事件用于装载需要传递的数据信息：
```java
public interface IRpcEvent{
    Object getData();
    IRpcEvent setData(Object data);
}
```

定义一个节点更新事件：

```java
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
```
当zookeeper的某个节点发生数据变动的时候，就会发送一个变更事件，然后由对应的监听器去捕获这些数据并做处理。

监听器接口设计如下：
```java
public interface IRpcListener<T> {

    void callBack(Object t);

}
```
定义好了统一的事件规范，监听接口，那么下边就需要有专门的类去发送事件了：
```java
public class IRpcListenerLoader {

    private static List<IRpcListener> iRpcListenerList = new ArrayList<>();

    private static ExecutorService eventThreadPool = Executors.newFixedThreadPool(2);

    public static void registerListener(IRpcListener iRpcListener) {
        iRpcListenerList.add(iRpcListener);
    }

    public void init() {
        registerListener(new ServiceUpdateListener());
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
```
zk的服务提供者节点发生了变更，客户端需要更新本地的一个目标服务列表，避免向无用的服务发送请求。
实现类：
```java
public class ServiceUpdateListener implements IRpcListener<IRpcUpdateEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUpdateListener.class);

    @Override
    public void callBack(Object t) {
        //获取到子节点的数据信息
        URLChangeWrapper urlChangeWrapper = (URLChangeWrapper) t;
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(urlChangeWrapper.getServiceName());
        if (CommonUtils.isEmptyList(channelFutureWrappers)) {
            LOGGER.error("[ServiceUpdateListener] channelFutureWrappers is empty");
            return;
        } else {
            List<String> matchProviderUrl = urlChangeWrapper.getProviderUrl();
            Set<String> finalUrl = new HashSet<>();
            List<ChannelFutureWrapper> finalChannelFutureWrappers = new ArrayList<>();
            for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
                String oldServerAddress = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
                //如果老的url没有，说明已经被移除了
                if (!matchProviderUrl.contains(oldServerAddress)) {
                    continue;
                } else {
                    finalChannelFutureWrappers.add(channelFutureWrapper);
                    finalUrl.add(oldServerAddress);
                }
            }
            //此时老的url已经被移除了，开始检查是否有新的url
            //ChannelFutureWrapper其实是一个自定义的包装类，将netty建立好的ChannelFuture做了一些封装
            List<ChannelFutureWrapper> newChannelFutureWrapper = new ArrayList<>();
            for (String newProviderUrl : matchProviderUrl) {
                if (!finalUrl.contains(newProviderUrl)) {
                    ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
                    String host = newProviderUrl.split(":")[0];
                    Integer port = Integer.valueOf(newProviderUrl.split(":")[1]);
                    channelFutureWrapper.setPort(port);
                    channelFutureWrapper.setHost(host);
                    ChannelFuture channelFuture = null;
                    try {
                        channelFuture = ConnectionHandler.createChannelFuture(host,port);
                        channelFutureWrapper.setChannelFuture(channelFuture);
                        newChannelFutureWrapper.add(channelFutureWrapper);
                        finalUrl.add(newProviderUrl);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            finalChannelFutureWrappers.addAll(newChannelFutureWrapper);
            //最终更新服务在这里
            CONNECT_MAP.put(urlChangeWrapper.getServiceName(),finalChannelFutureWrappers);
        }
    }
}
```

## 路由层接入




## 序列化

传输过程中，数据需要以字节数组形式传输，常见的序列化技术有以下几类：Hessian、Kryo、JDK、FastJson。为了能兼容各类不同的序列化框架，在IRpc框架内部抽离了一层序列化层，专门用于对接市面上常见的序列化技术框架。
项目对常见的几项序列化技术都进行了接入，可以通过配置文件中的类型来决定使用哪一类序列化技术。

如何评估序列化技术的优劣？
通常考虑这项技术在实际落地中的吞吐量，比较具有代表性的两个指标为：
- 产生码流的大小
- 序列化处理的速度

通过码流大小的比对测试发现，码流大小比较为：kryo > fastJson > hessian >> jdk, JDK产生的码流远大于前三项技术产生的码流。

基于JMH进行测试，比较相关序列化吞吐性,通过最终基准报告看，fastJson效果最佳，JDK最差。
```markdown
Benchmark                                    Mode  Cnt       Score        Error  Units
SerializeCompareTest.fastJsonSerializeTest  thrpt    5  418817.217 ±  23916.074  ops/s
SerializeCompareTest.hessianSerializeTest   thrpt    5  147008.575 ± 102135.128  ops/s
SerializeCompareTest.jdkSerializeTest       thrpt    5   34299.448 ±   3138.283  ops/s
SerializeCompareTest.kryoSerializeTest      thrpt    5  293025.368 ±  88426.848  ops/s
```
综上，考虑序列化技术时，可以优先考虑fastJson、kryo等技术。




