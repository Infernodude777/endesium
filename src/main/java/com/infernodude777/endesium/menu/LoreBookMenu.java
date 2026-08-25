package com.infernodude777.endesium.menu;

import com.infernodude777.endesium.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The lore book's container: a single item socket. Place any item in the
 * socket and the screen reveals everything the archives know about it. The
 * socket is transient - whatever is placed inside is returned to the player
 * the moment the book closes.
 */
public class LoreBookMenu extends AbstractContainerMenu {
	private final Container book;

	public LoreBookMenu(int id, Inventory playerInventory) {
		this(id, playerInventory, new SimpleContainer(1));
	}

	public LoreBookMenu(int id, Inventory playerInventory, Container book) {
		super(ModMenus.LORE_BOOK, id);
		this.book = book;
		book.startOpen(playerInventory.player);
		this.addSlot(new Slot(book, 0, 30, 34));
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 100 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 158));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = this.slots.get(index);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		if (index == 0) {
			if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else if (!this.moveItemStackTo(stack, 0, 1, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return original;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		ItemStack placed = book.getItem(0);
		if (!placed.isEmpty()) {
			book.setItem(0, ItemStack.EMPTY);
			if (!player.getInventory().add(placed)) {
				player.drop(placed, false);
			}
		}
	}
}
