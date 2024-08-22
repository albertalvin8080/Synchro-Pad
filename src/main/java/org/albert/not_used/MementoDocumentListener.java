//package org.albert.not_used;
//
//import org.albert.design_patterns.memento.TextAreaCaretaker;
//
//import javax.swing.event.DocumentEvent;
//import javax.swing.event.DocumentListener;
//
//public class MementoDocumentListener implements DocumentListener
//{
//    private final TextAreaCaretaker textAreaCaretaker;
//
//    public MementoDocumentListener(TextAreaCaretaker textAreaCaretaker)
//    {
//        this.textAreaCaretaker = textAreaCaretaker;
//    }
//
//    @Override
//    public void insertUpdate(DocumentEvent e)
//    {
//        System.out.println("INSERT");
//        textAreaCaretaker.saveState();
//    }
//
//    @Override
//    public void removeUpdate(DocumentEvent e)
//    {
//        System.out.println("REMOVE");
//        textAreaCaretaker.saveState();
//    }
//
//    @Override
//    public void changedUpdate(DocumentEvent e)
//    {
////                System.out.println("CHANGE");
//    }
//}
