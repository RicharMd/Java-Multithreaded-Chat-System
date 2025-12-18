package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientListen implements Runnable{
    final DataInputStream ipu;

    final Socket s;
    public ClientListen(Socket s_, DataInputStream ipu_){
        s=s_;
        ipu=ipu_;

    }
    @Override
    public void run() {
        while (true) {
            try {
                String str2 = ipu.readUTF();
                System.out.println(str2);
            } catch (IOException i){
                System.out.println(i);
                break;
            }

        }
    }
}
