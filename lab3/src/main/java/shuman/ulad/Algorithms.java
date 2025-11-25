package shuman.ulad;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс, содержащий реализацию базовых алгоритмов растеризации.
 * Реализованы: Пошаговый, ЦДА, Брезенхем (отрезок и окружность).
 */
public class Algorithms {

    // =============================================================
    // 1. Пошаговый алгоритм (Step-by-step)
    // =============================================================
    public static AlgorithmResult stepByStep(int x1, int y1, int x2, int y2, boolean isLog) {
        List<Point> points = new ArrayList<>();
        StringBuilder log = new StringBuilder("Пошаговый алгоритм:\n");

        // 1. Обработка частного случая: Вертикальная линия.
        // Уравнение y = kx + b неприменимо, т.к. k = dy/dx, а dx = 0 (деление на ноль).
        if (x1 == x2) {
            if (isLog) {
                log.append("Вертикальная линия (x = const)\n");
            }
            int start = Math.min(y1, y2);
            int end = Math.max(y1, y2);
            // Просто проходим циклом по Y, сохраняя X постоянным
            for (int y = start; y <= end; y++) {
                points.add(new Point(x1, y));
            }
            return new AlgorithmResult(points, log);
        }

        // 2. Вычисление коэффициентов уравнения прямой
        // k - угловой коэффициент (тангенс угла наклона)
        // b - смещение по оси Y
        double k = (double)(y2 - y1) / (x2 - x1);
        double b = y1 - k * x1;
        if (isLog) {
            log.append(String.format("k = %.2f, b = %.2f\n", k, b));
        }

        // 3. Выбор ведущей оси (чтобы избежать разрывов линии)
        // Если |k| <= 1, линия "пологоая" (угол <= 45 градусов).
        // Изменение X на 1 влечет изменение Y меньше чем на 1. Итерируем по X.
        if (Math.abs(k) <= 1) {
            int startX = Math.min(x1, x2);
            int endX = Math.max(x1, x2);
            for (int x = startX; x <= endX; x++) {
                // Вычисляем идеальное Y и округляем до ближайшего целого
                double y = k * x + b;
                int yRound = (int) Math.round(y);
                points.add(new Point(x, yRound));

                if (isLog) {
                    log.append(String.format("x=%d, y=%.2f -> (%d, %d)\n", x, y, x, yRound));
                }
            }
        }
        // Если |k| > 1, линия "крутая" (угол > 45 градусов).
        // Итерировать по X нельзя, будут "дырки". Итерируем по Y.
        else {
            if (isLog) {
                log.append("Крутой наклон (|k| > 1), итерируем по Y\n");
            }
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = startY; y <= endY; y++) {
                // Выражаем x через y: x = (y - b) / k
                double x = (y - b) / k;
                int xRound = (int) Math.round(x);
                points.add(new Point(xRound, y));
                if (isLog) {
                    log.append(String.format("y=%d, x=%.2f -> (%d, %d)\n", y, x, xRound, y));
                }
            }
        }
        return new AlgorithmResult(points, log);
    }

    // =============================================================
    // 2. Алгоритм ЦДА (Цифровой Дифференциальный Анализатор)
    // =============================================================
    public static AlgorithmResult dda(int x1, int y1, int x2, int y2, boolean isLog) {
        List<Point> points = new ArrayList<>();
        StringBuilder log = new StringBuilder("Алгоритм ЦДА:\n");

        // Вычисляем разницу координат
        int dx = x2 - x1;
        int dy = y2 - y1;

        // L - количество шагов (длина проекции на ведущую ось).
        // Выбираем максимум, чтобы шаг изменения координат не превышал 1.
        int L = Math.max(Math.abs(dx), Math.abs(dy));

        // Вычисляем приращение на один шаг цикла
        // Одно из приращений будет равно 1 (или -1), другое < 1.
        double xInc = (double) dx / L;
        double yInc = (double) dy / L;

        // Текущие координаты (храним в double для точности накопления)
        double x = x1;
        double y = y1;

        if (isLog) {
            log.append(String.format("L=%d, dx=%d, dy=%d\n", L, dx, dy));
            log.append(String.format("Приращения: xInc=%.2f, yInc=%.2f\n", xInc, yInc));
        }
        // Ставим первую точку (округление обязательно)
        points.add(new Point((int)Math.round(x), (int)Math.round(y)));

        // Основной цикл
        for (int i = 0; i < L; i++) {
            x += xInc; // Накапливаем изменение по X
            y += yInc; // Накапливаем изменение по Y

            // Округляем до ближайшего пикселя
            int px = (int) Math.round(x);
            int py = (int) Math.round(y);
            points.add(new Point(px, py));

            if (isLog) {
                log.append(
                    String.format("Шаг %d: x=%.2f, y=%.2f -> (%d, %d)\n", i + 1, x, y, px, py));
            }
        }
        return new AlgorithmResult(points, log);
    }

    // =============================================================
    // 3. Алгоритм Брезенхема для отрезка
    // =============================================================
    public static AlgorithmResult bresenhamLine(int x1, int y1, int x2, int y2, boolean isLog) {
        List<Point> points = new ArrayList<>();
        StringBuilder log = new StringBuilder("Брезенхем (Отрезок):\n");

        int x = x1;
        int y = y1;
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        // Определяем направление движения (вправо/влево, вверх/вниз)
        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;

        // Если dy > dx, то линия крутая (угол > 45).
        // Алгоритм Брезенхема классически работает для 1-го октанта (dx > dy).
        // Чтобы он работал везде, мы "меняем местами" оси для расчетов,
        // а при рисовании возвращаем как было.
        boolean swap = false;
        if (dy > dx) {
            int temp = dx; dx = dy; dy = temp;
            swap = true; // Запоминаем, что оси поменяны местами
        }

        // Инициализация ошибки: E = 2*dy - dx
        // (Формула выведена из условия средней точки)
        int e = 2 * dy - dx;

        if (isLog) {
            log.append(String.format("dx=%d, dy=%d, E_start=%d\n", dx, dy, e));
        }


        // Основной цикл выполняется dx раз (по длинной стороне)
        for (int i = 0; i <= dx; i++) {
            points.add(new Point(x, y));

            if (isLog) {
                log.append(String.format("Шаг %d: (%d, %d), E=%d\n", i, x, y, e));
            }

            // Если ошибка >= 0, значит мы слишком отклонились
            // Нужно сделать шаг по "короткой" стороне (диагональный шаг)
            if (e >= 0) {
                if (swap) {
                    x += sx; // Если оси меняли, то короткая сторона - это X
                } else {
                    y += sy; // Иначе короткая сторона - это Y
                }
                e -= 2 * dx; // Корректируем ошибку
            }

            // Шаг по "длинной" стороне делается всегда
            if (swap) {
                y += sy; // Если оси меняли, длинная сторона - Y
            } else {
                x += sx; // Иначе длинная сторона - X
            }

            e += 2 * dy; // Накапливаем ошибку для следующего шага
        }

        return new AlgorithmResult(points, log);
    }

    // =============================================================
    // 4. Алгоритм Брезенхема для окружности
    // =============================================================
    public static AlgorithmResult bresenhamCircle(int xc, int yc, int r, boolean isLog) {
        List<Point> points = new ArrayList<>();
        StringBuilder log = new StringBuilder("Брезенхем (Окружность):\n");

        int x = 0;
        int y = r; // Начинаем с верхней точки (0, R)

        // Начальное значение ошибки: E = 3 - 2*R
        // (Оптимизированная формула для целочисленных вычислений)
        int e = 3 - 2 * r;

        if (isLog) {
            log.append(String.format("R=%d, Start E=%d\n", r, e));
        }

        // Рисуем начальные 8 симметричных точек
        addCirclePoints(points, xc, yc, x, y);

        int step = 0;
        // Цикл пока x < y (рисуем только один октант, 1/8 часть)
        while (x < y) {
            if (e >= 0) {
                // Точка внутри окружности слишком далеко -> шаг по диагонали
                // Формула пересчета ошибки при диагональном шаге
                e = e + 4 * (x - y) + 10;
                x++;
                y--;
            } else {
                // Точка близка к окружности -> шаг по горизонтали
                // Формула пересчета ошибки при горизонтальном шаге
                e = e + 4 * x + 6;
                x++;
            }
            // Отражаем полученную точку (x, y) во все 8 октантов
            addCirclePoints(points, xc, yc, x, y);

            if (isLog) {
                log.append(String.format("Шаг %d: x=%d, y=%d, E=%d\n", step++, x, y, e));
            }
        }

        return new AlgorithmResult(points, log);
    }

    /**
     * Вспомогательный метод для симметричного отображения точек окружности.
     * Использует 8-стороннюю симметрию.
     */
    private static void addCirclePoints(List<Point> points, int xc, int yc, int x, int y) {
        points.add(new Point(xc + x, yc + y)); // 1 октант
        points.add(new Point(xc + x, yc - y)); // 4 октант
        points.add(new Point(xc - x, yc + y)); // 2 октант
        points.add(new Point(xc - x, yc - y)); // 3 октант

        points.add(new Point(xc + y, yc + x)); // 8 октант (зеркально 1)
        points.add(new Point(xc + y, yc - x)); // 5 октант
        points.add(new Point(xc - y, yc + x)); // 7 октант
        points.add(new Point(xc - y, yc - x)); // 6 октант
    }
}