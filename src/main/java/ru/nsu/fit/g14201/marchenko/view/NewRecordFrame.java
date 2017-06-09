package ru.nsu.fit.g14201.marchenko.view;

import ru.nsu.fit.g14201.marchenko.model.Column;
import ru.nsu.fit.g14201.marchenko.utils.SpringUtilities;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 */
class NewRecordFrame extends JFrame {

    NewRecordFrame(List<Column> columns, DefaultTableModel table) {
        super("Добавить запись");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        configureFrame(columns, table);
        pack();
        setLocationRelativeTo(null);
    }

    private void configureFrame(List<Column> columns, DefaultTableModel table) {
        int size = columns.size();

        JLabel[] labels = new JLabel[size];
        JTextField[] fields = new JTextField[size];

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        JPanel recordPanel = new JPanel(new SpringLayout());
        for (int i = 0; i < size; i++) {
            Column column = columns.get(i);
            labels[i] = new JLabel(column.getName() + " [" + column.getType() + "]");
            recordPanel.add(labels[i]);
            fields[i] = new JTextField();
            fields[i].setColumns(10);
            labels[i].setLabelFor(fields[i]);
            recordPanel.add(fields[i]);
        }

        SpringUtilities.makeCompactGrid(recordPanel,
                size, 2,
                6, 6,
                6, 6);

        mainPanel.add(recordPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton okayButton = new JButton("Добавить");
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean allEmpty = true;
                for (int i = 0; i < fields.length; i++)
                    if (!fields[i].getText().isEmpty()) {
                        allEmpty = false;
                        break;
                    }
                if (allEmpty) {
                    JOptionPane.showMessageDialog(null,
                            "Невозможно создать запись, все поля которой пустые.",
                            "Ошибка",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Object[] values = new Object[fields.length];
                for (int i = 0; i < fields.length; i++)
                    values[i] = fields[i].getText();
                table.addRow(values);
                dispose();
            }
        });
        okayButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(okayButton);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(mainPanel);
    }
}
