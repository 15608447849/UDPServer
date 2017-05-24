import imps.ServerImps;
import imps.ServerOps;
import itface.ServerOpration;
import utils.LOG;
import utils.NetUtil;

import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) {
        String ip = (args!=null && args.length>=1)?args[0]:NetUtil.getLocalIP();
        LOG.I("设置IP - "+ip);
        try {
            beans.UdpServer updServer = new beans.UdpServer( ip,9999);
            ServerOpration opser = new ServerOps();
            ServerImps.get().init(updServer).connectOps(opser).startServer();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }
}
