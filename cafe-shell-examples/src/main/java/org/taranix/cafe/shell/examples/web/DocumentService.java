package org.taranix.cafe.shell.examples.web;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.util.List;

@CafeSingleton
public class DocumentService {

    public String getPrintableVersion(Document doc) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(doc.title()).append(" ===\n");
        sb.append("URL: ").append(doc.location());

        Elements headings = doc.select("h1, h2, h3");
        if (!headings.isEmpty()) {
            sb.append("\n\n--- Headings ---");
            headings.forEach(h -> sb.append("\n[").append(h.tagName().toUpperCase()).append("] ").append(h.text()));
        }

        Elements paragraphs = doc.select("p");
        if (!paragraphs.isEmpty()) {
            sb.append("\n\n--- Content (first 5 paragraphs) ---");
            paragraphs.stream()
                    .map(p -> p.text().strip())
                    .filter(t -> !t.isEmpty())
                    .limit(5)
                    .forEach(t -> sb.append("\n").append(t));
        }

        Elements links = doc.select("a[href]");
        if (!links.isEmpty()) {
            sb.append("\n\n--- Links (first 10) ---");
            links.stream()
                    .limit(10)
                    .forEach(a -> sb.append("\n").append(a.text()).append("  ->  ").append(a.attr("abs:href")));
        }

        return sb.toString();
    }

    public List<Element> select(Document doc, String selector) {
        return doc.select(selector);
    }
}
