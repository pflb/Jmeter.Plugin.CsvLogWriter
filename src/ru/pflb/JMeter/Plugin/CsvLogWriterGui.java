package ru.pflb.JMeter.Plugin;


import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;

import javax.swing.*;
import java.awt.*;

/*import ru.pflb.JMeter.JMeterPluginsUtils;
import ru.pflb.JMeter.BrowseAction;
import ru.pflb.JMeter.GuiBuilderHelper;*/

public class CsvLogWriterGui extends AbstractListenerGui {
    private JTextField filename;
    /*private JTextField columns;
    private JCheckBox overwrite;
    private JTextArea header;
    private JTextArea footer;
    private String[] fields = {
            "endTime", "Epoch time when the request was ended",
            "endTimeMillis", "Same as endTime, but divided by 1000 (surrogate field, eg. 1311122631.104)"};*/

    //JMeterGUIComponent.getLabelResource()
    public CsvLogWriterGui() {
        super();
        //super.makeTitlePanel();
        init();
        // initFields();
    }

    /*   @Override
       public String getStaticLabel() {
           return JMeterPluginsUtils.prefixLabel("Csv Log Writer");
       }
   */
    @Override
    public String getLabelResource() {
        return getClass().getCanonicalName();
    }

    //JMeterGUIComponent.createTestElement()
    @Override
    public TestElement createTestElement() {
        TestElement te = new CsvLogWriter();
        //return te;
        // Подключает GUI
        modifyTestElement(te);
        // te.setComment(JMeterPluginsUtils.getWikiLinkText(WIKIPAGE));
        return te;
    }
    //   }


    @Override
    public void modifyTestElement(TestElement te) {
        super.configureTestElement(te);
        if (te instanceof CsvLogWriter) {
            CsvLogWriter fw = (CsvLogWriter) te;
            // Присваивается ИМЯ
            fw.setName("CSV Log Writer");

           /* fw.setFilename("CSV Log Writer");
            fw.setFileHeader("CSV Log Writer");
            fw.setFilename(filename.getText());
            fw.setColumns(columns.getText());
            fw.setOverwrite(overwrite.isSelected());
            fw.setFileHeader(header.getText());
            fw.setFileFooter(footer.getText());*/
        }
    }

 /*   @Override
    public void clearGui() {
        super.clearGui();
        //  initFields();
    }*/

    //JMeterGUIComponent.modifyTestElement(TestElement te)
/*    @Override
    public void modifyTestElement(TestElement te)
    {
        super.configureTestElement(te);
    }*/


    //


/*
    private void initFields() {
        filename.setText("testResults.txt");
        columns.setText("endTimeMillis|\\t|"
                + "responseTime|\\t|latency|\\t|"
                + "sentBytes|\\t|receivedBytes|\\t|"
                + "isSuccessful|\\t|responseCode|\\r\\n");
        overwrite.setSelected(false);
        header.setText("endTimeMillis\tresponseTime\tlatency\tsentBytes\t"
                + "receivedBytes\tisSuccessful\tresponseCode\n");
        footer.setText("");
    }*/

     private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        super.makeTitlePanel();

        //add(makeTitlePanel());
        //add(JMeterPluginsUtils.addHelpLinkToPanel(makeTitlePanel(), WIKIPAGE), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());

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
        //GuiBuilderHelper.strechItemToComponent(filename, browseButton);
        //browseButton.addActionListener(new BrowseAction(filename));
/*
        addToPanel(mainPanel, labelConstraints, 0, 2, new JLabel("Overwrite existing file: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, 2, overwrite = new JCheckBox());

        addToPanel(mainPanel, labelConstraints, 0, 3, new JLabel("Write File Header: ", JLabel.RIGHT));
        header = new JTextArea();
        header.setLineWrap(true);
        addToPanel(mainPanel, editConstraints, 1, 3, GuiBuilderHelper.getTextAreaScrollPaneContainer(header, 3));

        editConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        labelConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        addToPanel(mainPanel, labelConstraints, 0, 4, new JLabel("Record each sample as: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, 4, columns = new JTextField(20));

        editConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        labelConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        addToPanel(mainPanel, labelConstraints, 0, 5, new JLabel("Write File Footer: ", JLabel.RIGHT));
        footer = new JTextArea();
        footer.setLineWrap(true);
        addToPanel(mainPanel, editConstraints, 1, 5, GuiBuilderHelper.getTextAreaScrollPaneContainer(footer, 3));*/

        JPanel container = new JPanel(new BorderLayout());
        container.add(mainPanel, BorderLayout.NORTH);
        add(container, BorderLayout.CENTER);

        //add(createHelperPanel(), BorderLayout.SOUTH);
    }

    private void addToPanel(JPanel panel, GridBagConstraints constraints, int col, int row, JComponent component) {
        constraints.gridx = col;
        constraints.gridy = row;
        panel.add(component, constraints);
    }
}
//}
