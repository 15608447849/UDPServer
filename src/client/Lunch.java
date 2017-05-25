package client;

import client.imps.ClientImps;
import client.imps.ClientInfo;
import client.threads.ClientThread;
import utils.NetUtil;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by user on 2017/5/24.
 */
public class Lunch {
    public static void clien(){
        try {
        String mip = NetUtil.getLocalIP();
        int port1 = 5000;
        int port2 = 6000;

//            开启一个文件请求
//            startFileQ();

        String serverIp = "39.108.87.46";
//        String serverIp = "172.16.0.200";
        int serverPort1 = 9999;
        int serverPort2 = 9998;

         ClientInfo info = new ClientInfo(serverIp,serverPort1,serverPort2,mip,port1,port2);
            ClientImps.get().initClient(info);
            ClientImps.get().startClient();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


    }

    private static void startFileQ() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ClientImps.get().requestSource("psb.jpg","C:/FileServerDirs/a.png");
            }
        }).start();
    }
}
