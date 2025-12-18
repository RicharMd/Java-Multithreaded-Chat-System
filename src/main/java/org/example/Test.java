package org.example;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.IOException;


public class Test {
    static Terminal terminal;

    public static void main(String[] args) {
        String line = "[GROUP]|Client_number:1|mike:1(23:13:22)";
        String line_2="1:::";
        if (!(line.substring(line.indexOf(":", line.indexOf(":")+1) + 1,line.length()-10).equals(" "+1+" "))) {
            System.out.println(line.substring(line.indexOf(":",line.indexOf(":")+1)+1,line.length()-10));





    }}}

