package ru.nsu.fit.g14201.marchenko.view;

import javax.swing.*;
import java.awt.*;

/**
 */
public class TablePanel extends JPanel { //TODO JScrollPane
    TablePanel() {
        setLayout(new BorderLayout());
    }

    void addTable() {
        JTable table = new JTable();
        add(table, BorderLayout.CENTER);
    }
}
