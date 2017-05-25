package CommandHandler;

import utils.Command;
import utils.LOG;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/5/24.
 */
public class CommandManager {
    private HashMap<Byte,String> hashMap = new HashMap();
    private ReentrantLock lock = new ReentrantLock();
    private final String clazzPath = "CommandHandler.imps.";
    private CommandManager(){
        //服务器处理命令
        hashMap.put(Command.HRBT,clazzPath+"Command_HRBT");
        hashMap.put(Command.CLIENT_SERVER_QUESY_SOURCE,clazzPath+"ClientSourceRequest");//服务器收到某客户端的资源请求
        hashMap.put(Command.SOURCE_NOTITY,clazzPath+"ClientSouceResponse");//服务器收到某客户端的资源请求查询成功回执
        //服务区 数据端口
        hashMap.put(Command.CLIENT_CONNECT_DATA_CHANNEL,clazzPath+"ClientConnectDataPort");

        //一个客户端请求数据端口 查询bind关系,互发端口
        hashMap.put(Command.NOTIFY_DATA_PORT,clazzPath+"ClientQuestBindClient");

        //客户端处理命令
        hashMap.put(Command.NOTIFY_ALL_CLIENT_SOURCE,clazzPath+"ClientSourceLocalFind");//客户端查询文件
        hashMap.put(Command.ABOUT_SOUCE_PATH,clazzPath+"ClientSettingRemotePath");//索取资源客户端设置远程资源路径

        LOG.I("命令管理器初始化完成.");
    }
    private static class Holder{
        private static CommandManager manager = new CommandManager();
    }
    public static CommandManager get(){
        return Holder.manager;
    }

    public String getExcute(Byte key){
        try {
            lock.lock();
            if (hashMap.containsKey(key)){
                return hashMap.get(key);
            }
//            LOG.I("查询 - 失败:"+hashMap);
            return null;
        }finally {
            lock.unlock();
        }
    }













}
