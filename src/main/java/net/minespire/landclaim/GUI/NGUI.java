package net.minespire.landclaim.GUI;

import net.minespire.landclaim.Listener.GUIClick;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class NGUI {
  private Inventory inventory;
  private int size;

  public NGUI(int size, String title) {
    this.size = size;
    inventory = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', title));
  }

  public NGUI addItem(Material mat, String title, List<String> lore, Integer slot) {
    inventory.setItem(slot, GUIItem.make(mat, title, lore));
    return this;
  }

  public NGUI addItem(Material mat, String title, List<String> lore) {
    return addItem(mat, title, lore, inventory.firstEmpty());
  }

  public void open(Player player) {
    GUIClick.playerLCInventory.put(player.getUniqueId().toString(), player.openInventory(inventory));
  }

  public int size() {
    return size;
  }
}
