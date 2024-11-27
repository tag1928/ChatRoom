import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatRoomServerFunctionality
{
    static class ChatRoomClient
    {
        Socket socket;
        DataInputStream inputStream;
        DataOutputStream outputStream;

        public ChatRoomClient(Socket clientSocket) throws Exception
        {
            socket = clientSocket;

            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
        }
    }

    public static void main(String[] args) throws Exception
    {
        final int port = 6969;
        final int serverSize = 5;
        final String welcomeMessage = "Welcome to YapRoom!";
        final String instructionMessage = "To change name, enter \"/name <desired username>\".";
        final String divisorString = "\n================\n";

        ServerSocket serverSocket = new ServerSocket(port);
        ChatRoomClient[] clients = new ChatRoomClient[serverSize];

        clients[0] = new ChatRoomClient(serverSocket.accept());

        System.out.println("SERVER SIDE");
        System.out.println("CHAT LOGS:");
        System.out.println(welcomeMessage);
        System.out.println(instructionMessage);
        System.out.println(divisorString);

        clients[0].outputStream.writeUTF(welcomeMessage);
        clients[0].outputStream.writeUTF(instructionMessage);
        clients[0].outputStream.writeUTF(divisorString);

        Thread acceptThread = new Thread(() ->
        {
            try
            {
                for (int i = 1; i < serverSize; i++)
                {
                    clients[i] = new ChatRoomClient(serverSocket.accept());
                    clients[i].outputStream.writeUTF(welcomeMessage);
                    clients[i].outputStream.writeUTF(instructionMessage);
                    clients[i].outputStream.writeUTF(divisorString);
                }
            }
            catch (Exception e)
            {
                System.out.println("No connect for you");
                e.printStackTrace();
            }
        });

        acceptThread.start();

        acceptThread.join();

        serverSocket.close();

        for (ChatRoomClient x : clients)
        {
            x.socket.close();
        }
    }
}