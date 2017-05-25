package utils;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by user on 2017/5/23.
 * 定义协议
 *
 * UDP协议
 * 源端口,目的端口,用户数据包长度,检查和,数据  - 8字节
 *  16bit  16bit    16bit  16bit   位置
 *
 *  1 byte = 8 bit
 *  命令位(32)
 *  *(8)开始标志位
 *   数据 (200字节*8 = 1600bit)
 *  #(8)结束标志位
 *
 *
 *
 * byte是一个字节保存的，有8个位，即8个0、1
 *8位的第一个位是符号位，
 也就是说0000 0001代表的是数字1		1000 0000代表的就是-1
 所以正数最大位0111 1111，也就是数字127,  负数最大为1111 1111，也就是数字-128

 * byte[] data = {前四个协议,5-8数据长度,数据}
 * 1字节(1-255) 4字节(高位在前,低位在后)
 * 协议        长度  数据
 *
 */
public class Command {
    public static final String HOME_DIR = "C:/Users/user";
    public static final int BUFF_LENGTH = 256;
    public static final int DATA_BUFF_LENGTH = 2200;
    public static final String SEPARATOR = "@";
    //客户端->服务器 心跳 -> 添加到在线用户中  例如:数据为  HA-89-99-00-09-56 -> byte[] , 长度:length=100 -> 0 0 0 100 > {1,0,0,0,100,数据字节....MAC} ->恢复心跳{1}
    public static final byte HRBT = 1;
    public static final byte HRBT_RESP=3;
    public static final byte HRBT_DATA = 2;
    //某客户端请求资源  {5,0,0,0,50,"客户端MAC地址@请求的文件名@状态码(请求状态1)"} -> 通知所有客户端{7,"长度,""},设置状态->
    // 随后这个客户端会开启一个端口建立和服务器数据端口的连接(打洞准备) ,服务器会收到  {7,长度,mac地址},如果在队列中找到同样的mac得客户端,并且状态码为1(请求状态), 则记录他的数据端口,并且回复一个心跳{2,长度,"查询中"}
    public static final byte CLIENT_SERVER_QUESY_SOURCE = 5;
    public static final byte NOTIFY_ALL_CLIENT_SOURCE = 6;
    public static final byte CLIENT_CONNECT_DATA_CHANNEL = 7;

    //发送到所有客户端,查询是否存在这个资源,如果存在,请返回 {SOURCE_NOTITY10,数据长度,"请求资源的MAC@存在资源的mac@请求的资源名本地完整路径@状态码"} 进行绑定
    public static final byte SOURCE_NOTITY = 10;
    public static final byte ABOUT_SOUCE_PATH = 12; // 客户端发送资源在对方本地的路径,给索取资源客户端
    //某资源被正确查询到 - {15,长度,"需要资源的客户端@资源存在的客户端mac@状态码(请求状态1)"} -> 随后将会和服务器的数据端口建立连接,并且收到{6,长度,mac}, 记录它的数据端口
    //同时 互相通知他们两个客户端对方的数据端口 ->  例如: 发送A ; {SOUCE_QUERY_SUCCESS,长度,"B_IP,B_port"} 发送B: {SOUCE_QUERY_SUCCESS,A_ip,A_port}; 然后 a-b 将建立连接,服务器设置两客户端状态 :"传输中" (传输完毕后,将通知服务器自己的状态)
    public static final byte SOUCE_QUERY_SUCCESS  = 15;
    public static final byte NOTIFY_DATA_PORT = 20;

    /**
     * 客户端 - 客户端 ,握手
     * 需求数据客户端---->数据存在客户端 SYN
     * 收到 akc后 ,判断后续数据
     *  如果时 DATA,则保存数据  data,文件开始位置4字节,数据长度4字节,~~~ ~数据~~~****(数据长度验证符)
     *  保存一段数据成功后 发送SAVE , 发送下一段数据
     */
    public static final byte SYN = 50;
    public static final byte AKC = 51;
    public static final byte FLG = 53;//返回资源总大小
    public static final byte DATA = 100;
    public static final byte SAVE = 101;//指定资源客户端的文件的起始点{save,起始点} long 型
    public static final byte CLOSE = 102;//
    public static final String DATA_SEPARATOR = "****";


