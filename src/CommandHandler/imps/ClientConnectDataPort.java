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
        int dataPort = (int)objects[2];
        String mac = ((String) objects[3]).split(Command.SEPARATOR)[0];
        DatagramChannel channel = (DatagramChannel) objects[4];
//        LOG.I("客户端连接数据端口 -- "+mac + " - port" +dataPort);
        UdpClient client = server.opration.setClientDataPort(mac,dataPort);
        if (client==null) return;
        Command.sendMessage(channel,new InetSocketAddress(client.inetAddress,client.dataPort),Command.HRBT_DATA,"success.");
    }
}
