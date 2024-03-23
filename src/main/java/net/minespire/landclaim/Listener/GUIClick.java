package net.minespire.landclaim.Listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minespire.landclaim.Claim.Claim;
import net.minespire.landclaim.GUI.GUIManager;
import net.minespire.landclaim.LandClaim;
import net.minespire.landclaim.Prompt.Prompt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class GUIClick implements Listener {
  public static Map<String, InventoryView> playerLCInventory = new HashMap<>();

  @EventHandler(priority = EventPriority.HIGH)
  public void onInventoryClick(InventoryClickEvent clickEvent) {
    String inventoryTitle = clickEvent.getView().getTitle();
    ItemStack itemStack = clickEvent.getCurrentItem();
    ItemMeta itemMeta;
    String itemName;
    if (itemStack == null) {
      return;
    } else {
      itemMeta = itemStack.getItemMeta();
    }
    if (itemMeta == null) {
      return;
    } else {
      itemName = ChatColor.stripColor(itemMeta.getDisplayName());
    }

    //if(!GUIManager.getInst().isLandClaimGui(inventoryTitle)) return;

    GUIManager guiManager = GUIManager.getInst();
    Player player;
    if (clickEvent.getWhoClicked() instanceof Player) {
      player = ((Player) clickEvent.getWhoClicked()).getPlayer();
    } else {
      return;
    }
    if (!isLandClaimGui(player)) {
      return;
    }
    clickEvent.setCancelled(true);


    if (inventoryTitle.equals("LandClaim Main Menu")) {
      if (itemName.equals("Claims")) {
        guiManager.openAllClaimsGUI(player);
      }
      if (itemName.equals("Claim Limits")) {
        guiManager.openClaimLimitsGUI(player);
      }
      if (itemName.equals("Wand")) {
        guiManager.handleWandClick(player);
      }
      if (itemName.equals("Popular Regions")) {
        guiManager.openTopRegionsGUI(player);
      }
      if (itemName.equals("Close")) {
        player.closeInventory();
      }
      return;
    }

    if (inventoryTitle.startsWith("LandClaim Claims")) {
      if (player.hasPermission("landclaim.inspect.own")) {
        Material mat = itemStack.getType();
        if (mat.equals(Material.DIAMOND_BLOCK)
          || mat.equals(Material.DIAMOND_ORE)
          || mat.equals(Material.IRON_BLOCK)
          || mat.equals(Material.IRON_ORE)) {
          guiManager.openClaimInspector(player, itemName, ChatColor.stripColor(itemMeta.getLore().get(0)).substring(7));
        }
      }
      if (itemName.equals("Next Page")) {
        int numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(24));
        guiManager.openAllClaimsGUI(player, (numRegionsToSkip * 28));
      }
      if (itemName.equals("Previous Page")) {
        int numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(24));
        guiManager.openAllClaimsGUI(player, (numRegionsToSkip - 2) * 28);
      }
      if (itemName.equals("Back")) {
        guiManager.openMainGUI(player);
      }
      if (itemName.equals("Close")) {
        player.closeInventory();
      }
      return;
    }

    if (inventoryTitle.equals("LandClaim Claim Limits")) {
      if (itemName.equalsIgnoreCase("Back")) {
        guiManager.openMainGUI(player);
      }
      if (itemName.equalsIgnoreCase("Close")) {
        player.closeInventory();
      }
      return;
    }

    if (inventoryTitle.equals("LandClaim Inspector")) {
      String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
      String worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getLore().get(0)).substring(7);
      if (itemName.startsWith("Remove")) {
        guiManager.promptForRemoval(player.getName(), regionName, worldName);
      }
      if (itemName.startsWith("Players")) {
        guiManager.openOwnersMembersEditor(player, regionName, worldName);
      }
      if (itemName.startsWith("Flag Editor")) {
        guiManager.openFlagsGUI(player, regionName, worldName);
      }
      if (itemName.startsWith("Teleport")) {
        guiManager.openTeleportGUI(player, regionName, worldName);
      }

      if (ChatColor.stripColor(itemName).equalsIgnoreCase("Back")) {
        guiManager.openAllClaimsGUI(player);
      }
      if (ChatColor.stripColor(itemName).equalsIgnoreCase("Close")) {
        player.closeInventory();
      }
      return;
    }

    if (inventoryTitle.equals("Owners/Members Editor")) {
      String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
      String worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getLore().get(0)).substring(7);
      if (itemName.startsWith("View/Remove Players")) {
        guiManager.openPlayersEditor(player, regionName, worldName);
      }
      if (itemName.startsWith("Add Player to Claim")) {
        guiManager.openAddPlayer(player.getName(), regionName, worldName);
      }

      if (ChatColor.stripColor(itemName).equalsIgnoreCase("Back")) {
        guiManager.openClaimInspector(player, regionName, worldName);
      }
      if (ChatColor.stripColor(itemName).equalsIgnoreCase("Close")) {
        player.closeInventory();
      }
      return;
    }

    if (inventoryTitle.equals("LandClaim Claim Removal")) {
      String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
      String worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
      if (itemName.startsWith("Remove")) {
        LandClaim.econ.depositPlayer(player, LandClaim.claimMap.get(player.getUniqueId().toString()).getClaimCost());
        Claim.removeRegion(player, regionName, worldName);
        guiManager.openAllClaimsGUI(player);
      }

      if (ChatColor.stripColor(itemName).equalsIgnoreCase("Back")) {
        guiManager.openClaimInspector(player, regionName, worldName);
      }
      if (ChatColor.stripColor(itemName).equalsIgnoreCase("Close")) {
        player.closeInventory();
      }
      return;
    }

    if (inventoryTitle.equals("View/Remove Players")) {
      String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(47).getItemMeta().getDisplayName());
      String worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(47).getItemMeta().getLore().get(0)).substring(7);
      if (itemName.equalsIgnoreCase("Back")) {
        guiManager.openOwnersMembersEditor(player, regionName, worldName);
        return;
      }
      if (itemName.equalsIgnoreCase("Close")) {
        player.closeInventory();
        return;
      }

      if (player.hasPermission("landclaim.removeplayer")) {
        ItemMeta clickedItemMeta = clickEvent.getCurrentItem().getItemMeta();
        String firstLore = "";
        if (clickedItemMeta.getLore() != null) {
          firstLore = ChatColor.stripColor(clickedItemMeta.getLore().get(0));
        }
        if (clickedItemMeta != null && firstLore.startsWith("UUID")) {
          String ownerOrMember = Claim.playerIsOwnerOrMember(player, regionName, worldName);
          if ((ownerOrMember != null && ownerOrMember.equalsIgnoreCase("Owner")) || player.hasPermission("landclaim.edit.others")) {
            if (itemStack.getType().equals(Material.WITHER_SKELETON_SKULL)) {
              guiManager.openOwnerRemover(player, firstLore.substring(5), regionName, worldName);
            }
            if (itemStack.getType().equals(Material.SKELETON_SKULL)) {
              guiManager.openMemberRemover(player, firstLore.substring(5), regionName, worldName);
            }
          } else {
            player.sendMessage(ChatColor.GOLD + "Only claim owners can remove players from claims.");
          }
        }
      }
      return;
    }

    if (inventoryTitle.equalsIgnoreCase("Remove Member")) {
      String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
      String worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
      String uuid = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(1)).substring(6);
      String playerToRemoveName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(0)).substring(15);
      if (itemName.startsWith("Are you sure")) {
        World world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
        RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
        if (Claim.removeMember(player, uuid, rgManager.getRegion(regionName))) {
          player.sendMessage(ChatColor.GOLD + "You removed member " + ChatColor.AQUA + playerToRemoveName + ChatColor.GOLD + " from " + ChatColor.AQUA + regionName + ChatColor.GOLD + ".");
        }
        player.closeInventory();
      }
      if (ChatColor.stripColor(itemName).startsWith("Back")) {
        guiManager.openPlayersEditor(player, regionName, worldName);
      }
      if (ChatColor.stripColor(itemName).startsWith("Close")) {
        player.closeInventory();
      }
      return;
    }

    if (inventoryTitle.equalsIgnoreCase("Remove Owner")) {
      String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
      String worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
      String uuid = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(1)).substring(6);
      String playerToRemoveName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(0)).substring(14);
      if (itemName.startsWith("Are you sure")) {
        World world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
        RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
        if (Claim.removeOwner(player, uuid, rgManager.getRegion(regionName))) {
          player.sendMessage(ChatColor.GOLD + "You removed owner " + ChatColor.AQUA + playerToRemoveName + ChatColor.GOLD + " from " + ChatColor.AQUA + regionName + ChatColor.GOLD + ".");
        }
        player.closeInventory();
      }
      if (ChatColor.stripColor(itemName).startsWith("Back")) {
        guiManager.openPlayersEditor(player, regionName, worldName);
      }
      if (ChatColor.stripColor(itemName).startsWith("Close")) {
        player.closeInventory();
      }
      return;
    }

    if (inventoryTitle.equals("Add Player to Claim")) {
      String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
      String worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
      if (itemName.startsWith("Add Owner to")) {
        World world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
        RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
        Prompt prompt = new Prompt(ChatColor.GOLD + "Who would you like to add as an owner? " + ChatColor.RED +
          "/lc cancel" + ChatColor.GOLD + " to cancel", player, "ADDOWNER", rgManager.getRegion(regionName));
        prompt.sendPrompt();
        player.closeInventory();
      }
      if (itemName.startsWith("Add Member to")) {
        World world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
        RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
        Prompt prompt = new Prompt(ChatColor.GOLD + "Who would you like to add as a member? " + ChatColor.RED +
          "/lc cancel" + ChatColor.GOLD + " to cancel", player, "ADDMEMBER", rgManager.getRegion(regionName));
        prompt.sendPrompt();
        player.closeInventory();
      }
      if (itemName.startsWith("Back")) {
        guiManager.openOwnersMembersEditor(player, regionName, worldName);
      }
      if (itemName.startsWith("Close")) {
        player.closeInventory();
      }
      return;
    }

    if (inventoryTitle.equals("LandClaim Teleport")) {
      String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
      String worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
      if (itemName.startsWith("Teleport to ")) {
        Claim.teleportToClaim(player, regionName, worldName);
      }
      if (itemName.equalsIgnoreCase("Set Teleport Point")) {
        Claim.setClaimTeleport(player, regionName, worldName);
      }
      if (itemName.equalsIgnoreCase("Remove Teleport Point")) {
        Claim.removeClaimTeleport(player, regionName, worldName);
      }
      if (itemName.startsWith("Back")) {
        guiManager.openClaimInspector(player, regionName, worldName);
      }
      if (itemName.startsWith("Close")) {
        player.closeInventory();
      }
      return;
    }

    if (inventoryTitle.equals("LandClaim Flags")) {
      String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(38).getItemMeta().getDisplayName());
      String worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(38).getItemMeta().getLore().get(0)).substring(7);
      if (itemStack.getType().equals(Material.LIME_BANNER) || itemStack.getType().equals(Material.GRAY_BANNER)) {
        if (GUIManager.editableClaimFlags.get(itemName) instanceof StateFlag) {
          guiManager.openStateFlagEditor(player, regionName, itemName, worldName);
        }
        if (GUIManager.editableClaimFlags.get(itemName) instanceof StringFlag) {
          guiManager.openStringFlagEditor(player, regionName, itemName, worldName);
        }
      }
      if (ChatColor.stripColor(itemName).startsWith("Back")) {
        guiManager.openClaimInspector(player, regionName, worldName);
      }
      if (ChatColor.stripColor(itemName).startsWith("Close")) {
        player.closeInventory();
      }
      return;
    }

    if (inventoryTitle.equals("LandClaim State Flag Editor")) {
      String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
      String worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getLore().get(0)).substring(7);
      if (itemName.equalsIgnoreCase("Back")) {
        guiManager.openFlagsGUI(player, regionName, worldName);
        return;
      }
      if (itemName.equalsIgnoreCase("Close")) {
        player.closeInventory();
        return;
      }
      if (itemStack.getType().equals(Material.DARK_OAK_SIGN) || itemStack.getType().equals(Material.BIRCH_SIGN)) {
        return;
      }

      boolean nonOwnerInspector = !Claim.getRegionOwners(regionName, worldName).contains(player.getUniqueId());
      boolean nonOwnerEditor = player.hasPermission("landclaim.edit.others");

      if (nonOwnerInspector && !nonOwnerEditor) {
        player.sendMessage(ChatColor.GOLD + "You cannot edit this claim");
        return;
      }

      Flag flag = GUIManager.editableClaimFlags.get(clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName());
      ProtectedRegion region = Claim.getRegion(player, regionName, worldName);

      if (itemName.equalsIgnoreCase("Delete Flag")) {
        region.setFlag(flag, null);
      }

      if (itemName.equalsIgnoreCase("Allow")) {
        region.setFlag(flag, StateFlag.State.ALLOW);
      }
      if (itemName.equalsIgnoreCase("Deny")) {
        region.setFlag(flag, StateFlag.State.DENY);
      }

      if (itemName.equalsIgnoreCase("Set for everyone")) {
        region.setFlag(flag.getRegionGroupFlag(), RegionGroup.ALL);
      }
      if (itemName.equalsIgnoreCase("Set for members")) {
        region.setFlag(flag.getRegionGroupFlag(), RegionGroup.MEMBERS);
      }
      if (itemName.equalsIgnoreCase("Set for owners")) {
        region.setFlag(flag.getRegionGroupFlag(), RegionGroup.OWNERS);
      }
      if (itemName.equalsIgnoreCase("Set for non-members")) {
        region.setFlag(flag.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
      }
      if (itemName.equalsIgnoreCase("Set for non-owners")) {
        region.setFlag(flag.getRegionGroupFlag(), RegionGroup.NON_OWNERS);
      }

      guiManager.openStateFlagEditor(player, regionName, clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName(), worldName);
      return;
    }

    if (inventoryTitle.equals("LandClaim String Flag Editor")) {
      String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
      String worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getLore().get(0)).substring(7);
      if (itemName.equalsIgnoreCase("Back")) {
        guiManager.openFlagsGUI(player, regionName, worldName);
        return;
      }
      if (itemName.equalsIgnoreCase("Close")) {
        player.closeInventory();
        return;
      }
      boolean nonOwnerInspector = !Claim.getRegionOwners(regionName, worldName).contains(player.getUniqueId());
      boolean nonOwnerEditor = player.hasPermission("landclaim.edit.others");

      String flagName = clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName();
      Flag flag = GUIManager.editableClaimFlags.get(flagName);
      ProtectedRegion region = Claim.getRegion(player, regionName, worldName);

      if (itemName.equalsIgnoreCase("Enter New Value")) {
        if (nonOwnerInspector && !nonOwnerEditor) {
          player.sendMessage(ChatColor.GOLD + "You cannot edit this claim");
          return;
        }
        player.closeInventory();
        new Prompt(ChatColor.GOLD + "Enter a new value for the '" + flagName + "' flag", player, flagName, region).sendPrompt();
      }
      if (itemName.equalsIgnoreCase("Delete Flag")) {
        if (nonOwnerInspector && !nonOwnerEditor) {
          player.sendMessage(ChatColor.GOLD + "You cannot edit this claim");
          return;
        }
        region.setFlag(flag, null);
        player.sendMessage(ChatColor.GOLD + "Removed flag '" + flagName + "' from " + ChatColor.AQUA + regionName);
      }
      return;
    }

    if (inventoryTitle.equals("LandClaim Top Regions")) {
      if (itemName.endsWith("Year")) {
        guiManager.openTopYearRegions(player);
      }
      if (itemName.endsWith("Month")) {
        guiManager.openTopMonthRegions(player);
      }
      if (itemName.endsWith("Day")) {
        guiManager.openTopDayRegions(player);
      }

      if (itemName.startsWith("Back")) {
        guiManager.openMainGUI(player);
      }
      if (itemName.startsWith("Close")) {
        player.closeInventory();
      }
      return;
    }

    if (inventoryTitle.equalsIgnoreCase("LandClaim Top Regions - Year") ||
      inventoryTitle.equalsIgnoreCase("LandClaim Top Regions - Month") ||
      inventoryTitle.equalsIgnoreCase("LandClaim Top Regions - Day")) {

      if (itemName.startsWith("Back")) {
        guiManager.openTopRegionsGUI(player);
      }
      if (itemName.startsWith("Close")) {
        player.closeInventory();
      }
      if (itemStack.getType().equals(Material.EMERALD)) {
        String regionName = ChatColor.stripColor(itemMeta.getLore().get(0)).split(",")[0];
        String worldName = ChatColor.stripColor(itemMeta.getLore().get(0)).split(",")[1];
        Claim.teleportToClaim(player, regionName, worldName);
      }
      return;
    }

    if (inventoryTitle.equalsIgnoreCase("LandClaim Claim Region") || inventoryTitle.equalsIgnoreCase("LandClaim Claim Plot")) {
      if (itemName.startsWith("Back")) {
        guiManager.openMainGUI(player);
      }
      if (itemName.startsWith("Close")) {
        player.closeInventory();
      }
      if ((itemName.equalsIgnoreCase("Are You Sure You Want To Claim This Region?"))
        || (itemName.equalsIgnoreCase("Are You Sure You Want To Claim This Plot?"))) {
        Claim claim = LandClaim.claimMap.get(player.getUniqueId().toString());
        claim.saveClaim();
        //DeedListener deed = new DeedListener(player, claim.getRegionName(), claim.getWorld().getName(), true);
        //player.getInventory().addItem(deed.createDeed());
        if (!claim.isPlot()) {
          player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6You successfully claimed a region of land for &b$" + claim.getClaimCost() + "&6!"));
        } else if (claim.isPlot()) {
          player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6You successfully claimed a plot of land for &b$" + claim.getClaimCost() + "&6!"));
        }

        LandClaim.econ.withdrawPlayer(player, claim.getClaimCost());
        player.closeInventory();
        return;
      }
    }
  }

  public static boolean isLandClaimGui(Player player) {
    return player.getOpenInventory().equals(GUIClick.playerLCInventory.get(player.getUniqueId().toString()));
  }
}
