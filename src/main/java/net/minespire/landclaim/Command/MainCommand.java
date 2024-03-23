package net.minespire.landclaim.Command;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minespire.landclaim.Claim.*;
import net.minespire.landclaim.GUI.GUIManager;
import net.minespire.landclaim.GUI.NGUI;
import net.minespire.landclaim.LandClaim;
import net.minespire.landclaim.Prompt.Prompt;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MainCommand implements CommandExecutor {
  private Player player = null;
  private boolean isPlayer = false;
  private GUIManager guiManager = GUIManager.getInst();

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (sender instanceof Player) {
      player = (Player) sender;
      isPlayer = true;
    }

    if (args.length < 1) {
      if (isPlayer) {
        GUIManager.getInst().openMainGUI(player);
      }
      return true;
    }

    try {
      switch (args[0].toLowerCase()) {
        case "gui":
          if (isPlayer) {
            GUIManager.getInst().openMainGUI(player);
          }
          break;
        case "claim":
          if (player != null) {
            if (args[1] == null) {
              return false;
            }
            if (!Claimer.permToOwnAnotherRegion(player) && !player.isOp()) {
              player.sendMessage(ChatColor.RED + "You don't have permission to claim or have claimed too many regions.");
              return true;
            }
            if (!Claimer.permissionToClaimInWorld(player) && !player.isOp()) {
              player.sendMessage(ChatColor.RED + "You don't have permission to claim in this world.");
              return true;
            }
            if (!ProtectedRegion.isValidId(args[1])) {
              player.sendMessage(ChatColor.GOLD + "That is not a valid region name.");
              return true;
            }

            Claim claim = new Claim(player, args[1]);
            if (!claim.createClaim()) {
              return true;
            }

            if (claim.overlapsUnownedRegion()) {
              player.sendMessage(ChatColor.GOLD + "Your selection overlaps a region belonging to someone else!");
              return true;
            }
            if (!meetsClaimLimits(claim)) {
              return true;
            }
            if (LandClaim.econ.getBalance(player) < claim.getClaimCost()) {
              player.sendMessage(ChatColor.GOLD + "You don't have enough money to claim this region for $" + claim.getClaimCost());
              return true;
            }
            LandClaim.claimMap.put(player.getUniqueId().toString(), claim);

            NGUI claimGUI = new NGUI(36, "LandClaim Claim Region");
            claimGUI.addItem(Material.GRASS_BLOCK, ChatColor.translateAlternateColorCodes('&', "&3Are You Sure You Want To Claim This Region?"), guiManager.parseLoreString(Claim.parsePlaceholders("&5Region Name: &f{RegionName}|&5Region Cost: &f{RegionCost}|&5Region Size: &f{RegionLength} x {RegionHeight} x {RegionWidth}|&aClick to confirm purchase.", claim)), 13);
            claimGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 31);
            claimGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 33);
            claimGUI.open(player);


//						GUI gui = new GUI(LandClaim.plugin.getConfig().getInt("GUI.Slots"));
//						gui.setInventory(LandClaim.plugin.getConfig().getString("GUI.ClaimRegion.Title"));
//						GUI.GUIItem claimItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimRegion.ClaimItem.Material")));
//						gui.setPlayer(player);
//						gui.addGUIItem(claimItem.setDisplayName(Claim.parsePlaceholders(LandClaim.plugin.getConfig().getString("GUI.ClaimRegion.ClaimItem.ItemName"), claim))
//								.setLore(Claim.parsePlaceholders(LandClaim.plugin.getConfig().getString("GUI.ClaimRegion.ClaimItem.Lore"), claim))
//								.setSlot(LandClaim.plugin.getConfig().getInt("GUI.ClaimRegion.ClaimItem.Slot")-1)
//								.setMeta());
//
//						gui.openGUI();
          } else {
            sender.sendMessage("You must be a player to use that command!");
          }
          break;
        case "claimplot":
          if (player != null) {
            if (args[1] == null) {
              return false;
            }
            if (!Claimer.permToOwnAnotherPlot(player) && !player.isOp()) {
              player.sendMessage(ChatColor.RED + "You don't have permission to claim or have claimed too many plots.");
              return true;
            }
            if (!Claimer.permissionToClaimInWorld(player) && !player.isOp()) {
              player.sendMessage(ChatColor.RED + "You don't have permission to claim in this world.");
              return true;
            }

            if (!ProtectedRegion.isValidId(args[1])) {
              player.sendMessage(ChatColor.GOLD + "That is not a valid plot name.");
              return true;
            }

            Claim claim = new Claim(player, args[1], true);
            if (!claim.createClaim()) {
              return true;
            }

            if (!claim.insideOwnedRegion()) {
              player.sendMessage(ChatColor.GOLD + "You may only claim a plot inside a region that you own.");
              return true;
            }
            if (claim.overlapsUnownedRegion()) {
              player.sendMessage(ChatColor.GOLD + "Your selection overlaps a region belonging to someone else.");
              return true;
            }
            if (LandClaim.econ.getBalance(player) < claim.getClaimCost()) {
              player.sendMessage(ChatColor.GOLD + "You don't have enough money to claim this region for $" + claim.getClaimCost());
              return true;
            }
            LandClaim.claimMap.put(player.getUniqueId().toString(), claim);


            NGUI claimGUI = new NGUI(36, "LandClaim Claim Plot");
            claimGUI.addItem(Material.DIRT, ChatColor.translateAlternateColorCodes('&', "&3Are You Sure You Want To Claim This Plot?"), guiManager.parseLoreString(Claim.parsePlaceholders("&5Region Name: &f{RegionName}|&5Region Cost: &f{RegionCost}|&5Region Size: &f{RegionLength} x {RegionHeight} x {RegionWidth}|&aClick to confirm purchase.", claim)), 13);
            claimGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 31);
            claimGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 33);
            claimGUI.open(player);

