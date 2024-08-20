package org.albert.design_patterns.decorator.decorated;

import org.albert.design_patterns.decorator.contract.PatternFinder;

import java.util.ArrayList;
import java.util.List;

public class PlainPatternFinder implements PatternFinder
{
    @Override
    public List<Integer> find(String text, String pattern)
    {
        List<Integer> list = new ArrayList<>();
        int previousIndex = -1;
        while(true)
        {
            previousIndex = text.indexOf(pattern, previousIndex + 1);
            if(previousIndex == -1) break;
            list.add(previousIndex);
        }
        return list;
    }
}
