package ru.pflb.JMeter.Plugin;


import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.*;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

//jorphan.jar
//logkit-2.0.jar

public class CsvLogWriter
        extends AbstractListenerElement
        implements SampleListener, Serializable,
        TestStateListener, Remoteable, NoThreadClone {

    private static final Logger log = LoggingManager.getLoggerForClass();
    private final String FILENAME = "filename";
    private int number = 0;

    public static FileWriter fw;
    public String filepath;

    // Lock used to guard static mutable variables
    private static final Object LOCK = new Object();

    //@GuardedBy("LOCK")
    private static int instanceCount; // Keep track of how many instances are active
    /**
     * Shutdown Hook that ensures PrintWriter is flushed is CTRL+C or kill is called during a test
     */
    //@GuardedBy("LOCK")
    private static Thread shutdownHook;

    private static final class ShutdownHook implements Runnable {

        @Override
        public void run() {
            log.info("Shutdown hook started");
            synchronized (LOCK) {
                try {
                    fw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            log.info("Shutdown hook ended");
        }
    }
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
        if(input == null)
            return "";

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

    int varCount = 0;

    public FileWriter createFile(String filepath) throws IOException {

        File pdir = new File(filepath).getParentFile();
        if (pdir != null) {
            // returns false if directory already exists, so need to check again
            if(pdir.mkdirs()){
                log.info("Folder "+pdir.getAbsolutePath()+" was created");
            } // else if might have been created by another process so not a problem
            if (!pdir.exists()){
                log.warn("Error creating directories for "+pdir.toString());
            }
        }

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
            log.fatalError(e.getMessage(), e);
        }
    }

    /**
     * TestStateListener.testStarted
     */
    @Override
    public void testStarted(String host)
    {synchronized(LOCK){
        if (instanceCount == 0) { // Only add the hook once
            shutdownHook = new Thread(new ShutdownHook());
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
        instanceCount++;
        try {
            createFile(filepath);
            /*if (getVisualizer() != null) {
                this.isStats = getVisualizer().isStats();
            }*/
        } catch (Exception e) {
            log.error("", e);
        }
    }
        /*inTest = true;

        if(summariser != null) {
            summariser.testStarted(host);
        }*/}

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
        synchronized(LOCK){
            instanceCount--;
            if (instanceCount <= 0) {
                // No need for the hook now
                // Bug 57088 - prevent (im?)possible NPE
                if (shutdownHook != null) {
                    Runtime.getRuntime().removeShutdownHook(shutdownHook);
                } else {
                    log.warn("Should not happen: shutdownHook==null, instanceCount=" + instanceCount);
                }
                /*finalizeFileOutput();
                inTest = false;*/
            }
        }
    }
    //Методы для доступа к настройкам

    public void setFilename(String name) {
        setProperty(FILENAME, name);
    }

    public String getFilename() {
        return getPropertyAsString(FILENAME);
    }

}
