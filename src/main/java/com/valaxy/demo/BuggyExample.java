package com.valaxy.demo;

public class BuggyExample {

    public void riskyMethod(String input) {
        // Bug: possible NullPointerException
        System.out.println(input.toLowerCase());
    }

    public void swallowException() {
        try {
            int x = Integer.parseInt("abc");
        } catch (Exception e) {
            // Bug: empty catch block
        }
    }

    public void hardcodedPassword() {
        String password = "admin123"; // Security hotspot
        System.out.println("Password is " + password);
    }
}
