package server;
import chat.skUser;
import chat.skCode;
import java.net.*;
import java.io.*;
import java.util.*;

public class skServer {
    private static int CHAT_PORT=12345;
    
    /**
     * list of client threads for each connected user
     */
    LinkedList clientsList = new LinkedList();
    
    
    private synchronized void sendToAll(String message){
        System.out.println("-------sent\n"+message);
        for(Iterator i=clientsList.iterator();i.hasNext();)
            ((skNetwork)i.next()).sendMessage(message);
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
        for (Iterator i=clientsList.iterator();i.hasNext();){
            needle=(skNetwork)i.next();
            System.out.println(needle.user+" "+needle.nick+" "+user.trim());
            if ((needle).user.compareTo(user.trim())==0) {
                //user=needle.user;
                System.out.println(needle.user+" found "+needle.nick);
                
                break;
            }
        }
        return needle;
    }
    
    
    
    /**
     * client thread created with each connected user
     */
    class skNetwork extends Thread {
        
        Socket clientSocket;
        public String user,nick;
        
        BufferedReader in;
        //OutputStreamWriter out;
        BufferedWriter out;
        
        skNetwork(Socket clientSocket){
            this.clientSocket=clientSocket;
        }
        
        
        void sendMessage(String message){
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
                    while (line.compareTo(skCode.MSGINTRO)!=0){line = in.readLine();}
                    decode();
                }
                
                in.close();
                out.close();
                
                clientSocket.close();
            } catch (IOException ex) {
                //ex.printStackTrace();
                System.out.println("Chyba2 "+ex.getMessage());
            }
            
            
            StringBuffer  userListGlobal = new StringBuffer(new String(""));
            
