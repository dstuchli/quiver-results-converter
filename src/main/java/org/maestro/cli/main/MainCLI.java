package main.java.org.maestro.cli.main;

import java.io.IOException;
import java.util.Arrays;

public class MainCLI {

    public static final String VERSION = "0.0.1";

    /**
     * Prints out the help
     */
    private static void help() {
        System.out.println("Quiver results converter\n");
        System.out.println("Usage: qres <argument>\n");

        System.out.println("Arguments:");
        System.out.println("    convert\n");
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

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.out.println("You have to specify the operation");
            help();
        }

        String arg = args[0];
        String[] converterArgs = Arrays.copyOfRange(args, 1, args.length);

        Converter converter;        

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
                converter = new Converter(converterArgs);
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