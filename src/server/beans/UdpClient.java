package server.beans;

import java.net.InetAddress;

/**
 * Created by lzp on 2017/5/22.
 * udp客户端
 */
public class UdpClient {

    /**
     * net地址
     */
    public InetAddress inetAddress;
    /**
     * net端口
     */
    public int inetPort;
    /**
     * 客户端mac地址
     * @param localIp
     * @param localPort
     */
    public String macAddress;
    //数据端口
    public int dataPort;
    /**
     * 空闲0 - 请求中1 传输中2
     */
    public int state = 0;

    //更新时间
    public long updateTime;

    public void update(){
        updateTime = System.currentTimeMillis();
    }

    public UdpClient(InetAddress inetAddress, int inetPort, String macAddress) {
        this.inetAddress = inetAddress;
        this.inetPort = inetPort;
        this.macAddress = macAddress;
        update();
    }

    @Override
    public String toString() {
        StringBuffer sb =new StringBuffer();
        sb.append("[ mac = "+ macAddress);
        sb.append("; netIp = "+ inetAddress);
        sb.append("; netPort = "+ inetPort);
        sb.append("]");
        return sb.toString();
    }
}
