package server.imps;

import utils.ClazzUtil;
import utils.Command;
import utils.LOG;
import utils.ThreadUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by user on 2017/5/24.
 */
public class ServerReceiver extends Thread implements Command.SelectAction {
    private ServerImps server;
    private  ByteBuffer buf = ByteBuffer.allocate(Command.BUFF_LENGTH);
    public ServerReceiver(ServerImps server) {
        this.server = server;
    }
    @Override
    public void run() {
        while(true){
                receives();
        }

    }
    private void receives(){
      Command.loopSelect(server.selector,this);
    }
    @Override
    public void selectAction(SelectionKey key) {
        if (key == null) return;
        try {
            DatagramChannel sc = (DatagramChannel) key.channel();
            buf.clear();
            InetSocketAddress socketAddress = (InetSocketAddress) sc.receive(buf);
            buf.flip();
            byte[] datas = new byte[buf.limit()];
            while (buf.hasRemaining()) {
                buf.get(datas);// read 1 byte at a time
            }
            buf.clear();
            ArrayList<Object> dataList = Command.parseDatas(datas);
            if (dataList == null) return;
            final String command = server.commandManager.getExcute((byte) dataList.get(0));
            InetAddress inetAddress = socketAddress.getAddress();
            int inetPort = socketAddress.getPort();
            if ((byte) dataList.get(0) != Command.HRBT && (byte) dataList.get(0) != Command.CLIENT_CONNECT_DATA_CHANNEL){
                LOG.I("服务器接受>>> "+inetAddress+":"+inetPort +" 数据: "+ dataList);
            }

            final  Object paramList = new Object[]{server,inetAddress,inetPort,dataList.get(2),sc};
            ClazzUtil.createClazzInvokeMethod(command,"handlerCommand",new Class[]{Object[].class},new Object[]{paramList});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *  ThreadUtil.runing(new Runnable() {
    @Override
    public void run() {

    }
    });
     */









}
