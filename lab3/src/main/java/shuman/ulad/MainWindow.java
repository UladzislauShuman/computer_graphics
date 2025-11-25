package shuman.ulad;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainWindow extends JFrame {

    private static final boolean IS_LOG = true;

    private GridPanel canvasPanel;
    private JComboBox<String> algorithmBox;
    private JTextField x1Field, y1Field, x2Field, y2Field, rField;
    private JLabel labelX2, labelY2, labelR, timeLabel;
    private JTextArea logArea;
    private JSlider zoomSlider;

    public MainWindow() {
        setTitle("Лабораторная работа 3: Растровые алгоритмы");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Левая панель (Управление) ---
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(320, 0));

        // Выбор алгоритма
        controlPanel.add(new JLabel("Алгоритм:"));
        String[] algos = {
                "Пошаговый алгоритм",
                "Алгоритм ЦДА",
                "Брезенхем (Отрезок)",
                "Брезенхем (Окружность)"
        };
        algorithmBox = new JComboBox<>(algos);
        algorithmBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        algorithmBox.addActionListener(e -> updateInputFields());
        controlPanel.add(algorithmBox);
        controlPanel.add(Box.createVerticalStrut(10));

        // Поля ввода координат
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        inputPanel.add(new JLabel("X1 (Xc):"));
        x1Field = new JTextField("0");
        inputPanel.add(x1Field);

        inputPanel.add(new JLabel("Y1 (Yc):"));
        y1Field = new JTextField("0");
        inputPanel.add(y1Field);

        labelX2 = new JLabel("X2:");
        inputPanel.add(labelX2);
        x2Field = new JTextField("10");
        inputPanel.add(x2Field);

        labelY2 = new JLabel("Y2:");
        inputPanel.add(labelY2);
        y2Field = new JTextField("5");
        inputPanel.add(y2Field);

        labelR = new JLabel("Радиус R:");
        inputPanel.add(labelR);
        rField = new JTextField("10");
        inputPanel.add(rField);

        controlPanel.add(inputPanel);
        controlPanel.add(Box.createVerticalStrut(15));

        // Кнопки
        JButton drawButton = new JButton("Нарисовать");
        drawButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        drawButton.addActionListener(e -> draw());
        controlPanel.add(drawButton);

        controlPanel.add(Box.createVerticalStrut(5));

        JButton clearButton = new JButton("Очистить");
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearButton.addActionListener(e -> {
            canvasPanel.clear();
            logArea.setText("");
            timeLabel.setText("Время: -");
        });
        controlPanel.add(clearButton);

        controlPanel.add(Box.createVerticalStrut(20));

        // Слайдер масштаба
        controlPanel.add(new JLabel("Масштаб сетки (Zoom):"));
        zoomSlider = new JSlider(5, 60, 20);
        zoomSlider.addChangeListener(e -> {
            canvasPanel.setCellSize(zoomSlider.getValue());
        });
        controlPanel.add(zoomSlider);
        
        // Подсказка про мышь
        JLabel hintLabel = new JLabel("<html><i>Подсказка: двигайте холст<br>мышкой (Drag & Drop)</i></html>");
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(hintLabel);

        controlPanel.add(Box.createVerticalStrut(20));

        // Вывод времени и логов
        timeLabel = new JLabel("Время: -");
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        timeLabel.setForeground(new Color(0, 100, 0));
        controlPanel.add(timeLabel);
        
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(new JLabel("Лог вычислений (первые шаги):"));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        controlPanel.add(scrollPane);

        add(controlPanel, BorderLayout.WEST);

        // --- Центральная часть (Холст) ---
        canvasPanel = new GridPanel();
        add(canvasPanel, BorderLayout.CENTER);

        updateInputFields(); // Настройка видимости полей при запуске
    }

    private void updateInputFields() {
        String selected = (String) algorithmBox.getSelectedItem();
        boolean isCircle = selected.contains("Окружность");

        labelX2.setVisible(!isCircle);
        x2Field.setVisible(!isCircle);
        labelY2.setVisible(!isCircle);
        y2Field.setVisible(!isCircle);
        
        labelR.setVisible(isCircle);
        rField.setVisible(isCircle);
        
        // Обновляем панель, чтобы поля исчезли/появились
        x2Field.getParent().revalidate();
        x2Field.getParent().repaint();
    }

    private void draw() {
        try {
            int x1 = Integer.parseInt(x1Field.getText());
            int y1 = Integer.parseInt(y1Field.getText());
            
            String selected = (String) algorithmBox.getSelectedItem();
            AlgorithmResult result = null;

            long startTime = System.nanoTime();

            if (selected.contains("Окружность")) {
                int r = Integer.parseInt(rField.getText());
                if (r <= 0) {
                    JOptionPane.showMessageDialog(this, "Радиус должен быть > 0");
                    return;
                }
                result = Algorithms.bresenhamCircle(x1, y1, r, IS_LOG);
            } else {
                int x2 = Integer.parseInt(x2Field.getText());
                int y2 = Integer.parseInt(y2Field.getText());

                if (selected.contains("Пошаговый")) {
                    result = Algorithms.stepByStep(x1, y1, x2, y2, IS_LOG);
                } else if (selected.contains("ЦДА")) {
                    result = Algorithms.dda(x1, y1, x2, y2, IS_LOG);
                } else if (selected.contains("Брезенхем")) {
                    result = Algorithms.bresenhamLine(x1, y1, x2, y2, IS_LOG);
                }
            }

            long endTime = System.nanoTime();
            
            if (result != null) {
                canvasPanel.setPixels(result.pixels);
                logArea.setText(result.log.toString());
                // Форматируем время для красоты
                long duration = endTime - startTime;
                String timeStr = duration < 10000 ? duration + " нс" : (duration / 1000) + " мкс";
                timeLabel.setText("Время: " + timeStr);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, введите корректные целые числа!");
        }
    }
}