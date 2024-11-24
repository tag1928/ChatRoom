import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ChatRoomClient
{
    public static void main(String[] args) throws Exception
    {
        final int port = 6969;
        Socket clientSocket = new Socket("localhost", port);
        Scanner scanner = new Scanner(System.in);

        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());

        Thread inputThread = new Thread(() ->
        {
            try
            {
                synchronized (inputStream)
                {
                    while (true)
                    {
                        if (!inputStream.readUTF().isBlank())
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
                    outputStream.writeUTF(scanner.nextLine());
                    outputStream.flush();
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
    }
}