package client.threads.imps;

import client.imps.ClientImps;
import client.threads.ClientThread;
import utils.ClazzUtil;
import utils.Command;
import utils.LOG;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by user on 2017/5/24.
 */
public class receiveServerMessage extends ClientThread implements Command.SelectAction {

    public receiveServerMessage(ClientImps client) {
        super(client);
        buffer = ByteBuffer.allocate(Command.BUFF_LENGTH);
    }
    @Override
    protected void receiveMessage() {

        Command.loopSelect(client.selector,this);

    }


    @Override
    public void run() {
        LOG.I("启动数据接收.");
        while (true){
            receiveMessage();
        }
    }

    @Override
    public void selectAction(SelectionKey key) {
        try {
            buffer.clear();
            DatagramChannel sc = (DatagramChannel) key.channel();
            InetSocketAddress socketAddress = (InetSocketAddress) sc.receive(buffer);
            buffer.flip();
            byte[] datas = new byte[buffer.limit()];
            while (buffer.hasRemaining()) {
                buffer.get(datas);
            }
            buffer.clear();
            LOG.I("客户端,收到:"+ Arrays.toString(datas));
            ArrayList<Object> dataList = Command.parseDatas(datas);
            if (dataList==null) return;
            final String command = client.commandManager.getExcute((byte) dataList.get(0));
            if (command == null) return;
            InetAddress inetAddress = socketAddress.getAddress();
            int inetPort = socketAddress.getPort();
            LOG.I("客户端,收到 : "+inetAddress+":"+inetPort +" 数据: "+ dataList);

            final  Object paramList = new Object[]{client,inetAddress,inetPort,dataList.get(2),sc};
            ClazzUtil.createClazzInvokeMethod(command,"handlerCommand",new Class[]{Object[].class},new Object[]{paramList});
         } catch (IOException e) {
         e.printStackTrace();
         }

    }
}
