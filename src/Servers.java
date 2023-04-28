import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import java.lang.*;
import java.util.HashMap;
import java.util.Map;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class Servers extends JFrame implements  ActionListener,Runnable {
    private Socket s = null;
    private ServerSocket ss = null;
    private ArrayList clients = new ArrayList();//保存客户端的线程
    private ArrayList group = new ArrayList();//群组成员
    private BufferedReader br1 = null;
    private Map<String,Socket> map1 = new HashMap<String,Socket>();//保存昵称和socket
    private Map<Socket,String> map2 = new HashMap<Socket,String>();//保存昵称和socket
    private MulticastSocket s1 = null;
    private JComboBox<String> comboBox = null;
    private JFrame frame = new JFrame("服务器");
    private JPanel panel ;
    private JLabel activeusers = new JLabel("在线用户");
    private JTextField tfMSG = new JTextField();
    private JTextArea taMSG = new JTextArea("广播消息\n");
    private JButton exitButton = new JButton("强制下线");
    public String ip = "224.0.0.1";//组播虚拟地址
    public int group_port = 7777;//组播端口
    private String ready_to_exit = null;
    private int ready_to_exit_item;
    private Socket exit_socket;
    private String totalname="";



    public Servers() throws Exception {

        frame.setSize(450, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = (JPanel)frame.getContentPane();
        panel.setOpaque(false);
        ImageIcon image;
        image = new ImageIcon("2.jpg");
        JLabel background = new JLabel(image);
        background.setBounds(0,0,450,500);
        frame.getLayeredPane().add(background,new Integer(Integer.MIN_VALUE));

        panel.setLayout(null);
        comboBox = new JComboBox<>();
        comboBox.setBounds(305,40,130,40);
        comboBox.setFont(new Font("楷体",Font.PLAIN, 30));

        activeusers.setBounds(305,0,200,40);
        activeusers.setFont(new Font("楷体", Font.PLAIN, 30));
        panel.add(comboBox);
        panel.add(activeusers);

        tfMSG.setBackground(Color.WHITE);
        tfMSG.setPreferredSize(new Dimension(450, 70));
        taMSG.setFont(new Font("宋体", Font.BOLD, 15));
        tfMSG.setFont(new Font("宋体", Font.BOLD, 15));
        taMSG.setBounds(0,0,305,360);
        tfMSG.setBounds(0,360,450,140);

        panel.add(taMSG);
        panel.add(tfMSG);
        tfMSG.addActionListener(this);

        exitButton.setFont(new Font("楷体", Font.PLAIN, 20));
        exitButton.setBounds(305, 200, 130, 40);
        panel.add(exitButton);
        exitButton.addActionListener(this);

        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                                         // 只处理选中的状态
                if (e.getStateChange() == ItemEvent.SELECTED) {

                 ready_to_exit = comboBox.getSelectedItem().toString();
                ready_to_exit_item = comboBox.getSelectedIndex();
                System.out.println(ready_to_exit);

                                         }
                                     }
                                 });

        frame.setVisible(true);
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
                totalname +=nickname+" ";
                map1.put(nickname,s);
                map2.put(s,nickname);
                broadChat("increase:"+totalname,"null");
                broadChat(nickname+"上线啦！","null");
                taMSG.append(nickname+"上线啦！\n");
                comboBox.addItem(nickname);
                clients.add(ct);
                ct.start();
            }
        } catch (Exception e) {
            System.out.println("程序异常");
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == exitButton)
        {
            System.out.println("here"+ready_to_exit);
            Socket s = map1.get(ready_to_exit);
            String s1 = "已经将"+ready_to_exit+"强制下线";
            taMSG.append(s1+"\n");
            broadChat(ready_to_exit+"下线了","null");
            broadChat("delete:"+ready_to_exit,"null");
            ChatThread ct = null;
            try {
                ct = new ChatThread(s);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            ct.ps.println("你已经被强制下线");
            ct.ps.println("exit");
            int ready_to_exit_item = -1;
            for(int i =0;i<comboBox.getItemCount();i++)
            {
                if(ready_to_exit.equals(comboBox.getItemAt(i)))
                {
                    ready_to_exit_item = i;
                    break;
                }
            }
            Object obj1 = comboBox.getItemAt(ready_to_exit_item);
            comboBox.removeItem(obj1);

            System.out.println("remove");

        }
        else
        {
            OutputStream os = null;
            try {
                os = s.getOutputStream();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            String detect;
            detect = tfMSG.getText();
            broadChat(detect,"服务器");
            tfMSG.setText("");
            taMSG.append(detect+"\n");

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

                    if(str.startsWith("exit"))
                    {
                        String ready_to_exit1 =  str.split("\\:")[1];
                        int ready_to_exit_item1 = -1;
                        Socket s = map1.get(ready_to_exit1);
                        for(int i =0;i<comboBox.getItemCount();i++)
                        {
                            if(ready_to_exit1.equals(comboBox.getItemAt(i)))
                            {
                                ready_to_exit_item1 = i;
                                break;
                            }
                        }
                        Object obj = comboBox.getItemAt(ready_to_exit_item1);
                        broadChat(ready_to_exit1+"下线了","null");
                        broadChat("delete:"+ready_to_exit1,"null");
                        taMSG.append(ready_to_exit1+"下线了\n");
                        ChatThread ct = null;
                        try {
                            ct = new ChatThread(s);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        ct.ps.println("exit");
                        comboBox.removeItem(obj);


                    }


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
            ct.ps.println("私聊 "+hostname+":"+message);
            ChatThread ct2 = new ChatThread(s2);
            ct2.ps.println("To "+username+":"+message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void broadChat(String msg,String hostname)//用udp把信息发给所有的客户端
    {

        String ip = "255.255.255.255";//广播地址
        int pubport  =8888;//指定的广播端口
        String temp;
        if(hostname.equals("null"))
        {
             temp = msg;
        }
        else
        {
             temp = hostname+":"+msg;
        }

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
        Servers servers = new Servers();
    }
}