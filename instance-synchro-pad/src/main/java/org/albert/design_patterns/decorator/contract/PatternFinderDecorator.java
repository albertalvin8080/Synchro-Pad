package org.albert.design_patterns.decorator.contract;

public abstract class PatternFinderDecorator implements PatternFinder
{
    protected final PatternFinder wrapped;

    public PatternFinderDecorator(PatternFinder wrapped)
    {
        this.wrapped = wrapped;
    }
}
