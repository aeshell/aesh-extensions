package org.jboss.jreadline.extensions.more;

import org.jboss.jreadline.complete.CompleteOperation;
import org.jboss.jreadline.complete.Completion;
import org.jboss.jreadline.console.Buffer;
import org.jboss.jreadline.console.Config;
import org.jboss.jreadline.console.Console;
import org.jboss.jreadline.console.ConsoleCommand;
import org.jboss.jreadline.edit.actions.Operation;
import org.jboss.jreadline.extensions.utils.Page;
import org.jboss.jreadline.util.ANSI;
import org.jboss.jreadline.util.FileUtils;
import org.jboss.jreadline.util.Parser;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class More extends ConsoleCommand implements Completion {

    private int rows;
    private int columns;
    private int topVisibleRow;
    private StringBuilder number;
    private MorePage file;

    public More(Console console) {
        super(console);
        file = new MorePage();
        number = new StringBuilder();
    }

    public void setFile(File file) throws IOException {
        this.file.setPage(file);
    }

    public void setFile(String filename) throws IOException {
        this.file.setPage(new File(filename));
    }

    public void setInput(String input) throws IOException {
        this.file.setPageAsString(input);
    }

    @Override
    protected void afterAttach() throws IOException {
        rows = console.getTerminalHeight();
        columns = console.getTerminalWidth();
        this.file.loadPage(columns);

        if(getConsoleOutput().hasRedirectOrPipe()) {
            int count=0;
            for(String line : this.file.getLines()) {
                console.pushToStdOut(line);
                count++;
                if(count < this.file.size())
                    console.pushToStdOut(Config.getLineSeparator());
            }

            detach();
        }
        else {
            if(this.file.getFile().isFile())
                display(Background.INVERSE, this.file.getFile().getPath());
            else
                display(Background.NORMAL, ":");
        }
    }

    @Override
    protected void afterDetach() throws IOException {
        clearNumber();
    }

    @Override
    public void processOperation(Operation operation) throws IOException {
        if(operation.getInput()[0] == 'q') {
            detach();
        }
        else if( operation.equals(Operation.NEW_LINE)) {
            topVisibleRow = topVisibleRow + getNumber();
            if(topVisibleRow > (file.size()-rows)) {
                topVisibleRow = file.size()-rows;
                if(topVisibleRow < 0)
                    topVisibleRow = 0;
                detach();
            }
            else
                display(Background.INVERSE, ":");
            clearNumber();
        }
        // ctrl-f ||  space
        else if(operation.getInput()[0] == 6 || operation.getInput()[0] == 32) {
            topVisibleRow = topVisibleRow + rows*getNumber();
            if(topVisibleRow > (file.size()-rows)) {
                topVisibleRow = file.size()-rows;
                if(topVisibleRow < 0)
                    topVisibleRow = 0;
                detach();
            }
            else
                display(Background.INVERSE, ":");
            clearNumber();
        }
        else if(operation.getInput()[0] == 2) { // ctrl-b
            topVisibleRow = topVisibleRow - rows*getNumber();
            if(topVisibleRow < 0)
                topVisibleRow = 0;
            display(Background.INVERSE, ":");
            clearNumber();
        }
        else if(Character.isDigit(operation.getInput()[0])) {
            number.append(Character.getNumericValue(operation.getInput()[0]));
        }
    }

    private void display(Background background, String out) throws IOException {
        console.clear();
        for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
            if(i < file.size()) {
                console.pushToStdOut(file.getLine(i));
                console.pushToStdOut(Config.getLineSeparator());
            }
        }
        displayBottom(background, out);
    }

    private void displayBottom(Background background, String out) throws IOException {
        if(background == Background.INVERSE) {
            console.pushToStdOut(ANSI.getInvertedBackground());
            //make sure that we dont display anything longer than columns
            console.pushToStdOut(getPercentDisplayed()+"%");

            //console.pushToStdOut(ANSI.reset());
            console.pushToStdOut(Buffer.printAnsi("27m"));
        }
        else
            console.pushToStdOut(out);
    }

    private String getPercentDisplayed() {
        double row = topVisibleRow  + rows;
        if(row > this.file.size())
            row  = this.file.size();

        return String.valueOf((int) ((row / this.file.size()) * 100));
    }

    @Override
    public void complete(CompleteOperation completeOperation) {
        if(completeOperation.getBuffer().equals("m"))
            completeOperation.getCompletionCandidates().add("more");
        else if(completeOperation.getBuffer().equals("mo"))
            completeOperation.getCompletionCandidates().add("more");
        else if(completeOperation.getBuffer().equals("mor"))
            completeOperation.getCompletionCandidates().add("more");
        else if(completeOperation.getBuffer().equals("more"))
            completeOperation.getCompletionCandidates().add("more");
        else if(completeOperation.getBuffer().startsWith("more ")) {

            String word = Parser.findWordClosestToCursor(completeOperation.getBuffer(),
                    completeOperation.getCursor());
            //List<String> out = FileUtils.listMatchingDirectories(word, new File("."));
            //System.out.print(out);
            completeOperation.setOffset(completeOperation.getCursor());
            FileUtils.listMatchingDirectories(completeOperation, word,
                    new File(System.getProperty("user.dir")));
        }
    }

    private int getNumber() {
        if(number.length() > 0)
            return Integer.parseInt(number.toString());
        else
            return 1;
    }

    private void clearNumber() {
        number = new StringBuilder();
    }

    private static enum Background {
        NORMAL,
        INVERSE
    }

    private class MorePage extends Page {

    }
}
