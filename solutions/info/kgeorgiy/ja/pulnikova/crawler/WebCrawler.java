package info.kgeorgiy.ja.pulnikova.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class WebCrawler implements Crawler {

    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = newFixedThreadPool(downloaders);
        this.extractors = newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    @Override
    public Result download(String url, int depth) {
        Set<String> result = ConcurrentHashMap.newKeySet();
        Set<String> usedUrl = ConcurrentHashMap.newKeySet();
        Map<String, IOException> errors = new ConcurrentHashMap<>();
        Set<String> urls = ConcurrentHashMap.newKeySet();
        Set<String> urlsWork = ConcurrentHashMap.newKeySet();
        Phaser phaser = new Phaser(1);
        usedUrl.add(url);
        urls.add(url);
        while (depth > 0) {
            workDowloaders(urls, urlsWork, depth, result, usedUrl, errors, phaser);
            phaser.arriveAndAwaitAdvance();
            urls.clear();
            urls.addAll(urlsWork);
            urlsWork.clear();
            depth--;
        }
        return new Result(new ArrayList<>(result), errors);
    }

    private void workDowloaders(Set<String> urls,
                                Set<String> urlsWork,
                                int depth,
                                Set<String> result,
                                Set<String> usedUrl,
                                Map<String, IOException> errors,
                                Phaser phaser) {
        for (String link : urls) {
            phaser.register();
            downloaders.submit(() -> {
                try {
                    Document document = downloader.download(link);
                    result.add(link);
                    workExtractors(urlsWork, phaser, document, usedUrl, depth);
                } catch (IOException e) {
                    errors.put(link, e);
                } finally {
                    phaser.arriveAndDeregister();
                }
            });
        }
    }

    private void workExtractors(Set<String> workUrls,
                                Phaser phaser,
                                Document document,
                                Set<String> usedUrl,
                                int depth) {
        if (depth <= 1){
            return;
        }
        phaser.register();
        extractors.submit(() -> {
            try {
                for (String link : document.extractLinks()) {
                    if (usedUrl.add(link)) {
                        workUrls.add(link);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error with Document " + e);
            } finally {
                phaser.arriveAndDeregister();
            }
        });
    }

    @Override
    public void close() {
        downloaders.close();
        extractors.close();
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length > 4) {
            System.err.println("Invalid number of arguments");
            return;
        }
        if (args[0] == null) {
            System.err.println("Invalid url");
            return;
        }
        String url = args[0];
        int depth = argument(1, args);
        int downloaders = argument(2, args);
        int extractors = argument(3, args);
        int perHost = argument(4, args);
        if (perHost < extractors) {
            perHost = extractors;
        }
        try (Crawler crawler = new WebCrawler(new CachingDownloader(1), downloaders, extractors, perHost)) {
            crawler.download(url, depth);
        } catch (IOException e) {
            System.out.println("Error with crawler");
        }
    }

    private static int argument(int index, String[] args) {
        if (args.length <= index) {
            return 1;
        } else {
            return Integer.parseInt(args[index]);
        }
    }
}
