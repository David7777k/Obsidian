package org.obsidian.common.impl.viaversion;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.ITextComponent;
import org.obsidian.client.api.client.Constants;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ViaMCP {
    public static final int NATIVE_VERSION = SharedConstants.getProtocolVersion();
    private final List<ProtocolVersion> protocols;
    private final VersionSelectScreen viaScreen;
    public static ViaMCP INSTANCE;

    public static void create() {
        INSTANCE = new ViaMCP();
    }

    public ViaMCP() {
        List<ProtocolVersion> protocolList = ProtocolVersion.getProtocols().stream().filter(pv -> pv.getVersion() == 47 || pv.getVersion() >= 107).sorted((f, s) -> Integer.compare(s.getVersion(), f.getVersion())).toList();
        this.protocols = new ArrayList<>(protocolList.size() + 1);
        this.protocols.addAll(protocolList);
        ViaLoadingBase.ViaLoadingBaseBuilder.create().runDirectory(Constants.MAIN_DIR.resolve("viaversion").toFile()).nativeVersion(754).build();
        this.viaScreen = new VersionSelectScreen(Minecraft.getInstance().fontRenderer, 5, 5, 100, 20, ITextComponent.getTextComponentOrEmpty(ProtocolVersion.getProtocol(NATIVE_VERSION).getName()));
    }
}

