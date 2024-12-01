import java.io.Serializable;

public class Message implements Serializable
{
    private final String message;

    private final int senderId;

    private final int receiverId;

    public String getMessage()
    {
        return message;
    }

    public int getSenderId()
    {
        return senderId;
    }

    public int getReceiverId()
    {
        return receiverId;
    }

    public static boolean isEmpty(Message message)
    {
        if (message == null) return true;
        if (message.getMessage() == null) return true;
        if (message.getMessage().isBlank()) return true;

        return false;
    }

    public Message(String message, int senderId)
    {
        this.message = message;
        this.senderId = senderId;
        receiverId = -1;
    }

    public Message(String message, int senderId, int receiverId)
    {
        this.message = message;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }
}