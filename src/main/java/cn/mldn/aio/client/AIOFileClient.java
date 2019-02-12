package cn.mldn.aio.client;

import cn.mldn.commons.ServerInfo;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
class FileReadHandler implements CompletionHandler<Integer,ByteBuffer>{
    private AsynchronousSocketChannel clientChannel ;
    private CountDownLatch latch ;

    public FileReadHandler(AsynchronousSocketChannel clientChannel, CountDownLatch latch) {
        this.clientChannel = clientChannel;
        this.latch = latch;
    }

    @Override
    public void completed(Integer result, ByteBuffer buffer) {
        buffer.flip() ;
    }
    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        try {
            this.clientChannel.close();
        } catch (Exception e) {}
        this.latch.countDown();
    }
}
class FileWriteHandler implements CompletionHandler<Integer,ByteBuffer> {
    private AsynchronousSocketChannel clientChannel ;
    private CountDownLatch latch ;
    public FileWriteHandler(AsynchronousSocketChannel clientChannel,CountDownLatch latch) {
        this.clientChannel = clientChannel ;
        this.latch = latch ;
    }

    @Override
    public void completed(Integer result, ByteBuffer buffer) {
        if(buffer.hasRemaining()) {
            this.clientChannel.write(buffer,buffer,this) ;
        } else {
            ByteBuffer readBuffer = ByteBuffer.allocate(1024*1024) ;
            this.clientChannel.read(readBuffer,readBuffer,new FileReadHandler(this.clientChannel,this.latch)) ;
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        try {
            this.clientChannel.close();
        } catch (Exception e) {}
        this.latch.countDown();
    }
}
class AIOFileClientThread implements Runnable {
    private AsynchronousSocketChannel clientChannel;
    private CountDownLatch latch;

    public AIOFileClientThread() throws Exception {
        this.clientChannel = AsynchronousSocketChannel.open(); // 打开客户端的Channel
        this.clientChannel.connect(new InetSocketAddress(ServerInfo.SERVER_HOST, ServerInfo.PORT));
        this.latch = new CountDownLatch(1);

    }

    @Override
    public void run() {
        try {
            this.latch.await();
            this.clientChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void fileUpload() {
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        this.clientChannel.write(buffer, buffer, new FileWriteHandler(this.clientChannel, this.latch));

    }
}

public class AIOFileClient {
    public static void main(String[] args) throws Exception {
        AIOFileClientThread client = new AIOFileClientThread();
        new Thread(client).start();
        client.fileUpload();
    }
}
