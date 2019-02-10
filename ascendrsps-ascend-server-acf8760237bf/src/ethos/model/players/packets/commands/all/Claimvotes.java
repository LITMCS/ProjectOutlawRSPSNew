package ethos.model.players.packets.commands.all;

import java.util.Optional;

import ethos.model.players.Player;
import ethos.model.players.packets.commands.Command;

/**
 * Opens the vote page in the default web browser.
 *
 * @author Emiel
 */
public class Claimvotes extends Command {

    String lastAuth = "";


    @Override
    public void execute(Player player, String input) {

        String[] args = input.split(" ");
        if (args.length == 1) {
            player.sendMessage("Please use [::claimvotes 1 amount], or [::claimvotes 1 all].");
            player.sendMessage("1 Vote ticket is 1 Vote point.");
            return;
        }
        final String playerName = player.playerName;
        final String id = args[0];
        final String amount = args[1];
        com.everythingrs.vote.Vote.service.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    com.everythingrs.vote.Vote[] reward = com.everythingrs.vote.Vote.reward("zlfhyknitg3ygmwes2bc07ldi4gtajdhn7ln3tjybiu76jkbj4i7m3pavp7jjgek1p2twlcvxi529",
                            playerName, id, amount);
                    if (reward[0].message != null) {
                        player.sendMessage(reward[0].message);
                        return;
                    }
                    player.getItems().addItemUnderAnyCircumstance(reward[0].reward_id, reward[0].give_amount);
                    player.sendMessage(
                            "@cr10@Thank you for voting! You now have " + reward[0].vote_points + " vote points.");
                } catch (Exception e) {
                    player.sendMessage("Api Services are currently offline. Please check back shortly");
                    e.printStackTrace();
                }
            }

        });
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Claims your vote from ::vote");
    }

    @Override
    public Optional<String> getParameter() {
        return Optional.of("id# amount#");
    }
}

