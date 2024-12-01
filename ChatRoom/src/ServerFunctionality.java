import java.net.ServerSocket;

public class ServerFunctionality extends Server
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("Waiting for connections...");

        serverSocket = new ServerSocket(port);

        acceptThread = new Thread(() ->
        {
            try
            {
                while (isRunning)
                {
                    for (int i = 0; i < serverSize; i++)
                    {
                        if (clients[i] == null) connectClient(i);
                    }
                }
            }
            catch (Exception e)
            {
                System.err.println("Failed to accept client");
            }
        });

        outputThread = new Thread(() ->
        {
            Message sendMessage;

            try
            {
                while(isRunning)
                {
                    if (chatQueue.isEmpty()) continue;
                    sendMessage = chatQueue.poll();
                    if (Message.isEmpty(sendMessage)) continue;

                    if (sendMessage.getReceiverId() == serverId) sendAll(sendMessage);
                    else sendDM(sendMessage);
                }
            }

            catch (Exception e)
            {
                System.err.println("Failed to send message to client");
            }
        });

        inputThread = new Thread(() ->
        {
            Thread[] clientInputThreads = new Thread[serverSize];

            while (isRunning)
            {
                for (int i = 0; i < serverSize; i++)
                {
                    if (clients[i] == null) continue;
                    if (clientInputThreads[i] != null) continue;

                    int clientCursor = i;

                    clientInputThreads[i] = new Thread(() ->
                    {
                        Message readMessage;
                        String strMessage;

                        try
                        {
                            while (isRunning)
                            {
                                readMessage = (Message) clients[clientCursor].inputStream.readObject();

                                if (Message.isEmpty(readMessage)) continue;

                                strMessage = readMessage.getMessage();

                                if (clientExit.matcher(strMessage).matches())
                                {
                                    disconnectClient(clientCursor);
                                    return;
                                }

                                if (changeClientName.matcher(strMessage).matches()) changeClientName(clientCursor, strMessage.substring(6));

                                else if (readMessage.getReceiverId() == serverId) sendAll(readMessage);

                                else sendDM(readMessage);
                            }
                        }

                        catch (Exception e)
                        {
                            System.err.println("Failed to read message from client");
                        }
                    });

                    clientInputThreads[i].start();
                    if (!clientInputThreads[i].isAlive()) clientInputThreads[i] = null;
                }
            }
        });

        acceptThread.start();
        outputThread.start();
        inputThread.start();

        acceptThread.join();
        outputThread.join();
        inputThread.join();

        Server.shutDown();
    }
}