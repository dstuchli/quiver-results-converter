package org.maestro.cli.main;

import java.io.*;

// TODO - the data are stored in sender/receiver.dat as binary data

public class Converter {

    private File inputFile;
    private static final String SENDERFILE = "sender-transfers.csv";
    private static final String RECEIVERFILE = "receiver-transfers.csv";

    private static final String FORMAT_NAME = "maestro";
    private static final String FORMAT_VERSION = "1";
    private static final String MAESTRO_VERSION = "155";
    private static int ROLE = 0;

    /**
     * Prints out help for converter
     */
    private static void help() {
        System.out.println("Usage: qres convert INPUT\n");
        System.out.println(
                "The convert utility converts the data that are stored in the file named by the INPUT operand.\n");
        System.exit(0);

    }

    /**
     * Class constructor for converter
     * 
     * @param args consists of INPUT strings for sender and receiver files
     */
    public Converter(final String[] args) {
        parseCommand(args);
    }

    /**
     * Parses the arguments for the converter
     * 
     * @param args represents path to file
     * 
     *             TODO - Improve the parser to be more resilient
     */
    private void parseCommand(final String[] args) {

        if (args[0].equals("help")) {
            help();
        }

        final String inputPath = args[0];

        if (inputPath == null) {
            System.err.println("File as an argument is required.");
            help();
        }

        Process proc;
        try {
            // had a problem with permission denied had to change permissions manually
            proc = new ProcessBuilder("src/main/scripts/unzipXZ.sh", inputPath).start();
            // String[] cmd = { "sh", "src/main/scripts/unzipXZ.sh " + inputPath};
            // proc = Runtime.getRuntime().exec(cmd);
            proc.waitFor();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        inputFile = new File(inputPath.substring(0, inputPath.length() - 3));

    }

    /**
     * Gets output filename according to the input file
     * 
     * @return name of the output file
     * @throws Exception when the input file has invalid name
     */
    public String getOutputFileName() throws Exception {
        String filename;
        if (inputFile.getName().equals("sender-transfers.csv")) {
            filename = "sender.dat";
            ROLE = 1;
        } else {
            if (inputFile.getName().equals("receiver-transfers.csv")) {
                filename = "receiver.dat";
                ROLE = 2;
            } else {
                throw new Exception("Invalid file name!");
            }
        }
        return filename;
    }

    /**
     * Writes one record into the file
     */
    public void writeRecord() {
        // TODO
    }

    /**
     * Converts the data in sender-tranfers.csv to correct Maestro Data Format and
     * calculates average throughput
     * 
     * @throws Exception
     */
    public void createSenderFile() throws Exception {

        String outputFile = getOutputFileName();

        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

        bw.write(getMaestroHeader());

        String line;

        // variables for avrage throughput calculation
        long totalTimestampCount = 0;
        long totalMessageCount = 0;

        long messageCount = 0;
        long currentTimestamp = 0;

        while ((line = br.readLine()) != null) {
            
            String[] currentLine = line.split(",");
            
            if (currentTimestamp == 0) {
                currentTimestamp = Long.parseLong(currentLine[1]);
            }
            
            if (currentTimestamp < Long.parseLong(currentLine[1])) {
                bw.append("0," + messageCount + "," + currentTimestamp + "\n");

                totalMessageCount += messageCount;
                totalTimestampCount++;
                
                messageCount = 1;
                currentTimestamp = Long.parseLong(currentLine[1]);
                
            } else {
                messageCount += 1;
            }
            
        }

        // last report
        bw.append("0," + messageCount + "," + currentTimestamp + "\n");

        System.out.println("The average message count per timestamp: " + (totalMessageCount / totalTimestampCount));

        br.close();
        bw.close();

    }

    private String getMaestroHeader() {
        String header = FORMAT_NAME + "," + FORMAT_VERSION + "," + MAESTRO_VERSION + "," + ROLE + "\n";
        return header;
    }

    /**
     * Calculates average latency based on information from receiver-transfers.csv
     * 
     * @throws IOException when there was an error during reading the file
     */
    public void createReceiverFile() throws IOException {
        long latency = 0;

        final BufferedReader br = new BufferedReader(new FileReader(inputFile));

        String receiverLine;
        int counter = 0;

        while ((receiverLine = br.readLine()) != null) {
            String[] currentReceiverLine = receiverLine.split(",");
            
            latency += Long.parseLong(currentReceiverLine[2]) - Long.parseLong(currentReceiverLine[1]);
            counter++;
        }

        System.out.println("Average latency is: " + (latency / counter));

        br.close();
    }

    public int run() throws Exception {

        if (inputFile.getName().equals(SENDERFILE)) {
            createSenderFile();
            createTestProperties();
            createSystemProperties();
        } else {
            if (inputFile.getName().equals(RECEIVERFILE)) {
                createReceiverFile();
                createTestProperties();
                createSystemProperties();
                createHDRLatencyFile();
            }
        }
        return 0;
    }

    private void createTestProperties() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("test.properties"));
        bw.write("#maestro-quiver-agent");

        bw.close();
    }

    private void createSystemProperties() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("system.properties"));
        bw.write("#maestro-quiver-agent");

        bw.close();

    }

    private void createHDRLatencyFile() {
        // TODO
    }
    
}