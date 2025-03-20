package org.obsidian.client.utils.player;

import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.events.player.MoveEvent;
import org.obsidian.client.managers.events.player.MoveInputEvent;
import org.obsidian.client.utils.keyboard.Keyboard;

@UtilityClass
public class MoveUtil implements IMinecraft {
    public double speed() {
        return Math.hypot(mc.player.motion.x, mc.player.motion.z);
    }

    public double speedSqrt() {
        double dx = mc.player.getPosX() - mc.player.lastTickPosX;
        double dz = mc.player.getPosZ() - mc.player.lastTickPosZ;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public void setSpeed(final MoveEvent move, double motion, float yaw, double strafe, double forward) {
        if (forward != 0.0D) {
            if (strafe > 0.0D) {
                yaw += ((forward > 0.0D) ? -45 : 45);
            }
            if (strafe < 0.0D) {
                yaw += ((forward > 0.0D) ? 45 : -45);
            }
            strafe = 0.0D;
            if (forward > 0.0D) {
                forward = 1.0D;
            }
            if (forward < 0.0D) {
                forward = -1.0D;
            }
        }
        if (strafe > 0.0D) {
            strafe = 1.0D;
        }
        if (strafe < 0.0D) {
            strafe = -1.0D;
        }
        double motionx = Math.cos(Math.toRadians((yaw + 90.0F)));
        double motionz = Math.sin(Math.toRadians((yaw + 90.0F)));
        move.getMotion().x = forward * motion * motionx + strafe * motion * motionz;
        move.getMotion().z = forward * motion * motionz - strafe * motion * motionx;
    }

    public static double[] forward(final double d) {
        float f = mc.player.movementInput.moveForward;
        float f2 = mc.player.movementInput.moveStrafe;
        float f3 = mc.player.rotationYaw;
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += ((f > 0.0f) ? -45 : 45);
            } else if (f2 < 0.0f) {
                f3 += ((f > 0.0f) ? 45 : -45);
            }
            f2 = 0.0f;
            if (f > 0.0f) {
                f = 1.0f;
            } else if (f < 0.0f) {
                f = -1.0f;
            }
        }
        final double d2 = Math.sin(Math.toRadians(f3 + 90.0f));
        final double d3 = Math.cos(Math.toRadians(f3 + 90.0f));
        final double d4 = f * d * d3 + f2 * d * d2;
        final double d5 = f * d * d2 - f2 * d * d3;
        return new double[]{d4, d5};
    }


    public void setSpeed(double motion, float yaw, double strafe, double forward) {
        if (forward != 0.0D) {
            if (strafe > 0.0D) {
                yaw += ((forward > 0.0D) ? -45 : 45);
            }
            if (strafe < 0.0D) {
                yaw += ((forward > 0.0D) ? 45 : -45);
            }
            strafe = 0.0D;
            if (forward > 0.0D) {
                forward = 1.0D;
            }
            if (forward < 0.0D) {
                forward = -1.0D;
            }
        }
        if (strafe > 0.0D) {
            strafe = 1.0D;
        }
        if (strafe < 0.0D) {
            strafe = -1.0D;
        }
        double motionx = Math.cos(Math.toRadians((yaw + 90.0F)));
        double motionz = Math.sin(Math.toRadians((yaw + 90.0F)));
        mc.player.motion.x = forward * motion * motionx + strafe * motion * motionz;
        mc.player.motion.z = forward * motion * motionz - strafe * motion * motionx;
    }


    public void setSpeed(final MoveEvent move, final double motion) {
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;
        setSpeed(move, motion, yaw, strafe, forward);
    }

    public void setSpeed(final MoveEvent move, final double motion, final float yaw) {
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        setSpeed(move, motion, yaw, strafe, forward);
    }

    public void setSpeed(double moveSpeed) {
        setSpeed(moveSpeed, mc.player.rotationYaw, mc.player.movementInput.moveStrafe, mc.player.movementInput.moveForward);
    }

    public void setSpeed(double moveSpeed, float yaw) {
        setSpeed(moveSpeed, yaw, mc.player.movementInput.moveStrafe, mc.player.movementInput.moveForward);
    }

    public boolean isMoving() {
        return mc.player.moveForward != 0 || mc.player.moveStrafing != 0;
    }

    public void stop() {
        mc.player.motion.x = 0;
        mc.player.motion.z = 0;
    }

    public double direction(float rotationYaw, final float moveForward, final float moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;
        float forward = 1F;
        if (moveForward < 0F) forward = -0.5F;
        if (moveForward > 0F) forward = 0.5F;
        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;
        return Math.toRadians(rotationYaw);
    }


    public void fixMovement(final MoveInputEvent event, final float yaw) {
        final float forward = event.getForward();
        final float strafe = event.getStrafe();

        final double angle = MathHelper.wrapDegrees(Math.toDegrees(direction(mc.player.isElytraFlying() ? mc.player.rotationYaw : yaw, forward, strafe)));

        if (forward == 0 && strafe == 0) {
            return;
        }

        float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;
        for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
            for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                if (predictedStrafe == 0 && predictedForward == 0) continue;
                final double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(mc.player.rotationYaw, predictedForward, predictedStrafe)));
                final double difference = Math.abs(angle - predictedAngle);
                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }
        }

        event.setForward(closestForward);
        event.setStrafe(closestStrafe);
    }

    public double getInputYaw(float additionYaw) {
        boolean w = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.keyCode.getKeyCode()), a = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.keyCode.getKeyCode()), s = Keyboard.isKeyDown(mc.gameSettings.keyBindBack.keyCode.getKeyCode()), d = Keyboard.isKeyDown(mc.gameSettings.keyBindRight.keyCode.getKeyCode());
        return additionYaw + (a && d && !(w && s) && (w || s) ? w ? 0 : 180 : w && s && !(a && d) && (a || d) ? a ? -90 : 90 : a && d && !w || w && s && !a ? 0 : a || d || s ? (w && !s ? 45 : s && !w ? a || d ? 45 * 3 : 45 * 4 : 45 * 2) * (a ? -1 : 1) : 0);
    }

    public double getInputYaw(double forward, double strafe, float additionYaw) {
        boolean w = forward > 0, a = strafe > 0, s = forward < 0, d = strafe < 0;
        return additionYaw + (a || d || s ? (w ? 45 : s ? a || d ? 45 * 3 : 45 * 4 : 45 * 2) * (a ? -1 : 1) : 0);
    }
}
