package org.albert.design_patterns.decorator.decorator;

import org.albert.design_patterns.decorator.contract.PatternFinder;
import org.albert.design_patterns.decorator.contract.PatternFinderDecorator;
import org.albert.util.WordIndex;

import java.util.ArrayList;
import java.util.List;

public class WholeWordPatternFinderDecorator extends PatternFinderDecorator
{
    public WholeWordPatternFinderDecorator(PatternFinder wrapped)
    {
        super(wrapped);
    }

    @Override
    public List<WordIndex> find(String text, String pattern)
    {
        return wrapped.find(text, "\\b"+pattern+"\\b");
    }

//    private boolean isWholeWord(String text, String pattern, int textIndex)
//    {
//        int patternEnd = textIndex + pattern.length();
//
//        boolean beforeIsBoundary = (textIndex == 0) || !Character.isLetterOrDigit(text.charAt(textIndex - 1));
//        boolean afterIsBoundary = (patternEnd == text.length()) || !Character.isLetterOrDigit(text.charAt(patternEnd));
//
//        return beforeIsBoundary && afterIsBoundary;
//    }
}
