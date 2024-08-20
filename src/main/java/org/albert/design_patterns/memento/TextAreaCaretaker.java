package org.albert.design_patterns.memento;

import java.util.ArrayDeque;
import java.util.Deque;

public class TextAreaCaretaker
{
    private static final int MAX_SIZE = 30;

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

    public void saveState()
    {
        undoDeque.push(originator.createMemento());
        redoDeque.clear(); // Clear redo stack when new actions are performed
        checkSize();
    }

    public void undo()
    {
        if (!undoDeque.isEmpty())
        {
            stateChange = true;
            redoDeque.push(originator.createMemento());
            TextAreaMemento memento = undoDeque.pop();
            originator.restoreMemento(memento);
            checkSize();
        }
    }

    public void redo()
    {
        if (!redoDeque.isEmpty())
        {
            stateChange = true;
            undoDeque.push(originator.createMemento());
            TextAreaMemento memento = redoDeque.pop();
            originator.restoreMemento(memento);
            checkSize();
        }
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
}
