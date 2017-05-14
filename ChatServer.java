import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {
    public static final int CHAT_PORT=12345;

    //Zoznam klientov
    LinkedList clientsList = new LinkedList();

    //Rozoslanie spravy vsetkym
    private void sendToAll(String message){
		System.out.println("-------sent\n"+message);
        for(Iterator i=clientsList.iterator();i.hasNext();)
            ((ClientThread)i.next()).sendMessage(message);
    }

	String getNick(String user){
		return "nick_"+user;
	}
        
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


    //Vlakno komunikacie s klientom
    class ClientThread extends Thread {
        //Pomocou tohto socketu komunikujem s klientom
        Socket clientSocket;
		public String user,nick;
        //Streamy
        BufferedReader in;
        //OutputStreamWriter out;
		BufferedWriter out;
           String msgIntro="\6";
 
        ClientThread(Socket clientSocket){
            this.clientSocket=clientSocket;
        }

        //Poslanie spravy pre jedneho klienta
         void sendMessage(String message){
            try{

 				out.write(message+"\n");
                //Vyprazdnim buffer a tym prinutim poslat
                out.flush();
            }catch (Exception e){
                System.out.println("Chyba1 "+e.getMessage());

            }
        }


        public void run() {
            try{
                //Streamy
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                out = new OutputStreamWriter(clientSocket.getOutputStream());
                out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                String line;
                //Precitam riadok
                while ((line = in.readLine()) != null) {
                      while (line.compareTo(msgIntro)!=0){line = in.readLine();}
                      decode();
                }

                //Uzavriem streamy
                in.close();
                out.close();

                //Uzavriem klientsky socket
                clientSocket.close();

            }catch (Exception e){
                System.out.println("Chyba2 "+e.getMessage());

            }

            //Ak som skoncil komunikaciu vyhodim vlakno z obsluhy
							StringBuffer  userListGlobal = new StringBuffer(new String(""));

							for(Iterator i=clientsList.iterator();i.hasNext();){
//								System.out.println(userListGlobal+" in");
								ClientThread ct= (ClientThread)i.next();
								String auser=ct.user;
								if (auser.compareTo(user)!=0)   userListGlobal.append("\n"+auser+"\n"+ct.nick);//todo: was null with great delay not doing any thing!
                                                                //chyba1 triggers chyba2 with this code i think
                                                                else {
                                                                    
                                                                }
//								userListGlobal.append("\n"+ct.user+"\n"+ct.nick);
//								System.out.println(userListGlobal+" in");
							}

                                                       System.out.println(" CHAT"); 
                                                        for(int i=0;i<chats.size();i++){
                                                            LinkedList chusers=(LinkedList)chats.get(i);
                                                            if (chusers.contains(this)) {
                                                                chusers.remove(this);
                                                                sendToChat(i,msgIntro+"\n2\n"+i+"\n"+chusers.size()+"\n"+userListGlobal.toString().trim());
                                                                sendToChat(i,msgIntro+"\n4\n"+i+"\n1\n"+nick+" exits");
                                                            }
                                                       System.out.println(" in chat");
                                                        }
							sendToAll(msgIntro+"\n1\n"+(clientsList.size()-1)+userListGlobal.toString());

                                                        clientsList.remove(this);
        }

        synchronized public void decode() {
            String line;
            try {
                line = in.readLine();
                //Vypis pre testovanie
                System.out.println("----------received\nmsgcode:"+line);
                //Rozpozlem spravu vsetkym

                int msgCode=new Integer(line).intValue();
                switch (msgCode)
                {
                    case 6:
                            //Pridam do zoznamu klientov
                            clientsList.add(this);

                            line = in.readLine();
                            int users=new Integer(line).intValue();

                            line = in.readLine();user=line;	 nick=getNick(user);
                                                      // System.out.println(user);
                            StringBuffer  userListGlobal = new StringBuffer(new String(""));

                            for(Iterator i=clientsList.iterator();i.hasNext();){
//								System.out.println(userListGlobal+" in");
                                    ClientThread ct= (ClientThread)i.next();
                                    userListGlobal.append("\n"+ct.user+"\n"+ct.nick);
//								System.out.println(userListGlobal+" in");
                            }

                            sendToAll(msgIntro+"\n1\n"+clientsList.size()+userListGlobal.toString());

                            break;
                    case 0:

                            line = in.readLine();
                            System.out.println("chatid: "+line);

                            int chatid=new Integer(line).intValue();
                            chat thechat=new chat();
                            //LinkedList thechat.users=null;
                            //thechat.users=new LinkedList();
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
                                if (thechat.users.size()>2) thechat.name=new String("room"+chatid);
                                chats.add(thechat.users);
                            }else{
                                for (int i=0;i<chatters;i++){
                                    line = in.readLine();
                                    if (!thechat.users.contains(getUserThread(line)))
                                    thechat.users.add(getUserThread(line));
                                    System.out.println("user:"+line);
                                }
                                if (thechat.users.size()>2) thechat.name=new String("room"+chatid);
                            }



                            line = in.readLine();
                            System.out.println("chatname:"+line);

                            line = in.readLine();
                            System.out.println("lines:"+line);
                            int lines=new Integer(line).intValue();
                            line = in.readLine();
                            System.out.println("line:"+line);
                            //System.out.println(line);

                            StringBuffer usersline=new StringBuffer();
                            int clients=thechat.users.size();
                            for(Iterator i=thechat.users.iterator();i.hasNext();){
                                ClientThread h=(ClientThread)i.next();
                                usersline.append(h.user+"\n");
                                usersline.append(h.nick+"\n");
                            }

                            //todo: is inside sendToChat
                            sendToChat(chatid,msgIntro+"\n2\n"+chatid+"\n"+clients+"\n"+usersline.toString().trim());

                            if (thechat.name!=null) sendToChat(chatid,msgIntro+"\n3\n1\n"+chatid+"\n"+thechat.name);
                            sendToChat(chatid,msgIntro+"\n4\n"+chatid+"\n"+lines+"\n"+line);
                            //sendToThem(thechat.users,"0\n"+lines+"\n"+line);
                            //sendToAll("0\n"+lines+"\n"+line);
                            break;
                    default:
//						sendToAll(line.substring(1));break;
//						sendToAll("\0"+line.substring(1));break;

                }
            }catch (Exception e){
                System.out.println("Chyba6 "+e.getMessage());
            }
            
        }
    }

    //Tato metoda predstavuje cyklus obsluhy
    private void main(){
        try{
            //Pomocou tohto socketu cakam na pripojenie - na porte
            ServerSocket socket = new ServerSocket(CHAT_PORT);

            //Nekonecny cyklus na serveri - obsluhy
            while(true){
                Socket acceptedSocket = socket.accept();
                //Vytvaram vlakno obsluhy
                ClientThread ct = new ClientThread(acceptedSocket);
                //a zacnem obsluhu
                ct.start();
            }
        }catch (Exception e){
            System.out.println("Chyba3 "+e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        //Instancia tejto triedy
        ChatServer server = new ChatServer();
        server.main();
    }

    private ArrayList chats= new ArrayList();

    public void sendToThem(LinkedList them, String message) {
        System.out.println("-------sent\n"+message);
        for(Iterator i=them.iterator();i.hasNext();)
            ((ClientThread)i.next()).sendMessage(message);

    }

    public void sendToChat(int chatid,String message) {
        //todo: on code 2 it should be only first time members of chatid
        //now it sends every time
        LinkedList them=(LinkedList)chats.get(chatid);
        System.out.println("-------sent\n"+message);
        for(Iterator i=them.iterator();i.hasNext();)
            ((ClientThread)i.next()).sendMessage(message);
}

    public final class chat {
        private String name=null;

        private LinkedList users=new LinkedList();
    }



}
