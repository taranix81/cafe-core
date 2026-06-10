package org.taranix.cafe.shell.examples.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@CafeSingleton
public class URLService {

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
