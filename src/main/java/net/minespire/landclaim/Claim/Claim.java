package net.minespire.landclaim.Claim;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.minespire.landclaim.GUI.GUIManager;
import net.minespire.landclaim.LandClaim;
import org.bukkit.*;

import java.util.*;

public class Claim {
  private ProtectedRegion region;
  private BlockVector3 minPoint;
  private BlockVector3 maxPoint;
  private final Player player;
  private World world;
  public static List<org.bukkit.World> worlds = Bukkit.getWorlds();
  private RegionManager rgManager;
  private final String rgName;
  private final org.bukkit.entity.Player bukkitPlayer;
  private double claimCost;
  private int claimArea;
  private int claimVolume;
  private boolean isPlot = false;
  private boolean regionAlreadyExists = false;
  public static Map<String, List<String>> playerClaimsMap = new HashMap<>();
  public static Map<String, ProtectedRegion> awaitingRemovalConfirmation = new HashMap<>();

  public Claim(org.bukkit.entity.Player player, String rgName) {
    this.player = BukkitAdapter.adapt(player);
    this.bukkitPlayer = player;
    this.rgName = rgName;
  }

  public Claim(org.bukkit.entity.Player player, String rgName, boolean isPlot) {
    this.player = BukkitAdapter.adapt(player);
    this.bukkitPlayer = player;
    this.rgName = rgName;
    this.isPlot = isPlot;
  }

  public Claim(org.bukkit.entity.Player player, String rgName, String worldName) {
    this.player = BukkitAdapter.adapt(player);
    this.bukkitPlayer = player;
    this.rgName = rgName;
    this.world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
  }

  public static void teleportToClaim(org.bukkit.entity.Player player, String regionName, String worldName) {
    org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
    if (bukkitWorld == null) {
      player.sendMessage(GUIManager.colorize("&6That world does not exist."));
    } else {
      World world = BukkitAdapter.adapt(bukkitWorld);
      RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
      ProtectedRegion region = rgManager.getRegion(regionName);
      if (region == null) {
        player.sendMessage(GUIManager.colorize("&6That region does not exist."));
      } else {
        if (region.getFlag(Flags.TELE_LOC) != null) {
          player.teleport(new Location(bukkitWorld, region.getFlag(Flags.TELE_LOC).getX(), region.getFlag(Flags.TELE_LOC).getY(), region.getFlag(Flags.TELE_LOC).getZ()));
          BukkitAdapter.adapt(player).setLocation(region.getFlag(Flags.TELE_LOC));
          player.sendMessage(ChatColor.GOLD + "Teleported to " + ChatColor.DARK_PURPLE + regionName);
          player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        } else {
          player.sendMessage(ChatColor.GOLD + "There is no teleport set for this claim.");
        }
      }
    }
  }

