package org.obsidian.common.impl.viaversion.provider;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.base.BaseVersionProvider;
import org.obsidian.common.impl.viaversion.ViaLoadingBase;

public class ViaBaseVersionProvider extends BaseVersionProvider {
    @Override
    public int getClosestServerProtocol(UserConnection connection) throws Exception {
        if (connection.isClientSide()) {
            return ViaLoadingBase.getInstance().getTargetVersion().getVersion();
        }
        return super.getClosestServerProtocol(connection);
    }
}

