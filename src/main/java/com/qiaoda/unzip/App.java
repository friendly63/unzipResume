package com.qiaoda.unzip;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        String aa = "group1/M00/25/57/wKgGm1ZEC5eAf_ttAAAMPqE3s5E82.html";
        String a1 = aa.substring(aa.indexOf("M00/"));
        String a2 = aa.substring(0,aa.indexOf("M00/")-1);
        System.out.println(a1);
        System.out.println(a2);
    }
}
