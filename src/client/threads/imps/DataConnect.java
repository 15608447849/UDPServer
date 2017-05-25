package client.threads.imps;

import client.imps.ClientImps;
import client.threads.ClientThread;
import utils.Command;
import utils.LOG;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Created by user on 2017/5/24.
 */
public class DataConnect extends ClientThread {

    public InetSocketAddress targetAddress;
    public InetSocketAddress serverAddress;
    public String macAddress;
    public String source;
    public String localPath;
    public byte[] bytes;
    public RandomAccessFile rafile;
    public FileChannel fileChannel;
    public FileLock lock;
    public long fileSize = 0L;
    public long position = 0L; //当前文件流标记点
    /**
     * 接受数据者
     * 1 和服务器数据端口连接中
     * 2 收到资源客户端地址 >发送握手包
     * 3 收到客户端回执 > 发送资源文件名
     * 4 收到文件大小 > 第一次发送起始点 >
     *  接受数据 > 开始第一段数据的接受.>发送position..最后(循环)
     * 5 数据接受,通知对方关闭连接,通知服务,改变状态,关闭端口
     * 
     * 发送数据者
     * 1 尝试链接服务器 ->得到服务器回执 -> 请求服务器去绑定队列查询 ,互换双方端口 -> 尝试发送握手包,等待对方握手包,
     * ->收到对方的握手包 发送握手回执 -> 收到资源文件本地路径(设置路径)->发送文件大小 -> 接受文件起始点 ,开始发送数据...
     * 
     */
    public int state = 1;
    public DataConnect(ClientImps client) throws IOException {
        super(client);
        this.serverAddress = new InetSocketAddress(client.info.serverIp,client.info.serverDataPort);//数据端口
        this.macAddress = client.info.macAddress;//本机mac
        channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(client.info.localIp,client.info.dataPort));
        buffer = ByteBuffer.allocate(Command.DATA_BUFF_LENGTH);
        LOG.I("建立 和服务器 数据端口的连接创建完成. "+ serverAddress +" data channael: "+channel + " "+buffer);
    }

    @Override
    public void tranlslete(String path) {
        source = path;
        LOG.I("资源在远程的路径: "+source);
    }

    @Override
    public void run() {
        //建立连接 1 -> 发送心跳 根据返回值处理 :
        while (flag){
            if (state == 1 || state == 0){
                sendDataToServer();
            }else{
                sendDataToTarget();
            }
                receiveMessage();
        }
    }

    //发送数据到服务器数据管道保持连接
    public void sendDataToServer() {
        try {
            bytes = Command.createDatas(Command.CLIENT_CONNECT_DATA_CHANNEL, macAddress+Command.SEPARATOR+state);
            Command.createDatas(bytes,buffer);
            channel.send(buffer,serverAddress);
//            LOG.I("发送给服务器消息:. "+ Command.CLIENT_CONNECT_DATA_CHANNEL );
            if (state == 0){
                stopSelf();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //发送给目标对象
    public void sendDataToTarget() {
        try {
            if (state == 2){
                //握手中 {syn,长度,mac}
                bytes = Command.createDatas(Command.SYN, macAddress);
                LOG.I("向 "+ targetAddress +"发送握手包");
            }else
            if (state == 3){
                bytes = Command.createDatas(Command.AKC, source);//发送资源文件名

            }else if (state == 4){
                //发送postion
                bytes = Command.createDatas(Command.SAVE, position);//发送下标

            }else if (state == 5){
                //通知目标关闭通道
               bytes = Command.createDatas(Command.CLOSE,macAddress);
            }
            Command.createDatas(bytes,buffer);
            channel.send(buffer,targetAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  // 客户端接受心跳 - {2}
     //客户端发送 对方的 ip,端口 {20,长度,"ip@端口"}
     //目标尝试请求包 - {50}
     //目标发送收到握手包回执{61}
     //目标发送数据{100,文件起始位置,文件终止位置,文件长度,..数据..}  ->检查数据长度是否和指定长度一样,不一样(设定的buff过小,则增大),等待下一次信息,成功保存,发送回执SAVE

     */
    
    public byte[] getData() throws IOException {
        //处理结果
        buffer.clear();
       channel.receive(buffer);
        buffer.flip();
        byte[] datas = new byte[buffer.limit()];
        while (buffer.hasRemaining()) {
            buffer.get(datas);
        }
        buffer.clear();
        return datas;
    }
    
    @Override
    protected void receiveMessage() {
        try {
            byte[] datas = getData();
            if (datas==null)
            {
                receiveMessage();
            return;
            }
            byte command = datas[0];
            if (command == Command.SOUCE_QUERY_SUCCESS){
                //对方的IP地址 {SOUCE_QUERY_SUCCESS,长度,"B_IP@B_port"}
                    int dataLenth = Command.bytesToInt(datas,1);
                    String string = Command.bytesToString(datas,5,dataLenth);
                    LOG.I("资源请求者,收到对方的信息:"+string);
                    String[] sarr = string.split(Command.SEPARATOR);
                    targetAddress = new InetSocketAddress(sarr[0],Integer.parseInt(sarr[1]));
                    //接下来尝试给对方发送握手包
                    state = 2;
            }else if (command == Command.AKC){
                //收到握手包回执 里面有对方的mac信息
                state = 3;
            }else if (command == Command.FLG){
                //携带资源的总大小 {FLG,文件大小long}
                fileSize =Command.bytesToLong(datas,1);
                LOG.I("文件长度 : "+ fileSize);
                rafile = new RandomAccessFile(localPath,"rw");
                rafile.setLength(fileSize);
                fileChannel = rafile.getChannel();//文件管道
                lock = fileChannel.lock();//文件上锁
                state = 4;
            }else if (command == Command.DATA){
                //数据 {data,长度, ~~~~~~~~~~~~ ****}
                int dataSize = Command.bytesToInt(datas,1);
                String checkSpc = new String(datas,dataSize,dataSize+4);//检测符号
                if (checkSpc.equals(Command.DATA_SEPARATOR)){
                    buffer.clear();
                    buffer.put(datas,5,dataSize);
                    buffer.flip();
                    fileChannel.write(buffer);
                    position+=dataSize;
                    if (position==fileSize){
                        state = 5;//结束下载
                        fileChannel.close();
                        rafile.close();
                        lock.release();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
