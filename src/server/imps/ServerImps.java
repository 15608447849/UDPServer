package server.imps;
import CommandHandler.CommandManager;
import utils.ClazzUtil;
import utils.Command;
import utils.LOG;
import java.io.IOException;
import java.net.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import server.beans.*;
import utils.ThreadUtil;

/**
 * Created by lzp on 2017/5/22.
 * udp 服务端
 *
 *
 */
public class ServerImps{
    public UdpServer server;

    public Selector selector;
    public DatagramChannel commChannel;
    public DatagramChannel connChannel;

    public ServerOps opration;
    public CommandManager commandManager = CommandManager.get();//命令管理器
    //单例
    private static class Holder{
        private static ServerImps instand = new ServerImps();
    }
    private ServerImps(){
    }

    //关联

    public ServerImps connectOps(ServerOps ops) {
        this.opration = ops;
        return this;
    }
    //获取单例
    public static ServerImps get(){
        return Holder.instand;
    }

    public ServerImps init(UdpServer server){
        this.server = server;
        return this;
    }
    //开始服务
    public void startServer(){
        if (server==null) throw new NullPointerException("server info is null");
        try {
            selector = Selector.open();

            commChannel = DatagramChannel.open();
            commChannel.configureBlocking(false);
            commChannel.bind(new InetSocketAddress(server.localIp,server.socketPort));
            commChannel.register(selector, SelectionKey.OP_READ);

            connChannel = DatagramChannel.open();
            connChannel.configureBlocking(false);
            connChannel.bind(new InetSocketAddress(server.localIp,server.socket2port));
            connChannel.register(selector, SelectionKey.OP_READ);
            new ServerReceiver(this).start();
            LOG.I("P2P中转服务器启动成功 ,IP : "+ server.localIp +", 交互端口号 :"+server.socketPort +" 通道搭建端口 : "+server.socket2port);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }











}
