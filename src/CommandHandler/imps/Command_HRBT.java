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
 * Created by user on 2017/5/24.
 * 心跳
 *  {0,长度,mac}
 */
public class Command_HRBT implements ICommand {
    @Override
    public void handlerCommand(Object[] objects) {
        try{
            ServerImps serverImps = (ServerImps) objects[0];
            InetAddress senderIp = (InetAddress) objects[1];
            int senderPort = (int) objects[2];
            String data = (String) objects[3];
            DatagramChannel channel = (DatagramChannel) objects[4];
            //心跳, -> 添加用户 ->
            UdpClient client = serverImps.opration.addClient(senderIp,senderPort,data);//添加客户端
            if (client!=null){

                //返回结果给客户端 - 维持生命周期
                Command.sendMessage(channel,
                        new InetSocketAddress(client.inetAddress,client.inetPort),
                        Command.HRBT_RESP,
                        "success");
               // LOG.I("客户端存活 - "+client+" , 当前在线数:" + serverImps.opration.getOnliNunber() + channel);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
