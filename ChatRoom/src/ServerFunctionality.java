import java.net.ServerSocket;

public class ServerFunctionality extends Server
{
    public static void main(String[] args) throws Exception
    {
        final int port = 6969;
        final String welcomeMessage = "Welcome to YapRoom!";
        final String instructionMessage = "To change name, enter \"/name <desired username>\"\n" + "To quit, enter \"/exit\".";
        final String divisorString = "\n================\n";

        System.out.println("Waiting for connections...");

        ServerSocket serverSocket = new ServerSocket(port);

        Thread acceptThread = new Thread(() ->
        {
            try
            {
                for (int i = 0; i < serverSize; i++)
                {
                    clients[i] = new Client(serverSocket.accept());

                    clients[i].outputStream.writeInt(clients[i].clientId);
                    clients[i].outputStream.flush();

                    clients[i].outputStream.writeUTF(welcomeMessage);
                    clients[i].outputStream.writeUTF(instructionMessage);
                    clients[i].outputStream.writeUTF(divisorString);

                    System.out.println("New user has connected");
                }
            }
            catch (Exception e)
            {
                System.out.println("No connect for you");
                Server.shutDown();
                e.printStackTrace();
            }
        });

        acceptThread.start();

        acceptThread.join();
        for (int i = 0; i < serverSize; i++)
        {
            if (clients[i] == null) continue;
            clients[i].inputThread.join();
            clients[i].outputThread.join();
        }

        serverSocket.close();
        Server.shutDown();
    }
}