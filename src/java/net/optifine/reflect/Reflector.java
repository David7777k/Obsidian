package net.optifine.reflect;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.fonts.FontResourceManager;
import net.minecraft.client.gui.screen.EnchantmentScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.client.renderer.entity.model.*;
import net.minecraft.client.renderer.model.ItemOverride;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.client.renderer.tileentity.model.DragonHeadModel;
import net.minecraft.client.resources.LegacyResourcePackWrapper;
import net.minecraft.client.resources.LegacyResourcePackWrapperV4;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.forge.api.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.resources.IResourcePack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.Ticket;
import net.minecraft.world.server.TicketType;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.optifine.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Reflector {
    public static ReflectorClass BetterFoliageClient = new ReflectorClass("mods.betterfoliage.client.BetterFoliageClient");
    public static ReflectorClass BrandingControl = new ReflectorClass("net.minecraft.forge.fml.BrandingControl");
    public static ReflectorMethod BrandingControl_getBrandings = new ReflectorMethod(BrandingControl, "getBrandings");
    public static ReflectorMethod BrandingControl_forEachLine = new ReflectorMethod(BrandingControl, "forEachLine");
    public static ReflectorMethod BrandingControl_forEachAboveCopyrightLine = new ReflectorMethod(BrandingControl, "forEachAboveCopyrightLine");
    public static ReflectorClass CapabilityProvider = new ReflectorClass("net.minecraft.forge.common.capabilities.CapabilityProvider");
    public static ReflectorMethod CapabilityProvider_gatherCapabilities = new ReflectorMethod(CapabilityProvider, "gatherCapabilities", new Class[0]);
    public static ReflectorClass ClientModLoader = new ReflectorClass("net.minecraft.forge.fml.client.ClientModLoader");
    public static ReflectorMethod ClientModLoader_isLoading = new ReflectorMethod(ClientModLoader, "isLoading");
    public static ReflectorMethod ClientModLoader_renderProgressText = new ReflectorMethod(ClientModLoader, "renderProgressText");
    public static ReflectorClass ChunkDataEvent_Save = new ReflectorClass("net.minecraftforge.event.world.ChunkDataEvent$Save");
    public static ReflectorConstructor ChunkDataEvent_Save_Constructor = new ReflectorConstructor(ChunkDataEvent_Save, new Class[]{IChunk.class, IWorld.class, CompoundNBT.class});
    public static ReflectorClass ChunkEvent_Load = new ReflectorClass("net.minecraftforge.event.world.ChunkEvent$Load");
    public static ReflectorConstructor ChunkEvent_Load_Constructor = new ReflectorConstructor(ChunkEvent_Load, new Class[]{IChunk.class});
    public static ReflectorClass ChunkEvent_Unload = new ReflectorClass("net.minecraftforge.event.world.ChunkEvent$Unload");
    public static ReflectorConstructor ChunkEvent_Unload_Constructor = new ReflectorConstructor(ChunkEvent_Unload, new Class[]{IChunk.class});
    public static ReflectorClass CrashReportExtender = new ReflectorClass("net.minecraft.forge.fml.CrashReportExtender");
    public static ReflectorMethod CrashReportExtender_generateEnhancedStackTraceT = new ReflectorMethod(CrashReportExtender, "generateEnhancedStackTrace", new Class[]{Throwable.class});
    public static ReflectorMethod CrashReportExtender_generateEnhancedStackTraceSTE = new ReflectorMethod(CrashReportExtender, "generateEnhancedStackTrace", new Class[]{StackTraceElement[].class});
    public static ReflectorClass EarlyProgressVisualization = new ReflectorClass("net.minecraft.forge.fml.loading.progress.EarlyProgressVisualization");
    public static ReflectorField EarlyProgressVisualization_INSTANCE = new ReflectorField(EarlyProgressVisualization, "INSTANCE");
    public static ReflectorMethod EarlyProgressVisualization_handOffWindow = new ReflectorMethod(EarlyProgressVisualization, "handOffWindow");
    public static ReflectorClass EntityLeaveWorldEvent = new ReflectorClass("net.minecraftforge.event.entity.EntityLeaveWorldEvent");
    public static ReflectorConstructor EntityLeaveWorldEvent_Constructor = new ReflectorConstructor(EntityLeaveWorldEvent, new Class[]{Entity.class, World.class});
    public static ReflectorClass EntityJoinWorldEvent = new ReflectorClass("net.minecraftforge.event.entity.EntityJoinWorldEvent");
    public static ReflectorConstructor EntityJoinWorldEvent_Constructor = new ReflectorConstructor(EntityJoinWorldEvent, new Class[]{Entity.class, World.class});
    public static ReflectorClass Event = new ReflectorClass("net.minecraft.forge.api.Event");
    public static ReflectorMethod Event_isCanceled = new ReflectorMethod(Event, "isCanceled");
    public static ReflectorMethod Event_getResult = new ReflectorMethod(Event, "getResult");
    public static ReflectorClass EventBus = new ReflectorClass("net.minecraft.forge.api.IEventBus");
    public static ReflectorMethod EventBus_post = new ReflectorMethod(EventBus, "post", new Class[]{Event.class});
    public static ReflectorClass Event_Result = new ReflectorClass("net.minecraft.forge.api.Event$Result");
    public static ReflectorField Event_Result_DENY = new ReflectorField(Event_Result, "DENY");
    public static ReflectorField Event_Result_ALLOW = new ReflectorField(Event_Result, "ALLOW");
    public static ReflectorField Event_Result_DEFAULT = new ReflectorField(Event_Result, "DEFAULT");
    public static ReflectorClass IForgeBlock = new ReflectorClass("net.minecraft.forge.common.extensions.IForgeBlock");
    public static ReflectorMethod IForgeBlock_getTags = new ReflectorMethod(IForgeBlock, "getTags");
    public static ReflectorClass ForgeBlockModelRenderer = new ReflectorClass("net.minecraft.forge.client.model.pipeline.ForgeBlockModelRenderer");
    public static ReflectorConstructor ForgeBlockModelRenderer_Constructor = new ReflectorConstructor(ForgeBlockModelRenderer, new Class[]{BlockColors.class});
    public static ReflectorClass ForgeBlockModelShapes = new ReflectorClass(BlockModelShapes.class);
    public static ReflectorMethod ForgeBlockModelShapes_getTexture3 = new ReflectorMethod(ForgeBlockModelShapes, "getTexture", new Class[]{BlockState.class, World.class, BlockPos.class});
    public static ReflectorClass IForgeBlockState = new ReflectorClass("net.minecraft.forge.common.extensions.IForgeBlockState");
    public static ReflectorMethod IForgeBlockState_addDestroyEffects = new ReflectorMethod(IForgeBlockState, "addDestroyEffects");
    public static ReflectorMethod IForgeBlockState_addHitEffects = new ReflectorMethod(IForgeBlockState, "addHitEffects");
    public static ReflectorMethod IForgeBlockState_getLightValue2 = new ReflectorMethod(IForgeBlockState, "getLightValue", new Class[]{IBlockReader.class, BlockPos.class});
    public static ReflectorMethod IForgeBlockState_getSoundType3 = new ReflectorMethod(IForgeBlockState, "getSoundType", new Class[]{IWorldReader.class, BlockPos.class, Entity.class});
    public static ReflectorMethod IForgeBlockState_getStateAtViewpoint = new ReflectorMethod(IForgeBlockState, "getStateAtViewpoint");
    public static ReflectorMethod IForgeBlockState_hasTileEntity = new ReflectorMethod(IForgeBlockState, "hasTileEntity");
    public static ReflectorMethod IForgeBlockState_isAir2 = new ReflectorMethod(IForgeBlockState, "isAir", new Class[]{IBlockReader.class, BlockPos.class});
    public static ReflectorMethod IForgeBlockState_shouldDisplayFluidOverlay = new ReflectorMethod(IForgeBlockState, "shouldDisplayFluidOverlay");
    public static ReflectorClass IForgeFluid = new ReflectorClass("net.minecraft.forge.common.extensions.IForgeFluid");
    public static ReflectorMethod IForgeFluid_getAttributes = new ReflectorMethod(IForgeFluid, "getAttributes");
    public static ReflectorClass IForgeEntity = new ReflectorClass("net.minecraft.forge.common.extensions.IForgeEntity");
    public static ReflectorMethod IForgeEntity_canUpdate = new ReflectorMethod(IForgeEntity, "canUpdate", new Class[0]);
    public static ReflectorMethod IForgeEntity_getParts = new ReflectorMethod(IForgeEntity, "getParts");
    public static ReflectorMethod IForgeEntity_isMultipartEntity = new ReflectorMethod(IForgeEntity, "isMultipartEntity");
    public static ReflectorMethod IForgeEntity_onAddedToWorld = new ReflectorMethod(IForgeEntity, "onAddedToWorld");
    public static ReflectorMethod IForgeEntity_onRemovedFromWorld = new ReflectorMethod(IForgeEntity, "onRemovedFromWorld");
    public static ReflectorMethod IForgeEntity_shouldRiderSit = new ReflectorMethod(IForgeEntity, "shouldRiderSit");
    public static ReflectorClass FluidAttributes = new ReflectorClass("net.minecraftforge.fluids.FluidAttributes");
    public static ReflectorMethod FluidAttributes_getColor = new ReflectorMethod(FluidAttributes, "getClientColor", new Class[]{IBlockDisplayReader.class, BlockPos.class});
    public static ReflectorClass ForgeEntityType = new ReflectorClass(EntityType.class);
    public static ReflectorMethod ForgeEntityType_getTags = new ReflectorMethod(ForgeEntityType, "getTags");
    public static ReflectorClass ForgeEventFactory = new ReflectorClass("net.minecraftforge.event.ForgeEventFactory");
    public static ReflectorMethod ForgeEventFactory_getMobGriefingEvent = new ReflectorMethod(ForgeEventFactory, "getMobGriefingEvent");
    public static ReflectorClass ForgeFluid = new ReflectorClass(Fluid.class);
    public static ReflectorMethod ForgeFluid_getTags = new ReflectorMethod(ForgeFluid, "getTags");
    public static ReflectorClass ForgeHooks = new ReflectorClass("net.minecraft.forge.common.ForgeHooks");
    public static ReflectorMethod ForgeHooks_onDifficultyChange = new ReflectorMethod(ForgeHooks, "onDifficultyChange");
    public static ReflectorMethod ForgeHooks_onLivingSetAttackTarget = new ReflectorMethod(ForgeHooks, "onLivingSetAttackTarget");
    public static ReflectorClass ForgeHooksClient = new ReflectorClass("net.minecraft.forge.client.ForgeHooksClient");
    public static ReflectorMethod ForgeHooksClient_gatherFluidTextures = new ReflectorMethod(ForgeHooksClient, "gatherFluidTextures");
    public static ReflectorMethod ForgeHooksClient_getArmorTexture = new ReflectorMethod(ForgeHooksClient, "getArmorTexture");
    public static ReflectorMethod ForgeHooksClient_getFluidSprites = new ReflectorMethod(ForgeHooksClient, "getFluidSprites");
    public static ReflectorMethod ForgeHooksClient_getFogDensity = new ReflectorMethod(ForgeHooksClient, "getFogDensity");
    public static ReflectorMethod ForgeHooksClient_getFOVModifier = new ReflectorMethod(ForgeHooksClient, "getFOVModifier");
    public static ReflectorMethod ForgeHooksClient_getOffsetFOV = new ReflectorMethod(ForgeHooksClient, "getOffsetFOV");
    public static ReflectorMethod ForgeHooksClient_onGuiCharTypedPre = new ReflectorMethod(ForgeHooksClient, "onGuiCharTypedPre");
    public static ReflectorMethod ForgeHooksClient_onGuiCharTypedPost = new ReflectorMethod(ForgeHooksClient, "onGuiCharTypedPost");
    public static ReflectorMethod ForgeHooksClient_onGuiKeyPressedPre = new ReflectorMethod(ForgeHooksClient, "onGuiKeyPressedPre");
    public static ReflectorMethod ForgeHooksClient_onGuiKeyPressedPost = new ReflectorMethod(ForgeHooksClient, "onGuiKeyPressedPost");
    public static ReflectorMethod ForgeHooksClient_onGuiKeyReleasedPre = new ReflectorMethod(ForgeHooksClient, "onGuiKeyReleasedPre");
    public static ReflectorMethod ForgeHooksClient_onGuiKeyReleasedPost = new ReflectorMethod(ForgeHooksClient, "onGuiKeyReleasedPost");
    public static ReflectorMethod ForgeHooksClient_onScreenshot = new ReflectorMethod(ForgeHooksClient, "onScreenshot");
    public static ReflectorMethod ForgeHooksClient_onTextureStitchedPre = new ReflectorMethod(ForgeHooksClient, "onTextureStitchedPre");
    public static ReflectorMethod ForgeHooksClient_onTextureStitchedPost = new ReflectorMethod(ForgeHooksClient, "onTextureStitchedPost");
    public static ReflectorMethod ForgeHooksClient_renderMainMenu = new ReflectorMethod(ForgeHooksClient, "renderMainMenu", new Class[]{MainMenuScreen.class, MatrixStack.class, FontRenderer.class, Integer.TYPE, Integer.TYPE, Integer.TYPE});
    public static ReflectorMethod ForgeHooksClient_shouldCauseReequipAnimation = new ReflectorMethod(ForgeHooksClient, "shouldCauseReequipAnimation");
    public static ReflectorClass ForgeConfig = new ReflectorClass("net.minecraft.forge.common.ForgeConfig");
    public static ReflectorField ForgeConfig_CLIENT = new ReflectorField(ForgeConfig, "CLIENT");
    public static ReflectorClass ForgeConfig_Client = new ReflectorClass("net.minecraft.forge.common.ForgeConfig$Client");
    public static ReflectorField ForgeConfig_Client_forgeLightPipelineEnabled = new ReflectorField(ForgeConfig_Client, "forgeLightPipelineEnabled");
    public static ReflectorField ForgeConfig_Client_useCombinedDepthStencilAttachment = new ReflectorField(ForgeConfig_Client, "useCombinedDepthStencilAttachment");
    public static ReflectorClass ForgeConfigSpec = new ReflectorClass("net.minecraft.forge.common.ForgeConfigSpec");
    public static ReflectorField ForgeConfigSpec_childConfig = new ReflectorField(ForgeConfigSpec, "childConfig");
    public static ReflectorClass ForgeConfigSpec_ConfigValue = new ReflectorClass("net.minecraft.forge.common.ForgeConfigSpec$ConfigValue");
    public static ReflectorField ForgeConfigSpec_ConfigValue_defaultSupplier = new ReflectorField(ForgeConfigSpec_ConfigValue, "defaultSupplier");
    public static ReflectorField ForgeConfigSpec_ConfigValue_spec = new ReflectorField(ForgeConfigSpec_ConfigValue, "spec");
    public static ReflectorMethod ForgeConfigSpec_ConfigValue_get = new ReflectorMethod(ForgeConfigSpec_ConfigValue, "get");
    public static ReflectorClass ForgeIChunk = new ReflectorClass(IChunk.class);
    public static ReflectorMethod ForgeIChunk_getWorldForge = new ReflectorMethod(ForgeIChunk, "getWorldForge");
    public static ReflectorClass IForgeItem = new ReflectorClass("net.minecraft.forge.common.extensions.IForgeItem");
    public static ReflectorMethod IForgeItem_getDurabilityForDisplay = new ReflectorMethod(IForgeItem, "getDurabilityForDisplay");
    public static ReflectorMethod IForgeItem_getItemStackTileEntityRenderer = new ReflectorMethod(IForgeItem, "getItemStackTileEntityRenderer");
    public static ReflectorMethod IForgeItem_getRGBDurabilityForDisplay = new ReflectorMethod(IForgeItem, "getRGBDurabilityForDisplay");
    public static ReflectorMethod IForgeItem_isDamageable1 = new ReflectorMethod(IForgeItem, "isDamageable", new Class[]{ItemStack.class});
    public static ReflectorMethod IForgeItem_showDurabilityBar = new ReflectorMethod(IForgeItem, "showDurabilityBar");
    public static ReflectorClass IForgeItemStack = new ReflectorClass("net.minecraft.forge.common.extensions.IForgeItemStack");
    public static ReflectorMethod IForgeItemStack_canDisableShield = new ReflectorMethod(IForgeItemStack, "canDisableShield");
    public static ReflectorMethod IForgeItemStack_getEquipmentSlot = new ReflectorMethod(IForgeItemStack, "getEquipmentSlot");
    public static ReflectorMethod IForgeItemStack_getShareTag = new ReflectorMethod(IForgeItemStack, "getShareTag");
    public static ReflectorMethod IForgeItemStack_getHighlightTip = new ReflectorMethod(IForgeItemStack, "getHighlightTip");
    public static ReflectorMethod IForgeItemStack_isShield = new ReflectorMethod(IForgeItemStack, "isShield");
    public static ReflectorMethod IForgeItemStack_readShareTag = new ReflectorMethod(IForgeItemStack, "readShareTag");
    public static ReflectorClass ForgeItemTags = new ReflectorClass(ItemTags.class);
    public static ReflectorMethod ForgeItemTags_createOptional = ForgeItemTags.makeMethod("createOptional", new Class[]{ResourceLocation.class});
    public static ReflectorClass ForgeKeyBinding = new ReflectorClass(KeyBinding.class);
    public static ReflectorMethod ForgeKeyBinding_setKeyConflictContext = new ReflectorMethod(ForgeKeyBinding, "setKeyConflictContext");
    public static ReflectorMethod ForgeKeyBinding_setKeyModifierAndCode = new ReflectorMethod(ForgeKeyBinding, "setKeyModifierAndCode");
    public static ReflectorMethod ForgeKeyBinding_getKeyModifier = new ReflectorMethod(ForgeKeyBinding, "getKeyModifier");
    public static ReflectorClass IForgeEffectInstance = new ReflectorClass("net.minecraft.forge.common.extensions.IForgeEffectInstance");
    public static ReflectorMethod IForgeEffectInstance_shouldRenderHUD = new ReflectorMethod(IForgeEffectInstance, "shouldRenderHUD");
    public static ReflectorClass ForgeRegistryEntry = new ReflectorClass("net.minecraft.forge.registries.ForgeRegistryEntry");
    public static ReflectorMethod ForgeRegistryEntry_getRegistryName = new ReflectorMethod(ForgeRegistryEntry, "getRegistryName");
    public static ReflectorClass ForgeRenderTypeLookup = new ReflectorClass(RenderTypeLookup.class);
    public static ReflectorMethod ForgeRenderTypeLookup_canRenderInLayerBs = new ReflectorMethod(ForgeRenderTypeLookup, "canRenderInLayer", new Class[]{BlockState.class, RenderType.class});
    public static ReflectorMethod ForgeRenderTypeLookup_canRenderInLayerFs = new ReflectorMethod(ForgeRenderTypeLookup, "canRenderInLayer", new Class[]{FluidState.class, RenderType.class});
    public static ReflectorClass ForgeTicket = new ReflectorClass(Ticket.class);
    public static ReflectorConstructor ForgeTicket_Constructor = ForgeTicket.makeConstructor(new Class[]{TicketType.class, Integer.TYPE, Object.class, Boolean.TYPE});
    public static ReflectorMethod ForgeTicket_isForceTicks = ForgeTicket.makeMethod("isForceTicks");
    public static ReflectorClass IForgeTileEntity = new ReflectorClass("net.minecraft.forge.common.extensions.IForgeTileEntity");
    public static ReflectorMethod IForgeTileEntity_getRenderBoundingBox = new ReflectorMethod(IForgeTileEntity, "getRenderBoundingBox");
    public static ReflectorField ForgeWorld_tileEntitiesToBeRemoved = new ReflectorField(new FieldLocatorTypes(World.class, new Class[]{List.class}, Set.class, new Class[]{Thread.class}, "World.tileEntitiesToBeRemoved"));
    public static ReflectorClass ForgeDimensionRenderInfo = new ReflectorClass(DimensionRenderInfo.class);
    public static ReflectorClass ItemModelMesherForge = new ReflectorClass("net.minecraft.forge.client.ItemModelMesherForge");
    public static ReflectorConstructor ItemModelMesherForge_Constructor = new ReflectorConstructor(ItemModelMesherForge, new Class[]{ModelManager.class});
    public static ReflectorClass KeyConflictContext = new ReflectorClass("net.minecraft.forge.client.settings.KeyConflictContext");
    public static ReflectorField KeyConflictContext_IN_GAME = new ReflectorField(KeyConflictContext, "IN_GAME");
    public static ReflectorClass KeyModifier = new ReflectorClass("net.minecraft.forge.client.settings.KeyModifier");
    public static ReflectorMethod KeyModifier_valueFromString = new ReflectorMethod(KeyModifier, "valueFromString");
    public static ReflectorField KeyModifier_NONE = new ReflectorField(KeyModifier, "NONE");
    public static ReflectorClass Launch = new ReflectorClass("net.minecraft.launchwrapper.Launch");
    public static ReflectorField Launch_blackboard = new ReflectorField(Launch, "blackboard");
    public static ReflectorClass LightUtil = new ReflectorClass("net.minecraft.forge.client.model.pipeline.LightUtil");
    public static ReflectorField LightUtil_itemConsumer = new ReflectorField(LightUtil, "itemConsumer");
    public static ReflectorField LightUtil_tessellator = new ReflectorField(LightUtil, "tessellator");
    public static ReflectorMethod LightUtil_putBakedQuad = new ReflectorMethod(LightUtil, "putBakedQuad");
    public static ReflectorClass Loader = new ReflectorClass("net.minecraft.forge.fml.common.Loader");
    public static ReflectorMethod Loader_getActiveModList = new ReflectorMethod(Loader, "getActiveModList");
    public static ReflectorMethod Loader_instance = new ReflectorMethod(Loader, "instance");
    public static ReflectorClass MinecraftForge = new ReflectorClass("net.minecraft.forge.common.MinecraftForge");
    public static ReflectorField MinecraftForge_EVENT_BUS = new ReflectorField(MinecraftForge, "EVENT_BUS");
    public static ReflectorClass ModContainer = new ReflectorClass("net.minecraft.forge.fml.common.ModContainer");
    public static ReflectorMethod ModContainer_getModId = new ReflectorMethod(ModContainer, "getModId");
    public static ReflectorClass ModelLoaderRegistry = new ReflectorClass("net.minecraft.forge.client.model.ModelLoaderRegistry");
    public static ReflectorMethod ModelLoaderRegistry_onModelLoadingStart = ModelLoaderRegistry.makeMethod("onModelLoadingStart");
    public static ReflectorClass ModListScreen = new ReflectorClass("net.minecraft.forge.fml.client.gui.screen.ModListScreen");
    public static ReflectorConstructor ModListScreen_Constructor = new ReflectorConstructor(ModListScreen, new Class[]{Screen.class});
    public static ReflectorClass NotificationModUpdateScreen = new ReflectorClass("net.minecraft.forge.client.gui.NotificationModUpdateScreen");
    public static ReflectorMethod NotificationModUpdateScreen_init = new ReflectorMethod(NotificationModUpdateScreen, "init", new Class[]{MainMenuScreen.class, Button.class});
    public static ReflectorClass PartEntity = new ReflectorClass("net.minecraftforge.entity.PartEntity");
    public static ReflectorClass RenderItemInFrameEvent = new ReflectorClass("net.minecraft.forge.client.event.RenderItemInFrameEvent");
    public static ReflectorClass ScreenshotEvent = new ReflectorClass("net.minecraft.forge.client.event.ScreenshotEvent");
    public static ReflectorMethod ScreenshotEvent_getCancelMessage = new ReflectorMethod(ScreenshotEvent, "getCancelMessage");
    public static ReflectorMethod ScreenshotEvent_getScreenshotFile = new ReflectorMethod(ScreenshotEvent, "getScreenshotFile");
    public static ReflectorMethod ScreenshotEvent_getResultMessage = new ReflectorMethod(ScreenshotEvent, "getResultMessage");
    public static ReflectorClass ServerLifecycleHooks = new ReflectorClass("net.minecraft.forge.fml.server.ServerLifecycleHooks");
    public static ReflectorMethod ServerLifecycleHooks_handleServerAboutToStart = new ReflectorMethod(ServerLifecycleHooks, "handleServerAboutToStart");
    public static ReflectorMethod ServerLifecycleHooks_handleServerStarting = new ReflectorMethod(ServerLifecycleHooks, "handleServerStarting");
    public static ReflectorClass WorldEvent_Load = new ReflectorClass("net.minecraftforge.event.world.WorldEvent$Load");
    public static ReflectorConstructor WorldEvent_Load_Constructor = new ReflectorConstructor(WorldEvent_Load, new Class[]{IWorld.class});
    public static ReflectorClass EntityItem = new ReflectorClass(ItemEntity.class);
    public static ReflectorField EntityItem_ITEM = new ReflectorField(EntityItem, DataParameter.class);
    public static ReflectorClass EnderDragonRenderer = new ReflectorClass(EnderDragonRenderer.class);
    public static ReflectorField EnderDragonRenderer_model = new ReflectorField(EnderDragonRenderer, EnderDragonRenderer.EnderDragonModel.class);
    public static ReflectorClass GuiEnchantment = new ReflectorClass(EnchantmentScreen.class);
    public static ReflectorField GuiEnchantment_bookModel = new ReflectorField(GuiEnchantment, BookModel.class);
    public static ReflectorClass GuiMainMenu = new ReflectorClass(MainMenuScreen.class);
    public static ReflectorField GuiMainMenu_splashText = new ReflectorField(GuiMainMenu, String.class);
    public static ReflectorClass ItemOverride = new ReflectorClass(ItemOverride.class);
    public static ReflectorField ItemOverride_mapResourceValues = new ReflectorField(ItemOverride, Map.class);
    public static ReflectorClass LegacyResourcePackWrapper = new ReflectorClass(LegacyResourcePackWrapper.class);
    public static ReflectorField LegacyResourcePackWrapper_pack = new ReflectorField(LegacyResourcePackWrapper, IResourcePack.class);
    public static ReflectorClass LegacyResourcePackWrapperV4 = new ReflectorClass(LegacyResourcePackWrapperV4.class);
    public static ReflectorField LegacyResourcePackWrapperV4_pack = new ReflectorField(LegacyResourcePackWrapperV4, IResourcePack.class);
    public static ReflectorClass LayerLlamaDecor = new ReflectorClass(LlamaDecorLayer.class);
    public static ReflectorField LayerLlamaDecor_model = new ReflectorField(LayerLlamaDecor, LlamaModel.class);
    public static ReflectorClass Minecraft = new ReflectorClass(Minecraft.class);
    public static ReflectorField Minecraft_debugFPS = new ReflectorField(new FieldLocatorTypes(Minecraft.class, new Class[]{CrashReport.class}, Integer.TYPE, new Class[]{String.class}, "debugFPS"));
    public static ReflectorField Minecraft_fontResourceManager = new ReflectorField(Minecraft, FontResourceManager.class);
    public static ReflectorClass ModelHumanoidHead = new ReflectorClass(HumanoidHeadModel.class);
    public static ReflectorField ModelHumanoidHead_head = new ReflectorField(ModelHumanoidHead, ModelRenderer.class);
    public static ReflectorClass ModelArmorStand = new ReflectorClass(ArmorStandModel.class);
    public static ReflectorFields ModelArmorStand_ModelRenderers = new ReflectorFields(ModelArmorStand, ModelRenderer.class, 4);
    public static ReflectorClass ModelBat = new ReflectorClass(BatModel.class);
    public static ReflectorFields ModelBat_ModelRenderers = new ReflectorFields(ModelBat, ModelRenderer.class, 6);
    public static ReflectorClass ModelBee = new ReflectorClass(BeeModel.class);
    public static ReflectorFields ModelBee_ModelRenderers = new ReflectorFields(ModelBee, ModelRenderer.class, 10);
    public static ReflectorClass ModelBlaze = new ReflectorClass(BlazeModel.class);
    public static ReflectorField ModelBlaze_blazeHead = new ReflectorField(ModelBlaze, ModelRenderer.class);
    public static ReflectorField ModelBlaze_blazeSticks = new ReflectorField(ModelBlaze, ModelRenderer[].class);
    public static ReflectorClass ModelBoar = new ReflectorClass(BoarModel.class);
    public static ReflectorFields ModelBoar_ModelRenderers = new ReflectorFields(ModelBoar, ModelRenderer.class, 9);
    public static ReflectorClass ModelBook = new ReflectorClass(BookModel.class);
    public static ReflectorFields ModelBook_ModelRenderers = new ReflectorFields(ModelBook, ModelRenderer.class, 7);
    public static ReflectorField ModelBook_bookParts = new ReflectorField(ModelBook, List.class);
    public static ReflectorClass ModelChicken = new ReflectorClass(ChickenModel.class);
    public static ReflectorFields ModelChicken_ModelRenderers = new ReflectorFields(ModelChicken, ModelRenderer.class, 8);
    public static ReflectorClass ModelCod = new ReflectorClass(CodModel.class);
    public static ReflectorFields ModelCod_ModelRenderers = new ReflectorFields(ModelCod, ModelRenderer.class, 7);
    public static ReflectorClass ModelCreeper = new ReflectorClass(CreeperModel.class);
    public static ReflectorFields ModelCreeper_ModelRenderers = new ReflectorFields(ModelCreeper, ModelRenderer.class, 7);
    public static ReflectorClass ModelDragon = new ReflectorClass(EnderDragonRenderer.EnderDragonModel.class);
    public static ReflectorFields ModelDragon_ModelRenderers = new ReflectorFields(ModelDragon, ModelRenderer.class, 20);
    public static ReflectorClass RenderEnderCrystal = new ReflectorClass(EnderCrystalRenderer.class);
    public static ReflectorFields RenderEnderCrystal_modelRenderers = new ReflectorFields(RenderEnderCrystal, ModelRenderer.class, 3);
    public static ReflectorClass ModelEnderMite = new ReflectorClass(EndermiteModel.class);
    public static ReflectorField ModelEnderMite_bodyParts = new ReflectorField(ModelEnderMite, ModelRenderer[].class);
    public static ReflectorClass ModelEvokerFangs = new ReflectorClass(EvokerFangsModel.class);
    public static ReflectorFields ModelEvokerFangs_ModelRenderers = new ReflectorFields(ModelEvokerFangs, ModelRenderer.class, 3);
    public static ReflectorClass ModelGuardian = new ReflectorClass(GuardianModel.class);
    public static ReflectorField ModelGuardian_body = new ReflectorField(ModelGuardian, ModelRenderer.class, 0);
    public static ReflectorField ModelGuardian_eye = new ReflectorField(ModelGuardian, ModelRenderer.class, 1);
    public static ReflectorField ModelGuardian_spines = new ReflectorField(ModelGuardian, ModelRenderer[].class, 0);
    public static ReflectorField ModelGuardian_tail = new ReflectorField(ModelGuardian, ModelRenderer[].class, 1);
    public static ReflectorClass ModelDragonHead = new ReflectorClass(DragonHeadModel.class);
    public static ReflectorField ModelDragonHead_head = new ReflectorField(ModelDragonHead, ModelRenderer.class, 0);
    public static ReflectorField ModelDragonHead_jaw = new ReflectorField(ModelDragonHead, ModelRenderer.class, 1);
    public static ReflectorClass ModelHorse = new ReflectorClass(HorseModel.class);
    public static ReflectorFields ModelHorse_ModelRenderers = new ReflectorFields(ModelHorse, ModelRenderer.class, 11);
    public static ReflectorClass ModelHorseChests = new ReflectorClass(HorseArmorChestsModel.class);
    public static ReflectorFields ModelHorseChests_ModelRenderers = new ReflectorFields(ModelHorseChests, ModelRenderer.class, 2);
    public static ReflectorClass ModelIllager = new ReflectorClass(IllagerModel.class);
    public static ReflectorFields ModelIllager_ModelRenderers = new ReflectorFields(ModelIllager, ModelRenderer.class, 8);
    public static ReflectorClass ModelIronGolem = new ReflectorClass(IronGolemModel.class);
    public static ReflectorFields ModelIronGolem_ModelRenderers = new ReflectorFields(ModelIronGolem, ModelRenderer.class, 6);
    public static ReflectorClass ModelFox = new ReflectorClass(FoxModel.class);
    public static ReflectorFields ModelFox_ModelRenderers = new ReflectorFields(ModelFox, ModelRenderer.class, 10);
    public static ReflectorClass ModelLeashKnot = new ReflectorClass(LeashKnotModel.class);
    public static ReflectorField ModelLeashKnot_knotRenderer = new ReflectorField(ModelLeashKnot, ModelRenderer.class);
    public static ReflectorClass RenderLeashKnot = new ReflectorClass(LeashKnotRenderer.class);
    public static ReflectorField RenderLeashKnot_leashKnotModel = new ReflectorField(RenderLeashKnot, LeashKnotModel.class);
    public static ReflectorClass ModelLlama = new ReflectorClass(LlamaModel.class);
    public static ReflectorFields ModelLlama_ModelRenderers = new ReflectorFields(ModelLlama, ModelRenderer.class, 8);
    public static ReflectorClass ModelLlamaSpit = new ReflectorClass(LlamaSpitModel.class);
    public static ReflectorField ModelLlamaSpit_renderer = new ReflectorField(ModelLlamaSpit, ModelRenderer.class);
    public static ReflectorClass ModelMinecart = new ReflectorClass(MinecartModel.class);
    public static ReflectorField ModelMinecart_sideModels = new ReflectorField(ModelMinecart, ModelRenderer[].class);
    public static ReflectorClass ModelMagmaCube = new ReflectorClass(MagmaCubeModel.class);
    public static ReflectorField ModelMagmaCube_core = new ReflectorField(ModelMagmaCube, ModelRenderer.class);
    public static ReflectorField ModelMagmaCube_segments = new ReflectorField(ModelMagmaCube, ModelRenderer[].class);
    public static ReflectorClass ModelOcelot = new ReflectorClass(OcelotModel.class);
    public static ReflectorFields ModelOcelot_ModelRenderers = new ReflectorFields(ModelOcelot, ModelRenderer.class, 8);
    public static ReflectorClass ModelPhantom = new ReflectorClass(PhantomModel.class);
    public static ReflectorFields ModelPhantom_ModelRenderers = new ReflectorFields(ModelPhantom, ModelRenderer.class, 7);
    public static ReflectorClass ModelParrot = new ReflectorClass(ParrotModel.class);
    public static ReflectorFields ModelParrot_ModelRenderers = new ReflectorFields(ModelParrot, ModelRenderer.class, 11);
    public static ReflectorClass ModelPufferFishBig = new ReflectorClass(PufferFishBigModel.class);
    public static ReflectorFields ModelPufferFishBig_ModelRenderers = new ReflectorFields(ModelPufferFishBig, ModelRenderer.class, 13);
    public static ReflectorClass ModelPufferFishMedium = new ReflectorClass(PufferFishMediumModel.class);
    public static ReflectorFields ModelPufferFishMedium_ModelRenderers = new ReflectorFields(ModelPufferFishMedium, ModelRenderer.class, 11);
    public static ReflectorClass ModelPufferFishSmall = new ReflectorClass(PufferFishSmallModel.class);
    public static ReflectorFields ModelPufferFishSmall_ModelRenderers = new ReflectorFields(ModelPufferFishSmall, ModelRenderer.class, 6);
    public static ReflectorClass ModelQuadruped = new ReflectorClass(QuadrupedModel.class);
    public static ReflectorFields ModelQuadruped_ModelRenderers = new ReflectorFields(ModelQuadruped, ModelRenderer.class, 6);
    public static ReflectorClass ModelRabbit = new ReflectorClass(RabbitModel.class);
    public static ReflectorFields ModelRabbit_ModelRenderers = new ReflectorFields(ModelRabbit, ModelRenderer.class, 12);
    public static ReflectorClass ModelRavager = new ReflectorClass(RavagerModel.class);
    public static ReflectorFields ModelRavager_ModelRenderers = new ReflectorFields(ModelRavager, ModelRenderer.class, 8);
    public static ReflectorClass ModelSalmon = new ReflectorClass(SalmonModel.class);
    public static ReflectorFields ModelSalmon_ModelRenderers = new ReflectorFields(ModelSalmon, ModelRenderer.class, 5);
    public static ReflectorClass ModelShulker = new ReflectorClass(ShulkerModel.class);
    public static ReflectorFields ModelShulker_ModelRenderers = new ReflectorFields(ModelShulker, ModelRenderer.class, 3);
    public static ReflectorClass ModelShulkerBullet = new ReflectorClass(ShulkerBulletModel.class);
    public static ReflectorField ModelShulkerBullet_renderer = new ReflectorField(ModelShulkerBullet, ModelRenderer.class);
    public static ReflectorClass ModelSign = new ReflectorClass(SignTileEntityRenderer.SignModel.class);
    public static ReflectorFields ModelSign_ModelRenderers = new ReflectorFields(ModelSign, ModelRenderer.class, 2);
    public static ReflectorClass ModelGenericHead = new ReflectorClass(GenericHeadModel.class);
    public static ReflectorField ModelGenericHead_skeletonHead = new ReflectorField(ModelGenericHead, ModelRenderer.class);
    public static ReflectorClass ModelSilverfish = new ReflectorClass(SilverfishModel.class);
    public static ReflectorField ModelSilverfish_bodyParts = new ReflectorField(ModelSilverfish, ModelRenderer[].class, 0);
    public static ReflectorField ModelSilverfish_wingParts = new ReflectorField(ModelSilverfish, ModelRenderer[].class, 1);
    public static ReflectorClass ModelSlime = new ReflectorClass(SlimeModel.class);
    public static ReflectorFields ModelSlime_ModelRenderers = new ReflectorFields(ModelSlime, ModelRenderer.class, 4);
    public static ReflectorClass ModelSnowman = new ReflectorClass(SnowManModel.class);
    public static ReflectorFields ModelSnowman_ModelRenderers = new ReflectorFields(ModelSnowman, ModelRenderer.class, 5);
    public static ReflectorClass ModelSpider = new ReflectorClass(SpiderModel.class);
    public static ReflectorFields ModelSpider_ModelRenderers = new ReflectorFields(ModelSpider, ModelRenderer.class, 11);
    public static ReflectorClass ModelSquid = new ReflectorClass(SquidModel.class);
    public static ReflectorField ModelSquid_body = new ReflectorField(ModelSquid, ModelRenderer.class);
    public static ReflectorField ModelSquid_tentacles = new ReflectorField(ModelSquid, ModelRenderer[].class);
    public static ReflectorClass ModelStrider = new ReflectorClass(StriderModel.class);
    public static ReflectorFields ModelStrider_ModelRenderers = new ReflectorFields(ModelStrider, ModelRenderer.class, 9);
    public static ReflectorClass ModelTrident = new ReflectorClass(TridentModel.class);
    public static ReflectorField ModelTrident_tridentRenderer = new ReflectorField(ModelTrident, ModelRenderer.class);
    public static ReflectorClass ModelTropicalFishA = new ReflectorClass(TropicalFishAModel.class);
    public static ReflectorFields ModelTropicalFishA_ModelRenderers = new ReflectorFields(ModelTropicalFishA, ModelRenderer.class, 5);
    public static ReflectorClass ModelTropicalFishB = new ReflectorClass(TropicalFishBModel.class);
    public static ReflectorFields ModelTropicalFishB_ModelRenderers = new ReflectorFields(ModelTropicalFishB, ModelRenderer.class, 6);
    public static ReflectorClass ModelTurtle = new ReflectorClass(TurtleModel.class);
    public static ReflectorField ModelTurtle_body2 = new ReflectorField(ModelTurtle, ModelRenderer.class, 0);
    public static ReflectorClass ModelVex = new ReflectorClass(VexModel.class);
    public static ReflectorField ModelVex_leftWing = new ReflectorField(ModelVex, ModelRenderer.class, 0);
    public static ReflectorField ModelVex_rightWing = new ReflectorField(ModelVex, ModelRenderer.class, 1);
    public static ReflectorClass ModelVillager = new ReflectorClass(VillagerModel.class);
    public static ReflectorFields ModelVillager_ModelRenderers = new ReflectorFields(ModelVillager, ModelRenderer.class, 9);
    public static ReflectorClass ModelWitch = new ReflectorClass(WitchModel.class);
    public static ReflectorField ModelWitch_mole = new ReflectorField(ModelWitch, ModelRenderer.class, 0);
    public static ReflectorClass ModelWither = new ReflectorClass(WitherModel.class);
    public static ReflectorField ModelWither_bodyParts = new ReflectorField(ModelWither, ModelRenderer[].class, 0);
    public static ReflectorField ModelWither_heads = new ReflectorField(ModelWither, ModelRenderer[].class, 1);
    public static ReflectorClass ModelWolf = new ReflectorClass(WolfModel.class);
    public static ReflectorFields ModelWolf_ModelRenderers = new ReflectorFields(ModelWolf, ModelRenderer.class, 10);
    public static ReflectorClass OptiFineResourceLocator = ReflectorForge.getReflectorClassOptiFineResourceLocator();
    public static ReflectorMethod OptiFineResourceLocator_getOptiFineResourceStream = new ReflectorMethod(OptiFineResourceLocator, "getOptiFineResourceStream");
    public static ReflectorClass RenderBoat = new ReflectorClass(BoatRenderer.class);
    public static ReflectorField RenderBoat_modelBoat = new ReflectorField(RenderBoat, BoatModel.class);
    public static ReflectorClass RenderEvokerFangs = new ReflectorClass(EvokerFangsRenderer.class);
    public static ReflectorField RenderEvokerFangs_model = new ReflectorField(RenderEvokerFangs, EvokerFangsModel.class);
    public static ReflectorClass RenderLlamaSpit = new ReflectorClass(LlamaSpitRenderer.class);
    public static ReflectorField RenderLlamaSpit_model = new ReflectorField(RenderLlamaSpit, LlamaSpitModel.class);
    public static ReflectorClass RenderPufferfish = new ReflectorClass(PufferfishRenderer.class);
    public static ReflectorField RenderPufferfish_modelSmall = new ReflectorField(RenderPufferfish, PufferFishSmallModel.class);
    public static ReflectorField RenderPufferfish_modelMedium = new ReflectorField(RenderPufferfish, PufferFishMediumModel.class);
    public static ReflectorField RenderPufferfish_modelBig = new ReflectorField(RenderPufferfish, PufferFishBigModel.class);
    public static ReflectorClass RenderMinecart = new ReflectorClass(MinecartRenderer.class);
    public static ReflectorField RenderMinecart_modelMinecart = new ReflectorField(RenderMinecart, EntityModel.class);
    public static ReflectorClass RenderShulkerBullet = new ReflectorClass(ShulkerBulletRenderer.class);
    public static ReflectorField RenderShulkerBullet_model = new ReflectorField(RenderShulkerBullet, ShulkerBulletModel.class);
    public static ReflectorClass RenderTrident = new ReflectorClass(TridentRenderer.class);
    public static ReflectorField RenderTrident_modelTrident = new ReflectorField(RenderTrident, TridentModel.class);
    public static ReflectorClass RenderTropicalFish = new ReflectorClass(TropicalFishRenderer.class);
    public static ReflectorField RenderTropicalFish_modelA = new ReflectorField(RenderTropicalFish, TropicalFishAModel.class);
    public static ReflectorField RenderTropicalFish_modelB = new ReflectorField(RenderTropicalFish, TropicalFishBModel.class);
    public static ReflectorClass RenderWitherSkull = new ReflectorClass(WitherSkullRenderer.class);
    public static ReflectorField RenderWitherSkull_model = new ReflectorField(RenderWitherSkull, GenericHeadModel.class);
    public static ReflectorClass TileEntityBannerRenderer = new ReflectorClass(BannerTileEntityRenderer.class);
    public static ReflectorFields TileEntityBannerRenderer_modelRenderers = new ReflectorFields(TileEntityBannerRenderer, ModelRenderer.class, 3);
    public static ReflectorClass TileEntityBedRenderer = new ReflectorClass(BedTileEntityRenderer.class);
    public static ReflectorField TileEntityBedRenderer_headModel = new ReflectorField(TileEntityBedRenderer, ModelRenderer.class, 0);
    public static ReflectorField TileEntityBedRenderer_footModel = new ReflectorField(TileEntityBedRenderer, ModelRenderer.class, 1);
    public static ReflectorField TileEntityBedRenderer_legModels = new ReflectorField(TileEntityBedRenderer, ModelRenderer[].class);
    public static ReflectorClass TileEntityBellRenderer = new ReflectorClass(BellTileEntityRenderer.class);
    public static ReflectorField TileEntityBellRenderer_modelRenderer = new ReflectorField(TileEntityBellRenderer, ModelRenderer.class);
    public static ReflectorClass TileEntityBeacon = new ReflectorClass(BeaconTileEntity.class);
    public static ReflectorField TileEntityBeacon_customName = new ReflectorField(TileEntityBeacon, ITextComponent.class);
    public static ReflectorClass TileEntityChestRenderer = new ReflectorClass(ChestTileEntityRenderer.class);
    public static ReflectorFields TileEntityChestRenderer_modelRenderers = new ReflectorFields(TileEntityChestRenderer, ModelRenderer.class, 9);
    public static ReflectorClass TileEntityConduitRenderer = new ReflectorClass(ConduitTileEntityRenderer.class);
    public static ReflectorFields TileEntityConduitRenderer_modelRenderers = new ReflectorFields(TileEntityConduitRenderer, ModelRenderer.class, 4);
    public static ReflectorClass TileEntityEnchantmentTableRenderer = new ReflectorClass(EnchantmentTableTileEntityRenderer.class);
    public static ReflectorField TileEntityEnchantmentTableRenderer_modelBook = new ReflectorField(TileEntityEnchantmentTableRenderer, BookModel.class);
    public static ReflectorClass TileEntityLecternRenderer = new ReflectorClass(LecternTileEntityRenderer.class);
    public static ReflectorField TileEntityLecternRenderer_modelBook = new ReflectorField(TileEntityLecternRenderer, BookModel.class);
    public static ReflectorClass TileEntityShulkerBoxRenderer = new ReflectorClass(ShulkerBoxTileEntityRenderer.class);
    public static ReflectorField TileEntityShulkerBoxRenderer_model = new ReflectorField(TileEntityShulkerBoxRenderer, ShulkerModel.class);
    public static ReflectorClass TileEntitySignRenderer = new ReflectorClass(SignTileEntityRenderer.class);
    public static ReflectorField TileEntitySignRenderer_model = new ReflectorField(TileEntitySignRenderer, SignTileEntityRenderer.SignModel.class);
    public static ReflectorClass TileEntitySkullRenderer = new ReflectorClass(SkullTileEntityRenderer.class);
    public static ReflectorField TileEntitySkullRenderer_MODELS = new ReflectorField(TileEntitySkullRenderer, Map.class, 0);

    public static void callVoid(ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();

            if (method == null) {
                return;
            }

            method.invoke(null, params);
        } catch (Throwable throwable) {
            handleException(throwable, refMethod);
        }
    }

    public static boolean callBoolean(ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();

            if (method == null) {
                return false;
            } else {
                return (Boolean) method.invoke(null, params);
            }
        } catch (Throwable throwable) {
            handleException(throwable, refMethod);
            return false;
        }
    }

    public static float callFloat(ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();

            if (method == null) {
                return 0.0F;
            } else {
                return (Float) method.invoke(null, params);
            }
        } catch (Throwable throwable) {
            handleException(throwable, refMethod);
            return 0.0F;
        }
    }

    public static double callDouble(ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();

            if (method == null) {
                return 0.0D;
            } else {
                return (Double) method.invoke(null, params);
            }
        } catch (Throwable throwable) {
            handleException(throwable, refMethod);
            return 0.0D;
        }
    }

    public static String callString(ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            return method == null ? null : (String) method.invoke(null, params);
        } catch (Throwable throwable) {
            handleException(throwable, refMethod);
            return null;
        }
    }

    public static Object call(ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            return method == null ? null : method.invoke(null, params);
        } catch (Throwable throwable) {
            handleException(throwable, refMethod);
            return null;
        }
    }

    public static boolean callBoolean(Object obj, ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();

            if (method == null) {
                return false;
            } else {
                return (Boolean) method.invoke(obj, params);
            }
        } catch (Throwable throwable) {
            handleException(throwable, refMethod);
            return false;
        }
    }

    public static int callInt(Object obj, ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();

            if (method == null) {
                return 0;
            } else {
                return (Integer) method.invoke(obj, params);
            }
        } catch (Throwable throwable) {
            handleException(throwable, refMethod);
            return 0;
        }
    }

    public static long callLong(Object obj, ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();

            if (method == null) {
                return 0L;
            } else {
                return (Long) method.invoke(obj, params);
            }
        } catch (Throwable throwable) {
            handleException(throwable, refMethod);
            return 0L;
        }
    }

    public static double callDouble(Object obj, ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();

            if (method == null) {
                return 0.0D;
            } else {
                return (Double) method.invoke(obj, params);
            }
        } catch (Throwable throwable) {
            handleException(throwable, refMethod);
            return 0.0D;
        }
    }

    public static String callString(Object obj, ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            return method == null ? null : (String) method.invoke(obj, params);
        } catch (Throwable throwable) {
            handleException(throwable, refMethod);
            return null;
        }
    }

    public static Object call(Object obj, ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            return method == null ? null : method.invoke(obj, params);
        } catch (Throwable throwable) {
            handleException(throwable, refMethod);
            return null;
        }
    }

    public static Object getFieldValue(ReflectorField refField) {
        return getFieldValue(null, refField);
    }

    public static Object getFieldValue(Object obj, ReflectorField refField) {
        try {
            Field field = refField.getTargetField();
            return field == null ? null : field.get(obj);
        } catch (Throwable throwable) {
            Log.error("", throwable);
            return null;
        }
    }

    public static Object getFieldValue(ReflectorFields refFields, int index) {
        ReflectorField reflectorfield = refFields.getReflectorField(index);
        return reflectorfield == null ? null : getFieldValue(reflectorfield);
    }

    public static Object getFieldValue(Object obj, ReflectorFields refFields, int index) {
        ReflectorField reflectorfield = refFields.getReflectorField(index);
        return reflectorfield == null ? null : getFieldValue(obj, reflectorfield);
    }

    public static int getFieldValueInt(ReflectorField refField, int def) {
        return getFieldValueInt(null, refField, def);
    }

    public static int getFieldValueInt(Object obj, ReflectorField refField, int def) {
        try {
            Field field = refField.getTargetField();
            return field == null ? def : field.getInt(obj);
        } catch (Throwable throwable) {
            Log.error("", throwable);
            return def;
        }
    }

    public static boolean setFieldValue(ReflectorField refField, Object value) {
        return setFieldValue(null, refField, value);
    }

    public static boolean setFieldValue(Object obj, ReflectorFields refFields, int index, Object value) {
        ReflectorField reflectorfield = refFields.getReflectorField(index);

        if (reflectorfield == null) {
            return false;
        } else {
            setFieldValue(obj, reflectorfield, value);
            return true;
        }
    }

    public static boolean setFieldValue(Object obj, ReflectorField refField, Object value) {
        try {
            Field field = refField.getTargetField();

            if (field == null) {
                return false;
            } else {
                field.set(obj, value);
                return true;
            }
        } catch (Throwable throwable) {
            Log.error("", throwable);
            return false;
        }
    }

    public static boolean postForgeBusEvent(ReflectorConstructor constr, Object... params) {
        Object object = newInstance(constr, params);
        //noinspection PointlessNullCheck
        return object != null && postForgeBusEvent(object);
    }

    public static boolean postForgeBusEvent(Object event) {
        if (event == null) {
            return false;
        } else {
            Object object = getFieldValue(MinecraftForge_EVENT_BUS);

            if (object == null) {
                return false;
            } else {
                Object object1 = call(object, EventBus_post, event);

                if (!(object1 instanceof Boolean obool)) {
                    return false;
                } else {
                    return obool;
                }
            }
        }
    }

    public static Object newInstance(ReflectorConstructor constr, Object... params) {
        Constructor constructor = constr.getTargetConstructor();

        if (constructor == null) {
            return null;
        } else {
            try {
                return constructor.newInstance(params);
            } catch (Throwable throwable) {
                return null;
            }
        }
    }

    public static boolean matchesTypes(Class[] pTypes, Class[] cTypes) {
        if (pTypes.length != cTypes.length) {
            return false;
        } else {
            for (int i = 0; i < cTypes.length; ++i) {
                Class oclass = pTypes[i];
                Class oclass1 = cTypes[i];

                if (oclass != oclass1) {
                    return false;
                }
            }

            return true;
        }
    }

    private static void handleException(Throwable e, ReflectorMethod refMethod) {
        if (e instanceof InvocationTargetException) {
            Throwable throwable = e.getCause();

            if (throwable instanceof RuntimeException runtimeexception) {
                throw runtimeexception;
            } else {
                Log.error("", e);
            }
        } else {
            Log.warn("*** Exception outside of method ***");
            Log.warn("Method deactivated: " + refMethod.getTargetMethod());
            refMethod.deactivate();

            Log.warn("", e);
        }
    }

}
