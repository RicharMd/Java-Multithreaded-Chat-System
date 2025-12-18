package org.example;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private  Socket socket;
    private  BufferedReader reader;

    private  DataInputStream input;
    private DataOutputStream output;
//在这里开一个固定线程数目的线程池


    public Client(String address,int port){
        try{
            socket=new Socket(address,port);
            System.out.println("Connected to chat_room");
            input =new DataInputStream(socket.getInputStream());
            output =new DataOutputStream(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(System.in));



        String str1="";
        Thread thread = new Thread(new ClientListen(socket,input));
        System.out.println("Please type down your User name:");
        String username=reader.readLine();
        output.writeUTF(username);
        output.flush();
        thread.start();

        while (true) {
//                在这里把任务提交到线程中分别是read任务和write任务
                str1 =reader.readLine();
                output.writeUTF(str1);
                output.flush();
                if(str1.equals("Exit") || socket==null){
                    System.out.println("You are leaving~~ \nConnection is closing.....");
                    socket.close();
                    System.out.println("Connection is closed!!");
                    break;
                }

        }
                input.close();
                output.close();
                reader.close();
            } catch (IOException i) {
                System.out.println(i);
            }

    }
}
