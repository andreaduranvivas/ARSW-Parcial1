package edu.eci.arsw.math;


import java.util.concurrent.atomic.AtomicInteger;

public class PiThread extends Thread {

    private int start;
    private int count;

    private byte[] digits;
    private static int DigitsPerSum = 8;
    private static double Epsilon = 1e-17;

    public AtomicInteger quantityDigits;

    private Object lock;


    public PiThread(int start, int count) {
        this.start = start;
        this.count = count;
    }


    /**
     * 1.
     * Este es el primer paso del laboratorio. Se crea la clase PiThread, que es una clase hija de la clase Thread.
     * Esta clase tiene los mismos métodos que la clase anterior de PiDigits. Se copian los métodos de getDigits(),
     * sum(int m, int n) y hexExponentModulo(int p, int m).
     *
     * Para parámetros como DigitsPerSum y Epsilon, se les asigna un valor por defecto como estaba definido en la clase
     * original de PiDigits y se quedan como atributos de la clase PiThread.
     *
     * Returns a range of hexadecimal digits of pi.
     * @return An array containing the hexadecimal digits.
     */
    public static byte[] getDigits(int start, int count) {
        if (start < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        if (count < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        byte[] digits = new byte[count];
        double sum = 0;

        for (int i = 0; i < count; i++) {
            if (i % DigitsPerSum == 0) {
                sum = 4 * sum(1, start)
                        - 2 * sum(4, start)
                        - sum(5, start)
                        - sum(6, start);

                start += DigitsPerSum;
            }

            sum = 16 * (sum - Math.floor(sum));
            digits[i] = (byte) sum;
        }

        return digits;
    }


    /// <summary>
    /// Returns the sum of 16^(n - k)/(8 * k + m) from 0 to k.
    /// </summary>
    /// <param name="m"></param>
    /// <param name="n"></param>
    /// <returns></returns>
    private static double sum(int m, int n) {
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

    /**
     * 2.
     * Este es el segundo paso. Se crea el método run() para que el hilo sepa que debe ejecutar.
     * En este caso, el hilo debe ejecutar el método getDigits(). Es decir, calculará los dígitos de pi
     * según la información de start y count suministrada cuando se creó el hilo.
     * Executes the function.
     *
     * @Override
     * @return None
     */
    @Override
    public void run() {
        this.digits = this.getDigits(this.start, this.count);
    }

    public byte[] getDigitsResult() {return this.digits;}

    public int getStart() {
        return start;
    }

    public int getCount() {
        return count;
    }
}