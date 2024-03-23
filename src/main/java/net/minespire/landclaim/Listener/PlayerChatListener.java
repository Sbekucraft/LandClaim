package net.minespire.landclaim.Listener;

import net.minespire.landclaim.Claim.Claim;
import net.minespire.landclaim.GUI.GUIManager;
import net.minespire.landclaim.Prompt.Prompt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {
  @EventHandler(priority = EventPriority.HIGH)
  private void onAnswerPrompt(AsyncPlayerChatEvent chatEvent) {
    if (Prompt.hasActivePrompt(chatEvent.getPlayer())) {
      chatEvent.setCancelled(true);
      Player player = chatEvent.getPlayer();
      Prompt prompt = Prompt.getPrompt(player.getName());
      String chatMessage = chatEvent.getMessage();
      prompt.setAnswer(chatMessage);
      switch (prompt.getPromptType()) {
        case "ADDMEMBER":
          if (!Claim.addMember(player, chatMessage, prompt.getRegion())) {
            player.sendMessage(ChatColor.RED + "Could not add member to region. Player must be online.");
          } else {
            player.sendMessage(ChatColor.GOLD + "Added " + ChatColor.AQUA + Bukkit.getPlayer(chatMessage).getDisplayName() + ChatColor.GOLD + " as new member to claim " + ChatColor.AQUA + prompt.getRegion().getId());
          }
          break;
        case "ADDOWNER":
          if (!Claim.addOwner(player, chatMessage, prompt.getRegion())) {
            player.sendMessage(ChatColor.RED + "Could not add owner to region. Player must be online.");
          } else {
            player.sendMessage(ChatColor.GOLD + "Added " + ChatColor.AQUA + Bukkit.getPlayer(chatMessage).getDisplayName() + ChatColor.GOLD + " as new owner to claim " + ChatColor.AQUA + prompt.getRegion().getId());
          }
          break;
      }
      if (GUIManager.editableClaimFlags.keySet().contains(prompt.getPromptType())) {
        prompt.getRegion().setFlag(GUIManager.editableClaimFlags.get(prompt.getPromptType()), prompt.getAnswer());
        player.sendMessage(ChatColor.GOLD + "The new value for '" + prompt.getPromptType() + "' was set on " + ChatColor.AQUA + prompt.getRegion().getId());
      }
    }
  }
}
