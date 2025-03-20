package net.minecraft.client.gui.chat;


import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class NarratorChatListener implements IChatListener {
    public static final ITextComponent EMPTY = StringTextComponent.EMPTY;
    private static final Logger LOGGER = LogManager.getLogger();
    public static final NarratorChatListener INSTANCE = new NarratorChatListener();

    public void say(ChatType chatTypeIn, ITextComponent message, UUID sender) {

    }

    public void say() {

    }

    public boolean isActive() {
        return false;
    }

}
