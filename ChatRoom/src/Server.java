import java.net.ServerSocket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

abstract class Server
{
    static volatile boolean isRunning = true;

    static final int serverSize = 3;
    static final int port = 6969;
    static final int serverId = -1;

    static final String welcomeMessage = "Welcome to YapRoom!";
    static final String instructionMessage = "To change name, enter \"/name <desired username>\"\n" + "To quit, enter \"/exit\".";
    static final String divisorString = "\n================\n";

    static Client[] clients = new Client[Server.serverSize];
    static final ConcurrentLinkedQueue <Message> chatQueue = new ConcurrentLinkedQueue <>();
    static final private ConcurrentLinkedQueue <Message> chatLog = new ConcurrentLinkedQueue <>();

    static final Pattern changeClientName = Pattern.compile("^\\/name [\\w\\d]+$");
    static final Pattern clientExit = Pattern.compile("/exit");

    static ServerSocket serverSocket;

    static Thread acceptThread;
    static Thread outputThread;
    static Thread inputThread;

    static synchronized void disconnectClient(int clientId)
    {
        clients[clientId].disconnect();
        System.out.println(clients[clientId].clientName + " has quit.");

        clients[clientId] = null;
    }

    static int freeID() throws Exception
    {
        for (int i = 0; i < serverSize; i++)
        {
            if (clients[i] == null) return i + 1;
        }

        throw new Exception("No free ID");
    }

    static void connectClient(int clientId)
    {
        try
        {
            clients[clientId] = new Client();

            clients[clientId].outputStream.writeInt(clientId);
            clients[clientId].outputStream.flush();

            clients[clientId].outputStream.writeObject(new Message(welcomeMessage, serverId));
            clients[clientId].outputStream.writeObject(new Message(instructionMessage, serverId));
            clients[clientId].outputStream.writeObject(new Message(divisorString, serverId));

            clients[clientId].outputStream.flush();

            System.out.println("New user has connected");
        }

        catch (Exception e)
        {
            System.err.println("Failed to connect client to server");
        }
    }

    static protected synchronized void shutDown()
    {
        isRunning = false;

        for (int i = 0; i < serverSize; i++)
        {
            disconnectClient(i);
        }

        try
        {
            serverSocket.close();
        }

        catch (Exception e)
        {
            System.err.println("Failed to close server socket");
        }

        System.out.println("Shutting down...");
    }

    static void changeClientName(int clientId, String name)
    {
        System.out.println(clients[clientId].clientName + " changed name to " + name);
        clients[clientId].clientName = name;
    }

    static void sendAll(Message message)
    {
        if (Message.isEmpty(message)) return;

        String buffer = clients[message.getSenderId()].clientName + ": " + message.getMessage();

        try
        {
            for (int i = 0; i < serverSize; i++)
            {
                if (clients[i] == null) continue;
                if (i == message.getSenderId()) continue;

                clients[i].outputStream.writeObject(new Message(buffer, message.getSenderId()));
            }

            System.out.println(buffer);
            chatLog.add(message);
        }

        catch (Exception e)
        {
            System.err.println("Failed to globally send message");
        }
    }

    static void sendDM(Message message)
    {
        if (Message.isEmpty(message)) return;

        String buffer = clients[message.getSenderId()].clientName + "DM-ed you: " + message.getMessage();

        try
        {
            clients[message.getReceiverId()].outputStream.writeObject(new Message(buffer, message.getSenderId()));
        }

        catch (Exception e)
        {
            System.err.println("Failed to send private message");
        }
    }
}