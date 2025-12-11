package org.taranix.cafe.desktop.components_old.model.table;

import lombok.Getter;
import lombok.Setter;


@Getter
public class TableHeader {
    private final String text;
    private final String id;

    @Setter
    private boolean isVisible;

    public TableHeader(String text, String id, boolean isVisible) {
        this.text = text;
        this.id = id;
        this.isVisible = isVisible;
    }

    public static TableHeader from(String name, String id) {
        return new TableHeader(name, id, true);
    }

    public static TableHeader from(String name) {
        return new TableHeader(name, name, true);
    }
}
