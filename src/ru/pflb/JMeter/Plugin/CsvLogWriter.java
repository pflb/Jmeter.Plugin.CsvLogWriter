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
import java.util.concurrent.ConcurrentLinkedQueue;

import static ru.pflb.JMeter.Plugin.CsvLogWriterGui.*;

//jorphan.jar
//logkit-2.0.jar

public class CsvLogWriter
        extends AbstractListenerElement
        implements SampleListener, Serializable,
        TestStateListener, Remoteable, NoThreadClone {

    private static final Logger log = LoggingManager.getLoggerForClass();
    private final String FILENAME = "filename";
    private int number = 0;

    public FileWriter fw;
    public String filepath;

    public CsvLogWriter() throws IOException {
        super();
    }

    /**
     * Преобразование содытия в строку
     * @param event
     * @param sample
     * @param delimiter
     * @param transactionLevel
     * @return
     */
    public String resultToDelimitedString(SampleEvent event, SampleResult sample, String delimiter, int transactionLevel) {
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
        text.append(sample.getURL());
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

        for(int var8 = 0; var8 < this.varCount; ++var8) {
            text.append(event.getVarValue(var8));
        }

        text.addFinish();

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

        public void addFinish()
        {
            this.sb.append('\n');
        }

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
        String csvString = resultToDelimitedString(e, result, ";", transactionLevel);

        try {

            this.fw.write(csvString);
        } catch (IOException e1) {
            log.error(e1.getMessage());
        }

        SampleResult[] subResults = result.getSubResults();
        if(subResults != null) {
            for (SampleResult subResult: subResults)
                logSample(e, subResult, transactionLevel + 1);
        }
    }

    Integer varCount;

    public FileWriter createFile(String filepath) throws IOException {
        File f = new File(filepath);
        if (f.exists()) {
            String newfilepath = "";
            while (f.exists()) {
                number++;
                int lastPointIndex = filepath.lastIndexOf(".");
                String dir = filepath.substring(0, lastPointIndex);
                newfilepath = dir + "_" + number + filepath.substring(lastPointIndex);
                f = new File(newfilepath);
            }
            filepath = newfilepath;
        }
        fw = new FileWriter(filepath, false);
        log.info("CREATED FILE: " + filepath);
        fw.write("timeStamp;elapsed;label;responseCode;responseMessage;threadName;dataType;success;failureMessage;bytes;grpThreads;allThreads;URL;Filename;Latency;Encoding;SampleCount;ErrorCount;Hostname;IdleTime;Connect;\"responseData\";\"transactionLevel\"");
        this.varCount = SampleEvent.getVarCount();
        for(int resultString = 0; resultString < this.varCount; ++resultString) {
            fw.write(";\"");
            fw.write(SampleEvent.getVarName(resultString));
            fw.write("\"");
        }
        fw.write("\n");
        return fw;
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
        filepath = this.getFilename();
        try {
            this.fw = createFile(filepath);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    /**
     * TestStateListener.testStarted
     */
    @Override
    public void testStarted(String host)
    {}

    /**
     * TestStateListener.testEnded
     */
    @Override
    public void testEnded()
    {
        try {
            closeFile(fw);
        } catch (IOException e) {
            log.error(e.getMessage());
            //e.printStackTrace();
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
}