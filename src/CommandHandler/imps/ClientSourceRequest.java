package CommandHandler.imps;

import CommandHandler.ICommand;
import server.imps.ServerImps;
import utils.Command;

import java.net.InetAddress;
import java.nio.channels.DatagramChannel;

/**
 * Created by user on 2017/5/24.
 * 客户端请求服务器一个资源文件
 * {5,0,0,0,50,"客户端MAC地址@请求的文件名@状态码"}
 */
public class ClientSourceRequest  implements ICommand {

    @Override
    public void handlerCommand(Object[] objects) {
        try{
            ServerImps serverImps = (ServerImps) objects[0];
            InetAddress senderIp = (InetAddress) objects[1];
            int senderPort = (int) objects[2];
            String data = (String) objects[3];
            DatagramChannel channel = (DatagramChannel) objects[4];
            String[] dataArray = data.split(Command.SEPARATOR);
            String cMac = dataArray[0];
            String souce = dataArray[1];
            int state = Integer.parseInt(dataArray[2]);
            //查询这个客户端 设置状态
            settingClientState(serverImps,cMac,state);
            //通知除这个客户端之外的所以客户端
            notifyAllClient(serverImps,channel,cMac,souce);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void settingClientState(ServerImps serverImps, String cMac, int state) {
        serverImps.opration.setClientState(cMac,state);
    }
    private void notifyAllClient(ServerImps serverImps,DatagramChannel channel, String cMac, String souce) {
        String message =cMac+Command.SEPARATOR+souce;// 目的mac@文件名
        serverImps.opration.notifyAllClientMessage(channel,cMac,Command.NOTIFY_ALL_CLIENT_SOURCE,message);
    }
}
