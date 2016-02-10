package ru.pflb.JMeter.Plugin;


import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.*;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import java.io.*;
import java.util.Date;
import java.util.stream.Stream;

import static ru.pflb.JMeter.Plugin.CsvLogWriterGui.*;

//jorphan.jar
//logkit-2.0.jar



public class CsvLogWriter
        extends AbstractListenerElement
        implements SampleListener, Serializable,
        TestStateListener, Remoteable, NoThreadClone {

    private static final String WRITE_BUFFER_LEN_PROPERTY = "ru.pflb.JMeter.Plugin.CLWBufferSize";

    private static final String FILENAME = "filename";
    private static final String ROTATION = "rotation";
    private static int number = 0;
    private static int event_count = 0;
    public static FileWriter fw;
    private final int writeBufferSize = JMeterUtils.getPropDefault(WRITE_BUFFER_LEN_PROPERTY, 1024 * 10);
    public String filepath;
    public CsvLogWriter() throws IOException {
        super();
    }

    /**
     * SampleListener.sampleOccurred
     * @param e
     */

    public static String resultToDelimitedString(SampleEvent event, SampleResult sample, String delimiter, int transactionLevel) {
        StringQuoter text = new StringQuoter(delimiter.charAt(0));
        SampleSaveConfiguration saveConfig = sample.getSaveConfig();
        String i;
        text.append(sample.getTimeStamp());
        text.append(sample.getTime());
        text.append(sample.getSampleLabel());

        String responceCode = sample.getResponseCode();
        text.append(sample.getResponseCode());



        text.append(sample.getResponseMessage());

        text.append(sample.getThreadName());
        text.append(sample.getDataType());
        text.append(sample.isSuccessful());

        i = null;
        AssertionResult[] results = sample.getAssertionResults();
        if(results != null) {
           for(int i1 = 0; i1 < results.length; ++i1) {
              i = results[i1].getFailureMessage();
              if(i != null) {
                break;
              }
           }
        }

        if(i != null) {
           text.append(i);
         } else {
           text.append("");
         }

        text.append(sample.getBytes());
        text.append(sample.getGroupThreads());
        text.append(sample.getAllThreads());
        text.append((Object)sample.getURL());
        text.append(sample.getResultFileName());
        text.append(sample.getLatency());
        text.append(sample.getDataEncodingWithDefault());
        text.append(sample.getSampleCount());
        text.append(sample.getErrorCount());
        text.append(event.getHostname());
        text.append(event.getResult().getIdleTime());
        text.append(sample.getConnectTime());

        if (!((responceCode.length() == 3) && (Integer.parseInt(responceCode) < 400)))
        {
            text.append(sample.getResponseDataAsString().replace("\n", " "));
        }
        else
        {
            text.append("");
        }

        text.append(transactionLevel);
//        for(int var8 = 0; var8 < SampleEvent.getVarCount(); ++var8) {
//            text.append(event.getVarValue(var8));
//        }

        return text.toString();
    }

    private static String quoteDelimiters(String input, char[] specialChars) {
            StringBuilder buffer = new StringBuilder(input.length() + 10);
            char quote = specialChars[1];
            buffer.append(quote);

            for(int i = 0; i < input.length(); ++i) {
                char c = input.charAt(i);
                if(c == quote) {
                    buffer.append(quote);
                }

                buffer.append(c);
            }

            buffer.append(quote);
            return buffer.toString();
    }

    static final class StringQuoter {
        private final StringBuilder sb = new StringBuilder(150);
        private final char[] specials;
        private boolean addDelim;

        public StringQuoter(char delim) {
            this.specials = new char[]{delim, '\"', '\r', '\n'};
            this.addDelim = false;
        }

        private void addDelim() {
            if(this.addDelim) {
                this.sb.append(this.specials[0]);
            } else {
                this.addDelim = true;
            }

        }

        public void append(String s) {
            this.addDelim();
            this.sb.append(quoteDelimiters(s, this.specials));
        }

        public void append(Object obj) {
            this.append(String.valueOf(obj));
        }

        public void append(int i) {
            this.addDelim();
            this.sb.append(i);
        }

        public void append(long l) {
            this.addDelim();
            this.sb.append(l);
        }

        public void append(boolean b) {
            this.addDelim();
            this.sb.append(b);
        }

        public String toString() {
            return this.sb.toString();
        }
    }

    @Override
    public void sampleOccurred(SampleEvent e)
    {
        logSample(e, e.getResult(), 0);
    }

    void logSample(SampleEvent e, SampleResult result, int transactionLevel) {
        StringBuffer sb = new StringBuffer();
        sb.append(resultToDelimitedString(e, result, ";", transactionLevel));
        sb.append("\n");
        try {
            writeEvent(fw,sb);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        SampleResult[] subResults = result.getSubResults();
        if(subResults != null) {
            for (SampleResult subResult: subResults)
                logSample(e, subResult, transactionLevel + 1);
        }
    }

    public String computeFileName(int number)
    {
        filepath = getFilename();
        if (number > 0) {
            int lastPointIndex = filepath.lastIndexOf(".");
            String dir = filepath.substring(0, lastPointIndex);
            filepath = dir + "_" + number + filepath.substring(lastPointIndex);
        }

        return filepath;
    }

    public FileWriter createFile(String filepath) throws IOException {
        File f = new File(filepath);
        String path = f.getAbsolutePath();
        if (f.exists())
        {
           BufferedReader br = new BufferedReader(new FileReader(path));
           String line = br.readLine();

           if (line.equals("timeStamp;elapsed;label;responseCode;responseMessage;threadName;dataType;success;failureMessage;bytes;grpThreads;allThreads;URL;Filename;Latency;Encoding;SampleCount;ErrorCount;Hostname;IdleTime;Connect;\"responseData\";\"transactionLevel\""))
           {
               fw = new FileWriter(filepath, true);
           }
            else {
               fw = new FileWriter(filepath, false);
               fw.write("timeStamp;elapsed;label;responseCode;responseMessage;threadName;dataType;success;failureMessage;bytes;grpThreads;allThreads;URL;Filename;Latency;Encoding;SampleCount;ErrorCount;Hostname;IdleTime;Connect;\"responseData\";\"transactionLevel\"");
               fw.write("\n");
           }
            br.close();
        }
        else
        {
           fw = new FileWriter(filepath, true);
           fw.write("timeStamp;elapsed;label;responseCode;responseMessage;threadName;dataType;success;failureMessage;bytes;grpThreads;allThreads;URL;Filename;Latency;Encoding;SampleCount;ErrorCount;Hostname;IdleTime;Connect;\"responseData\";\"transactionLevel\"");
           fw.write("\n");
        }
        return fw;
    }

    public synchronized void writeEvent(FileWriter fw, StringBuffer sb) throws IOException {
        event_count++;
        CsvLogWriter.fw.write(sb.toString());
        String RotationLimit = getRotation();
        if (RotationLimit.equals("")) {
            RotationLimit = "100000";
        }

        if (event_count >= Integer.parseInt(RotationLimit))
                {
                    event_count = 0;
                    closeFile(fw);
                    number++;
                    String newfilename = computeFileName(number);
                    fw = createFile(newfilename);
                }

        }


    public void closeFile(FileWriter fw) throws IOException {
        fw.close();
    }

    /**
     * SampleListener.sampleStarted
     * @param e
     */
    @Override
    public void sampleStarted(SampleEvent e) {
         }

    /**
     * SampleListener.sampleStopped
     * @param e
     */
    @Override
    public void sampleStopped(SampleEvent e) {
    }

    /**
     * TestStateListener.testStarted
     */


    @Override
    public void testStarted() {
        number = 0;
        filepath = computeFileName(number);
        try {
            fw = createFile(filepath);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * TestStateListener.testStarted
     */
    @Override
    public void testStarted(String host)
    {
    }

    /**
     * TestStateListener.testEnded
     */
    @Override
    public void testEnded()
    {
        try {
            closeFile(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * TestStateListener.testEnded
     */
    @Override
    public void testEnded(String host)
    {
    }

    //Методы для доступа к настройкам

    public void setFilename(String name) {
        setProperty(FILENAME, name);
    }

    public String getFilename() {
        return getPropertyAsString(FILENAME);
    }

    public String getRotation() {
        return getPropertyAsString(ROTATION);
    }

    public void setRotation(String name) {
        setProperty(ROTATION, name);
    }

}