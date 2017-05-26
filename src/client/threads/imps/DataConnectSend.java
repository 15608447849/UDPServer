package client.threads.imps;

import client.imps.ClientImps;
import client.threads.ClientThread;
import utils.Command;
import utils.LOG;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;

/**
 * Created by user on 2017/5/25.
 * 发送资源数据
 */
public class DataConnectSend extends DataConnect {
    private MappedByteBuffer mappedByteBuffer;
    public DataConnectSend(ClientImps client) throws IOException {
        super(client);
    }


    @Override
    public void run() {

        while (flag){
                if (state == 1 || state == 0){
                    //发送消息到服务器  - 接受一次心跳
                    sendDataToServer();
                }else if (state == 10){
                    questDataToServer();
                }else if (state > 10){
                    sendDataToTarget();
                }
                receiveMessage();
        }
    }

    private void questDataToServer() {
        try {
            //请求服务器 ->
            bytes = Command.createDatas(Command.NOTIFY_DATA_PORT, macAddress);
            Command.createDatas(bytes,buffer);
            channel.send(buffer,serverAddress);
            LOG.I("请求服务器 互换双方信息");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendDataToTarget() {
        try {
            if (state == 11){
                //发送握手包
                bytes = Command.createDatas(Command.SYN, macAddress);
                LOG.I("资源发送者 - 发送握手包  >> "+targetAddress);
            }
            if (state == 12){
                //接受到握手包发送回执
                bytes = Command.createDatas(Command.AKC, macAddress);
            }
            if (state == 13){
                //发送 文件 大小
                bytes = Command.createDatas(Command.FLG,fileSize);
            }if (state == 14){
                int len=0;
                //从下标开始获取数据
               if ((fileSize - position) > 1000){
                       len =1000;
               }else{
                   len = (int) (fileSize-position);
               }
               LOG.I("传输大小:"+len);
                byte[] lenby = Command.intToBytes(len);
                byte [] strArr = Command.DATA_SEPARATOR.getBytes();
                bytes = new byte[1+lenby.length+len+strArr.length];
                bytes[0] = Command.DATA;
                System.arraycopy(lenby, 0, bytes, 1, lenby.length); //
                mappedByteBuffer.get(bytes,1+lenby.length,len);

                System.arraycopy(strArr, 0, bytes, 1+lenby.length + len , strArr.length);
            }

            Command.createDatas(bytes,buffer);
            channel.send(buffer,targetAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void receiveMessage() {
        try {
            if (state==0) return;
            byte[] datas = getData();
            byte command = datas[0];
            LOG.I("命令:"+command);
            if (command == Command.HRBT_DATA){
                //收到服务心跳
                state = 10;
            }else if (command == Command.SOUCE_QUERY_SUCCESS){
                // {ip@port}
                int dataLenth = Command.bytesToInt(datas,1);
                LOG.I("资源发送者获取到对方的 信息 string length : "+dataLenth);
                String string = Command.bytesToString(datas,5,dataLenth);
                LOG.I("资源发送者获取到对方的 信息: "+string);
                String[] sarr = string.split(Command.SEPARATOR);
                targetAddress = new InetSocketAddress(sarr[0],Integer.parseInt(sarr[1]));
                state = 11; //尝试发送握手包
            }
            else if (command == Command.SYN){
                LOG.I("收到对方的 信息握手包");
                //对方的握手包
                state = 12;
                //发送回执
            }
            else if (command == Command.AKC){
                //收到对方的回执信息 - 包含本地资源的全路径
                int len = Command.bytesToInt(datas,1);
                localPath = Command.bytesToString(datas,5,len);
                LOG.I("本地文件 - "+localPath);
                //获取文件流
               if (rafile==null
                        && fileChannel==null && mappedByteBuffer==null){
                   rafile = new RandomAccessFile(localPath,"r");
                   fileChannel = rafile.getChannel();
                   fileSize = fileChannel.size(); //文件大小
                   mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);//内存映射
               }
                //通知对方
                state = 13;
            }else if (command == Command.SAVE){
                //获取下标 传递数据
                position = Command.bytesToLong(datas,1);
                LOG.I("下标 - " + position);
                state = 14;
            }else if (command == Command.CLOSE){
                state = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
