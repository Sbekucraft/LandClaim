package net.minespire.landclaim.GUI;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

class GUIItem {
  private GUIItem() {}

  public static ItemStack make(Material material, String title, List<String> lore) {
    ItemStack itemStack = new ItemStack(material);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    itemMeta.setDisplayName(title);
    itemMeta.setLore(lore);
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }
}
