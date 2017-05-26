package CommandHandler.imps;

import CommandHandler.ICommand;
import server.beans.UdpClient;
import server.imps.ServerImps;
import utils.Command;
import utils.LOG;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * Created by user on 2017/5/25.
 * CLIENT_CONNECT_DATA_CHANNEL
 */
public class ClientConnectDataPort implements ICommand {
    @Override
    public void handlerCommand(Object[] objects) {
        //客户端链接 数据端口
        ServerImps server = (ServerImps) objects[0];
        InetAddress address = (InetAddress) objects[1];
        int dataPort = (int)objects[2];
        String mac = ((String) objects[3]).split(Command.SEPARATOR)[0];
        int tag = Integer.parseInt(((String) objects[3]).split(Command.SEPARATOR)[1]);
        if (tag == 1){
            DatagramChannel channel = (DatagramChannel) objects[4];
//        LOG.I("客户端连接数据端口 -- "+mac + " - port" +dataPort);
            UdpClient client = server.opration.setClientDataPort(mac,dataPort);
            if (client!=null){
                LOG.I("客户端连接数据端口成功:\n"+client);
            }
            //回复心跳
            Command.sendMessage(channel,new InetSocketAddress(address,dataPort),Command.HRBT_DATA);
        }else if (tag == 0){
            //关闭通道
            UdpClient client = server.opration.setClientDataPortClose(mac);
            if (client!=null){
                LOG.I("客户端连接数据端口关闭,已空闲:\n"+client);
            }
        }

    }
}
