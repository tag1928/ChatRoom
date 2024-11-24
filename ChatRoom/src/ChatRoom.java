import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatRoom
{
    public static void main(String[] args) throws Exception
    {
        final int port = 6969;
        final String welcomeMessage = "Welcome to YapRoom\n";
        ServerSocket serverSocket = new ServerSocket(port);
        Socket clientSocket = serverSocket.accept();

        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

        System.out.println(welcomeMessage);
        outputStream.writeUTF(welcomeMessage);

        Thread inputThread = new Thread(() ->
        {
            try
            {
                while (true)
                {
                    synchronized (inputStream)
                    {
                        if (!inputStream.toString().isBlank())
                        {
                            System.out.println(inputStream.readUTF() + '\n');
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
            try
            {
                while (true)
                {
                    synchronized (inputStream)
                    {
                        if (!inputStream.toString().isBlank())
                        {
                            outputStream.writeUTF(inputStream.readUTF() + '\n');
                            outputStream.flush();
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
    }
}