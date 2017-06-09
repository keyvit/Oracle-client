package ru.nsu.fit.g14201.marchenko.view;

import ru.nsu.fit.g14201.marchenko.utils.TableColumnAdjuster;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

/**
 */
public class ReportFrame extends JFrame {
    public ReportFrame(DefaultTableModel tableModel) {
        super("Отчёт");
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        configureFrame(tableModel);
        pack();
        setLocationRelativeTo(null);
    }

    private void configureFrame(DefaultTableModel tableModel) {
        JTable jTable = new JTable(tableModel);
        jTable.setEnabled(false);
        jTable.setGridColor(new Color((0x9D9D9D)));
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        jTable.setRowSorter(sorter);

        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnAdjuster tca = new TableColumnAdjuster(jTable);
        tca.adjustColumns();

        JScrollPane sp = new JScrollPane(jTable);
        add(sp);
    }
}
