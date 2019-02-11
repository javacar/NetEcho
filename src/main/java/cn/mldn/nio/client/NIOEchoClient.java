package cn.mldn.nio.client;

import cn.mldn.commons.ServerInfo;
import cn.mldn.util.InputUtil;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 进行NIO客户端的连接访问。
 */
public class NIOEchoClient {
    public static void main(String[] args) throws Exception {
        new EchoClientHandle();
    }
}

class EchoClientHandle implements AutoCloseable {
    private SocketChannel clientChannel ;
    public EchoClientHandle() throws Exception {
        this.clientChannel = SocketChannel.open() ; // 创建一个客户端的通道实例
        // 设置要连接的主机信息，包括主机名称以及端口号
        this.clientChannel.connect(new InetSocketAddress(ServerInfo.SERVER_HOST,ServerInfo.PORT)) ;
        this.accessServer();
    }
    public void accessServer() throws Exception {    // 访问服务器端
        ByteBuffer buffer = ByteBuffer.allocate(50) ; // 开辟一个缓冲区
        String msg=null;
        while (!"exit".equalsIgnoreCase(msg)){
            buffer.clear();// 清空缓冲区，因为该部分代码会重复执行
            msg = InputUtil.getString("请输入要发送的内容：") ;
            buffer.put(msg.getBytes());
            buffer.flip() ; // 重置缓冲区
            this.clientChannel.write(buffer) ; // 发送数据内容
            // 当消息发送过去之后还需要进行返回内容的接收处理
            buffer.clear() ; // 清空缓冲区，等待新的内容的输入
            int readCount = this.clientChannel.read(buffer) ; // 将内容读取到缓冲区之中，并且返回个数
            buffer.flip() ; // 得到前需要进行重置
            System.out.println(new String(buffer.array(),0,readCount)); // 输出信息
        }
    }

    @Override
    public void close() throws Exception {
        this.clientChannel.close();
    }
}