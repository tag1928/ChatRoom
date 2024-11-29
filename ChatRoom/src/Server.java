import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

class Server
{
    static volatile boolean isRunning = true;
    static final int serverSize = 3;

    static final ConcurrentLinkedQueue <ClientMessage> chatQueue = new ConcurrentLinkedQueue <>();
    static final ConcurrentLinkedQueue <ClientMessage> chatLog = new ConcurrentLinkedQueue <>();

    static final Client[] clients = new Client[Server.serverSize];

    static final Pattern changeClientName = Pattern.compile("^\\/name [\\w\\d]+$");
    static final Pattern clientExit = Pattern.compile("/exit");

    static synchronized void shutDown()
    {
        isRunning = false;

        try
        {
            for (int i = 0; i < serverSize; i++)
            {
                if (clients[i] == null) continue;
                clients[i].socket.close();
                clients[i].inputStream.close();
                clients[i].outputStream.close();
            }

            System.out.println("Shutting down...");
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static synchronized void disconnectClient(Client client)
    {
        try
        {
            System.out.println(client.clientName + " has quit.");
            client.inputStream.close();
            client.outputStream.close();
            client.socket.close();

            clients[client.clientId] = null;
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static synchronized void sendAll(ClientMessage message)
    {
        try
        {
            if (message.getMessage() == null) return;
            if (message.getMessage().isBlank()) return;

            for (int i = 0; i < serverSize; i++)
            {
                if (clients[i] == null) continue;
                if (clients[i].clientId == message.getSenderId()) continue;

                clients[i].outputStream.writeUTF(clients[message.getSenderId()].clientName + ": " + message.getMessage());
            }

            chatQueue.poll();
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}