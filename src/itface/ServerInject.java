package itface;

import beans.UdpServer;
import itface.ServerOpration;

/**
 * Created by user on 2017/5/23.
 */
public interface ServerInject {
    /**
     * 初始化
     * @param server
     * @return
     */
    ServerInerface init(UdpServer server);

    /**
     * 关联操作
     * @param ops
     * @return
     */
    ServerInerface connectOps(ServerOpration ops);
}