            for(Iterator i=clientsList.iterator();i.hasNext();){
                skNetwork ct= (skNetwork)i.next();
                String auser=ct.user;
                if (auser.compareTo(user)!=0)   userListGlobal.append("\n"+auser+"\n"+ct.nick);//@todo was null with great delay not doing anything!
                //chyba1 triggers chyba2 with this code i think
            }
            clientsList.remove(this);
            sendToAll(skCode.MSGINTRO+"\n"+skCode.USERS+"\n"+(clientsList.size())+userListGlobal.toString());//gets exception, one ct is missing
            
            
            System.out.println(" CHAT");
            for(int i=0;i<chats.size();i++){
                LinkedList chusers=(LinkedList)chats.get(i);
                if (chusers.contains(this)) {
                    chusers.remove(this);
                    int chid=findChat(i);
                    //chid=i;
                    sendToChat(i,skCode.MSGINTRO+"\n"+skCode.CHAT_USERS+"\n"+chid+"\n"+chusers.size()+serializeUsers(chusers));
                    int lines=1;
                    sendToChat(i,skCode.MSGINTRO+"\n"+skCode.SERVER_TEXT+"\n"+chid+"\n"+lines+"\n"+nick+" disappeared");
                }
                System.out.println(" in chat");
            }
        }
        
        /**
         * decoding incoming messages and sending responses
         */
        private synchronized void decode() {
            String line;
            try {
                
                line = in.readLine();
                
                System.out.println("----------received\nmsgcode:"+line);
                
                int msgCode=new Integer(line).intValue();
                switch (msgCode) {
                    case skCode.CHAT_EXIT:
                        int chatid=new Integer(line).intValue();//!!!!!!!!!!!
                        
                        System.out.println(" CHAT");
                        for(int i=0;i<chats.size();i++){
                            LinkedList chusers=(LinkedList)chats.get(i);
                            if (chusers.contains(this)) {
                                chusers.remove(this);
                                int chid=findChat(i);
                                //chid=i;
                                sendToChat(i,skCode.MSGINTRO+"\n"+skCode.CHAT_USERS+"\n"+chid+"\n"+chusers.size()+serializeUsers(chusers));
                                int lines=1;
                                sendToChat(i,skCode.MSGINTRO+"\n"+skCode.SERVER_TEXT+"\n"+chid+"\n"+lines+"\n"+nick+" left this chat");
                            }
                            System.out.println(" in chat");
                        }
                        
                        break;
                    case skCode.ENTER:
                        clientsList.add(this);
                        
                        line = in.readLine();
                        int users=new Integer(line).intValue();
                        
                        line = in.readLine();user=line;	 nick=getNick(user);
                        
                        sendToAll(skCode.MSGINTRO+"\n"+skCode.USERS+"\n"+clientsList.size()+serializeUsers(clientsList));
                        
                        break;
                    case skCode.CLIENT_TEXT:
                        
                        line = in.readLine();
                        System.out.println("chatid: "+line);
                        
                        chatid=new Integer(line).intValue();//!!!!!!!!!!!
                        skChat thechat=new skChat();
                        
                        if (chatid<0) {
                            
                            thechat.users.add(this);
                            
                        }else {
                            //thechat.users=(LinkedList)chats.get(chatid);
                            thechat.users=(LinkedList)chats.get(findChat(chatid));
                        }
                        
                        line = in.readLine();
                        System.out.println("users:"+line);
                        int chatters=new Integer(line).intValue();
                        
                        if (chatid<0){
                            line = in.readLine();
                            System.out.println("user:"+line);
                            thechat.users.add(getUserThread(line));
                            chatid=thechat.hashCode();//@todo temporary solution
                            //chatid=chats.size();
                            if (thechat.users.size()>2) thechat.name=new String(chatid+"#");
                            chats.add(thechat.users);
                        }else{
                            for (int i=0;i<chatters;i++){
                                line = in.readLine();
                                if (!thechat.users.contains(getUserThread(line)))
                                    thechat.users.add(getUserThread(line));
                                System.out.println("user:"+line);
                            }
                            if (thechat.users.size()>2) thechat.name=new String(chatid+"#");
                        }
                        
                        
                        
                        line = in.readLine();
                        System.out.println("chatname:"+line);
                        
                        line = in.readLine();
                        System.out.println("lines:"+line);
                        int lines=new Integer(line).intValue();
                        line = in.readLine();
                        System.out.println("line:"+line);
                        
                        sendToChat(chatid,skCode.MSGINTRO+"\n"+skCode.CHAT_USERS+"\n"+chatid+"\n"+thechat.users.size()+serializeUsers(thechat.users));
                        
                        int chats=1;
                        if (thechat.name!=null) sendToChat(chatid,skCode.MSGINTRO+"\n"+skCode.CHATS+"\n"+chats+"\n"+chatid+"\n"+thechat.name);
                        sendToChat(chatid,skCode.MSGINTRO+"\n"+skCode.SERVER_TEXT+"\n"+chatid+"\n"+lines+"\n"+line);
                        break;
                    default:
                        
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                System.out.println("Chyba6 "+ex.getMessage());
                
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Chyba6.0 "+ex.getMessage());
                
            }
            
        }
        
        private String serializeUsers(LinkedList users) {
            StringBuffer usersline=new StringBuffer();
            int clients=users.size();
            for(Iterator i=users.iterator();i.hasNext();){
                skNetwork ct=(skNetwork)i.next();
                usersline.append("\n"+ct.user+"\n"+ct.nick);
            }
            
            return usersline.toString();
        }
        
    }
    
    
    private void main(){
        downloadNicks();
        try {
            //waiting for socket on this port
            ServerSocket socket = new ServerSocket(CHAT_PORT);
            
            //endless loop
            while(true){
                Socket acceptedSocket = socket.accept();
                
                skNetwork ct = new skNetwork(acceptedSocket);
                
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
        LinkedList them=(LinkedList)chats.get(findChat(chatid));
        System.out.println("-------sent\n"+message);
        for(Iterator i=them.iterator();i.hasNext();)
            ((skNetwork)i.next()).sendMessage(message);
    }
    
    public int findChat(int hash) {
        for (int i=0;i<chats.size();i++){
            if (chats.get(i).hashCode()==hash) return i;
        }
        return 0;
    }
    
    public final class skChat {
        private String name=null;
        
        /**
         * participats on this skChat
         */
        private LinkedList users=new LinkedList();
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
