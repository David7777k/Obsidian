package org.obsidian.client.managers.events.other;

import com.mojang.datafixers.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.text.ITextComponent;
import org.obsidian.client.api.events.Event;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ScoreBoardEvent extends Event {
    private List<Pair<Score, ITextComponent>> list;
}