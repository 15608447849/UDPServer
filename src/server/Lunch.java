package server;

import server.beans.UdpServer;
import server.imps.ServerImps;
import server.imps.ServerOps;
import utils.NetUtil;

/**
 * Created by user on 2017/5/24.
 */
public class Lunch {
    public static void server(){

        try {
            UdpServer updServer = new UdpServer( NetUtil.getLocalIP(),9999,9998);
            ServerOps opser = new ServerOps();
            ServerImps.get().init(updServer).connectOps(opser).startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
