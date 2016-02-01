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
import java.io.IOException;
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
    private JTextField filename;
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
        TestElement te = null;
        try {
            te = new CsvLogWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            CsvLogWriter fw = (CsvLogWriter) te;
            fw.setFilename(filename.getText());
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

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.FIRST_LINE_END;

        GridBagConstraints editConstraints = new GridBagConstraints();
        editConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        editConstraints.weightx = 1.0;
        editConstraints.fill = GridBagConstraints.HORIZONTAL;

        addToPanel(mainPanel, labelConstraints, 0, 1, new JLabel("Filename: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, 1, filename = new JTextField(20));
        JButton browseButton = new JButton("Browse...");
        addToPanel(mainPanel, labelConstraints, 2, 1, browseButton);

        /*GuiBuilderHelper.strechItemToComponent(filename, browseButton);
        browseButton.addActionListener(new BrowseAction(filename));*/

       /* addToPanel(mainPanel, labelConstraints, 0, 2, new JLabel("Overwrite existing file: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, 2, overwrite = new JCheckBox());

        addToPanel(mainPanel, labelConstraints, 0, 3, new JLabel("Write File Header: ", JLabel.RIGHT));
        header = new JTextArea();
        header.setLineWrap(true);
        addToPanel(mainPanel, editConstraints, 1, 3, GuiBuilderHelper.getTextAreaScrollPaneContainer(header, 3));*/
    }
    private void addToPanel(JPanel panel, GridBagConstraints constraints, int col, int row, JComponent component) {
        constraints.gridx = col;
        constraints.gridy = row;
        panel.add(component, constraints);
    }


}