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
