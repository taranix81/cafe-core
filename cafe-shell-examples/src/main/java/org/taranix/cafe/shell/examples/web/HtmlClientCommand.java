package org.taranix.cafe.shell.examples.web;

import org.jsoup.nodes.Document;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.annotations.CafeCommandRun;
import org.taranix.cafe.shell.commands.CafeCommandArguments;
import org.taranix.cafe.shell.commands.CafePrintHelpCommand;
import org.taranix.cafe.shell.exceptions.CafeCommandRuntimeServiceException;
import org.taranix.cafe.shell.services.CafeCommandRuntimeService;
import org.taranix.cafe.shell.services.CafeConsoleService;

import java.io.IOException;

@CafeCommand(description = "Fetches HTML content from a URL, local HTML file, or Windows .URL shortcut")
public class HtmlClientCommand {

    @CafeInject
    private CafeCommandRuntimeService runtimeService;

    @CafeInject
    private CafeConsoleService consoleService;

    @CafeInject
    private DocumentService documentService;

    @CafeInject
    private URLService urlService;

    @CafeCommandRun
    public Document execute(CafeCommandArguments args) {
        if (args.isEmpty()) {
            try {
                runtimeService.run(CafePrintHelpCommand.class);
            } catch (CafeCommandRuntimeServiceException e) {
                System.out.println("Usage: web-client <url|file.html|file.URL>");
            }
            return null;
        }

        String input = args.getValue(0).orElseThrow();
        Document doc;
        try {
            doc = urlService.resolveDocument(input);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }

        consoleService.add(HtmlClientCommand.class, documentService.getPrintableVersion(doc));
        return doc;
    }
}
