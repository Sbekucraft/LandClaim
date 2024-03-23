package net.minespire.landclaim.Prompt;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minespire.landclaim.LandClaim;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Prompt {

  private Player player;
  private int serviceTaskID;
  private String answer;
  private String promptMessage;
  private int ticksPassed = 1;
  private String promptType; //ADDMEMBER, ADDOWNER, [flagname key fom GUIManager.editableClaimFlags]
  private ProtectedRegion region;
  private static Set<String> playersWithPrompts = new HashSet<>();
  private static Map<String, Prompt> playerPrompts = new HashMap<>();

  public Prompt(String message, Player player, String promptType, ProtectedRegion region) {
    this.promptMessage = message;
    this.player = player;
    this.promptType = promptType;
    this.region = region;
  }

  private void awaitResponse() {

    answer = null;
    playersWithPrompts.add(player.getName());
    serviceTaskID = Bukkit.getScheduler().runTaskTimer(LandClaim.plugin, () -> {
      ticksPassed += 5;
        if (ticksPassed == 200 - 60 + 1) {
            player.sendMessage(ChatColor.RED + "Prompt expires in..");
        }
        if (ticksPassed == 200 - 60 + 1) {
            player.sendMessage(ChatColor.RED + "3");
        }
        if (ticksPassed == 200 - 40 + 1) {
            player.sendMessage(ChatColor.RED + "2");
        }
        if (ticksPassed == 200 - 20 + 1) {
            player.sendMessage(ChatColor.RED + "1");
        }

      if (answer != null || ticksPassed > 200) {
        Bukkit.getScheduler().cancelTask(serviceTaskID);
        playersWithPrompts.remove(player.getName());
        playerPrompts.remove(player.getName());
      }
        if (answer == null && ticksPassed > 200) {
            player.sendMessage(ChatColor.RED + "Prompt expired.");
        }

    }, 1L, 5L).getTaskId();
  }

  public static boolean hasActivePrompt(Player player) {
    return playersWithPrompts.contains(player.getName());
  }

  public boolean sendPrompt() {
      if (hasActivePrompt(player)) {
          return false;
      } else {
          player.sendMessage(promptMessage);
          awaitResponse();
          savePrompt(this);
          return true;
      }
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  public void cancelPrompt() {
    Bukkit.getScheduler().cancelTask(serviceTaskID);
    playersWithPrompts.remove(player.getName());
    playerPrompts.remove(player.getName());
    player.sendMessage(ChatColor.RED + "Prompt cancelled");
  }

  public void savePrompt(Prompt prompt) {
    playerPrompts.put(player.getName(), this);
  }

  public static Prompt getPrompt(String playerName) {
    return playerPrompts.get(playerName);
  }

  public String getAnswer() {
    return answer;
  }

  public String getPromptType() {
    return promptType;
  }

  public ProtectedRegion getRegion() {
    return region;
  }
}
