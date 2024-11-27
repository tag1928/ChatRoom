import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatRoomClientFunctionality
{
    public static void main(String[] args) throws Exception
    {
        final int port = 6969;
        final String[] name = new String[1];
        name[0] = "Anon";

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
                String writeLine;
                Pattern changeName = Pattern.compile("^\\/name .+$");
                Matcher matcher;

                while (true)
                {
                    writeLine = scanner.nextLine();
                    matcher = changeName.matcher(writeLine);

                    if (matcher.find())
                    {
                        outputStream.writeUTF(name[0] + "changed name to " + writeLine.substring(6));
                        outputStream.flush();
                        name[0] = writeLine.substring(6);
                    }
                    else
                    {
                        outputStream.writeUTF(name[0] + ": " + writeLine);
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