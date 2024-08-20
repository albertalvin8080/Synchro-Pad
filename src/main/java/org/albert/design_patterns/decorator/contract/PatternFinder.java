package org.albert.design_patterns.decorator.contract;

import java.util.List;

public interface PatternFinder
{
    List<Integer> find(String text, String pattern);
}
