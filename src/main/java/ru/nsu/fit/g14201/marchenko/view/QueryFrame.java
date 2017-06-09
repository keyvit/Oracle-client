package ru.nsu.fit.g14201.marchenko.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 */
public class QueryFrame extends JFrame {
    public QueryFrame(DatabaseActionListener databaseActionListener) {
        super("Редактор запросов");
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        configureFrame(databaseActionListener);
        pack();
        setLocationRelativeTo(null);
    }

    private void configureFrame(DatabaseActionListener databaseActionListener) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        JTextArea queryArea = new JTextArea();
        queryArea.setColumns(30);
        queryArea.setRows(10);
        queryArea.setFont(new Font("Serif", Font.PLAIN, 20));
        queryArea.setLineWrap(true);
        queryArea.setWrapStyleWord(true);
        queryArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(queryArea);
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton executeBut = new JButton("Выполнить");
        executeBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = queryArea.getText();
                if (query.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "Введите запрос",
                            "Ошибка",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (databaseActionListener.onExecuteQuery(query))
                    dispose();
            }
        });
        executeBut.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(executeBut);

        int borderWidth = 20;
        mainPanel.setBorder(BorderFactory.createEmptyBorder(borderWidth, borderWidth, borderWidth, borderWidth));
        add(mainPanel);
    }
}
