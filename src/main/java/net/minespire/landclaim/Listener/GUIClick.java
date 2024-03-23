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
    if (itemStack != null) {
      itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
        itemName = ChatColor.stripColor(itemMeta.getDisplayName());
        if (clickEvent.getWhoClicked() instanceof Player) {
          Player player = ((Player) clickEvent.getWhoClicked()).getPlayer();
          if (isLandClaimGui(player)) {
            clickEvent.setCancelled(true);
            GUIManager guiManager = GUIManager.getInst();
            if (inventoryTitle.equals("LandClaim Main Menu")) {
              switch (itemName) {
                case "Claims":
                  guiManager.openAllClaimsGUI(player);
                  break;
                case "Claim Limits":
                  guiManager.openClaimLimitsGUI(player);
                  break;
                case "Wand":
                  guiManager.handleWandClick(player);
                  break;
                case "Popular Regions":
                  guiManager.openTopRegionsGUI(player);
                  break;
                case "Close":
                  player.closeInventory();
                  break;
              }
            } else if (inventoryTitle.startsWith("LandClaim Claims")) {
              if (player.hasPermission("landclaim.inspect.own")) {
                Material mat = itemStack.getType();
                if (mat.equals(Material.DIAMOND_BLOCK) || mat.equals(Material.DIAMOND_ORE) || mat.equals(Material.IRON_BLOCK) || mat.equals(Material.IRON_ORE)) {
                  guiManager.openClaimInspector(player, itemName, ChatColor.stripColor((String) itemMeta.getLore().get(0)).substring(7));
                }
              }
              int numRegionsToSkip;
              switch (itemName) {
                case "Next Page":
                  numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(24));
                  guiManager.openAllClaimsGUI(player, numRegionsToSkip * 28);
                  break;
                case "Previous Page":
                  numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(24));
                  guiManager.openAllClaimsGUI(player, (numRegionsToSkip - 2) * 28);
                  break;
                case "Back":
                  guiManager.openMainGUI(player);
                  break;
                case "Close":
                  player.closeInventory();
                  break;
              }
            } else if (inventoryTitle.equals("LandClaim Claim Limits")) {
              switch (itemName) {
                case "Back":
                  guiManager.openMainGUI(player);
                  break;
                case "Close":
                  player.closeInventory();
                  break;
              }
            } else {
              String regionName;
              String worldName;
              switch (inventoryTitle) {
                case "LandClaim Inspector":
                  regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
                  worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getLore().get(0)).substring(7);
                  if (itemName.startsWith("Remove")) {
                    guiManager.promptForRemoval(player.getName(), regionName, worldName);
                  } else if (itemName.startsWith("Players")) {
                    guiManager.openOwnersMembersEditor(player, regionName, worldName);
                  } else if (itemName.startsWith("Flag Editor")) {
                    guiManager.openFlagsGUI(player, regionName, worldName);
                  } else if (itemName.startsWith("Teleport")) {
                    guiManager.openTeleportGUI(player, regionName, worldName);
                  } else if (ChatColor.stripColor(itemName).equalsIgnoreCase("Back")) {
                    guiManager.openAllClaimsGUI(player);
                  } else if (ChatColor.stripColor(itemName).equalsIgnoreCase("Close")) {
                    player.closeInventory();
                  }
                  break;
                case "Owners/Members Editor":
                  regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
                  worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getLore().get(0)).substring(7);
                  if (itemName.startsWith("View/Remove Players")) {
                    guiManager.openPlayersEditor(player, regionName, worldName);
                  } else if (itemName.startsWith("Add Player to Claim")) {
                    guiManager.openAddPlayer(player.getName(), regionName, worldName);
                  } else if (ChatColor.stripColor(itemName).equalsIgnoreCase("Back")) {
                    guiManager.openClaimInspector(player, regionName, worldName);
                  } else if (ChatColor.stripColor(itemName).equalsIgnoreCase("Close")) {
                    player.closeInventory();
                  }
                  break;
                case "LandClaim Claim Removal":
                  regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
                  worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
                  if (itemName.startsWith("Remove")) {
                    double cost = LandClaim.claimMap.get(player.getUniqueId().toString()).getClaimCost();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6You successfully unclaimed a region of land for &b$" + cost + "&6!"));
                    LandClaim.econ.depositPlayer(player, cost);
                    Claim.removeRegion(player, regionName, worldName);
                    guiManager.openAllClaimsGUI(player);
                  } else if (ChatColor.stripColor(itemName).equalsIgnoreCase("Back")) {
                    guiManager.openClaimInspector(player, regionName, worldName);
                  } else if (ChatColor.stripColor(itemName).equalsIgnoreCase("Close")) {
                    player.closeInventory();
                  }
                  break;
                default:
                  String flagName;
                  String firstLore;
                  if (inventoryTitle.equals("View/Remove Players")) {
                    regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(47).getItemMeta().getDisplayName());
                    worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(47).getItemMeta().getLore().get(0)).substring(7);
                    if (itemName.equalsIgnoreCase("Back")) {
                      guiManager.openOwnersMembersEditor(player, regionName, worldName);
                    } else if (itemName.equalsIgnoreCase("Close")) {
                      player.closeInventory();
                    } else {
                      if (player.hasPermission("landclaim.removeplayer")) {
                        ItemMeta clickedItemMeta = clickEvent.getCurrentItem().getItemMeta();
                        firstLore = "";
                        if (clickedItemMeta != null) {
                          if (clickedItemMeta.getLore() != null) {
                            firstLore = ChatColor.stripColor((String) clickedItemMeta.getLore().get(0));
                          }
                          if (firstLore.startsWith("UUID")) {
                            flagName = Claim.playerIsOwnerOrMember(player, regionName, worldName);
                            if ((flagName == null || !flagName.equalsIgnoreCase("Owner")) && !player.hasPermission("landclaim.edit.others")) {
                              player.sendMessage(ChatColor.GOLD + "Only claim owners can remove players from claims.");
                            } else {
                              if (itemStack.getType().equals(Material.WITHER_SKELETON_SKULL)) {
                                guiManager.openOwnerRemover(player, firstLore.substring(5), regionName, worldName);
                              }
                              if (itemStack.getType().equals(Material.SKELETON_SKULL)) {
                                guiManager.openMemberRemover(player, firstLore.substring(5), regionName, worldName);
                              }
                            }
                          }
                        }
                      }
                    }
                  } else {
                    String uuid;
                    World world;
                    RegionManager rgManager;
                    if (inventoryTitle.equalsIgnoreCase("Remove Member")) {
                      regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
                      worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
                      uuid = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(1)).substring(6);
                      firstLore = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(0)).substring(15);
                      if (itemName.startsWith("Are you sure")) {
                        world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
                        rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
                        if (Claim.removeMember(player, uuid, rgManager.getRegion(regionName))) {
                          player.sendMessage(ChatColor.GOLD + "You removed member " + ChatColor.AQUA + firstLore + ChatColor.GOLD + " from " + ChatColor.AQUA + regionName + ChatColor.GOLD + ".");
                        }
                        player.closeInventory();
                      } else if (ChatColor.stripColor(itemName).startsWith("Back")) {
                        guiManager.openPlayersEditor(player, regionName, worldName);
                      } else if (ChatColor.stripColor(itemName).startsWith("Close")) {
                        player.closeInventory();
                      }
                    } else if (inventoryTitle.equalsIgnoreCase("Remove Owner")) {
                      regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
                      worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
                      uuid = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(1)).substring(6);
                      firstLore = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(0)).substring(14);
                      if (itemName.startsWith("Are you sure")) {
                        world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
                        rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
                        if (Claim.removeOwner(player, uuid, rgManager.getRegion(regionName))) {
                          player.sendMessage(ChatColor.GOLD + "You removed owner " + ChatColor.AQUA + firstLore + ChatColor.GOLD + " from " + ChatColor.AQUA + regionName + ChatColor.GOLD + ".");
                        }
                        player.closeInventory();
                      } else if (ChatColor.stripColor(itemName).startsWith("Back")) {
                        guiManager.openPlayersEditor(player, regionName, worldName);
                      } else if (ChatColor.stripColor(itemName).startsWith("Close")) {
                        player.closeInventory();
                      }
                    } else if (inventoryTitle.equals("Add Player to Claim")) {
                      regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
                      worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
                      Prompt prompt;
                      if (itemName.startsWith("Add Owner to")) {
                        world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
                        rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
                        prompt = new Prompt(ChatColor.GOLD + "Who would you like to add as an owner? " + ChatColor.RED + "/lc cancel" + ChatColor.GOLD + " to cancel", player, "ADDOWNER", rgManager.getRegion(regionName));
                        prompt.sendPrompt();
                        player.closeInventory();
                      } else if (itemName.startsWith("Add Member to")) {
                        world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
                        rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
                        prompt = new Prompt(ChatColor.GOLD + "Who would you like to add as a member? " + ChatColor.RED + "/lc cancel" + ChatColor.GOLD + " to cancel", player, "ADDMEMBER", rgManager.getRegion(regionName));
                        prompt.sendPrompt();
                        player.closeInventory();
                      } else if (itemName.startsWith("Back")) {
                        guiManager.openOwnersMembersEditor(player, regionName, worldName);
                      } else if (itemName.startsWith("Close")) {
                        player.closeInventory();
                      }
                    } else if (inventoryTitle.equals("LandClaim Teleport")) {
                      regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
                      worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
                      if (itemName.startsWith("Teleport to ")) {
                        Claim.teleportToClaim(player, regionName, worldName);
                      } else if (itemName.equalsIgnoreCase("Set Teleport Point")) {
                        Claim.setClaimTeleport(player, regionName, worldName);
                      } else if (itemName.equalsIgnoreCase("Remove Teleport Point")) {
                        Claim.removeClaimTeleport(player, regionName, worldName);
                      } else if (itemName.startsWith("Back")) {
                        guiManager.openClaimInspector(player, regionName, worldName);
                      } else if (itemName.startsWith("Close")) {
                        player.closeInventory();
                      }
                    } else if (inventoryTitle.equals("LandClaim Flags")) {
                      regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(38).getItemMeta().getDisplayName());
                      worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(38).getItemMeta().getLore().get(0)).substring(7);
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
                      } else if (ChatColor.stripColor(itemName).startsWith("Close")) {
                        player.closeInventory();
                      }
                    } else {
                      boolean nonOwnerInspector;
                      boolean nonOwnerEditor;
                      if (inventoryTitle.equals("LandClaim State Flag Editor")) {
                        regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
                        worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getLore().get(0)).substring(7);
                        if (itemName.equalsIgnoreCase("Back")) {
                          guiManager.openFlagsGUI(player, regionName, worldName);
                        } else if (itemName.equalsIgnoreCase("Close")) {
                          player.closeInventory();
                        } else if (!itemStack.getType().equals(Material.DARK_OAK_SIGN) && !itemStack.getType().equals(Material.BIRCH_SIGN)) {
                          nonOwnerInspector = !Claim.getRegionOwners(regionName, worldName).contains(player.getUniqueId());
                          nonOwnerEditor = player.hasPermission("landclaim.edit.others");
                          if (nonOwnerInspector && !nonOwnerEditor) {
                            player.sendMessage(ChatColor.GOLD + "You cannot edit this claim");
                          } else {
                            Flag flag = GUIManager.editableClaimFlags.get(clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName());
                            ProtectedRegion region = Claim.getRegion(player, regionName, worldName);
                            if (itemName.equalsIgnoreCase("Delete Flag")) {
                              region.setFlag(flag, null);
                            } else if (itemName.equalsIgnoreCase("Allow")) {
                              region.setFlag(flag, StateFlag.State.ALLOW);
                            } else if (itemName.equalsIgnoreCase("Deny")) {
                              region.setFlag(flag, StateFlag.State.DENY);
                            } else if (itemName.equalsIgnoreCase("Set for everyone")) {
                              region.setFlag(flag.getRegionGroupFlag(), RegionGroup.ALL);
                            } else if (itemName.equalsIgnoreCase("Set for members")) {
                              region.setFlag(flag.getRegionGroupFlag(), RegionGroup.MEMBERS);
                            } else if (itemName.equalsIgnoreCase("Set for owners")) {
                              region.setFlag(flag.getRegionGroupFlag(), RegionGroup.OWNERS);
                            } else if (itemName.equalsIgnoreCase("Set for non-members")) {
                              region.setFlag(flag.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
                            } else if (itemName.equalsIgnoreCase("Set for non-owners")) {
                              region.setFlag(flag.getRegionGroupFlag(), RegionGroup.NON_OWNERS);
                            }
                            guiManager.openStateFlagEditor(player, regionName, clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName(), worldName);
                          }
                        }
                      } else if (inventoryTitle.equals("LandClaim String Flag Editor")) {
                        regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
                        worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getLore().get(0)).substring(7);
                        if (itemName.equalsIgnoreCase("Back")) {
                          guiManager.openFlagsGUI(player, regionName, worldName);
                        } else if (itemName.equalsIgnoreCase("Close")) {
                          player.closeInventory();
                        } else {
                          nonOwnerInspector = !Claim.getRegionOwners(regionName, worldName).contains(player.getUniqueId());
                          nonOwnerEditor = player.hasPermission("landclaim.edit.others");
                          flagName = clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName();
                          Flag flag = GUIManager.editableClaimFlags.get(flagName);
                          ProtectedRegion region = Claim.getRegion(player, regionName, worldName);
                          if (itemName.equalsIgnoreCase("Enter New Value")) {
                            if (nonOwnerInspector && !nonOwnerEditor) {
                              player.sendMessage(ChatColor.GOLD + "You cannot edit this claim");
                              return;
                            }
                            player.closeInventory();
                            (new Prompt(ChatColor.GOLD + "Enter a new value for the '" + flagName + "' flag", player, flagName, region)).sendPrompt();
                          } else if (itemName.equalsIgnoreCase("Delete Flag")) {
                            if (nonOwnerInspector && !nonOwnerEditor) {
                              player.sendMessage(ChatColor.GOLD + "You cannot edit this claim");
                              return;
                            }
                            region.setFlag(flag, null);
                            player.sendMessage(ChatColor.GOLD + "Removed flag '" + flagName + "' from " + ChatColor.AQUA + regionName);
                          }
                        }
                      } else if (inventoryTitle.equals("LandClaim Top Regions")) {
                        if (itemName.endsWith("Year")) {
                          guiManager.openTopYearRegions(player);
                        } else if (itemName.endsWith("Month")) {
                          guiManager.openTopMonthRegions(player);
                        } else if (itemName.endsWith("Day")) {
                          guiManager.openTopDayRegions(player);
                        } else if (itemName.startsWith("Back")) {
                          guiManager.openMainGUI(player);
                        } else if (itemName.startsWith("Close")) {
                          player.closeInventory();
                        }
                      } else if (!inventoryTitle.equalsIgnoreCase("LandClaim Top Regions - Year") && !inventoryTitle.equalsIgnoreCase("LandClaim Top Regions - Month") && !inventoryTitle.equalsIgnoreCase("LandClaim Top Regions - Day")) {
                        if (inventoryTitle.equalsIgnoreCase("LandClaim Claim Region") || inventoryTitle.equalsIgnoreCase("LandClaim Claim Plot")) {
                          if (itemName.startsWith("Back")) {
                            guiManager.openMainGUI(player);
                          } else if (itemName.startsWith("Close")) {
                            player.closeInventory();
                          } else if (itemName.equalsIgnoreCase("Are You Sure You Want To Claim This Region?") || itemName.equalsIgnoreCase("Are You Sure You Want To Claim This Plot?")) {
                            Claim claim = LandClaim.claimMap.get(player.getUniqueId().toString());
                            claim.saveClaim();
                            if (!claim.isPlot()) {
                              player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6You successfully claimed a region of land for &b$" + claim.getClaimCost() + "&6!"));
                            } else {
                              player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6You successfully claimed a plot of land for &b$" + claim.getClaimCost() + "&6!"));
                            }
                            LandClaim.econ.withdrawPlayer(player, claim.getClaimCost());
                            player.closeInventory();
                            return;
                          }
                        }
                      } else {
                        if (itemName.startsWith("Back")) {
                          guiManager.openTopRegionsGUI(player);
                        } else if (itemName.startsWith("Close")) {
                          player.closeInventory();
                        } else if (itemStack.getType().equals(Material.EMERALD)) {
                          regionName = ChatColor.stripColor(itemMeta.getLore().get(0)).split(",")[0];
                          worldName = ChatColor.stripColor(itemMeta.getLore().get(0)).split(",")[1];
                          Claim.teleportToClaim(player, regionName, worldName);
                        }
                      }
                    }
                  }
                  break;
              }
            }
          }
        }
      }
    }
  }

  public static boolean isLandClaimGui(Player player) {
    return player.getOpenInventory().equals(playerLCInventory.get(player.getUniqueId().toString()));
  }
}
