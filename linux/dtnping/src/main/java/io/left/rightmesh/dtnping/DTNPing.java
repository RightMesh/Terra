package io.left.rightmesh.dtnping;

import java.util.concurrent.Callable;

import picocli.CommandLine;

@CommandLine.Command(
        name = "dtnping", mixinStandardHelpOptions = true, version = "dtnping 1.0",
        //descriptionHeading = "@|bold %nDescription|@:%n",
        description = {
                "dtnping - send ping bundle to dtn node", },
        optionListHeading = "@|bold %nOptions|@:%n",
        footer = {
                ""})
public class DTNPing implements Callable<Void> {

    @Override
    public Void call() throws Exception {
        System.out.println("coucou!");
        return null;
    }

    public static void main(String[] args) {
        CommandLine.call(new DTNPing(), args);
    }

}
