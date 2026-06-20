package org.taranix.cafe.shell.examples.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.shell.services.CafeDiskService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@CafeSingleton
public class URLService {

    @CafeInject
    private CafeDiskService diskService;

    public Document resolveDocument(String input) throws IOException {
        if (isUrl(input)) {
            return Jsoup.connect(input).get();
        }
        if (isUrlShortcut(input)) {
            return Jsoup.connect(extractUrlFromShortcut(new File(input))).get();
        }
        return Jsoup.parse(new File(input), "UTF-8");
    }

    public boolean isUrl(String input) {
        String lower = input.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("ftp://");
    }

    public boolean isUrlShortcut(String input) {
        return input.toLowerCase().endsWith(".url");
    }

    public void createShortcut(String url, Path filePath) {
        String content = "[InternetShortcut]\nURL=" + url;
        diskService.write(filePath, content);
    }

    public void createShortcut(String url, String filePath) {
        createShortcut(url, Path.of(filePath));
    }

    private String extractUrlFromShortcut(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::strip)
                    .filter(line -> line.regionMatches(true, 0, "url=", 0, 4))
                    .map(line -> line.substring(4))
                    .findFirst()
                    .orElseThrow(() -> new IOException("No URL= entry found in shortcut: " + file.getName()));
        }
    }
}
