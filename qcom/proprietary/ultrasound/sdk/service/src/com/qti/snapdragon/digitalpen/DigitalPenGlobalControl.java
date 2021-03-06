/*===========================================================================
                           DigitalPenGlobalControl.java

Copyright (c) 2013-2014 Qualcomm Technologies, Inc.  All Rights Reserved.
Qualcomm Technologies Proprietary and Confidential.
==============================================================================*/

package com.qti.snapdragon.digitalpen;


import com.qti.snapdragon.digitalpen.util.DigitalPenConfig;
import com.qti.snapdragon.digitalpen.IDigitalPenService;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;

public class DigitalPenGlobalControl {

    public static class DigitalPenGlobalSettings {

        public enum OffScreenMode {
            DISABLED(DigitalPenConfig.DP_OFF_SCREEN_MODE_DISABLED),
            DUPLICATE(DigitalPenConfig.DP_OFF_SCREEN_MODE_DUPLICATE);

            private final byte code;

            private OffScreenMode(byte code) {
                this.code = code;
            }

            private static OffScreenMode fromCode(byte code) {
                for (OffScreenMode mode : OffScreenMode.values()) {
                    if (mode.code == code) {
                        return mode;
                    }
                }
                return null;
            }
        }

        public enum EraseMode {
            TOGGLE(DigitalPenConfig.DP_ERASE_MODE_TOGGLE),
            HOLD(DigitalPenConfig.DP_ERASE_MODE_HOLD);

            private final byte code;

            private EraseMode(byte code) {
                this.code = code;
            }

            private static EraseMode fromCode(byte code) {
                for (EraseMode mode : EraseMode.values()) {
                    if (code == mode.code) {
                        return mode;
                    }
                }
                return null;
            }
        }

        public enum PowerProfile {
            OPTIMIZE_ACCURACY(DigitalPenConfig.DP_POWER_PROFILE_OPTIMIZE_ACCURACY),
            OPTIMIZE_POWER(DigitalPenConfig.DP_POWER_PROFILE_OPTIMIZE_POWER);

            private final byte code;

            private PowerProfile(byte code) {
                this.code = code;
            }

            public static PowerProfile fromCode(byte code) {
                for (PowerProfile profile : PowerProfile.values()) {
                    if (code == profile.code) {
                        return profile;
                    }
                }
                return null;
            }

        }

        public enum Side {
            RIGHT(DigitalPenConfig.DP_PORTRAIT_SIDE_RIGHT),
            LEFT(DigitalPenConfig.DP_PORTRAIT_SIDE_LEFT);

            private byte code;

            private Side(byte code) {
                this.code = code;
            }

            private static Side fromCode(byte code) {
                for (Side side : Side.values()) {
                    if (side.code == code) {
                        return side;
                    }
                }
                return null;
            }

        }

        private final DigitalPenConfig config;

        // only constructable through factory method
        private DigitalPenGlobalSettings(DigitalPenConfig config) {
            this.config = config;
        }

        private DigitalPenConfig getConfig() {
            return config;
        }

        public DigitalPenGlobalSettings setDefaultOffScreenMode(OffScreenMode mode) {
            config.setOffScreenMode(mode.code);
            return this;
        }

        public OffScreenMode getOffScreenMode() {
            return OffScreenMode.fromCode(config.getOffScreenMode());
        }

        public DigitalPenGlobalSettings enableSmarterStand(boolean enable) {
            config.setSmarterStand(enable);
            return this;
        }

        public boolean isSmarterStandEnabled() {
            return config.isSmarterStandEnabled();
        }

        public int getInRangeDistance() {
            return config.getTouchRange();
        }

        public DigitalPenGlobalSettings setInRangeDistance(int distance) {
            config.setTouchRange(distance);
            return this;
        }

        public boolean isHoverEnabled() {
            return config.isOnScreenHoverEnabled();
        }

        public DigitalPenGlobalSettings enableHover(boolean enable) {
            config.setOnScreenHoverEnable(enable);
            return this;
        }

        public int getEraseButtonIndex() {
            return config.getEraseButtonIndex();
        }

        public EraseMode getEraseButtonMode() {
            return EraseMode.fromCode(config.getEraseButtonMode());
        }

        public DigitalPenGlobalSettings enableErase(int index, EraseMode mode) {
            config.setEraseButtonBehavior(mode.code);
            config.setEraseButtonIndex(index);
            return this;
        }

        public DigitalPenGlobalSettings disableErase() {
            config.setEraseButtonIndex(-1);
            return this;
        }

        public DigitalPenGlobalSettings setPowerProfile(PowerProfile profile) {
            config.setPowerSave(profile.code);
            return this;
        }

        public PowerProfile getPowerProfile() {
            return PowerProfile.fromCode(config.getPowerSave());
        }

        public Side getPortraitOffScreenSide() {
            return Side.fromCode(config.getOffSceenPortraitSide());
        }

        public DigitalPenGlobalSettings setPortraitOffScreenSide(Side side) {
            config.setOffScreenPortraitSide(side.code);
            return this;
        }

        public boolean isStopPenOnScreenOffEnabled() {
            return config.isStopPenOnScreenOffEnabled();
        }

        public DigitalPenGlobalSettings enableStopPenOnScreenOff(boolean enable) {
            config.setStopPenOnScreenOff(enable);
            return this;
        }

        public boolean isStartPenOnBootEnabled() {
            return config.isStartPenOnBootEnabled();
        }

        public DigitalPenGlobalSettings enableStartPenOnBoot(boolean enable) {
            config.setStartPenOnBoot(enable);
            return this;
        }
    }

    protected IDigitalPenService attachedService;

    // TODO: remove local copy of config once we can get it from service
    private final DigitalPenConfig config = new DigitalPenConfig();

    public DigitalPenGlobalSettings getCurrentSettings() {
        DigitalPenGlobalSettings settings = new DigitalPenGlobalSettings(config);
        return settings;
    }

    public void commitSettings(DigitalPenGlobalSettings settings) throws RemoteException {
        IDigitalPenService service = attachService();
        if (!service.setGlobalConfig(settings.getConfig())) {
            throw new RuntimeException("Couldn't set default config in service");
        }
    }

    public DigitalPenGlobalControl(Context context) {
    }

    protected IDigitalPenService attachService() {
        if (attachedService == null) {
            attachedService = IDigitalPenService.Stub.asInterface(ServiceManager.getService("DigitalPen"));
        }
        if (null == attachedService) {
            throw new RuntimeException("Could not connect to Digital Pen service");
        }
        return attachedService;
    }

    public void disablePenFeature() throws RemoteException {
        IDigitalPenService service = attachService();
        if (!service.isEnabled()) {
            return;
        }
        boolean success = service.disable();
        if (!success) {
            throw new RuntimeException("Problem disabling pen feature");
        }
    }

    public void enablePenFeature() throws RemoteException {
        IDigitalPenService service = attachService();
        if (service.isEnabled()) {
            return;
        }
        boolean success = service.enable();
        if (!success) {
            throw new RuntimeException("Problem enabling pen feature");
        }
    }

    public boolean isPenFeatureEnabled() throws RemoteException {
        IDigitalPenService service = attachService();
        return service.isEnabled();
    }

}
