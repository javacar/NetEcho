package cn.mldn.aio.server;

import cn.mldn.commons.ServerInfo;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * 2、实现客户端的回应处理操作
 */
class FileHandler implements CompletionHandler<Integer, ByteBuffer> {
    private AsynchronousSocketChannel clientChannel;

    public FileHandler(AsynchronousSocketChannel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        try {
            FileHandler.this.clientChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void completed(Integer result, ByteBuffer buffer) {
        buffer.flip();

        this.download();
    }

    private void download() {
        try {
            ByteBuffer buffer = read();

            this.clientChannel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    try {
                        write(buffer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        FileHandler.this.clientChannel.close();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ByteBuffer read() throws Exception {
        Path path = Paths.get("D:\\BaiduNetdiskDownload", "个人头像.jpg");
        AsynchronousFileChannel afc = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
        Future<Integer> read = afc.read(byteBuffer, 0);

        while (!read.isDone()) {
        }
        afc.close();
        return byteBuffer;
    }


    public void write(ByteBuffer byteBuffer) throws Exception {
        Path path = Paths.get("D:\\log", "个人头像.jpg");
        AsynchronousFileChannel afc = AsynchronousFileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        byteBuffer.flip();
        Future<Integer> write = afc.write(byteBuffer, 0);
        while (!write.isDone()) {
        }
        afc.close();
    }

}

/**
 * 1、实现客户端连接回调的处理操作
 */
class FileAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AIOFileServerThread> {

    @Override
    public void completed(AsynchronousSocketChannel result, AIOFileServerThread attachment) {
        attachment.getServerChannel().accept(attachment, this); // 接收连接对象
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        result.read(buffer, buffer, new FileHandler(result));
    }


    @Override
    public void failed(Throwable exc, AIOFileServerThread attachment) {
        System.out.println("服务器端客户端连接失败 ...");
        attachment.getLatch().countDown(); // 恢复执行
    }
}

class AIOFileServerThread implements Runnable {// 是进行AIO处理的线程类
    private AsynchronousServerSocketChannel serverChannel;
    private CountDownLatch latch;  // 进行线程等待操作

    public AIOFileServerThread() throws Exception {
        this.latch = new CountDownLatch(1); // 设置一个线程等待个数
        this.serverChannel = AsynchronousServerSocketChannel.open(); // 打开异步的通道
        this.serverChannel.bind(new InetSocketAddress(ServerInfo.PORT)); // 绑定服务端口
        System.out.println("服务器启动成功，在" + ServerInfo.PORT + "端口上监听服务 ...");
    }

    public AsynchronousServerSocketChannel getServerChannel() {
        return serverChannel;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    @Override
    public void run() {
        this.serverChannel.accept(this, new FileAcceptHandler()); // 等待客户端连接

        try {
            this.latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

public class AIOFileServer {
    public static void main(String[] args) throws Exception {
        new Thread(new AIOFileServerThread()).start();
    }
}

