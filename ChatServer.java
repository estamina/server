import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {
    public static final int CHAT_PORT=12345;
    
    /**
     * list of client threads for each connected user
     */
    LinkedList clientsList = new LinkedList();
    
    
    private void sendToAll(String message){
        System.out.println("-------sent\n"+message);
        for(Iterator i=clientsList.iterator();i.hasNext();)
            ((ClientThread)i.next()).sendMessage(message);
    }
    
    /**
     * todo: configuration file with nicknames
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
    public ClientThread getUserThread(String user) {
        //String user=null;
        ClientThread needle=null;
        for (Iterator i=clientsList.iterator();i.hasNext();){
            needle=(ClientThread)i.next();
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
    class ClientThread extends Thread {
        
        Socket clientSocket;
        public String user,nick;
        
        BufferedReader in;
        //OutputStreamWriter out;
        BufferedWriter out;
        String msgIntro="\6";
        
        ClientThread(Socket clientSocket){
            this.clientSocket=clientSocket;
        }
        
        
        void sendMessage(String message){
            try{
                
                out.write(message+"\n");
                //flushing a buffer to send a message
                out.flush();
            }catch (Exception e){
                System.out.println("Chyba1 "+e.getMessage());
                
            }
        }
        
        
        public void run() {
            try{
                
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                
                String line;
                
                //endless loop
                while ((line = in.readLine()) != null) {
                    //fishing for the intro byte indicating a valid message start
                    while (line.compareTo(msgIntro)!=0){line = in.readLine();}
                    decode();
                }
                
                in.close();
                out.close();
                
                clientSocket.close();
                
            }catch (Exception e){
                System.out.println("Chyba2 "+e.getMessage());
                
            }
            
            StringBuffer  userListGlobal = new StringBuffer(new String(""));
            
            for(Iterator i=clientsList.iterator();i.hasNext();){
                ClientThread ct= (ClientThread)i.next();
                String auser=ct.user;
                if (auser.compareTo(user)!=0)   userListGlobal.append("\n"+auser+"\n"+ct.nick);//todo: was null with great delay not doing any thing!
                //chyba1 triggers chyba2 with this code i think
            }
            clientsList.remove(this);
            sendToAll(msgIntro+"\n1\n"+(clientsList.size())+userListGlobal.toString());//gets exception, one ct is missing
            
            
            System.out.println(" CHAT");
            for(int i=0;i<chats.size();i++){
                LinkedList chusers=(LinkedList)chats.get(i);
                if (chusers.contains(this)) {
                    chusers.remove(this);
                    sendToChat(i,msgIntro+"\n2\n"+i+"\n"+chusers.size()+"\n"+serializeUsers(chusers));
                    sendToChat(i,msgIntro+"\n4\n"+i+"\n1\n"+nick+" disappeared");
                }
                System.out.println(" in chat");
            }
        }
        
        /**
         * decoding incoming messages and sending responses
         */
        synchronized public void decode() {
            String line;
            try {
                line = in.readLine();
                
                System.out.println("----------received\nmsgcode:"+line);
                
                int msgCode=new Integer(line).intValue();
                switch (msgCode) {
                    case 9:
                        int chatid=new Integer(line).intValue();//!!!!!!!!!!!

                        System.out.println(" CHAT");
                        for(int i=0;i<chats.size();i++){
                            LinkedList chusers=(LinkedList)chats.get(i);
                            if (chusers.contains(this)) {
                                chusers.remove(this);
                                sendToChat(i,msgIntro+"\n2\n"+i+"\n"+chusers.size()+"\n"+serializeUsers(chusers));
                                sendToChat(i,msgIntro+"\n4\n"+i+"\n1\n"+nick+" left this chat");
                            }
                            System.out.println(" in chat");
                        }
                        
                        break;
                    case 6:
                        clientsList.add(this);
                        
                        line = in.readLine();
                        int users=new Integer(line).intValue();
                        
                        line = in.readLine();user=line;	 nick=getNick(user);
                        
                        StringBuffer userListGlobal = new StringBuffer(new String(""));
                        
                        for(Iterator i=clientsList.iterator();i.hasNext();){
                            ClientThread ct= (ClientThread)i.next();
                            userListGlobal.append("\n"+ct.user+"\n"+ct.nick);
                        }
                        
                        sendToAll(msgIntro+"\n1\n"+clientsList.size()+userListGlobal.toString());
                        
                        break;
                    case 0:
                        
                        line = in.readLine();
                        System.out.println("chatid: "+line);
                        
                        chatid=new Integer(line).intValue();//!!!!!!!!!!!
                        chat thechat=new chat();
                        
                        if (chatid<0) {
                            
                            thechat.users.add(this);
                            
                        }else thechat.users=(LinkedList)chats.get(chatid);
                        
                        line = in.readLine();
                        System.out.println("users:"+line);
                        int chatters=new Integer(line).intValue();
                        
                        if (chatid<0){
                            line = in.readLine();
                            System.out.println("user:"+line);
                            thechat.users.add(getUserThread(line));
                            chatid=chats.size();
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
                        
/*
                        StringBuffer usersline=new StringBuffer();
                        int clients=thechat.users.size();
                        for(Iterator i=thechat.users.iterator();i.hasNext();){
                            ClientThread h=(ClientThread)i.next();
                            usersline.append(h.user+"\n");
                            usersline.append(h.nick+"\n");
                        }
 */
                        
                        //todo: is inside sendToChat
//                        sendToChat(chatid,msgIntro+"\n2\n"+chatid+"\n"+clients+"\n"+usersline.toString().trim());
                        sendToChat(chatid,msgIntro+"\n2\n"+chatid+"\n"+thechat.users.size()+"\n"+serializeUsers(thechat.users));
                        
                        
                        if (thechat.name!=null) sendToChat(chatid,msgIntro+"\n3\n1\n"+chatid+"\n"+thechat.name);
                        sendToChat(chatid,msgIntro+"\n4\n"+chatid+"\n"+lines+"\n"+line);
                        break;
                    default:
                        
                }
            }catch (Exception e){
                System.out.println("Chyba6 "+e.getMessage());
            }
            
        }
        
        public String serializeUsers(LinkedList users) {
            StringBuffer usersline=new StringBuffer();
            int clients=users.size();
            for(Iterator i=users.iterator();i.hasNext();){
                ClientThread h=(ClientThread)i.next();
                usersline.append(h.user+"\n");
                usersline.append(h.nick+"\n");
            }
            
            return usersline.toString().trim();
        }
    }
    
    
    private void main(){
        downloadNicks();
        try{
            //waiting for socket on this port
            ServerSocket socket = new ServerSocket(CHAT_PORT);
            
            //endless loop
            while(true){
                Socket acceptedSocket = socket.accept();
                
                ClientThread ct = new ClientThread(acceptedSocket);
                
                ct.start();
            }
        }catch (Exception e){
            System.out.println("Chyba3 "+e.getMessage());
        }
    }
    
    public static void main(String[] args) throws Exception {
        ChatServer server = new ChatServer();
        server.main();
    }
    
    /**
     * chat list source of chat ids sent to client
     */
    private ArrayList chats= new ArrayList();
    
    /**
     * sends to users subscribed to chat id
     */
    public void sendToChat(int chatid,String message) {
        //todo: on code 2 it should be only first time members of chatid
        //now it sends every time to each client
        LinkedList them=(LinkedList)chats.get(chatid);
        System.out.println("-------sent\n"+message);
        for(Iterator i=them.iterator();i.hasNext();)
            ((ClientThread)i.next()).sendMessage(message);
    }
    
    public final class chat {
        private String name=null;
        
        /**
         * participats on this chat
         */
        private LinkedList users=new LinkedList();
    }
    
    public final class skUser {
        private String user;
        
        private String nick;
        
        private String family_name;
        
        private String given_name;
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
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.out.println("Chyba "+ex.getMessage());
        } catch (IOException ex){
            ex.printStackTrace();
            System.out.println("Chyba "+ex.getMessage());
        }
    }
    
    
}
