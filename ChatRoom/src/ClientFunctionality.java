import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientFunctionality
{
    public static void main(String[] args)
    {
        try
        {
            final int port = 6969;

            Socket clientSocket = new Socket("localhost", port);
            Scanner scanner = new Scanner(System.in);

            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

            final int clientId = inputStream.readInt();

            Thread outputThread = new Thread(() ->
            {
                try
                {
                    Message sendMessage;
                    String writeLine;

                    while (Server.isRunning)
                    {
                        writeLine = scanner.nextLine();
                        if (writeLine.isBlank()) continue;

                        if (Server.clientExit.matcher(writeLine).matches())
                        {
                            outputStream.writeObject(new Message(writeLine, clientId));

                            return;
                        }

                        if (Server.DM.matcher(writeLine).matches())
                        {
                            outputStream.writeObject(new Message(writeLine, clientId, Server.getId(writeLine.split(writeLine)[1])));
                        }

                        sendMessage = new Message(writeLine, clientId);

                        outputStream.writeObject(sendMessage);
                    }
                }
                catch (Exception e)
                {
                    System.err.println("Failed to send message to server");
                }
            });

            Thread inputThread = new Thread(() ->
            {
                try
                {
                    Message readMessage;

                    while (Server.isRunning)
                    {
                        readMessage = (Message) inputStream.readObject();

                        if (readMessage.getSenderId() == Server.serverId && readMessage.getMessage().equals(Server.disconnectString))
                        {
                            return;
                        }

                        System.out.println(readMessage.getMessage());
                    }
                }
                catch (Exception e)
                {
                    System.err.println("Failed to read message from server");
                }
            });

            outputThread.start();
            inputThread.start();

            outputThread.join();
            inputThread.join();

            clientSocket.close();
            scanner.close();
        }

        catch (UnknownHostException e)
        {
            System.err.println("Failed to connect to server. Server might be full");
        }

        catch (IOException e)
        {
            System.err.println("Failed to read/write messages from/to server");
        }

        catch (InterruptedException e)
        {
            System.err.println("Threads got interrupted");
        }
    }
}