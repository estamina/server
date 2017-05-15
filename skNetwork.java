package server;

import chat.skChat;
import chat.skCode;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;




/**
 * client thread created with each connected user
 */
class skNetwork extends Thread {
    private final skServer skServer;
    
    
    private Socket clientSocket;
    String user;
    String nick;
    
    private BufferedReader in;
    //OutputStreamWriter out;
    private BufferedWriter out;
    
    skNetwork(skServer skServer, Socket clientSocket, ArrayList chats, LinkedList clientsList){
        this.skServer = skServer;
        this.clientSocket=clientSocket;
        this.chats=chats;
        this.clients=clientsList;
    }
    
    
    synchronized void sendMessage(String message){
        try {
            out.write(message+"\n");
            //flushing a buffer to send a message
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Chyba1 "+ex.getMessage());
        }
    }
    
    
    public void run() {
        try {
            
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            
            String line;
            
            //endless loop
            while ((line = in.readLine()) != null) {
                //fishing for the intro byte indicating a valid message start
                while (line.compareTo(skCode.MSGINTRO) != 0){
                    line = in.readLine();
                    System.out.println("! "+line);
                }
                decode();
            }
            
            in.close();
            out.close();
            
            clientSocket.close();
        }  catch (IOException ex) {
            //ex.printStackTrace();
            System.out.println("Chyba2 "+ex.getMessage());
        }
        
        
        StringBuffer  userListGlobal = new StringBuffer(new String(""));
        
        for (Iterator i = clients.iterator(); i.hasNext(); ){
            skNetwork ct = (skNetwork)i.next();
            String auser = ct.user;
            if (auser.compareTo(user)!=0)   userListGlobal.append("\n"+auser+"\n"+ct.nick);//@todo was null with great delay not doing anything!
            //chyba1 triggers chyba2 with this code i think
        }
        clients.remove(this);
        this.skServer.sendToAll(skCode.MSGINTRO + "\n" + skCode.USERS + "\n" + (clients.size()) + userListGlobal.toString());//gets exception, one ct is missing
        
        
        System.out.println(" CHAT");
        for (int i = 0; i<chats.size(); i++){
            LinkedList chusers = ((skChat)chats.get(i)).users;
            if (chusers.contains(this)) {
                int chid = ((skChat)chats.get(i)).hash;
                chusers.remove(this);
                this.skServer.sendToChat(i,skCode.MSGINTRO + "\n" + skCode.CHAT_USERS + "\n" + chid + "\n" + chusers.size() + serializeUsers(chusers));
                int lines = 1;
                this.skServer.sendToChat(i,skCode.MSGINTRO + "\n" + skCode.SERVER_TEXT + "\n" + chid + "\n" + lines + "\n" + nick + " disappeared");
            }
            System.out.println(" in chat");
        }
    }
    
