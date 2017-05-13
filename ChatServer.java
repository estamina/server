import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {
    public static final int CHAT_PORT=12345;

    //Zoznam klientov
    LinkedList clientsList = new LinkedList();

    //Rozoslanie spravy vsetkym
    private void sendToAll(String message){
        for(Iterator i=clientsList.iterator();i.hasNext();)
            ((ClientThread)i.next()).sendMessage(message);
    }

    //Vlakno komunikacie s klientom
    class ClientThread extends Thread {
        //Pomocou tohto socketu komunikujem s klientom
        Socket clientSocket;

        //Streamy
        BufferedReader in;
        OutputStreamWriter out;

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
                System.out.println("Chyba "+e.getMessage());
            }
        }

        public void run() {
            try{
                //Streamy
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new OutputStreamWriter(clientSocket.getOutputStream());

                String line;
                //Precitam riadok
                while ((line = in.readLine()) != null) {
                    //Rozpozlem spravu vsetkym
                    sendToAll(line);

                    //Vypis pre testovanie
                    System.out.println(line);
                }

                //Uzavriem streamy
                in.close();
                out.close();

                //Uzavriem klientsky socket
                clientSocket.close();

            }catch (Exception e){
                System.out.println("Chyba "+e.getMessage());
            }

            //Ak som skoncil komunikaciu vyhodim vlakno z obsluhy
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
            System.out.println("Chyba "+e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        //Instancia tejto triedy
        ChatServer server = new ChatServer();
        server.main();
    }
}
