package org.maestro.cli.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.maestro.common.io.data.common.FileHeader;
import org.maestro.common.io.data.common.exceptions.InvalidRecordException;
import org.maestro.common.io.data.writers.*;
import org.maestro.plotter.rate.*;
import org.apache.commons.lang.StringUtils;

public class QuiverResultsConverter {

    private File dataInputFile;
    private File jsonFile;
    private static int isSender = 0;

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
    public QuiverResultsConverter(final String[] args) {
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

        if (args.length < 2) {
            System.err.println("Files .xz and .json are needed.");
            help();
        }

        String dataInputPath = args[0];
        String jsonFilePath = "";

        if (dataInputPath.contains("json")) {
            jsonFilePath = dataInputPath;
            dataInputPath = args[1];
        } else {
            jsonFilePath = args[1];
        }

        Process proc;
        try {
            // had a problem with permission denied had to change permissions manually
            proc = new ProcessBuilder("src/main/scripts/unzipXZ.sh", dataInputPath).start();
            proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataInputFile = new File(dataInputPath.substring(0, dataInputPath.length() - 3));
        jsonFile = new File(jsonFilePath);

        if (dataInputPath.contains("sender")) {
            isSender = 1;
        }

    }

    /**
     * Gets output filename according to the input file
     * 
     * @return name of the output file
     * @throws FileNotFoundException when the input file has invalid name
     */
    public String getOutputFileName() throws FileNotFoundException {
        String filename;
        if (dataInputFile.getName().equals("sender-transfers.csv")) {
            filename = "sender.dat";
        } else {
            if (dataInputFile.getName().equals("receiver-transfers.csv")) {
                filename = "receiver.dat";
            } else {
                throw new FileNotFoundException();
            }
        }
        return filename;
    }

    /**
     * Writes one record into the file
     */
    public void writeRecord(BinaryRateWriter brw, RateRecord record) {
        try {
            brw.write(0, record.getCount(), TimeUnit.SECONDS.toMicros(record.getTimestamp().getEpochSecond()));
        } catch (IOException e) {
            System.err.println("I/O error while trying to convert the rate record: " + e.getMessage());
            e.printStackTrace();
        } catch (InvalidRecordException e) {
            System.out.println(TimeUnit.SECONDS.toMicros(record.getTimestamp().getEpochSecond()));
            System.err.println("Invalid record for entry for: " + record);
        }
    }

    // private Instant processTimestamp(String timestamp) {
    //     final int indexLen = 19;

    //     try {
    //         String period = timestamp.substring(0, indexLen);
    //         RateRecord rateRecord = cache.get(period);

    //         if (rateRecord == null) {
    //             final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //             Instant ataDate = formatter.parse(ata).toInstant();

    //             rateRecord = new RateRecord(ataDate, 1);
    //             cache.put(period, rateRecord);
    //         } else {
    //             long i = rateRecord.getCount();

    //             i++;
    //             assert i >= 0;
    //             rateRecord.setCount(i);
    //         }
    //     } catch (Exception e) {
    //         logger.warn("Error parsing record with values ata {}: {}", ata, e.getMessage());
    //     }
    // }

    /**
     * Converts the data in sender-tranfers.csv to correct Maestro Data Format and
     * calculates average throughput
     * 
     * @throws IOException
     * @throws NumberFormatException
     */
    public RateData convertResults() throws NumberFormatException, IOException {

        BufferedReader br = new BufferedReader(new FileReader(dataInputFile));

        String line;

        long messageCount = 0;
        long timestamp = 0;
        RateData data = new RateData();
        RateRecord record;

        while ((line = br.readLine()) != null) {

            String[] currentLine = line.split(",");
            long currentTimestamp = 0;
            if (isSender == 1) {
                currentTimestamp = Long.parseLong(currentLine[1]);
            } else {
                currentTimestamp = Long.parseLong(currentLine[2]);
            }

            if (timestamp == 0) {
                timestamp = currentTimestamp;
            }

            if (timestamp < currentTimestamp) {

                System.out.println("ORIGINAL: " + timestamp);
                System.out.println("EpochMilli: " + Instant.ofEpochMilli(timestamp));
                record = new RateRecord(Instant.ofEpochMilli(timestamp), messageCount);
                data.add(record);

                messageCount = 1;
                timestamp = currentTimestamp;

            } else {
                messageCount += 1;
            }

        }

        // last record
        record = new RateRecord(Instant.ofEpochMilli(timestamp), messageCount);
        data.add(record);

        br.close();

        return data;

    }

    private BinaryRateWriter getWriter() throws IOException {
        String outputFileName = getOutputFileName();


        File output = new File(dataInputFile.getParent(), outputFileName);

        BinaryRateWriter brw;
        if (outputFileName.equals("sender.dat")) {
            brw = new BinaryRateWriter(output, FileHeader.WRITER_DEFAULT_SENDER);
        }
        else {
            brw = new BinaryRateWriter(output, FileHeader.WRITER_DEFAULT_RECEIVER);
        }

        return brw;
    }

    public int run() {

        try (BinaryRateWriter brw = getWriter()) {
            final RateData rateData = convertResults();
            final Set<RateRecord> records = rateData.getRecordSet();

            records.forEach(record -> writeRecord(brw, record));

            createTestProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // createSystemProperties();

        if (isSender == 0) {
            createHDRLatencyFile();
        }
        return 0;
    }

    private void createTestProperties() throws IOException {

        ArrayList<String> properties = readTestProperties();

        BufferedWriter bw = new BufferedWriter(new FileWriter("test.properties"));
        bw.write("#maestro-quiver-agent\n");
        bw.append("fcl=0\n");
        bw.append("parallelCount=1\n");
        bw.append("apiName=" + properties.get(0) + "\n");
        bw.append("duration=" + properties.get(2) + "\n");
        bw.append("limitDestinations=0\n");
        bw.append("durationType=count\n");
        bw.append("rate=" + properties.get(4) + "\n");
        // TODO find out if quiver defines variableSize
        bw.append("variableSize=???\n");
        bw.append("apiVersion=1.1\n");
        bw.append("brokerUri=" + properties.get(1) + "\n");
        bw.append("messageSize=" + properties.get(3) + "\n");
        bw.append("protocol=AMQP");
        bw.close();
    }

    private ArrayList<String> readTestProperties() throws IOException {
        ArrayList<String> testProperties = new ArrayList<>();
        
        final BufferedReader br = new BufferedReader(new FileReader(jsonFile));

        String line;

        while ((line = br.readLine()) != null) {
            if (line.contains("url")) {
                String[] currentLine = line.split("\":");
                testProperties.add(StringUtils.substringBetween(currentLine[1], "\"", "\""));
            }

            String[] currentLine = line.split(":");
            if (line.contains("impl")) {
                testProperties.add(StringUtils.substringBetween(currentLine[1], "\"", "\""));

            } else if (line.contains("count")) {
                testProperties.add(StringUtils.substringBetween(currentLine[1], " ", ","));

            } else if (line.contains("body_size")) {
                testProperties.add(StringUtils.substringBetween(currentLine[1], " ", ","));

            } else if (line.contains("message_rate")) {
                testProperties.add(StringUtils.substringBetween(currentLine[1], " ", ","));

            }
        }

        br.close();

        return testProperties;
    }

    // private void createSystemProperties() throws IOException {
    //     // TODO
    // }

    private void createHDRLatencyFile() {
        // TODO
    }

    
}