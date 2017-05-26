package client.threads.imps;

import client.imps.ClientImps;
import utils.Command;
import utils.LOG;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.MappedByteBuffer;
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
    protected void receiveMessage() {
        try {
            getData();
            byte[] bytes = buffer.array();
            byte command = bytes[0];
            if (command == Command.HRBT_DATA){
                //收到服务心跳
                state = 10;
                LOG.I("收到服务器数据端口的心跳. 开始请求建立连接.");
            }else if (command == Command.SOUCE_QUERY_SUCCESS){
                // {ip@port}
                int dataLenth = Command.bytesToInt(bytes,1);
                String string = Command.bytesToString(bytes,5,dataLenth);
                LOG.I("资源发送者,收到对方的地址信息:"+string);
                String[] sarr = string.split(Command.SEPARATOR);
                targetAddress = new InetSocketAddress(sarr[0],Integer.parseInt(sarr[1]));
                if (state == 20){
                    //收到过对方的握手包
                    state = 12;
                }else{
                    state = 11; //尝试发送握手包
                }
            }
            else if (command == Command.SYN){
                LOG.I("收到握手包,当前是否存在 资源接受方信息 -> " +targetAddress);
                //对方的握手包
                if (targetAddress==null){
                    state = 20;
                    LOG.I("等待服务器告知对方地址.");
                    //发送回执
                }else{
                    state = 12;
                    LOG.I("发送回执");
                }
            }
            else if (command == Command.AKC){
                //收到对方的回执信息 - 包含本地资源的全路径
                int len = Command.bytesToInt(bytes,1);
                localPath = Command.bytesToString(bytes,5,len);
                LOG.I("收到对方的回执信息 : "+localPath);
                //获取文件流
               if (rafile==null && fileChannel==null){
                   File file = new File(localPath);
                   fileSize = file.length(); //文件大小
                   rafile = new RandomAccessFile(file,"r");
                   fileChannel = rafile.getChannel();
                   LOG.I("已找到资源,准备传输 : " + localPath);
                   //mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);//内存映射
                   //通知对方文件大小
                   state = 13;
               }
            }else if (command == Command.SAVE){
                //获取下标 传递数据
                position = Command.bytesToLong(bytes,1);
                LOG.I("收到指定文件位置 :" + position);
                //读取一段资源发送到客户端 - 等客户端下一次指示
                sendDatas();
                state = 14;
            }else if (command == Command.CLOSE){
                state = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //发送数据
    private void sendDatas() throws IOException {
        //发送数据
        buffer.clear();
        int len = 0;
        //从下标开始获取数据
        long csun = fileSize - position;
        LOG.I("剩余文件大小:"+csun);
        if (csun >= Command.DATA_LENGTH){
            len = Command.DATA_LENGTH ;
        }else if (csun>0){
            len = (int) csun;
        }
        buffer.put( Command.DATA);
        byte[] lenby = Command.intToBytes(len);
        for (int i= 0;i<lenby.length;i++){
            buffer.put(lenby[i]);
        }

        try {
            fileChannel.position(position);//移动到下标
            mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, position, len);
            buffer.put(mappedByteBuffer);
            byte [] strArr = Command.DATA_SEPARATOR.getBytes();//分隔符
            for (int i = 0;i<strArr.length;i++){
                buffer.put(strArr[i]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        buffer.flip();
        LOG.I("传输数据大小:"+buffer.limit());
        buffer.rewind();
        channel.send(buffer,targetAddress);
    }

    @Override
    public void deleteOnMap() {
        client.threadMap.remove("respond");
    }
}
