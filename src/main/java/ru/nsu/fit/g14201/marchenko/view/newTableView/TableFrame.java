package ru.nsu.fit.g14201.marchenko.view.newTableView;

import ru.nsu.fit.g14201.marchenko.model.Column;
import ru.nsu.fit.g14201.marchenko.model.Constraint;
import ru.nsu.fit.g14201.marchenko.model.ForeignKey;
import ru.nsu.fit.g14201.marchenko.model.Table;
import ru.nsu.fit.g14201.marchenko.view.DatabaseActionListener;
import ru.nsu.fit.g14201.marchenko.model.TableCreator;
import ru.nsu.fit.g14201.marchenko.model.TableInfoGetter;
import ru.nsu.fit.g14201.marchenko.model.TableModifier;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

/**
 */
public class TableFrame extends JFrame implements ListSelectionListener, TableCreator {
    private TableFrame tableFrame;
    private DefaultListModel<Column> columnsListModel;
    private DefaultListModel<Constraint> constraintsListModel;
    private JTextField nameField;
    private JComboBox tablespaceList;

    private boolean hasPrimaryKey = false;
    private Table curTable = null;

    private DatabaseActionListener databaseActionListener = null;
    private TableInfoGetter tableInfoGetter;
    private TableModifier tableModifier;

    public TableFrame(TableInfoGetter tableInfoGetter, TableModifier tableModifier, String tableName)
            throws SQLException {
        super("Создание таблицы");
        tableFrame = this;
        this.tableModifier = tableModifier;
        this.tableInfoGetter = tableInfoGetter;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        curTable = tableInfoGetter.getTable(tableName);
        configureWindow(null);
        setMinimumSize(new Dimension(600, 500));
        pack();
        setLocationRelativeTo(null);
    }
    public TableFrame(java.util.List<String> tablespaces) {
        super("Создание таблицы");
        tableFrame = this;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        configureWindow(tablespaces);
        setMinimumSize(new Dimension(600, 500));
        pack();
        setLocationRelativeTo(null);
    }

    public void setDatabaseActionListener(DatabaseActionListener databaseActionListener) {
        this.databaseActionListener = databaseActionListener;
    }
    public void setTableInfoGetter(TableInfoGetter tableInfoGetter) {
        this.tableInfoGetter = tableInfoGetter;
    }

