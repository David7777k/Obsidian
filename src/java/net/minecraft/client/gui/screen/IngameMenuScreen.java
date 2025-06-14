package net.minecraft.client.gui.screen;

import net.minecraft.client.gui.advancements.AdvancementsScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.screen.mainmenu.MainMenu;

public class IngameMenuScreen extends Screen {
    private final boolean isFullMenu;

    public IngameMenuScreen(boolean isFullMenu) {
        super(isFullMenu ? new TranslationTextComponent("menu.game") : new TranslationTextComponent("menu.paused"));
        this.isFullMenu = isFullMenu;
    }

    protected void init() {
        if (this.isFullMenu) {
            this.addButtons();
        }
    }

    private void addButtons() {
        int i = -16;
        int j = 98;
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, new TranslationTextComponent("menu.returnToGame"), (button2) ->
        {
            this.minecraft.displayScreen(null);
            this.minecraft.mouseHelper.grabMouse();
        }));
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 48 + -16, 98, 20, new TranslationTextComponent("gui.advancements"), (button2) ->
        {
            this.minecraft.displayScreen(new AdvancementsScreen(this.minecraft.player.connection.getAdvancementManager()));
        }));
        this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 48 + -16, 98, 20, new TranslationTextComponent("gui.stats"), (button2) ->
        {
            this.minecraft.displayScreen(new StatsScreen(this, this.minecraft.player.getStats()));
        }));
        String s = SharedConstants.getVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game";
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 72 + -16, 98, 20, new TranslationTextComponent("menu.sendFeedback"), (button2) ->
        {
            this.minecraft.displayScreen(new ConfirmOpenLinkScreen((open) -> {
                if (open) {
                    Util.getOSType().openURI(s);
                }

                this.minecraft.displayScreen(this);
            }, s, true));
        }));
        this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 72 + -16, 98, 20, new TranslationTextComponent("menu.reportBugs"), (button2) ->
        {
            this.minecraft.displayScreen(new ConfirmOpenLinkScreen((open) -> {
                if (open) {
                    Util.getOSType().openURI("https://aka.ms/snapshotbugs?ref=game");
                }

                this.minecraft.displayScreen(this);
            }, "https://aka.ms/snapshotbugs?ref=game", true));
        }));
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 96 + -16, 98, 20, new TranslationTextComponent("menu.options"), (button2) ->
        {
            this.minecraft.displayScreen(new OptionsScreen(this, this.minecraft.gameSettings));
        }));
        Button button = this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 96 + -16, 98, 20, new TranslationTextComponent("menu.shareToLan"), (button2) ->
        {
            this.minecraft.displayScreen(new ShareToLanScreen(this));
        }));
        button.active = this.minecraft.isSingleplayer() && !this.minecraft.getIntegratedServer().getPublic();
        Button button1 = this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20, new TranslationTextComponent("menu.returnToMenu"), (button2) ->
        {
            boolean flag = this.minecraft.isIntegratedServerRunning();
            button2.active = false;
            this.minecraft.world.sendQuittingDisconnectingPacket();

            if (flag) {
                this.minecraft.unloadWorld(new DirtMessageScreen(new TranslationTextComponent("menu.savingLevel")));
            } else {
                this.minecraft.unloadWorld();
            }

            if (flag) {
                this.minecraft.displayScreen(new MainMenu());
            } else {
                this.minecraft.displayScreen(new MultiplayerScreen(new MainMenu()));
            }
        }));

        if (!this.minecraft.isIntegratedServerRunning()) {
            button1.setMessage(new TranslationTextComponent("menu.disconnect"));
        }
    }

    public void tick() {
        super.tick();
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.isFullMenu) {
            this.renderBackground(matrixStack);
            drawCenteredStringWithShadow(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
        } else {
            drawCenteredStringWithShadow(matrixStack, this.font, this.title, this.width / 2, 10, 16777215);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
