package server;
import chat.skUser;
import chat.skCode;
import chat.skChat;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;


public class skServer {
    private static int CHAT_PORT=12345;
    
    /**
     * list of client threads for each connected user
     */
    private List clients = Collections.synchronizedList(new LinkedList());
    
    
    void sendToAll(String message){
        System.out.println("-------sent\n"+message);
        synchronized(clients){
            for(Iterator i=clients.iterator();i.hasNext();)
                ((skNetwork)i.next()).sendMessage(message);
        }
        System.out.println("<--sent");
    }
    
    /**
     * @todo user not listed i names.cfg should be denied
     */
    String getNick(String user){
        for (int i=0;i<users.size();i++){
            skUser uu=(skUser)users.get(i);
            if (uu.user.compareTo(user)==0){
                return uu.nick;
            }
        }
        return user;
    }
    
    /**
     * gets client thread for a user
     */
    public skNetwork getUserThread(String user) {
        //String user=null;
        skNetwork needle=null;
        synchronized(clients){
            for (Iterator i=clients.iterator();i.hasNext();){
                needle=(skNetwork)i.next();
                System.out.println(needle.user+" "+needle.nick+" "+user.trim());
                if ((needle).user.compareTo(user.trim())==0) {
                    //user=needle.user;
                    System.out.println(needle.user+" found "+needle.nick);
                    
                    break;
                }
            }
        }
        return needle;
    }
    
    
    private void main(){
        downloadNicks();
        try {
            //waiting for socket on this port
            ServerSocket socket = new ServerSocket(CHAT_PORT);
            
            //endless loop
            while(true){
                Socket acceptedSocket = socket.accept();
                
                skNetwork ct = new skNetwork(this, acceptedSocket, chats, clients);
                
                ct.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Chyba3 "+ex.getMessage());
        }
    }
    
    public static void main(String[] args) throws Exception {
        if (args[0].length()>0){
            CHAT_PORT=new Integer(args[0]).intValue();
        }
        skServer server = new skServer();
        server.main();
    }
    
    /**
     * skChat list source of skChat ids sent to client
     */
    private ArrayList chats= new ArrayList();
    
    /**
     * sends to users subscribed to skChat id
     * @param chatid chat id >=0
     * @param message all kinds of messages
     */
    public void sendToChat(int chatid,String message) {
        //@todo on skCode.CHATS it should be sent only to first time members of chatid
        //now it sends every time to each client
        //LinkedList them=(LinkedList)chats.get(chatid);
        LinkedList them=((skChat)chats.get(findChat(chatid))).users;
        System.out.println("-------sent\n"+message);
        for(Iterator i=them.iterator();i.hasNext();)
            ((skNetwork)i.next()).sendMessage(message);
    }
    
    public int findChat(int hash) {
        for (int i=0;i<chats.size();i++){
            if (((skChat)chats.get(i)).hash==hash) return i;
        }
        return 0;
        //return hash;
    }
    
    
    /**
     * nicknames from names.cfg file
     */
    private ArrayList users=new ArrayList();
    
    public void downloadNicks() {
        try {
            BufferedReader afile=new BufferedReader(new FileReader("names.cfg"));;
            String line;
            while((line=afile.readLine())!=null){
                skUser uu=new skUser();
                uu.user=line;
                uu.nick=afile.readLine();
                uu.family_name=afile.readLine();
                uu.given_name=afile.readLine();
                users.add(uu);
            }
            afile.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.out.println("Chyba "+ex.getMessage());
        } catch (IOException ex){
            ex.printStackTrace();
            System.out.println("Chyba "+ex.getMessage());
        }
    }
    
    
}
