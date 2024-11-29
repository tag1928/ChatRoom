import java.io.Serializable;

public class ClientMessage implements Serializable
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

    public ClientMessage(String message, int senderId)
    {
        this.message = message;
        this.senderId = senderId;
        receiverId = -1;
    }
    public ClientMessage(String message, int senderId, int receiverId)
    {
        this.message = message;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }
}