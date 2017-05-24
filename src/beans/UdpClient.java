package beans;

import java.net.InetAddress;
import java.util.Date;

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
     * 客户端内网地址
     */
    public InetAddress clientAddress;

    /**
     *  客户端内网端口
     * @param localIp
     * @param localPort
     */
    public int clientPort;

    /**
     * 客户端mac地址
     * @param localIp
     * @param localPort
     */
    public String clientMacAddress;
    //更新时间
    public long updateTime = System.currentTimeMillis();
    /**
     * 判断是否是同一个客户端
     */
    @Override
    public boolean equals(Object o) {
        if (o!=null && o instanceof UdpClient){

            UdpClient client = (UdpClient)o;

            return this.clientMacAddress.equalsIgnoreCase(client.clientMacAddress);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return clientMacAddress.hashCode();
    }

    @Override
    public String toString() {
        StringBuffer sb =new StringBuffer();
        sb.append("[ MAC = "+ clientMacAddress);
        sb.append("; netIp = "+ inetAddress);
        sb.append("; netPort = "+ inetPort);
        sb.append("; ip = "+ clientAddress);
        sb.append(" port = "+ clientPort);
        sb.append("]");
        return sb.toString();
    }
}
