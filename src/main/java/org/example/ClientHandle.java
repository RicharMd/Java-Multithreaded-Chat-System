package org.example;

import javax.print.attribute.standard.Severity;
import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.locks.Lock;

public  class ClientHandle implements Runnable{
    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime = new SimpleDateFormat("HH:mm:ss");
    final DataInputStream ipu;
    final DataOutputStream opu;
    final Socket s;
    String client_num;
    boolean isloggedin;
    File file;
    FileWriter fl_w;
    Lock readlock;
    Lock writelock;
    int message_num;
    String User_name;;
    public ClientHandle(String cl_n,Socket s_, DataInputStream ipu_, DataOutputStream opu_,File fl,Lock rl,Lock wl,FileWriter flw){
    ipu=ipu_;
    opu=opu_;
    s=s_;
    file=fl;
    client_num=cl_n;
    isloggedin=true;
    fl_w=flw;
    readlock=rl;
    writelock=wl;
    message_num=0;
    }


    public void run(){

        try {
            try {
                User_name=ipu.readUTF();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            opu.writeUTF("Welcome to Chat Room!!!\nIf you want to leave\ntype 'Exit'! \n\nIf you want to check date \ntype 'date' \n\nIf your want to check time \ntype 'time' \n\nIf you want to talk to people in private \ntype 'Client_number + # + message'\n\nIf you want to talk in group\ntype 'group+ # +message'\n\nIf you want to search the chat log\ntype 'search#+key word'\n\nIf you want to recall the message\ntype 'recall +#+ message'\n\nIf you want to search all the chat log\ntype 'searchAll'\n\nIf you want to review the help page\ntype 'help'\n\nIf you want to review your client number\ntype 'cn'\n\nIf you want to check who is online\ntype 'check online\n\nWish you have a good day!!!");
            opu.flush();
            opu.writeUTF("Your client_num is : "+client_num+"\n"+fordate.format(new Date())+"\n"+fortime.format(new Date()));
            opu.flush();
            greetings();
            String msg=client_num+" login "+fordate.format(new Date())+" "+fortime.format(new Date())+"\n";
            writelock.lock();
            fl_w.write(msg);
            fl_w.flush();
            writelock.unlock();
            String str1,str2="";
            Date date=new Date();
            String[] arr=new String[2];
            while(true){
                //Listen the inputStream from client
                str1="";
                str2=ipu.readUTF();
                arr[1]=User_name;
                //deal with the input//
                String recipient="";
                if(str2.contains("#")) {
                    StringTokenizer st = new StringTokenizer(str2, "#");
                    if(str2.indexOf("#")-1==-1){
                        recipient=null;
                    }  else {
                        recipient = st.nextToken();
                    }
                    if (str2.indexOf("#")+1==str2.length()){
                        str2="";
                    } else {
                        str2 = st.nextToken();
                    }
                    str1="#";
                }
                str1+=str2;

                //Deal with the request from client

                //when client want to exit//
                if(str1.equals("Exit")){
                    System.out.println(arr[1] + " whose adress is "+this.s + " exits...");
                    for(int i=0;i<Server.client_list.size();i++){
                        if((!(Server.client_list.get(i).client_num.equals(this.client_num)))&&(Server.client_list.get(i).isloggedin)) {
                            Server.client_list.get(i).opu.writeUTF(arr[1]  + " exits..."+fortime.format(date));
                            Server.client_list.get(i).opu.flush();
                        }
                    }
                    String msg_=client_num+"exits... "+fortime.format(date)+"\n";
                    writeFile(msg_);
                    isloggedin=false;
                    s.close();
                    fl_w.close();
                    break;
                }

                //handle with other request//
                handleWithRequestFromClient(str1,str2,recipient,arr,date);
            }
            ipu.close();
            opu.close();
        } catch (IOException i) {
            try{
                isloggedin=false;
                System.out.println(client_num + " whose adress is "+this.s + " exits...");
                for(int j=0;j<Server.client_list.size();j++){
                    if((!(Server.client_list.get(j).client_num.equals(this.client_num)))&&(Server.client_list.get(j).isloggedin)) {
                        Server.client_list.get(j).opu.writeUTF(client_num  + " exits..."+fortime.format(new Date()));
                        Server.client_list.get(j).opu.flush();
                    }
                }
                String msg_=client_num+"exits... "+fortime.format(new Date())+"\n";
                writeFile(msg_);
                s.close();
                ipu.close();
                opu.close();
                fl_w.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void handleWithRequestFromClient(String str1,String str2,String recipient,String[] arr,Date date){
        try {
            switch (str1) {
                case "time":
                    str2 = fortime.format(date);
                    opu.writeUTF(str2);
                    opu.flush();
                    break;
                case "date":
                    str2 = fordate.format(date);
                    opu.writeUTF(str2);
                    opu.flush();
                    break;
                case "searchAll":
                    try {
                        readlock.lock();
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                        String str;
                        int i = 0;
                        opu.writeUTF("---------------------------------SEARCHING---------------------------------------");
                        while ((str = bufferedReader.readLine()) != null) {
                            if ((str.contains("GROUP") || str.contains("PRIVATE"))) {
                                opu.writeUTF(str);
                                i++;
                            }
                        }
                        if (i == 0) {
                            opu.writeUTF("There is no chatlog yet");
                        }
                        opu.writeUTF("---------------------------------ALL---------------------------------------------");
                        bufferedReader.close();
                        readlock.unlock();
                    } catch (IOException i){
                        i.printStackTrace();
                    }
                    break;
                case "check online":
                    checkLogin();
                    break;
                case "help":
                    opu.writeUTF("If you want to leave\ntype 'Exit'! \n\nIf you want to check date \ntype 'date' \n\nIf your want to check time \ntype 'time' \n\nIf you want to talk to people in private \ntype 'Client_number + # + message'\n\nIf you want to talk in group\ntype 'group+ # +message'\n\nIf you want to search the chat log\ntype 'search#+key word'\n\nIf you want to recall the message\ntype 'recall +#+ message'\n\nIf you want to search all the chat log\ntype 'searchAll'\n\nIf you want to review the help page\ntype 'help'\n\nIf you want to review your client number\ntype 'cn'\n\nIf you want to check who is online\ntype 'check online'");
                    opu.flush();
                    break;
                case "cn":
                    opu.writeUTF(client_num);
                    break;
                default:
                    if (recipient == null) {
                        opu.writeUTF("this message is sent to 'no recipient' \nPlease check....");
                        opu.flush();

                        //handle with group talk//
                    } else if (recipient.equals("group")) {
                        handleWithGroupTalk(str2,arr,date);

                        //handle with meaningless request//
                    } else if (recipient.equals("")) {
                        opu.writeUTF("There is no this kind of command....\nPlease check....");
                        opu.flush();

                        //handle with search
                    } else if (recipient.equals("search")) {
                        handleWithSearch(str2);

                        //handle with recall//
                    } else if (recipient.equals("recall")) {
                        Vector<String> temp_file_line = new Vector<>();
                        readlock.lock();
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line;
                        int cnt = 0;
                        while ((line = br.readLine()) != null) {
                            if (!(((line.contains("GROUP")||line.contains("PRIVATE"))&&line.contains(client_num))&&(line.substring(line.indexOf(":", line.indexOf(":")+1) + 1,line.length()-10).equals(" "+str2+" ")))) {
                                temp_file_line.add(line);
                            } else {
                                cnt++;
                            }
                        }
                        br.close();
                        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                        for (int i = 0; i < temp_file_line.size(); i++) {
                            bw.write(temp_file_line.get(i));
                            bw.newLine();
                            bw.flush();
                        }
                        bw.close();
                        if (cnt == 0) {
                            opu.writeUTF("There is no the " + str2 + " which you want to do revocation");
                        } else {
                            opu.writeUTF("You have already do the revocation successfully");
                        }
                        readlock.unlock();

                        //handle with private talk
                    } else {
                        handleWithPrivateTalk(str2,recipient,arr,date);
                    }
                    break;
            }
        } catch (IOException i){
            try {
                opu.writeUTF("Something wrong");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void handleWithGroupTalk(String str2,String[] arr,Date date){
        try {
            message_num+=1;
            int t=message_num;
            for (int i = 0; i < Server.client_list.size(); i++) {
                if ((!(Server.client_list.get(i).client_num.equals(this.client_num))) && (Server.client_list.get(i).isloggedin)) {
                    Server.client_list.get(i).opu.writeUTF("[GROUP]" + "|" + client_num + "|" + arr[1] + " : " + str2 + " (" + fortime.format(date) + ")"+"{"+"message "+t+"}");
                    Server.client_list.get(i).opu.flush();
                }
            }
            opu.writeUTF("You have already sent the message to group!" + "\n" + fortime.format(date)+"{"+"message "+t+"}");
            opu.flush();
            String msg_ = "[GROUP]" + "|" + client_num + "|" + arr[1] + " : " + str2 + " (" + fortime.format(date) + ")\n";
            writeFile(msg_);
        } catch (IOException i){
            i.printStackTrace();
        }
    }


    private void handleWithSearch(String str2){
        try {
            readlock.lock();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String str;
            int i = 0;
            opu.writeUTF("---------------------------------SEARCHING---------------------------------------");
            while ((str = bufferedReader.readLine()) != null) {
                if (str.substring(23,str.length()-10).contains(str2) && (str.contains("GROUP") || str.contains("PRIVATE"))) {
                    opu.writeUTF(str);
                    i++;
                }
            }
            if (i == 0) {
                opu.writeUTF("There is no " + "'" + str2 + "'" + " in the chatlog");
            }
            opu.writeUTF("---------------------------------ALL---------------------------------------------");
            bufferedReader.close();
            readlock.unlock();
        } catch (IOException i){
            i.printStackTrace();
        }
    }


    private void handleWithPrivateTalk(String str2,String recipient,String[] arr,Date date){
        try{
            int cnt = 0;

            for (int i = 0; i < Server.client_list.size(); i++) {


                if (((Server.client_list.get(i).client_num.equals(recipient))) && (Server.client_list.get(i).isloggedin)) {
                    Server.client_list.get(i).opu.writeUTF("[PRIVATE]" + "|" + client_num + "|" + arr[1] + " : " + str2 + " (" + fortime.format(date) + ")");
                    Server.client_list.get(i).opu.flush();
                    opu.writeUTF("You have already sent the message to : " + recipient + "\n" + fortime.format(date));
                    opu.flush();
                    String msg_ = "[PRIVATE]" + "|" + client_num + "|" + arr[1] + " : " + str2 + " to " + recipient + " (" + fortime.format(date) + ")\n";
                    writeFile(msg_);
                } else if (((Server.client_list.get(i).client_num.equals(recipient))) && !(Server.client_list.get(i).isloggedin)) {
                    opu.writeUTF("Sorry!This client has left!!");
                    opu.flush();
                } else if (cnt == Server.client_list.size() - 1) {
                    opu.writeUTF("Can't find this client or the format is wrong....");
                    opu.flush();
                }else
                 cnt++;
            }
        } catch (IOException i){
            i.printStackTrace();
        }
    }
   private void writeFile(String msg_){
       writelock.lock();
       try {
           fl_w.write(msg_);
           fl_w.flush();
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
       writelock.unlock();
   }
   private  void greetings(){
        for (ClientHandle ch:Server.client_list){
            if(!(ch.client_num.equals(this.client_num))&&ch.isloggedin){
                try {
                    ch.opu.writeUTF(User_name+"  |"+client_num +" is login at "+fortime.format(new Date()));
                    ch.opu.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
   }
   private  void  checkLogin(){
        for(ClientHandle ch: Server.client_list){
            if(ch.isloggedin){
                try {
                    opu.writeUTF(ch.User_name+"  |"+ch.client_num+" is online...");
                    opu.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
   }
}
