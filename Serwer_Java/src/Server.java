import java.net.ServerSocket;
import java.util.Random;


public class Server
{
    private ServerSocket server_socket;


    private Server() {} //konstruktor domyślny klasy Server // default constructor server class


    private int generateSessionId() //funkcja generująca id sesji dla klientów //function which generates sessionID for clients
    {
        int session_id;

        Random random = new Random();

        session_id = random.nextInt(511) + 1; //losujemy liczbę z przedziału od 1 do 511 (liczba musi być zapisana maksymalnie na 9 bitach) //random generating ID

        return session_id;
    }

    private void startConnection() //metoda inicjująca połączenie serwera z klientami // initializing connection between server and clients
    {
        try
        {
            int session_id = generateSessionId(); //generowanie id sesji dla pierwszego klienta //generating session Id for first client
            int session_id1;

            server_socket = new ServerSocket(1234);

            do
            {
                session_id1 = generateSessionId(); //generowanie id sesji dla drugiego klienta //generating session Id for second client
            }
            while(session_id == session_id1); //warunek zapewniający wylosowanie unikalnego id sesji dla każdego klienta //condition which provides uniqe session id

            Client client = new Client(server_socket, 1, session_id); //tworzenie obiektu pierwszego klienta //client1 object
            Client client1 = new Client(server_socket, 2, session_id1); //tworzenie obiektu drugiego klienta //client2 object

            client.makePartner(client1.getClient()); //połączenie klientów //making partners
            client1.makePartner(client.getClient()); //połącznie klientów

            Thread thread = new Thread(client); //utworzenie wątku dla klienta pierwszego //thread for first client
            Thread thread1 = new Thread(client1); //utworzenie wątku dla klienta drugiego //thread for second client

            thread.start(); //wystartowanie wątku //starting threads
            thread1.start(); //wystartowanie wątku

            thread.interrupt();
            thread1.interrupt();
        }
        catch(Exception exception) {}
    }

    private void stopConnection() //metoda zamykająca serwer //closing socket
    {
        try
        {
            server_socket.close();
        }
        catch(Exception exception) {}
    }


    public static void main(String[] args)
    {
        Server server = new Server(); //tworzenie obiektu typu Serwer //server object initialization

        server.startConnection(); //wystarwowanie serwera i łączenie klientów //starting connection
        server.stopConnection(); //zatrzymanie serwera
    }
}