package com.valaxy.demo;

import java.io.FileInputStream;
import java.io.IOException;

public class BuggyExample {

    // Null pointer risk
    public String riskyMethod(String input) {
        return input.toLowerCase(); // Bug: input could be null
    }

    // Empty catch block
    public void swallowException() {
        try {
            int x = Integer.parseInt("abc");
        } catch (Exception e) {
            // Bug: swallowed exception
        }
    }

    // Resource leak
    public void readFile() throws IOException {
        FileInputStream fis = new FileInputStream("file.txt");
        // Bug: fis never closed
    }

    // Hardcoded password (security hotspot)
    public void hardcodedPassword() {
        String password = "admin123"; // Security issue
        System.out.println("Password is " + password);
    }
}
