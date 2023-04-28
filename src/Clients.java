import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.lang.*;

public class Clients extends JFrame implements ActionListener,Runnable {
    private JTextArea taMSG = new JTextArea("聊天记录\n");
    private JTextField tfMSG = new JTextField();
    private Socket s = null;
    private String nickName = null;
    private String detect = null;
    private String nickname =null;
    //UDP广播站
    private DatagramSocket pubsocket = null;

    public String ip = "224.0.0.1";//组播虚拟地址
    public int group_port = 7777;//组播端口
    private MulticastSocket s1 = null;
    private JComboBox<String> comboBox = null;
    private JLabel activeusers = new JLabel("发送对象");
    private JFrame frame ;
    private JPanel panel = new JPanel();
    private String AdviceMsg = null;

    private JButton exitButton = new JButton("下线");



    public Clients(String nickN) {
        this.nickName = nickN;
        frame = new JFrame("客户端" + nickN);
        frame.setSize(450, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = (JPanel)frame.getContentPane();
        panel.setOpaque(false);
        ImageIcon image;
        image = new ImageIcon("mo.jpg");
        JLabel background = new JLabel(image);
        background.setBounds(0,0,450,500);
        frame.getLayeredPane().add(background,new Integer(Integer.MIN_VALUE));


        panel.setLayout(null);
        comboBox = new JComboBox<>();
        comboBox.setBounds(305,40,130,40);
        comboBox.setFont(new Font("楷体", Font.PLAIN, 30));
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

        frame.setVisible(true);

        exitButton.setFont(new Font("楷体", Font.PLAIN, 30));
        exitButton.setBounds(305, 320, 130, 40);
        panel.add(exitButton);
        exitButton.addActionListener(this);



        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // 只处理选中的状态
                if (e.getStateChange() == ItemEvent.SELECTED) {

                    String temp  = comboBox.getSelectedItem().toString();
                    int  temp_item = comboBox.getSelectedIndex();
                    if(temp.equals("全部"))
                    {
                        AdviceMsg = "b:";
                    }
                    else
                    {
                        AdviceMsg = "p:"+temp+"-";

                    }


                }
            }
        });


        try {
            pubsocket = new DatagramSocket(null);
            pubsocket.setReuseAddress(true);
            pubsocket.bind(new InetSocketAddress(8888));
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            s = new Socket("127.0.0.1", 9999);//指定服务器的地址和端口
            JOptionPane.showMessageDialog(this, "连接成功");


            OutputStream os = s.getOutputStream();
            PrintStream ps = new PrintStream(os);
            ps.println(nickName);

            new Thread(this).start();

        } catch (Exception e) {

        }
    }
    public void exit_now()
    {
        System.out.println("exit this");
        frame.setVisible(false);
    }

    public void run() {
        try {
            while (true) {


                TcpPointThread thread1 = new TcpPointThread();
                UdpBroadcastThread thread2 = new UdpBroadcastThread();
                thread1.start();
                thread2.start();
                if (s1 != null) {
                    UdpMulticastThread thread3 = new UdpMulticastThread();
                    thread3.start();
                }


            }

        } catch (Exception e) {
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
                if(str.equals("exit"))
                {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //this.setVisible(true);
                    exit_now();

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!str.isEmpty())
                taMSG.append(str + "\n");
        }
    }

    class UdpMulticastThread extends Thread {//组播

        @Override
        public void run() {

            byte[] arb = new byte[1024];
            try {

                DatagramPacket datagramPacket = new DatagramPacket(arb, arb.length);
                s1.receive(datagramPacket);
                String str3 = new String(arb);
                if (!str3.isEmpty())
                    taMSG.append(str3 + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class UdpBroadcastThread extends Thread {
        @Override
        public void run() {
            //UDP广播通信
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                pubsocket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] data = packet.getData();//获取数据内容
            System.out.println("端口号：" + packet.getPort());
            String str1 = new String(data, packet.getOffset(), packet.getLength());
            if(str1!=null)
            {
                if(str1.startsWith("delete"))
                {
                    String ready_to_exit = str1.split("\\:")[1];
                    int ready_to_exit_item = -1;
                    for(int i =0;i<comboBox.getItemCount();i++)
                    {
                        if(ready_to_exit.equals(comboBox.getItemAt(i)))
                        {
                            ready_to_exit_item = i;
                            break;
                        }
                    }
                    Object obj = comboBox.getItemAt(ready_to_exit_item);
                    comboBox.removeItem(obj);
                    str1 = null;

                }


                if(str1.startsWith("increase"))
                {
                    String increasement = str1.split("\\:")[1];
                    System.out.println("increasement:"+increasement);
                    String str[] = increasement.split(" ");
                    for(int i=0;i<str.length;i++)
                    {
                        System.out.println(str[i]);

                    }
                    comboBox.removeAllItems();
                    comboBox.addItem("全部");
                    for(int i=0;i<str.length;i++)
                    {
                        comboBox.addItem(str[i]);

                    }


                    str1 = null;

                }
                System.out.println(str1);
            }

            if (str1 != null)
                taMSG.append(str1 + "\n");
        }
    }


    public void actionPerformed(ActionEvent e) {
        try {
            if(e.getSource() == exitButton)
            {

                String temp;
                temp = "exit:"+nickName;
                OutputStream os1 = s.getOutputStream();
                PrintStream ps1 = new PrintStream(os1);
                ps1.println(temp);
            }
            OutputStream os = s.getOutputStream();
            PrintStream ps = new PrintStream(os);
            detect = tfMSG.getText();
            String result = AdviceMsg + detect;
            System.out.println(result);
            ps.println(result);//私聊的话输入为p:jim-hello,群聊g:hello,加入群组为join：group
            tfMSG.setText("");//清空输入框
            if (detect.startsWith("j")) {
                InetAddress group = InetAddress.getByName(ip);
                s1 = new MulticastSocket(group_port);
                byte[] arb = new byte[1024];
                s1.joinGroup(new InetSocketAddress(group, 7777), NetworkInterface.getByName(ip));//加入该组
                System.out.println("加群成功");
                taMSG.append("加群成功" + "\n");
            }
            detect = null;




        } catch (Exception e1) {
            System.out.println("有异常");
        }
    }

    public static void main(String args[])
    {
        Clients c1 = new Clients("测试1");
        Clients c2 = new Clients("测试2");
        Clients c3 = new Clients("测试3");
    }

}