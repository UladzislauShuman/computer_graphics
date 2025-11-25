package shuman.ulad;

import java.awt.Point;
import java.util.List;

public class AlgorithmResult {
    public List<Point> pixels;
    public StringBuilder log;

    public AlgorithmResult(List<Point> pixels, StringBuilder log) {
        this.pixels = pixels;
        this.log = log;
    }
}