package imps;

import beans.UdpClient;
import itface.ServerInerface;
import itface.ServerOpration;
import itface.Command;
import utils.LOG;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lzp on 2017/5/22.
 * udp 服务端
 * 中介
 *
 */
public class ServerImps extends Thread implements ServerInerface{


    private ExecutorService mThreadpool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private void runTask(Runnable runnable){
        mThreadpool.execute(runnable);
    }

    private beans.UdpServer server;
    private DatagramSocket serverSocket;//接受信息

    private volatile boolean isStart;
    private ServerOpration opration;
    private ServerImps(){

    }

    //关联
    @Override
    public ServerInerface connectOps(ServerOpration ops) {
        this.opration = ops;
        return this;
    }



    private static class Holder{
        private static ServerImps instand = new ServerImps();
    }

    public static ServerImps get(){
        return Holder.instand;
    }

    public ServerInerface init(beans.UdpServer server){
        this.server = server;
        return this;
    }

    @Override
    public DatagramSocket getServerSocke() {
        return serverSocket;
    }

    public void startServer(){
        if (server==null) throw new NullPointerException("server info is null");
        try {
            serverSocket = new DatagramSocket(server.socketPort, server.localIp);
            isStart = true;
            start();
            LOG.I("中转服务器启动成功 ,IP : "+ server.localIp +", 端口号 :"+server.socketPort);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
            byte[] buffByte  = new byte[Command.BUFF_LENGTH];
            DatagramPacket packet = new DatagramPacket(buffByte, buffByte.length);
            while (isStart) {
                try {
                    serverSocket.receive(packet);
                    handleMessage(packet);//消息处理
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    //处理
    public void handleMessage(DatagramPacket packet) {
        final InetAddress inetAddress = packet.getAddress();
        final int inetPort  = packet.getPort();
        String receiveMessage = new String(packet.getData());
        LOG.I("收到一个UDP信息:\n " + packet.getData().length +" [ "+inetAddress+":"+inetPort +" -> "+receiveMessage+" ]");
        final byte[] data = packet.getData();
        runTask(() -> {
            //****
            if (data.length>=4){
                byte[] command = new byte[4];
                System.arraycopy(data, 0, command, 0, 4);
                if (Arrays.equals(Command.HRBT,command)) {
                    HRBT(inetAddress,inetPort,data);
                }
            }
            //****
        });

    }

    @Override
    public void sendMessage(DatagramSocket socket,  InetAddress ip,int port, byte[] bytes) {
        try {
            LOG.I(" 使用"+socket.getLocalPort()+"发送消息到:"+ip);
            socket.send(new DatagramPacket(bytes,bytes.length, ip, port));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //心跳 HRBT*/192.168.55.121/5555/MAC_ADDRESS/#
    private void HRBT(InetAddress inetAddress,int inetPort, byte[] data) {
        try {
        //心跳, ->添加用户->通知所有在线用户
        String str = new String(data,0,data.length);
        str = str.substring(str.indexOf(Command.TAGS_START_STR)+1,
                str.lastIndexOf(Command.TAGS_END_STR));
        String[] strArr = str.split(Command.separator);
        if (strArr.length>=3){
            InetAddress clientAddress = InetAddress.getByName(strArr[0]);
            int clientPort = Integer.parseInt(strArr[1]);
            String macAddress = strArr[2];

            UdpClient client = new UdpClient();
                client.inetAddress = inetAddress;
                client.inetPort = inetPort;
                client.clientAddress = clientAddress;
                client.clientPort = clientPort;
                client.clientMacAddress = macAddress;
                boolean flag = opration.addClient(client);//添加客户端
                if (flag){
                    LOG.I("客户端存活 - "+client+" \n当前在线数:" + opration.getOnliNunber());
                }

        }
        } catch (Exception e) {
            e.printStackTrace();
        }
            //返回结果给客户端 - 维持生命周期
            sendMessage(serverSocket, inetAddress,inetPort,Command.RESP);

            //通知所有客户端,这个新上线的客户端的信息

    }
















}
