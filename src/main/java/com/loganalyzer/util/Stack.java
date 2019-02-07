package com.loganalyzer.util;

public class Stack
{
    String a[]=new String[100];
    int top=-1;

    void push(String c)
    {
        try
        {
            a[++top]= c;
        }
        catch(StringIndexOutOfBoundsException e)
        {
            System.out.println("Stack full , no room to push , size=100");
            System.exit(0);
        }
    }

    String pop()
    {
        return a[top--];
    }

    boolean isEmpty()
    {
        return (top==-1)?true:false;
    }

    String peek()
    {
        return a[top];
    }

}