package org.albert.design_patterns.memento_v2;

import org.albert.design_patterns.observer.DataSharerFacade;
import org.albert.design_patterns.observer.DataSharerStateChangeObserver;
import org.albert.util.OperationType;

import javax.swing.*;
import java.util.Arrays;

public class TextAreaOriginator
{
    private final JTextArea textArea;
    private final DataSharerFacade dataSharerFacade;

    public TextAreaOriginator(JTextArea textArea, DataSharerFacade dataSharerFacade)
    {
        this.textArea = textArea;
        this.dataSharerFacade = dataSharerFacade;
    }

    public TextAreaMemento createMemento(int offset, int length, String text, OperationType operationType, String replacementText, boolean undoOrRedo)
    {
        if (operationType == OperationType.INSERT)
        {
            if (!undoOrRedo)
            {
                final String wholeText = textArea.getText();
                final int wholeTextLength = wholeText.length();

//            System.out.println(offset);
//            System.out.println(wholeTextLength);

                if (offset + length > wholeTextLength)
                    replacementText = wholeText.substring(offset, wholeTextLength);
                else
                    replacementText = wholeText.substring(offset, offset + length);

//                System.out.println("REPLACED: " + replacementText);
            }
//            // The length prior to this point was the length of the characters being replaced.
//            // From here, it's the length of the replacing characters.
//            length = text.length();
        }
        // `&& text == null` check is in case the memento being saved is a previous redo/undo one.
        else if (operationType == OperationType.DELETE && text == null)
        {
            // Necessary because remove() from the filter doesn't return the string being removed.
            text = textArea.getText().substring(offset, offset + length);
            System.out.println("text: " + text);
        }

        return new TextAreaMemento(
                offset, text, operationType, textArea.getCaretPosition(), replacementText
        );
    }

    public void restoreMemento(TextAreaMemento memento)
    {
        final StringBuilder sb = new StringBuilder(textArea.getText());
        final int offset = memento.offset;
        final String text = memento.text;
        final String replacementText = memento.replacementText;

        // WARNING: if the operation was INSERT, then you need to replace the new text with the old one in the textArea (even if it was an empty string).
        // WARNING: if the operation was DELETE, then you need to re-insert the text into the textArea.
        if (memento.operationType == OperationType.INSERT)
        {
            final int length = offset + text.length();
            sb.replace(offset, length, replacementText);
            textArea.setText(sb.toString());
            // DANGER: You must pass the length WITHOUT the offset because the DataSharer will also sum it.
            dataSharerFacade.onDelete(offset, text.length(), replacementText);

            System.out.println("ORIGINATOR INSERT");
            System.out.println(Arrays.toString(
                    new Object[]{offset, text.length(), replacementText, DataSharerStateChangeObserver.OP_DELETE}
            ));
        }
        else if (memento.operationType == OperationType.DELETE)
        {
            final int length = offset + replacementText.length();
            sb.replace(offset, length, text);
            textArea.setText(sb.toString());
            // DANGER: You must pass the length WITHOUT the offset because the DataSharer will also sum it.
            dataSharerFacade.onInsert(offset, replacementText.length(), text);

            System.out.println("ORIGINATOR DELETE");
            System.out.println(Arrays.toString(
                    new Object[]{offset, replacementText.length(), text, DataSharerStateChangeObserver.OP_INSERT}
            ));
        }

//        System.out.println("BEFORE CARET");
        textArea.setCaretPosition(memento.caretPosition);
//        System.out.println("AFTER CARET");
    }

}

