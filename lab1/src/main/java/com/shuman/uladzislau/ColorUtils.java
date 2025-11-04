package com.shuman.uladzislau;

import java.awt.Color;

/**
 * Вспомогательный класс для преобразования цветов между RGB, CMYK и HLS.
 */
public class ColorUtils {

    // --- RGB to CMYK ---
    public static float[] rgbToCmyk(int r, int g, int b) {
        float R_ = r / 255f;
        float G_ = g / 255f;
        float B_ = b / 255f;

        float K = 1 - Math.max(R_, Math.max(G_, B_));

        // от деления на ноль
        if (K == 1.0f) {
            return new float[]{0, 0, 0, 1};
        }

        float C = (1 - R_ - K) / (1 - K);
        float M = (1 - G_ - K) / (1 - K);
        float Y = (1 - B_ - K) / (1 - K);

        return new float[]{C, M, Y, K};
    }

    // --- CMYK to RGB ---
    public static Color cmykToRgb(float c, float m, float y, float k) {
        int r = (int) (255 * (1 - c) * (1 - k));
        int g = (int) (255 * (1 - m) * (1 - k));
        int b = (int) (255 * (1 - y) * (1 - k));
        return new Color(r, g, b);
    }

    // --- RGB → HLS ---
    public static float[] rgbToHls(int r, int g, int b) {
        float R = r / 255f;
        float G = g / 255f;
        float B = b / 255f;

        float max = Math.max(R, Math.max(G, B));
        float min = Math.min(R, Math.min(G, B));
        float L = (max + min) / 2f;
        float S, H;

        if (max == min) {
            H = 0;
            S = 0;
        } else {
            float delta = max - min;

            if (L < 0.5f) {
                S = delta / (max + min);
            } else {
                S = delta / (2f - max - min);
            }

            if (max == R) {
                H = (G - B) / delta;
            } else if (max == G) {
                H = 2f + (B - R) / delta;
            } else {
                H = 4f + (R - G) / delta;
            }

            H *= 60f;
            if (H < 0) {
                H += 360f;
            }
        }

        return new float[]{H, L, S};
    }

    // --- HLS → RGB ---
    public static Color hlsToRgb(float H, float L, float S) {
        float R, G, B;

        if (S == 0) {
            R = G = B = L;
        } else {
            float M2 = (L < 0.5f) ? (L * (1 + S)) : (L + S - L * S);
            float M1 = 2 * L - M2;

            R = value(H + 120, M1, M2);
            G = value(H, M1, M2);
            B = value(H - 120, M1, M2);
        }

        return new Color(
            Math.round(R * 255),
            Math.round(G * 255),
            Math.round(B * 255)
        );
    }

    private static float value(float h, float m1, float m2) {
        if (h < 0) h += 360;
        if (h > 360) h -= 360;

        if (h < 60)
            return m1 + (m2 - m1) * h / 60;
        else if (h < 180)
            return m2;
        else if (h < 240)
            return m1 + (m2 - m1) * (240 - h) / 60;
        else
            return m1;
    }
}