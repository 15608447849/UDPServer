package server.beans;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by lzp on 2017/5/22.
 */
public class UdpServer{
    public InetAddress localIp;
    public int socketPort = 8000; //通讯接口
    public int socket2port;//通道搭建接口
    public UdpServer(String localIp, int socketPort,int socket2port) throws UnknownHostException {
        this.localIp = InetAddress.getByName(localIp);
        this.socketPort = socketPort;
        this.socket2port = socket2port;
    }
}
