import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class Main {
    private static BlockingQueue<String> queueOfText1 = new ArrayBlockingQueue<>(100);
    private static BlockingQueue<String> queueOfText2 = new ArrayBlockingQueue<>(100);
    private static BlockingQueue<String> queueOfText3 = new ArrayBlockingQueue<>(100);

    private static List<String> resultText = new CopyOnWriteArrayList<>();


    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(3);

        Runnable fillingQueue = () -> {
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

        executor.submit(fillingQueue);
        executor.submit(searchLineWithMaxCharacter(queueOfText1, 'a', latch));
        executor.submit(searchLineWithMaxCharacter(queueOfText2, 'b', latch));
        executor.submit(searchLineWithMaxCharacter(queueOfText3, 'c', latch));
        latch.await();
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

    public static Runnable searchLineWithMaxCharacter(BlockingQueue<String> str, char character, CountDownLatch latch) {
        return () -> {
            String result = null;
            int countOfCharacter = 0;
            for (int i = 0; i < 10_000; i++) {
                try {
                    String tempString = str.take();
                    int tempC = countOfCharacter(tempString, character);
                    if (countOfCharacter < tempC) {
                        countOfCharacter = tempC;
                        result = tempString;
                    }
                } catch (InterruptedException ignored) {
                }
            }
            resultText.add(result);
            latch.countDown();
        };
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