  public static void setClaimTeleport(org.bukkit.entity.Player player, String regionName, String worldName) {
    if (!player.getWorld().getName().equals(worldName)) {
      player.sendMessage(ChatColor.GOLD + "You must be standing inside the claim to set a teleport.");
    } else {
      World world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
      RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
      ProtectedRegion region = rgManager.getRegion(regionName);
      Location loc = player.getLocation();
      if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
        region.setFlag(Flags.TELE_LOC, BukkitAdapter.adapt(loc));
        player.sendMessage(ChatColor.GOLD + "You have successfully set the teleport location for " + ChatColor.DARK_PURPLE + regionName + ChatColor.GOLD + ".");
      } else {
        player.sendMessage(ChatColor.GOLD + "You must be standing inside the claim to set a teleport.");
      }
    }
  }

  public static void removeClaimTeleport(org.bukkit.entity.Player player, String regionName, String worldName) {
    org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
    if (bukkitWorld == null) {
      player.sendMessage("That world does not exist.");
    } else {
      World world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
      RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
      ProtectedRegion region = rgManager.getRegion(regionName);
      if (region.getFlag(Flags.TELE_LOC) == null) {
        player.sendMessage(ChatColor.GOLD + "There was no teleport set for this claim.");
      } else {
        region.setFlag(Flags.TELE_LOC, null);
        player.sendMessage(ChatColor.GOLD + "You removed the teleport location for " + ChatColor.DARK_PURPLE + regionName + ChatColor.GOLD + ".");
        player.closeInventory();
      }
    }
  }

  public boolean createClaim() {
    world = this.player.getWorld();
    rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
    if (rgManager == null) {
      return false;
    }
    Map<String, ProtectedRegion> regions = rgManager.getRegions();
    LocalSession session = WorldEdit.getInstance().getSessionManager().get(player);
    Region selection = null;
    try {
      selection = session.getSelection(this.player.getWorld());
    } catch (IncompleteRegionException e) {
      bukkitPlayer.sendMessage(ChatColor.GOLD + "You must select two points first.");
      return false;
    }
    minPoint = selection.getMinimumPoint();
    maxPoint = selection.getMaximumPoint();
    if (!isPlot) {
      minPoint = BlockVector3.at(minPoint.getX(), (player.getWorld().getMaxY() + 1), minPoint.getZ());
      maxPoint = BlockVector3.at(maxPoint.getX(), 0, maxPoint.getZ());
    }
    region = new ProtectedCuboidRegion(rgName, minPoint, maxPoint);

    for (ProtectedRegion rg : regions.values()) {
      if (rg.getId().equals(region.getId())) {
        bukkitPlayer.sendMessage(ChatColor.GOLD + "A region with that name already exists.");
        return false;
      }
    }

    if (!isPlot) {
      region.setFlag(LandClaim.LandClaimRegionFlag, "region");
    } else {
      region.setFlag(LandClaim.LandClaimRegionFlag, "plot");
    }

    calculateClaimArea();
    calculateClaimCost();
    return true;
  }

  public static void queueForRemoval(String playerName, String regionName) {
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getPlayer(playerName).getWorld()));
    if (regions != null) {
      ProtectedRegion region = regions.getRegion(regionName);
      awaitingRemovalConfirmation.put(playerName, region);
    }
  }

  public void deleteRegion() {
    rgManager.removeRegion(rgName);
  }

  public static Set<UUID> getRegionOwners(String regionName, String worldName) {
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
    if (bukkitWorld == null) {
      return null;
    }
    RegionManager regions = container.get(BukkitAdapter.adapt(bukkitWorld));
    if (regions != null) {
      ProtectedRegion region = regions.getRegion(regionName);
      if (region != null) {
        return region.getOwners().getUniqueIds();
      }
    }
    return null;
  }

  public void setNewOwner() {
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionManager regions = container.get(world);
    if (regions != null) {
      ProtectedRegion region = regions.getRegion(rgName);
      DefaultDomain owners = new DefaultDomain();
      owners.addPlayer(player.getUniqueId());
      region.setOwners(owners);
    }
  }

  public void saveClaim() {
    rgManager.addRegion(region);
    DefaultDomain owner = new DefaultDomain();
    owner.addPlayer(player.getUniqueId());
    region.setOwners(owner);
  }

  public boolean overlapsUnownedRegion() {
    Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();
    for (ProtectedRegion rg : region.getIntersectingRegions(regionCollection)) {
      if (!rg.getOwners().contains(player.getUniqueId())) {
        return true;
      }
    }
    return false;
  }

  private void calculateClaimCost() {
    if (isPlot) {
      calculateClaimVolume();
      double tmpCost = LandClaim.plugin.getConfig().getDouble("Claims.Plots.PricePerBlock") * claimVolume;
      double baseCost = LandClaim.plugin.getConfig().getDouble("Claims.Plots.BaseCost");
      claimCost = Math.max(tmpCost, baseCost);
    } else {
      claimCost = claimArea * LandClaim.plugin.getConfig().getDouble("Claims.Regions.PricePerBlock");
    }
  }

  private void calculateClaimArea() {
    claimArea = (maxPoint.getX() - minPoint.getX() + 1) * (maxPoint.getZ() - minPoint.getZ() + 1);
  }

  private void calculateClaimVolume() {
    claimVolume = (maxPoint.getX() - minPoint.getX() + 1) * (maxPoint.getY() - minPoint.getY() + 1) * (maxPoint.getZ() - minPoint.getZ() + 1);
  }

  public int getClaimArea() {
    return claimArea;
  }

  public int getClaimVolume() {
    return claimVolume;
  }

  public int getClaimLength() {
    int side1 = Math.abs(maxPoint.getX() - minPoint.getX() + 1);
    int side2 = Math.abs(maxPoint.getZ() - minPoint.getZ() + 1);
    return Math.max(side1, side2);
  }

  public int getClaimWidth() {
    int side1 = Math.abs(maxPoint.getX() - minPoint.getX() + 1);
    int side2 = Math.abs(maxPoint.getZ() - minPoint.getZ() + 1);
    return Math.min(side1, side2);
  }

  public int getClaimHeight() {
    return Math.abs(maxPoint.getY() - minPoint.getY() + 1);
  }

  public boolean insideOwnedRegion() {
    Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();
    for (ProtectedRegion rg : region.getIntersectingRegions(regionCollection)) {
      if (rg.getOwners().contains(player.getUniqueId())) {
        if (rg.contains(minPoint.getX(), 30, minPoint.getZ()) && rg.contains(minPoint.getX(), 30, maxPoint.getZ()) && rg.contains(maxPoint.getX(), 30, minPoint.getZ()) && rg.contains(maxPoint.getX(), 30, maxPoint.getZ())) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean regionIsPlot(org.bukkit.World bukkitWorld, String regionName) {
    World world = BukkitAdapter.adapt(bukkitWorld);
    RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
    Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();
    for (ProtectedRegion rg : regionCollection) {
      if (rg.getId().equals(regionName)) {
        return rg.getFlag(LandClaim.LandClaimRegionFlag) != null && "plot".equals(rg.getFlag(LandClaim.LandClaimRegionFlag));
      }
    }
    return false;
  }

  public static String playerIsOwnerOrMember(org.bukkit.entity.Player player, String regionName, String worldName) {
    org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
    if (bukkitWorld == null) {
      return null;
    }
    World world = BukkitAdapter.adapt(bukkitWorld);
    RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
    ProtectedRegion region = rgManager.getRegion(regionName);
    if (region == null) {
      return null;
    }
    if (region.getOwners().contains(player.getUniqueId())) {
      return "Owner";
    }
    if (region.getMembers().contains(player.getUniqueId())) {
      return "Member";
    }
    return null;
  }

  public static boolean exists(String regionName, String worldName) {
    org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
    if (bukkitWorld == null) {
      return false;
    }
    World world = BukkitAdapter.adapt(bukkitWorld);
    RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
    return rgManager.getRegion(regionName) != null;
  }

  public static void removeRegion(org.bukkit.entity.Player player) {
    World world = BukkitAdapter.adapt(player.getWorld());
    RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
    String regionName = Claim.awaitingRemovalConfirmation.get(player.getName()).getId();
    rgManager.removeRegion(regionName);
    player.sendMessage(ChatColor.GOLD + "You removed claim " + ChatColor.DARK_PURPLE + regionName);
  }

  public static void removeRegion(org.bukkit.entity.Player player, String regionName, String worldName) {
    org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
    if (bukkitWorld == null) {
      player.sendMessage(ChatColor.GOLD + "That world does not exist.");
    } else {
      World world = BukkitAdapter.adapt(bukkitWorld);
      RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
      rgManager.removeRegion(regionName);
      player.sendMessage(ChatColor.GOLD + "You removed claim " + ChatColor.DARK_PURPLE + regionName);
    }
  }

  public static String parsePlaceholders(String string, Claim claim) {
    return string.replace("{RegionCost}", String.format("$%.2f", claim.getClaimCost()))
      .replace("{RegionSize}", String.valueOf(claim.getClaimArea()))
      .replace("{RegionName}", claim.getRegionName())
      .replace("{PlayerName}", claim.getPlayerName())
      .replace("{RegionLength}", String.valueOf(claim.getClaimLength()))
      .replace("{RegionWidth}", String.valueOf(claim.getClaimWidth()))
      .replace("{RegionHeight}", String.valueOf(claim.getClaimHeight()));
  }

  public static List<String> getClaimListOwner(org.bukkit.entity.Player player, boolean getPlots) {
    return getClaimListPlayer(player, getPlots, true);
  }

  public static List<String> getClaimListMember(org.bukkit.entity.Player player, boolean getPlots) {
    return getClaimListPlayer(player, getPlots, false);
  }

  public double getClaimCost() {
    return claimCost;
  }

  public String getPlayerName() {
    return player.getName();
  }

  public String getRegionName() {
    return rgName;
  }

  public boolean regionExists() {
    return regionAlreadyExists;
  }

  public World getWorld() {
    return world;
  }

  public void setClaimAsPlot() {
    this.isPlot = true;
  }

  public boolean isPlot() {
    return isPlot;
  }

  public static void saveClaimToPlayerMap(org.bukkit.entity.Player player, String claim) {
    List<String> playerClaimList = new ArrayList<>();
    if (playerClaimsMap.containsKey(player.getUniqueId().toString())) {
      playerClaimList = playerClaimsMap.get(player.getUniqueId().toString());
    }
    playerClaimList.add(claim);
    playerClaimsMap.put(player.getUniqueId().toString(), playerClaimList);
  }

  public static boolean addOwner(org.bukkit.entity.Player checkIfOwner, String personToAdd, ProtectedRegion region) {
    return addPlayer(checkIfOwner, personToAdd, region, true);
  }

  public static boolean addMember(org.bukkit.entity.Player checkIfOwner, String personToAdd, ProtectedRegion region) {
    return addPlayer(checkIfOwner, personToAdd, region, false);
  }

  public static boolean removeOwner(org.bukkit.entity.Player checkIfOwner, String personToRemoveUUID, ProtectedRegion region) {
    return removePlayer(checkIfOwner, personToRemoveUUID, region, true);
  }

  public static boolean removeMember(org.bukkit.entity.Player checkIfOwner, String personToRemoveUUID, ProtectedRegion region) {
    return removePlayer(checkIfOwner, personToRemoveUUID, region, false);
  }

  public static ProtectedRegion getRegion(org.bukkit.entity.Player player, String regionName, String worldName) {
    World world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
    RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
    return rgManager.getRegion(regionName);
  }

  private static List<String> getClaimListPlayer(org.bukkit.entity.Player player, boolean getPlots, boolean owner) {
    List<String> claimPlotList = new ArrayList<>();
    List<String> claimRegionList = new ArrayList<>();
    for (org.bukkit.World bukkitWorld : worlds) {
      World world = BukkitAdapter.adapt(bukkitWorld);
      RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
      Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();
      for (ProtectedRegion rg : regionCollection) {
        if ((owner ? rg.getOwners() : rg.getMembers()).contains(player.getUniqueId())) {
          if (getPlots) {
            if (rg.getFlag(LandClaim.LandClaimRegionFlag) != null && "plot".equals(rg.getFlag(LandClaim.LandClaimRegionFlag))) {
              claimPlotList.add(rg.getId() + "," + bukkitWorld.getName());
            }
          } else {
            if (rg.getFlag(LandClaim.LandClaimRegionFlag) != null && "region".equals(rg.getFlag(LandClaim.LandClaimRegionFlag))) {
              claimRegionList.add(rg.getId() + "," + bukkitWorld.getName());
            }
          }
        }
      }
    }
    if (getPlots) {
      return claimPlotList;
    }
    return claimRegionList;
  }

  private static boolean addPlayer(org.bukkit.entity.Player checkIfOwner, String personToAdd, ProtectedRegion region, boolean owner) {
    DefaultDomain regionPlayers = owner ? region.getOwners() : region.getMembers();
    if (checkIfOwner.hasPermission("landclaim.edit.others") || region.getOwners().contains(checkIfOwner.getUniqueId())) {
      org.bukkit.entity.Player playerToAdd = Bukkit.getPlayer(personToAdd);
      if (playerToAdd != null) {
        regionPlayers.addPlayer(WorldGuardPlugin.inst().wrapPlayer(playerToAdd));
        if (owner) {
          region.setOwners(regionPlayers);
        } else {
          region.setMembers(regionPlayers);
        }
        return true;
      }
    }
    return false;
  }

  private static boolean removePlayer(org.bukkit.entity.Player checkIfOwner, String personToRemoveUUID, ProtectedRegion region, boolean owner) {
    DefaultDomain regionPlayers = owner ? region.getOwners() : region.getMembers();
    UUID uuid = UUID.fromString(personToRemoveUUID);
    if (checkIfOwner.hasPermission("landclaim.edit.others") || region.getOwners().contains(checkIfOwner.getUniqueId())) {
      OfflinePlayer playerToRemove = Bukkit.getOfflinePlayer(uuid);
      if (playerToRemove != null) {
        regionPlayers.removePlayer(WorldGuardPlugin.inst().wrapOfflinePlayer(playerToRemove));
        if (owner) {
          region.setOwners(regionPlayers);
        } else {
          region.setMembers(regionPlayers);
        }
        return true;
      }
      return false;
    }
    checkIfOwner.sendMessage(ChatColor.GOLD + "You are not an owner of this claim");
    return false;
  }
}
