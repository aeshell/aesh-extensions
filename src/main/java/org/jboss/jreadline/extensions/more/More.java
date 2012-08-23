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
    private int topVisibleRow;
    private int prevTopVisibleRow;
    private StringBuilder number;
    private MorePage page;

    public More(Console console) {
        super(console);
        page = new MorePage();
        number = new StringBuilder();
    }

    public void setFile(File page) throws IOException {
        this.page.setPage(page);
    }

    public void setFile(String filename) throws IOException {
        this.page.setPage(new File(filename));
    }

    public void setInput(String input) throws IOException {
        this.page.setPageAsString(input);
    }

    @Override
    protected void afterAttach() throws IOException {
        rows = console.getTerminalHeight();
        int columns = console.getTerminalWidth();
        this.page.loadPage(columns);

        if(getConsoleOutput().hasRedirectOrPipe()) {
            int count=0;
            for(String line : this.page.getLines()) {
                console.pushToStdOut(line);
                count++;
                if(count < this.page.size())
                    console.pushToStdOut(Config.getLineSeparator());
            }

            detach();
        }
        else {
            if(!page.hasData()) {
                //display help
            }
            else
                display(Background.INVERSE);
        }
    }

    @Override
    protected void afterDetach() throws IOException {
        clearNumber();
        topVisibleRow = prevTopVisibleRow = 0;
        if(!getConsoleOutput().hasRedirectOrPipe()) {
            console.pushToStdOut(Buffer.printAnsi("K"));
            console.pushToStdOut(Buffer.printAnsi("1G"));
        }
        page.clear();
    }

    @Override
    public void processOperation(Operation operation) throws IOException {
        if(operation.getInput()[0] == 'q') {
            detach();
        }
        else if( operation.equals(Operation.NEW_LINE)) {
            topVisibleRow = topVisibleRow + getNumber();
            if(topVisibleRow > (page.size()-rows)) {
                topVisibleRow = page.size()-rows;
                if(topVisibleRow < 0)
                    topVisibleRow = 0;
                display(Background.INVERSE);
                detach();
            }
            else
                display(Background.INVERSE);
            clearNumber();
        }
        // ctrl-f ||  space
        else if(operation.getInput()[0] == 6 || operation.getInput()[0] == 32) {
            topVisibleRow = topVisibleRow + rows*getNumber();
            if(topVisibleRow > (page.size()-rows)) {
                topVisibleRow = page.size()-rows;
                if(topVisibleRow < 0)
                    topVisibleRow = 0;
                display(Background.INVERSE);
                detach();
            }
            else
                display(Background.INVERSE);
            clearNumber();
        }
        else if(operation.getInput()[0] == 2) { // ctrl-b
            topVisibleRow = topVisibleRow - rows*getNumber();
            if(topVisibleRow < 0)
                topVisibleRow = 0;
            display(Background.INVERSE);
            clearNumber();
        }
        else if(Character.isDigit(operation.getInput()[0])) {
            number.append(Character.getNumericValue(operation.getInput()[0]));
        }
    }

    private void display(Background background) throws IOException {
        //console.clear();
        console.pushToStdOut(Buffer.printAnsi("0G"));
        console.pushToStdOut(Buffer.printAnsi("2K"));
        if(prevTopVisibleRow == 0 && topVisibleRow == 0) {
            for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
                if(i < page.size()) {
                    console.pushToStdOut(page.getLine(i));
                    console.pushToStdOut(Config.getLineSeparator());
                }
            }
        }
        else if(prevTopVisibleRow < topVisibleRow) {

            for(int i=prevTopVisibleRow; i < topVisibleRow; i++) {
                console.pushToStdOut(page.getLine(i + rows));
                console.pushToStdOut(Config.getLineSeparator());

            }
            prevTopVisibleRow = topVisibleRow;

        }
        else if(prevTopVisibleRow > topVisibleRow) {
            for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
                if(i < page.size()) {
                    console.pushToStdOut(page.getLine(i));
                    console.pushToStdOut(Config.getLineSeparator());
                }
            }
            prevTopVisibleRow = topVisibleRow;
        }
        displayBottom(background);
    }

    private void displayBottom(Background background) throws IOException {
        if(background == Background.INVERSE) {
            console.pushToStdOut(ANSI.getInvertedBackground());
            console.pushToStdOut("--More--(");
            console.pushToStdOut(getPercentDisplayed()+"%)");

            console.pushToStdOut(ANSI.getNormalBackground());
            //console.pushToStdOut(ANSI.getBold());
        }
    }

    private String getPercentDisplayed() {
        double row = topVisibleRow  + rows;
        if(row > this.page.size())
            row  = this.page.size();
        return String.valueOf((int) ((row / this.page.size()) * 100));
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
            completeOperation.setOffset(completeOperation.getCursor());
            FileUtils.listMatchingDirectories(completeOperation, word,
                    new File(System.getProperty("user.dir")));
        }
    }

    public void displayHelp() throws IOException {
        console.pushToStdOut(Config.getLineSeparator()
                +"Usage: more [options] file..."
                +Config.getLineSeparator());
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
