import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

public class Main {

    // Класс представляющий задачу загрузки файла
    private static class DownloadTask {
        private final URL url;
        private final String destination;
        private final String name;

        public DownloadTask(URL url, String destination, String name) {
            this.url = url;
            this.destination = destination;
            this.name = name;
        }

        // Метод выполняющий загрузку файла
        public void execute() throws IOException {
            try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                 FileOutputStream fos = new FileOutputStream(destination + name)) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
        }
    }

    // Асинхронная загрузка и воспроизведение музыкального файла
    private static CompletableFuture<Void> downloadAndPlayMusic(URL url, String destination, String name) {
        return CompletableFuture.runAsync(() -> {
            try {
                DownloadTask musicTask = new DownloadTask(url, destination, name + ".mp3");
                musicTask.execute();
                playMusic(destination + name + ".mp3");
            } catch (IOException | JavaLayerException e) {
                throw new RuntimeException("Ошибка при скачивании и воспроизведении музыки", e);
            }
        });
    }

    // Асинхронная загрузка изображения
    private static CompletableFuture<Void> downloadImage(URL url, String destination, String name) {
        return CompletableFuture.runAsync(() -> {
            try {
                DownloadTask imageTask = new DownloadTask(url, destination, name + ".jpg");
                imageTask.execute();
            } (IOException e) {
                throw new RuntimeException("Ошибка при скачивании изображения", e);
            }
        });
    }

    // Асинхронное чтение входных данных из файла и запуск соответствующих задач для загрузки музыки или изображения
    private static CompletableFuture<Void> readInputFromFile() {
        return CompletableFutureAsync(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader("file/inFile.txt"))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] words = line.split(" ");
                    if(words.length < 2){
                        throw new IllegalArgumentException("Ошибка в структуре входных данных");
                    }
                    URL url = new URL(words[0]);
                    String destination = words[1];
                    if (line.contains("jpg")) {
                        downloadImage(url, destination, "image").get();
                    } else if (line.contains("mp3")) {
                        downloadAndPlayMusic(url, destination, "music").get();
                    }
                }

            } catch (IOException | ExecutionException | InterruptedException e) {
                throw new RuntimeException("Ошибка чтения входных данных", e);
            }
        });
    }

    // Метод для воспроизведения музыки
    private static void playMusic(String filePath) {
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            AdvancedPlayer player = new AdvancedPlayer(inputStream);
            player.setPlayBackListener(new PlaybackListener() {
                @Override
                public void playbackFinished(PlaybackEvent evt) {
                    if (evt.getType() == PlaybackEvent.STOPPED) {
                        player.close();
                    }
                }
            });
            System.out.println("Playing the song");
            player.play();
        } catch (IOException | JavaLayerException e) {
            throw new RuntimeException("Ошибка воспроизведения музыки", e);
        }
    }

    public static void main(String[] args) {
        try {
            readInputFromFile().get(); // Ждем завершения всех задач
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Ошибка выполнения задач: " + e.getMessage());
        }
    }
}
