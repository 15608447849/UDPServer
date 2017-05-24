package beans;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by user on 2017/5/22.
 */
public class UdpServer{
    public InetAddress localIp;
    public int socketPort = 8000;

    public UdpServer(String localIp, int socketPort) throws UnknownHostException {
        this.localIp = InetAddress.getByName(localIp);
        this.socketPort = socketPort;

    }
}
