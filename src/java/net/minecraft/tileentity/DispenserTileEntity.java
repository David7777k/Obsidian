package net.minecraft.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.DispenserContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.obsidian.common.impl.fastrandom.FastRandom;

import java.util.Random;

public class DispenserTileEntity extends LockableLootTileEntity {
    private static final Random RNG = new FastRandom();
    private NonNullList<ItemStack> stacks = NonNullList.withSize(9, ItemStack.EMPTY);

    protected DispenserTileEntity(TileEntityType<?> p_i48286_1_) {
        super(p_i48286_1_);
    }

    public DispenserTileEntity() {
        this(TileEntityType.DISPENSER);
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory() {
        return 9;
    }

    public int getDispenseSlot() {
        this.fillWithLoot(null);
        int i = -1;
        int j = 1;

        for (int k = 0; k < this.stacks.size(); ++k) {
            if (!this.stacks.get(k).isEmpty() && RNG.nextInt(j++) == 0) {
                i = k;
            }
        }

        return i;
    }

    /**
     * Add the given ItemStack to this Dispenser. Return the Slot the Item was placed in or -1 if no free slot is
     * available.
     */
    public int addItemStack(ItemStack stack) {
        for (int i = 0; i < this.stacks.size(); ++i) {
            if (this.stacks.get(i).isEmpty()) {
                this.setInventorySlotContents(i, stack);
                return i;
            }
        }

        return -1;
    }

    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container.dispenser");
    }

    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);

        if (!this.checkLootAndRead(nbt)) {
            ItemStackHelper.loadAllItems(nbt, this.stacks);
        }
    }

    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.stacks);
        }

        return compound;
    }

    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    protected void setItems(NonNullList<ItemStack> itemsIn) {
        this.stacks = itemsIn;
    }

    protected Container createMenu(int id, PlayerInventory player) {
        return new DispenserContainer(id, player, this);
    }
}
