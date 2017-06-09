package ru.nsu.fit.g14201.marchenko.view.newTableView;


import ru.nsu.fit.g14201.marchenko.model.Column;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 */
public class PrimaryKeyFrame extends ConstraintFrame {

    public PrimaryKeyFrame(DefaultListModel<Column> columnListModel) {
        super("Добавление первичного ключа");
        configureFrame(columnListModel);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void configureFrame(DefaultListModel<Column> columnListModel) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        JPanel namePanel = new JPanel();
        JTextField nameField = new JTextField(15);
        JLabel nameLabel = new JLabel("Название ограничения: ");

        namePanel.add(nameLabel);
        nameLabel.setLabelFor(nameField);
        namePanel.add(nameField);
        namePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(namePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JList list = new JList(columnListModel);
        configureList(list);
        JScrollPane scrollPane = new JScrollPane(list);
        mainPanel.add(scrollPane);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel buttonPanel = new JPanel();
        JButton okayButton = new JButton("OK"),
                cancelButton = new JButton("Cancel");

        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Введите название ограничения.",
                            "Предупреждение",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                List<Column> selColumns = list.getSelectedValuesList();
                if (selColumns.isEmpty()) {
                    dispose();
                    return;
                }
                tableCreator.addPrimaryKey(name, selColumns);
                dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPanel.add(okayButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel);

        int borderWidth = 20;
        mainPanel.setBorder(BorderFactory.createEmptyBorder(borderWidth, borderWidth, borderWidth, borderWidth));
        add(mainPanel);
    }
}
