package org.albert.design_patterns.memento_v3;

import org.albert.util.OperationType;

import java.util.ArrayDeque;
import java.util.Deque;

public class TextAreaCaretaker
{
    private static final int MAX_SIZE = 50;

    private boolean stateChange;

    private final TextAreaOriginator originator;
    private final Deque<TextAreaMemento> undoDeque;
    private final Deque<TextAreaMemento> redoDeque;

    public TextAreaCaretaker(TextAreaOriginator originator)
    {
        this.originator = originator;
        this.undoDeque = new ArrayDeque<>();
        this.redoDeque = new ArrayDeque<>();
    }

    public void saveState(int offset, int length, String text, OperationType operationType)
    {
        undoDeque.push(originator.createMemento(offset, length, text, operationType));
        redoDeque.clear(); // Clear redo stack when new actions are performed
        checkSize();
    }

    public void undo()
    {
        if (undoDeque.isEmpty()) return;

        stateChange = true;
        TextAreaMemento memento = undoDeque.pop();

        redoDeque.push(originator.createMemento(
                memento.offset,
                memento.length,
                memento.text,
                memento.operationType
        ));

        originator.restoreMemento(memento);
        checkSize();
    }

    public void redo()
    {
        if (undoDeque.isEmpty()) return;

        stateChange = true;
        TextAreaMemento memento = redoDeque.pop();
        undoDeque.push(originator.createMemento(
                memento.offset,
                memento.length,
                memento.text,
                memento.operationType
        ));
        originator.restoreMemento(memento);
        checkSize();
    }

    private void checkSize()
    {
        if (undoDeque.size() > MAX_SIZE)
            undoDeque.removeLast();
        if (redoDeque.size() > MAX_SIZE)
            redoDeque.removeLast();
    }

    public boolean getStateChange()
    {
        return stateChange;
    }

    public void setStateChange(boolean stateChange)
    {
        this.stateChange = stateChange;
    }

    public void clearAll()
    {
        undoDeque.clear();
        redoDeque.clear();
    }
}
