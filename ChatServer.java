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
        synchronized void sendMessage(String message){
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
                    line = in.readLine();
                  //Vypis pre testovanie
                    System.out.println("----------received\nmsgcode:"+line);
                    //Rozpozlem spravu vsetkym

					int msgCode=new Integer(line).intValue();
					switch (msgCode)
					{
						case 6:
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
                                                        int chatidwas=chatid;
                                                        LinkedList chatusers=null;
                                                        chatusers=new LinkedList();
                                                        if (chatid<0) {
                                                            
                                                            chatusers.add(this);
                                                            
                                                        }else chatusers=(LinkedList)chats.get(chatid);
							
							line = in.readLine();
							System.out.println("users:"+line);
							int chatters=new Integer(line).intValue();

                                                        if (chatid<0){ 
                                                            line = in.readLine();
                                                            System.out.println("user:"+line);
                                                            chatusers.add(getUserThread(line));
                                                            chatid=chats.size();
                                                            chats.add(chatusers);
                                                        }else{
                                                            for (int i=0;i<chatters;i++){
                                                                line = in.readLine();
                                                                chatusers.add(getUserThread(line));
                                                                System.out.println("user:"+line);
                                                            }
                                                            chatidwas=-1;
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
                                                        int clients=chatusers.size();
                                                        for(Iterator i=chatusers.iterator();i.hasNext();){
                                                            ClientThread h=(ClientThread)i.next();
                                                            usersline.append(h.user+"\n");
                                                            usersline.append(h.nick+"\n");
                                                        }
                                                        

                                                        if (chatidwas==-1) sendToChat(chatid,msgIntro+"\n2\n"+chatid+"\n"+clients+"\n"+usersline.toString().trim());
                                                        sendToChat(chatid,msgIntro+"\n0\n"+chatid+"\n"+lines+"\n"+line);
                                                        //sendToThem(chatusers,"0\n"+lines+"\n"+line);
							//sendToAll("0\n"+lines+"\n"+line);
                                                        break;
						default:
//						sendToAll(line.substring(1));break;
//						sendToAll("\0"+line.substring(1));break;

					}

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
								if (auser.compareTo(user)!=0)   userListGlobal.append("\n"+auser+"\n"+ct.nick);
//								userListGlobal.append("\n"+ct.user+"\n"+ct.nick);
//								System.out.println(userListGlobal+" in");
							}

							sendToAll(msgIntro+"\n1\n"+(clientsList.size()-1)+userListGlobal.toString());
            clientsList.remove(this);
        }
    }

    //Tato metoda predstavuje cyklus obsluhy
    private void main(){
        try{
            //Pomocou tohto socketu cakam na pripojenie - na porte
            ServerSocket socket = new ServerSocket(CHAT_PORT);

            //Nekonecny cyklus na servery - obsluhy
            while(true){
                Socket acceptedSocket = socket.accept();
                //Vytvaram vlakno obsluhy
                ClientThread ct = new ClientThread(acceptedSocket);
                //Pridam do zoznamu klientov
                clientsList.add(ct);
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
        LinkedList them=(LinkedList)chats.get(chatid);
        System.out.println("-------sent\n"+message);
        for(Iterator i=them.iterator();i.hasNext();)
            ((ClientThread)i.next()).sendMessage(message);
}
}
