package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server {
    private Socket socket;
    private ServerSocket server;
    private DataInputStream input;
    private DataOutputStream output;
    static Vector<ClientHandle> client_list=new Vector<>();
    static File save_file;
    int i=1;
    FileWriter fl_w;
    ReadWriteLock rw_lock=new ReentrantReadWriteLock();
    Lock readlock=rw_lock.readLock();
    Lock writelock=rw_lock.writeLock();

    public Server(int port) throws  IOException{
        try {
            ServerSocket server = new ServerSocket(port);

            save_file=new File(Math.random()+" Save_file.txt");
            if (save_file.createNewFile()){
                System.out.println("Have already created a new file to save the messages.....");
            } else {
                System.out.println("Failed to create a file.....");
            }
            while(true){
                socket = server.accept();
                System.out.println("New client request received : " + socket);
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
                System.out.println("Creating a new handler for this client...");
                try {
                    fl_w=new FileWriter(save_file,true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ClientHandle ch=new ClientHandle("Client_number:"+i,socket,input,output,save_file,readlock,writelock,fl_w);
                Thread thread=new Thread(ch);
                thread.start();
                client_list.add(ch);
                i++;

            }

        } catch (IOException i){
            server.close();
            i.printStackTrace();
        }
    }

}
