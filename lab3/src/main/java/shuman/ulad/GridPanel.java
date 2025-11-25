package shuman.ulad;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class GridPanel extends JPanel {
    private int cellSize = 20;
    private List<Point> pixels = new ArrayList<>();
    
    // Смещение начала координат (для перетаскивания мышью)
    private int offsetX = 0;
    private int offsetY = 0;
    
    // Переменные для хранения позиции мыши при перетаскивании
    private int lastMouseX;
    private int lastMouseY;

    public GridPanel() {
        // Обработчики событий мыши
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Запоминаем, где нажали кнопку
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // Вычисляем разницу и обновляем смещение
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;
                
                offsetX += dx;
                offsetY += dy;
                
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                
                repaint(); // Перерисовываем
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    public void setCellSize(int size) {
        this.cellSize = size;
        repaint();
    }

    public void setPixels(List<Point> pixels) {
        this.pixels = pixels;
        repaint();
    }

    public void clear() {
        pixels.clear();
        // При очистке можно сбрасывать позицию в центр, а можно не сбрасывать.
        // Раскомментируй строки ниже, если хочешь сброс позиции:
        // offsetX = 0;
        // offsetY = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();

        // Вычисляем экранные координаты начала системы координат (0,0)
        // w/2 и h/2 - это центр окна, плюс наше смещение мышью
        int originX = (w / 2) + offsetX;
        int originY = (h / 2) + offsetY;

        // 1. Рисуем сетку
        g2.setColor(new Color(230, 230, 230));
        
        // Рисуем вертикальные линии от центра вправо и влево
        // Используем originX как точку отсчета, чтобы сетка "ехала" вместе с осями
      for (int x = originX; x < w; x += cellSize) {
        g2.drawLine(x, 0, x, h);
      }
      for (int x = originX; x > 0; x -= cellSize) {
        g2.drawLine(x, 0, x, h);
      }
        
        // Рисуем горизонтальные линии от центра вверх и вниз
      for (int y = originY; y < h; y += cellSize) {
        g2.drawLine(0, y, w, y);
      }
      for (int y = originY; y > 0; y -= cellSize) {
        g2.drawLine(0, y, w, y);
      }

        // 2. Рисуем оси координат
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(0, originY, w, originY); // Ось X
        g2.drawLine(originX, 0, originX, h); // Ось Y

        // Подписи осей
        g2.drawString("X", w - 20, originY - 5);
        g2.drawString("Y", originX + 5, 15);

        // 3. Рисуем закрашенные пиксели
        g2.setColor(Color.BLUE);
        for (Point p : pixels) {
            // Преобразование: Логические координаты -> Экранные координаты
            // X растет вправо, Y растет вверх (поэтому минус)
            int screenX = originX + p.x * cellSize;
            int screenY = originY - p.y * cellSize - cellSize; 
            // -cellSize нужно, так как прямоугольник рисуется от верхнего левого угла

            // Рисуем только если пиксель попадает в видимую область (оптимизация)
            if (screenX + cellSize > 0 && screenX < w && screenY + cellSize > 0 && screenY < h) {
                g2.fillRect(screenX + 1, screenY + 1, cellSize - 1, cellSize - 1);
            }
        }

        // 4. Рисуем деления и цифры на осях
        g2.setColor(Color.GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        
        // Рисуем деления каждые 5 клеток
        // Диапазон берем с запасом, чтобы при сдвиге цифры не исчезали
        int range = Math.max(w, h) / cellSize; 
        
        for (int i = -range; i <= range; i += 5) {
          if (i == 0) {
            continue; // Ноль не рисуем, он в центре пересечения
          }

            int tickX = originX + i * cellSize;
            int tickY = originY - i * cellSize;

            // Деления на оси X
            if (tickX > 0 && tickX < w) {
                g2.drawLine(tickX, originY - 3, tickX, originY + 3);
                g2.drawString(String.valueOf(i), tickX - 5, originY + 15);
            }
            
            // Деления на оси Y
            if (tickY > 0 && tickY < h) {
                g2.drawLine(originX - 3, tickY, originX + 3, tickY);
                g2.drawString(String.valueOf(i), originX + 10, tickY + 5);
            }
        }
    }
}