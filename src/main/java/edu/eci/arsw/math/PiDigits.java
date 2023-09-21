package edu.eci.arsw.math;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

///  <summary>
///  An implementation of the Bailey-Borwein-Plouffe formula for calculating hexadecimal
///  digits of pi.
///  https://en.wikipedia.org/wiki/Bailey%E2%80%93Borwein%E2%80%93Plouffe_formula
///  *** Translated from C# code: https://github.com/mmoroney/DigitsOfPi ***
///  </summary>
public class PiDigits {

    private static int DigitsPerSum = 8;
    private static double Epsilon = 1e-17;

    private static ArrayList<PiThread> PiThreads = new ArrayList<>();

    public static AtomicInteger quantityDigits = new AtomicInteger();

    public Object Mylock;

    public static Object lock;


    public PiDigits(Object lock) {
        this.lock = lock;
    }

    /**
     *
     */


    public static void divideRanges(int start, int end, int N){
        int residual = (end - start) % N;
        int ranges = (end - start) / N;

        if (residual != 0) {
            ranges += 1;
        }

        int sum = (end - start) / ranges;




        for (int i = 0; i <= N; i ++ ){

            if (start + sum < end) {
                PiThread t = new PiThread(start, start + sum, quantityDigits, lock);
                //System.out.println(start +" " + (start + sum));
                start +=  sum + 1;
                PiThreads.add(t);
            }else{
                PiThread t = new PiThread(start, end, quantityDigits, lock);
                //System.out.println(start +" " + end + "Ult");
                PiThreads.add(t);
                break;
            }

            //System.out.println(" ");

        }
    }


    public static void startPiThreads(){
        for (PiThread t : PiThreads){
            t.start();
        }
    }



    /**
     * Returns a range of hexadecimal digits of pi.
     * @param start The starting location of the range.
     * @param count The number of digits to return
     * @param N The number of threads
     * @return An array containing the hexadecimal digits.
     */
    public byte[] getDigits(int start, int count, int N){
        divideRanges(start, count, N);
        startPiThreads();

        for (PiThread t : PiThreads){
            t.getMyDigits(DigitsPerSum, Epsilon);
        }

        joinPiThreads();

        byte[] digits = new byte[count];
        int i = 0;

        for (PiThread t : PiThreads){
            byte[] digitsT = t.getDigits();
            for (int j = 0; j < digitsT.length; j ++ ){
                digits[i] = digitsT[j];
                if (i + 1 < count){
                    i += 1;
                }
                //System.out.println(i);
            }
        }



        return digits;
    }

    public static void joinPiThreads(){
        for (PiThread t : PiThreads){
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static AtomicInteger getQuantityDigits() {
        return quantityDigits;
    }

    public void waitPiThreads() throws InterruptedException {
        for (PiThread t : PiThreads){
            t.wait();
        }
    }

    public void notifyPiThreads() throws InterruptedException {
        lock.notifyAll();
    }

}
