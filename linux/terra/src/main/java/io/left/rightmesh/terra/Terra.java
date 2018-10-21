package io.left.rightmesh.terra;

import java.util.concurrent.Callable;

import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.core.agents.APIStaticApplicationAgent;
import io.left.rightmesh.libdtn.storage.blob.Factory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "terra", mixinStandardHelpOptions = true, version = "terra 1.0",
        header = {
                "@|green          *                                                   .             |@",
                "@|green       +. | .+         .                  .              .         .        |@",
                "@|green   .*.... O   .. *        .                     .       .             .     |@",
                "@|green    ...+.. `'O ..                                    .           |          |@",
                "@|green    +.....+  | ..+                    .                  .      -*-         |@",
                "@|green    ...+...  O ..      ---========================---       .    |          |@",
                "@|green    *...  O'` ...*             .          .                   .             |@",
                "@|green  .    O'`  .+.    _________ ____   _____    _____ .  ___                   |@",
                "@|green        `'O       /___  ___// __/  / __  |  / __  |  /   |                  |@",
                "@|green    .                / /.  / /_   / /_/ /  / /_/ /  / /| | .                |@",
                "@|green      .             / /   / __/  / _  |   / _  |   / __  |          .       |@",
                "@|green               .   /./   / /__  / / | |  / / | |  / /  | |                  |@",
                "@|green    |             /_/   /____/ /_/  |_| /_/  |_| /_/   |_|      .           |@",
                "@|green   -*-                                                                *     |@",
                "@|green    |     .           ---========================---             .          |@",
                "@|green       .                 Terrestrial DTN - v1.0     .                    .  |@",
                "@|green           .    .             *                    .             .          |@",
                "@|green                                  .                         .               |@",
                "@|green ____ /\\__________/\\____ ______________/\\/\\___/\\____________________________|@",
                "@|green                 __                                               ---       |@",
                "@|green          --           -            --  -      -         ---  __            |@",
                "@|green    --  __                      ___--     RightMesh (c) 2018        --  __  |@",
                ""},
        //descriptionHeading = "@|bold %nDescription|@:%n",
        description = {
                "",
                "Terra is a full node DTN implementation for Terrestrial DTN", },
        optionListHeading = "@|bold %nOptions|@:%n",
        footer = {
                ""})
public class Terra implements Callable<Void> {

    @Option(names = {"-d", "--daemon"}, description = "Start Terra as a daemon.")
    private boolean daemon;

    @Override
    public Void call() throws Exception {
        DTNCore.init();
        APIStaticApplicationAgent.register("/netflix/video/", new APIStaticApplicationAgent.StaticAPICallback() {
            @Override
            public void recv(Factory payload) {
                System.out.println("receive a Factory");
            }

            @Override
            public void close() {

            }
        });
        return null;
    }


    public static void main(String[] args) throws Exception {
        CommandLine.call(new Terra(), args);
    }
}
