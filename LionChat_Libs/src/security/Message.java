package security;

/**
 *
 * @author fazo
 */
public class Message {

    private String signature, text;
    private boolean encrypted;

    public Message(String text, String signature) {
        this.signature = signature;
        this.text = text;
        encrypted = true;
    }
}
