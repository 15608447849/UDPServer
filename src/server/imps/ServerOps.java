package server.imps;

import server.beans.*;
import utils.Command;
import utils.LOG;

import javax.swing.text.html.parser.Entity;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by lzp on 2017/5/23.
 *
 */
public class ServerOps extends HashSet implements CheckThread.Action{
    private ReentrantLock lock = new ReentrantLock();
    public ServerOps() {
      new CheckThread(this,10);
    }
    //数据传输绑定列表
    private HashMap<UdpClient,UdpClient> dataConnBindMap = new HashMap<>();

    //添加客户端
    public UdpClient addClient(InetAddress inetAddress, int inetPort, String mac) {
        try {
            lock.lock();
            Iterator<UdpClient> itr = iterator();
            UdpClient client ;
            while (itr.hasNext()){
                client = itr.next();
                if (mac.equalsIgnoreCase(client.macAddress)){
                    client.inetAddress = inetAddress;
                    client.inetPort = inetPort;
                    client.update();
                    return client;
                }
            }
            client = new UdpClient(inetAddress,inetPort,mac);
            if (add(client)){
                return client;
            }
            return null;
        }finally {
            lock.unlock();
        }
    }

    //当前在线数
    public int getOnliNunber() {
        return size();
    }


    /**
     * 检测存活通道
     */
    @Override
    public void check() {
        checkClient();
    }
    public void checkClient() {

        try{
            lock.lock();
            Iterator<UdpClient> itr = iterator();
            while (itr.hasNext()){
               if ((System.currentTimeMillis() - itr.next().updateTime)> (10 * 1000L)){
                   itr.remove();
               }
            }
        }finally {
            lock.unlock();
        }

    }
    //设置客户端状态
    public void setClientState(String cMac, int state) {
        try{
            lock.lock();
            Iterator<UdpClient> itr = iterator();
            UdpClient client;
            while (itr.hasNext()){
               client = itr.next();
               if (client.macAddress.equalsIgnoreCase(cMac)){
                   client.state = state;
                   LOG.E(client.toString());
               }
            }
        }finally {
            lock.unlock();
        }
    }
    //通知除指定mac外的所有 空闲用户
    public void notifyAllClientMessage(DatagramChannel channel, String cMac, byte type, String message) {
        try{
            lock.lock();
            LOG.I("通知所有人 - "+message);
            Iterator<UdpClient> itr = iterator();
            UdpClient client;
            while (itr.hasNext()){
               client = itr.next();
               if (client.macAddress.equalsIgnoreCase(cMac)){
                   continue;
               }else{
                   if (client.state == 0){ //空闲的
                       Command.sendMessage(channel,new InetSocketAddress(client.inetAddress,client.inetPort),type,message);
                   }
               }
            }
            LOG.I("通知所有人 - "+message + " 完毕");
        }finally {
            lock.unlock();
        }
    }

    //两个客户端绑定数据通道
    public UdpClient bindP2P(String dMac, String sMac) {

        try{
            lock.lock();
            Iterator<UdpClient> itr = iterator();
            UdpClient clientDest = null,clientSorc = null;
            UdpClient client;
            while (itr.hasNext()){
                client = itr.next();
                if (client.macAddress.equalsIgnoreCase(dMac)){
                    clientDest = client;
                }else if (client.macAddress.equals(sMac)){
                    clientSorc = client;
                }
            }
            if (clientDest!=null && clientSorc!=null && clientDest.state==2){
                 //请求资源者端口准备就绪
                    dataConnBindMap.put(clientSorc,clientDest);
                    LOG.I(clientSorc+"  绑定 "+clientDest);
                    return clientDest;//索取者客户端

            }
            return null;
        }finally {
            lock.unlock();
        }



    }

    public Map.Entry<UdpClient,UdpClient> findBindMac(String sMac) {
        try{
            lock.lock();
            Iterator<Map.Entry<UdpClient,UdpClient>> itr = dataConnBindMap.entrySet().iterator();
            Map.Entry<UdpClient,UdpClient> entry;
            while (itr.hasNext()){
                entry = itr.next();

                if (entry.getKey().macAddress.equalsIgnoreCase(sMac)){
                    itr.remove();// //删除绑定信息
                    return entry;
                }
            }
            return null;
        }finally {
            lock.unlock();
        }
    }
    //设置数据端口
    public UdpClient setClientDataPort(String mac, int dataPort) {
        try{
            lock.lock();
            Iterator<UdpClient> itr = iterator();
           UdpClient client;
            while (itr.hasNext()){
              client =  itr.next();
              if (client.macAddress.equalsIgnoreCase(mac) && client.state==1){//请求中->连接上数据端口|| client.state == 2
                  client.dataPort = dataPort;
                  client.state = 2;//已设置数据端口
                  return client;
              }
           }
           return  null;
        }finally {
            lock.unlock();
        }
    }

    public UdpClient setClientDataPortClose(String mac) {
        try{
            lock.lock();
            Iterator<UdpClient> itr = iterator();
            UdpClient client;
            while (itr.hasNext()){
                client =  itr.next();
                if (client.macAddress.equalsIgnoreCase(mac) && client.state== 2){//请求中->连接上数据端口
                    client.dataPort = 0;
                    client.state = 0;//空闲
                    return client;
                }
            }
            return  null;
        }finally {
            lock.unlock();
        }
    }
}
