package itface;

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
 *
 *
 */
public class Command {

    public static final int BUFF_LENGTH = 1024;
    public static final String separator = "/";

    //标志位 开始
    public static final byte[] TAGS_START = new byte[]{'*'};

    public static final String TAGS_START_STR = new String(Command.TAGS_START);

    //标志位 结束
    public static final byte[] TAGS_END = new byte[]{'#'};
    public static final String TAGS_END_STR = new String(Command.TAGS_END);

    //响应
    public static final byte[] RESP = new byte[]{'R','E','S','P'};
    //心跳 -> 添加到在线用户中 HRBT*/192.168.55.121:5555/MAC_ADDRESS/#
    public static final byte[] HRBT = new byte[]{'H','R','B','T'};

    //请求连接
    public static final byte[] QYCN = new byte[]{'Q','Y','C','N'};


}
