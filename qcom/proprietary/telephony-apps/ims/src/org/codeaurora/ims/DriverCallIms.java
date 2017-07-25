/*
 * Copyright (c) 2012 Qualcomm Technologies, Inc.
 * All Rights Reserved. Qualcomm Technologies Confidential and Proprietary.
 *
 * Not a Contribution, Apache license notifications and license are retained
 * for attribution purposes only.
 *
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (c) 2012 Code Aurora Forum. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codeaurora.ims;

import com.android.internal.telephony.DriverCall;
import com.android.internal.telephony.CallDetails;

/**
 * {@hide}
 */
public class DriverCallIms extends DriverCall {
    static final String LOG_TAG = "DRIVERCALL-IMS";
    public CallDetails callDetails;

    public String toString() {
        return "id=" + index + "," + state + "," + "toa=" + TOA + ","
                + (isMpty ? "conf" : "norm") + "," + (isMT ? "mt" : "mo") + ","
                + als + "," + (isVoice ? "voc" : "nonvoc") + ","
                + (isVoicePrivacy ? "evp" : "noevp") + ","
                /* + "number=" + number */+ ",cli=" + numberPresentation + ","
                /* + "name="+ name */+ "," + namePresentation
                + "Call Details =" + callDetails;
    }
}
