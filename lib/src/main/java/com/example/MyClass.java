package com.example;

import java.util.Scanner;

public class MyClass {

    public static boolean isPrime ( int start, int number )
    {
        if (number < 2)
        {
            return false;
        }
        start++;
        if (start > number / 2)
        {
            return true;
        }
        if (number % start == 0)
        {
            return false;
        }
        return isPrime (start, number);
    }

    private static int what ( int number )
    {
        int code = 0;
        for ( int i = 2; i < number; i++ )
        {
            for ( int j = number -2; j >= i; j-- )
            {
                if (isPrime (1, i) && isPrime (1, j) && i + j == number)
                {
                    System.out.println (i + " + " + j + " = " + number);
                    code++;
                }
            }
        }
        return code;
    }

    public static void main ( String[] args )
    {
        Scanner scanner = new Scanner (System.in);
        System.out.print ("请输入一个小于1000的整数: ");
        String line = scanner.nextLine ().trim ();
        if ("".equals (line))
        {
            scanner.close ();
        }
        int number = -1;
        try
        {
            number = Integer.parseInt (line);
        }
        catch (NumberFormatException e)
        {
            System.out.println ("请输入整数");
        }
        int code = what (number);
        System.out.println ("结果是:" + code);
    }
}
