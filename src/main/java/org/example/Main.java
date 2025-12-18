package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args){
        try {
            Server server=new Server(5000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
class CreatClient{
    public static void main(String[] args) {
        Client client=new Client("localhost",5000);
    }
}