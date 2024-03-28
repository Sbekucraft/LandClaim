package net.minespire.landclaim.GUI;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minespire.landclaim.Claim.Claim;
import net.minespire.landclaim.Claim.Claimer;
import net.minespire.landclaim.Claim.VoteRegion;
import net.minespire.landclaim.LandClaim;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GUIManager {

  private static final GUIManager inst = new GUIManager();
  public static Map<String, Flag> editableClaimFlags = new LinkedHashMap<>();

  static {
    editableClaimFlags.put("build", Flags.BUILD);
    editableClaimFlags.put("interact", Flags.INTERACT);
    editableClaimFlags.put("block-break", Flags.BLOCK_BREAK);
    editableClaimFlags.put("block-place", Flags.BLOCK_PLACE);
    editableClaimFlags.put("use", Flags.USE);
    editableClaimFlags.put("damage-animals", Flags.DAMAGE_ANIMALS);
    editableClaimFlags.put("chest-access", Flags.CHEST_ACCESS);
    editableClaimFlags.put("ride", Flags.RIDE);
    editableClaimFlags.put("pvp", Flags.PVP);
    editableClaimFlags.put("sleep", Flags.SLEEP);
    editableClaimFlags.put("respawn-anchors", Flags.RESPAWN_ANCHORS);
    editableClaimFlags.put("tnt", Flags.TNT);
    editableClaimFlags.put("vehicle-place", Flags.PLACE_VEHICLE);
    editableClaimFlags.put("vehicle-destroy", Flags.DESTROY_VEHICLE);
    editableClaimFlags.put("lighter", Flags.LIGHTER);
    editableClaimFlags.put("block-trampling", Flags.TRAMPLE_BLOCKS);
    editableClaimFlags.put("entry", Flags.ENTRY);
    editableClaimFlags.put("entry-deny-message", Flags.ENTRY_DENY_MESSAGE);
    editableClaimFlags.put("greeting", Flags.GREET_MESSAGE);
    editableClaimFlags.put("greeting-title", Flags.GREET_TITLE);
    editableClaimFlags.put("farewell", Flags.FAREWELL_MESSAGE);
    editableClaimFlags.put("farewell-title", Flags.FAREWELL_TITLE);
    editableClaimFlags.put("enderpearl", Flags.ENDERPEARL);
    editableClaimFlags.put("chorus-fruit-teleport", Flags.CHORUS_TELEPORT);
    editableClaimFlags.put("item-pickup", Flags.ITEM_PICKUP);
    editableClaimFlags.put("item-drop", Flags.ITEM_DROP);
    editableClaimFlags.put("deny-message", Flags.DENY_MESSAGE);
  }

  public static GUIManager getInst() {
    return inst;
  }

  public void openMainGUI(Player player) {
    NGUI mainGUI = new NGUI(9, LandClaim.plugin.getLocalizedString("GUI.Title_MainMenu"));
    mainGUI.addItem(Material.GRASS_BLOCK, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_Claims")), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_ClaimsDescription")));
    mainGUI.addItem(Material.STICK, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_Wand")), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_WandDescription")));
    mainGUI.addItem(Material.OBSERVER, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_ClaimLimits")), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_ClaimLimitsDescription")));
    mainGUI.addItem(Material.EMERALD, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_PopularRegions")), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_TopDescription")));
    mainGUI.addItem(Material.BIRCH_DOOR, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_Close")), null, 8);
    mainGUI.open(player);
  }

  public void openClaimLimitsGUI(Player player) {
    NGUI claimLimitsGUI = new NGUI(36, LandClaim.plugin.getLocalizedString("GUI.Title_ClaimLimits"));
    claimLimitsGUI.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 31);
    claimLimitsGUI.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 33);
    int numOwnedRegions, numOwnedPlots, numAllowedRegions, numAllowedPlots;
    numOwnedRegions = Claim.getClaimListOwner(player, false).size();
    numOwnedPlots = Claim.getClaimListOwner(player, true).size();
    numAllowedRegions = Claimer.getNumAllowedRegions(player);
    numAllowedPlots = Claimer.getNumAllowedPlots(player);
    String[] claimableWorlds = Claimer.claimableWorlds(player).stream().map(world -> colorize("&9" + world.getName())).collect(Collectors.toList()).toArray(new String[0]);
    claimLimitsGUI.addItem(Material.FILLED_MAP, colorize(LandClaim.plugin.getLocalizedString("GUI.Text_AllowedWorlds")), createLore(claimableWorlds), 11);
    claimLimitsGUI.addItem(Material.GRASS_BLOCK, colorize(LandClaim.plugin.getLocalizedString("GUI.Text_Regions")), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Text_RegionLore")
            .replace("{AllowedRegions}", Integer.toString(numAllowedRegions))
            .replace("{OwnedRegions}", Integer.toString(numOwnedRegions))
            .replace("{RemainingRegions}", Integer.toString((numAllowedRegions - numOwnedRegions)))), 13);
    claimLimitsGUI.addItem(Material.DIRT, colorize(LandClaim.plugin.getLocalizedString("GUI.Text_Plots")), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Text_PlotsLore")
            .replace("{AllowedPlots}", Integer.toString(numAllowedPlots))
            .replace("{OwnedPlots}", Integer.toString(numOwnedPlots))
            .replace("{RemainingPlots}", Integer.toString((numAllowedPlots - numOwnedPlots)))), 15);
    claimLimitsGUI.open(player);
  }

  public void openAllClaimsGUI(Player player) {openAllClaimsGUI(player, 0);}

  public void openAllClaimsGUI(Player player, int numRegionsToSkip) {
    int nextSlot = 9;
    List<String> allClaims = new LinkedList<>();
    List<String> ownerRegions = Claim.getClaimListOwner(player, false);
    List<String> ownerPlots = Claim.getClaimListOwner(player, true);
    Collections.sort(ownerRegions);
    Collections.sort(ownerPlots);
    allClaims.addAll(ownerRegions);
    allClaims.addAll(ownerPlots);
    List<String> memberRegions = Claim.getClaimListMember(player, false);
    List<String> memberPlots = Claim.getClaimListMember(player, true);
    memberRegions.removeIf(allClaims::contains);
    memberPlots.removeIf(allClaims::contains);
    Collections.sort(memberRegions);
    Collections.sort(memberPlots);
    allClaims.addAll(memberRegions);
    allClaims.addAll(memberPlots);
    int totalSlots = Math.min(((int) Math.ceil(allClaims.size() / 7d) + 2) * 9, 54);
    NGUI allClaimsGUI = new NGUI(totalSlots, LandClaim.plugin.getLocalizedString("GUI.Title_Claims")+" - "+LandClaim.plugin.getLocalizedString("GUI.Text_Page") + " " + (numRegionsToSkip / 28 + 1));
    if (numRegionsToSkip > 0) {
      allClaimsGUI.addItem(Material.PAPER, LandClaim.plugin.getLocalizedString("GUI.Action_PreviousPage"), null, 48);
    }
    int loopCounter = 0;
    for (String rg : allClaims) {
      String rgName = rg.split(",")[0];
      String rgWorld = rg.split(",")[1];
      if (loopCounter++ < numRegionsToSkip) {
        continue;
      }
      if (++nextSlot > 43) {
        allClaimsGUI.addItem(Material.PAPER, LandClaim.plugin.getLocalizedString("GUI.Action_NextPage"), null, 50);
        break;
      }
      if ((nextSlot + 1) % 9 == 0) {
        nextSlot = nextSlot + 2;
      }
      RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getWorld(rgWorld)));
      ProtectedRegion region = rgManager.getRegion(rgName);
      double[] xyz = {(region.getMaximumPoint().getX() - region.getMinimumPoint().getX()), (region.getMaximumPoint().getY() - region.getMinimumPoint().getY()), (region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ())};
      double[] anchor = {Math.min(region.getMaximumPoint().getX(), region.getMinimumPoint().getX()), Math.max(region.getMaximumPoint().getZ(), region.getMinimumPoint().getZ())};
      if (Claim.playerIsOwnerOrMember(player, rgName, rgWorld).equalsIgnoreCase("Owner")) {
        if (!Claim.regionIsPlot(Bukkit.getWorld(rgWorld), rgName)) {
          allClaimsGUI.addItem(Material.DIAMOND_BLOCK, colorize("&5" + rgName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_ClaimDescription")
                  .replace("{WorldName}",rgWorld)
                  .replace("{RegionWidth}", Double.toString(xyz[0]))
                  .replace("{RegionHeight}", Double.toString(xyz[1]))
                  .replace("{RegionLength}", Double.toString(xyz[2]))
                  .replace("{AnchorX}", Double.toString(anchor[0]))
                  .replace("{AnchorZ}", Double.toString(anchor[1]))), nextSlot);
        } else {
          allClaimsGUI.addItem(Material.DIAMOND_ORE, colorize("&5" + rgName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_ClaimDescription")
                  .replace("{WorldName}",rgWorld)
                  .replace("{RegionWidth}", Double.toString(xyz[0]))
                  .replace("{RegionHeight}", Double.toString(xyz[1]))
                  .replace("{RegionLength}", Double.toString(xyz[2]))
                  .replace("{AnchorX}", Double.toString(anchor[0]))
                  .replace("{AnchorZ}", Double.toString(anchor[1]))), nextSlot);
        }
      } else if (Claim.playerIsOwnerOrMember(player, rgName, rgWorld).equalsIgnoreCase("Member")) {
        if (!Claim.regionIsPlot(Bukkit.getWorld(rgWorld), rgName)) {
          allClaimsGUI.addItem(Material.IRON_BLOCK, colorize("&5" + rgName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_ClaimDescription")
                  .replace("{WorldName}",rgWorld)
                  .replace("{RegionWidth}", Double.toString(xyz[0]))
                  .replace("{RegionHeight}", Double.toString(xyz[1]))
                  .replace("{RegionLength}", Double.toString(xyz[2]))
                  .replace("{AnchorX}", Double.toString(anchor[0]))
                  .replace("{AnchorZ}", Double.toString(anchor[1]))), nextSlot);
        } else {
          allClaimsGUI.addItem(Material.DIAMOND_ORE, colorize("&5" + rgName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_ClaimDescription")
                  .replace("{WorldName}",rgWorld)
                  .replace("{RegionWidth}", Double.toString(xyz[0]))
                  .replace("{RegionHeight}", Double.toString(xyz[1]))
                  .replace("{RegionLength}", Double.toString(xyz[2]))
                  .replace("{AnchorX}", Double.toString(anchor[0]))
                  .replace("{AnchorZ}", Double.toString(anchor[1]))), nextSlot);
        }
      }
    }
    allClaimsGUI.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, totalSlots - 5);
    allClaimsGUI.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, totalSlots - 3);
    allClaimsGUI.open(player);
  }

  public void handleWandClick(Player player) {
    Bukkit.dispatchCommand(player, "/wand");
  }

  public List<String> createLore(String... lorePieces) {
    List<String> completedLore = new ArrayList<>();
    for (String lore : lorePieces) {
      completedLore.add(ChatColor.translateAlternateColorCodes('&', lore));
    }
    return completedLore;
  }

  public List<String> parseLoreString(String loreString) {
    String[] loreArray = loreString.split("\\|");
    List<String> loreList = new ArrayList<>();
    for (int x = 0; x < loreArray.length; x++) {
      loreList.add(x, ChatColor.translateAlternateColorCodes('&', loreArray[x]));
    }
    return loreList;
  }

  public static String colorize(String s) {
    return ChatColor.translateAlternateColorCodes('&', s);
  }

  public void openClaimInspector(Player player, String regionName, String worldName) {
    ProtectedRegion region = LandClaim.wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld())).getRegion(regionName);
    Boolean isRegion = region.getFlag(LandClaim.LandClaimRegionFlag) != null && "region".equals(region.getFlag(LandClaim.LandClaimRegionFlag));
    NGUI inspectorGUI = new NGUI(18, LandClaim.plugin.getLocalizedString("GUI.Title_ClaimInspector"));

    inspectorGUI.addItem(Material.PLAYER_HEAD, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_Players")), null);
    if(isRegion) inspectorGUI.addItem(Material.CRAFTING_TABLE, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_ResizeClaim")), null);
    if (player.hasPermission("landclaim.teleport") || player.hasPermission("landclaim.inspect.others")) {
      inspectorGUI.addItem(Material.ENDER_PEARL, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_Teleport")), null);
    }
    String ownerOrMember = Claim.playerIsOwnerOrMember(player, regionName, worldName);
    if (ownerOrMember == null) {
      ownerOrMember = "";
    }
    boolean isOwner = ownerOrMember.equalsIgnoreCase("Owner");
    if ((isOwner && player.hasPermission("landclaim.flageditor")) || player.hasPermission("landclaim.inspect.others")) {
      inspectorGUI.addItem(Material.CYAN_BANNER, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_FlagEditor")), null);
    }
    if ((isOwner && player.hasPermission("landclaim.remove.own")) || player.hasPermission("landclaim.edit.others")) {
      inspectorGUI.addItem(Material.STRUCTURE_VOID, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_Remove") + " " + regionName), null);
    }

    inspectorGUI.addItem(Material.BIRCH_SIGN, colorize("&5" + regionName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Text_World").replace("{WorldName}",worldName)), 11);
    inspectorGUI.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 13);
    inspectorGUI.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 15);
    inspectorGUI.open(player);
  }

  public void openOwnersMembersEditor(Player player, String regionName, String worldName) {
    int firstSlot = 0;
    NGUI inspectorGUI = new NGUI(18, LandClaim.plugin.getLocalizedString("GUI.Title_MembersEditor"));

    inspectorGUI.addItem(Material.PLAYER_HEAD, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_ManagePlayers")), null, firstSlot++);
    String ownerOrMember = Claim.playerIsOwnerOrMember(player, regionName, worldName);
    if ((ownerOrMember != null && ownerOrMember.equalsIgnoreCase("Owner") && player.hasPermission("landclaim.addplayer")) || player.hasPermission("landclaim.edit.others")) {
      inspectorGUI.addItem(Material.TOTEM_OF_UNDYING, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_AddToClaim")), null, firstSlot++);
    }

    inspectorGUI.addItem(Material.BIRCH_SIGN, colorize("&5" + regionName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Text_World").replace("{WorldName}",worldName)), 11);
    inspectorGUI.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 13);
    inspectorGUI.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 15);
    inspectorGUI.open(player);
  }

  public void promptForRemoval(String playerName, String regionName, String worldName) {
    NGUI removalPrompt = new NGUI(36, LandClaim.plugin.getLocalizedString("GUI.Title_ClaimRemoval"));
    removalPrompt.addItem(Material.STRUCTURE_VOID, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_Remove")), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_RegionRemoveDescription")), 13);

    removalPrompt.addItem(Material.BIRCH_SIGN, colorize("&5" + regionName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Text_World").replace("{WorldName}",worldName)), 29);
    removalPrompt.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 31);
    removalPrompt.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 33);
    removalPrompt.open(Bukkit.getPlayer(playerName));
  }

  public void openAddPlayer(Player player, String regionName, String worldName) {
    NGUI addPlayer = new NGUI(36, LandClaim.plugin.getLocalizedString("GUI.Title_AddMember"));
    addPlayer.addItem(Material.WITHER_SKELETON_SKULL, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_AddOwner")), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_RegionOwnershipDescription")), 12);
    addPlayer.addItem(Material.SKELETON_SKULL, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_AddMember")), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_RegionMembershipDescription")), 14);

    addPlayer.addItem(Material.BIRCH_SIGN, colorize("&5" + regionName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Text_World").replace("{WorldName}",worldName)), 29);
    addPlayer.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 31);
    addPlayer.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 33);
    addPlayer.open(player);
  }

  public void openPlayersEditor(Player player, String regionName, String worldName) {
    NGUI playersEditor = new NGUI(54, LandClaim.plugin.getLocalizedString("GUI.Action_ManagePlayers"));
    Set<UUID> owners = LandClaim.wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getWorld(worldName))).getRegion(regionName).getOwners().getPlayerDomain().getUniqueIds();
    for (UUID uuid : owners) {
      playersEditor.addItem(Material.WITHER_SKELETON_SKULL, colorize("&b" + Bukkit.getOfflinePlayer(uuid).getName()), parseLoreString("&7UUID:" + uuid));
    }
    Set<UUID> members = LandClaim.wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getWorld(worldName))).getRegion(regionName).getMembers().getPlayerDomain().getUniqueIds();
    for (UUID uuid : members) {
      playersEditor.addItem(Material.SKELETON_SKULL, colorize("&b" + Bukkit.getOfflinePlayer(uuid).getName()), parseLoreString("&7UUID:" + uuid));
    }

    playersEditor.addItem(Material.BIRCH_SIGN, colorize("&5" + regionName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Text_World").replace("{WorldName}",worldName)), 47);
    playersEditor.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 49);
    playersEditor.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 51);
    playersEditor.open(player);
  }

  public void openMemberRemover(Player player, String uuid, String regionName, String worldName) {
    NGUI removeMember = new NGUI(36, LandClaim.plugin.getLocalizedString("GUI.Title_RemoveMember"));
    removeMember.addItem(Material.BARRIER, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_Confirm")), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_RemoveMemberDescription").replace("{PlayerName}", Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName()).replace("{PlayerUUID}", uuid)), 13);
    removeMember.addItem(Material.BIRCH_SIGN, colorize("&5" + regionName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Text_World").replace("{WorldName}",worldName)), 29);
    removeMember.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 31);
    removeMember.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 33);
    removeMember.open(player);
  }

  public void openOwnerRemover(Player player, String uuid, String regionName, String worldName) {
    NGUI removeOwner = new NGUI(36, LandClaim.plugin.getLocalizedString("GUI.Title_RemoveOwner"));
    removeOwner.addItem(Material.BARRIER, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_Confirm")), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_RemoveOwnerDescription").replace("{PlayerName}", Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName()).replace("{PlayerUUID}", uuid)), 13);
    removeOwner.addItem(Material.BIRCH_SIGN, colorize("&5" + regionName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Text_World").replace("{WorldName}",worldName)), 29);
    removeOwner.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 31);
    removeOwner.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 33);
    removeOwner.open(player);
  }

  public void openTeleportGUI(Player player, String regionName, String worldName) {
    NGUI teleportGUI = new NGUI(36, LandClaim.plugin.getLocalizedString("GUI.Title_ClaimTeleport"));
    String ownerOrMember = Claim.playerIsOwnerOrMember(player, regionName, worldName);
    if ((ownerOrMember != null && ownerOrMember.equalsIgnoreCase("Owner")) || player.hasPermission("landclaim.edit.others")) {
      teleportGUI.addItem(Material.FURNACE, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_RemoveTpPoint")), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_TeleportRemoveDescription").replace("{RegionName}", regionName)), 11);
    }
    teleportGUI.addItem(Material.ENDER_PEARL, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_TeleportTo") + " " + regionName), null, 13);
    if ((ownerOrMember != null && ownerOrMember.equalsIgnoreCase("Owner")) || player.hasPermission("landclaim.edit.others")) {
      teleportGUI.addItem(Material.CRAFTING_TABLE, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_SetTpPoint")), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Lore_TeleportSetDescription").replace("{RegionName}", regionName)), 15);
    }
    teleportGUI.addItem(Material.BIRCH_SIGN, colorize("&5" + regionName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Text_World").replace("{WorldName}",worldName)), 29);
    teleportGUI.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 31);
    teleportGUI.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 33);
    teleportGUI.open(player);
  }

  public void openConfirmResizeGUI(Player player, String regionName, String worldName, Claim claim, double cost){
    NGUI claimGUI = new NGUI(36, LandClaim.plugin.getLocalizedString("GUI.Title_ClaimResize"));
    claimGUI.addItem(Material.BIRCH_SIGN, colorize("&5" + regionName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Text_World").replace("{WorldName}",worldName)), 29);
    claimGUI.addItem(Material.GRASS_BLOCK, LandClaim.plugin.getLocalizedString("GUI.PopupTitle_ClaimRegion"), this.parseLoreString(Claim.parsePlaceholders(LandClaim.plugin.getLocalizedString("GUI.Lore_RegionDescription").replace("{RegionName}", regionName).replace("{RegionCost}", Double.toString(cost)), claim)), 13);
    claimGUI.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 31);
    claimGUI.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 33);
    claimGUI.open(player);
    Claim.queueForRemoval(player.getName(), claim.getRegionName());
  }

  public void openFlagsGUI(Player player, String regionName, String worldName) {
    NGUI flagEditor = new NGUI(45, LandClaim.plugin.getLocalizedString("GUI.Title_ClaimFlags"));
    ProtectedRegion region = Claim.getRegion(player, regionName, worldName);

    editableClaimFlags.forEach((flagName, flag) -> {
      if (player.hasPermission("landclaim.flag." + flagName)) {
        if (region.getFlags().containsKey(editableClaimFlags.get(flagName))) {
          flagEditor.addItem(Material.LIME_BANNER, ChatColor.GOLD + flagName, null);
        } else {
          flagEditor.addItem(Material.GRAY_BANNER, ChatColor.GOLD + flagName, null);
        }
      }

    });

    flagEditor.addItem(Material.BIRCH_SIGN, colorize("&5" + regionName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Text_World").replace("{WorldName}",worldName)), 38);
    flagEditor.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 40);
    flagEditor.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 42);
    flagEditor.open(player);
  }

  public void openStateFlagEditor(Player player, String regionName, String flagName, String worldName) {
    NGUI flagEditor = new NGUI(18, LandClaim.plugin.getLocalizedString("GUI.Title_StateFlagEditor"));

    flagEditor.addItem(Material.DARK_OAK_SIGN, flagName, null, 0);

    ProtectedRegion region = Claim.getRegion(player, regionName, worldName);

    if (region.getFlags().containsKey(editableClaimFlags.get(flagName))) {
      if (region.getFlag(editableClaimFlags.get(flagName)).toString().equalsIgnoreCase("ALLOW")) {
        flagEditor.addItem(Material.EMERALD_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_Allow"), null);
        flagEditor.addItem(Material.REDSTONE_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_Deny"), null);
      } else {
        flagEditor.addItem(Material.REDSTONE_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_Allow"), null);
        flagEditor.addItem(Material.EMERALD_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_Deny"), null);
      }

      flagEditor.addItem(Material.BARRIER, LandClaim.plugin.getLocalizedString("GUI.Action_DeleteFlag"), null);
      RegionGroup regionGroup = region.getFlag(editableClaimFlags.get(flagName).getRegionGroupFlag());

      if (regionGroup == null || regionGroup.equals(RegionGroup.ALL)) {
        flagEditor.addItem(Material.EMERALD_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetEveryone"), null);
      } else {
        flagEditor.addItem(Material.REDSTONE_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetEveryone"), null);
      }

      if (regionGroup != null && regionGroup.equals(RegionGroup.MEMBERS)) {
        flagEditor.addItem(Material.EMERALD_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetMembers"), null);
      } else {
        flagEditor.addItem(Material.REDSTONE_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetMembers"), null);
      }

      if (regionGroup != null && regionGroup.equals(RegionGroup.OWNERS)) {
        flagEditor.addItem(Material.EMERALD_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetOwners"), null);
      } else {
        flagEditor.addItem(Material.REDSTONE_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetOwners"), null);
      }

      if (regionGroup != null && regionGroup.equals(RegionGroup.NON_MEMBERS)) {
        flagEditor.addItem(Material.EMERALD_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetNotMembers"), null);
      } else {
        flagEditor.addItem(Material.REDSTONE_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetNotMembers"), null);
      }

      if (regionGroup != null && regionGroup.equals(RegionGroup.NON_OWNERS)) {
        flagEditor.addItem(Material.EMERALD_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetNotOwners"), null);
      } else {
        flagEditor.addItem(Material.REDSTONE_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetNotOwners"), null);
      }
    } else {
      flagEditor.addItem(Material.REDSTONE_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_Allow"), null);
      flagEditor.addItem(Material.REDSTONE_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_Deny"), null);
      flagEditor.addItem(Material.BARRIER, LandClaim.plugin.getLocalizedString("GUI.Action_DeleteFlag"), null);
      flagEditor.addItem(Material.REDSTONE_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetEveryone"), null);
      flagEditor.addItem(Material.REDSTONE_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetMembers"), null);
      flagEditor.addItem(Material.REDSTONE_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetOwners"), null);
      flagEditor.addItem(Material.REDSTONE_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetNotMembers"), null);
      flagEditor.addItem(Material.REDSTONE_BLOCK, LandClaim.plugin.getLocalizedString("GUI.Action_SetNotOwners"), null);
    }


    flagEditor.addItem(Material.BIRCH_SIGN, colorize("&5" + regionName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Text_World").replace("{WorldName}",worldName)), 11);
    flagEditor.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 13);
    flagEditor.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 15);
    flagEditor.open(player);
  }

  public void openStringFlagEditor(Player player, String regionName, String flagName, String worldName) {
    NGUI flagEditor = new NGUI(18, LandClaim.plugin.getLocalizedString("GUI.Title_StringFlagEditor"));
    List<String> currentFlagText = new ArrayList<>();
    ProtectedRegion region = Claim.getRegion(player, regionName, worldName);
    if (region.getFlags().containsKey(editableClaimFlags.get(flagName))) {
      currentFlagText.add(colorize("&fCurrent Flag Text:"));
      String[] flagTextTokens = region.getFlag(editableClaimFlags.get(flagName)).toString().split(" ");
      int countLineLength = 0;
      StringBuilder stringBuilder = new StringBuilder(50);
      for (String token : flagTextTokens) {
        countLineLength += token.length() + 1;
        stringBuilder.append(token + " ");
        if (countLineLength > 28) {
          currentFlagText.add(stringBuilder.toString());
          stringBuilder.setLength(0);
          countLineLength = 0;
        }
      }
      if (stringBuilder.length() > 0) {
        currentFlagText.add(stringBuilder.toString());
      }
    }
    flagEditor.addItem(Material.DARK_OAK_SIGN, flagName, currentFlagText, 0);
    flagEditor.addItem(Material.WRITABLE_BOOK, colorize(LandClaim.plugin.getLocalizedString("GUI.Action_NewValue")), null, 4);
    flagEditor.addItem(Material.BARRIER, LandClaim.plugin.getLocalizedString("GUI.Action_DeleteFlag"), null, 8);


    flagEditor.addItem(Material.BIRCH_SIGN, colorize("&5" + regionName), parseLoreString(LandClaim.plugin.getLocalizedString("GUI.Text_World").replace("{WorldName}",worldName)), 11);
    flagEditor.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 13);
    flagEditor.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 15);
    flagEditor.open(player);
  }

  public void openTopRegionsGUI(Player player) {
    NGUI topRegionsGUI = new NGUI(9, LandClaim.plugin.getLocalizedString("GUI.Title_TopRegions"));

    topRegionsGUI.addItem(Material.EMERALD, colorize(LandClaim.plugin.getLocalizedString("GUI.Title_YearTopRegions")), null);
    topRegionsGUI.addItem(Material.EMERALD, colorize(LandClaim.plugin.getLocalizedString("GUI.Title_MonthTopRegions")), null);
    topRegionsGUI.addItem(Material.EMERALD, colorize(LandClaim.plugin.getLocalizedString("GUI.Title_DayTopRegions")), null);

    topRegionsGUI.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 7);
    topRegionsGUI.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 8);
    topRegionsGUI.open(player);
  }

  public void openTopYearRegions(Player player) {
    NGUI topYearRegions = new NGUI(36, LandClaim.plugin.getLocalizedString("GUI.Title_YearTopRegions"));
    doGlassPattern1(topYearRegions);
    List<VoteRegion> voteRegions = VoteRegion.getVoteRegionList();
    voteRegions.sort(Comparator.comparingInt(VoteRegion::getVotesThisYear).reversed());
    int countRegions = 1;
    for (VoteRegion region : voteRegions) {
      topYearRegions.addItem(Material.EMERALD, colorize("&6#" + countRegions++ + " &5Region of the Year"), parseLoreString("&5" + region.getRegionCommaWorld().split(",")[0] + "&7,&9" + region.getRegionCommaWorld().split(",")[1] + "|" + LandClaim.plugin.getLocalizedString("GUI.Text_VotesPastYear") + "&6" + region.getVotesThisYear() + "|" + LandClaim.plugin.getLocalizedString("GUI.Text_ClickToVisit")));
      if (countRegions > 7) {
        break;
      }
    }

    topYearRegions.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 31);
    topYearRegions.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 33);
    topYearRegions.open(player);
  }

  public void openTopMonthRegions(Player player) {
    NGUI topMonthRegions = new NGUI(36, LandClaim.plugin.getLocalizedString("GUI.Title_MonthTopRegions"));
    doGlassPattern1(topMonthRegions);
    List<VoteRegion> voteRegions = VoteRegion.getVoteRegionList();
    voteRegions.sort(Comparator.comparingInt(VoteRegion::getVotesThisMonth).reversed());
    int countRegions = 1;
    for (VoteRegion region : voteRegions) {
      topMonthRegions.addItem(Material.EMERALD, colorize("&6#" + countRegions++ + " &5Region of the Month"), parseLoreString("&5" + region.getRegionCommaWorld().split(",")[0] + "&7,&9" + region.getRegionCommaWorld().split(",")[1] + "|" + LandClaim.plugin.getLocalizedString("GUI.Text_VotesPastMonth") + "&6" + region.getVotesThisMonth() + "|" + LandClaim.plugin.getLocalizedString("GUI.Text_ClickToVisit")));
      if (countRegions > 7) {
        break;
      }
    }

    topMonthRegions.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 31);
    topMonthRegions.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 33);
    topMonthRegions.open(player);
  }

  public void openTopDayRegions(Player player) {
    NGUI topDayRegions = new NGUI(36, LandClaim.plugin.getLocalizedString("GUI.Title_DayTopRegions"));
    doGlassPattern1(topDayRegions);
    List<VoteRegion> voteRegions = VoteRegion.getVoteRegionList();
    voteRegions.sort(Comparator.comparingInt(VoteRegion::getVotesToday).reversed());
    int countRegions = 1;
    for (VoteRegion region : voteRegions) {
      topDayRegions.addItem(Material.EMERALD, colorize("&6#" + countRegions++ + " &5Region of the Day"), parseLoreString("&5" + region.getRegionCommaWorld().split(",")[0] + "&7,&9" + region.getRegionCommaWorld().split(",")[1] + "|" + LandClaim.plugin.getLocalizedString("GUI.Text_VotesPastDay") + "&6" + region.getVotesToday() + "|" + LandClaim.plugin.getLocalizedString("GUI.Text_ClickToVisit")));
      if (countRegions > 7) {
        break;
      }
    }

    topDayRegions.addItem(Material.ARROW, LandClaim.plugin.getLocalizedString("GUI.Action_Back"), null, 31);
    topDayRegions.addItem(Material.BIRCH_DOOR, LandClaim.plugin.getLocalizedString("GUI.Action_Close"), null, 33);
    topDayRegions.open(player);
  }

  private void doGlassPattern1(NGUI topYearRegions) {
    for (int i = 0; i < 27; i++) {
      if (i == 10) {
        i += 7;
      }
      topYearRegions.addItem(Material.GLASS_PANE, " ", null, i);
    }
  }
}
