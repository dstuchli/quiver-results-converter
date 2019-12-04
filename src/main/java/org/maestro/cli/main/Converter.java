package main.java.org.maestro.cli.main;

import java.io.*;
// import java.util.HashMap;
// import java.util.Map;

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

        final BufferedReader br = new BufferedReader(new FileReader(inputFile));
  
        String line; 
        // Map<String, Long> newLine = new HashMap<>();
        long messageCount = 0;
        long currentTimestamp = 0;

        while ((line = br.readLine()) != null) {

            String[] currentLine = line.split(","); 

            if (currentTimestamp == 0) {
                currentTimestamp = Long.parseLong(currentLine[1]);
            }

            if (currentTimestamp < Long.parseLong(currentLine[1])) {
                System.out.println("0," + messageCount + "," + currentTimestamp);

                messageCount = 1;
                currentTimestamp = Long.parseLong(currentLine[1]);

                
            } else {
                messageCount += 1;
            }
            
        }

        System.out.println("0," + messageCount + "," + currentTimestamp);

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