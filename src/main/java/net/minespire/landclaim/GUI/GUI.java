package net.minespire.landclaim.GUI;

import net.minespire.landclaim.Claim.Claim;
import net.minespire.landclaim.LandClaim;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUI {
  private Inventory inventory;
  private Player player;
  private int guiSlots;
  public static Map<String, List<GUI>> playersGUIMap = new HashMap<>();
  private List<GUIItem> guiItems;
  public static List<String> inventoryNames = new ArrayList<>();

  public GUI(int guiSlots) {
    int slotCountModifier = guiSlots % 9;
    if (slotCountModifier != 0) {
      this.guiSlots = (9 - slotCountModifier) + guiSlots;
    } else {
      this.guiSlots = guiSlots;
    }
    //playerMenus.put("list", null);
    //playerMenus.put("listByCategory", null);
    //playerMenus.put("editClaim", null);
    guiItems = new ArrayList<>();
  }

  public GUI() {
    this(LandClaim.plugin.getConfig().getInt("GUI.Slots"));
  }

  public int getNumSlots() {
    return guiSlots;
  }

  public int getNumGUIItems() {
    return guiItems.size();
  }

  public void openGUI() {
    setItemsToSlots();
    player.openInventory(inventory);
  }

  public GUI setPlayer(Player player) {
    this.player = player;
    //playersGUIMap.put(player.getUniqueId().toString(), playerMenus);
    return this;
  }

  public GUI setInventory(String inventoryName) {
    inventory = Bukkit.createInventory(null, guiSlots, ChatColor.translateAlternateColorCodes('&', inventoryName));
    if (!inventoryNames.contains(inventoryName)) {
      inventoryNames.add(inventoryName);
    }
    return this;
  }

  public GUI addGUIItem(GUIItem item) {
    guiItems.add(item);
    return this;
  }

  private void createFillerItems() {
    int slotsToFill = this.getNumSlots() - this.getNumGUIItems();
    for (int x = 0; x < slotsToFill; x++) {
      this.addGUIItem(this.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.FillerItem"))).setDisplayName(" ").setMeta());
    }
  }

  public static void promptForRemoval(String playerName) {
    if (Claim.awaitingRemovalConfirmation.containsKey(playerName)) {
      GUI gui = new GUI(27);
      gui.setPlayer(Bukkit.getPlayer(playerName));
      gui.setInventory("Region Removal");
      GUIItem button = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.RemoveClaimButton.Material")));
      button.setDisplayName("Remove " + Claim.awaitingRemovalConfirmation.get(playerName).getId() + "?");
      button.setLore(ChatColor.RED + "Warning:" + ChatColor.WHITE + " This cannot be undone");
      button.setSlot(13).setMeta();
      gui.addGUIItem(button);
      gui.openGUI();
    }
  }

  private void setItemsToSlots() {
    if (inventory.firstEmpty() < 0) {
      return;
    }
    createFillerItems();
    Iterator<GUIItem> iterator = guiItems.iterator();
    List<GUIItem> fillerItems = new ArrayList<>();
    GUIItem tempHolder;
    while (iterator.hasNext()) {
      tempHolder = iterator.next();
      if (tempHolder.slotNum > 0 && tempHolder.slotNum < 54) {
        inventory.setItem(tempHolder.slotNum, tempHolder.item);
      } else {
        fillerItems.add(tempHolder);
      }
    }
    iterator = fillerItems.iterator();
    while (iterator.hasNext()) {
      inventory.setItem(inventory.firstEmpty(), iterator.next().item);
    }
  }

  public class GUIItem {
    private ItemStack item;
    private ItemMeta guiItemMeta;
    private String displayName;
    private List<String> lore;
    private int slotNum = -1;

    public GUIItem(Material material) {
      item = new ItemStack(material);
      guiItemMeta = item.getItemMeta();
      displayName = "";
      lore = new ArrayList<>();
    }

    public GUIItem setDisplayName(String name) {
      this.guiItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
      return this;
    }

    public GUIItem setSlot(int slot) {
      slotNum = slot;
      return this;
    }

    public GUIItem setLore(List<String> lore) {
      guiItemMeta.setLore(lore);
      return this;
    }

    public GUIItem setLore(String loreString) {
      guiItemMeta.setLore(parseLoreString(loreString));
      return this;
    }

    public GUIItem setMeta() {
      item.setItemMeta(guiItemMeta);
      return this;
    }

    private List<String> parseLoreString(String loreString) {
      String[] loreArray = loreString.split("\\|");
      List<String> loreList = new ArrayList<>();
      for (int x = 0; x < loreArray.length; x++) {
        loreList.add(x, ChatColor.translateAlternateColorCodes('&', loreArray[x]));
      }
      return loreList;
    }
  }

  public static void saveGUIToPlayerMap(Player player, GUI gui, boolean startNewList) {
    List<GUI> playerGUIList;
    if (!GUI.playersGUIMap.containsKey(player.getUniqueId().toString()) || startNewList == true) {
      playerGUIList = new ArrayList<>();
      playerGUIList.add(gui);
      GUI.playersGUIMap.put(player.getUniqueId().toString(), playerGUIList);
    } else {
      playerGUIList = GUI.playersGUIMap.get(player.getUniqueId().toString());
      playerGUIList.add(gui);
      GUI.playersGUIMap.put(player.getUniqueId().toString(), playerGUIList);
    }
  }
}
