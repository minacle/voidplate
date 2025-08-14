package moe.minacle.minecraft.plugins.voidplate;

import org.jetbrains.annotations.NotNull;

import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.datacomponent.item.PotionContents;
import net.kyori.adventure.key.Key;

public final class Plugin extends JavaPlugin implements Listener {

    private static final int BSTATS_PLUGIN_ID = 26857;

    @EventHandler
    public void onPrepareSmithing(final @NotNull PrepareSmithingEvent event) {
        final SmithingInventory smithingInventory = event.getInventory();
        final ItemStack inputTemplate = smithingInventory.getInputTemplate();
        final ItemStack inputEquipment;
        final ItemStack inputMineral;
        final Equippable inputEquipmentEquippable;
        final ItemRarity inputEquipmentRarity;
        final ItemStack result;
        Equippable.Builder resultEquippableBuilder;
        if (inputTemplate == null)
            return;
        if (inputTemplate.getType() == Material.POTION) {
            final PotionContents potionContents = inputTemplate.getData(DataComponentTypes.POTION_CONTENTS);
            if (potionContents == null || potionContents.potion() != PotionType.LONG_INVISIBILITY)
                return;
        }
        inputMineral = smithingInventory.getInputMineral();
        if (inputMineral == null || inputMineral.getType() != Material.ECHO_SHARD)
            return;
        inputEquipment = smithingInventory.getInputEquipment();
        if (inputEquipment == null)
            return;
        inputEquipmentEquippable = inputEquipment.getData(DataComponentTypes.EQUIPPABLE);
        if (inputEquipmentEquippable != null) {
            final EquipmentSlot slot = inputEquipmentEquippable.slot();
            if (slot != EquipmentSlot.HEAD && slot != EquipmentSlot.CHEST && slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET)
                return;
        }
        else
            return;
        inputEquipmentRarity = inputEquipment.getData(DataComponentTypes.RARITY);
        if (inputEquipmentRarity != null && inputEquipmentRarity == ItemRarity.EPIC)
            return;
        result = inputEquipment.clone();
        resultEquippableBuilder =
            Equippable
            .equippable(inputEquipmentEquippable.slot())
            .equipSound(inputEquipmentEquippable.equipSound())
            .assetId(Key.key("missingno"))
            .cameraOverlay(inputEquipmentEquippable.cameraOverlay())
            .allowedEntities(inputEquipmentEquippable.allowedEntities())
            .dispensable(inputEquipmentEquippable.dispensable())
            .swappable(inputEquipmentEquippable.swappable())
            .damageOnHurt(inputEquipmentEquippable.damageOnHurt());
        try {
            // 1.21.5
            resultEquippableBuilder = resultEquippableBuilder.equipOnInteract(inputEquipmentEquippable.equipOnInteract());
            // 1.21.6
            resultEquippableBuilder = resultEquippableBuilder.canBeSheared(inputEquipmentEquippable.canBeSheared());
            resultEquippableBuilder = resultEquippableBuilder.shearSound(inputEquipmentEquippable.shearSound());
        }
        catch (NoSuchMethodError e) {
        }
        result.setData(DataComponentTypes.EQUIPPABLE, resultEquippableBuilder.build());
        result.setData(DataComponentTypes.RARITY, ItemRarity.EPIC);
        event.setResult(result);
    }

    @EventHandler
    public void onInventoryClick(final @NotNull InventoryClickEvent event) {
        final Inventory clickedInventory = event.getClickedInventory();
        final ItemStack cursor;
        final Material cursorType;
        final int slot;
        if (clickedInventory == null)
            return;
        if (clickedInventory.getType() != InventoryType.SMITHING)
            return;
        cursor = event.getCursor();
        cursorType = cursor.getType();
        slot = event.getSlot();
        if (cursorType == Material.POTION && slot == 0) {
            final PotionContents potionContents = cursor.getData(DataComponentTypes.POTION_CONTENTS);
            if (potionContents == null)
                return;
            if (potionContents.potion() == PotionType.LONG_INVISIBILITY) {
                event.getWhoClicked().setItemOnCursor(event.getCurrentItem());
                event.setCurrentItem(cursor);
                event.setResult(Result.DENY);
                return;
            }
        }
        else if (cursorType == Material.ECHO_SHARD && slot == 2) {
            event.getWhoClicked().setItemOnCursor(event.getCurrentItem());
            event.setCurrentItem(cursor);
            event.setResult(Result.DENY);
            return;
        }
    }

    // MARK: JavaPlugin

    @Override
    public void onEnable() {
        super.onEnable();
        new Metrics(this, BSTATS_PLUGIN_ID);
        getServer().getPluginManager().registerEvents(this, this);
    }
}
