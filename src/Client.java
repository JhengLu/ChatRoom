import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.lang.*;

public class Client extends JFrame implements ActionListener,Runnable {
    private JTextArea taMSG = new JTextArea("以下是聊天记录\n(私聊为p:jim-hi,广播b:hi,组播g:hi,加群:join:group)\n");
    private JTextField tfMSG = new JTextField();
    private Socket s = null;
    private String nickName = null;
    private String detect = null;
    //UDP广播站
    private DatagramSocket pubsocket = null;

    public String ip = "224.0.0.1";//组播虚拟地址
    public int group_port = 7777;//组播端口
    private MulticastSocket s1 = null;


    public Client(){
        this.setTitle("客户端");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(taMSG,BorderLayout.CENTER);
        tfMSG.setBackground(Color.WHITE);
        tfMSG.setPreferredSize(new Dimension(450,70));
        taMSG.setFont(new Font("宋体",Font.BOLD,15));
        tfMSG.setFont(new Font("宋体",Font.BOLD,15));;
        this.add(tfMSG,BorderLayout.SOUTH);
        tfMSG.addActionListener(this);
        this.setSize(450,500);
        this.setVisible(true);
        try {
            pubsocket = new DatagramSocket(null);
            pubsocket.setReuseAddress(true);
            pubsocket.bind(new InetSocketAddress(8888));
        } catch (SocketException e) {
            e.printStackTrace();
        }
        //nickName = JOptionPane.showInputDialog("输入昵称");

        try{
            s = new Socket("127.0.0.1",9999);//指定服务器的地址和端口
            JOptionPane.showMessageDialog(this,"连接成功");

            this.setTitle("客户端"+nickName);
            OutputStream os = s.getOutputStream();
            PrintStream ps = new PrintStream(os);
            ps.println(nickName);

            new Thread(this).start();

        }
        catch (Exception e)
        {

        }
    }
    public void run()
    {
        try{
            while (true){


                TcpPointThread thread1 = new TcpPointThread();
                UdpBroadcastThread thread2 = new UdpBroadcastThread();
                thread1.start();
                thread2.start();
                if(s1!=null)
                {
                    UdpMulticastThread thread3 = new UdpMulticastThread();
                    thread3.start();
                }


            }

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    class TcpPointThread extends Thread {
        @Override
        public void run() {
            InputStream is = null;
            try {
                is = s.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String str = null;//读
            try {
                str = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!str.isEmpty())
                taMSG.append(str + "\n");
        }
    }
    class UdpMulticastThread extends Thread{//组播

        @Override
        public void run() {

            byte[] arb = new byte[1024];
            try {

                DatagramPacket datagramPacket =new DatagramPacket(arb,arb.length);
                s1.receive(datagramPacket);
                String str3 = new String(arb);
                if(!str3.isEmpty())
                    taMSG.append(str3+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class UdpBroadcastThread extends Thread
    {
        @Override
        public void run() {
            //UDP广播通信
            //System.out.println("线程启动");
            byte [] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                pubsocket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //System.out.println("here");
            byte[] data = packet.getData();//获取数据内容
            System.out.println("端口号："+packet.getPort());
            String str1 = new String(data,packet.getOffset(),packet.getLength());
            System.out.println(str1);
            if(!str1.isEmpty())
                taMSG.append(str1+"\n");
        }
    }


    public void actionPerformed(ActionEvent e)
    {
        try{
            OutputStream os = s.getOutputStream();
            PrintStream ps = new PrintStream(os);
            detect = tfMSG.getText();
            ps.println(detect);//私聊的话输入为p:jim-hello,群聊g:hello,加入群组为join：group
            tfMSG.setText("");//清空输入框
            if(detect.startsWith("j"))
            {
                    InetAddress	group = InetAddress.getByName(ip);
                     s1 = new MulticastSocket(group_port);
                    byte[] arb = new byte[1024];
                    s1.joinGroup(new InetSocketAddress(group,7777),NetworkInterface.getByName(ip));//加入该组
                    System.out.println("加群成功");
                    taMSG.append("加群成功"+"\n");
            }
            detect = null;


        }
        catch (Exception e1)
        {
            System.out.println("有异常");
        }
    }
    /*public static void main(String []args) throws Exception{
        int client_num = 0;
        String temp;
        temp = JOptionPane.showInputDialog("输入连接客户的数量");
        client_num = Integer.parseInt(temp);
        for(int i=1;i<=client_num;i++)
        {
            Client client = new Client();
        }

    }*/

}
