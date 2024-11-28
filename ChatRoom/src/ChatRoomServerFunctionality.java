import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

public class ChatRoomServerFunctionality
{
    static final int serverSize = 5;

    static final ConcurrentLinkedQueue <String> chatQueue = new ConcurrentLinkedQueue <>();
    static final ConcurrentLinkedQueue <String> chatLog = new ConcurrentLinkedQueue <>();

    static ChatRoomClient[] clients = new ChatRoomClient[serverSize];

    static Pattern changeName = Pattern.compile("^\\/name .+$");

    static synchronized void sendAll(String message, ChatRoomClient sender) throws Exception
    {
        if (message.isBlank()) return;

        for (int i = 0; i < serverSize; i++)
        {
            if (clients[i] == null) return;

            if (clients[i] != sender)
            {
                clients[i].outputStream.writeUTF(message);
            }
        }
    }

    static class ChatRoomClient
    {
        Socket socket;

        DataInputStream inputStream;
        DataOutputStream outputStream;

        Thread inputThread;
        Thread outputThread;

        private String clientName;
        private static int clientCount = 1;

        private void setClientName(String name)
        {
            clientName = name;
        }

        public ChatRoomClient(Socket clientSocket) throws Exception
        {
            socket = clientSocket;
            clientName = "Anon " + clientCount;
            clientCount++;

            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());

            inputThread = new Thread(() ->
            {
                String readLine;

                try
                {
                    while (true)
                    {
                        readLine = inputStream.readUTF();

                        if (!readLine.isBlank())
                        {
                            if (changeName.matcher(readLine).matches())
                            {
                                System.out.print(clientName + " changed name to ");
                                setClientName(readLine.substring(6));
                                System.out.print(clientName + '\n');
                            }
                            else
                            {
                                System.out.println(clientName + ": " + readLine);
                                chatQueue.add(clientName + ": " + readLine);
                                chatLog.add(clientName + ": " + readLine);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    System.out.println("I aint reading allat");
                    e.printStackTrace();
                }
            });

            outputThread = new Thread(() ->
            {
                String writeLine;

                try
                {
                    while (true)
                    {
                        writeLine = chatQueue.poll();

                        if (writeLine == null) writeLine = "";

                        if (!writeLine.isBlank())
                        {
                            sendAll(writeLine, this);
                        }
                    }
                }
                catch (Exception e)
                {
                    System.out.println("Yapping failed");
                    e.printStackTrace();
                }
            });

            inputThread.start();
            outputThread.start();
        }
    }

    public static void main(String[] args) throws Exception
    {
        final int port = 6969;

        final String welcomeMessage = "Welcome to YapRoom!";
        final String instructionMessage = "To change name, enter \"/name <desired username>\".";
        final String divisorString = "\n================\n";

        System.out.println("Waiting for connections...");

        ServerSocket serverSocket = new ServerSocket(port);

        Thread acceptThread = new Thread(() ->
        {
            try
            {
                for (int i = 0; i < serverSize; i++)
                {
                    clients[i] = new ChatRoomClient(serverSocket.accept());

                    clients[i].outputStream.writeUTF(welcomeMessage);
                    clients[i].outputStream.writeUTF(instructionMessage);
                    clients[i].outputStream.writeUTF(divisorString);

                    for (String x : chatLog.stream().toList())
                    {
                        if (x == null) break;
                        if (x.isBlank()) break;
                        clients[i].outputStream.writeUTF(x);
                    }

                    System.out.println("New user has connected");
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
        for (int i = 0; i < serverSize; i++)
        {
            if (clients[i] != null)
            {
                clients[i].inputThread.join();
                clients[i].outputThread.join();
            }
        }

        serverSocket.close();
        for (ChatRoomClient x : clients)
        {
            x.socket.close();
        }
    }
}