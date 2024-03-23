package net.minespire.landclaim.Listener;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minespire.landclaim.Claim.Claim;
import net.minespire.landclaim.GUI.GUI;
import net.minespire.landclaim.LandClaim;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.List;

public class InventoryClickListener implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onInventoryClick(InventoryClickEvent clickEvent) {
    boolean LandClaimGUI = false;
    for (String title : GUI.inventoryNames) {
      if (title != "" && clickEvent.getWhoClicked().getOpenInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', title))) {
        clickEvent.setCancelled(true);
        LandClaimGUI = true;
      } else {
        LandClaimGUI = false;
      }
    }
    if (!LandClaimGUI) {
      return;
    }

    Player player = (Player) clickEvent.getWhoClicked();
    ItemStack clickedItem = clickEvent.getCurrentItem();

    //Click to create region or plot
    if ((clickedItem != null) && ((clickedItem.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimRegion.ClaimItem.ItemName"))) || (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimPlot.ClaimItem.ItemName"))))))) {
      Claim claim = LandClaim.claimMap.get(player.getUniqueId().toString());
      claim.saveClaim();
      //DeedListener deed = new DeedListener(player, claim.getRegionName(), claim.getWorld().getName(), true);
      //player.getInventory().addItem(deed.createDeed());
      player.sendMessage("You successfully claimed a region of land for $" + claim.getClaimCost() + "!");
      LandClaim.econ.withdrawPlayer(player, claim.getClaimCost());
      player.getOpenInventory().close();
      return;
    }

    //Click List OwnerRegions
    if ((clickedItem != null) && ((clickedItem.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimsList.OwnerRegions.ItemName")))))) {
      GUI gui = new GUI();
      GUI.GUIItem guiItem;
      gui.setInventory(ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimsList.OwnerRegions.ItemName")));
      gui.setPlayer(player);
      for (String owner : Claim.getClaimListOwner(player, false)) {
        guiItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.OwnerRegions.Material")));
        gui.addGUIItem(guiItem.setDisplayName(ChatColor.WHITE + owner).setMeta());
      }
      guiItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.BackButton.Material")));
      gui.addGUIItem(guiItem.setDisplayName(LandClaim.plugin.getConfig().getString("GUI.BackButton.ItemName")).setMeta().setSlot(gui.getNumSlots() - 5));
      GUI.saveGUIToPlayerMap(player, gui, false);
      gui.openGUI();
      return;
    }

    //Click List MemberRegions
    if ((clickedItem != null) && ((clickedItem.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimsList.MemberRegions.ItemName")))))) {
      GUI gui = new GUI();
      GUI.GUIItem guiItem;
      gui.setInventory(ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimsList.MemberRegions.ItemName")));
      gui.setPlayer(player);
      for (String member : Claim.getClaimListMember(player, false)) {
        guiItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.MemberRegions.Material")));
        gui.addGUIItem(guiItem.setDisplayName(ChatColor.WHITE + member).setMeta());
      }
      guiItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.BackButton.Material")));
      gui.addGUIItem(guiItem.setDisplayName(LandClaim.plugin.getConfig().getString("GUI.BackButton.ItemName")).setMeta().setSlot(gui.getNumSlots() - 5));
      GUI.saveGUIToPlayerMap(player, gui, false);
      gui.openGUI();
      return;
    }

    //Click List OwnerPlots
    if ((clickedItem != null) && ((clickedItem.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimsList.OwnerPlots.ItemName")))))) {
      GUI gui = new GUI();
      GUI.GUIItem guiItem;
      gui.setInventory(ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimsList.OwnerPlots.ItemName")));
      gui.setPlayer(player);
      for (String owner : Claim.getClaimListOwner(player, true)) {
        guiItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.OwnerPlots.Material")));
        gui.addGUIItem(guiItem.setDisplayName(ChatColor.WHITE + owner).setMeta());
      }
      guiItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.BackButton.Material")));
      gui.addGUIItem(guiItem.setDisplayName(LandClaim.plugin.getConfig().getString("GUI.BackButton.ItemName")).setMeta().setSlot(gui.getNumSlots() - 5));
      GUI.saveGUIToPlayerMap(player, gui, false);
      gui.openGUI();
      return;
    }

    //Click List MemberPlots
    if ((clickedItem != null) && ((clickedItem.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimsList.MemberPlots.ItemName")))))) {
      GUI gui = new GUI();
      GUI.GUIItem guiItem;
      gui.setInventory(ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimsList.MemberPlots.ItemName")));
      gui.setPlayer(player);
      for (String member : Claim.getClaimListMember(player, true)) {
        guiItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.MemberPlots.Material")));
        gui.addGUIItem(guiItem.setDisplayName(ChatColor.WHITE + member).setMeta());
      }
      guiItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.BackButton.Material")));
      gui.addGUIItem(guiItem.setDisplayName(LandClaim.plugin.getConfig().getString("GUI.BackButton.ItemName")).setMeta().setSlot(gui.getNumSlots() - 5));
      GUI.saveGUIToPlayerMap(player, gui, false);
      gui.openGUI();
      return;
    }

    //Delete Claim Button
    if ((clickedItem != null) && ((clickedItem.getItemMeta().getDisplayName().startsWith("Remove")))) {
      GUI gui = new GUI();
      String regionName = clickedItem.getItemMeta().getDisplayName().replace("Remove ", "");
      GUI.GUIItem confirmDeleteButton, backButton;
      gui.setInventory("Confirm Remove Claim").setPlayer(player);
      confirmDeleteButton = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.RemoveClaimButton.Material")));
      //backButton = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.BackButton.Material")));
      gui.addGUIItem(confirmDeleteButton.setDisplayName("Are you sure you want to remove " + regionName).setMeta().setSlot(gui.getNumSlots() / 2));
      //gui.addGUIItem(backButton.setDisplayName(LandClaim.plugin.getConfig().getString("GUI.BackButton.ItemName")).setMeta().setSlot(gui.getNumSlots()-5));
      GUI.saveGUIToPlayerMap(player, gui, false);
      gui.openGUI();
      return;
    }

    //Confirm Delete Button
    if ((clickedItem != null) && ((clickedItem.getItemMeta().getDisplayName().startsWith("Are you sure you want to remove")))) {
      Claim.removeRegion(player);
      player.getOpenInventory().close();
    }

    //Click Claim To Open Edit GUI
    String OwnerRegionsInventoryName = ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimsList.OwnerRegions.ItemName"));
    String MemberRegionsInventoryName = ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimsList.MemberRegions.ItemName"));
    String OwnerPlotsInventoryName = ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimsList.OwnerPlots.ItemName"));
    String MemberPlotsInventoryName = ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimsList.MemberPlots.ItemName"));
    String openInventoryName = player.getOpenInventory().getTitle();
    if ((clickedItem != null) && ((openInventoryName.equals(OwnerRegionsInventoryName) || openInventoryName.equals(OwnerPlotsInventoryName)))) {
      String clickedItemName = clickedItem.getItemMeta().getDisplayName();
      List<String> claimList = Claim.playerClaimsMap.get(player.getUniqueId().toString());
      if (claimList != null && claimList.contains(clickedItemName)) {
        GUI gui = new GUI();
        gui.setInventory("Edit " + clickedItemName).setPlayer(player);
        GUI.GUIItem regionGUIItem = null, backButtonItem, deleteButtonItem, addMemberButtonItem, addOwnerButtonItem;
        if (Claim.playerIsOwnerOrMember(player, clickedItemName, player.getWorld().getName()).equalsIgnoreCase("Owner")) {
          if (!Claim.regionIsPlot(player.getWorld(), clickedItemName)) {
            regionGUIItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.OwnerRegions.Material")));
          } else {
            regionGUIItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.OwnerPlots.Material")));
          }

        } else if (Claim.playerIsOwnerOrMember(player, clickedItemName, player.getWorld().getName()).equalsIgnoreCase("Member")) {
          if (!Claim.regionIsPlot(player.getWorld(), clickedItemName)) {
            regionGUIItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.MemberRegions.Material")));
          } else {
            regionGUIItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.MemberPlots.Material")));
          }
        }
        backButtonItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.BackButton.Material")));
        deleteButtonItem = gui.new GUIItem(Material.BARRIER);
        addMemberButtonItem = gui.new GUIItem(Material.OAK_DOOR);
        addOwnerButtonItem = gui.new GUIItem(Material.SPRUCE_DOOR);
        gui.addGUIItem(backButtonItem.setDisplayName(LandClaim.plugin.getConfig().getString("GUI.BackButton.ItemName")).setMeta().setSlot(gui.getNumSlots() - 5));
        gui.addGUIItem(deleteButtonItem.setSlot(gui.getNumSlots() / 2 + 1).setDisplayName("Remove " + clickedItemName).setMeta());
        //gui.addGUIItem(addMemberButtonItem.setSlot(gui.getNumSlots()/2-1).setDisplayName("Add a member to this claim").setMeta());
        //gui.addGUIItem(addOwnerButtonItem.setSlot(gui.getNumSlots()/2-2).setDisplayName("Add an owner to this claim").setMeta());
        gui.addGUIItem(regionGUIItem.setDisplayName(clickedItemName).setSlot(gui.getNumSlots() / 2).setMeta());
        GUI.saveGUIToPlayerMap(player, gui, false);
        gui.openGUI();
      }
    }

    //Back Button
    if ((clickedItem != null) && ((clickedItem.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.BackButton.ItemName")))))) {
      int listSize = GUI.playersGUIMap.get(player.getUniqueId().toString()).size();
      GUI.playersGUIMap.get(player.getUniqueId().toString()).get(listSize - 2).openGUI();
      GUI.playersGUIMap.get(player.getUniqueId().toString()).remove(listSize - 1);
      return;
    }

    if ((clickedItem != null) && (clickedItem.getItemMeta().getDisplayName().equals("Yes, Activate this deed!"))) {
      Claim claim = new Claim(player, clickedItem.getItemMeta().getCustomTagContainer().getCustomTag(DeedListener.regionNameKey, ItemTagType.STRING), clickedItem.getItemMeta().getCustomTagContainer().getCustomTag(DeedListener.worldNameKey, ItemTagType.STRING));
      claim.setNewOwner();
      player.sendMessage("You successfully claimed land!");
      player.getOpenInventory().close();
      return;
    }

    if ((clickedItem != null) && (clickedItem.getItemMeta().getDisplayName().equals("No. Do not activate."))) {
      player.sendMessage("You cancelled the deed activation");
      player.getOpenInventory().close();
      return;
    }
  }
}
