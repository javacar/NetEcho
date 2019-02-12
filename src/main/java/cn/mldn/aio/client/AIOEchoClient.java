package cn.mldn.aio.client;

import cn.mldn.commons.ServerInfo;
import cn.mldn.util.InputUtil;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
class AIOClientThread implements Runnable {
    private AsynchronousSocketChannel clientChannel;
    private CountDownLatch latch;

    public AIOClientThread() throws Exception {
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

    /**
     * 进行消息的发送处理
     *
     * @param msg 输入的交互内容
     * @return 是否停止交互的标记
     */
    public boolean sendMessage(String msg) {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        buffer.put(msg.getBytes());
        buffer.flip();
        this.clientChannel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (buffer.hasRemaining()) {
                    clientChannel.write(buffer, buffer, this);
                } else {
                    ByteBuffer readBuffer = ByteBuffer.allocate(50);
                    //clientChannel.read(readBuffer,readBuffer,new ClientReadHandler(clientChannel,latch)) ;
                    clientChannel.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            buffer.flip();
                            String receiveMessage = new String(buffer.array(), 0, buffer.remaining());
                            System.out.println(receiveMessage);
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            try {
                                clientChannel.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            latch.countDown();
                        }
                    });
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    clientChannel.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        });
        return !"exit".equalsIgnoreCase(msg);
    }
}


public class AIOEchoClient {
    public static void main(String[] args) throws Exception {
        AIOClientThread client = new AIOClientThread();
        new Thread(client).start();
        while (client.sendMessage(InputUtil.getString("请输入要发送的信息："))) {
        }
    }
}
