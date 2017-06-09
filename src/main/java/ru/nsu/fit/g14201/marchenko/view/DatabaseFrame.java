package ru.nsu.fit.g14201.marchenko.view;

import ru.nsu.fit.g14201.marchenko.model.Table;
import ru.nsu.fit.g14201.marchenko.model.TableInfoGetter;
import ru.nsu.fit.g14201.marchenko.model.TableModifier;
import ru.nsu.fit.g14201.marchenko.utils.TableColumnAdjuster;
import ru.nsu.fit.g14201.marchenko.view.newTableView.TableFrame;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.*;
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
public class DatabaseFrame extends JFrame implements TreeSelectionListener {
    private TablePanel tablePanel;
    private JTree hierarchy;
    private Color tableLinesColor = new Color(0x9D9D9D);

    private DatabaseActionListener listener = null;

    public DatabaseFrame(Map<String, List<String>> tableHierarchy, TableInfoGetter tableInfoGetter,
                         TableModifier tableModifier) {
        super("Клиент Oracle");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setJMenuBar(createMenuBar());
        setLayout(new BorderLayout());

        JScrollPane tableListPanel = createTableListPart(tableHierarchy, tableInfoGetter, tableModifier);
        add(tableListPanel, BorderLayout.WEST);
        tablePanel = new TablePanel();
        tablePanel.setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableListPanel, tablePanel);
        add(splitPane, BorderLayout.CENTER);

        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
    }

    public void setDatabaseActionListener(DatabaseActionListener listener) {
        this.listener = listener;
    }

    public void addTable(Table table) {
        tablePanel.removeAll();
        JTable jTable = new JTable(table);
        jTable.setGridColor(tableLinesColor);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table);
        jTable.setRowSorter(sorter);

        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnAdjuster tca = new TableColumnAdjuster(jTable);
        tca.adjustColumns();

        JScrollPane sp = new JScrollPane(jTable);
        tablePanel.add(sp, BorderLayout.CENTER);
        addTablePopupMenu(jTable, sp);
        tablePanel.revalidate();
    }
    public void insertIntoHierarchy(String tablespace, String table) {
        DefaultTreeModel model = (DefaultTreeModel) hierarchy.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

        int childrenNum = root.getChildCount();
        for (int i = 0; i < childrenNum; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
            if (node.getUserObject().equals(tablespace)) {
                model.insertNodeInto(new DefaultMutableTreeNode(table),
                        node, childrenNum);
                return;
            }
        }
    }

    private JScrollPane createTableListPart(Map<String, List<String>> tableHierarchy, TableInfoGetter tableInfoGetter,
                                            TableModifier tableModifier) {
        hierarchy = new JTree(createTree(tableHierarchy));
        hierarchy.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        hierarchy.addTreeSelectionListener(this);
        JScrollPane tableListPanel = new JScrollPane(hierarchy);
        tableListPanel.setMinimumSize(new Dimension(200, 600));

        JPopupMenu tableMenu = new JPopupMenu();
        JMenuItem removeItem = new JMenuItem("Удалить");
        removeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = hierarchy.getSelectionPath();
                listener.onDropTable((String) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject());
                DefaultTreeModel model = (DefaultTreeModel) hierarchy.getModel();
                model.removeNodeFromParent((DefaultMutableTreeNode) path.getLastPathComponent());
                tablePanel.removeAll();
                tablePanel.revalidate();
                tablePanel.repaint();
            }
        });
        tableMenu.add(removeItem);

        JMenuItem alterItem = new JMenuItem("Изменить");
        alterItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = hierarchy.getSelectionPath();
                try {
                    TableFrame tableFrame = new TableFrame(tableInfoGetter, tableModifier,
                            (String) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject());
                    tableFrame.setDatabaseActionListener(listener);
                    tableFrame.setVisible(true);
                } catch (SQLException exc) {
                    JOptionPane.showMessageDialog(null,
                            exc.getLocalizedMessage(),
                            "Ошибка",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        tableMenu.add(alterItem);


        hierarchy.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (SwingUtilities.isRightMouseButton(e)) {
                    int selRow = hierarchy.getRowForLocation(e.getX(), e.getY());
                    TreePath selPath = hierarchy.getPathForLocation(e.getX(), e.getY());
                    hierarchy.setSelectionPath(selPath);
                    if (selRow > -1 && selPath.getPathCount() == 3) {
                        hierarchy.setSelectionRow(selRow);
                        tableMenu.show(hierarchy, e.getX(), e.getY());
                    }
                }
            }
        });
        return tableListPanel;
    }

    private void addTablePopupMenu(JTable jTable, JScrollPane tablePanel) {
        JPopupMenu insideTableMenu = new JPopupMenu();
        JMenuItem addRowItem = new JMenuItem("Добавить запись");
        addRowItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Table table = (Table) jTable.getModel();
                NewRecordFrame newRecordFrame = new NewRecordFrame(table.getColumns(), table);
                newRecordFrame.setVisible(true);
            }
        });
        insideTableMenu.add(addRowItem);


        JMenuItem delRowItem = new JMenuItem("Удалить запись");
        delRowItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = jTable.getSelectedRow();
                ((DefaultTableModel) jTable.getModel()).removeRow(
                        jTable.getRowSorter().convertRowIndexToModel(row)
                );
                tablePanel.revalidate();
                tablePanel.repaint();
            }
        });
        insideTableMenu.add(delRowItem);

        JMenuItem formReportItem = new JMenuItem("Вывести в виде отчёта");
        formReportItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel tableModel = (DefaultTableModel) jTable.getModel();
                ReportFrame reportFrame = new ReportFrame(tableModel);
                reportFrame.setVisible(true);
            }
        });
        insideTableMenu.add(formReportItem);

        jTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = jTable.rowAtPoint(e.getPoint());
                    jTable.getSelectionModel().setSelectionInterval(row, row);
                    insideTableMenu.show(jTable, e.getX(), e.getY());
                }
            }
        });

        JPopupMenu outsideTableMenu = new JPopupMenu();
        JMenuItem addRowOutItem = new JMenuItem("Добавить запись");
        addRowOutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Table table = (Table) jTable.getModel();
                NewRecordFrame newRecordFrame = new NewRecordFrame(table.getColumns(), table);
                newRecordFrame.setVisible(true);
            }
        });
        outsideTableMenu.add(addRowOutItem);
        tablePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (SwingUtilities.isRightMouseButton(e)) {
                    outsideTableMenu.show(tablePanel, e.getX(), e.getY());
                }
            }
        });
    }
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu toolsMenu = new JMenu("Инструменты");
        JMenuItem newTable = new JMenuItem("Создать таблицу");
        toolsMenu.add(newTable);

        newTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.onRequireTablespaces();
            }
        });

        JMenuItem requestEditor = new JMenuItem("Открыть редактор запросов");
        requestEditor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                QueryFrame queryFrame = new QueryFrame(listener);
                queryFrame.setVisible(true);
            }
        });
        toolsMenu.add(requestEditor);

        menuBar.add(toolsMenu);

        return menuBar;
    }
    private DefaultMutableTreeNode createTree(Map<String, List<String>> tableHierarchy) {
        if (tableHierarchy == null) { //FIXME To delete
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("RootTest");
            DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("Child 1");
            DefaultMutableTreeNode child2 = new DefaultMutableTreeNode("Child 2");
            root.add(child1);
            root.add(child2);
            return root;
        }

        DefaultMutableTreeNode globalRoot = new DefaultMutableTreeNode("Tablespaces");

        for (Map.Entry<String, List<String>> tablespace : tableHierarchy.entrySet()) {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(tablespace.getKey());
            List<String> tables = tablespace.getValue();
            for (String table : tables)
                root.add(new DefaultMutableTreeNode(table));
            globalRoot.add(root);
        }

        return globalRoot;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        if (e.getPath().getPathCount() != 3 || e.getNewLeadSelectionPath() == null)
            return;

        if (listener != null)
            listener.onLoadTable((String) ((DefaultMutableTreeNode)
                    e.getPath().getLastPathComponent()).getUserObject());
    }
}
