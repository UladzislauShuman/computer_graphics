package shuman.uladzilau;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Arrays;

/**
 * Лабораторная работа №2 по Компьютерной Графике
 * Вариант: 3
 *
 * Задачи:
 * 1. Реализовать методы повышения контраста:
 *    - Линейное контрастирование.
 *    - Эквализация (выравнивание) гистограммы для полутоновых и цветных изображений (в RGB и HSV).
 * 2. Реализовать высокочастотный фильтр для увеличения резкости.
 * 3. Создать GUI на Java Swing для демонстрации работы.
 *
 * Все алгоритмы реализованы в строгом соответствии с предоставленными лекциями.
 */
public class ImageProcessingApp extends JFrame {

    private BufferedImage originalImage;
    private BufferedImage processedImage;

    private ImagePanel originalImagePanel;
    private ImagePanel processedImagePanel;

    public ImageProcessingApp() {
        setTitle("Лабораторная работа №2 - Вариант 3");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Панели для изображений
        JPanel imagePanelsContainer = new JPanel(new GridLayout(1, 2, 10, 10));
        originalImagePanel = new ImagePanel("Исходное изображение");
        processedImagePanel = new ImagePanel("Обработанное изображение");
        imagePanelsContainer.add(originalImagePanel);
        imagePanelsContainer.add(processedImagePanel);

        // Панель управления с кнопками
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton linearContrastButton = new JButton("Линейное контрастирование");
        linearContrastButton.addActionListener(this::onLinearContrast);
        
        JButton equalizeRgbButton = new JButton("Эквализация (RGB)");
        equalizeRgbButton.addActionListener(this::onEqualizeRgb);

        JButton equalizeHsvButton = new JButton("Эквализация (HSV)");
        equalizeHsvButton.addActionListener(this::onEqualizeHsv);

        JButton sharpenButton = new JButton("Увеличить резкость");
        sharpenButton.addActionListener(this::onSharpen);

        controlPanel.add(linearContrastButton);
        controlPanel.add(equalizeRgbButton);
        controlPanel.add(equalizeHsvButton);
        controlPanel.add(sharpenButton);

        // Меню
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");
        JMenuItem openItem = new JMenuItem("Открыть изображение...");
        openItem.addActionListener(this::onOpenFile);
        JMenuItem resetItem = new JMenuItem("Сбросить");
        resetItem.addActionListener(this::onReset);
        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.add(resetItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        add(imagePanelsContainer, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // --- Обработчики событий кнопок ---

    private void onOpenFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("./src/main/resources/test_images"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Изображения (jpg, png, bmp)", "jpg", "jpeg", "png", "bmp");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(selectedFile);
                onReset(null); // Сбрасываем изображение к только что открытому
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при чтении файла.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onReset(ActionEvent e) {
        if (originalImage != null) {
            // Создаем копию, чтобы не изменять оригинал при обработке
            processedImage = deepCopy(originalImage);
            originalImagePanel.setImage(originalImage);
            processedImagePanel.setImage(processedImage);
        }
    }

    private void onLinearContrast(ActionEvent e) {
      if (processedImage == null) {
        return;
      }
        processedImage = applyLinearContrasting(processedImage);
        processedImagePanel.setImage(processedImage);
    }

    private void onEqualizeRgb(ActionEvent e) {
      if (processedImage == null) {
        return;
      }
        processedImage = applyHistogramEqualizationRGB(processedImage);
        processedImagePanel.setImage(processedImage);
    }
    
    private void onEqualizeHsv(ActionEvent e) {
      if (processedImage == null) {
        return;
      }
        processedImage = applyHistogramEqualizationHSV(processedImage);
        processedImagePanel.setImage(processedImage);
    }

    private void onSharpen(ActionEvent e) {
      if (processedImage == null) {
        return;
      }
        processedImage = applySharpenFilter(processedImage);
        processedImagePanel.setImage(processedImage);
    }

    // --- Реализация алгоритмов обработки ---

    /**
     * Метод повышения контраста: Линейное контрастирование.
     * Алгоритм растягивает гистограмму изображения на весь доступный диапазон [0, 255].
     * Формула: f'(m, n) = 255 * (f(m, n) - f_min) / (f_max - f_min)
     * где f_min и f_max - минимальная и максимальная яркости на исходном изображении.
     * Для цветного изображения операция применяется к каждому каналу (R, G, B) независимо.
     */
    private BufferedImage applyLinearContrasting(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = new BufferedImage(width, height, source.getType());

        int[] rMinMax = {255, 0};
        int[] gMinMax = {255, 0};
        int[] bMinMax = {255, 0};

        // 1. Находим минимальные и максимальные значения для каждого канала
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(source.getRGB(x, y));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();

              if (r < rMinMax[0]) {
                rMinMax[0] = r;
              }
              if (r > rMinMax[1]) {
                rMinMax[1] = r;
              }
              if (g < gMinMax[0]) {
                gMinMax[0] = g;
              }
              if (g > gMinMax[1]) {
                gMinMax[1] = g;
              }
              if (b < bMinMax[0]) {
                bMinMax[0] = b;
              }
              if (b > bMinMax[1]) {
                bMinMax[1] = b;
              }
            }
        }

        // 2. Применяем формулу линейного контрастирования к каждому пикселю
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(source.getRGB(x, y));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();

                int newR = (rMinMax[1] - rMinMax[0] == 0) ? r : 255 * (r - rMinMax[0]) / (rMinMax[1] - rMinMax[0]);
                int newG = (gMinMax[1] - gMinMax[0] == 0) ? g : 255 * (g - gMinMax[0]) / (gMinMax[1] - gMinMax[0]);
                int newB = (bMinMax[1] - bMinMax[0] == 0) ? b : 255 * (b - bMinMax[0]) / (bMinMax[1] - bMinMax[0]);
                
                Color newColor = new Color(clamp(newR), clamp(newG), clamp(newB));
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }

    /**
     * Метод повышения контраста: Эквализация гистограммы для цветного изображения в пространстве RGB.
     * Подход заключается в применении алгоритма эквализации к каждому цветовому каналу (R, G, B) по отдельности.
     * Это может привести к искажению цветового баланса, но является одним из двух требуемых подходов.
     */
    private BufferedImage applyHistogramEqualizationRGB(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = new BufferedImage(width, height, source.getType());
        int totalPixels = width * height;

        // 1. Строим гистограммы для каждого канала
        int[] rHistogram = new int[256];
        int[] gHistogram = new int[256];
        int[] bHistogram = new int[256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(source.getRGB(x, y));
                rHistogram[color.getRed()]++;
                gHistogram[color.getGreen()]++;
                bHistogram[color.getBlue()]++;
            }
        }

        // 2. Строим кумулятивные гистограммы (CDF) и таблицы поиска (LUT) для каждого канала
        int[] rLut = createLutFromHistogram(rHistogram, totalPixels);
        int[] gLut = createLutFromHistogram(gHistogram, totalPixels);
        int[] bLut = createLutFromHistogram(bHistogram, totalPixels);

        // 3. Применяем LUT для создания нового изображения
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(source.getRGB(x, y));
                int newR = rLut[color.getRed()];
                int newG = gLut[color.getGreen()];
                int newB = bLut[color.getBlue()];
                Color newColor = new Color(newR, newG, newB);
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }

    /**
     * Метод повышения контраста: Эквализация гистограммы для цветного изображения в пространстве HSV.
     * Этот подход сохраняет цветовой баланс, так как эквализация применяется только к компоненте яркости (V),
     * а тон (H) и насыщенность (S) остаются неизменными.
     */
    private BufferedImage applyHistogramEqualizationHSV(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = new BufferedImage(width, height, source.getType());
        int totalPixels = width * height;

        // 1. Конвертируем изображение в HSV и строим гистограмму для компоненты V (яркость)
        float[][] hsvPixels = new float[totalPixels][3];
        int[] vHistogram = new int[256]; // Яркость V будет представлена в диапазоне 0-255 для удобства

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(source.getRGB(x, y));
                float[] hsv = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
                hsvPixels[y * width + x] = hsv;
                int vValue = (int) (hsv[2] * 255); // hsv[2] это V (в диапазоне 0.0-1.0)
                vHistogram[vValue]++;
            }
        }

