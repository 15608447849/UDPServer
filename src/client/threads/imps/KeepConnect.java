package client.threads.imps;

import client.imps.ClientImps;
import client.threads.ClientThread;
import utils.Command;
import utils.LOG;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;


/**
 * Created by user on 2017/5/24.
 */
public class KeepConnect extends ClientThread {
    private int time = 5;
    private InetSocketAddress serverAddress;
    public KeepConnect(ClientImps client) throws UnsupportedEncodingException {
        super(client);
        //制作心跳数据
        channel = client.commChannel;
        serverAddress = new InetSocketAddress(client.info.serverIp,client.info.serverPort);
        LOG.E(" HRBT - "+serverAddress);
    }
    @Override
    protected void sendHrbt() {
        //发送心跳
       Command.sendMessage(channel,serverAddress,Command.HRBT,client.info.macAddress);
    }

    @Override
    public void run() {
        while (true){
            synchronized (channel){
                try {
                    if (flag){
                        //发送心跳
                        sendHrbt();
                    }
                    channel.wait(time * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
