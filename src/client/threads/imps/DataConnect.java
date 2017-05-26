package client.threads.imps;

import client.imps.ClientImps;
import client.threads.ClientThread;
import utils.Command;
import utils.LOG;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Arrays;

/**
 * Created by user on 2017/5/24.
 */
public class DataConnect extends ClientThread {

    public InetSocketAddress targetAddress;
    public InetSocketAddress serverAddress;
    public String macAddress;
    public String source;
    public String localPath;
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


    //保存通讯
    public Thread hebtThread = new Thread(new Runnable() {


        @Override
        public void run() {

            ByteBuffer buff =ByteBuffer.wrap(new byte[1]);
            byte tag = 77;
            buff.put(tag);
            while (flag){
                synchronized (buff){
                    try {
                        buff.wait(30 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (targetAddress!=null){
                    buff.rewind();
                    try {
                        channel.send(buff,targetAddress);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    });

    public void startHrbt(){
        hebtThread.start();
    }



    public DataConnect(ClientImps client) throws IOException {
        super(client);
        this.serverAddress = new InetSocketAddress(client.info.serverIp,client.info.serverDataPort);//数据端口
        this.macAddress = client.info.macAddress;//本机mac
        channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(client.info.localIp,client.info.dataPort));
        buffer = ByteBuffer.allocate(Command.DATA_BUFF_LENGTH);
        LOG.I("数据端口的连接创建完成. "+ serverAddress);
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
            if (state == 1 || state == 10 || state==0){
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
            byte[] bytes = null;
            if (state==1 || state==0){
                bytes = Command.createDatas(Command.CLIENT_CONNECT_DATA_CHANNEL, macAddress+Command.SEPARATOR+state);
                LOG.I("向服务器告知状态..: "+state);
                state = -1;//等待服务器信息中
            }else if (state == 10){
                //请求服务器 ->
                bytes = Command.createDatas(Command.NOTIFY_DATA_PORT, macAddress);
                LOG.I("请求服务器 互换双方地址信息.建立通讯");
            }
            if (serverAddress!=null && bytes!=null){
                createDatas(bytes);
                channel.send(buffer,serverAddress);
            }
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
            byte[] bytes = null;
            if (state == 2){
                //握手中 {syn,长度,mac}
                bytes = Command.createDatas(Command.SYN, macAddress);
                LOG.I("向 "+ targetAddress +"发送握手包");
            }else
            if (state == 3){
                bytes = Command.createDatas(Command.AKC, source);//发送资源在其本地的全路径
                 LOG.I("向 "+ targetAddress +"发送资源文件名:"+source);
            }else if (state == 4){
                //发送postion
                bytes = Command.createDatas(Command.SAVE, position);//发送下标
                LOG.I("发送下标:"+position);
            }else if (state == 5){
                //通知目标关闭通道
                bytes = Command.createDatas(Command.CLOSE,macAddress);
                state = 0;
                LOG.I("关闭传输");
            }else
            if (state == 11){
                //发送握手包
                bytes = Command.createDatas(Command.SYN, macAddress);
                LOG.I("资源发送者 - 发送握手包 -> "+targetAddress);
            }else
            if (state == 12){
                //接受到握手包发送回执
                bytes = Command.createDatas(Command.AKC, macAddress);
            }else
            if (state == 13){
                //发送 文件 大小
                bytes = Command.createDatas(Command.FLG,fileSize);
                LOG.I("发送文件大小 : "+fileSize);
            }

            if (targetAddress!=null && bytes!=null){
                createDatas(bytes);
                channel.send(buffer,targetAddress);
            }
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
    
    public void getData() throws IOException {
        if (state==0) return;
        //处理结果
        buffer.clear();
        SocketAddress address = channel.receive(buffer);
        buffer.flip();
        byte tag = buffer.get();
        LOG.I("来自 - "+address.toString() +" 数据标识 :"+ tag);
        buffer.rewind();
    }
    
    @Override
    protected void receiveMessage() {
        try {
            getData();
            byte[] bytes = buffer.array();
            byte command =bytes[0];
            if (command == Command.SOUCE_QUERY_SUCCESS){
                //对方的IP地址 {SOUCE_QUERY_SUCCESS,长度,"B_IP@B_port"}
                    int dataLenth = Command.bytesToInt(bytes,1);
                    String string = Command.bytesToString(bytes,5,dataLenth);
                    LOG.I("资源请求者,收到对方的地址信息:"+string);
                    String[] sarr = string.split(Command.SEPARATOR);
                    targetAddress = new InetSocketAddress(sarr[0],Integer.parseInt(sarr[1]));

                    //接下来尝试给对方发送握手包
                    state = 2;
            }else if (command == Command.AKC){
                //收到握手包回执 里面有对方的mac信息 - 通道打通
                LOG.I("收到握手回执 - 通道建立成功");
                state = 3;
                client.stopServerHrbt();//停止服务器心跳
            }else if (command == Command.FLG){
                //携带资源的总大小 {FLG,文件大小long}
                fileSize =Command.bytesToLong(bytes,1);
                LOG.I("收到资源文件长度 : "+ fileSize+" , 资源本地文件保存路径:"+localPath);
                //对方发送的此文件大小
                if (rafile==null && fileChannel==null) {
                    rafile = new RandomAccessFile(localPath, "rw");
                    rafile.setLength(fileSize);
                    fileChannel = rafile.getChannel();//文件管道
                    state = 4;//开始传输 -> 发送下标
                    LOG.I("文件流已打开,发送下载点");
                }

            }else if (command == Command.DATA){
                //数据 {data,长度, ~~~~~~~~~~~~ ****}
                LOG.I("接受到数据 :"+ Arrays.toString(bytes));
                int dataSize = Command.bytesToInt(bytes,1);
                LOG.I("长度 :"+dataSize);
                String checkSpc = null;//检测符号
                try {
                    checkSpc = new String(bytes,5+dataSize, Command.DATA_SEPARATOR.getBytes().length);
                } catch (Exception e) {
                    checkSpc = "error";
                }
                LOG.I("checkSpc : "+checkSpc);
                if (checkSpc.equals(Command.DATA_SEPARATOR)){
                    LOG.I("开始写入进度");
                    lock = fileChannel.lock();//文件上锁
                    ByteBuffer buf = ByteBuffer.allocate(dataSize);
                    buf.put(bytes,5,dataSize);
                    buf.flip();
                    fileChannel.position(position);
                    fileChannel.write(buf);
                    buf.clear();
                    position+=dataSize;
                    lock.release();
                    LOG.I("写入进度:"+ dataSize +" , 当前pos:" +position+",文件总大小:"+fileSize);
                    if (position==fileSize){
                        state = 5;//结束下载
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void stopSelf() {
        super.stopSelf();
        if (rafile!=null){
            try {
                rafile.close();
            } catch (IOException e) {
            } finally {
                rafile = null;
            }
        }
        if (fileChannel!=null){
            try {
                fileChannel.close();
            } catch (IOException e) {
            } finally {
                fileChannel = null;
            }
        }
        deleteOnMap();
        client.starServerHrbt();//开始心跳
    }
    public void deleteOnMap(){
        client.threadMap.remove("request");
    }

    private void createDatas(byte[] bytes){
        buffer.clear();
        buffer.put(bytes);
        buffer.flip();
    }
}
