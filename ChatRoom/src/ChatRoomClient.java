import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatRoomClient
{
    public static void main(String[] args) throws Exception
    {
        final int port = 6969;
        final String[] name = {"Anon"};

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
                Pattern changeName = Pattern.compile("^\\/name .+$");

                while (true)
                {
                    String input = scanner.nextLine();
                    Matcher matcher = changeName.matcher(input);
                    if (matcher.find())
                    {
                        name[0] = input.substring(6);
                    }
                    else
                    {
                        outputStream.writeUTF(name[0] + ": " + input);
                        outputStream.flush();
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