package ru.pflb.JMeter.Plugin;


import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.*;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

/*
import ru.pflb.JMeter.JMeterPluginsUtils;
import ru.pflb.JMeter.BrowseAction;
import ru.pflb.JMeter.GuiBuilderHelper;*/
//jorphan.jar
//logkit-2.0.jar



public class CsvLogWriter
        extends AbstractListenerElement
        implements SampleListener, Serializable,
        TestStateListener, Remoteable, NoThreadClone {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String WRITE_BUFFER_LEN_PROPERTY = "ru.pflb.JMeter.Plugin.CLWBufferSize";

    private static final String FILENAME = "filename";
    private static final String[] DATE_FORMAT_STRINGS = new String[]{"yyyy/MM/dd HH:mm:ss.SSS", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss", "MM/dd/yy HH:mm:ss"};
    private static int number = 0;
    private static int event_count = 0;
    public static FileWriter fw;
    private final int writeBufferSize = JMeterUtils.getPropDefault(WRITE_BUFFER_LEN_PROPERTY, 1024 * 10);
    private String filepath = computeFileName(number);
    public CsvLogWriter() throws IOException {
        super();
        fw = createFile(filepath);
        if (log.isDebugEnabled()) { log.debug("CsvLogWriter()");}
    }

    /*private static String[] splitHeader(String headerLine, String delim) {
        String[] parts = headerLine.split("\\Q" + delim);
        int previous = -1;

        for(int i = 0; i < parts.length; ++i) {
            String label = parts[i];
            if(isVariableName(label)) {
                previous = 2147483647;
            } else {
                int current = headerLabelMethods.indexOf(label);
                if(current == -1) {
                    return null;
                }

                if(current <= previous) {
                    log.warn("Column header number " + (i + 1) + " name " + label + " is out of order.");
                    return null;
                }

                previous = current;
            }
        }

        return parts;
    }*/

/*    private static String[] splitHeader(String headerLine, String delim) {
        String[] parts = headerLine.split("\\Q" + delim);
        return parts;
    }*/

    /**
     * SampleListener.sampleOccurred
     * @param e
     */

    public static String resultToDelimitedString(SampleEvent event, String delimiter) {
        StringQuoter text = new StringQuoter(delimiter.charAt(0));
        SampleResult sample = event.getResult();
        SampleSaveConfiguration saveConfig = sample.getSaveConfig();
        String i;
        //if(saveConfig.saveTimestamp()) {
        //    if(saveConfig.printMilliseconds()) {
                text.append(sample.getTimeStamp());
         /*   } else if(saveConfig.formatter() != null) {
                i = saveConfig.formatter().format(new Date(sample.getTimeStamp()));
                text.append(i);
            }
        }*/

        //if(saveConfig.saveTime()) {
            text.append(sample.getTime());
       // }

       // if(saveConfig.saveLabel()) {
            text.append(sample.getSampleLabel());
        //}

       // if(saveConfig.saveCode()) {
            text.append(sample.getResponseCode());
        //}

       // if(saveConfig.saveMessage()) {
            text.append(sample.getResponseMessage());
        //}

        //if(saveConfig.saveThreadName()) {
            text.append(sample.getThreadName());
        //}

        //if(saveConfig.saveDataType()) {
            text.append(sample.getDataType());
        //}

       // if(saveConfig.saveSuccess()) {
            text.append(sample.isSuccessful());
       // }

       // if(saveConfig.saveAssertionResultsFailureMessage()) {
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
       // }

       // if(saveConfig.saveBytes()) {
            text.append(sample.getBytes());
       // }

       // if(saveConfig.saveThreadCounts()) {
            text.append(sample.getGroupThreads());
            text.append(sample.getAllThreads());
       // }

      //  if(saveConfig.saveUrl()) {
            text.append((Object)sample.getURL());
      //  }

       // if(saveConfig.saveFileName()) {
            text.append(sample.getResultFileName());
       // }

       // if(saveConfig.saveLatency()) {
            text.append(sample.getLatency());
      //  }

       // if(saveConfig.saveEncoding()) {
            text.append(sample.getDataEncodingWithDefault());
      //  }

     //   if(saveConfig.saveSampleCount()) {
            text.append(sample.getSampleCount());
            text.append(sample.getErrorCount());
       // }

      //  if(saveConfig.saveHostname()) {
            text.append(event.getHostname());
      //  }

       // if(saveConfig.saveIdleTime()) {
            text.append(event.getResult().getIdleTime());
     //   }

      //  if(saveConfig.saveConnectTime()) {
            text.append(sample.getConnectTime());
      //  }

        for(int var8 = 0; var8 < SampleEvent.getVarCount(); ++var8) {
            text.append(event.getVarValue(var8));
        }

        //text.append("\r\n");

        return text.toString();
    }

    private static String quoteDelimiters(String input, char[] specialChars) {
       /* if(StringUtils.containsNone(input, specialChars)) {
            return input;
        } else {*/
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
     //   }
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

 /*   @Override
    public void sampleOccurred(SampleEvent event) {
        SampleResult result = event.getResult();

        if (isSampleWanted(result.isSuccessful())) {
            sendToVisualizer(result);
            if (out != null && !isResultMarked(result) && !this.isStats) {
                SampleSaveConfiguration config = getSaveConfig();
                result.setSaveConfig(config);
                try {
                    if (config.saveAsXml()) {
                        SaveService.saveSampleResult(event, out);
                    } else { // !saveAsXml
                        String savee = org.apache.jmeter.save.CSVSaveService.resultToDelimitedString(event);
                        out.println(savee);
                    }
                } catch (Exception err) {
                    log.error("Error trying to record a sample", err); // should throw exception back to caller
                }
            }
        }

        if(summariser != null) {
            summariser.sampleOccurred(event);
        }
    }
*/
   /* public void writeHeader()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("TimeStamp;Time;SampleLabel;ResponseCode;ResponseMessage;ThreadName;DataType;isSuccessful;FailureMessage;Bytes;GroupThreads;AllThreads;URL;FileName;Latency;DataEncodingWithDefault;SampleCount;ErrorCount;Hostname;IdleTime;ConnectTime;VarValue\n");
    }*/
    @Override
    public void sampleOccurred(SampleEvent e)
    {
        StringBuffer sb = new StringBuffer();
        if (log.isDebugEnabled()) { log.debug("CsvLogWriter.sampleOccurred( SampleEvent e == " + e + " )");}
        //e.getResult().getTimeStamp() + ";" + e.getHostname() + ";"+e.getResult().getTime()+";" + e.getResult().getSampleLabel() + ";" + e.getResult().getLatency()+ "\n"
        sb.append(resultToDelimitedString(e, ";"));
        sb.append("\n");
        log.info(resultToDelimitedString(e, ";"));
        try {
            writeEvent(fw,sb);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public String computeFileName(int number)
    {

        String dirName = "C:\\Users\\a.perevozchikova\\Desktop\\";
        String baseName = "logX";
        String extention = "csv";
        String filepath = dirName + baseName + String.valueOf(number) + "." + extention;
        return filepath;
    }

    /*public FileWriter fw(String filepath)
    {
        fw = new FileWriter(filepath, true);
    }*/

    public FileWriter createFile(String filepath) throws IOException {
        //filepath = getFilename();
        FileWriter fw = new FileWriter(filepath, true);
        try {
            fw.write("TimeStamp;Time;SampleLabel;ResponseCode;ResponseMessage;ThreadName;DataType;isSuccessful;FailureMessage;Bytes;GroupThreads;AllThreads;URL;FileName;Latency;DataEncodingWithDefault;SampleCount;ErrorCount;Hostname;IdleTime;ConnectTime\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fw;
    }

 /*   public void openFile(FileWriter fw) throws IOException {
        /*filepath = getFilename();
        FileWriter fw = new FileWriter(filepath, true);*/
     /*   try {
            fw.write("TimeStamp;Time;SampleLabel;ResponseCode;ResponseMessage;ThreadName;DataType;isSuccessful;FailureMessage;Bytes;GroupThreads;AllThreads;URL;FileName;Latency;DataEncodingWithDefault;SampleCount;ErrorCount;Hostname;IdleTime;ConnectTime\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/
 //FileWriter fw,
    public void writeEvent(FileWriter fw, StringBuffer sb) throws IOException {
        event_count++;
        CsvLogWriter.fw.write(sb.toString());
        if (event_count >= 100000)
        {
            event_count = 0;
            closeFile(CsvLogWriter.fw);
            number++;
            String newfilename = computeFileName(number);
            CsvLogWriter.fw = createFile(newfilename);
        }
    }

    public void closeFile(FileWriter fw) throws IOException {
        fw.close();
    }


 /*   public void CSVWriter(StringBuffer sb)
    {
        try {
            FileWriter fw = new FileWriter(getFilename(), true);
            fw.write("TimeStamp;Time;SampleLabel;ResponseCode;ResponseMessage;ThreadName;DataType;isSuccessful;FailureMessage;Bytes;GroupThreads;AllThreads;URL;FileName;Latency;DataEncodingWithDefault;SampleCount;ErrorCount;Hostname;IdleTime;ConnectTime\n");
            fw.write(sb.toString());
            fw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }*/

    /**
     * SampleListener.sampleStarted
     * @param e
     */
    @Override
    public void sampleStarted(SampleEvent e) {
        if (log.isDebugEnabled()) { log.debug("CsvLogWriter.sampleStarted( SampleEvent e == " + e + " )");}
    }

    /**
     * SampleListener.sampleStopped
     * @param e
     */
    @Override
    public void sampleStopped(SampleEvent e) {
        if (log.isDebugEnabled()) { log.debug("CsvLogWriter.sampleStopped( SampleEvent e == " + e + " )");}
    }

    /**
     * TestStateListener.testStarted
     */


    @Override
    public void testStarted() {
        if (log.isDebugEnabled()) { log.debug("CsvLogWriter.testStarted()");}
    }

    /**
     * TestStateListener.testStarted
     */
    @Override
    public void testStarted(String host)
    {
        if (log.isDebugEnabled()) { log.debug("CsvLogWriter.testStarted( String host == " + host + " )");}
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
        if (log.isDebugEnabled()) { log.debug("CsvLogWriter.testEnded()");}
    }

    /**
     * TestStateListener.testEnded
     */
    @Override
    public void testEnded(String host)
    {
        if (log.isDebugEnabled()) { log.debug("CsvLogWriter.testEnded( String host == " + host + " )");}
    }

    //Методы для доступа к настройкам

    public void setFilename(String name) {
        setProperty(FILENAME, name);
    }

    public String getFilename() {
        return getPropertyAsString(FILENAME);
    }



}