import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;


public class Client implements Runnable
{
    private static boolean accept_statement = false; //flaga komendy command flag !accept
    private static boolean invite_statement = true; //flaga komendy  command flag !invite
    private static boolean reject_statement = false; //flaga komendy command flag !reject
    private boolean statement = true; //warunek czytania pakietu packet reading condition
    private DataInputStream data_input_stream;
    private DataOutputStream data_output_stream;
    private long session_id; //id sesji// sessionID


    private Client(String ip, int port) //konstruktor klasy Client//client class object constructor
    {
        try
        {
            Socket client_socket = new Socket(ip, port);

            data_input_stream = new DataInputStream(client_socket.getInputStream());
            data_output_stream = new DataOutputStream(client_socket.getOutputStream());
        }
        catch(Exception exception) {}
    }


    @Override
    public void run() //metoda czytająca pakiety tak długo jak nadchodzą//void which reads packets as long as they come
    {
        try
        {
            while(statement) //warunek odczytywania pakietów// reading packets condition
            {
                readPacket();
            }
        }
        catch(Exception exception) {}
    }


    private long bytesToLong(byte[] bytes) //funkcja zamieniająca statyczną tablicę bajtów na zmienną typu long // function which converts static array to long type variable
    {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();

        return buffer.getLong();
    }

    private void decodePacket(byte[] packet) //metoda dekodująca pakiety //decode void
    {
        int answer; //pole odpowiedzi //
        int operation; //pole operacji
        String message; //pole wiadomości

        byte[] message_length_bytes_table = new byte[8];
        byte[] message_bytes_table = new byte[packet.length - 10];
        byte[] session_id_bytes_table = new byte[8];

        operation = ((packet[0] & 0b11110000) >> 4); //dekodowanie pola operacji decoding operation field
        answer = ((packet[0] & 0b00001110) >> 1); //dekodowanie pola odpowiedzi decoding answer field

        for(int i = 0; i < 8; i++)
        {
            message_length_bytes_table[i] = (byte) ((packet[i] & 0b00000001) << 7);
            message_length_bytes_table[i] = (byte) (message_length_bytes_table[i] | ((packet[i + 1] & 0b11111110) >> 1));
        }

        for(int i = 8; i < (packet.length - 2); i++)
        {
            message_bytes_table[i - 8] = (byte) ((packet[i] & 0b00000001) << 7);
            message_bytes_table[i - 8] = (byte) (message_bytes_table[i - 8] | ((packet[i + 1] & 0b11111110) >> 1));
        }

        message = new String(message_bytes_table); //dekodowanie wiadomości za pomocą tablicy bajtów utworzonej dla danego słowa// decoding message with array made for exact word

        session_id_bytes_table[6] = (byte) (packet[packet.length - 2] & 0b00000001);
        session_id_bytes_table[7] = (byte) (packet[packet.length - 1] & 0b11111111);

        session_id = bytesToLong(session_id_bytes_table); //dekodowanie id sesji // decoding sessionID

        if(operation == 0 && answer == 0) {} //pakiet inicjalizacyjny wysyłany przez serwer// server sends a initializing packet
        else if(operation == 0 && answer == 1)
        {
            System.out.println("friend: " + message); //odczytywanie wiadomości od drugiego klienta// displaying message from other client
        }
        else if(operation == 1 && answer == 2) //odczytywanie odpowiedzi serwera na akceptacje zaproszenia ze strony drugiego klienta// reading a message from server about accepting invitation from other client
        {
            accept_statement = false;
            invite_statement = false;
            reject_statement = false;

            System.out.println(message);
        }
        else if(operation == 2 && answer == 3) //odczytywanie odpowiedzi serwera na sprawdzanie dostępności drugiego klienta// reading a message from server about second client availability
        {
            System.out.println(message);
        }
        else if(operation == 3 && answer == 4) //odczytywanie odpowiedzi serwera na rozłączenie przez drugiego klienta// reading a message from server about second client disconnect
        {
            accept_statement = false;
            invite_statement = true;
            reject_statement = false;

            System.out.println(message);
        }
        else if(operation == 4 && answer == 5) //odczytywanie odpowiedzi serwera na opuszczenie czatu przez drugiego klienta // reading a message from server about leaving chat by second client
        {
            statement = false;

            System.out.println(message);
        }
        else if(operation == 5 && answer == 6) //odczytywanie odpowiedzi serwera na zaproszenie do czatu przez drugiego klienta// reading a message from server about invite acceptation from second client
        {
            accept_statement = true;
            invite_statement = false;
            reject_statement = true;

            System.out.println(message);
        }
        else if(operation == 6 && answer == 7) //odczytywanie odpowiedzi serwera na odrzucenie zaproszenia ze strony drugiego klienta //reading a message from server about rejection by second client
        {
            accept_statement = false;
            invite_statement = true;
            reject_statement = false;

            System.out.println(message);
        }
    }

