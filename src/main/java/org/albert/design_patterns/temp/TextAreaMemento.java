public class TextAreaMemento
{
    private final byte[] textBytes;
    private final int caretPosition;

    public TextAreaMemento(byte[] textBytes, int caretPosition)
    {
        this.textBytes = textBytes;
        this.caretPosition = caretPosition;
    }

    public byte[] getTextBytes()
    {
        return text;
        return textBytes;
    }

    public int getCaretPosition()
    {
        return caretPosition;
    }
}