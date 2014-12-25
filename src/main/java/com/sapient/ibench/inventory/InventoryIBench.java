package com.sapient.ibench.inventory;

import com.sapient.ibench.items.ItemIBench;
import com.sapient.ibench.reference.Names;
import com.sapient.ibench.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.UUID;

public class InventoryIBench extends InventoryCrafting {


    public ItemStack parent;
    public EntityPlayer player;

    protected ItemStack[] inventory;

    private Container eventHandler;

    protected ItemStack[] recipes;

    // This class gets instantiated when we right click a clipboard. Called from the GuiHandler
    public InventoryIBench(EntityPlayer player, ItemStack itemStack) {
        super(null, 3, 3);
        this.parent = itemStack;
        this.player = player;

        this.inventory = new ItemStack[this.getSizeInventory()];

        readFromNBT(parent.getTagCompound());
    }

    public void setEventHandler(Container eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     *
     * @param slotIndex
     * @param itemStack
     */
    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
        inventory[slotIndex] = itemStack;
        if (itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
        eventHandler.onCraftMatrixChanged(this);
    }

    /**
     * Returns the itemstack in the slot specified (Top left is 0, 0). Args: row, column
     *
     * @param row
     * @param col
     */
    @Override
    public ItemStack getStackInRowAndColumn(int row, int col) {
        if (row >= 0 && row < 3) {
            int k = row + col * 3;
            return this.getStackInSlot(k);
        } else {
            return null;
        }
    }

    /**
     * Returns the stack in slot i
     *
     * @param slotIndex
     */
    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        if (slotIndex < inventory.length)
            return inventory[slotIndex];

        return null;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory() {
        return 9;
    }

    /**
     * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a
     * new stack.
     *
     * @param slotIndex
     * @param amount
     */
    @Override
    public ItemStack decrStackSize(int slotIndex, int amount) {
        if (this.inventory[slotIndex] != null) {
            ItemStack itemstack;

            if (this.inventory[slotIndex].stackSize <= amount) {
                itemstack = this.inventory[slotIndex];
                this.inventory[slotIndex] = null;
                this.eventHandler.onCraftMatrixChanged(this);
                return itemstack;
            } else {
                itemstack = this.inventory[slotIndex].splitStack(amount);

                if (this.inventory[slotIndex].stackSize == 0) {
                    this.inventory[slotIndex] = null;
                }

                this.eventHandler.onCraftMatrixChanged(this);
                return itemstack;
            }
        } else {
            return null;
        }
    }


    /**
     * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
     * like when you close a workbench GUI.
     *
     * @param p_70304_1_
     */
    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_) {
        return null;
    }

    /**
     * Returns the name of the inventory
     */
    @Override
    public String getInventoryName() {
        return "crafting.inventory";
    }

    /**
     * Returns if the inventory is named
     */
    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    /**
     * Returns the maximum stack size for a inventory slot.
     */
    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    @Override
    public void markDirty() {
        //
    }

    @Override
    public void openInventory() {
        //
    }

    @Override
    public void closeInventory() {
        //
    }


    public void onGuiSaved(EntityPlayer entityPlayer) {
        if (parent != null)
            save();
    }

    public ItemStack findParentItemStack(EntityPlayer entityPlayer) {
        if (NBTHelper.hasUUID(parent)) {
            UUID parentUUID = new UUID(parent.getTagCompound().getLong(Names.NBT.MOST_SIG_UUID), parent.getTagCompound().getLong(Names.NBT.LEAST_SIG_UUID));
            for (int i = 0; i < entityPlayer.inventory.getSizeInventory(); i++) {
                ItemStack itemStack = entityPlayer.inventory.getStackInSlot(i);

                if (NBTHelper.hasUUID(itemStack)) {
                    if (itemStack.getTagCompound().getLong(Names.NBT.MOST_SIG_UUID) == parentUUID.getMostSignificantBits() &&
                            itemStack.getTagCompound().getLong(Names.NBT.LEAST_SIG_UUID) == parentUUID.getLeastSignificantBits()) {
                        return itemStack;
                    }
                }
            }
        }

        return null;
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        parent = findParentItemStack(player);
        if (parent != null) {
            nbtTagCompound = parent.getTagCompound();

            if (nbtTagCompound != null && nbtTagCompound.hasKey("Items")) {
                NBTTagList tagList = nbtTagCompound.getTagList("Items", 10);
                this.inventory = new ItemStack[this.getSizeInventory()];

                for (int i = 0; i < tagList.tagCount(); i++) {
                    NBTTagCompound stackTag = tagList.getCompoundTagAt(i);
                    int j = stackTag.getByte("Slot");
                    if (i >= 0 && i <= inventory.length) {
                        this.inventory[j] = ItemStack.loadItemStackFromNBT(stackTag);
                    }
                }
            }
        }
    }

    public void save() {
        NBTTagCompound nbtTagCompound = parent.getTagCompound();

        if (nbtTagCompound == null) {
            nbtTagCompound = new NBTTagCompound();
        }

        writeToNBT(nbtTagCompound);
        parent.setTagCompound(nbtTagCompound);
    }


    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound = findParentItemStack(player).getTagCompound();

        // Write the ItemStacks in the inventory to NBT
        NBTTagList tagList = new NBTTagList();

        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        nbtTagCompound.setTag("Items", tagList);
    }
}
