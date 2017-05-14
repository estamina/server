import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {
    public static final int CHAT_PORT=12345;

    //Zoznam klientov
    LinkedList clientsList = new LinkedList();

    //Rozoslanie spravy vsetkym
    private void sendToAll(String message){
		System.out.println("snd\n"+message);
        for(Iterator i=clientsList.iterator();i.hasNext();)
            ((ClientThread)i.next()).sendMessage(message);
    }

    //Vlakno komunikacie s klientom
    class ClientThread extends Thread {
        //Pomocou tohto socketu komunikujem s klientom
        Socket clientSocket;
		public String user;
        //Streamy
        BufferedReader in;
        //OutputStreamWriter out;
		BufferedWriter out;

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
                    //Vypis pre testovanie
                    System.out.println("rcv\n"+line);
                    //Rozpozlem spravu vsetkym

					int msgCode=new Integer(line).intValue();
					switch (msgCode)
					{
						case 6:
							line = in.readLine();
							int users=new Integer(line).intValue();

							line = in.readLine();user=line;
										  // System.out.println(user);
							StringBuffer  userListGlobal = new StringBuffer(new String(""));

							for(Iterator i=clientsList.iterator();i.hasNext();){
//								System.out.println(userListGlobal+" in");
								userListGlobal.append("\n"+((ClientThread)i.next()).user);
//								System.out.println(userListGlobal+" in");
							}

							sendToAll("1\n"+clientsList.size()+userListGlobal.toString());

							break;
						case 0:
							line = in.readLine();
							int lines=new Integer(line).intValue();
							line = in.readLine();
							sendToAll("0\n"+lines+"\n"+line);break;
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
								String auser=((ClientThread)i.next()).user;
								if (auser.compareTo(user)!=0)   userListGlobal.append("\n"+auser);
//								System.out.println(userListGlobal+" in");
							}

							sendToAll("1\n"+(clientsList.size()-1)+userListGlobal.toString());
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
}
