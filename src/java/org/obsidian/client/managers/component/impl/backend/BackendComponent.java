package org.obsidian.client.managers.component.impl.backend;

import org.obsidian.client.managers.component.Component;

public class BackendComponent extends Component {
//    private final StopWatch time = new StopWatch();
//    private final ThreadPool executor = new ThreadPool();
//
//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onEvent(GameUpdateEvent event) {
//        ExcNetwork network = Obsidian.inst().excNetwork();
//        if (network == null) return;
//        if (time.finished(1000 * 10)) {
//            executor.execute(() -> {
//                network.setRunning(false);
//                network.handleConnectionLost();
//            });
//            time.reset();
//        }
//    }
//
//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onEvent(BackendPacketEvent event) {
//        ExcNetwork network = Obsidian.inst().excNetwork();
//        if (network.getCommunication() == null) return;
//        final ServerPacket packet = event.getPacket();
//        if (IRC.getInstance().isEnabled() && packet instanceof SMessagePacket wrapper) {
//            boolean server = wrapper.role().equalsIgnoreCase("system");
//            boolean developer = wrapper.id() <= 3 && wrapper.id() != 0;
//            String role = server ? wrapper.role().toUpperCase() : developer ? "Разработчик" : wrapper.role();
//
//            String roleColor = String.valueOf(server || developer ? TextFormatting.RED : TextFormatting.GRAY);
//            String userColor = String.valueOf(developer ? TextFormatting.RED : TextFormatting.WHITE);
//            String messageColor = String.valueOf(server ? TextFormatting.DARK_RED : TextFormatting.GRAY);
//
//            StringBuilder messageBuilder = new StringBuilder();
//            messageBuilder.append(TextFormatting.GRAY)
//                    .append("[")
//                    .append(TextFormatting.GREEN)
//                    .append(TextFormatting.BOLD)
//                    .append("IRC")
//                    .append(TextFormatting.GRAY)
//                    .append("] ")
//                    .append("[")
//                    .append(roleColor)
//                    .append(role)
//                    .append((server || developer) ? "" : "/" + wrapper.id())
//                    .append(TextFormatting.GRAY)
//                    .append("]");
//
//            if (!server) {
//                messageBuilder.append(" [")
//                        .append(userColor)
//                        .append(wrapper.user())
//                        .append(TextFormatting.GRAY)
//                        .append("]");
//            }
//
//            messageBuilder.append(TextFormatting.DARK_GRAY)
//                    .append(": ")
//                    .append(messageColor)
//                    .append(TextFormatting.removeFormatting(wrapper.message()));
//
//            ChatUtil.addText(messageBuilder.toString());
//        } else if (packet instanceof SLoginPacket wrapper) {
//            if (wrapper.success()) {
//                Obsidian.log("Connected to backend!");
//                network.setConnected(true);
//            }
//        } else if (packet instanceof SKeepAlivePacket) {
//            network.setConnected(true);
//            time.reset();
//            network.handleKeepAlive();
//        } else if (!Obsidian.devMode() && packet instanceof SKickPacket wrapper) {
//            network.getCommunication().write(new CKickPacket(Obsidian.inst().userData().id()));
//            network.closeConnection();
//            CrashReport crashreport = CrashReport.makeCrashReport(new Exception(wrapper.reason()), "Reason: " + wrapper.reason());
//            Minecraft.displayCrashReport(crashreport);
//        }
//    }

}