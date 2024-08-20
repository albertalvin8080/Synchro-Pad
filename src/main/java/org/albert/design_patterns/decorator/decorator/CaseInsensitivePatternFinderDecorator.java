package org.albert.design_patterns.decorator.decorator;

import org.albert.design_patterns.decorator.contract.PatternFinder;
import org.albert.design_patterns.decorator.contract.PatternFinderDecorator;

import java.util.List;

public class CaseInsensitivePatternFinderDecorator extends PatternFinderDecorator
{
    public CaseInsensitivePatternFinderDecorator(PatternFinder wrapped)
    {
        super(wrapped);
    }

    @Override
    public List<Integer> find(String text, String pattern)
    {
        return wrapped.find(text.toLowerCase(), pattern.toLowerCase());
    }
}
