package server.beans;

import utils.NetUtil;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by lzp on 2017/5/22.
 */
public class UdpServer{
    public InetAddress localIp;
    public int socketPort; //通讯接口
    public int socket2port;//通道搭建接口
    public String macAddress;
    public UdpServer(String localIp, int socketPort,int socket2port) throws UnknownHostException, SocketException {
        this.localIp = InetAddress.getByName(localIp);
        this.socketPort = socketPort;
        this.socket2port = socket2port;
        macAddress = NetUtil.getMACAddress(InetAddress.getByName(localIp));
    }
}
