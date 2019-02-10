package ethos.model.players.packets.commands.all;

import java.util.Optional;

import ethos.model.players.Player;
import ethos.model.players.packets.commands.Command;
import ethos.util.MotivoteExecute;

public class Auth extends Command {
	
	String lastAuth = "";
	
	@Override
	public void execute(Player player, String input) {
		if (lastAuth.equals(input)) {
			player.sendMessage("@cr10@This auth was recently claimed.");
			return;
		}
		lastAuth = input;
		MotivoteExecute.run(player, input);
	}
	
	@Override
	public Optional<String> getDescription() {
		return Optional.of("Claims an auth code from ::vote");
	}

	@Override
	public Optional<String> getParameter() {
		return Optional.of("#####");
	}

}
