package edu.eci.arsw.math;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
     * 9.
     * Se modifica la creación de los hilos para enviarle los dos nuevos parámetros (lock y quantityDigits)
     *
     * Divides the given range into multiple smaller ranges and creates a PiThread for each range.
     *
     * @param start          the starting index of the range
     * @param count          the total number of elements in the range
     * @param N              the number of threads to create
     * @param quantityDigits
     * @param lock
     * @return an array of PiThread objects representing the created threads
     */
    public static PiThread[] divideRanges(int start, int count, int N, AtomicInteger quantityDigits, Object lock){
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
            threads[i] = new PiThread(startPerThread, countForThisThread, quantityDigits, lock);
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
     * (cantidad de hilos) que creamos en el paso anterior. Después, hacemos otro método que crea un ciclo for para recorrer
     * cada hilo y devolver el array de bytes de cada hilo, para ello, usamos un join, que permite esperar a que cada hilo termine.
     * Luego, se llama a un método que organiza los resultados de cada hilo en un array de bytes llamado totalDigits. Este arreglo
     * se retornará.
     *
     * 8.
     * Se modifica el método para crear tanto el número atómico que servirá como contador de los dígitos calculados como el
     * lock que será usado para controlar el bloqueo de los hilos. Estas dos variables se enviarán como parámetros al método
     * divideRanges() que es el que crea los hilos.
     *
     * Returns a range of hexadecimal digits of pi.
     *
     * @param start The starting location of the range.
     * @param count The number of digits to return
     * @param N     The number of threads
     * @return An array containing the hexadecimal digits.
     */
    public static byte[] getDigits(int start, int count, int N) {
        AtomicInteger quantityDigits = new AtomicInteger(0);
        Object lock = new Object();
        PiThread[] threads = divideRanges(start, count, N, quantityDigits, lock);
        //joinDigits(threads);
        try {
            waitForEnter(count, quantityDigits, lock);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    /**
     * Joins the given array of PiThread objects.
     *
     * @param  threads  an array of PiThread objects to join
     */
    private static void joinDigits(PiThread[] threads) {
        for (PiThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 10.
     *
     * Se crea un nuevo método que esperará a que se presione ENTER para continuar cada 5 segundos. Cada vez que se
     * detenga, indicará el número de dígitos calculados, guardados en la variable quantityDigits. Para ello, se crea un
     * bloque sincronizado con el objeto lock, para no tener condiciones de carrera para la variable quantityDigits.
     * Además, se verifica si la cantidad de dígitos calculados es igual al total de dígitos que se esperan. Si es así,
     * se detendrá la operación.
     *
     * Wait to read an ENTER key press from the user every 5 seconds until the calculation of the digits is finished
     *
     * @param  count            the number of digits to calculate
     * @param  quantityDigits   the current quantity of digits calculated
     * @param  lock             the lock object for synchronization
     * @throws InterruptedException if the thread is interrupted while sleeping
     * @throws IOException          if an I/O error occurs while reading input
     */
    private static void waitForEnter(int count, AtomicInteger quantityDigits, Object lock) throws InterruptedException, IOException {

        synchronized (lock) {
            while (quantityDigits.get() < count) {
                //Thread.sleep(5000);

                lock.wait(5000);

                System.out.println("Digits found: " + quantityDigits.get());
                System.out.println("Press ENTER to resume...");

                // Wait for Enter to be pressed
                new BufferedReader(new InputStreamReader(System.in)).readLine();

                // Notify all threads that they can continue
                lock.notifyAll();
            }
        }
    }





}
