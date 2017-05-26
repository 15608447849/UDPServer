package CommandHandler.imps;

import CommandHandler.ICommand;
import server.beans.UdpClient;
import server.imps.ServerImps;
import utils.Command;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * Created by user on 2017/5/25.
 * 处理某一个客户端回复资源信息
 * {SOURCE_NOTITY,数据长度,"请求资源的MAC@存在资源的mac@请求的资源名本地完整路径@状态码"}
 */
public class ClientSouceResponse implements ICommand {
    @Override
    public void handlerCommand(Object[] objects) {
        ServerImps serverImps = (ServerImps) objects[0];
        InetAddress senderIp = (InetAddress) objects[1];
        int senderPort = (int) objects[2];
        String data = (String) objects[3];
        DatagramChannel channel = (DatagramChannel) objects[4];
        String[] dataArray = data.split(Command.SEPARATOR);
            String dMac = dataArray[0];
            String sMac = dataArray[1];
            String sourcePath = dataArray[2]; //资源在客户端本地的路径 -> 告知索取者
            int state = Integer.parseInt(dataArray[3]);
            //set状态
            serverImps.opration.setClientState(sMac,state);
            //设置通道绑定
            UdpClient client = serverImps.opration.bindP2P(dMac,sMac);
            if (client!=null){
                String message = sourcePath;
                Command.sendMessage(channel,new InetSocketAddress(client.inetAddress,client.inetPort),Command.ABOUT_SOUCE_PATH,sourcePath);
            }
    }
}
