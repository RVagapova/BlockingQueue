import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class Main {
    private static BlockingQueue<String> queueOfText1 = new ArrayBlockingQueue<>(100);
    private static BlockingQueue<String> queueOfText2 = new ArrayBlockingQueue<>(100);
    private static BlockingQueue<String> queueOfText3 = new ArrayBlockingQueue<>(100);


    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<String> resultText = new CopyOnWriteArrayList<>();

        Runnable fillingQueue = () -> {
            System.out.println("первый");
            for (int i = 0; i < 10_000; i++) {
                try {
                    String text = generateText("abc", 100_000);
                    queueOfText1.put(text);
                    queueOfText2.put(text);
                    queueOfText3.put(text);
                } catch (InterruptedException ignored) {
                }
            }
        };

        Runnable maxA = () -> {
            String resultA = null;
            int a = 0;
            for (int i = 0; i < 10_000; i++) {
                try {
                    String tempString = queueOfText1.take();
                    int tempA = countOfCharacter(tempString, 'a');
                    if (a < tempA) {
                        a = tempA;
                        resultA = tempString;
                    }
                } catch (InterruptedException ignored) {
                }
            }
            resultText.add(resultA);
        };

        Runnable maxB = () -> {
            String resultB = null;
            int b = 0;
            for (int i = 0; i < 10_000; i++) {
                try {
                    String tempString = queueOfText2.take();
                    int tempB = countOfCharacter(tempString, 'b');
                    if (b < tempB) {
                        b = tempB;
                        resultB = tempString;
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
            resultText.add(resultB);
        };

        Runnable maxC = () -> {
            String resultC = null;
            int c = 0;
            for (int i = 0; i < 10_000; i++) {
                try {
                    String tempString = queueOfText3.take();
                    int tempC = countOfCharacter(tempString, 'c');
                    if (c < tempC) {
                        c = tempC;
                        resultC = tempString;
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
            resultText.add(resultC);
        };

        executor.submit(fillingQueue);
        executor.submit(maxA);
        executor.submit(maxB);
        executor.submit(maxC);

        executor.awaitTermination(1, TimeUnit.MINUTES);
        executor.shutdown();

        resultText.forEach(System.out::println);
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    public static int countOfCharacter(String text, char symbol) {
        int result = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == symbol) {
                result++;
            }
        }
        return result;
    }
}