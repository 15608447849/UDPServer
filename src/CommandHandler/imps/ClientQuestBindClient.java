package CommandHandler.imps;

import CommandHandler.ICommand;
import server.beans.UdpClient;
import server.imps.ServerImps;
import utils.Command;
import utils.LOG;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Map;

/**
 * Created by user on 2017/5/25.
 * NOTIFY_DATA_PORT
 */
public class ClientQuestBindClient implements ICommand {
    @Override
    public void handlerCommand(Object[] objects) {

        //数据端口收到了  资源存在客户端发来的请求, 我要查询绑定的队列,是否存在这个mac对应的客户端, 拿到绑定对象,互换IP PORT
        ServerImps server = (ServerImps) objects[0];
        InetSocketAddress clientAddress = new InetSocketAddress((InetAddress) objects[1],(int)objects[2]);
        String sMac = (String) objects[3];
        DatagramChannel channel = (DatagramChannel) objects[4];

        LOG.I("资源 源地址:"+sMac +"请求绑定的目的地,互换信息!" );
        synchronized (this){
            try {
                this.wait( 1000 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Map.Entry<UdpClient,UdpClient> entry =  server.opration.findBindMac(sMac);
        LOG.E("是否获取到一组绑定的信息 >> "+entry);
        if (entry!=null){
            //互换
            UdpClient scr = entry.getKey();
            UdpClient des = entry.getValue();
            Command.sendMessage(channel,new InetSocketAddress(scr.inetAddress,scr.dataPort),Command.SOUCE_QUERY_SUCCESS,des.inetAddress.getHostAddress()+Command.SEPARATOR+des.dataPort);
            LOG.I("告知 资源存在者 ->"+ des.inetAddress+"-"+des.dataPort);

            synchronized (this){
                try {
                    this.wait( 100 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Command.sendMessage(channel,new InetSocketAddress(des.inetAddress,des.dataPort),Command.SOUCE_QUERY_SUCCESS,scr.inetAddress.getHostAddress()+Command.SEPARATOR+scr.dataPort);
            LOG.I("告知 资源索取者 ->"+ scr.inetAddress+"-"+scr.dataPort);

        }
    }
}
