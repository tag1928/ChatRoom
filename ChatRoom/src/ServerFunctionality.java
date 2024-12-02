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
                    connectClient(freeID());
                }
            }
            catch (Exception e)
            {
                System.out.println("No room left for more clients");
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

                    if (sendMessage.getReceiverId() == serverId)
                        sendAll(sendMessage);
                    else
                        sendDM(sendMessage);
                }
            }

            catch (Exception e)
            {
                System.err.println("Failed to send message to client");
            }
        });

        inputThread = new Thread(() ->
        {
            try
            {
                Thread[] clientInputThreads = new Thread[serverSize];

                while (isRunning)
                {
                    if (Thread.currentThread().isInterrupted())
                    {
                        for (int i = 0; i < serverSize; i++)
                        {
                            if (clientInputThreads[i] != null)
                            {
                                clientInputThreads[i].interrupt();
                                clientInputThreads[i] = null;
                            }
                        }
                    }

                    for (int i = 0; i < serverSize; i++)
                    {
                        if (clientIsEmpty(i)) continue;
                        if (clientInputThreads[i] != null) continue;

                        int clientCount = i;

                        clientInputThreads[i] = new Thread(() ->
                        {
                            while (isRunning)
                            {
                                if (Thread.currentThread().isInterrupted())
                                    return;
                                handleMessage(readMessage(clientCount));
                            }
                        });

                        clientInputThreads[i].start();
                    }
                }
            }

            catch (Exception e)
            {
                System.err.println("Failed to receive client message");
                e.printStackTrace();
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