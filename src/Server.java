import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.JFrame;
import java.lang.*;
import java.util.HashMap;
import java.util.Map;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class Server extends JFrame implements Runnable {
    private Socket s = null;
    private ServerSocket ss = null;
    private ArrayList clients = new ArrayList();//保存客户端的线程
    private ArrayList group = new ArrayList();//群组成员
    private BufferedReader br1 = null;
    private Map<String,Socket> map1 = new HashMap<String,Socket>();//保存昵称和socket
    private Map<Socket,String> map2 = new HashMap<Socket,String>();//保存昵称和socket
    private MulticastSocket s1 = null;

    public String ip = "224.0.0.1";//组播虚拟地址
    public int group_port = 7777;//组播端口

    public Server() throws Exception {
        this.setTitle("服务器端");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBackground(Color.WHITE);
        this.setSize(200, 100);
        this.setVisible(true);
        ss = new ServerSocket(9999);//服务器端开辟端口,接受连接
        new Thread(this).start();//接受服务器连接的死循环开始运行
    }

    public void run() {
        try {
            while (true) {
                s = ss.accept();
                ChatThread ct = new ChatThread(s);
                br1 = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String nickname = br1.readLine();//获得socket传过来的昵称
                map1.put(nickname,s);
                map2.put(s,nickname);
                clients.add(ct);
                ct.start();
            }
        } catch (Exception e) {
            System.out.println("程序异常");
        }
    }

    class ChatThread extends Thread {
        private Socket s = null;
        private BufferedReader br = null;
        public PrintStream ps = null;

        public ChatThread(Socket s) throws Exception {
            this.s = s;
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ps = new PrintStream(s.getOutputStream());

        }

        public void run() {
            try {
                while (true) {//私聊的话输入为p:jim-hello,群聊g:hello
                    String str = br.readLine();//读取socket传过来的信息

                    String msg = str.split("\\:")[1];//获取核心内容
                    String hostname = map2.get(s);


                    if(str.startsWith("b"))
                    {
                        broadChat(msg,hostname);//将str转发给客户端
                    }

                    if(str.startsWith("p")){//私聊：P:username-hhh
                        String useName = msg.split("\\-")[0];//要发送的对象名字
                        //取得消息内容
                        String message = msg.split("\\-")[1];//要发送的内容
                        privateChat(useName, message,hostname);

                    }
                    if(str.startsWith("j")){//加入群组
                        System.out.println("here");
                        InetAddress	group = InetAddress.getByName(ip);
                         s1 = new MulticastSocket(group_port);
                        byte[] arb = new byte[1024];
                        s1.joinGroup(new InetSocketAddress(group,7777),NetworkInterface.getByName(ip));//加入该组
                        System.out.println("加入成功");

                    }
                    if(str.startsWith("g")){
                        groupChat(msg,hostname);
                    }

                }
            } catch (Exception e) {
            }
        }
    }
    public void groupChat(String msg,String hostname)
    {
        InetAddress inetAddress = null; //指定组播地址
        try {
            inetAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String temp = hostname+":"+msg;
        byte[] message = temp.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(message, message.length, inetAddress, 7777); //发送数据包囊
        MulticastSocket multicastSocket = null;//创建组播socket
        try {
            multicastSocket = new MulticastSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            multicastSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void privateChat(String username,String message,String hostname)//私聊给指定的用户
    {
        Socket s = map1.get(username);
        Socket s2 = map1.get(hostname);
        try {
            ChatThread ct = new ChatThread(s);
            ct.ps.println(hostname+":"+message);
            ChatThread ct2 = new ChatThread(s2);
            ct2.ps.println("To"+username+":"+message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void broadChat(String msg,String hostname)//用udp把信息发给所有的客户端
    {

        String ip = "255.255.255.255";//广播地址
        int pubport  =8888;//指定的广播端口
        String temp = hostname+":"+msg;
        byte [] buf = (temp).getBytes();
        System.out.println(temp);
        DatagramSocket ds = null;
        try {
            InetAddress address = InetAddress.getByName(ip);
            ds  = new DatagramSocket();//仓库
            DatagramPacket dp = new DatagramPacket(buf, buf.length,address,pubport);//数据
            ds.send(dp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
                ds.close();
                }

    }

    public static void main(String[] args) throws Exception{
        Server server = new Server();
    }
}