//						GUI gui = new GUI(LandClaim.plugin.getConfig().getInt("GUI.Slots"));
//						gui.setInventory(LandClaim.plugin.getConfig().getString("GUI.ClaimPlot.Title"));
//						GUI.GUIItem claimItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimPlot.ClaimItem.Material")));
//						gui.setPlayer(player);
//						gui.addGUIItem(claimItem.setDisplayName(Claim.parsePlaceholders(LandClaim.plugin.getConfig().getString("GUI.ClaimPlot.ClaimItem.ItemName"), claim))
//								.setLore(Claim.parsePlaceholders(LandClaim.plugin.getConfig().getString("GUI.ClaimPlot.ClaimItem.Lore"), claim))
//								.setSlot(LandClaim.plugin.getConfig().getInt("GUI.ClaimPlot.ClaimItem.Slot")-1)
//								.setMeta());
//
//						gui.openGUI();
          } else {
            sender.sendMessage("You must be a player to use that command!");
          }
          break;
        case "reload":
          if ((sender instanceof Player) && !player.hasPermission("landclaim.reload") && !player.isOp()) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
            return true;
          }
          LandClaim.plugin.reloadConfig();
          LandClaim.plugin.getCommand("lc").setTabCompleter(new CommandCompleter());
          LandClaim.plugin.getCommand("lc").setExecutor(new MainCommand());
          sender.sendMessage(ChatColor.GOLD + "LandClaim config reloaded!");
          break;
        case "cancel":
          if (Prompt.hasActivePrompt(player)) {
            Prompt.getPrompt(player.getName()).cancelPrompt();
          }
          break;
        case "nearby":
          if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use that command.");
          }
          if ((sender instanceof Player) && !player.hasPermission("landclaim.nearby")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
            return true;
          }

          String playerName = player.getName();
          Player nPlayer = Bukkit.getPlayer(player.getName());
          if (Visualizer.seeNearbyBukkitTask.containsKey(playerName)) {
            if (Visualizer.seeNearbyAsyncService.containsKey(playerName)) {
              Bukkit.getScheduler().cancelTask(Visualizer.seeNearbyBukkitTask.get(playerName));
              Visualizer.seeNearbyAsyncService.get(playerName).shutdown();
              Visualizer.seeNearbyBukkitTask.remove(playerName);
              Visualizer.seeNearbyAsyncService.remove(playerName);
              Visualizer.timer.remove(playerName);
              nPlayer.sendMessage(ChatColor.GOLD + "Disabled nearby region viewing.");
              return true;
            }
          }
          AtomicInteger timer = new AtomicInteger(0);
          Visualizer.timer.put(playerName, timer);
          nPlayer.sendMessage(ChatColor.GOLD + "Enabled nearby region viewing.");

          try {
            Visualizer.seeNearbyAsyncService.put(playerName, Executors.newSingleThreadScheduledExecutor());
            final Runnable task = () -> {
              Visualizer.timer.get(playerName).getAndAdd(2);
              Visualizer.seeNearbyRegions(nPlayer);
              if (Visualizer.timer.get(playerName).get() >= 14) {
                Visualizer.seeNearbyAsyncService.get(playerName).shutdown();
                Visualizer.seeNearbyAsyncService.remove(playerName);
              }
            };
            Visualizer.seeNearbyAsyncService.get(playerName).scheduleAtFixedRate(task, 0L, 2L, TimeUnit.SECONDS);
          } catch (Throwable t) {
            t.printStackTrace();
          }

          BukkitRunnable bukkitTask = new BukkitRunnable() {
            Queue<BlockVector3> particleLocations = Visualizer.playerParticleCoords.get(playerName);
            int maxSeconds = 15;
            int ticks = 0;

            @Override
            public void run() {
              ticks++;
              if (particleLocations != null) {
                int loops = Math.min(particleLocations.size(), 400);
                for (int i = 0; i < loops; i++) {
                  if (particleLocations == null) {
                    break;
                  }
                  BlockVector3 loc = particleLocations.remove();
                  Location location = Visualizer.getBestSpawnLocation(nPlayer.getWorld(), loc.getX(), loc.getY(), loc.getZ());
                  if (location != null) {
                    nPlayer.spawnParticle(Particle.BLOCK_MARKER, location.getX() + .5, location.getY() + .5, location.getZ() + .5, 1, Bukkit.createBlockData(Material.BARRIER));
                  }
                }
              } else {
                particleLocations = Visualizer.playerParticleCoords.get(playerName);
              }
              if (ticks / 20 == maxSeconds) {
                this.cancel();
                Visualizer.seeNearbyBukkitTask.remove(playerName);
                nPlayer.sendMessage(ChatColor.GOLD + "Disabled nearby region viewing.");
              }
            }
          };
          bukkitTask.runTaskTimer(LandClaim.plugin, 0, 1);
          Integer taskID = bukkitTask.getTaskId();
          Visualizer.seeNearbyBukkitTask.put(player.getName(), taskID);
          break;
        case "teleport":
          if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to do that.");
            return true;
          }
          if (!player.hasPermission("landclaim.teleport")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
          }
          if (args.length < 2) {
            player.sendMessage(ChatColor.GOLD + "Teleport to a claim with /lc teleport [region],[world]");
            return true;
          }
          Claim.teleportToClaim(player, args[1].split(",")[0], args[1].split(",")[1]);
          return true;
        case "delete":
          if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to do that.");
            return true;
          }
          if (!player.hasPermission("landclaim.delete.own")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
          }
          if (args.length < 2) {
            player.sendMessage(ChatColor.GOLD + "You must specify the claim you want to remove.");
            return true;
          }
          if (args[1].split(",").length < 2) {
            player.sendMessage(ChatColor.GOLD + "Please format region as " + ChatColor.AQUA + "[region],[worldName]" + ChatColor.GOLD + ". Type /lc world to get the world name.");
            return true;
          }
          Set<UUID> regionOwners = Claim.getRegionOwners(args[1].split(",")[0], args[1].split(",")[1]);
          if (regionOwners != null) {
            if (regionOwners.contains(player.getUniqueId()) || player.hasPermission("landclaim.delete.others")) {
              guiManager.promptForRemoval(player.getName(), args[1].split(",")[0], args[1].split(",")[1]);
              return true;
            } else {
              player.sendMessage(ChatColor.RED + "You don't own that region.");
            }
          } else {
            player.sendMessage(ChatColor.GOLD + "Invalid Region");
          }
          break;
        case "list":
          if (!player.hasPermission("landclaim.list")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
            return true;
          }
          guiManager.openAllClaimsGUI(player);
          break;
        case "recountvotes":
          if (!player.hasPermission("landclaim.recountvotes") && !player.isOp()) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
            return true;
          }
          VoteRegion.tallyAllVotes();
          player.sendMessage(GUIManager.colorize("&6Recounted votes."));
          break;
        case "world":
          if (!player.hasPermission("landclaim.getworld") && !player.isOp()) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
          } else {
            sender.sendMessage(ChatColor.GOLD + "You are in world: " + ChatColor.AQUA + player.getWorld().getName());
          }
          break;
        case "inspect":
          if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to do that.");
            return true;
          }
          if (!player.hasPermission("landclaim.inspect.own") && !player.isOp()) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
          }
          if (args.length < 2) {
            player.sendMessage(ChatColor.GOLD + "You must specify the claim you want to inspect.");
            return true;
          }
          if (args[1].split(",").length < 2) {
            player.sendMessage(ChatColor.GOLD + "Please format region as " + ChatColor.AQUA + "[region],[worldName]" + ChatColor.GOLD + ". Type /lc world to get the world name.");
            return true;
          }
          String regionName = args[1].split(",")[0];
          String worldName = args[1].split(",")[1];
          if (!Claim.exists(regionName, worldName)) {
            player.sendMessage(ChatColor.GOLD + "That claim does not exist");
            return true;
          }
          String ownerOrMember = Claim.playerIsOwnerOrMember(player, regionName, worldName);
          if ((ownerOrMember != null && player.hasPermission("landclaim.inspect.own")) || player.hasPermission("landclaim.inspect.others")) {
            guiManager.openClaimInspector(player, args[1].split(",")[0], args[1].split(",")[1]);
            return true;
          } else {
            player.sendMessage(ChatColor.GOLD + "You cannot inspect that region.");
          }
          break;
        case "vote":
          if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.GOLD + "You must be a player to do that.");
            return true;
          }
          if (!player.hasPermission("landclaim.vote") && !player.isOp()) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
          }
          if (args.length < 2) {
            List<ProtectedRegion> regionsList = Claims.getRegionsAtLocation(player.getLocation());
            if (regionsList.size() == 0) {
              player.sendMessage(ChatColor.GOLD + "You are not standing in any regions. /lc vote [REGION] to vote for a region.");
              return true;
            }
            if (regionsList.size() == 1) {
              VoteFile voteFile = VoteFile.get();
              Vote lastVote = voteFile.getLatestVote(regionsList.get(0).getId(), player.getWorld().getName(), player.getUniqueId().toString());
              if (lastVote != null && !lastVote.dayHasPassed()) {
                player.sendMessage(ChatColor.GOLD + "You must wait " + (1 - lastVote.daysSinceLastVote()) + " days before voting for that region again");
                return true;
              }
              VoteFile.get().addVote(regionsList.get(0).getId(), player.getWorld().getName(), player.getUniqueId().toString()).save();
              VoteRegion.addVote(regionsList.get(0).getId() + "," + player.getWorld().getName());
              player.sendMessage(ChatColor.GOLD + "Vote registered successfully!");
              return true;
            }
            if (regionsList.size() > 1) {
              String message = ChatColor.GOLD + "You are standing in regions: ";
              StringBuilder extraMessage = new StringBuilder();
              for (ProtectedRegion region : regionsList) {
                extraMessage.append(region.getId()).append(", ");
              }
              extraMessage.delete(extraMessage.length() - 2, extraMessage.length());
              player.sendMessage(message + extraMessage + ".");
              player.sendMessage(ChatColor.GOLD + "/lc vote [REGION] to pick which region");
            }
            return true;
          } else {
            ProtectedRegion region = Claims.getRegionByName(args[1], BukkitAdapter.adapt(player.getWorld()));
            if (region != null) {
              VoteFile voteFile = VoteFile.get();
              Vote lastVote = voteFile.getLatestVote(region.getId(), player.getWorld().getName(), player.getUniqueId().toString());
              if (lastVote != null && !lastVote.dayHasPassed()) {
                player.sendMessage(ChatColor.GOLD + "You must wait " + (1 - lastVote.daysSinceLastVote()) + " days before vote for that region again");
                return true;
              }
              VoteFile.get().addVote(region.getId(), player.getWorld().getName(), player.getUniqueId().toString()).save();
              VoteRegion.addVote(region.getId() + "," + player.getWorld().getName());
              player.sendMessage(ChatColor.GOLD + "Vote registered successfully!");
              return true;
            } else {
              player.sendMessage(ChatColor.GOLD + "That is not a valid region.");
            }
          }
          break;
				/*
				case "blankdeed":
					if (player != null) {
						player.getInventory().addItem(DeedListener.getBlankDeed());
					} else sender.sendMessage("You must be a player to use that command!");
					break;*/
        default:
          return false;
      }
    } catch (IndexOutOfBoundsException e) {
      return false;
    }

    return true;
  }

  public boolean meetsClaimLimits(Claim claim) {
    if (claim.isPlot()) {
      return true;
    } else {
      int area = claim.getClaimArea();
      int length = claim.getClaimLength();
      int width = claim.getClaimWidth();
      if (area < LandClaim.plugin.getConfig().getInt("Claims.Regions.MinSize")) {
        player.sendMessage(ChatColor.GOLD + "Your selection is too small!");
        return false;
      } else if (area > LandClaim.plugin.getConfig().getInt("Claims.Regions.MaxSize")) {
        player.sendMessage(ChatColor.GOLD + "Your selection is too large!");
        return false;
      } else if ((length / width) > LandClaim.plugin.getConfig().getDouble("Claims.Regions.MaxLWRatio") || (width / length) > LandClaim.plugin.getConfig().getDouble("Claims.Regions.MaxLWRatio")) {
        player.sendMessage(ChatColor.GOLD + "Your selection doesn't meet proportion requirements!");
        return false;
      } else {
        return true;
      }
    }
  }
}