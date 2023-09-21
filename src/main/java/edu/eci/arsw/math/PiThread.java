package edu.eci.arsw.math;


import java.util.concurrent.atomic.AtomicInteger;

public class PiThread extends Thread {

    private static int start;
    private static int end = 0;

    private byte[] digits;

    public AtomicInteger quantityDigits;

    private Object lock;


    public PiThread(int start, int end, AtomicInteger quantityDigits, Object lock) {
        this.start = start;
        this.end = end;
        this.quantityDigits = quantityDigits;
        this.lock = lock;


    }

    /**
     * Returns a range of hexadecimal digits of pi.
     *
     * @param DigitsPerSum
     * @param Epsilon
     * @return An array containing the hexadecimal digits.
     */
    public void getMyDigits(int DigitsPerSum, double Epsilon) {
        if (start < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        if (end < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        this.digits = new byte[end];
        double sum = 0;

        for (int i = 0; i < end; i++) {
            if (i % DigitsPerSum == 0) {
                sum = 4 * sum(1, start, Epsilon)
                        - 2 * sum(4, start, Epsilon)
                        - sum(5, start, Epsilon)
                        - sum(6, start, Epsilon);

                start += DigitsPerSum;
            }

            sum = 16 * (sum - Math.floor(sum));
            digits[i] = (byte) sum;
            quantityDigits.addAndGet(1);
        }
    }


    /// <summary>
    /// Returns the sum of 16^(n - k)/(8 * k + m) from 0 to k.
    /// </summary>
    /// <param name="m"></param>
    /// <param name="n"></param>
    /// <returns></returns>
    private static double sum(int m, int n, double Epsilon) {
        double sum = 0;
        int d = m;
        int power = n;

        while (true) {
            double term;

            if (power > 0) {
                term = (double) hexExponentModulo(power, d) / d;
            } else {
                term = Math.pow(16, power) / d;
                if (term < Epsilon) {
                    break;
                }
            }

            sum += term;
            power--;
            d += 8;
        }

        return sum;
    }


    private static int hexExponentModulo(int p, int m) {
        int power = 1;
        while (power * 2 <= p) {
            power *= 2;
        }

        int result = 1;

        while (power > 0) {
            if (p >= power) {
                result *= 16;
                result %= m;
                p -= power;
            }

            power /= 2;

            if (power > 0) {
                result *= result;
                result %= m;
            }
        }

        return result;
    }

    public byte[] getDigits() {
        return digits;
    }

    public static int getStart() {
        return start;
    }

    public static int getEnd() {
        return end;
    }
}