    private void configureWindow(java.util.List<String> tablespaces) {
        Dimension dim10 = new Dimension(0, 10);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        JPanel namePanel = new JPanel();
        JLabel nameLabel = new JLabel("Название таблицы: ", JLabel.LEFT);
        namePanel.add(nameLabel);
        if (curTable == null) {
            nameField = new JTextField(20);
            nameLabel.setLabelFor(nameField);
            namePanel.add(nameField);
        } else {
            JLabel name = new JLabel(curTable.getName());
            nameLabel.setLabelFor(name);
            namePanel.add(name);
        }
        mainPanel.add(namePanel);

        if (curTable == null) {
            JPanel tableSpacePanel = new JPanel();
            JLabel tablespaceLabel = new JLabel("Выберите tablespace: ", JLabel.LEFT);
            tableSpacePanel.add(tablespaceLabel);
            String[] tablespaceArr = new String[tablespaces.size()];
            tablespaces.toArray(tablespaceArr);
            tablespaceList = new JComboBox(tablespaceArr);
            tablespaceLabel.setLabelFor(tablespaceList);
            tableSpacePanel.add(tablespaceList);
            mainPanel.add(tableSpacePanel);
        }

        configureAddColumnPart(mainPanel);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        JLabel constraintsLabel = new JLabel("Ограничения", JLabel.CENTER);
        constraintsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(constraintsLabel);
        mainPanel.add(Box.createRigidArea(dim10));

        constraintsListModel = new DefaultListModel<>();
        JList<Constraint> constraintsList = new JList<>(constraintsListModel);

        constraintsList.setLayoutOrientation(JList.VERTICAL);
        constraintsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        constraintsList.setVisibleRowCount(5);

        configureList(constraintsList, constraintsListModel, false);

        if (curTable != null) {
            try {
                List<Constraint> constraints = tableInfoGetter.getConstraints(curTable.getName());
                for (Constraint c : constraints)
                    constraintsListModel.addElement(c);
            } catch (SQLException exc) {
                JOptionPane.showMessageDialog(null,
                        exc.getLocalizedMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        JScrollPane constraintsScrollPane = new JScrollPane(constraintsList);
        mainPanel.add(constraintsScrollPane);
        mainPanel.add(Box.createRigidArea(dim10));

        mainPanel.add(configureKeysPart());

        if (curTable == null) {
            mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
            JButton addTableButton = new JButton("Добавить таблицу");
            addTableButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (nameField.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(null,
                                "Введите название таблицы.",
                                "Невозможно создать таблицу",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (databaseActionListener != null) {
                        Object[] columnsArr = columnsListModel.toArray();
                        if (columnsArr.length == 0) {
                            JOptionPane.showMessageDialog(null,
                                    "Добавьте в таблицу хотя бы одно поле.",
                                    "Невозможно создать таблицу",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        List<Column> columnsList = new ArrayList<>(columnsArr.length);
                        for (int i = 0; i < columnsArr.length; i++)
                            columnsList.add((Column) columnsArr[i]);

                        Constraint[] constraints;
                        if (constraintsListModel.isEmpty())
                            constraints = null;
                        else {
                            Object[] constrObj = constraintsListModel.toArray();
                            constraints = new Constraint[constrObj.length];
                            for (int i = 0; i < constrObj.length; i++)
                                constraints[i] = (Constraint) constrObj[i];
                        }

                        databaseActionListener.onCreateTable(nameField.getText(),
                                tablespaceList.getSelectedItem().toString(), columnsList,
                                constraints);
                    }
                    dispose();
                }
            });
            addTableButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(addTableButton);
        }

        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));
        add(mainPanel);
    }
    private void configureAddColumnPart(JPanel mainPanel) {
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        JLabel l = new JLabel("Структура таблицы", JLabel.CENTER);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(l);

        columnsListModel = new DefaultListModel<>();

        if (curTable != null) {
            List<Column> cols = curTable.getColumns();
            for (Column column : cols)
                columnsListModel.addElement(column);
        }

        JList<Column> list = new JList<>(columnsListModel);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(5);

        configureList(list, columnsListModel, true);

        JScrollPane tableStructScrollPane = new JScrollPane(list);
        Dimension dim10 = new Dimension(0, 10);
        mainPanel.add(Box.createRigidArea(dim10));
        mainPanel.add(tableStructScrollPane);
        mainPanel.add(Box.createRigidArea(dim10));

        JButton addColumnButton = new JButton("Добавить поле");
        addColumnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddColumnFrame addColumnFrame = new AddColumnFrame();
                addColumnFrame.setTableCreator(tableFrame);
                addColumnFrame.setVisible(true);
            }
        });
        JPanel addColimnPanel = new JPanel(new BorderLayout());
        addColimnPanel.setMaximumSize(new Dimension(5000, 1000));
        addColimnPanel.add(addColumnButton, BorderLayout.LINE_END);
        mainPanel.add(addColimnPanel);
    }
    private JPanel configureKeysPart() {
        JPanel constraintButtonsPanel = new JPanel();
        JButton primaryKeyButton = new JButton("Добавить первичный ключ");
        primaryKeyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PrimaryKeyFrame primaryKeyFrame = new PrimaryKeyFrame(columnsListModel);
                primaryKeyFrame.setTableCreator(tableFrame);
                primaryKeyFrame.setVisible(true);
            }
        });

        JButton foreignKeyButton = new JButton("Добавить внешний ключ");
        foreignKeyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ForeignKeyFrame foreignKeyFrame;
                    if (curTable != null) {
                        foreignKeyFrame = new ForeignKeyFrame(
                                columnsListModel.toArray(),
                                tableInfoGetter.getTablespaceTables(curTable.getTablespace()),
                                tableInfoGetter
                        );
                    } else {
                        foreignKeyFrame = new ForeignKeyFrame(
                                columnsListModel.toArray(),
                                tableInfoGetter.getTablespaceTables(tablespaceList.getSelectedItem().toString()),
                                tableInfoGetter
                        );
                    }
                    foreignKeyFrame.setTableCreator(tableFrame);
                    foreignKeyFrame.setVisible(true);
                } catch (SQLException exc) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Невозможно добавить внешний ключ: " + exc.getLocalizedMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        JButton uniqueButton = new JButton("Сделать поле UNIQUE");
        uniqueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UniqueFrame uniqueFrame = new UniqueFrame(columnsListModel);
                uniqueFrame.setTableCreator(tableFrame);
                uniqueFrame.setVisible(true);
            }
        });

        constraintButtonsPanel.add(primaryKeyButton);
        constraintButtonsPanel.add(foreignKeyButton);
        constraintButtonsPanel.add(uniqueButton);

        return constraintButtonsPanel;
    }

    @Override
    public boolean addColumn(Column column) {
        Object[] exColumns = columnsListModel.toArray();
        for (int i = 0; i < exColumns.length; i++) {
            if (((Column) exColumns[i]).getName().equals(column.getName()))
                return false;
        }

        if (curTable != null) {
            try {
                tableModifier.addColumn(curTable.getName(), column);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        e.getLocalizedMessage(),
                        "Ошибка",
                        JOptionPane.WARNING_MESSAGE);
                return true;
            }
        }
        columnsListModel.addElement(column);
        return true;
    }

    @Override
    public void addPrimaryKey(String name, List<Column> columns) {
        if (hasConstraintNamedLikeThat(name)) {
            JOptionPane.showMessageDialog(
                    null,
                    "Ограничение с таким названием уже существует",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (hasPrimaryKey) {
            int n = JOptionPane.showConfirmDialog(
                    null,
                    "Первичный ключ уже задан. Хотите заменить его?",
                    "Замена первичного ключа",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (n == JOptionPane.NO_OPTION)
                return;
            //Удалить прежний ключ
            Object[] constraints = constraintsListModel.toArray();
            for (int i = 0; i < constraints.length; i++)
                if (((Constraint) constraints[i]).getType() == Constraint.ConstraintType.PRIMARY_KEY) {
                    constraintsListModel.remove(i);
                    break;
                }
        } else
            hasPrimaryKey = true;

        Column[] col = new Column[columns.size()];
        columns.toArray(col);
        Constraint constraint = new Constraint(
                Constraint.ConstraintType.PRIMARY_KEY, name, col);
        if (curTable != null) {
            if (!applyConstr(constraint))
                return;
        }
        constraintsListModel.addElement(constraint);
    }

    @Override
    public void addUniqueConstraint(String name, List<Column> columns) {
        if (hasConstraintNamedLikeThat(name)) {
            JOptionPane.showMessageDialog(
                    null,
                    "Ограничение с таким названием уже существует",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        Column[] colArray = new Column[columns.size()];
        columns.toArray(colArray);
        Constraint constraint = new Constraint(
                Constraint.ConstraintType.UNIQUE, name, colArray);
        if (curTable != null) {
            if (!applyConstr(constraint))
                return;
        }
        constraintsListModel.addElement(constraint);
    }

    @Override
    public void addForeignKey(String name, Column[] columns, String refTable, Column[] refColumns) {
        if (hasConstraintNamedLikeThat(name)) {
            JOptionPane.showMessageDialog(
                    null,
                    "Ограничение с таким названием уже существует",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        Constraint constraint = new ForeignKey(
                name, columns, refTable, refColumns);
        if (curTable != null) {
            if (!applyConstr(constraint))
                return;
        }
        constraintsListModel.addElement(constraint);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
    }

    private boolean hasConstraintNamedLikeThat(String name) {
        for (int i = 0; i < constraintsListModel.size(); i++) {
            String curConstrName = constraintsListModel.get(i).getName();
            if (curConstrName.equals(name))
                return true;
        }
        return false;
    }

    private void configureList(JList configList, DefaultListModel model, boolean isColumnList) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Удалить");

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = configList.getSelectedIndex();
                if (index == -1)
                    return;
                if (curTable != null) {
                    try {
                        if (isColumnList)
                            tableModifier.removeColumn(curTable.getName(), (Column) configList.getSelectedValue());
                        else
                            tableModifier.dropConstraint(curTable.getName(),
                                    ((Constraint) configList.getSelectedValue()).getName());
                    } catch (SQLException exc) {
                        JOptionPane.showMessageDialog(null,
                                exc.getLocalizedMessage(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                model.removeElementAt(index);
            }
        });
        menu.add(item);

        configList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (SwingUtilities.isRightMouseButton(e)) {
                    JList list = (JList) e.getSource();
                    int row = list.locationToIndex(e.getPoint());
                    list.setSelectedIndex(row);
                    menu.show(list, e.getX(), e.getY());
                }
            }
        });
    }
    private boolean applyConstr(Constraint constraint) {
        try {
            tableModifier.applyConstraint(curTable.getName(), constraint);
        } catch (SQLException exc) {
            JOptionPane.showMessageDialog(
                    null,
                    exc.getLocalizedMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        return true;
    }
}
