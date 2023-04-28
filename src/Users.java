import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.lang.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

class Login extends JFrame implements ActionListener,Runnable
{
    private JFrame frame = new JFrame("注册窗 ");
    private JLabel userLabel = new JLabel("用户名:");
    private  JTextField userText = new JTextField(20);
    private JLabel passwordLabel = new JLabel("  密码:");//来输入密码
    private  JTextField passwordText = new JTextField(20);
    private JButton registButton = new JButton("注册");
    private JPanel panel = new JPanel();
    public String user =null;
    public  String password = null;
    private JButton backButton = new JButton("返回");
    private  Users users =null;
    private  ResultSet rs =null;
    private Connection dbConn = null;
    private String userid_new = null;

    public Login(/*Users us*/)
    {
        //users = us;
        frame.setSize(400, 300);
        frame.setLocation(500,100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        panel = (JPanel)frame.getContentPane();
        panel.setOpaque(false);
        ImageIcon image;
        image = new ImageIcon("new.jpg");
        JLabel background = new JLabel(image);
        background.setBounds(0,0,400,300);
        frame.getLayeredPane().add(background,new Integer(Integer.MIN_VALUE));


        //frame.add(panel);
        panel.setLayout(null);//一定要加，不然就修改不了布局
        userLabel.setBounds(40,20,120,30);
        Font font2 = new Font("楷体",Font.PLAIN, 30);
        Font font1 = new Font("Times New Roman",Font.PLAIN, 30);
        userLabel.setFont(font2);
        panel.add(userLabel);
        userText.setBounds(180,20,165,35);
        userText.setFont(font2);
        panel.add(userText);
        passwordLabel.setBounds(40,100,120,30);
        passwordLabel.setFont(font2);
        panel.add(passwordLabel);
        passwordText.setBounds(180,100,165,35);
        panel.add(passwordText);
        registButton.setBounds(60, 180, 100, 40);
        panel.add(registButton);
        backButton.setBounds(230, 180, 100, 40);
        panel.add(backButton);
        frame.setVisible(true);

        String driverName="com.microsoft.sqlserver.jdbc.SQLServerDriver";
        String dbURL="jdbc:sqlserver://localhost:1433;DatabaseName=Web";
        String userName="sa";
        String userPwd="123456";

        try {
            Class.forName(driverName);
            dbConn= DriverManager.getConnection(dbURL,userName,userPwd);
            System.out.println("success!");
            Statement stmt = dbConn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        registButton.addActionListener(this);
        backButton.addActionListener(this);

    }


    @Override
    public void run() {


    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == registButton)
        {
            user = userText.getText();
            password = passwordText.getText();
            System.out.println(user);
            System.out.println(password);

            try
            {
                Statement stmt = dbConn.createStatement();
                String sql_select ="select max(userid) from usersnew";
                rs = stmt.executeQuery(sql_select);
                rs.next();
                String id = rs.getString(1);
                System.out.println(id);
                int idnew = Integer.parseInt(id.trim());
                idnew += 1;
                userid_new =  String.format("%06d",idnew);
                String sql = "insert into usersnew values ("+'\''+userid_new+'\''+','+'\''+user+'\''+','+'\''+password+'\''+')';
                System.out.println(sql);
                //注意上面这条语句需要加上单引号
                stmt.execute(sql);//插入使用execute,
                userText.setText("");
                passwordText.setText("");
                JOptionPane.showMessageDialog(this,"注册成功,你的账户是："+userid_new);



            }
            catch(Exception e1)
            {
                e1.printStackTrace();
                System.out.print("fail!");
            }


        }
        if(e.getSource() == backButton)
        {
            frame.setVisible(false);
            users = new Users();
        }

    }
}

public class Users extends JFrame implements ActionListener,Runnable
{

    private JFrame frame = new JFrame("登录");
    private JPanel panel = new JPanel();
    private JLabel userLabel = new JLabel("  账号:");
    private  JTextField userText = new JTextField(20);
    private JLabel passwordLabel = new JLabel("  密码:");//来输入密码
    private JPasswordField passwordText = new JPasswordField(20);
    private JButton loginButton = new JButton("登录");
    private JButton registButton = new JButton("注册");
    private Clients clients = null;
    ResultSet rs =null;
    public Users()
    {
        // Setting the width and height of frame
        frame.setSize(500, 400);
        frame.setLocation(500,100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = (JPanel)frame.getContentPane();
        panel.setOpaque(false);
        ImageIcon image;
        image = new ImageIcon("server.jpg");
        JLabel background = new JLabel(image);
        background.setBounds(0,0,500,400);
        frame.getLayeredPane().add(background,new Integer(Integer.MIN_VALUE));

        panel.setLayout(null);
        Font font = new Font("楷体", Font.PLAIN, 30);
        Font font2 = new Font("Times New Roman",Font.PLAIN, 30);
        userLabel.setFont(font);
        userLabel.setBounds(90,60,200,40);
        panel.add(userLabel);
        userText.setBounds(200,65,165,30);
        userText.setFont(font);
        panel.add(userText);
        passwordLabel.setBounds(90,120,200,40);
        passwordLabel.setFont(font);
        panel.add(passwordLabel);
        passwordText.setBounds(200,125,165,30);
        passwordText.setFont(font2);
        panel.add(passwordText);
        loginButton.setBounds(80, 200, 100, 40);
        loginButton.setFont(font);
        panel.add(loginButton);
        registButton.setBounds(300, 200, 100, 40);
        registButton.setFont(font);
        panel.add(registButton);

        loginButton.addActionListener(this);
        registButton.addActionListener(this);
        // 设置界面可见
        frame.setVisible(true);
    }
    @Override
    public void run() {


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() ==registButton)
        {
            frame.setVisible(false);
            Login lg = new Login();
        }
        if(e.getSource() == loginButton)
        {
            String driverName="com.microsoft.sqlserver.jdbc.SQLServerDriver";
            String dbURL="jdbc:sqlserver://localhost:1433;DatabaseName=Web";
            String userName="sa";
            String userPwd="123456";
            String use = null;
            String password = null;
            try
            {
                use = userText.getText();
                password = passwordText.getText();
                System.out.println(use);

                Class.forName(driverName);
                Connection dbConn=DriverManager.getConnection(dbURL,userName,userPwd);
                System.out.println("success!");
                Statement stmt = dbConn.createStatement();

                String sql = " select * from usersnew where userid = "+'\''+use+'\'';
                System.out.println(sql);

                //注意上面这条语句需要加上单引号
                rs = stmt.executeQuery(sql);//插入使用execute,


                if(!rs.next() )
                    JOptionPane.showMessageDialog(this,"没有该用户，需要注册");
                else
                {
                    String result_password = rs.getString(3);
                    String login_name =rs.getString(2);
                    System.out.println(result_password+"here");
                    System.out.println(password+"here");

                    if(result_password!=null&&!password .equals(result_password))
                        JOptionPane.showMessageDialog(this,"密码错误");
                    if(result_password!=null&&password .equals(result_password) )
                    {
                        JOptionPane.showMessageDialog(this,"登录成功");
                        clients = new Clients(login_name);
                        this.frame.setVisible(false);
                    }

                }

                userText.setText("");
                passwordText.setText("");

            }
            catch(Exception e2)
            {
                e2.printStackTrace();
                System.out.print("fail!");
            }
        }


    }
    public static void main(String args[])
    {
        Users user1 =new Users();
        Users user2 =new Users();
        Users user3 =new Users();
       // Login login = new Login();
    }


}







