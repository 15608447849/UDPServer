package client.threads;

import client.imps.ClientImps;
import utils.Command;
import utils.LOG;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by user on 2017/5/24.
 */
public abstract class ClientThread extends Thread {
    public ClientImps client;
    public DatagramChannel channel;
    public ByteBuffer buffer;
    public ClientThread(ClientImps client) {
        this.client = client;
    }
    protected boolean flag = true;
    public void setFlag(boolean flag){
        this.flag = flag;
    }
    //心跳
    protected void sendHrbt(){

    }
    //通讯
    protected void sendMessage(){

    }
    //接受消息
    protected void receiveMessage(){

    }
    //传输数据
    public void tranlslete(String path){

    }
    protected void stopSelf() {
        try {
            LOG.I("关闭连接");
            if (channel!=null){
                //关闭连接
                channel.close();
                buffer.clear();
                buffer = null;
            }
        } catch (IOException e) {
        }
        //退出线程
        setFlag(false);
    }
}