    public static void createDatas(byte[] bytes,ByteBuffer byteBuffer){
        byteBuffer.clear();
        byteBuffer.put(bytes);
        byteBuffer.flip();
    }

    public static byte[] createDatas(byte proc,int size){
            byte[] intArr = Command.intToBytes(size);
            byte[] bytes = new byte[intArr.length+1];
            bytes[0] = proc;
            System.arraycopy(intArr, 0, bytes, 1, intArr.length);
            return bytes;
    }
    public static byte[] createDatas(byte proc,long size){
        byte[] longArr = Command.longToBytes(size);
        byte[] bytes = new byte[longArr.length+1];
        bytes[0] = proc;
        System.arraycopy(longArr, 0, bytes, 1, longArr.length);
        return bytes;
    }
    public static byte[] createDatas(byte proc,String message) throws UnsupportedEncodingException {
        byte[] bytes = message.getBytes("GBK");
        byte[] length = Command.intToBytes(bytes.length);
        byte[] data = new byte[1+length.length+bytes.length];
        int pos = 0;
        data[pos] = proc;
        pos++;
        System.arraycopy(length, 0, data, pos, length.length); //从length数组的第一个元素起复制四个元素到 data ,从pos位置开始算
        pos+=length.length;
        System.arraycopy(bytes, 0, data, pos, bytes.length);
        return data;
    }
    //解析数据
    public static ArrayList<Object> parseDatas(byte[] bytes){

        try {
            ArrayList<Object> list = new ArrayList<>();
            int position = 0;
            //协议位
            byte protocol = bytes[position];
            position++;
            list.add(protocol);
            //长度位
            int length = bytesToInt(bytes, position); // 1 2 3 4
            position += 4;
            list.add(length);
            String str = bytesToString(bytes,position,length);
            if (str.equalsIgnoreCase("error")) return null;
            //数据实体
            list.add(str); //5
            return list;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String bytesToString(byte[] bytes,int position,int length){
        try {
            String str = new String(bytes, position, length, "GBK");
            return str;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "error";
    }

    //发送消息
    public static void sendMessage(DatagramChannel channel, InetSocketAddress targetAddress,  byte proc, String message,ByteBuffer buffer) {
        try {

            if (buffer == null){
                if (message==null || message.length()==0 || proc == 0) return;
                byte[] data = Command.createDatas(proc,message);
                buffer = ByteBuffer.wrap(data);
                buffer.clear();
                buffer.put(data);
                buffer.flip();
            }
            channel.send(buffer,targetAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface SelectAction{
        void selectAction(SelectionKey key);
    }

    public static void loopSelect(Selector selector,SelectAction action){
        try {
            if (selector==null && action==null) return;

            while (selector.select() > 0){
                Iterator iterator = selector.selectedKeys().iterator();
                SelectionKey key = null;
                while (iterator.hasNext()) {
                    key = (SelectionKey) iterator.next();
                    iterator.remove();
                    try {
                        if (key.isReadable()){
                            action.selectAction(key);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * 将int数值转换为占四个字节的byte数组，高位在前，低位在后
     */
    public static byte[] intToBytes(int value)
    {
        byte[] src = new byte[4];
        src[0] = (byte) ((value>>24) & 0xFF);
        src[1] = (byte) ((value>>16)& 0xFF);
        src[2] = (byte) ((value>>8)&0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }
    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ( ((src[offset] & 0xFF)<<24)
                |((src[offset+1] & 0xFF)<<16)
                |((src[offset+2] & 0xFF)<<8)
                |(src[offset+3] & 0xFF));
        return value;
    }
    //byte 数组与 long 的相互转换
    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes,int start) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes, start,8);
        buffer.flip();//need flip
        return buffer.getLong();
    }

}
