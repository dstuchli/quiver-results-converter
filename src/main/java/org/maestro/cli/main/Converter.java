package main.java.org.maestro.cli.main;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Converter {


    private File inputFile;
    private File receiver;

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
        final String receiverPath = args[1];

        if (inputPath == null || receiverPath == null) {
            System.err.println("Input file is required.");
            help();
        }

        inputFile = new File(inputPath);
        receiver = new File(receiverPath);

    }

    /**
     * Writes one record into the file
     */
    public void writeRecord() {

    }

    public void convert() throws IOException {

        final BufferedReader br = new BufferedReader(new FileReader(inputFile));
  
        String line; 
        Map<Long, Long> throughput = new HashMap<>();
        long messageCount = 0;
        long currentTimestamp = 0;

        while ((line = br.readLine()) != null) {

            String[] currentLine = line.split(","); 

            if (currentTimestamp == 0) {
                currentTimestamp = Long.parseLong(currentLine[1]);
            }

            if (currentTimestamp < Long.parseLong(currentLine[1])) {
                System.out.println("0," + messageCount + "," + currentTimestamp);
                throughput.put(currentTimestamp, messageCount);

                messageCount = 1;
                currentTimestamp = Long.parseLong(currentLine[1]);

                
            } else {
                messageCount += 1;
            }
            
        }


        System.out.println("0," + messageCount + "," + currentTimestamp);
        throughput.put(currentTimestamp, messageCount); 

        long averageMessageCount = 0;
        long counter = 0;
        Iterator it = throughput.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Long, Long> entry = (Map.Entry)it.next();
            counter++;
            averageMessageCount += entry.getValue();
            
             it.remove(); // avoids a ConcurrentModificationException
        }
        
        System.out.println("------------------");
        System.out.println("The average message count per timestamp: " + (averageMessageCount / counter));

        br.close();

    }

    public int run() throws IOException {
        convert();
        calculateLatency();
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

    public void calculateLatency() throws IOException {
        long latency = 0;

        final BufferedReader br = new BufferedReader(new FileReader(receiver));

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

    
}