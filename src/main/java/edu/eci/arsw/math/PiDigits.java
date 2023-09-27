package edu.eci.arsw.math;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

///  <summary>
///  An implementation of the Bailey-Borwein-Plouffe formula for calculating hexadecimal
///  digits of pi.
///  https://en.wikipedia.org/wiki/Bailey%E2%80%93Borwein%E2%80%93Plouffe_formula
///  *** Translated from C# code: https://github.com/mmoroney/DigitsOfPi ***
///  </summary>
public class PiDigits {

    public static AtomicInteger quantityDigits = new AtomicInteger();

    public Object Mylock;

    public static Object lock;


    public PiDigits(Object lock) {
        this.lock = lock;
    }

    /**
     * 3.
     * En el tercer paso, se debe tener en cuenta la refactorización para la clase PiDigits.
     * El objetivo es que el método getDigits() devuelva un array de bytes con las respuestas de cada hilo.
     * Para ello, es necesario primero definir el rango de cada hilo, es decir, definir dónde inicia el hilo
     * y cuántos dígitos va a calcular. Por este motivo, se crea un método que divide el rango original en N (cantidad de hilos)
     * rangos distintos. Una vez que crea el hilo, lo inicia y lo agrega a un array de hilos llamado threads.
     *
     * Como los rangos no siempre serán múltiplos de N, se hace una verificación para asegurarnos que no salga del rango.
     * Para ello, sumamos 1 al contador establecido para el hilo si i es menor que el residuo. Es decir, si count = 3 y
     * N = 2, el residuo es 1 y countPerThread = 1; por lo que para la primera iteración de la asignación de rangos (i = 0),
     * countForThisThread = 2 y para la segunda iteración (i = 1), countForThisThread = 1.
     * Es decir, los primeros hilos son los que se llevarán el residuo repartido de uno en uno. Cuando se acabe el residuo,
     * los contadores serán del mismo tamaño para cada hilo.
     *
     * Divides the given range into multiple smaller ranges and creates a PiThread for each range.
     *
     * @param  start  the starting index of the range
     * @param  count  the total number of elements in the range
     * @param  N      the number of threads to create
     * @return        an array of PiThread objects representing the created threads
     */
    public static PiThread[] divideRanges(int start, int count, int N){
        PiThread[] threads = new PiThread[N];
        int residual = count % N;
        int countPerThread = count / N;
        int startPerThread = start;

        for (int i = 0; i < N; i++) {

            int countForThisThread = countPerThread;
            if (i < residual) {
                countForThisThread += 1;
            }
            //System.out.println(startPerThread + " " + countForThisThread);
            threads[i] = new PiThread(startPerThread, countForThisThread);
            threads[i].start();
            //System.out.println(threads[i].getStart() + " " + threads[i].getCount());
            startPerThread += countForThisThread;
        }

        return threads;
    }



    /**
     * 4.
     * En el cuarto paso, ya se implementa totalmente el método getDigits(). El objetivo es devolver un array de bytes con
     * las respuestas de cada hilo. Para ello, es necesario primero definir el rango de cada hilo, es decir, definir dónde
     * inicia el hilo y cuántos dígitos va a calcular. Por este motivo, se crea un método que divide el rango original en N
     * (cantidad de hilos) que creamos en el paso anterior. Después, hacemos un ciclo for para recorrer cada hilo y devolver
     * el array de bytes de cada hilo, para ello, usamos un join, que permite esperar a que cada hilo termine. Luego, se
     * llama a un método que organiza los resultados de cada hilo en un array de bytes llamado totalDigits. Este arreglo
     * se retornará.
     *
     * Returns a range of hexadecimal digits of pi.
     * @param start The starting location of the range.
     * @param count The number of digits to return
     * @param N The number of threads
     * @return An array containing the hexadecimal digits.
     */
    public static byte[] getDigits(int start, int count, int N){
        PiThread[] threads = divideRanges(start, count, N);

        for (PiThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        byte[] totalDigits = organizeResults(threads, count);

        return totalDigits;
    }

    /**
     * 5.
     * Como los hilos están divididos en rangos distintos, es necesario organizar los resultados de cada hilo para
     * poder devolver un array de los dígitos de pi en orden. Para ello, se crea un método que ordena los resultados
     * de cada hilo. La respuesta que retorne cada hilo se añadirá a un array de bytes llamado totalDigits.
     * Y este array se devolverá.
     *
     * Organizes the results from an array of PiThread objects into a single byte array.
     *
     * @param  threads  an array of PiThread objects representing the individual threads
     * @param  count    the total number of digits in the result array
     * @return          a byte array containing the organized results from the threads
     */
    private static byte[] organizeResults(PiThread[] threads, int count) {
        byte[] totalDigits = new byte[count];
        int index = 0;

        Arrays.sort(threads, Comparator.comparingInt(PiThread::getStart));

        for (PiThread thread : threads) {
            //System.out.println(thread.getStart() + " " + thread.getCount());
            byte[] threadDigits = thread.getDigitsResult();
            for (byte digit : threadDigits) {
                totalDigits[index] = digit;
                index++;
            }
        }

        return totalDigits;
    }


    public static AtomicInteger getQuantityDigits() {
        return quantityDigits;
    }


}
