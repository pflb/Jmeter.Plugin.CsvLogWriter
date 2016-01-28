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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class CsvLogWriterGui extends AbstractListenerGui {

    //JMeterGUIComponent.getLabelResource()
    @Override
    public String getLabelResource()
    {
        throw new NotImplementedException();
    }

    //JMeterGUIComponent.createTestElement()
    @Override
    public TestElement createTestElement()
    {
        throw new NotImplementedException();
    }

    //JMeterGUIComponent.modifyTestElement(TestElement te)
    @Override
    public void modifyTestElement(TestElement te)
    {

    }


}