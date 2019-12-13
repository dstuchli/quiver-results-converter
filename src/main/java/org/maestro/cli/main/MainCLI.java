package org.maestro.cli.main;

import java.util.Arrays;

public class MainCLI {

    public static final String VERSION = "1.0-SNAPSHOT";

    /**
     * Prints out the help
     */
    private static void help() {
        System.out.println("Quiver results converter v." + VERSION + "\n");
        System.out.println("Usage: <Action>\n");

        System.out.println("Action:");
        System.out.println("    convert <arguments>\n");
        System.out.println("        Arguments being the .csv.xz file and .json file from sender or receiver");
        System.out.println("    help");
        System.out.println("    version");

        System.exit(0);
    }

    /**
     * Prints the version of the converter
     */
    private static void version() {
        System.out.println(VERSION);
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("You have to specify the operation");
            help();
        }

        String arg = args[0];
        String[] converterArgs = Arrays.copyOfRange(args, 1, args.length);

        QuiverResultsConverter converter;        

        switch(arg) {
            case "help": {
                help();
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
                help();
                return;
            }
        }
        System.exit(converter.run());

    }
}