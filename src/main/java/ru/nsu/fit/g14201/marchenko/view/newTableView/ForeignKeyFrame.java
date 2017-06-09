package ru.nsu.fit.g14201.marchenko.view.newTableView;

import ru.nsu.fit.g14201.marchenko.model.Column;
import ru.nsu.fit.g14201.marchenko.utils.SpringUtilities;
import ru.nsu.fit.g14201.marchenko.model.TableInfoGetter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class ForeignKeyFrame extends ConstraintFrame {
    private TableInfoGetter tableInfoGetter;
    private Column[] colsForComboBoxModel;
    private List<JComboBox> refAttr;
    private Column[] columns;
    private String[] tables;

    public ForeignKeyFrame(Object[] columns, List<String> tables,
                           TableInfoGetter tableInfoGetter) {
        super("Добавление внешнего ключа");

        this.columns = new Column[columns.length];
        for (int i = 0; i < columns.length; i++)
            this.columns[i] = (Column) columns[i];

        this.tables = new String[tables.size()];
        for (int i = 0; i < tables.size(); i++)
            this.tables[i] = tables.get(i);

        this.tableInfoGetter = tableInfoGetter;
        configureFrame();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void configureFrame() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        JPanel namePanel = new JPanel();

        JLabel nameLabel = new JLabel("Название");
        JTextField nameField = new JTextField(15);

        namePanel.add(nameLabel);
        nameLabel.setLabelFor(nameField);
        namePanel.add(nameField);
        mainPanel.add(namePanel);

        List<JComboBox> comboBoxes = new ArrayList<>();
        mainPanel.add(addFirstAttribute(comboBoxes));

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton addAttribute = new JButton("Добавить атрибут");
        addAttribute.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton addForeignKey = new JButton("Добавить внешний ключ");
        addForeignKey.setAlignmentX(Component.CENTER_ALIGNMENT);

        addForeignKey.addActionListener(new ActionListener() {
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
                int size = (comboBoxes.size() - 1) / 2;
                Column[] columns = new Column[size];
                Column[] refColumns = new Column[size];

                columns[0] = (Column) comboBoxes.get(0).getSelectedItem();
                refColumns[0] = (Column) comboBoxes.get(2).getSelectedItem();

                for (int i = 3, j = 1; i < comboBoxes.size(); i += 2, j++) {
                    columns[j] = (Column) comboBoxes.get(i).getSelectedItem();
                    refColumns[j] = (Column) comboBoxes.get(i + 1).getSelectedItem();
                }

                tableCreator.addForeignKey(name, columns,
                        (String) comboBoxes.get(1).getSelectedItem(), refColumns);
                dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(addAttribute);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(addForeignKey);

        mainPanel.add(buttonPanel);

        addAttribute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (JComboBox comboBox : comboBoxes) {
                    if (comboBox.getSelectedIndex() == -1) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Перед добавлением нового атрибута необходимо определить старые.",
                                "Ошибка",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                }
                mainPanel.remove(buttonPanel);
                mainPanel.add(addAttribute(comboBoxes));
                mainPanel.add(buttonPanel);
                mainPanel.revalidate();
                pack();
                mainPanel.repaint();
            }
        });

        int borderW = 20;
        mainPanel.setBorder(BorderFactory.createEmptyBorder(borderW, borderW, borderW, borderW));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane);
    }

    private JPanel addFirstAttribute(List<JComboBox> comboBoxes) {
        JPanel panel = new JPanel(new SpringLayout());

        JLabel[] labels = new JLabel[3];
        labels[0] = new JLabel("Атрибут");
        labels[1] = new JLabel("Главная таблица");
        labels[2] = new JLabel("Главный атрибут");

        JComboBox[] cb = new JComboBox[3];
        cb[0] = new JComboBox(columns);
        cb[1] = new JComboBox(tables);
        cb[2] = new JComboBox();

        refAttr = new ArrayList<>();
        refAttr.add(cb[2]);

        try {
            List<Column> columnList = tableInfoGetter.getTableColumns(tables[0]);
            colsForComboBoxModel = new Column[columnList.size()];
            columnList.toArray(colsForComboBoxModel);
            DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(colsForComboBoxModel);
            cb[2].setModel(comboBoxModel);
        } catch (SQLException exc) {
            JOptionPane.showMessageDialog(
                    null,
                    exc.getLocalizedMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
            dispose();
        }

        cb[1].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String table = (String)cb[1].getSelectedItem();
                try {
                    List<Column> columnList = tableInfoGetter.getTableColumns(table);
                    colsForComboBoxModel = new Column[columnList.size()];
                    columnList.toArray(colsForComboBoxModel);
                    for (JComboBox box : refAttr)
                        box.setModel(new DefaultComboBoxModel(colsForComboBoxModel));
                    //cb[2].setModel(new DefaultComboBoxModel(columnsArr));
                } catch (SQLException exc) {
                    JOptionPane.showMessageDialog(
                            null,
                            exc.getLocalizedMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                    );
                    dispose();
                }
            }
        });

        for (int i = 0; i < 3; i++) {
            comboBoxes.add(cb[i]);
            panel.add(labels[i]);
            labels[i].setLabelFor(cb[i]);
            panel.add(cb[i]);
        }

        SpringUtilities.makeCompactGrid(panel,
                3, 2,
                6, 6,
                6, 6);

        return panel;
    }

    private JPanel addAttribute(List<JComboBox> comboBoxes) {
        JPanel panel = new JPanel(new SpringLayout());

        JLabel[] labels = new JLabel[2];
        labels[0] = new JLabel("Атрибут");
        labels[1] = new JLabel("Главный атрибут");

        JComboBox[] cb = new JComboBox[2];
        cb[0] = new JComboBox(columns);
        cb[1] = new JComboBox();

        cb[1].setModel(new DefaultComboBoxModel(colsForComboBoxModel));
        refAttr.add(cb[1]);

        for (int i = 0; i < 2; i++) {
            comboBoxes.add(cb[i]);
            panel.add(labels[i]);
            labels[i].setLabelFor(cb[i]);
            panel.add(cb[i]);
        }

        SpringUtilities.makeCompactGrid(panel,
                2, 2,
                6, 6,
                6, 6);

        return panel;
    }
}
