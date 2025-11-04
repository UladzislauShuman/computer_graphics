package com.shuman.uladzislau;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Класс, который описывает саму программу
 *
 * Важная деталь реализации:
 * - у каждого слушателя есть свой метод, который берет данные для своей модели (1 модель -- 1 метод)
 * - и каждый такой метод вызывает метод, который обновляет Все модели (берет Color, переводит в RGB и далее из RGB во все остальные)
 * - все формулы по переводу из одной модели в другую описаны в классе com.shuman.uladzislau.ColorUtils
 */
public class ColorConverterApp extends JFrame {
    private boolean isUpdating = false;

    // Панель для отображения текущего цвета
    private final JPanel colorPreviewPanel = new JPanel();

    // Компоненты для CMYK
    private final JSlider cSlider = new JSlider(0, 100);
    private final JSlider mSlider = new JSlider(0, 100);
    private final JSlider ySlider = new JSlider(0, 100);
    private final JSlider kSlider = new JSlider(0, 100);
    private final JTextField cTextField = new JTextField(3);
    private final JTextField mTextField = new JTextField(3);
    private final JTextField yTextField = new JTextField(3);
    private final JTextField kTextField = new JTextField(3);

    // Компоненты для RGB
    private final JSlider rSlider = new JSlider(0, 255);
    private final JSlider gSlider = new JSlider(0, 255);
    private final JSlider bSlider = new JSlider(0, 255);
    private final JTextField rTextField = new JTextField(3);
    private final JTextField gTextField = new JTextField(3);
    private final JTextField bTextField = new JTextField(3);

    // Компоненты для HLS
    private final JSlider hSlider = new JSlider(0, 360);
    private final JSlider lSlider = new JSlider(0, 100);
    private final JSlider sSlider = new JSlider(0, 100);
    private final JTextField hTextField = new JTextField(3);
    private final JTextField lTextField = new JTextField(3);
    private final JTextField sTextField = new JTextField(3);

