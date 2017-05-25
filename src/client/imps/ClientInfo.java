package client.imps;

import utils.NetUtil;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by user on 2017/5/24.
 */
public class ClientInfo {


    /**
     *
     * 服务器地址
     */
    public InetAddress serverIp;
    /**
     * 和服务器通讯的端口
     */
    public int serverPort;
    /**
     * 和服务器搭建数据通道的端口
     */
    public int serverDataPort;
    /**
     * 本地IP
     */
    public InetAddress localIp;

    /**
     * 和服务器通讯的本地端口
     */
    public int localPort;
    /**
     * 数据交互的端口
     */
    public int dataPort;

    public String macAddress;


    public ClientInfo(String serverIp, int serverPort, int serverDataPort, String localIp, int localPort, int dataPort) throws SocketException, UnknownHostException {
        this.serverIp = InetAddress.getByName(serverIp);
        this.serverPort = serverPort;
        this.serverDataPort = serverDataPort;
        this.localIp = InetAddress.getByName(localIp);
        this.localPort = localPort;
        this.dataPort = dataPort;
        this.macAddress = NetUtil.getMACAddress(this.localIp);
    }




}