    private byte[] generatePacket(int operation, long session_id, String message) //funkcja generująca i kodująca pakiet do postaci binarnej //function which generates and coed packet to binary form
    {
        int answer = 0; //pole odpowiedzi

        byte[] packet = new byte[10 + message.length()]; //ustalenie długości pakietu na podstawie długości słowa // determining packet length by word length

        long message_length = message.length();

        byte[] length_table = longToBytes(message_length);
        byte[] message_table = new byte[message.length()];
        byte[] session_id_table = longToBytes(session_id);

        packet[0] = (byte) ((operation & 0b00001111) << 4); //kodowanie pola operacji //encoding operation field
        packet[0] = (byte) (packet[0] | (byte) ((answer  & 0b00000111) << 1)); //kodowanie pola odpowiedzi //encoding answer field
        packet[0] = (byte) (packet[0] | (byte) ((length_table[0]) & 0b10000000) >> 7); //kodowanie liczby określającej długość słowa (linia od 150 do 165) // encoding value determining word length
        packet[1] = (byte) (((length_table[0]) & 0b01111111) << 1);
        packet[1] = (byte) (packet[1] | (byte) ((length_table[1]) & 0b10000000) >> 7);
        packet[2] = (byte) (((length_table[1]) & 0b01111111) << 1);
        packet[2] = (byte) (packet[2] | (byte) ((length_table[2]) & 0b10000000) >> 7);
        packet[3] = (byte) (((length_table[2]) & 0b01111111) << 1);
        packet[3] = (byte) (packet[3] | (byte) ((length_table[3]) & 0b10000000) >> 7);
        packet[4] = (byte) (((length_table[3]) & 0b01111111) << 1);
        packet[4] = (byte) (packet[4] | (byte) ((length_table[4]) & 0b10000000) >> 7);
        packet[5] = (byte) (((length_table[4]) & 0b01111111) << 1);
        packet[5] = (byte) (packet[5] | (byte) ((length_table[5]) & 0b10000000) >> 7);
        packet[6] = (byte) (((length_table[5]) & 0b01111111) << 1);
        packet[6] = (byte) (packet[6] | (byte) ((length_table[6]) & 0b10000000) >> 7);
        packet[7] = (byte) (((length_table[6]) & 0b01111111) << 1);
        packet[7] = (byte) (packet[7] | (byte) ((length_table[7]) & 0b10000000) >> 7);
        packet[8] = (byte) (((length_table[7]) & 0b01111111) << 1);

        System.arraycopy(message.getBytes(), 0, message_table, 0, message.length());

        for(int i = 8; i < (packet.length - 2); i++) //encoding message
        {
            packet[i] = (byte) (packet[i] | ((message_table[i - 8]) & 0b10000000) >> 7);
            packet[i + 1] = (byte) (((message_table[i - 8]) & 0b01111111) << 1);
        }

        packet[packet.length - 2] = (byte) (packet[packet.length - 2] | ((session_id_table[6]) & 0b00000001)); //kodowanie id sesji (linia 175 do 176) // encoding sessionID
        packet[packet.length - 1] = (byte) (packet[packet.length - 1] | ((session_id_table[7]) & 0b11111111));

        return packet; //zwracamy gotowy, zakodowany pakiet // returning encoded packet
    }

    private byte[] longToBytes(long x) //funkcja zamieniająca zmienną typu long na tablicę statyczną bajtow
    {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);

