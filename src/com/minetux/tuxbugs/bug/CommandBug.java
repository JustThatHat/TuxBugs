package com.minetux.tuxbugs.bug;

import com.minetux.tuxbugs.Main;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by JustThatHat
 */
public class CommandBug implements CommandExecutor {

    // Allowing all methods from Main class to be called here
    private Main plugin;
    public CommandBug(Main pl) {
        plugin = pl;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Checking if sender is a player
        if (sender instanceof Player) {

            // Casting sender to Player type
            Player player = (Player) sender;

            // Creating config variables
            FileConfiguration config = plugin.getConfig();
            FileConfiguration msgConf = plugin.getMsgConfig();
            FileConfiguration bugConf = plugin.getBugConfig();

            // Setting default command to /bug help if player has permission.
            // Setting args[0] to "no-help" if player has no permission.
            // May change in future to plugin info, likely not.
            if (args.length == 0) {
                args = new String[1];
                if (player.hasPermission("tuxbugs.help")) {
                    args[0] = "help";
                } else {
                    args[0] = "no-help";
                }
            }

            // Creating switch for subcommands rather than if/else statement
            switch (args[0]) {
                // Creating case for 'no-help' situation
                case "no-help":
                    player.sendMessage(translateOptions(msgConf.getString("Messages.err-no-help"), player));
                    break;

                // Creating case for report subcommand
                case "report":
                    // Checking for permission
                    if (player.hasPermission("tuxbugs.report")) {
                        // Checking to ensure player is actually reporting a bug
                        if (args.length >= 2) {

                            // Creating String variable for the reported bug
                            String bugReport = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                            // Sending message to player to confirm report
                            String bugMessage = translateOptionsReport(msgConf.getString("Messages.on-bug-report"), player, bugReport);
                            player.sendMessage(bugMessage);

                            // Getting server time to store with bug report
                            Calendar currentDate = Calendar.getInstance();
                            SimpleDateFormat formatter = new SimpleDateFormat(config.getString("time-format"));
                            String dateNow = formatter.format(currentDate.getTime());

                            // Incrementing the Bug count for IDs
                            bugConf.set("Bug count", bugConf.getInt("Bug count") + 1);

                            // Adding the reported bug to bugs.yml with relevant information
                            bugConf.set(("Bugs." + bugConf.getInt("Bug count") + ".Player"), player.getDisplayName());
                            bugConf.set(("Bugs." + bugConf.getInt("Bug count") + ".Resolved"), false);
                            bugConf.set(("Bugs." + bugConf.getInt("Bug count") + ".Server Time"), dateNow);
                            bugConf.set(("Bugs." + bugConf.getInt("Bug count") + ".Report"), bugReport);

                            // Saving changes
                            try {
                                bugConf.save(new File("./plugins/TuxBugs/bugs.yml"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // Notifying admins of the bug if true in config
                            if (config.getBoolean("notify-admins")) {
                                Bukkit.broadcast(translateOptionsReport(msgConf.getString("Messages.bug-report-notification"), player, String.join(" ", Arrays.copyOfRange(args, 1, args.length))), "tuxbugs.notify");
                            }

                        } else {
                            // Warning the player if they haven't included a bug report
                            player.sendMessage(translateOptions(msgConf.getString("Messages.err-no-report"), player));
                        }
                    } else {
                        // Warning the player if they have no permission
                        player.sendMessage(translateOptions(msgConf.getString("Messages.err-no-permission"), player));
                    }

                    break;

                // Creating case for help subcommand
                case "help":
                    if (player.hasPermission("tuxbugs.help")) {
                        // Creating and sending the help message
                        // May change this in future to only show commands player has permission for
                        String helpMessage = ChatColor.translateAlternateColorCodes('&', "&e--------------<&e&lBugs&e>--------------\n&e/bug help: &fBrings up this help menu." +
                                "\n&e/bug report: &fReports a bug.\n&e/bug list [filters]: &fLists reported bugs.\n&e/bug resolve: &fMarks a bug as resolved\n&e/bug delete: &fDeletes a bug report." +
                                "\n&e/bug reload: &fReloads the config.\n&e---------------=(*)=---------------");
                        player.sendMessage(helpMessage);
                    } else {
                        // Warning player if they don't have permission
                        player.sendMessage(translateOptions(msgConf.getString("Messages.err-no-permission"), player));
                    }
                    break;

                // Creating case for list subcommand
                case "list":
                    if (player.hasPermission("tuxbugs.list")) {
//Before I get criticism on the filtering method, I know it's a horrible way to do things, I just couldn't think of a better one. Suggestions always appreciated.

                        // Creating List of bugs using getKeys()
                        List<String> entries = new ArrayList<>(bugConf.getConfigurationSection("Bugs").getKeys(false));

                        // Sending message if no bugs exist
                        if (entries.isEmpty()) {
                            player.sendMessage(translateOptions(msgConf.getString("Messages.err-no-bugs"), player));
                            break;
                        }

	                    if(args.length > 1) {

                            // Creating new List
                            List<String> matched = new ArrayList<>();

                            // Adding first relevant filter of each type to matched if it exists
                            for (int x = 1; x < args.length; x++) {
                                if (args[x].toLowerCase().startsWith("p:")) {
                                    matched.add(args[x]);
                                    break;
                                }
                            }

                            for (int w = 1; w < args.length; w++) {
                                if (args[w].toLowerCase().startsWith("r:")) {
                                    matched.add(args[w]);
                                    break;
                                }
                            }

                            for (int y = 1; y < args.length; y++) {
                                if (args[y].toLowerCase().startsWith("t:")) {
                                    matched.add(args[y]);
                                    break;
                                }
                            }

                            // Creating new list to store matching bugs
                            List<String> bugsThatMatch = new ArrayList<>();

                            // For every filter, adding matching bugs to list then shortening list until a final list is reached
                            for (String filter : matched) {

                                for (String key : entries) {
                                    if (filter.toLowerCase().startsWith("p:")) {
                                        if (filter.contains(bugConf.getString("Bugs." + key + ".Player"))) {
                                            bugsThatMatch.add(key);
                                        }
                                    } else if (filter.toLowerCase().startsWith("r:")) {
                                        if (filter.contains(bugConf.getString("Bugs." + key + ".Resolved"))) {
                                            bugsThatMatch.add(key);
                                        }
                                    } else if (filter.toLowerCase().startsWith("t:")) {
                                        if (filter.contains(bugConf.getString("Bugs." + key + ".Server Time"))) {
                                            bugsThatMatch.add(key);
                                        }
                                    }
                                }

                                entries.clear();
                                entries.addAll(bugsThatMatch);
                                bugsThatMatch.clear();

	                    	}
	                    }

                        // Creating page number variable if relevant argument exists
                        int page = args.length > 1 && isInteger(args[1]) ? Integer.parseInt(args[1])-1 : 0;

                        // Setting topPage variable for highest page number available
                        int topPage;

                        if (entries.size() % 4 == 0) {
                            topPage = (int)Math.ceil(entries.size() / 4);
                        } else {
                            topPage = ((int)Math.ceil(entries.size() / 4) + 1);
                        }

	                    // Checking specified page using aforementioned page variable
	                    if (page + 1 <= topPage) {
                            // Sending player opening of bug list
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e---------------<&e&lBugs&e>---------------"));

                            // Listing bugs on page then sending to player
                            for (int i = 0; i < 4; i++) {
                                if (entries.size() <= page * 4 + i) {
                                    break;
                                }
                                String key = entries.get(page * 4 + i);
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "\n&e#" + key + ":"));
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&ePlayer: &r" + bugConf.get(("Bugs." + key + ".Player"))));
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eResolved: &r" + bugConf.get(("Bugs." + key + ".Resolved"))));
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eTime: &r" + bugConf.get(("Bugs." + key + ".Server Time"))));
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eReport: &r" + bugConf.get(("Bugs." + key + ".Report"))));
                            }

                            // Sending closing message
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "\n&e---------------=(" + (page + 1) + "/" + topPage + ")=---------------"));
                        } else {
	                        // Warning player if specified page doesn't exist
	                        player.sendMessage(translateOptions(msgConf.getString("Messages.err-no-page"), player));
                        }
                    } else {
                        // Warning player if they don't have permission
                        player.sendMessage(translateOptions(msgConf.getString("Messages.err-no-permission"), player));
                    }
                    break;

                // Creating case for resolve subcommand
                case "resolve":
                    if (player.hasPermission("tuxbugs.resolve")) {
                        // Checking if specified bug exists
                        if (args.length == 2 && StringUtils.isNumeric(args[1]) && bugConf.contains("Bugs." + args[1])) {
                            // Resolving bug and confirming resolve
                            try {
                                bugConf.set(("Bugs." + args[1] + ".Resolved"), true);
                                player.sendMessage(translateOptions(msgConf.getString("Messages.on-resolve"), player));
                                // Saving bugs.yml
                                try {
                                    bugConf.save(new File("./plugins/TuxBugs/bugs.yml"));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (CommandException e) {
                                e.printStackTrace();
                            }
                        } else if (!bugConf.contains("Bugs." + args[1])) {
                            // Warning player if specified bug doesn't exist
                            player.sendMessage(translateOptions(msgConf.getString("Messages.err-no-id"), player));
                        } else {
                            // Showing usage for resolve command
                            player.sendMessage(translateOptions(msgConf.getString("Messages.err-resolve-usage"), player));
                        }
                    } else {
                        // Warning player if they don't have permission
                        player.sendMessage(translateOptions(msgConf.getString("Messages.err-no-permission"), player));
                    }

                    break;

                // Creating case for delete command
                case "delete":
                    if (player.hasPermission("tuxbugs.delete")) {
                        // Checking if bug exists
                        if (args.length == 2 && StringUtils.isNumeric(args[1]) && bugConf.contains("Bugs." + args[1])) {
                            // Checking if bug has been resolved
                            if (bugConf.getBoolean("Bugs." + args[1] + ".Resolved")) {
                                // Deleting bug and confirming delete
                                bugConf.set(("Bugs." + args[1]), null);
                                player.sendMessage(translateOptions(msgConf.getString("Messages.on-delete"), player));
                                // Saving bugs.yml
                                try {
                                    bugConf.save(new File("./plugins/TuxBugs/bugs.yml"));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Warning that bug hasn't been resolved
                                player.sendMessage(translateOptions(msgConf.getString("Messages.err-not-resolved"), player));
                            }
                        } else if (!bugConf.contains("Bugs." + args[1])) {
                            // Warning that bug doesn't exist
                            player.sendMessage(translateOptions(msgConf.getString("Messages.err-no-id"), player));
                        } else {
                            // Showing delete usage
                            player.sendMessage(translateOptions(msgConf.getString("Messages.err-delete-usage"), player));
                        }
                    } else {
                        // Warning that player doesn't have permission
                        player.sendMessage(translateOptions(msgConf.getString("Messages.err-no-permission"), player));
                    }

                    break;

                // Creating case for reload subcommand
                case "reload":
                    if (player.hasPermission("tuxbugs.reload")) {
                        // Reloading through registration and confirming
                        plugin.registerConfigs();
                        System.out.println("[TuxBugs] Config reloaded!");
                        player.sendMessage(translateOptions(msgConf.getString("Messages.on-reload"), player));
                    } else {
                        // Warning that player doesn't have permission
                        player.sendMessage(translateOptions(msgConf.getString("Messages.err-no-permission"), player));
                    }

                    break;

                // Creating default case
                default:
                    player.sendMessage(translateOptions(msgConf.getString("Messages.err-not-found"), player));
            }

        } else {
            // When sender is not player, only allow reload command
            if (args.length == 1 && args[0].equals("reload")) {
                // Reloading and confirming
                plugin.registerConfigs();
                System.out.println("[TuxBugs] Config reloaded!");
            } else {
                // Warning that only reload command can be used from console
                System.out.println("[TuxBugs] Only the reload command can be executed from the console.");
            }
        }

        return true;
    }

    // Creating method to translate colours and variables of message from messages.yml
    private String translateOptions(String message, Player sender) {

        String msg = message;

        // Checking if user wishes to use prefix
        if (plugin.getMsgConfig().getBoolean("use-prefix")) {
            msg = plugin.getMsgConfig().getString("Messages.prefix") + msg;
        }

        // Doing relevant replacements
        msg = msg.replace("%player%", sender.getDisplayName());
        msg = msg.replace("\\n", "\n");
        msg = ChatColor.translateAlternateColorCodes('&', msg);

        return msg;
    }

    // This is basically the same bug includes support for the reported bug as well
    private String translateOptionsReport(String message, Player sender, String report) {

        String msg = message;

        if (plugin.getMsgConfig().getBoolean("use-prefix")) {
            msg = plugin.getMsgConfig().getString("Messages.prefix") + msg;
        }

        msg = msg.replace("%player%", sender.getDisplayName());
        msg = msg.replace("%bugReport%", report);
        msg = msg.replace("\\n", "\n");
        msg = ChatColor.translateAlternateColorCodes('&', msg);

        return msg;
    }

    // Method to check if given String is integer
    private boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        }
        catch(Exception e) {
            return false;
        }
    }
}
