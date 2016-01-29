package ru.pflb.JMeter.Plugin;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.JMeterUtils;
//jorphan.jar
import org.apache.jorphan.logging.LoggingManager;
//logkit-2.0.jar
import org.apache.log.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;

public class CsvLogWriter
        extends AbstractListenerElement
        implements SampleListener, Serializable,
        TestStateListener, Remoteable, NoThreadClone {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String MAXFILESIZE = "maxFileSize";
    private static final String FILENAME = "filename";
    private static final String WRITE_BUFFER_LEN_PROPERTY = "ru.pflb.JMeter.Plugin.CLWBufferSize";
    private final int writeBufferSize = JMeterUtils.getPropDefault(WRITE_BUFFER_LEN_PROPERTY, 1024 * 10);

    public CsvLogWriter() {
        super();
        if (log.isDebugEnabled()) { log.debug("CsvLogWriter(3)");}
    }

    /**
     * SampleListener.sampleOccurred
     * @param e
     */
    @Override
    public void sampleOccurred(SampleEvent e)
    {
        if (log.isDebugEnabled()) { log.debug("CsvLogWriter.sampleOccurred( SampleEvent e == " + e + " )");}
    }

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



}