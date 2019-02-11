package cn.mldn.nio.server;


import cn.mldn.commons.ServerInfo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class FileServerHandle implements AutoCloseable {// 定义服务器端的服务处理类
    private ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
    //使用Map保存每个连接，当OP_READ就绪时，根据key找到对应的文件对其进行写入。若将其封装成一个类，作为值保存，可以再上传过程中显示进度等等
    Map<SelectionKey, FileChannel> fileMap = new HashMap<>();
    private final ServerSocketChannel serverSocketChannel; // 服务器端的通讯通道
    private Selector selector;
    private SocketChannel clientChannel ; // 客户端的信息
    public FileServerHandle() throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open(); // 打开服务器端连接通道
        this.serverSocketChannel.configureBlocking(false); // 设置为非阻塞模式
        this.serverSocketChannel.bind(new InetSocketAddress(ServerInfo.PORT));
        this.selector = Selector.open(); // 获取Selector实例
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // NIO之中的Reactor模型重点在于所有的Channel需要向Selector之中进行注册

        System.out.println("服务器已开启...");
        this.clientHandle();
    }

    public void clientHandle() throws IOException {
        int keySelect = 0 ;     // 保存一个当前的状态
        while((keySelect = this.selector.select()) > 0) {  // 需要进行连接等待
            Set<SelectionKey> selectedKeys = this.selector.selectedKeys() ; // 获取全部连接通道信息
            Iterator<SelectionKey> selectionIter = selectedKeys.iterator() ;
            while(selectionIter.hasNext()) {
                SelectionKey selectionKey = selectionIter.next() ; // 获取每一个通道
                if(selectionKey.isAcceptable()) {  // 该通道为接收状态
                    this.clientChannel = this.serverSocketChannel.accept() ; // 等待连接
                    if (this.clientChannel != null) {  // 当前有连接
                        clientChannel.configureBlocking(false);
                        SelectionKey key1 = clientChannel.register(selector, SelectionKey.OP_READ);
                        FileChannel fileChannel = new FileOutputStream("D:\\log\\个人头像.jpg").getChannel();
                        fileMap.put(key1, fileChannel);
                        System.out.println(clientChannel.getRemoteAddress() + "连接成功...");
                        clientChannel.write(buffer);
                    }
                }
                else if(selectionKey.isReadable()){
                    readData(selectionKey);
                }
                selectionIter.remove(); // 移除掉此通道
            }
        }

    }

    private void readData(SelectionKey key) throws IOException {
        FileChannel fileChannel = fileMap.get(key);
        buffer.clear();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int num = 0;
        try {
            while ((num = socketChannel.read(buffer)) > 0) {
                buffer.flip();
                // 写入文件
                fileChannel.write(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            key.cancel();
            e.printStackTrace();
            return;
        }
        // 调用close为-1 到达末尾
        if (num == -1) {
            fileChannel.close();
            System.out.println("上传完毕");
            key.cancel();
        }
    }

    @Override
    public void close() throws Exception {
        //      this.executorService.shutdown(); // 关闭线程池
        this.serverSocketChannel.close(); // 关闭服务器端
    }
}

public class NIOFileServer {
    public static void main(String[] args) throws IOException {
        new FileServerHandle();
    }
}





