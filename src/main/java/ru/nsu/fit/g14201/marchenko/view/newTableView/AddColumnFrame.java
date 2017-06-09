package ru.nsu.fit.g14201.marchenko.view.newTableView;

import ru.nsu.fit.g14201.marchenko.model.Column;
import ru.nsu.fit.g14201.marchenko.utils.SpringUtilities;
import ru.nsu.fit.g14201.marchenko.model.TableCreator;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

/**
 */
class AddColumnFrame extends JFrame {
    private JTextField nameField;
    private JComboBox dataTypeBox;
    private JCheckBox notNullBox;
    private String[] possibleDataTypes = new String[] {"VARCHAR2", "NUMBER", "DATE"};

    private TableCreator tableCreator = null;

    AddColumnFrame() {
        super("Добавление поля");
        configureFrame();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    void setTableCreator(TableCreator tableCreator) {
        this.tableCreator = tableCreator;
    }

    private void configureFrame() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        add(mainPanel);

        JLabel nameLabel = new JLabel("Название", JLabel.TRAILING),
                typeLabel = new JLabel("Тип", JLabel.TRAILING),
                notNullLabel = new JLabel("NOT NULL", JLabel.TRAILING);

        nameField = new JTextField(20);
        dataTypeBox = new JComboBox(possibleDataTypes);
        notNullBox = new JCheckBox();

        JPanel infoPanel = new JPanel(new SpringLayout());

        infoPanel.add(nameLabel);
        nameLabel.setLabelFor(nameField);
        infoPanel.add(nameField);

        infoPanel.add(typeLabel);
        typeLabel.setLabelFor(dataTypeBox);
        infoPanel.add(dataTypeBox);

        infoPanel.add(notNullLabel);
        notNullLabel.setLabelFor(notNullBox);
        infoPanel.add(notNullBox);

        SpringUtilities.makeCompactGrid(infoPanel,
                3, 2,
                6, 6,
                6, 6);

        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name;
                if ((name = nameField.getText()).isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "Введите название поля.",
                            "Недостаточно информации",
                            JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                switch (dataTypeBox.getSelectedItem().toString()) {
                    case "VARCHAR2":
                        Varchar2Frame varchar2Frame = new Varchar2Frame();
                        varchar2Frame.setVisible(true);
                        break;
                    default:
                        formColumn(dataTypeBox.getSelectedItem().toString());
                }
            }
        });
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(infoPanel);
        mainPanel.add(addButton);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }
    private void formColumn(String type) {
        if (tableCreator != null) {
            if (!tableCreator.addColumn(new Column(nameField.getText(), type, notNullBox.isSelected()))) {
                JOptionPane.showMessageDialog(null,
                        "Поле с таким именем уже существует.",
                        "Невозможно добавить поле",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        dispose();
    }
    private void onVarchar2Definition(int length, boolean isInChar) {
        StringBuilder type = new StringBuilder("VARCHAR2(");
        type.append(length);
        if (isInChar)
            type.append(" CHAR");
        type.append(")");
        formColumn(type.toString());
    }

    class Varchar2Frame extends JFrame {
        private final int DEFAULT_LENGTH_IN_BYTES = 100;

        Varchar2Frame() {
            super("VARCHAR2");
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            this.configureFrame();
            pack();
            setLocationRelativeTo(null);
        }
        private void configureFrame() {
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
            add(mainPanel);

            JLabel label = new JLabel("Введите максимальную длину переменной типа VARCHAR2:",
                    JLabel.LEFT);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(label);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            JPanel inputDataPanel = new JPanel();
            NumberFormatter lengthFormatter = new NumberFormatter(NumberFormat.getIntegerInstance());
            lengthFormatter.setMinimum(1);
            lengthFormatter.setMaximum(4000);
            JFormattedTextField varcharLength = new JFormattedTextField(lengthFormatter);
            varcharLength.setColumns(5);
            inputDataPanel.add(varcharLength);
            JComboBox measurement = new JComboBox(new String[] {"байт", "символ(ов)"});
            inputDataPanel.add(measurement);
            mainPanel.add(inputDataPanel);

            JPanel buttonsPanel = new JPanel();
            JButton okayButton = new JButton("OK");
            okayButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (varcharLength.getValue() == null) {
                        JOptionPane.showMessageDialog(null,
                                "Введите размер переменной типа VARCHAR2.",
                                "Недостаточно информации",
                                JOptionPane.PLAIN_MESSAGE);
                        return;
                    }
                    finishDefiningVarchar2((Integer) varcharLength.getValue(),
                            measurement.getSelectedItem().toString());
                }
            });
            JButton defaultButton = new JButton("Использовать значение по умолчанию");
            defaultButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    finishDefiningVarchar2(DEFAULT_LENGTH_IN_BYTES, "байт");
                }
            });
            buttonsPanel.add(okayButton);
            buttonsPanel.add(defaultButton);
            mainPanel.add(buttonsPanel);

            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }

        private void finishDefiningVarchar2(int length, String unit) {
            setVisible(false);
            onVarchar2Definition(length, !unit.equals("байт"));
            dispose();
        }
    }
}
