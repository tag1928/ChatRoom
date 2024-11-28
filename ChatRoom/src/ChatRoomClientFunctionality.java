import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ChatRoomClientFunctionality
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
                String readLine = "";

                while (true)
                {
                    readLine = inputStream.readUTF();

                    if (!readLine.isBlank())
                    {
                        System.out.println(readLine);
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
                String writeLine;

                while (true)
                {
                    writeLine = scanner.nextLine();

                    if (!writeLine.isBlank())
                    {
                        outputStream.writeUTF(writeLine);
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
}