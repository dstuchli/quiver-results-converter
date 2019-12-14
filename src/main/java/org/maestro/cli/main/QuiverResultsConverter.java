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

import org.maestro.common.evaluators.HardLatencyEvaluator;
import org.maestro.common.evaluators.LatencyEvaluator;
import org.maestro.common.io.data.common.FileHeader;
import org.maestro.common.io.data.common.exceptions.InvalidRecordException;
import org.maestro.common.io.data.writers.BinaryRateWriter;
import org.maestro.common.io.data.writers.LatencyWriter;
import org.maestro.plotter.rate.RateData;
import org.maestro.plotter.rate.RateRecord;
import org.apache.commons.lang3.StringUtils;
import org.HdrHistogram.Histogram;

public class QuiverResultsConverter {

    private File dataInputFile;
    private File jsonFile;
    private static int isSender = 0;
    private static final long MAX_LATENCY = 2000000000;
    private static String baseDir;

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
     * @param args represents paths to files
     * 
     *             TODO - Improve the parser to be more resilient
     */
    private void parseCommand(final String[] args) {

        if (args[0].equals("help")) {
            MainCLI.help(0);
        }

        if (args.length < 2) {
            System.err.println("Files .csv.xz and .json are needed.");
            MainCLI.help(1);
        }

        String dataInputPath = args[0];
        String jsonFilePath = "";

        if (dataInputPath.contains("json")) {
            jsonFilePath = dataInputPath;
            dataInputPath = args[1];
        } else {
            jsonFilePath = args[1];
        }

        baseDir = dataInputPath.substring(0, dataInputPath.lastIndexOf("/"));

        Process proc;
        try {
            proc = new ProcessBuilder("xz", "-d", dataInputPath).start();
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
     * Gets output filename according to the data file
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
     * @param brw writer used to write the records to binary file
     * @param record record to be written into the output file
     */
    public void writeRecord(BinaryRateWriter brw, RateRecord record) {
        try {
            brw.write(0, record.getCount(), TimeUnit.MILLISECONDS.toMicros(record.getTimestamp().toEpochMilli()));
        } catch (IOException e) {
            System.err.println("I/O error while trying to convert the rate record: " + e.getMessage());
            e.printStackTrace();
        } catch (InvalidRecordException e) {
            System.out.println(TimeUnit.MILLISECONDS.toMicros(record.getTimestamp().toEpochMilli()));
            System.err.println("Invalid record for entry for: " + e.getMessage());
        }
    }

    /**
     * Converts the data in dataInputFile to correct Maestro Data Format
     * 
     * @return RateData including all records already converted to right format
     * @throws IOException when reading of the dataInputFile was unsuccessful
     */
    public RateData convertResults() throws IOException {

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
                currentTimestamp = Long.parseLong(currentLine[1]) / 1000;
            } else {
                currentTimestamp = Long.parseLong(currentLine[2]) / 1000;
            }

            if (timestamp == 0) {
                timestamp = currentTimestamp;
            }

            if (timestamp < currentTimestamp) {

                record = new RateRecord(Instant.ofEpochMilli(timestamp * 1000), messageCount);
                data.add(record);

                messageCount = 1;
                timestamp = currentTimestamp;

            } else {
                messageCount += 1;
            }

        }

        record = new RateRecord(Instant.ofEpochMilli(timestamp * 1000), messageCount);
        data.add(record);

        br.close();

        return data;

    }

    /**
     * Creates correct writer based on dataInputFile name
     * 
     * @return BinaryRateWriter with correct File header
     * @throws IOException when writing to the output file was unsuccessful
     */
    public BinaryRateWriter getWriter() throws IOException {
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

    /**
     * Reads test summary from quiver
     * @return list of all data needed for test.properties file
     * @throws IOException when reading of the file was unsuccessful
     */
    public ArrayList<String> readTestProperties() throws IOException {
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

    /**
     * Creates test.properties file for sender or receiver
     * @throws IOException when writing to the file was unsuccessful
     */
    public void createTestProperties() throws IOException {

        ArrayList<String> properties = readTestProperties();

        BufferedWriter bw;
        String path = jsonFile.getAbsolutePath();
        if (isSender == 1) {
            bw = new BufferedWriter(new FileWriter(path.replace(jsonFile.getName(), "test.properties")));
        } else {
            bw = new BufferedWriter(new FileWriter(path.replace(jsonFile.getName(), "test.properties-rec")));
        }
        bw.write("#maestro-quiver-agent\n");
        bw.append("fcl=0\n");
        bw.append("parallelCount=1\n");
        bw.append("apiName=" + properties.get(0) + "\n");
        bw.append("duration=" + properties.get(2) + "\n");
        bw.append("limitDestinations=0\n");
        bw.append("durationType=count\n");
        bw.append("rate=" + properties.get(4) + "\n");
        bw.append("variableSize=false\n");
        bw.append("apiVersion=1.1\n");
        bw.append("brokerUri=" + properties.get(1) + "\n");
        bw.append("messageSize=" + properties.get(3) + "\n");
        bw.append("protocol=AMQP");
        bw.close();
    }

    /**
     * creates latency file for receiver
     * @throws IOException
     */
    public void createHDRLatencyFile() throws IOException {
        Histogram histogram = new Histogram(1);
        LatencyEvaluator latencyEvaluator = new HardLatencyEvaluator(MAX_LATENCY);
        long startedEpochMillis = System.currentTimeMillis();

        BufferedReader br = new BufferedReader(new FileReader(dataInputFile));

        String line;
        long latency = 0;
        while((line = br.readLine()) != null) {
            String[] currentLine = line.split(",");
            latency = Long.parseLong(currentLine[2]) - Long.parseLong(currentLine[1]);
            histogram.recordValue(latency);

        }
        latencyEvaluator.record(histogram);

        LatencyWriter lw = new LatencyWriter(new File(baseDir, "receiverd-latency.hdr"));
        lw.outputLegend(startedEpochMillis);
        lw.outputIntervalHistogram(histogram);

        lw.close();

        br.close();

    }

    /**
     * Wrapper for all methodes. Runs the utility itself.
     * @return integer representing success of the run
     */
    public int run() {

        RateData rateData;
        try (BinaryRateWriter brw = getWriter()) {
            rateData = convertResults();
            final Set<RateRecord> records = rateData.getRecordSet();

            records.forEach(record -> writeRecord(brw, record));

            createTestProperties();
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }

        if (isSender == 0) {
            try {
                createHDRLatencyFile();
            } catch (IOException e) {
                e.printStackTrace();
                return 1;
            }
        }
        return 0;
    }

    // private void createSystemProperties() throws IOException {
    //     // TODO
    // }

}