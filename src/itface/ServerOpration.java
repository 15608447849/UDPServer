package itface;

import beans.UdpClient;

/**
 * Created by user on 2017/5/23.
 */
public interface ServerOpration {
    boolean addClient(UdpClient client);
    int getOnliNunber();
    void checkClient();
}
