package org.taranix.cafe.shell.examples.web;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.annotations.CafeCommandRun;
import org.taranix.cafe.shell.commands.CafeCommandArguments;
import org.taranix.cafe.shell.services.CafeConsoleService;

import java.util.List;

@CafeCommand(
        command = "f",
        longCommand = "filter",
        description = "Filters document nodes by CSS selector. Usage: --filter <selector>",
        noOfArgs = 1,
        argumentName = "selector"
)
class FilterCommand {

    @CafeInject
    private CafeConsoleService consoleService;

    @CafeInject
    private DocumentService documentService;

    @CafeCommandRun
    public List<Element> execute(CafeCommandArguments args, Document document) {
        if (document == null) {
            return null;
        }
        String selector = args.getValue(0).orElseThrow();
        List<Element> elements = documentService.select(document, selector);

        consoleService.removeAll();
        consoleService.add(FilterCommand.class, "--- Filter: " + selector + " (" + elements.size() + " matches) ---");
        consoleService.add(FilterCommand.class, elements.stream()
                .map(e -> "[" + e.tagName().toUpperCase() + "] " + e.text())
                .toList());

        return elements;
    }
}
