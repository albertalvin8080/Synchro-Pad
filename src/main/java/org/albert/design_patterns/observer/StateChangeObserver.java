package org.albert.design_patterns.observer;

public interface StateChangeObserver
{
    void onInsert(int offset, int length, String text);
    void onDelete(int offset, int length, String text);
}