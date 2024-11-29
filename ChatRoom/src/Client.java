import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

class Client
{
    Socket socket;

    ObjectInputStream inputStream;
    DataOutputStream outputStream;

    Thread inputThread;
    Thread outputThread;

    String clientName;
    private static int clientCount = 1;
    public final int clientId = clientCount;

    protected void setClientName(String name)
    {
        clientName = name;
    }

    public Client(){}

    public Client(Socket clientSocket) throws Exception
    {
        socket = clientSocket;
        clientName = "Anon " + clientId;
        clientCount++;

        inputStream = new ObjectInputStream(clientSocket.getInputStream());
        outputStream = new DataOutputStream(clientSocket.getOutputStream());

        inputThread = new Thread(() ->
        {
            try
            {
                ClientMessage readMessage;

                while (Server.isRunning)
                {
                    readMessage = (ClientMessage)inputStream.readObject();

                    if (readMessage.getMessage().isBlank()) continue;

                    if (Server.clientExit.matcher(readMessage.getMessage()).matches())
                    {
                        Server.disconnectClient(Client.this);
                        return;
                    }

                    if (Server.changeClientName.matcher(readMessage.getMessage()).matches())
                    {
                        System.out.print(clientName + " changed name to ");
                        setClientName(readMessage.getMessage().substring(6));
                        System.out.print(clientName + '\n');
                    }

                    else
                    {
                        System.out.println(clientName + ": " + readMessage.getMessage());
                        Server.chatQueue.add(readMessage);
                        Server.chatLog.add(readMessage);
                    }
                }
            }
            catch (Exception e)
            {
                System.out.println("I aint reading allat");
                Server.shutDown();
                e.printStackTrace();
            }
        });

        outputThread = new Thread(() ->
        {
            ClientMessage writeMessage;

            try
            {
                while (Server.isRunning)
                {
                    writeMessage = Server.chatQueue.peek();

                    if (writeMessage == null) continue;

                    if (writeMessage.getMessage().isBlank()) continue;

                    Server.sendAll(writeMessage);
                }
            }
            catch (Exception e)
            {
                System.out.println("Yapping failed");
                Server.shutDown();
                e.printStackTrace();
            }
        });

        inputThread.start();
        outputThread.start();
    }
}