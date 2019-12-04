package main.java.org.maestro.cli.main;

import java.io.File;

public class Converter {


    private File inputFile;

    /**
     * Prints out help for converter
     */
    private static void help() {
        System.out.println("Usage: qres convert INPUT OUTPUT\n");
        System.out.println("The convert utility converts the data that are stored in the file named by the INPUT operand and stores them into the file named by the OUTPUT operand.\n");


    }

	public Converter(String[] args) {
        parseCommand(args);
    }
    
    private void parseCommand(String[] args) {


    }

    public int run() {
        help();
        return 0;
    }

    
}