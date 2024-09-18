package org.albert.design_patterns.observer;

import java.io.IOException;

public interface StateChangeObserver
{
    void onInsert(int offset, int length, String text) throws IOException;
    void onDelete(int offset, int length, String text) throws IOException;
}