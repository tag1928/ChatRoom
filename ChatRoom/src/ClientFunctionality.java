import java.io.DataInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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

            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            final int clientId = inputStream.readInt();

            Thread inputThread = new Thread(() ->
            {
                try
                {
                    String readLine;
                    while (Server.isRunning)
                    {
                        readLine = inputStream.readUTF();

                        if (readLine.isBlank()) continue;

                        System.out.println(readLine);
                    }
                }
                catch (Exception e)
                {
                    System.out.println("I aint reading allat");
                    e.printStackTrace();
                }
            });

            Thread outputThread = new Thread(() ->
            {
                try
                {
                    String writeLine;

                    while (Server.isRunning)
                    {
                        writeLine = scanner.nextLine();

                        if (writeLine.isBlank()) continue;

                        outputStream.writeObject(new ClientMessage(writeLine, clientId));

                        if (Server.clientExit.matcher(writeLine).matches())
                        {
                            inputStream.close();
                            outputStream.close();
                            clientSocket.close();
                            scanner.close();

                            return;
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

            inputThread.join();
            outputThread.join();

            clientSocket.close();
            scanner.close();

            inputStream.close();
            outputStream.close();
        }

        catch (Exception e)
        {
            System.out.println("Server is full. Try again next time");
        }
    }
}