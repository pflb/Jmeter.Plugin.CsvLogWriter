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

    private final String checkAdditionalParams = "checkAdditionalParams";
    private final String checkResponseData = "checkResponseData";
    private final String checkUserVariables = "checkUserVariables";

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

        text.append(sample.getTimeStamp()); // timeStamp - 1
        text.append(sample.getTime());  // elapsedTime - 2
        text.append(sample.getSampleLabel()); // label - 3

        text.append(sample.getResponseCode()); // responseCode - 4
        text.append(sample.getResponseMessage()); // responseMessage - 5
        text.append(sample.getThreadName()); // threadName - duplicate  event.threadGroup - 6
        text.append(sample.getDataType()); // dataType - 7
        text.append(sample.isSuccessful()); // success - 8

        // TODO: исправить логирование AssertionResults - заносить все неуспешные результаты в лог
        String assertionResults = null;
        StringBuilder assertionResultsBuilder = null;
        AssertionResult[] results = sample.getAssertionResults();
        if (results != null) {
            for (AssertionResult result : results) {
                if (result.isError() || result.isFailure()) {
                    if (assertionResultsBuilder == null) {
                        assertionResultsBuilder = new StringBuilder(150);
                        assertionResultsBuilder.append('[');
                        assertionResultsBuilder.append('{');
                    } else {
                        assertionResultsBuilder.append(",{");
                    }
                    assertionResultsBuilder.append("\"name\":\"");
                    assertionResultsBuilder.append(result.getName());
                    assertionResultsBuilder.append("\", \"failureMessage\":\"");
                    assertionResultsBuilder.append(result.getFailureMessage());
                    assertionResultsBuilder.append("\", \"isError\":\"");
                    assertionResultsBuilder.append(result.isError());
                    assertionResultsBuilder.append("\", \"isFailure\":\"");
                    assertionResultsBuilder.append(result.isFailure());
                    assertionResultsBuilder.append("\"");

                    assertionResultsBuilder.append("}");
                }
            }
            if (assertionResultsBuilder != null)
                assertionResultsBuilder.append(']');
        }

        if (assertionResultsBuilder != null) {
            text.append(assertionResultsBuilder.toString()); // assertionResults - 9
        } else {
            text.append("");
        }

        text.append(sample.getBytes()); // размер ответа (responseData.length, headersSize, bodySize - 10
        text.append(sample.getGroupThreads()); // groupThreads - 11
        text.append(sample.getAllThreads()); // allThreads - 12
        text.append(sample.getURL()); // location - 13
        text.append(sample.getResultFileName()); // resultFileName - 14
        text.append(sample.getLatency()); // latency - 15
        text.append(sample.getDataEncodingWithDefault()); // dataEncoding - 16
        text.append(sample.getSampleCount()); // sampleCount - 17
        text.append(sample.getErrorCount()); // 0 или 1 (success) - 18
        text.append(event.getHostname()); // hostname - 19
        text.append(event.getResult().getIdleTime()); // idleTime - 20
        text.append(sample.getConnectTime()); // connectTime - 21

        if (getCheckAdditionalParams()) {

            text.append(sample.getHeadersSize()); // headersSize - 23
            text.append(sample.getBodySize()); // bodySize - 24
            text.append(sample.getContentType()); // contentType - 25
            text.append(sample.getEndTime()); // endTime - 26
            text.append(sample.isMonitor()); // isMonitor - 27

            StringBuilder sbThreadNameLabel = new StringBuilder(sample.getThreadName().length() + sample.getSampleLabel().length() + 1);
            sbThreadNameLabel.append(sample.getThreadName());
            sbThreadNameLabel.append(':');
            sbThreadNameLabel.append(sample.getSampleLabel());
            text.append(sbThreadNameLabel.toString()); // threadName + label - 28

            SampleResult parent = sample.getParent();
            if (parent != null) {
                StringBuilder sbThreadNameLabelParent = new StringBuilder(parent.getThreadName().length() + parent.getSampleLabel().length() + 1);
                sbThreadNameLabelParent.append(parent.getThreadName());
                sbThreadNameLabelParent.append(':');
                sbThreadNameLabelParent.append(parent.getSampleLabel());
                text.append(sbThreadNameLabelParent.toString()); // parent - 29
            } else {
                text.append("");
            }
            text.append(sample.getStartTime()); // startTime - 30
            text.append(sample.isStopTest()); // stopTest - 31
            text.append(sample.isStopTestNow()); // stopTestNow - 32
            text.append(sample.isStopThread()); // stopThread - 33
            text.append(sample.isStartNextThreadLoop()); // startNextThreadLoop - 34
            //text.append(event.getHostname()); // hostname - 35
            text.append(event.isTransactionSampleEvent()); // isTransactionSampleEvent - 36

            text.append(transactionLevel); // transactionLevel - 37
        }
        if (getCheckResponseData()) {
            if (!sample.isSuccessful()) {
                text.append(sample.getResponseDataAsString()); // responseDataAsString - 38
                text.append(sample.getRequestHeaders()); // requestHeaders - 39
                text.append(sample.getResponseData()); // responseData - 40
                text.append(sample.getResponseHeaders()); // responseHeaders - 41
                // TODO: проверить
                //text.append(sample.getSamplerData()); // samplerData - 42
            } else {
                text.append("");
                text.append("");
                text.append("");
                text.append("");
                //text.append("");
            }
        }

        if (getCheckUserVariables()){
            for (int variableIndex = 0; variableIndex < this.varCount; ++variableIndex) {
                text.append(event.getVarValue(variableIndex));
            }
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

        public void append(byte[] binaryData)
        {
            String stringData = org.apache.commons.codec.binary.Base64.encodeBase64String(binaryData);
            append(stringData);

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
            fw.write(csvString);
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

    public FileWriter createFile(String logFilePath) throws IOException {

        File parentDirectory = new File(logFilePath).getParentFile();
        if (parentDirectory != null) {
            // returns false if directory already exists, so need to check again
            if(parentDirectory.mkdirs()){
                log.info("Folder "+parentDirectory.getAbsolutePath()+" was created");
            } // else if might have been created by another process so not a problem
            if (!parentDirectory.exists()){
                log.warn("Error creating directories for "+parentDirectory.toString());
            }
        }

        File logFile = new File(logFilePath);
        if (logFile.exists()) {
            String logFilePathNew = "";
            while (logFile.exists()) {
                number++;
                int lastPointIndex = logFilePath.lastIndexOf(".");
                String logFilePathBaseName = logFilePath.substring(0, lastPointIndex);
                logFilePathNew = logFilePathBaseName + "_" + number + logFilePath.substring(lastPointIndex);
                logFile = new File(logFilePathNew);
            }
            logFilePath = logFilePathNew;
        }

        StringBuilder logHeaderLine = new StringBuilder(100);

        logHeaderLine.append("timeStamp"); // timeStamp
        logHeaderLine.append(";elapsed"); // elapsedTime
        logHeaderLine.append(";label"); // label
        logHeaderLine.append(";responseCode"); // responseCode
        logHeaderLine.append(";responseMessage"); // responseMessage
        logHeaderLine.append(";threadName"); // threadName - duplicate  event.threadGroup
        logHeaderLine.append(";dataType"); // dataType
        logHeaderLine.append(";success"); // success
        logHeaderLine.append(";failureMessage"); // failureMessage
        logHeaderLine.append(";bytes"); // размер ответа (responseData.length, headersSize, bodySize
        logHeaderLine.append(";grpThreads"); // groupThreads
        logHeaderLine.append(";allThreads"); // allThreads
        logHeaderLine.append(";URL"); // location
        logHeaderLine.append(";Filename"); // resultFileName
        logHeaderLine.append(";Latency"); // latency
        logHeaderLine.append(";Encoding"); // dataEncoding
        logHeaderLine.append(";SampleCount"); // sampleCount
        logHeaderLine.append(";ErrorCount"); // 0 или 1 (success)
        logHeaderLine.append(";Hostname"); // hostname
        logHeaderLine.append(";IdleTime"); // idleTime
        logHeaderLine.append(";Connect"); // connectTime

        if (getCheckAdditionalParams()) {
            logHeaderLine.append(";\"headersSize\""); // headersSize - 23
            logHeaderLine.append(";\"bodySize\""); // bodySize - 24
            logHeaderLine.append(";\"contentType\""); // contentType - 25
            logHeaderLine.append(";\"endTime\""); // endTime - 26
            logHeaderLine.append(";\"isMonitor\""); // isMonitor - 27
            logHeaderLine.append(";\"threadName_label\""); // threadName + label - 28
            logHeaderLine.append(";\"parent_threadName_label\""); // parent - threadName + label - 29
            logHeaderLine.append(";\"startTime\""); // startTime - 30
            logHeaderLine.append(";\"stopTest\""); // stopTest - 31
            logHeaderLine.append(";\"stopTestNow\""); // stopTestNow - 32
            logHeaderLine.append(";\"stopThread\""); // stopThread - 33
            logHeaderLine.append(";\"startNextThreadLoop\""); // startNextThreadLoop - 34
            logHeaderLine.append(";\"isTransactionSampleEvent\""); // isTransactionSampleEvent - 36
            logHeaderLine.append(";\"transactionLevel\""); // transactionLevel - 37
        }

        if (getCheckResponseData()) {
            logHeaderLine.append(";\"responseDataAsString\""); // responseDataAsString - 38
            logHeaderLine.append(";\"requestHeaders\""); // requestHeaders - 39
            logHeaderLine.append(";\"responseData\""); // responseData - 40
            logHeaderLine.append(";\"responseHeaders\""); // responseHeaders - 41
        }

        if (getCheckUserVariables()) {
            this.varCount = SampleEvent.getVarCount();

            if (this.varCount > 0) {
                for (int resultString = 0; resultString < this.varCount; ++resultString) {
                    logHeaderLine.append(";\"");
                    logHeaderLine.append(SampleEvent.getVarName(resultString));
                    logHeaderLine.append("\"");
                }
            }
        }
        logHeaderLine.append('\n');

        fw = new FileWriter(logFilePath, false);
        log.info("CREATED FILE: " + logFilePath);
        fw.write(logHeaderLine.toString());
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
        this.filepath = this.getFilename();
        try {
            this.fw = createFile(this.filepath);
        } catch (IOException e) {
            log.fatalError(e.getMessage(), e);
        }
    }

    /**
     * TestStateListener.testStarted
     */
    @Override
    public void testStarted(String host)
    {
        synchronized(LOCK) {
            if (instanceCount == 0) { // Only add the hook once
                shutdownHook = new Thread(new ShutdownHook());
                Runtime.getRuntime().addShutdownHook(shutdownHook);
            }
            instanceCount++;
            try {
                createFile(this.filepath);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
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
            log.error(e.getMessage(), e);
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

    public void setCheckAdditionalParams(boolean value) {
            setProperty(checkAdditionalParams, value);
        }

    public boolean getCheckAdditionalParams() {
            return getPropertyAsBoolean(checkAdditionalParams);
    }

    public void setCheckResponseData(boolean value) {
            setProperty(checkResponseData, value);
        }

    public boolean getCheckResponseData() {
            return getPropertyAsBoolean(checkResponseData);
    }

    public void setCheckUserVariables(boolean value) {
            setProperty(checkUserVariables, value);
        }

    public boolean getCheckUserVariables() {
            return getPropertyAsBoolean(checkUserVariables);
        }
}
