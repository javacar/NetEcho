package cn.mldn.bio.server;

import cn.mldn.commons.ServerInfo;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

class FileServerHandle  implements AutoCloseable{
    private ServerSocket serverSocket;

    public FileServerHandle() throws Exception {
        this.serverSocket = new ServerSocket(ServerInfo.PORT);   // 进行服务端的Socket启动
        System.out.println("File服务器端已经启动了，该服务在" + ServerInfo.PORT + "端口上监听....");
        this.clientConnect();
    }
    private void clientConnect() throws Exception{
        while (true){
            Socket client = this.serverSocket.accept(); // 等待客户端连接
           new Thread(() -> {
               DataInputStream dis=null;
               FileOutputStream fos=null;
               try {
                   dis=new DataInputStream(client.getInputStream());
                  byte[] bytes = new byte[1024];
                   String fileName = dis.readUTF();
                   fos=new FileOutputStream("D:\\log\\"+fileName);
                   int length = 0;
                   while((length = dis.read(bytes, 0, bytes.length)) != -1) {
                       fos.write(bytes, 0, length);
                       fos.flush();
                   }

               }catch (Exception e){
                   e.printStackTrace();
               }
               finally {
                   try{
                       if(fos != null) fos.close();
                       if(dis != null)dis.close();
                       client.close();
                   }
                   catch (Exception e){
                       e.printStackTrace();
                   }
               }

           }).start();

        }
    }
    @Override
    public void close() throws Exception {
        this.serverSocket.close();
    }
}
public class FileSever  {
    public static void main(String[] args) throws  Exception{
        new FileServerHandle();
    }
}
