package itface;

import beans.UdpClient;
import beans.UdpServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by user on 2017/5/22.
 */
public interface ServerInerface extends ServerInject{
    /**
     * 获取udp socket server
     */
    DatagramSocket getServerSocke();
    /**
     * 开启服务
     */
    void startServer();
    /**
     * 处理接受udp数据
     * @param packet
     */
     void handleMessage(DatagramPacket packet);
    /**
     * 发送消息
     */
    void sendMessage(DatagramSocket socket, InetAddress ip,int port, byte[] bytes);

}
