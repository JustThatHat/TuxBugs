package com.minetux.tuxbugs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by JustThatHat
 */
public class BugTabCompleter implements TabCompleter {

    private static final String[] COMMANDS = { "delete", "help", "list", "reload", "report", "resolve" };

    @Override
    public List<String> onTabComplete(CommandSender player, Command cmd, String alias, String[] args) {
        // Checking for the correct command
        if (cmd.getName().equalsIgnoreCase("bug")) {
            // Making sure it's the first argument only
            if (args.length == 1) {
                //Creating lists
                final List<String> completions = new ArrayList<>(Arrays.asList(COMMANDS));
                final List<String> autoComplete = new ArrayList<>();

                // Using autoComplete list for auto completions
                for (String b : completions) {
                    if (!args[0].isEmpty()) {
                        if (b.startsWith(args[0])) {
                            autoComplete.add(b);
                        }
                    } else {
                        autoComplete.add(b);
                    }
                }

                List<String> newList = new ArrayList<>();

                // Filter to only show subcommand if player has permission
                for (String command : autoComplete) {
                    if (player.hasPermission("tuxbugs." + command)) {
                        newList.add(command);
                    }
                }

                // Sorting for fun
                Collections.sort(newList);
                return newList;
            }
        }
        return null;
    }
}
