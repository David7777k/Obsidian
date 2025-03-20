package org.obsidian.common.impl.globals;

import lombok.experimental.UtilityClass;

import java.util.HashMap;

/**
 * @author ConeTin
 * @since 13 июн. 2024 г.
 */

@UtilityClass
public class ClientAPI {

    public final HashMap<String, String> USERS = new HashMap<>();

    public void update(String data) {
        if (data == null) return;
        USERS.clear();
        String[] userDatas = data.split("&");
        for (String userData : userDatas) {
            String[] parts = userData.split("/");
            if (parts.length < 2) continue;
            USERS.put(parts[0], parts[1]);
        }
    }

    public String getClient(String nick) {
        return USERS.get(nick);
    }

    public boolean isRockstar(String nick) {
        String client = getClient(nick);
        if (client == null) return false;
        return client.equals("rockstar");
    }

}
