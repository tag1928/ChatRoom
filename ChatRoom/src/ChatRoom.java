import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatRoom
{
    public static void main(String[] args) throws Exception
    {
        final int port = 6969;
        final String welcomeMessage = "Welcome to YapRoom!";
        final String instructionMessage = "To change name, enter \"/name <desired username>\".";
        final String divisorString = "\n================\n";

        ServerSocket serverSocket = new ServerSocket(port);
        Socket clientSocket = serverSocket.accept();

        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());

        System.out.println("SERVER SIDE");
        System.out.println("CHAT LOGS:");
        System.out.println(welcomeMessage);
        System.out.println(instructionMessage);
        System.out.println(divisorString);

        outputStream.writeUTF(welcomeMessage);
        outputStream.writeUTF(instructionMessage);
        outputStream.writeUTF(divisorString);
        outputStream.flush();

        Thread inputThread = new Thread(() ->
        {
            String readLine = "";
            try
            {
                synchronized (inputStream)
                {
                    while (true)
                    {
                        readLine = inputStream.readUTF();

                        if (!readLine.isBlank())
                        {
                            System.out.println(readLine);
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

        Thread outputThread = new Thread(() ->
        {
            String writeLine = "";
            try
            {
                synchronized (inputStream)
                {
                    while (true)
                    {
                        writeLine = inputStream.readUTF();

                        if (!writeLine.isBlank())
                        {
                            outputStream.writeUTF(writeLine);
                        }
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

        inputStream.close();
        outputStream.close();

        serverSocket.close();
        clientSocket.close();
    }
}