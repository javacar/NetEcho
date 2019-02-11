package cn.mldn.bio.client;

import cn.mldn.commons.ServerInfo;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

class FileClientHandle  implements AutoCloseable {
    private Socket client;
    public FileClientHandle() throws Exception {
        client=new Socket(ServerInfo.SERVER_HOST, ServerInfo.PORT);
        System.out.println("已经成功的连接到了服务器端，可以进行消息的发送处理。");
        this.accessServer();
    }
    private void accessServer() throws Exception{
        byte[] bytes = new byte[1024];
        int length = 0;
        DataOutputStream dos = new DataOutputStream(client.getOutputStream());
        JFileChooser fd = new JFileChooser();
        fd.showOpenDialog(null);
        File file=fd.getSelectedFile();
        FileInputStream fis=null;
        if (file.exists()){
            dos.writeUTF(file.getName());
            dos.flush();
            fis = new FileInputStream(file);
            while ((length=fis.read(bytes,0,bytes.length))!=-1){
                dos.write(bytes,0,length);
                dos.flush();
            }

        }

        if(fis != null)
            fis.close();
        if(dos != null)
            dos.close();
        client.close();

    }
    @Override
    public void close() throws Exception {
        client.close();
    }
}
public class FileClient {
    public static void main(String[] args)throws Exception{
        new FileClientHandle();
    }


}
