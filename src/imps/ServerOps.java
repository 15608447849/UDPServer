package imps;

import beans.UdpClient;
import itface.ServerInerface;
import itface.ServerOpration;

import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by lzp on 2017/5/23.
 *
 */
public class ServerOps extends HashSet implements ServerOpration , CheckThread.Action{
    private ReentrantLock lock = new ReentrantLock();

    public ServerOps() {
      new CheckThread(this,10);
    }

    //添加客户端
    @Override
    public boolean addClient(UdpClient client) {
        try {
            lock.lock();
            if (contains(client)){
                this.remove(client);
            }
            return add(client);
        }finally {
            lock.unlock();
        }
    }

    //当前在线数
    @Override
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
    @Override
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


}
