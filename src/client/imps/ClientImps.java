package client.imps;

import CommandHandler.CommandManager;
import client.threads.ClientThread;
import client.threads.imps.DataConnect;
import client.threads.imps.KeepConnect;
import client.threads.imps.receiveServerMessage;
import utils.Command;
import utils.LOG;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by user on 2017/5/24.
 *
 */
public class ClientImps {

    public CommandManager commandManager = CommandManager.get();

    private static class Holder{
        private static ClientImps imps = new ClientImps();
    }

    public static ClientImps get(){
        return Holder.imps;
    }

    public HashMap<String,ClientThread> threadMap;//线程集合
    public ClientInfo info;
    public Selector selector;
    public DatagramChannel commChannel;//讯通

    private ClientImps(){
        threadMap = new HashMap<>();
    }

    //初始化信息
    public void initClient(ClientInfo info) {
        this.info = info;
    }

    //关联线程
    public void conneThreads(HashMap<String,ClientThread> threadMap){
        this.threadMap = threadMap;
    }

    //开始服务
    public void startClient() {
        //创建连接
        if (info == null) throw new NullPointerException("client into is null.");
        try {
            selector = Selector.open();
            commChannel = DatagramChannel.open();
            commChannel.configureBlocking(false);
            commChannel.socket().bind(new InetSocketAddress(info.localIp,info.localPort));//关联自己的端口
            commChannel.register(selector, SelectionKey.OP_READ);//注册
            threadMap.put("receiveThread",new receiveServerMessage(this)); //接受消息线程
            threadMap.put("keepAliveThread",new KeepConnect(this));//心跳
            //循环开始所有的线程
            Iterator<ClientThread> itr = threadMap.values().iterator();
            while (itr.hasNext()){
                itr.next().start();
            }
            LOG.I("客户端 : "+ info.macAddress +" - 已启动, 服务器 IP: "+ info.serverIp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //请求下载一个文件  {5,0,0,0,50,"客户端MAC地址@请求的文件名@状态码"}
    public void requestSource(String source,String localPath){
        //先确定不在文件传输中
        ClientThread thread = threadMap.get("request");
        if (thread!=null) new Exception("current data translate ,Denied request.");
        int state = 1;
        String message = info.macAddress+Command.SEPARATOR+source+Command.SEPARATOR+state;
        //发送消息到服务器
        Command.sendMessage(commChannel,new InetSocketAddress(info.serverIp,info.serverPort),Command.CLIENT_SERVER_QUESY_SOURCE,message,null);
        LOG.I("请求资源 - "+Command.CLIENT_SERVER_QUESY_SOURCE+" "+message);
        //开启一个线程,尝试和服务器建立连接
        try {
            DataConnect dCon =  new DataConnect(this);
            threadMap.put("request",dCon);
            dCon.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
