package ru.pflb.JMeter.Plugin;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class CsvLogWriterGui extends AbstractListenerGui {

    private static final Logger log = LoggingManager.getLoggerForClass();

    //JMeterGUIComponent.getLabelResource()
    @Override
    public String getLabelResource()
    {
        if (log.isDebugEnabled()) { log.debug("CsvLogWriterGui.getLabelResource()");}

        return "CsvLogWriterGui_displayName";
    }

    //JMeterGUIComponent.createTestElement()
    @Override
    public TestElement createTestElement(){
        if (log.isDebugEnabled()) { log.debug("CsvLogWriterGui.createTestElement()");}
        TestElement te = new CsvLogWriter();
        modifyTestElement(te);
        te.setComment("comment");
        te.setName("pflb@CsvLogWriter");
        return te;
    }

    //JMeterGUIComponent.modifyTestElement(TestElement te)
    @Override
    public void modifyTestElement(TestElement te)
    {
        super.configureTestElement(te);
        if (log.isDebugEnabled()) { log.debug("CsvLogWriterGui.modifyTestElement( TestElement te = " + te + " )"); }
        if (te instanceof CsvLogWriter) {

        }
    }

    public CsvLogWriterGui()
    {
        super();
        if (log.isDebugEnabled()) { log.debug("CsvLogWriterGui.CsvLogWriterGui()");}
        init();
    }

    @Override
    public String getStaticLabel() {
        if (log.isDebugEnabled()) { log.debug("CsvLogWriterGui.getStaticLabel()");}
        return "pflb@CsvLogWriter";
    }

    private void init() {
        if (log.isDebugEnabled()) { log.debug("CsvLogWriterGui.init()");}
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());

        JPanel container = new JPanel(new BorderLayout());
        container.add(mainPanel, BorderLayout.NORTH);
        add(container, BorderLayout.CENTER);
    }

}