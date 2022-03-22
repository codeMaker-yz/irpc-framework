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


