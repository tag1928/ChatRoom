import java.io.IOException;
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
    static final String instructionMessage = "To change name, enter \"/name <desired username>\"\n" + "To quit, enter \"/exit\".\n" + "To DM someone, type \"/dm <name> <message>\"";
    static final String divisorString = "\n================\n";
    static final String disconnectString = "disconnect approved";

    static final private Client[] clients = new Client[serverSize];
    static final ConcurrentLinkedQueue <Message> chatQueue = new ConcurrentLinkedQueue <>();
    static final private ConcurrentLinkedQueue <Message> chatLog = new ConcurrentLinkedQueue <>();

    static final Pattern changeClientName = Pattern.compile("^\\/name [\\w\\d]+$");
    static final Pattern clientExit = Pattern.compile("/exit");
    static final Pattern DM = Pattern.compile("^\\/dm [\\w\\d]+ [\\w\\d]+$");

    static ServerSocket serverSocket;

    static Thread acceptThread;
    static Thread outputThread;
    static Thread inputThread;

    static int getId(String name) throws Exception
    {
        for (int i = 0; i < serverSize; i++)
        {
            if (clients[i] == null) continue;
            if (clients[i].clientName == name) return i;
        }

        throw new Exception("No such client");
    }

    static synchronized void disconnectClient(int clientId)
    {
        try
        {
            System.out.println(clients[clientId].clientName + " has quit.");

            inputThread.interrupt();

            clients[clientId].disconnect();
            clients[clientId] = null;
            clients[clientId].outputStream.writeObject(new Message(disconnectString, serverId));
        }

        catch (IOException e)
        {
            System.err.println("Failed to open client outputStream");
        }
    }

    static synchronized int freeID() throws Exception
    {
        for (int i = 0; i < serverSize; i++)
        {
            if (clients[i] == null) return i;
        }

        throw new Exception("No free ID");
    }

    static synchronized boolean clientIsEmpty(int clientId)
    {
        return clients[clientId] == null;
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

    static synchronized void shutDown()
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

    static Message readMessage(int clientId)
    {
        Message message;

        try
        {
            if (clients[clientId] == null)
                return null;

            message = (Message) clients[clientId].inputStream.readObject();

            if (Message.isEmpty(message))
                return null;
            return message;
        }

        catch (Exception e)
        {
            System.err.println("Failed to read client inputStream");
            e.printStackTrace();
        }
        return null;
    }

    static synchronized void handleMessage(Message message)
    {
        try
        {
            if (Message.isEmpty(message))
                return;

            if (clientExit.matcher(message.getMessage()).matches())
                disconnectClient(message.getSenderId());

            if (changeClientName.matcher(message.getMessage()).matches())
                changeClientName(message.getSenderId(), message.getMessage().substring(6));

            else chatQueue.add(message);
        }

        catch (Exception e)
        {
            System.err.println("Failed to process client message");
        }
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