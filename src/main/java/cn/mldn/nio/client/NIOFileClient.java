package cn.mldn.nio.client;

import cn.mldn.bio.client.FileClient;
import cn.mldn.commons.ServerInfo;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

class FileClientHandle implements AutoCloseable {
    private SocketChannel clientChannel;

    public FileClientHandle() throws Exception {
        this.clientChannel = SocketChannel.open(); // 创建一个客户端的通道实例
        // 设置要连接的主机信息，包括主机名称以及端口号
        this.clientChannel.connect(new InetSocketAddress(ServerInfo.SERVER_HOST,ServerInfo.PORT));
        this.accessServer();
    }

    public void accessServer() throws Exception {     // 访问服务器端
        JFileChooser jd=new JFileChooser();
        jd.showOpenDialog(null);
        File file=jd.getSelectedFile();
        FileChannel fileChannel = new FileInputStream(file).getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        buffer.put(file.getName().getBytes());
        buffer.flip() ; // 重置缓冲区
        this.clientChannel.write(buffer) ; // 发送数据内容
        clientChannel.read(buffer);
        buffer.clear();
        int num = 0;
        while ((num = fileChannel.read(buffer)) > 0) {
            buffer.flip();
            clientChannel.write(buffer);
            buffer.clear();
        }
        if (num == -1) {
            fileChannel.close();
            clientChannel.shutdownOutput();
        }



    }

    @Override
    public void close() throws Exception {
        this.clientChannel.close();
    }
}

public class NIOFileClient {
    public static void main(String[] args) throws Exception {
        new FileClientHandle();
    }
}