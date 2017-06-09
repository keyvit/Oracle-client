package ru.nsu.fit.g14201.marchenko.view;

import ru.nsu.fit.g14201.marchenko.utils.SpringUtilities;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.StringTokenizer;


public class AuthorizationWindow extends JFrame implements ActionListener {
    private JPanel mainPanel;
    private DatabaseActionListener listener = null;

    private JFormattedTextField[] fields;
    private JPasswordField passField;

    public AuthorizationWindow(byte[] IP, int port, String username, String password) {
        super("Авторизация");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        configureMainPanel(IP, port, username, password);

        pack();
        setLocationRelativeTo(null);
    }

    private void configureMainPanel(byte[] defaultIP, int defaultPort, String defaultUsername,
                                    String defaultPassword) {
        String[] labelText = {"IP ", "Порт ", "Логин ", "Пароль "};
        int numPairs = labelText.length;

        fields = new JFormattedTextField[3];
        fields[0] = new JFormattedTextField(new IPAddressFormatter());
        fields[0].setValue(defaultIP);

        NumberFormatter portFormatter = new NumberFormatter(NumberFormat.getIntegerInstance());
        portFormatter.setMinimum(0);
        portFormatter.setMaximum(65535);
        fields[1] = new JFormattedTextField(portFormatter);
        fields[1].setValue(defaultPort);

        fields[2] = new JFormattedTextField(defaultUsername);
        passField = new JPasswordField(defaultPassword);

        JLabel[] labels = new JLabel[4];
        JPanel p = new JPanel(new SpringLayout());
        for (int i = 0; i < numPairs - 1; i++) {
            labels[i] = new JLabel(labelText[i], JLabel.LEFT);
            p.add(labels[i]);
            fields[i].setColumns(10);
            labels[i].setLabelFor(fields[i]);
            p.add(fields[i]);
        }
        labels[3] = new JLabel(labelText[3], JLabel.LEFT);
        p.add(labels[3]);
        labels[3].setLabelFor(passField);
        p.add(passField);

        SpringUtilities.makeCompactGrid(p,
                numPairs, 2, //rows, cols
                6, 6, //initX, initY
                6, 6); //xPad, yPad
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(p);
        JPanel p2 = new JPanel();
        JButton loginButton = new JButton("Войти");
        loginButton.addActionListener(this);
        p2.add(loginButton);
        mainPanel.add(p2);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(mainPanel);
    }

    public void setDatabaseActionListener(DatabaseActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (listener != null) {
            StringBuilder stringBuilder = new StringBuilder();
            byte[] ipBytes = (byte[]) fields[0].getValue();
            for (int i = 0; i < ipBytes.length - 1; i++) {
                stringBuilder.append(ipBytes[i]);
                stringBuilder.append(".");
            }
            stringBuilder.append(ipBytes[ipBytes.length - 1]);

            listener.onAuthorize(stringBuilder.toString(),
                    (int) fields[1].getValue(),
                    (String) fields[2].getValue(),
                    new String(passField.getPassword()));
        }
        dispose();
    }

    class IPAddressFormatter extends DefaultFormatter {
        IPAddressFormatter() {
            setOverwriteMode(false);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (!(value instanceof byte[])) throw new ParseException("Not a byte[]", 0);
            byte[] a = (byte[]) value;
            if (a.length != 4) throw new ParseException("Length != 4", 0);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                int b = a[i];
                if (b < 0) b += 256;
                builder.append(String.valueOf(b));
                if (i < 3) builder.append('.');
            }
            return builder.toString();
        }
        @Override
        public Object stringToValue(String text) throws ParseException {
            StringTokenizer tokenizer = new StringTokenizer(text, ".");
            byte[] a = new byte[4];
            for (int i = 0; i < 4; i++) {
                int b = 0;
                if (!tokenizer.hasMoreTokens()) throw new ParseException("Too few bytes", 0);
                try {
                    b = Integer.parseInt(tokenizer.nextToken());
                }
                catch (NumberFormatException e) {
                    throw new ParseException("Not an integer", 0);
                }
                if (b < 0 || b >= 256) throw new ParseException("Byte out of range", 0);
                a[i] = (byte) b;
            }
            if (tokenizer.hasMoreTokens()) throw new ParseException("Too many bytes", 0);
            return a;
        }
    }
}
