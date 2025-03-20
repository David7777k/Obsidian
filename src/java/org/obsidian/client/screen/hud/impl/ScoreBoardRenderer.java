package org.obsidian.client.screen.hud.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.managers.events.render.RenderScoreBoardEvent;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.managers.module.settings.impl.DragSetting;
import org.obsidian.client.screen.hud.IScoreBoardRenerer;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreBoardRenderer implements IScoreBoardRenerer {
    private final DragSetting drag;

    public ScoreBoardRenderer(DragSetting drag) {
        this.drag = drag;
    }

    @Override
    public void renderScoreBoard(RenderScoreBoardEvent event) {
        event.cancel();
        MatrixStack matrix = event.getMatrix();
        FontRenderer fontRenderer = event.getFontRenderer();
        Theme theme = Theme.getInstance();
        ScoreObjective objective = event.getObjective();
        Scoreboard scoreboard = objective.getScoreboard();
        Collection<Score> collection = scoreboard.getSortedScores(objective);
        List<Score> list = collection.stream().filter((p_lambda$renderScoreboard$1_0_) -> !p_lambda$renderScoreboard$1_0_.getPlayerName().startsWith("#")).collect(Collectors.toList());
        collection = list.size() > 15 ? Lists.newArrayList(Iterables.skip(list, collection.size() - 15)) : list;

        List<Pair<Score, ITextComponent>> list1 = Lists.newArrayListWithCapacity(collection.size());
        ITextComponent itextcomponent = objective.getDisplayName();
        int i = fontRenderer.getStringPropertyWidth(itextcomponent);
        int width = i;
        int k = fontRenderer.getStringWidth(": ");

        for (Score score : collection) {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            ITextComponent itextcomponent1 = ScorePlayerTeam.func_237500_a_(scoreplayerteam, new StringTextComponent(score.getPlayerName()));
            list1.add(Pair.of(score, itextcomponent1));
            width = Math.max(width, fontRenderer.getStringPropertyWidth(itextcomponent1) + k + fontRenderer.getStringWidth(Integer.toString(score.getScorePoints())));
        }
        float x = drag.position.x;
        int height = collection.size() * 9 + 13;
        float y = drag.position.y;
        drag.size.set(width, height);
        int l = 0;

        theme.drawClientRect(matrix, x, y, width, height);
        for (Pair<Score, ITextComponent> pair : list1) {
            ++l;
            ITextComponent itextcomponent2 = pair.getSecond();
            float y1 = y - l * 9 + height;

            fontRenderer.drawString(matrix, itextcomponent2, x + 4, y1, -1);

            if (l == collection.size())
                fontRenderer.drawString(matrix, itextcomponent, x + width / 2F - i / 2F, y + 4, -1);
        }
    }
}
