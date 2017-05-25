package CommandHandler.imps;

import CommandHandler.ICommand;
import client.imps.ClientImps;
import client.threads.ClientThread;
import client.threads.imps.DataConnectSend;
import utils.Command;
import utils.FindFileVisitor;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;

/**
 * Created by user on 2017/5/25.
 * 客户端查询某一个资源
 *  在本地文件中匹配是否存在资源
 * {NOTIFY_ALL_CLIENT_SOURCE,索取者MAC@资源名}
 */
public class ClientSourceLocalFind implements ICommand {
    @Override
    public void handlerCommand(Object[] objects) {
        // final  Object paramList = new Object[]{client,inetAddress,inetPort,dataList.get(2),sc};

        ClientImps client = (ClientImps) objects[0];
        InetSocketAddress serverSocketAddress = new InetSocketAddress((InetAddress)objects[1],(int)objects[2]);

        String[] arr =  ((String)objects[3]).split(Command.SEPARATOR);
        String dMac = arr[0];
        String fileName = arr[1];
        DatagramChannel channel = (DatagramChannel) objects[4];

        ClientThread thread = client.threadMap.get("respond");
        if (thread!=null) return;
        //开始查询
        ArrayList<String> arrayList = new FindFileVisitor(Command.HOME_DIR).setFindFileName(fileName).find();
        if (arrayList.size() > 0){
            //取第一个
            //告知服务器,并设置自己的状态 -> 请求中  {索取者MAC,本地mac,文件本地完整路径,自己的状态码} ->打开数据传输线程,准备接通服务器 ( 服务器将绑定我的mac和对方mac , 进行ip端口交换,并设置状态为 传输中,防止其他客户端找到相同文件,破坏通道)
            String message = dMac + Command.SEPARATOR + client.info.macAddress + Command.SEPARATOR + arrayList.get(0) + Command.SEPARATOR + 1;
            Command.sendMessage(channel,serverSocketAddress,Command.SOURCE_NOTITY,message,null);//发送消息给服务器
            //尝试连接服务器
            try {
                DataConnectSend sendThread = new DataConnectSend(client);
                client.threadMap.put("respond",sendThread);
                sendThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