        return buffer.array();
    }

    private void readPacket() throws Exception //metoda odczytująca pakiety //decoding void
    {
        int message_length1;
        int statement_value;
        long message_length;

        byte[] packet = new byte[9];

        statement_value = data_input_stream.read(packet, 0, 9); //odczytujemy pierwsze 9 bajtów pakietu co umożliwi nam odczytanie całego rozmiaru pakietu i pobranie go // we read firs 9 bytes,which will allow us to read the size of whole packet

        byte[] message_length_bytes_table = new byte[8];

        for (int i = 0; i < 8; i++)
        {
            message_length_bytes_table[i] = (byte) ((packet[i] & 0b00000001) << 7);
            message_length_bytes_table[i] = (byte) (message_length_bytes_table[i] | ((packet[i + 1] & 0b11111110) >> 1));
        }

        message_length = bytesToLong(message_length_bytes_table);

        message_length1 = (int) message_length;

        byte[] packet1 = new byte[10 + message_length1];

        System.arraycopy(packet, 0, packet1, 0, packet.length);

        data_input_stream.read(packet1, 9, message_length1 + 1);

        if(statement_value == -1) //statement_value ustawi się na -1 w momencie gdy nie będzie miał więcej pakietów do pobrania
        {
            statement = false; //zakończy działanie funkcji run()
        }
        else
        {
            decodePacket(packet1); //wysłanie pakietu do metody dekodującej // decoding packet
        }
    }

    private void writePacket(int operation, String message, long session_id) //metoda wysyłająca pakiety // void which sends packets
    {
        int packet_length = 10 + message.length(); //tworzenie rozmiaru pakietu na podstawie rozmiaru wiadomości // creating packet's size

        try
        {
            data_output_stream.write(generatePacket(operation, session_id, message), 0, packet_length); //wysłanie pakietu do serwera based on word's length
        }
        catch(Exception exception) {}
    }


    public static void main(String args[]) throws Exception //main
    {
        boolean loop_exit_statement = false; //zmienna boolowska umożliwiająca nam wyjście z pętli //allows us to get out of the loop

        Client client = new Client("127.0.0.1",1234); //tworzenie obiektu klienta// creating client object

        Thread thread =new Thread(client); //tworzenie wątku//starting thread

        thread.start(); //wystartowanie wątku

        while(!loop_exit_statement) //warunek wyjścia z pętli
        {
            int operation = 0; //przypisujemy wartość 0, aby za każdym kolejnym wykonaniem się pętli wartość się zerowała

            String message; //wiadomość//message

            Scanner scanner = new Scanner(System.in);

            message = scanner.nextLine(); //wczytanie wiadomości od użytkownika// user is typing a message

            switch(message)
            {
                case "!accept": //komenda !accept
                {
                    operation = 1;

                    if(accept_statement) //warunek akceptacji zaproszenia
                    {
                        System.out.println("INFO: You have accepted second users invite.");

                        accept_statement = false;
                        invite_statement = false;
                        reject_statement = false;

                        client.writePacket(operation, message, client.session_id);
                    }

                    break;
                }
                case "!available": //komenda !available
                {
                    operation = 2;

                    System.out.println("INFO: Request about second user availability been sent to the server. No reply from the server means that second user is unavailable.");

                    client.writePacket(operation, message, client.session_id);

                    break;
                }
                case "!disconnect": //komenda !disconnect
                {
                    operation = 3;

                    System.out.println("INFO: You have disconnected from the chat. Send an invite to connect with second user again.");

                    accept_statement = false;
                    invite_statement = true;
                    reject_statement = false;

                    client.writePacket(operation, message, client.session_id);

                    break;
                }
                case "!exit": //komenda !exit
                {
                    operation = 4;

                    System.out.println("INFO: You have left the chat.");

                    client.writePacket(operation, message, client.session_id);

                    loop_exit_statement = true;

                    break;
                }
                case "!invite": //komenda !invite
                {
                    operation = 5;

                    if(invite_statement) //warunek wysłania zaproszenia
                    {
                        System.out.println("INFO: You have invited second user to the chat.");

                        accept_statement = false;
                        invite_statement = false;
                        reject_statement = false;

                        client.writePacket(operation, message, client.session_id);
                    }

                    break;
                }
                case "!reject": //komenda !reject
                {
                    operation = 6;

                    if(reject_statement) //warunek odrzucenia zaproszenia
                    {
                        System.out.println("INFO: You have rejected second users invite.");

                        accept_statement = false;
                        invite_statement = true;
                        reject_statement = false;

                        client.writePacket(operation, message, client.session_id);
                    }

                    break;
                }
            }

            if(operation == 0)
            {
                client.writePacket(operation, message, client.session_id); //wysłanie wiadomości
            }
        }

        thread.join();
    }
}