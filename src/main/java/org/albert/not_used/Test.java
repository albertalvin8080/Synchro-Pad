package org.albert.not_used;

public class Test
{
//    public static void main(String[] args)
//    {
//        String str1 = "a, hoje eu sou hoje.";
//        String str2 = "ops";
//        int offset = 10;
//        int length = 0;
//
//        final StringBuilder stringBuilder = new StringBuilder(str1);
//        stringBuilder.insert(offset, str2);
//        System.out.println(stringBuilder.toString());
//    }

    public static void main(String[] args)
    {
        String str1 = "a, hoje euops sou hoje.";
        int offset = 10;
        int length = 3;

        final StringBuilder stringBuilder = new StringBuilder(str1);
        stringBuilder.delete(offset, offset+length);
        System.out.println(stringBuilder.toString());
    }
}