    /**
     * decoding incoming messages and sending responses
     */
    private synchronized void decode() {
        System.out.println("-->decode");
        String line;
        try {
            
            line = in.readLine();
            
            System.out.println("----------received\nmsgcode:"+line);
            
            int msgCode = new Integer(line).intValue();
            switch(msgCode) {
                case skCode.CHAT_EXIT:
                    
                    int chatid = new Integer(in.readLine()).intValue();//!!!!!!!!!!!
                    
                    
                    System.out.println(" CHAT");
                    
                    for (int i = 0; i<chats.size(); i++){
                        LinkedList chusers = ((skChat)chats.get(i)).users;
                        if (chusers.contains(this)) {
                            chusers.remove(this);
                            this.skServer.sendToChat(i,skCode.MSGINTRO + "\n" + skCode.CHAT_USERS + "\n" + chatid + "\n" + chusers.size() + serializeUsers(chusers));
                            int lines = 1;
                            this.skServer.sendToChat(i,skCode.MSGINTRO + "\n" + skCode.SERVER_TEXT + "\n" + chatid + "\n" + lines + "\n" + nick + " left this chat");
                        }
                        System.out.println(" in chat");
                        if (chusers.size()==0) chats.remove(i);
                    }
                    
                    
                    break;
                case skCode.ENTER:
                    
                    clients.add(this);
                    
                    
                    line = in.readLine();
                    
                    int users = new Integer(line).intValue();
                    
                    
                    line = in.readLine();
                    user=line;
                    nick = this.skServer.getNick(user);
                    
                    
                    this.skServer.sendToAll(skCode.MSGINTRO + "\n" + skCode.USERS + "\n" + clients.size() + serializeUsers(clients));
                    
                    
                    break;
                case skCode.CLIENT_TEXT:
                    
                    
                    line = in.readLine();
                    
                    System.out.println("chatid: "+line);
                    
                    
                    chatid = new Integer(line).intValue();//!!!!!!!!!!!
                    
                    skChat thechat = new skChat();
                    
                    
                    if (chatid<0) {
                        
                        thechat.users.add(this);
                        
                    }else {
                        //thechat.users=(LinkedList)chats.get(chatid);
                        //thechat.users=((skChat)chats.get(findChat(chatid))).users;
                        thechat = (skChat) chats.get(this.skServer.findChat(chatid));
                    }
                    
                    
                    line = in.readLine();
                    
                    System.out.println("users:"+line);
                    
                    int chatters = new Integer(line).intValue();
                    
                    
                    if (chatid<0){
                        line = in.readLine();
                        System.out.println("user:"+line);
                        thechat.users.add(this.skServer.getUserThread(line));
                        chatid=thechat.users.hashCode();//@todo temporary solution
                        thechat.hash=chatid;
                        //chatid=chats.size();
                        if (thechat.users.size()>2)                            thechat.name = new String(chatid+"#");
                        chats.add(thechat);//chats.add(thechat.users);
                    }else{
                        for (int i = 0; i<chatters; i++){
                            line = in.readLine();
                            if (!thechat.users.contains(this.skServer.getUserThread(line)))
                                thechat.users.add(this.skServer.getUserThread(line));
                            System.out.println("user:"+line);
                        }
                        if (thechat.users.size()>2)                            thechat.name = new String(chatid+"#");
                    }
                    
                    
                    
                    
                    line = in.readLine();
                    
                    System.out.println("chatname:"+line);
                    
                    
                    line = in.readLine();
                    
                    System.out.println("lines:"+line);
                    
                    int lines = new Integer(line).intValue();
                    
                    line = in.readLine();
                    
                    System.out.println("line:"+line);
                    
                    
                    this.skServer.sendToChat(chatid,skCode.MSGINTRO + "\n" + skCode.CHAT_USERS + "\n" + chatid + "\n" + thechat.users.size() + serializeUsers(thechat.users));
                    
                    
                    int chats = 1;
                    
                    if (thechat.name!=null)                        this.skServer.sendToChat(chatid,skCode.MSGINTRO + "\n" + skCode.CHATS + "\n" + chats + "\n" + chatid + "\n" + thechat.name);
                    
                    this.skServer.sendToChat(chatid,skCode.MSGINTRO + "\n" + skCode.SERVER_TEXT + "\n" + chatid + "\n" + lines + "\n" + line);
                    
                    break;
            }
        }  catch (NumberFormatException ex) {
            ex.printStackTrace();
            System.out.println("Chyba6 "+ex.getMessage());
            
        }  catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Chyba6.0 "+ex.getMessage());
            
        }
        System.out.println("<--decode");
    }
    
    private String serializeUsers(LinkedList users) {
        StringBuffer usersline = new StringBuffer();
        int clients = users.size();
        for (Iterator i = users.iterator(); i.hasNext(); ){
            skNetwork ct = (skNetwork)i.next();
            usersline.append("\n"+ct.user+"\n"+ct.nick);
        }
        
        return usersline.toString();
    }
    
    private ArrayList chats;
    
    private LinkedList clients;
    
}