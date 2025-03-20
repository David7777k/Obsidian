package org.obsidian.common.impl.viaversion.model;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import lombok.Getter;
import org.obsidian.common.impl.viaversion.ViaLoadingBase;


@Getter
public class ComparableProtocolVersion
        extends ProtocolVersion {
    private final int index;

    public ComparableProtocolVersion(int version, String name, int index) {
        super(version, name);
        this.index = index;
    }

    public boolean olderThan(ProtocolVersion other) {
        return this.getIndex() > ViaLoadingBase.fromProtocolVersion(other).getIndex();
    }

    public boolean olderThanOrEqualTo(ProtocolVersion other) {
        return this.getIndex() >= ViaLoadingBase.fromProtocolVersion(other).getIndex();
    }

    public boolean newerThan(ProtocolVersion other) {
        return this.getIndex() < ViaLoadingBase.fromProtocolVersion(other).getIndex();
    }

    public boolean newerThanOrEqualTo(ProtocolVersion other) {
        return this.getIndex() <= ViaLoadingBase.fromProtocolVersion(other).getIndex();
    }

    public boolean equalTo(ProtocolVersion other) {
        return this.getIndex() == ViaLoadingBase.fromProtocolVersion(other).getIndex();
    }

}

