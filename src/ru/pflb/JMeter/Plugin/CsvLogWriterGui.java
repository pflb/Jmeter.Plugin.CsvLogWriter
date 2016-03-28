package ru.pflb.JMeter.Plugin;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class CsvLogWriterGui extends AbstractListenerGui {

    private JTextField filename;

    @Override
    public String getLabelResource()
    {
        return "CsvLogWriterGui_displayName";
    }


    @Override
    public TestElement createTestElement(){
        TestElement testElement = null;
        try {
            testElement = new CsvLogWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        modifyTestElement(testElement);
        testElement.setComment("Документация: http://wiki.performance-lab.ru/index.php/Плагин_CsvLogWriter");
        testElement.setName("pflb@CsvLogWriter");
        return testElement;
    }

    public CsvLogWriterGui()
    {
        super();
        init();
    }

    @Override
    public void modifyTestElement(TestElement testElement)
    {
        super.configureTestElement(testElement);
        if (testElement instanceof CsvLogWriter) {
            CsvLogWriter fw = (CsvLogWriter) testElement;
            fw.setFilename(filename.getText());
        }
    }

    @Override
    public void configure(TestElement testElement) {
        super.configure(testElement);
        CsvLogWriter csvLogWriter = (CsvLogWriter) testElement;
        filename.setText(csvLogWriter.getFilename());
    }

    @Override
    public String getStaticLabel() {
        return "pflb@CsvLogWriter";
    }


    private void init() {
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

        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser();
                int ret = fileopen.showDialog(null, "Открыть файл");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fileopen.getSelectedFile();
                    filename.setText(file.getAbsolutePath());
                }
            }
        });
    }

    private void addToPanel(JPanel panel, GridBagConstraints constraints, int col, int row, JComponent component) {
        constraints.gridx = col;
        constraints.gridy = row;
        panel.add(component, constraints);
    }

}