        // 2. Создаем LUT для компоненты V
        int[] vLut = createLutFromHistogram(vHistogram, totalPixels);

        // 3. Применяем LUT к компоненте V и конвертируем обратно в RGB
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float[] hsv = hsvPixels[y * width + x];
                int oldV = (int) (hsv[2] * 255);
                float newV = vLut[oldV] / 255.0f; // Преобразуем новое значение обратно в диапазон 0.0-1.0
                
                int rgb = Color.HSBtoRGB(hsv[0], hsv[1], newV);
                result.setRGB(x, y, rgb);
            }
        }
        return result;
    }

    /**
     * Реализация высокочастотного фильтра для увеличения резкости.
     * Увеличение резкости достигается путем применения операции свертки с ядром (маской),
     * которое подчеркивает перепады яркости (высокие частоты).
     * Используется ядро "Лапласиан с усилением", которое является суммой исходного изображения и его лапласиана.
     * Ядро:
     *   [[ 0, -1,  0],
     *    [-1,  5, -1],
     *    [ 0, -1,  0]]
     */
    private BufferedImage applySharpenFilter(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = new BufferedImage(width, height, source.getType());

        float[][] kernel = {
            { 0, -1,  0},
            {-1,  5, -1},
            { 0, -1,  0}
        };

        // Применяем свертку. Пропускаем края (1 пиксель), чтобы не выходить за границы.
        // Края можно было бы обработать отдельно, но для простоты оставим их черными или скопируем.
        // Здесь мы их скопируем.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (y == 0 || y == height - 1 || x == 0 || x == width - 1) {
                    result.setRGB(x, y, source.getRGB(x, y)); // Копируем пиксели на границе
                    continue;
                }

                float sumR = 0, sumG = 0, sumB = 0;

                // Операция свертки
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color pixelColor = new Color(source.getRGB(x + kx, y + ky));
                        float kernelVal = kernel[ky + 1][kx + 1];
                        
                        sumR += pixelColor.getRed() * kernelVal;
                        sumG += pixelColor.getGreen() * kernelVal;
                        sumB += pixelColor.getBlue() * kernelVal;
                    }
                }

                Color newColor = new Color(clamp((int)sumR), clamp((int)sumG), clamp((int)sumB));
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }


    // --- Вспомогательные методы ---

    /**
     * Вспомогательный метод для создания таблицы поиска (LUT) на основе гистограммы.
     * Этот метод инкапсулирует шаги 2 и 3 алгоритма эквализации:
     * - Построение кумулятивной гистограммы (CDF).
     * - Нормализация CDF для получения новых значений яркости.
     */
    private int[] createLutFromHistogram(int[] histogram, int totalPixels) {
        int[] lut = new int[256];
        long[] cdf = new long[256];

        // 1. Строим CDF
        cdf[0] = histogram[0];
        for (int i = 1; i < 256; i++) {
            cdf[i] = cdf[i - 1] + histogram[i];
        }

        // 2. Находим минимальное ненулевое значение в CDF для корректной нормализации
        long cdfMin = 0;
        for (int i = 0; i < 256; i++) {
            if (cdf[i] > 0) {
                cdfMin = cdf[i];
                break;
            }
        }

        // 3. Строим LUT по формуле
        // f_new = round( ((cdf(f_old) - cdf_min) * 255) / (totalPixels - cdf_min) )
        for (int i = 0; i < 256; i++) {
            if (totalPixels - cdfMin == 0) { // Защита от деления на ноль, если изображение одноцветное
                lut[i] = i;
            } else {
                lut[i] = (int) Math.round(((cdf[i] - cdfMin) * 255.0) / (totalPixels - cdfMin));
            }
        }
        return lut;
    }

    /**
     * Ограничивает значение в диапазоне [0, 255].
     */
    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    /**
     * Создает глубокую копию BufferedImage.
     */
    private BufferedImage deepCopy(BufferedImage bi) {
        return new BufferedImage(
            bi.getColorModel(),
            bi.copyData(null),
            bi.isAlphaPremultiplied(),
            null
        );
    }

    /**
     * Главный метод для запуска приложения.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ImageProcessingApp::new);
    }
}

/**
 * Вспомогательный класс-панель для отрисовки изображения и заголовка.
 */
class ImagePanel extends JPanel {
    private BufferedImage image;
    private String title;

    public ImagePanel(String title) {
        this.title = title;
        setPreferredSize(new Dimension(400, 400));
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imgWidth = image.getWidth();
            int imgHeight = image.getHeight();

            double scale = Math.min((double) panelWidth / imgWidth, (double) panelHeight / imgHeight);
            int newImgWidth = (int) (imgWidth * scale);
            int newImgHeight = (int) (imgHeight * scale);

            int x = (panelWidth - newImgWidth) / 2;
            int y = (panelHeight - newImgHeight) / 2;

            g.drawImage(image, x, y, newImgWidth, newImgHeight, null);
        }
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g.drawString(title, (getWidth() - titleWidth) / 2, 20);
    }
}