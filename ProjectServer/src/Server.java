import data.jdbc.DBConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Server{


    public static void main(String[] args) {
        try {



            DBConfig.loadDriver();
            Connection connection = DBConfig.connectToDatabase("localhost:3306", "");
            Statement statement = DBConfig.createStatement(connection);
            //Generating new database
            DBConfig.createDatabase(statement);



            ServerSocket serverSocket = new ServerSocket(9500);



            ExecutorService executor = Executors.newFixedThreadPool(2);
            Socket clientSocket;


        List<Future<String>> futureList = new ArrayList<>();


            while (true) {

                clientSocket = serverSocket.accept();
                futureList.add(executor.submit(new ServerThread(clientSocket, connection)));

            }




        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}