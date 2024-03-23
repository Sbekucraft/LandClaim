package net.minespire.landclaim.Listener;

import net.minespire.landclaim.Claim.Claim;
import net.minespire.landclaim.LandClaim;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DeedListener implements Listener {

  private String regionName;
  private String worldName;
  private ItemStack itemInHand;
  private Player player;
  private boolean newRegion = false;
  private boolean blankDeed = false;
  public static NamespacedKey regionNameKey = new NamespacedKey(LandClaim.plugin, "regionName");
  public static NamespacedKey worldNameKey = new NamespacedKey(LandClaim.plugin, "worldName");
  public static NamespacedKey blankDeedKey = new NamespacedKey(LandClaim.plugin, "blankKey");

  public DeedListener() {}

  public DeedListener(Player player, String regionName, String worldName, boolean newRegion) {
    itemInHand = new ItemStack(Material.PAPER);
    this.player = player;
    this.worldName = worldName;
    this.regionName = regionName;
    this.newRegion = newRegion;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onDeedClick(PlayerInteractEvent click) {
		if (!click.getAction().equals(Action.RIGHT_CLICK_AIR) && !click.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		if (!click.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.PAPER)) {
			return;
		}

    itemInHand = click.getPlayer().getInventory().getItemInMainHand();
    player = click.getPlayer();

    if (isDeed()) {
      activateDeedNewOwner();
      return;
    }
    blankDeed = isBlankDeed();
		if (blankDeed) {
			createDeed();
		}
  }

  public boolean isDeed() {
    ItemMeta itemToCheckMeta = itemInHand.getItemMeta();
		if (itemToCheckMeta.getCustomTagContainer().hasCustomTag(regionNameKey, ItemTagType.STRING)) {
			regionName = itemToCheckMeta.getCustomTagContainer().getCustomTag(regionNameKey, ItemTagType.STRING);
			worldName = itemToCheckMeta.getCustomTagContainer().getCustomTag(worldNameKey, ItemTagType.STRING);
			return true;
		} else {
			return false;
		}
  }

  public boolean isBlankDeed() {
    ItemMeta itemToCheckMeta = itemInHand.getItemMeta();
		if (itemToCheckMeta.getCustomTagContainer().hasCustomTag(regionNameKey, ItemTagType.STRING)) {
			regionName = itemToCheckMeta.getCustomTagContainer().getCustomTag(blankDeedKey, ItemTagType.STRING);
			return true;
		} else {
			return false;
		}
  }

  public void activateDeedNewOwner() {
    Claim claim = new Claim(player, regionName, worldName);
    Set<UUID> owners = claim.getRegionOwners(regionName, worldName);
    for (UUID owner : owners) {
      if (player.getUniqueId().equals(owner)) {
        player.sendMessage("You are already an owner of this region!");
        return;
      }
    }
    //GUI deedActivationGUI = new GUI(player, regionName);

    //deedActivationGUI.makeActivateDeedGUI();
    //deedActivationGUI.openGUI();
  }

  public ItemStack createDeed() {
    ItemMeta newDeedItemMeta = itemInHand.getItemMeta();
		if (blankDeed) {
			newDeedItemMeta.getCustomTagContainer().removeCustomTag(blankDeedKey);
		}
    newDeedItemMeta.getCustomTagContainer().setCustomTag(regionNameKey, ItemTagType.STRING, regionName);
    newDeedItemMeta.getCustomTagContainer().setCustomTag(worldNameKey, ItemTagType.STRING, worldName);
    newDeedItemMeta.setDisplayName("Property Deed For Region: " + regionName);
    List<String> itemLore = new ArrayList<>();
    itemLore.add("Owner: " + player.getName());
    itemLore.add("World: " + worldName);
    newDeedItemMeta.setLore(itemLore);
    itemInHand.setItemMeta(newDeedItemMeta);
    return itemInHand;
  }

  public static ItemStack getBlankDeed() {
    ItemStack newBlankDeed = new ItemStack(Material.PAPER);
    ItemMeta newBlankDeedItemMeta = newBlankDeed.getItemMeta();
    newBlankDeedItemMeta.setDisplayName("Blank Property Deed");
    newBlankDeedItemMeta.getCustomTagContainer().setCustomTag(blankDeedKey, ItemTagType.STRING, "blank");
    newBlankDeed.setItemMeta(newBlankDeedItemMeta);
    return newBlankDeed;
  }
}
