package net.minespire.landclaim.Listener;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minespire.landclaim.Claim.Claim;
import net.minespire.landclaim.Claim.ClaimManager;
import net.minespire.landclaim.Claim.Vote;
import net.minespire.landclaim.Claim.VoteRegion;
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
import org.bukkit.util.BlockVector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
            if (inventoryTitle.equals(LandClaim.plugin.getLocalizedString("GUI.Title_MainMenu"))) {
              if(itemName.equals(LandClaim.plugin.getLocalizedString("GUI.Action_Claims"))) guiManager.openAllClaimsGUI(player);
              else if(itemName.equals(LandClaim.plugin.getLocalizedString("GUI.Action_ClaimLimits"))) guiManager.openClaimLimitsGUI(player);
              else if(itemName.equals(LandClaim.plugin.getLocalizedString("GUI.Action_Wand"))) guiManager.handleWandClick(player);
              else if(itemName.equals(LandClaim.plugin.getLocalizedString("GUI.Action_PopularRegions"))) guiManager.openTopRegionsGUI(player);
              else if(itemName.equals(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) player.closeInventory();
            } else if (inventoryTitle.startsWith(LandClaim.plugin.getLocalizedString("GUI.Title_Claims"))) {
              if (player.hasPermission("landclaim.inspect.own")) {
                Material mat = itemStack.getType();
                if (mat.equals(Material.DIAMOND_BLOCK) || mat.equals(Material.DIAMOND_ORE) || mat.equals(Material.IRON_BLOCK) || mat.equals(Material.IRON_ORE)) {
                  guiManager.openClaimInspector(player, itemName, ChatColor.stripColor((String) itemMeta.getLore().get(0)).substring(7));
                }
              }
              int numRegionsToSkip;
              if(itemName.equals(LandClaim.plugin.getLocalizedString("GUI.Action_NextPage"))){
                numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(24));
                guiManager.openAllClaimsGUI(player, numRegionsToSkip * 28);
              }
              else if(itemName.equals(LandClaim.plugin.getLocalizedString("GUI.Action_PreviousPage"))){
                numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(24));
                guiManager.openAllClaimsGUI(player, (numRegionsToSkip - 2) * 28);
              }
              else if(itemName.equals(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))){
                guiManager.openMainGUI(player);
              }
              else if(itemName.equals(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))){
                player.closeInventory();
              }
            } else if (inventoryTitle.equals(LandClaim.plugin.getLocalizedString("GUI.Title_ClaimLimits"))) {
              if(itemName.equals(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))){
                guiManager.openMainGUI(player);
              }
              else if(itemName.equals(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))){
                player.closeInventory();
              }
            } else {
              String regionName;
              String worldName;
              if(inventoryTitle.equals(LandClaim.plugin.getLocalizedString("GUI.Title_ClaimInspector", true))){
                regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
                worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getLore().get(0)).substring(7);
                if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Remove"))) {
                  guiManager.promptForRemoval(player.getName(), regionName, worldName);
                } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Players"))) {
                  guiManager.openOwnersMembersEditor(player, regionName, worldName);
                } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_ResizeClaim"))){
                  Region selection = null;
                  BukkitPlayer bukkitPlayer = BukkitAdapter.adapt(player);
                  RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getWorld(worldName)));
                  ProtectedRegion region = rgManager.getRegion(regionName);

                  LocalSession session = WorldEdit.getInstance().getSessionManager().get(bukkitPlayer);
                  try {
                    selection = session.getSelection(bukkitPlayer.getWorld());
                  } catch (IncompleteRegionException e){
                    player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_NoRegionSelected"));
                    return;
                  }

                  int currentArea = (region.getMaximumPoint().getX() - region.getMinimumPoint().getX() + 1) * (region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ() + 1);
                  int newArea = (selection.getMaximumPoint().getX() - selection.getMinimumPoint().getX() + 1) * (selection.getMaximumPoint().getZ() - selection.getMinimumPoint().getZ() + 1);
                  double currentAreaPrice = LandClaim.plugin.getConfig().getDouble("Claims.Regions.PricePerBlock") * currentArea;

                  if(newArea < LandClaim.plugin.getConfig().getDouble("Claims.Regions.MinSize")){
                    player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_SelectionTooSmall"));
                    return;
                  }
                  else if (newArea > LandClaim.plugin.getConfig().getDouble("Claims.Regions.MaxSize")){
                    player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_SelectionTooBig"));
                    return;
                  }

                  Claim newClaim = new Claim(player, UUID.randomUUID().toString(), worldName);
                  newClaim.createClaim();
                  guiManager.openConfirmResizeGUI(player, regionName, worldName, newClaim, (newClaim.getClaimCost() - currentAreaPrice));
                } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_FlagEditor"))) {
                  guiManager.openFlagsGUI(player, regionName, worldName);
                } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Teleport"))) {
                  guiManager.openTeleportGUI(player, regionName, worldName);
                } else if (ChatColor.stripColor(itemName).equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                  guiManager.openAllClaimsGUI(player);
                } else if (ChatColor.stripColor(itemName).equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
                  player.closeInventory();
                }
              } else if(inventoryTitle.equals(LandClaim.plugin.getLocalizedString("GUI.Title_ClaimResize"))){
                if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                  guiManager.openMainGUI(player);
                } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
                  player.closeInventory();
                } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.PopupTitle_ClaimRegion", true)) || itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.PopupTitle_ClaimPlot", true))) {
                  // YOOOO
                  try{
                    regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
                    worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
                  } catch(NullPointerException e){
                    System.out.print(e.toString());
                    player.closeInventory();
                    return;
                  }
                  regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
                  worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
                  BukkitPlayer bukkitPlayer = BukkitAdapter.adapt(player);
                  RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getWorld(worldName)));
                  ProtectedRegion region = rgManager.getRegion(regionName);
                  LocalSession session = WorldEdit.getInstance().getSessionManager().get(bukkitPlayer);
                  player.closeInventory();
                  Region selection = null;
                  try {
                    selection = session.getSelection(bukkitPlayer.getWorld());
                  } catch (IncompleteRegionException e){
                    player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_NoRegionSelected"));
                    return;
                  }

                  int currentArea = (region.getMaximumPoint().getX() - region.getMinimumPoint().getX() + 1) * (region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ() + 1);
                  int newArea = (selection.getMaximumPoint().getX() - selection.getMinimumPoint().getX() + 1) * (selection.getMaximumPoint().getZ() - selection.getMinimumPoint().getZ() + 1);
                  double currentAreaPrice = LandClaim.plugin.getConfig().getDouble("Claims.Regions.PricePerBlock") * currentArea;
                  double newAreaPrice = LandClaim.plugin.getConfig().getDouble("Claims.Regions.PricePerBlock") * newArea;
                  double priceDiff = Math.abs(newAreaPrice - currentAreaPrice);

                  Map<com.sk89q.worldguard.protection.flags.Flag<?>, Object> flags = region.getFlags();
                  DefaultDomain owners = region.getOwners();
                  DefaultDomain members = region.getMembers();

                  Boolean isRegion = region.getFlag(LandClaim.LandClaimRegionFlag) != null && "region".equals(region.getFlag(LandClaim.LandClaimRegionFlag));
                  rgManager.removeRegion(regionName);

                  Claim newClaim = new Claim(player, regionName, worldName);

                  if (!newClaim.createClaim()) {
                    return;
                  }
                  if (newClaim.overlapsUnownedRegion()) {
                    player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_OverlappingRegions"));
                    return;
                  }

                  region = rgManager.getRegion(regionName);
                  if(newAreaPrice > currentAreaPrice){
                    if(LandClaim.econ.has(player, priceDiff)){
                      LandClaim.econ.withdrawPlayer(player, priceDiff);
                      player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Success_WithdrawnMoney").replace("{Amount}", Double.toString(priceDiff)).replace("{CurrencySymbol}", LandClaim.plugin.getLocalizedString("Economy.CurrencySymbol")));
                    }
                  }else if(newAreaPrice < currentAreaPrice && LandClaim.plugin.getConfig().getBoolean("Claims.RefundOnDelete") && isRegion){
                    double refundMultiplier = LandClaim.plugin.getConfig().getDouble("Claims.RefundMultiplier");
                    double refundAmount = priceDiff;
                    if (refundMultiplier >= 0) refundAmount = priceDiff * refundMultiplier;
                    LandClaim.econ.depositPlayer(player, refundAmount);
                    player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Success_DepositedMoney").replace("{Amount}", Double.toString(priceDiff)).replace("{CurrencySymbol}", LandClaim.plugin.getLocalizedString("Economy.CurrencySymbol")));
                  }


                  newClaim.setFlags(flags);
                  newClaim.setMembers(members);
                  newClaim.setOwners(owners);
                  newClaim.createClaim();
                  newClaim.saveClaim(true);

                }
              }
              else if(inventoryTitle.equals(LandClaim.plugin.getLocalizedString("GUI.Title_MembersEditor"))){
                regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
                worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getLore().get(0)).substring(7);
                if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_ManagePlayers"))) {
                  guiManager.openPlayersEditor(player, regionName, worldName);
                } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_AddToClaim", true))) {
                  guiManager.openAddPlayer(player, regionName, worldName);
                } else if (ChatColor.stripColor(itemName).equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                  guiManager.openClaimInspector(player, regionName, worldName);
                } else if (ChatColor.stripColor(itemName).equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
                  player.closeInventory();
                }
              }
              else if(inventoryTitle.equals(LandClaim.plugin.getLocalizedString("GUI.Title_ClaimRemoval"))) {
                regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
                worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
                if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Remove"))) {
                  RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getWorld(worldName)));
                  ProtectedRegion region = rgManager.getRegion(regionName);
                  double area = (region.getMaximumPoint().getX() - region.getMinimumPoint().getX() + 1) * (region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ() + 1);
                  double cost = LandClaim.plugin.getConfig().getDouble("Claims.Regions.PricePerBlock") * area;
                  if (LandClaim.plugin.getConfig().getBoolean("Claims.RefundOnDelete") && region.getFlag(LandClaim.LandClaimRegionFlag) != null && "region".equals(region.getFlag(LandClaim.LandClaimRegionFlag))) {
                    double refundMultiplier = LandClaim.plugin.getConfig().getDouble("Claims.RefundMultiplier");
                    double refundAmount;
                    if (refundMultiplier >= 0) refundAmount = cost * refundMultiplier;
                    else refundAmount = cost;
                    player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Success_UnclaimedRegion")
                            .replace("{RegionCost}", Double.toString(cost))
                            .replace("{CurrencySymbol}", LandClaim.plugin.getLocalizedString("Economy.CurrencySymbol"))
                            .replace("{CurrencyName}", LandClaim.plugin.getLocalizedString("Economy.Currency")));
                    LandClaim.econ.depositPlayer(player, refundAmount);
                    player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Success_DepositedMoney").replace("{Amount}", Double.toString(refundAmount)).replace("{CurrencySymbol}", LandClaim.plugin.getLocalizedString("Economy.CurrencySymbol")));
                  }
                  try {
                    Claim.removeRegion(player, regionName, worldName);
                  }catch(Exception e){}
                  guiManager.openAllClaimsGUI(player);
                } else if (ChatColor.stripColor(itemName).equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                  guiManager.openClaimInspector(player, regionName, worldName);
                } else if (ChatColor.stripColor(itemName).equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
                  player.closeInventory();
                }
              }
              else{
                String flagName;
                String firstLore;
                if (inventoryTitle.equals(LandClaim.plugin.getLocalizedString("GUI.Action_ManagePlayers"))) {
                  regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(47).getItemMeta().getDisplayName());
                  worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(47).getItemMeta().getLore().get(0)).substring(7);
                  if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                    guiManager.openOwnersMembersEditor(player, regionName, worldName);
                  } else if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
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
                            player.sendMessage(LandClaim.plugin.getLocalizedString("Message.Error_NotAnOwner"));
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
                  if (inventoryTitle.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Title_RemoveMember"))) {
                    regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
                    worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
                    uuid = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(1)).substring(6);
                    firstLore = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(0)).substring(15);
                    if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Confirm"))) {
                      world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
                      rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
                      if (Claim.removeMember(player, uuid, rgManager.getRegion(regionName))) {
                        player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Success_RemovedMember").replace("{Player}", firstLore).replace("{RegionName}", regionName));
                      }
                      player.closeInventory();
                    } else if (ChatColor.stripColor(itemName).startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                      guiManager.openPlayersEditor(player, regionName, worldName);
                    } else if (ChatColor.stripColor(itemName).startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
                      player.closeInventory();
                    }
                  } else if (inventoryTitle.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Title_RemoveOwner"))) {
                    regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
                    worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
                    uuid = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(1)).substring(6);
                    firstLore = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(0)).substring(14);
                    if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Confirm"))) {
                      world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
                      rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
                      if (Claim.removeOwner(player, uuid, rgManager.getRegion(regionName))) {
                        player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Success_RemovedOwner").replace("{Player}", firstLore).replace("{RegionName}", regionName));
                      }
                      player.closeInventory();
                    } else if (ChatColor.stripColor(itemName).startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                      guiManager.openPlayersEditor(player, regionName, worldName);
                    } else if (ChatColor.stripColor(itemName).startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
                      player.closeInventory();
                    }
                  } else if (inventoryTitle.equals(LandClaim.plugin.getLocalizedString("GUI.Title_AddMember"))) {
                    regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
                    worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
                    Prompt prompt;
                    if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_AddOwner"))) {
                      world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
                      rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
                      prompt = new Prompt(LandClaim.plugin.getLocalizedString("Messages.Prompt_AddOwner"), player, "ADDOWNER", rgManager.getRegion(regionName));
                      prompt.sendPrompt();
                      player.closeInventory();
                    } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_AddMember"))) {
                      world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
                      rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
                      prompt = new Prompt(LandClaim.plugin.getLocalizedString("Messages.Prompt_AddMember"), player, "ADDMEMBER", rgManager.getRegion(regionName));
                      prompt.sendPrompt();
                      player.closeInventory();
                    } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                      guiManager.openOwnersMembersEditor(player, regionName, worldName);
                    } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
                      player.closeInventory();
                    }
                  } else if (inventoryTitle.equals(LandClaim.plugin.getLocalizedString("GUI.Title_ClaimTeleport"))) {
                    regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());
                    worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getLore().get(0)).substring(7);
                    if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_TeleportTo"))) {
                      if (player.hasPermission("landclaim.teleport"))
                        Claim.teleportToClaim(player, regionName, worldName);
                      else
                        player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_MissingPermission"));
                    } else if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_SetTpPoint"))) {
                      Claim.setClaimTeleport(player, regionName, worldName);
                    } else if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_RemoveTpPoint"))) {
                      Claim.removeClaimTeleport(player, regionName, worldName);
                    } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                      guiManager.openClaimInspector(player, regionName, worldName);
                    } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
                      player.closeInventory();
                    }
                  } else if (inventoryTitle.equals(LandClaim.plugin.getLocalizedString("GUI.Title_ClaimFlags"))) {
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
                    if (ChatColor.stripColor(itemName).startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                      guiManager.openClaimInspector(player, regionName, worldName);
                    } else if (ChatColor.stripColor(itemName).startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
                      player.closeInventory();
                    }
                  } else {
                    boolean nonOwnerInspector;
                    boolean nonOwnerEditor;
                    if (inventoryTitle.equals(LandClaim.plugin.getLocalizedString("GUI.Title_StateFlagEditor"))) {
                      regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
                      worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getLore().get(0)).substring(7);
                      if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                        guiManager.openFlagsGUI(player, regionName, worldName);
                      } else if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
                        player.closeInventory();
                      } else if (!itemStack.getType().equals(Material.DARK_OAK_SIGN) && !itemStack.getType().equals(Material.BIRCH_SIGN)) {
                        nonOwnerInspector = !Claim.getRegionOwners(regionName, worldName).contains(player.getUniqueId());
                        nonOwnerEditor = player.hasPermission("landclaim.edit.others");
                        if (nonOwnerInspector && !nonOwnerEditor) {
                          player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_CannotEdit"));
                        } else {
                          Flag flag = GUIManager.editableClaimFlags.get(clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName());
                          ProtectedRegion region = Claim.getRegion(player, regionName, worldName);
                          if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_DeleteFlag"))) {
                            region.setFlag(flag, null);
                          } else if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_Allow"))) {
                            region.setFlag(flag, StateFlag.State.ALLOW);
                          } else if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_Deny"))) {
                            region.setFlag(flag, StateFlag.State.DENY);
                          } else if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_SetEveryone"))) {
                            region.setFlag(flag.getRegionGroupFlag(), RegionGroup.ALL);
                          } else if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_SetMembers"))) {
                            region.setFlag(flag.getRegionGroupFlag(), RegionGroup.MEMBERS);
                          } else if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_SetOwners"))) {
                            region.setFlag(flag.getRegionGroupFlag(), RegionGroup.OWNERS);
                          } else if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_SetNotMembers"))) {
                            region.setFlag(flag.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
                          } else if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_SetNotOwners"))) {
                            region.setFlag(flag.getRegionGroupFlag(), RegionGroup.NON_OWNERS);
                          }
                          guiManager.openStateFlagEditor(player, regionName, clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName(), worldName);
                        }
                      }
                    } else if (inventoryTitle.equals(LandClaim.plugin.getLocalizedString("GUI.Title_StringFlagEditor"))) {
                      regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
                      worldName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getLore().get(0)).substring(7);
                      if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                        guiManager.openFlagsGUI(player, regionName, worldName);
                      } else if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
                        player.closeInventory();
                      } else {
                        nonOwnerInspector = !Claim.getRegionOwners(regionName, worldName).contains(player.getUniqueId());
                        nonOwnerEditor = player.hasPermission("landclaim.edit.others");
                        flagName = clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName();
                        Flag flag = GUIManager.editableClaimFlags.get(flagName);
                        ProtectedRegion region = Claim.getRegion(player, regionName, worldName);
                        if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_NewValue"))) {
                          if (nonOwnerInspector && !nonOwnerEditor) {
                            player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_CannotEdit"));
                            return;
                          }
                          player.closeInventory();
                          (new Prompt(LandClaim.plugin.getLocalizedString("Messages.Prompt_NewValue").replace("{FlagName}", flagName), player, flagName, region)).sendPrompt();
                        } else if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Action_DeleteFlag"))) {
                          if (nonOwnerInspector && !nonOwnerEditor) {
                            player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Error_CannotEdit"));
                            return;
                          }
                          region.setFlag(flag, null);
                          player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Success_FlagRemoved").replace("{FlagName}", flagName).replace("{RegionName}", regionName));
                        }
                      }
                    } else if (inventoryTitle.equals(LandClaim.plugin.getLocalizedString("GUI.Title_TopRegions"))) {
                      if (itemName.endsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Year"))) {
                        guiManager.openTopYearRegions(player);
                      } else if (itemName.endsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Month"))) {
                        guiManager.openTopMonthRegions(player);
                      } else if (itemName.endsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Day"))) {
                        guiManager.openTopDayRegions(player);
                      } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                        guiManager.openMainGUI(player);
                      } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
                        player.closeInventory();
                      }
                    } else if (!inventoryTitle.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Title_YearTopRegions")) && !inventoryTitle.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Title_MonthTopRegions")) && !inventoryTitle.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Title_DayTopRegions"))) {
                      if (inventoryTitle.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Title_ClaimRegion", true)) || inventoryTitle.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.Title_ClaimPlot", true))) {
                        if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                          guiManager.openMainGUI(player);
                        } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
                          player.closeInventory();
                        } else if (itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.PopupTitle_ClaimRegion", true)) || itemName.equalsIgnoreCase(LandClaim.plugin.getLocalizedString("GUI.PopupTitle_ClaimPlot", true))) {
                          Claim claim = LandClaim.claimMap.get(player.getUniqueId().toString());
                          claim.saveClaim();
                          if (!claim.isPlot()) {
                            player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Success_ClaimedRegion")
                                    .replace("{RegionCost}", Double.toString(claim.getClaimCost()))
                                    .replace("{CurrencySymbol}", LandClaim.plugin.getLocalizedString("Economy.CurrencySymbol"))
                                    .replace("{CurrencyName}", LandClaim.plugin.getLocalizedString("Economy.Currency")));
                          } else {
                            player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Success_ClaimedPlot").replace("{RegionCost}", Double.toString(claim.getClaimCost())));
                          }
                          LandClaim.econ.withdrawPlayer(player, claim.getClaimCost());
                          player.sendMessage(LandClaim.plugin.getLocalizedString("Messages.Success_WithdrawnMoney").replace("{Amount}", Double.toString(claim.getClaimCost())).replace("{CurrencySymbol}", LandClaim.plugin.getLocalizedString("Economy.CurrencySymbol")));
                          player.closeInventory();
                          return;
                        }
                      }
                    } else {
                      if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Back"))) {
                        guiManager.openTopRegionsGUI(player);
                      } else if (itemName.startsWith(LandClaim.plugin.getLocalizedString("GUI.Action_Close"))) {
                        player.closeInventory();
                      } else if (itemStack.getType().equals(Material.EMERALD)) {
                        regionName = ChatColor.stripColor(itemMeta.getLore().get(0)).split(",")[0];
                        worldName = ChatColor.stripColor(itemMeta.getLore().get(0)).split(",")[1];
                        Claim.teleportToClaim(player, regionName, worldName);
                      }
                    }
                  }
                }
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