    public ColorConverterApp() {
        super("Lab1 нечетный вариант (CMYK-RGB-HLS)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Панель предпросмотра
        colorPreviewPanel.setPreferredSize(new Dimension(100, 100));
        colorPreviewPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.BOTH;
        add(colorPreviewPanel, gbc);

        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Панели для моделей
        gbc.gridx = 2;
        gbc.gridy = 0;
        add(createModelPanel("CMYK", new JComponent[][]{
            {new JLabel("C:"), cSlider, cTextField},
            {new JLabel("M:"), mSlider, mTextField},
            {new JLabel("Y:"), ySlider, yTextField},
            {new JLabel("K:"), kSlider, kTextField}
        }), gbc);

        gbc.gridy = 1;
        add(createModelPanel("RGB", new JComponent[][]{
            {new JLabel("R:"), rSlider, rTextField},
            {new JLabel("G:"), gSlider, gTextField},
            {new JLabel("B:"), bSlider, bTextField}
        }), gbc);

        gbc.gridy = 2;
        add(createModelPanel("HLS", new JComponent[][]{
            {new JLabel("H:"), hSlider, hTextField},
            {new JLabel("L:"), lSlider, lTextField},
            {new JLabel("S:"), sSlider, sTextField}
        }), gbc);

        // выбор цвета
        JButton colorChooserButton = new JButton("Выбрать цвет");
        colorChooserButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(this, "Выберите цвет", colorPreviewPanel.getBackground());
            if (selectedColor != null) {
                updateAll(selectedColor);
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        add(colorChooserButton, gbc);

        addListeners();

        // начальный цвет
        updateAll(new Color(255, 0, 101));

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createModelPanel(String title, JComponent[][] components) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);

        for (int i = 0; i < components.length; i++) {
            gbc.gridy = i;
            gbc.gridx = 0;
            gbc.weightx = 0;
            panel.add(components[i][0], gbc); // Label

            gbc.gridx = 1;
            gbc.weightx = 1;
            panel.add(components[i][1], gbc); // Slider

            gbc.gridx = 2;
            gbc.weightx = 0;
            panel.add(components[i][2], gbc); // TextField
        }
        return panel;
    }

    private void addListeners() {
        ChangeListener sliderListener = e -> {
            if (isUpdating) return;
            JSlider source = (JSlider) e.getSource();
            // Обновляется только тогда, когда пользователь отпустил ползунок
            if (!source.getValueIsAdjusting()) {
                if (source == cSlider || source == mSlider || source == ySlider || source == kSlider) updateFromCmyk(true);
                else if (source == rSlider || source == gSlider || source == bSlider) updateFromRgb(true);
                else if (source == hSlider || source == lSlider || source == sSlider) updateFromHls(true);
            }
        };
        // Для слайдеров
        cSlider.addChangeListener(sliderListener);
        mSlider.addChangeListener(sliderListener);
        ySlider.addChangeListener(sliderListener);
        kSlider.addChangeListener(sliderListener);
        rSlider.addChangeListener(sliderListener);
        gSlider.addChangeListener(sliderListener);
        bSlider.addChangeListener(sliderListener);
        hSlider.addChangeListener(sliderListener);
        lSlider.addChangeListener(sliderListener);
        sSlider.addChangeListener(sliderListener);

        // Для текстовых полей
        addDocumentListener(cTextField, () -> updateFromCmyk(false));
        addDocumentListener(mTextField, () -> updateFromCmyk(false));
        addDocumentListener(yTextField, () -> updateFromCmyk(false));
        addDocumentListener(kTextField, () -> updateFromCmyk(false));

        addDocumentListener(rTextField, () -> updateFromRgb(false));
        addDocumentListener(gTextField, () -> updateFromRgb(false));
        addDocumentListener(bTextField, () -> updateFromRgb(false));

        addDocumentListener(hTextField, () -> updateFromHls(false));
        addDocumentListener(lTextField, () -> updateFromHls(false));
        addDocumentListener(sTextField, () -> updateFromHls(false));
    }

    private void addDocumentListener(JTextField textField, Runnable action) {
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { runAction(); }
            @Override public void removeUpdate(DocumentEvent e) { runAction(); }
            @Override public void changedUpdate(DocumentEvent e) { runAction(); }

            private void runAction() {
                if (isUpdating) return;
                // Откладываем выполнение, чтобы избежать конфликтов с EDT
                SwingUtilities.invokeLater(action);
            }
        });
    }

    private void updateFromCmyk(boolean fromSlider) {
        if (isUpdating) return;
        try {
            float c = (fromSlider ? cSlider.getValue() : Float.parseFloat(cTextField.getText())) / 100f;
            float m = (fromSlider ? mSlider.getValue() : Float.parseFloat(mTextField.getText())) / 100f;
            float y = (fromSlider ? ySlider.getValue() : Float.parseFloat(yTextField.getText())) / 100f;
            float k = (fromSlider ? kSlider.getValue() : Float.parseFloat(kTextField.getText())) / 100f;

            if (c < 0 || c > 1 || m < 0 || m > 1 || y < 0 || y > 1 || k < 0 || k > 1) return;
            updateAll(ColorUtils.cmykToRgb(c, m, y, k));
        } catch (NumberFormatException ignored) {}
    }

    private void updateFromRgb(boolean fromSlider) {
        if (isUpdating) return;
        try {
            int r = fromSlider ? rSlider.getValue() : Integer.parseInt(rTextField.getText());
            int g = fromSlider ? gSlider.getValue() : Integer.parseInt(gTextField.getText());
            int b = fromSlider ? bSlider.getValue() : Integer.parseInt(bTextField.getText());
            updateAll(new Color(r, g, b));
        } catch (IllegalArgumentException ignored) {}
    }

    private void updateFromHls(boolean fromSlider) {
        if (isUpdating) return;
        try {
            float h = fromSlider ? hSlider.getValue() : Float.parseFloat(hTextField.getText());
            float l = (fromSlider ? lSlider.getValue() : Float.parseFloat(lTextField.getText())) / 100f;
            float s = (fromSlider ? sSlider.getValue() : Float.parseFloat(sTextField.getText())) / 100f;

            if (h < 0 || h > 360 || l < 0 || l > 1 || s < 0 || s > 1) return;
            updateAll(ColorUtils.hlsToRgb(h, l, s));
        } catch (NumberFormatException ignored) {}
    }

    private void updateAll(Color color) {
        isUpdating = true;

        colorPreviewPanel.setBackground(color);

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        float[] cmyk = ColorUtils.rgbToCmyk(r, g, b);
        float[] hls = ColorUtils.rgbToHls(r, g, b);

        rSlider.setValue(r); rTextField.setText(String.valueOf(r));
        gSlider.setValue(g); gTextField.setText(String.valueOf(g));
        bSlider.setValue(b); bTextField.setText(String.valueOf(b));

        cSlider.setValue(Math.round(cmyk[0] * 100)); cTextField.setText(String.valueOf(Math.round(cmyk[0] * 100)));
        mSlider.setValue(Math.round(cmyk[1] * 100)); mTextField.setText(String.valueOf(Math.round(cmyk[1] * 100)));
        ySlider.setValue(Math.round(cmyk[2] * 100)); yTextField.setText(String.valueOf(Math.round(cmyk[2] * 100)));
        kSlider.setValue(Math.round(cmyk[3] * 100)); kTextField.setText(String.valueOf(Math.round(cmyk[3] * 100)));

        hSlider.setValue(Math.round(hls[0])); hTextField.setText(String.valueOf(Math.round(hls[0])));
        lSlider.setValue(Math.round(hls[1] * 100)); lTextField.setText(String.valueOf(Math.round(hls[1] * 100)));
        sSlider.setValue(Math.round(hls[2] * 100)); sTextField.setText(String.valueOf(Math.round(hls[2] * 100)));

        isUpdating = false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ColorConverterApp::new);
    }
}