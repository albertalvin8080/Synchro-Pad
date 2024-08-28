package org.albert.not_used;

import java.util.ArrayList;
import java.util.List;

public class StringDelta
{

    // Class to represent a delta operation
    private static class Delta
    {
        @Override
        public String toString()
        {
            return "Delta{" +
                    "type=" + type +
                    ", startPosition=" + startPosition +
                    ", text='" + text + '\'' +
                    '}';
        }

        enum OperationType
        {DELETE, INSERT}

        OperationType type;
        int startPosition;
        String text;

        Delta(OperationType type, int startPosition, String text)
        {
            this.type = type;
            this.startPosition = startPosition;
            this.text = text;
        }
    }

    // Function to calculate the delta between original and modified strings
    public static List<Delta> calculateDeltas(String original, String modified)
    {
        List<Delta> deltas = new ArrayList<>();
        int originalLength = original.length();
        int modifiedLength = modified.length();
        int i = 0, j = 0;

        while (i < originalLength && j < modifiedLength)
        {
            if (original.charAt(i) != modified.charAt(j))
            {
                // Identify difference in the text
                int startI = i;
                int startJ = j;

                while (i < originalLength && j < modifiedLength && original.charAt(i) != modified.charAt(j))
                {
                    i++;
                    j++;
                }

                // Store the delta operations
                if (startI < i)
                {
                    deltas.add(new Delta(Delta.OperationType.DELETE, startI, original.substring(startI, i)));
                }
                if (startJ < j)
                {
                    deltas.add(new Delta(Delta.OperationType.INSERT, startI, modified.substring(startJ, j)));
                }
            }
            else
            {
                i++;
                j++;
            }
        }

        // Handle remaining characters
        if (i < originalLength)
        {
            deltas.add(new Delta(Delta.OperationType.DELETE, i, original.substring(i)));
        }
        if (j < modifiedLength)
        {
            deltas.add(new Delta(Delta.OperationType.INSERT, i, modified.substring(j)));
        }

        return deltas;
    }

    // Function to revert modified string to original using stored deltas
    public static String applyDeltas(String modified, List<Delta> deltas)
    {
        StringBuilder result = new StringBuilder(modified);

        // Apply deltas in reverse order
        for (int k = deltas.size() - 1; k >= 0; k--)
        {
            Delta delta = deltas.get(k);
            switch (delta.type)
            {
                case DELETE:
                    result.insert(delta.startPosition, delta.text);
                    break;
                case INSERT:
                    result.delete(delta.startPosition, delta.startPosition + delta.text.length());
                    break;
            }
        }

        return result.toString();
    }

    public static void main(String[] args)
    {
        String original = "My name is suck you, bitcj!";
        String modified = "My name is fuck you, bitch!";

        // Calculate deltas
        List<Delta> deltas = calculateDeltas(original, modified);
        System.out.println(deltas);

        // Apply deltas to revert to original string
        String reverted = applyDeltas(modified, deltas);

        System.out.println("Original String: " + original);
        System.out.println("Modified String: " + modified);
        System.out.println("Reverted String: " + reverted);
    }
}