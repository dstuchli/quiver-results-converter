package org.maestro.cli.main;

import java.util.Arrays;

public class MainCLI {

    public static final String VERSION = "1.0-SNAPSHOT";

    /**
     * Prints out the help
     */
    static void help(int exitNum) {
        System.out.println("Quiver results converter v." + VERSION + "\n");
        System.out.println("Usage:\n");
        
        System.out.println("    convert <arguments>");
        System.out.println("        Arguments being the .csv.xz file and .json file. Both with sender or both with receiver prefix.\n");
        System.out.println("    help");
        System.out.println("    version");

        System.exit(exitNum);
    }

    /**
     * Prints the version of the converter
     */
    private static void version() {
        System.out.println(VERSION);
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("You have to specify the operation");
            help(1);
        }

        String arg = args[0];
        String[] converterArgs = Arrays.copyOfRange(args, 1, args.length);

        QuiverResultsConverter converter;        

        switch(arg) {
            case "help": {
                help(0);
                return;
            }
            case "version": {
                version();
                return;
            }
            case "convert": {
                converter = new QuiverResultsConverter(converterArgs);
                break;

            }
            default: {
                System.out.println("Unknown operation!");
                help(1);
                return;
            }
        }
        System.exit(converter.run());

    }
}