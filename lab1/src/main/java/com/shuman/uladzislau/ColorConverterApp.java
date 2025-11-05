package com.shuman.uladzislau;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class ColorConverterApp extends JFrame {

    private boolean isUpdating = false;

    private float lastHue = 0;
    private float lastSaturation = 0;

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

    // Компоненты для модели RGB
    private final JSlider rSlider = new JSlider(0, 255);
    private final JSlider gSlider = new JSlider(0, 255);
    private final JSlider bSlider = new JSlider(0, 255);
    private final JTextField rTextField = new JTextField(3);
    private final JTextField gTextField = new JTextField(3);
    private final JTextField bTextField = new JTextField(3);

    // Компоненты для модели HLS
    private final JSlider hSlider = new JSlider(0, 359);
    private final JSlider lSlider = new JSlider(0, 100);
    private final JSlider sSlider = new JSlider(0, 100);
    private final JTextField hTextField = new JTextField(3);
    private final JTextField lTextField = new JTextField(3);
    private final JTextField sTextField = new JTextField(3);

    public ColorConverterApp() {
        super("Конвертер цветовых моделей (CMYK-RGB-HLS)");
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

        // Кнопка выбора цвета
        JButton colorChooserButton = new JButton("Выбрать цвет...");
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
            if (!source.getValueIsAdjusting()) {
                if (source == cSlider || source == mSlider || source == ySlider || source == kSlider) updateFromCmyk(true);
                else if (source == rSlider || source == gSlider || source == bSlider) updateFromRgb(true);
                else if (source == hSlider || source == lSlider || source == sSlider) updateFromHls(true);
            }
        };

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

            this.lastHue = h;
            this.lastSaturation = s * 100;

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

        // Обновляем RGB
        rSlider.setValue(r); rTextField.setText(String.valueOf(r));
        gSlider.setValue(g); gTextField.setText(String.valueOf(g));
        bSlider.setValue(b); bTextField.setText(String.valueOf(b));

        // Обновляем CMYK
        cSlider.setValue(Math.round(cmyk[0] * 100)); cTextField.setText(String.valueOf(Math.round(cmyk[0] * 100)));
        mSlider.setValue(Math.round(cmyk[1] * 100)); mTextField.setText(String.valueOf(Math.round(cmyk[1] * 100)));
        ySlider.setValue(Math.round(cmyk[2] * 100)); yTextField.setText(String.valueOf(Math.round(cmyk[2] * 100)));
        kSlider.setValue(Math.round(cmyk[3] * 100)); kTextField.setText(String.valueOf(Math.round(cmyk[3] * 100)));

        float calculatedL = hls[1];
        float calculatedS = hls[2];

        lSlider.setValue(Math.round(calculatedL * 100));
        lTextField.setText(String.valueOf(Math.round(calculatedL * 100)));

        if (calculatedL == 0.0f || calculatedL == 1.0f || calculatedS == 0.0f) {
            hSlider.setValue(Math.round(this.lastHue));
            hTextField.setText(String.valueOf(Math.round(this.lastHue)));
            sSlider.setValue(Math.round(this.lastSaturation));
            sTextField.setText(String.valueOf(Math.round(this.lastSaturation)));
        } else {
            float calculatedH = hls[0];
            hSlider.setValue(Math.round(calculatedH));
            hTextField.setText(String.valueOf(Math.round(calculatedH)));
            sSlider.setValue(Math.round(calculatedS * 100));
            sTextField.setText(String.valueOf(Math.round(calculatedS * 100)));

            this.lastHue = calculatedH;
            this.lastSaturation = calculatedS * 100;
        }

        isUpdating = false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ColorConverterApp::new);
    }
}