package sjk;
import java.sql.*;
public class Main {
    public static void main(String []args) {
        /*String driverName="com.microsoft.sqlserver.jdbc.SQLServerDriver";
        String dbURL="jdbc:sqlserver://localhost:1433;DatabaseName=Web";
        String userName="sa";
        String userPwd="123456";
        try
        {
            Class.forName(driverName);
            Connection dbConn=DriverManager.getConnection(dbURL,userName,userPwd);
            System.out.println("success!");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.print("fail!");
        }*/
        String str1 = "a b c";
        String str[] = str1.split(" ");
        for(int i=0;i<str.length;i++)
        {
            System.out.println(str[i]);
        }
        System.out.println("herre");
    }

}
