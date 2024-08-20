package org.albert.design_patterns.decorator.decorated;

import org.albert.design_patterns.decorator.contract.PatternFinder;
import org.albert.util.WordIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlainPatternFinder implements PatternFinder
{
    @Override
    public List<WordIndex> find(String text, String pattern)
    {
        List<WordIndex> matches = new ArrayList<>();

        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(text);

        while (matcher.find())
        {
            matches.add(new WordIndex(matcher.start(), matcher.end()));
        }

        return matches;
    }
}
