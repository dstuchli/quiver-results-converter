package main.java.org.maestro.cli.main;

import java.io.*;

public class Converter {


    private File inputFile;

    /**
     * Prints out help for converter
     */
    private static void help() {
        System.out.println("Usage: qres convert INPUT\n");
        System.out.println("The convert utility converts the data that are stored in the file named by the INPUT operand.\n");
        System.exit(0);

    }

    /**
     * Class constructor for converter
     * @param args consists of INPUT and OUTPUT strings
     */
	public Converter(final String[] args) {
        parseCommand(args);
    }

    /**
     * Parses the arguments for the converter
     * 
     * @param args consists of INPUT and OUTPUT strings
     */
    private void parseCommand(final String[] args) {


        if (args[0].equals("help")) {
            help();
        }

        final String inputPath = args[0];

        if (inputPath == null) {
            System.err.println("Input file is required.");
            help();
        }

        inputFile = new File(inputPath);

    }

    /**
     * Writes one record into the file
     */
    public void writeRecord() {

    }

    public void convert() throws IOException {
        // We need to provide file path as the parameter:
        // double backquote is to avoid compiler interpret words
        // like \test as \t (ie. as a escape sequence)

        final BufferedReader br = new BufferedReader(new FileReader(inputFile));
  
        String st; 
        while ((st = br.readLine()) != null)
            System.out.println(st); 

        br.close();

    }

    public int run() throws IOException {
        convert();
        return 0;
    }

    /**
     * Gets output filename according to the input file
     * @return name of the output file
     * @throws Exception when the input file has invalid name
     */
    public String getOutputFileName() throws Exception {
        String filename;
        if (inputFile.getName().equals("sender-transfers.csv.gz")) {
            filename = "sender.dat";
        }
        else {
            if (inputFile.getName().equals("receiver-transfers.csv.gz")) {
                filename = "receiver.dat";
            }
            else {
                throw new Exception("Invalid file name!");
            }
        }
        return filename;
    }

    
}