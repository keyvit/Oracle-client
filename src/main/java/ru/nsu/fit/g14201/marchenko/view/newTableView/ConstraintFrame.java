package ru.nsu.fit.g14201.marchenko.view.newTableView;

import ru.nsu.fit.g14201.marchenko.model.TableCreator;

import javax.swing.*;

/**
 */
abstract class ConstraintFrame extends JFrame {
    protected TableCreator tableCreator = null;

    ConstraintFrame(String title) {
        super(title);
    }

    protected void setTableCreator(TableCreator tableCreator) {
        this.tableCreator = tableCreator;
    }

    protected void configureList(JList list) {
        list.setLayoutOrientation(JList.VERTICAL);
        list.setSelectionModel(new DefaultListSelectionModel() {
            public void setSelectionInterval(int index0, int index1) {
                if (index0 == index1) {
                    if (isSelectedIndex(index0)) {
                        removeSelectionInterval(index0, index0);
                        return;
                    }
                }
                super.setSelectionInterval(index0, index1);
            }

            @Override
            public void addSelectionInterval(int index0, int index1) {
                if (index0 == index1) {
                    if (isSelectedIndex(index0)) {
                        removeSelectionInterval(index0, index0);
                        return;
                    }
                    super.addSelectionInterval(index0, index1);
                }
            }
        });
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(5);
    }
}
