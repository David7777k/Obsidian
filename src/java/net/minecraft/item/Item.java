package net.minecraft.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.obsidian.common.impl.fastrandom.FastRandom;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Item implements IItemProvider {
    public static final Map<Block, Item> BLOCK_TO_ITEM = Maps.newHashMap();
    protected static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    protected static final Random random = new FastRandom();
    protected final ItemGroup group;
    private final Rarity rarity;
    private final int maxStackSize;
    private final int maxDamage;
    private final boolean burnable;
    private final Item containerItem;
    @Nullable
    private String translationKey;
    @Nullable
    private final Food food;

    public static int getIdFromItem(Item itemIn) {
        return itemIn == null ? 0 : Registry.ITEM.getId(itemIn);
    }

    public static Item getItemById(int id) {
        return Registry.ITEM.getByValue(id);
    }


    public static Item getItemFromBlock(Block blockIn) {
        return BLOCK_TO_ITEM.getOrDefault(blockIn, Items.AIR);
    }

    public Item(Item.Properties properties) {
        this.group = properties.group;
        this.rarity = properties.rarity;
        this.containerItem = properties.containerItem;
        this.maxDamage = properties.maxDamage;
        this.maxStackSize = properties.maxStackSize;
        this.food = properties.food;
        this.burnable = properties.immuneToFire;
    }

    /**
     * Called as the item is being used by an entity.
     */
    public void onUse(World worldIn, LivingEntity livingEntityIn, ItemStack stack, int count) {
    }

    /**
     * Called when an ItemStack with NBT data is read to potentially that ItemStack's NBT data
     */
    public boolean updateItemStackNBT(CompoundNBT nbt) {
        return false;
    }

    public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        return true;
    }

    public Item asItem() {
        return this;
    }

    /**
     * Called when this item is used when targetting a Block
     */
    public ActionResultType onItemUse(ItemUseContext context) {
        return ActionResultType.PASS;
    }

    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 1.0F;
    }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (this.isFood()) {
            ItemStack itemstack = playerIn.getHeldItem(handIn);

            if (playerIn.canEat(this.getFood().canEatWhenFull())) {
                playerIn.setActiveHand(handIn);
                return ActionResult.resultConsume(itemstack);
            } else {
                return ActionResult.resultFail(itemstack);
            }
        } else {
            return ActionResult.resultPass(playerIn.getHeldItem(handIn));
        }
    }

    /**
     * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
     * the Item before the action is complete.
     */
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        return this.isFood() ? entityLiving.onFoodEaten(worldIn, stack) : stack;
    }

    /**
     * Returns the maximum size of the stack for a specific item.
     */
    public final int getMaxStackSize() {
        return this.maxStackSize;
    }

    /**
     * Returns the maximum damage an item can take.
     */
    public final int getMaxDamage() {
        return this.maxDamage;
    }

    public boolean isDamageable() {
        return this.maxDamage > 0;
    }

    /**
     * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
     * the damage on the stack.
     */
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return false;
    }

    /**
     * Called when a Block is destroyed using this Item. Return true to trigger the "Use Item" statistic.
     */
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        return false;
    }

    /**
     * Check whether this Item can harvest the given Block
     */
    public boolean canHarvestBlock(BlockState blockIn) {
        return false;
    }

    /**
     * Returns true if the item can be used on the given entity, e.g. shears on sheep.
     */
    public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
        return ActionResultType.PASS;
    }

    public ITextComponent getName() {
        return new TranslationTextComponent(this.getTranslationKey());
    }

    public String toString() {
        return Registry.ITEM.getKey(this).getPath();
    }

    protected String getDefaultTranslationKey() {
        if (this.translationKey == null) {
            this.translationKey = Util.makeTranslationKey("item", Registry.ITEM.getKey(this));
        }

        return this.translationKey;
    }

    /**
     * Returns the unlocalized name of this item.
     */
    public String getTranslationKey() {
        return this.getDefaultTranslationKey();
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getTranslationKey(ItemStack stack) {
        return this.getTranslationKey();
    }

    /**
     * If this function returns true (or the item is damageable), the ItemStack's NBT tag will be sent to the client.
     */
    public boolean shouldSyncTag() {
        return true;
    }

    @Nullable
    public final Item getContainerItem() {
        return this.containerItem;
    }

    /**
     * True if this Item has a container item (a.k.a. crafting result)
     */
    public boolean hasContainerItem() {
        return this.containerItem != null;
    }

    /**
     * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
     * update it's contents.
     */
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
    }

    /**
     * Called when item is crafted/smelted. Used only by maps so far.
     */
    public void onCreated(ItemStack stack, World worldIn, PlayerEntity playerIn) {
    }

    /**
     * Returns {@code true} if this is a complex item.
     */
    public boolean isComplex() {
        return false;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public UseAction getUseAction(ItemStack stack) {
        return stack.getItem().isFood() ? UseAction.EAT : UseAction.NONE;
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getUseDuration(ItemStack stack) {
        if (stack.getItem().isFood()) {
            return this.getFood().isFastEating() ? 16 : 32;
        } else {
            return 0;
        }
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
    }

    public ITextComponent getDisplayName(ItemStack stack) {
        return new TranslationTextComponent(this.getTranslationKey(stack));
    }

    /**
     * Returns true if this item has an enchantment glint. By default, this returns
     * <code>stack.isItemEnchanted()</code>, but other items can override it (for instance, written books always return
     * true).
     * <p>
     * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
     * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
     */
    public boolean hasEffect(ItemStack stack) {
        return stack.isEnchanted();
    }

    /**
     * Return an item rarity from EnumRarity
     */
    public Rarity getRarity(ItemStack stack) {
        if (!stack.isEnchanted()) {
            return this.rarity;
        } else {
            switch (this.rarity) {
                case COMMON:
                case UNCOMMON:
                    return Rarity.RARE;

                case RARE:
                    return Rarity.EPIC;

                case EPIC:
                default:
                    return this.rarity;
            }
        }
    }

    /**
     * Checks isDamagable and if it cannot be stacked
     */
    public boolean isEnchantable(ItemStack stack) {
        return this.getMaxStackSize() == 1 && this.isDamageable();
    }

    protected static BlockRayTraceResult rayTrace(World worldIn, PlayerEntity player, RayTraceContext.FluidMode fluidMode) {
        float f = player.rotationPitch;
        float f1 = player.rotationYaw;
        Vector3d vector3d = player.getEyePosition(1.0F);
        float f2 = MathHelper.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f3 = MathHelper.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -MathHelper.cos(-f * ((float) Math.PI / 180F));
        float f5 = MathHelper.sin(-f * ((float) Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d0 = 5.0D;
        Vector3d vector3d1 = vector3d.add((double) f6 * 5.0D, (double) f5 * 5.0D, (double) f7 * 5.0D);
        return worldIn.rayTraceBlocks(new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.OUTLINE, fluidMode, player));
    }

    /**
     * Return the enchantability factor of the item, most of the time is based on material.
     */
    public int getItemEnchantability() {
        return 0;
    }

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            items.add(new ItemStack(this));
        }
    }

    protected boolean isInGroup(ItemGroup group) {
        ItemGroup itemgroup = this.getGroup();
        return itemgroup != null && (group == ItemGroup.SEARCH || group == itemgroup);
    }

    @Nullable

    /**
     * gets the CreativeTab this item is displayed on
     */
    public final ItemGroup getGroup() {
        return this.group;
    }

    /**
     * Return whether this item is repairable in an anvil.
     */
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
        return ImmutableMultimap.of();
    }

    /**
     * If this itemstack's item is a crossbow
     */
    public boolean isCrossbow(ItemStack stack) {
        return stack.getItem() == Items.CROSSBOW;
    }

    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }

    public boolean isIn(ITag<Item> tagIn) {
        return tagIn.contains(this);
    }

    public boolean isFood() {
        return this.food != null;
    }

    @Nullable
    public Food getFood() {
        return this.food;
    }

    public SoundEvent getDrinkSound() {
        return SoundEvents.ENTITY_GENERIC_DRINK;
    }

    public SoundEvent getEatSound() {
        return SoundEvents.ENTITY_GENERIC_EAT;
    }

    public boolean isImmuneToFire() {
        return this.burnable;
    }

    public boolean isDamageable(DamageSource damageSource) {
        return !this.burnable || !damageSource.isFireDamage();
    }

    public static class Properties {
        private int maxStackSize = 64;
        private int maxDamage;
        private Item containerItem;
        private ItemGroup group;
        private Rarity rarity = Rarity.COMMON;
        private Food food;
        private boolean immuneToFire;

        public Item.Properties food(Food foodIn) {
            this.food = foodIn;
            return this;
        }

        public Item.Properties maxStackSize(int maxStackSizeIn) {
            if (this.maxDamage > 0) {
                throw new RuntimeException("Unable to have damage AND stack.");
            } else {
                this.maxStackSize = maxStackSizeIn;
                return this;
            }
        }

        public Item.Properties defaultMaxDamage(int maxDamageIn) {
            return this.maxDamage == 0 ? this.maxDamage(maxDamageIn) : this;
        }

        public Item.Properties maxDamage(int maxDamageIn) {
            this.maxDamage = maxDamageIn;
            this.maxStackSize = 1;
            return this;
        }

        public Item.Properties containerItem(Item containerItemIn) {
            this.containerItem = containerItemIn;
            return this;
        }

        public Item.Properties group(ItemGroup groupIn) {
            this.group = groupIn;
            return this;
        }

        public Item.Properties rarity(Rarity rarityIn) {
            this.rarity = rarityIn;
            return this;
        }

        public Item.Properties isImmuneToFire() {
            this.immuneToFire = true;
            return this;
        }
    }
}
