package CommandHandler.imps;

import CommandHandler.ICommand;
import client.imps.ClientImps;
import client.threads.ClientThread;

/**
 * Created by user on 2017/5/25.
 */
public class ClientSettingRemotePath implements ICommand {
    @Override
    public void handlerCommand(Object[] objects) {
        ClientImps client = (ClientImps) objects[0];
        String path = (String) objects[3];
        ClientThread thread = client.threadMap.get("request");
        if (thread!=null){
            thread.tranlslete(path);
        }
    }
}
