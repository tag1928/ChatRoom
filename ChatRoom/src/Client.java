import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class Client
{
    Socket socket;

    ObjectOutputStream outputStream;
    ObjectInputStream inputStream;

    String clientName;
    private int clientId;

    public int getClientId()
    {
        return clientId;
    }

    public synchronized void disconnect()
    {
        try
        {
            outputStream = null;
            inputStream = null;
            clientName = null;
            socket.close();
        }

        catch (Exception e)
        {
            System.err.println("Failed to disconnect client");
        }
    }

    public Client()
    {
        try
        {
            clientId = Server.freeID() + 1;
            socket = Server.serverSocket.accept();
            clientName = "Anon " + clientId;

            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        }

        catch (Exception e)
        {
            System.err.println("Failed to create client object");
        }
    }
}