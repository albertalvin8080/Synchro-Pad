package org.albert.design_patterns.decorator.contract;

import org.albert.util.WordIndex;

import java.util.List;

public interface PatternFinder
{
    List<WordIndex> find(String text, String pattern);
}
