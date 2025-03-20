package net.minecraft.client.gui.overlay;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.ClientBossInfo;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import net.optifine.Config;
import net.optifine.CustomColors;
import org.obsidian.client.managers.module.impl.render.NoRender;

import java.util.Map;
import java.util.UUID;

public class BossOverlayGui extends AbstractGui {
    private static final ResourceLocation GUI_BARS_TEXTURES = new ResourceLocation("textures/gui/bars.png");
    private final Minecraft client;
    @Getter
    private final Map<UUID, ClientBossInfo> mapBossInfos = Maps.newConcurrentMap();

    public BossOverlayGui(Minecraft clientIn) {
        this.client = clientIn;
    }

    public void func_238484_a_(MatrixStack p_238484_1_) {
        NoRender noRender = NoRender.getInstance();
        if (noRender.isEnabled() && noRender.elements().getValue("Боссбар")) return;

        if (!this.mapBossInfos.isEmpty()) {
            int i = this.client.getMainWindow().getScaledWidth();
            int j = 12;

            for (ClientBossInfo clientbossinfo : this.mapBossInfos.values()) {
                int k = i / 2 - 91;
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                boolean flag = true;
                int l = 19;


                this.client.getTextureManager().bindTexture(GUI_BARS_TEXTURES);
                this.func_238485_a_(p_238484_1_, k, j, clientbossinfo);
                ITextComponent itextcomponent = clientbossinfo.getName();
                int i1 = this.client.fontRenderer.getStringPropertyWidth(itextcomponent);
                int j1 = i / 2 - i1 / 2;
                int k1 = j - 9;
                int l1 = 16777215;

                if (Config.isCustomColors()) {
                    l1 = CustomColors.getBossTextColor(l1);
                }

                this.client.fontRenderer.drawStringWithShadow(p_238484_1_, itextcomponent, (float) j1, (float) k1, l1);

                j += l;

                if (j >= this.client.getMainWindow().getScaledHeight() / 3) {
                    break;
                }
            }
        }
    }

    private void func_238485_a_(MatrixStack p_238485_1_, int p_238485_2_, int p_238485_3_, BossInfo p_238485_4_) {
        this.blit(p_238485_1_, p_238485_2_, p_238485_3_, 0, p_238485_4_.getColor().ordinal() * 5 * 2, 182, 5);

        if (p_238485_4_.getOverlay() != BossInfo.Overlay.PROGRESS) {
            this.blit(p_238485_1_, p_238485_2_, p_238485_3_, 0, 80 + (p_238485_4_.getOverlay().ordinal() - 1) * 5 * 2, 182, 5);
        }

        int i = (int) (p_238485_4_.getPercent() * 183.0F);

        if (i > 0) {
            this.blit(p_238485_1_, p_238485_2_, p_238485_3_, 0, p_238485_4_.getColor().ordinal() * 5 * 2 + 5, i, 5);

            if (p_238485_4_.getOverlay() != BossInfo.Overlay.PROGRESS) {
                this.blit(p_238485_1_, p_238485_2_, p_238485_3_, 0, 80 + (p_238485_4_.getOverlay().ordinal() - 1) * 5 * 2 + 5, i, 5);
            }
        }
    }

    public void read(SUpdateBossInfoPacket packetIn) {
        UUID uniqueId = packetIn.getUniqueId();
        SUpdateBossInfoPacket.Operation operation = packetIn.getOperation();

        switch (operation) {
            case ADD:
                this.mapBossInfos.put(uniqueId, new ClientBossInfo(packetIn));
                break;
            case REMOVE:
                this.mapBossInfos.remove(uniqueId);
                break;
            default:
                ClientBossInfo bossInfo = this.mapBossInfos.get(uniqueId);
                if (bossInfo != null) {
                    bossInfo.updateFromPacket(packetIn);
                }
                break;
        }
    }


    public void clearBossInfos() {
        this.mapBossInfos.clear();
    }

    public boolean shouldPlayEndBossMusic() {
        if (!this.mapBossInfos.isEmpty()) {
            for (BossInfo bossinfo : this.mapBossInfos.values()) {
                if (bossinfo.shouldPlayEndBossMusic()) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean shouldDarkenSky() {
        if (!this.mapBossInfos.isEmpty()) {
            for (BossInfo bossinfo : this.mapBossInfos.values()) {
                if (bossinfo.shouldDarkenSky()) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean shouldCreateFog() {
        if (!this.mapBossInfos.isEmpty()) {
            for (BossInfo bossinfo : this.mapBossInfos.values()) {
                if (bossinfo.shouldCreateFog()) {
                    return true;
                }
            }
        }

        return false;
    }
}
