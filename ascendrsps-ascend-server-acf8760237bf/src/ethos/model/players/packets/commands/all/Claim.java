package ethos.model.players.packets.commands.all;

import ethos.model.players.Player;
import ethos.model.players.PlayerHandler;
import ethos.model.players.packets.commands.Command;


import ethos.model.players.Player;
import ethos.model.players.packets.commands.Command;

/**
 * Auto Donation System / https://EverythingRS.com
 * @author Genesis
 *
 */

public class Claim extends Command {

	@Override
	public void execute(Player player, String input) {
		new java.lang.Thread() {
			public void run() {
				try {
					com.everythingrs.donate.Donation[] donations = com.everythingrs.donate.Donation
							.donations("zlfhyknitg3ygmwes2bc07ldi4gtajdhn7ln3tjybiu76jkbj4i7m3pavp7jjgek1p2twlcvxi529", player.playerName);
					if (donations.length == 0) {
						player.sendMessage("You currently don't have any items waiting. You must donate first!");
						return;
					}
					if (donations[0].message != null) {
						player.sendMessage(donations[0].message);
						return;
					}
					for (com.everythingrs.donate.Donation donate : donations) {
						player.getItems().addItemUnderAnyCircumstance(donate.product_id, donate.product_amount);
						PlayerHandler.executeGlobalMessage("@red@"+player.playerName+ " has donated for "+donate.product_amount+" trading sticks! Thank you!");
					}
					player.sendMessage("Thank you for donating!");
				} catch (Exception e) {
					player.sendMessage("Api Services are currently offline. Please check back shortly");
					e.printStackTrace();
				}
			}
		}.start();
	}

}
