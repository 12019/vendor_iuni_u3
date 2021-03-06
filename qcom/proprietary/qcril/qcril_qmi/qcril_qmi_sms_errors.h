//=============================================================================
// FILE: qcril_qmi_sms_errors.h
//
// SERVICES: RIL
//
// DESCRIPTION: defines sms error cause codes
//
// Copyright (c) 2012 Qualcomm Technologies, Inc. All Rights Reserved.
// Qualcomm Technologies Proprietary and Confidential.
//=============================================================================

//=============================================================================
// DATA TYPES
//=============================================================================

//=============================================================================
// CONSTANTS
//=============================================================================

// START gw_cause_code_str
// WMS Cause codes from wireless_messaging_service_v01.h
#define WMS_RP_CAUSE_UNASSIGNED_NUMBER_V01   0x01
#define WMS_RP_CAUSE_OPERATOR_DETERMINED_BARRING_V01   0x08
#define WMS_RP_CAUSE_CALL_BARRED_V01   0x0A
#define WMS_RP_CAUSE_RESERVED_V01   0x0B
#define WMS_RP_CAUSE_SMS_TRANSFER_REJECTED_V01   0x15
#define WMS_RP_CAUSE_MEMORY_CAP_EXCEEDED_V01   0x16
#define WMS_RP_CAUSE_DESTINATION_OUT_OF_ORDER_V01   0x1B
#define WMS_RP_CAUSE_UNIDENTIFIED_SUBSCRIBER_V01   0x1C
#define WMS_RP_CAUSE_FACILITY_REJECTED_V01   0x1D
#define WMS_RP_CAUSE_UNKNOWN_SUBSCRIBER_V01   0x1E
#define WMS_RP_CAUSE_NETWORK_OUT_OF_ORDER_V01   0x26
#define WMS_RP_CAUSE_TEMPORARY_FAILURE_V01   0x29
#define WMS_RP_CAUSE_CONGESTION_V01   0x2A
#define WMS_RP_CAUSE_RESOURCES_UNAVAILABLE_V01   0x2F
#define WMS_RP_CAUSE_REQUESTED_FACILITY_NOT_SUBSCRIBED_V01   0x32
#define WMS_RP_CAUSE_REQUESTED_FACILITY_NOT_IMPLEMENTED_V01   0x45
#define WMS_RP_CAUSE_INVALID_SMS_TRANSFER_REFERENCE_VALUE_V01   0x51
#define WMS_RP_CAUSE_SEMANTICALLY_INCORRECT_MESSAGE_V01   0x5F
#define WMS_RP_CAUSE_INVALID_MANDATORY_INFO_V01   0x60
#define WMS_RP_CAUSE_MESSAGE_TYPE_NOT_IMPLEMENTED_V01   0x61
#define WMS_RP_CAUSE_MESSAGE_NOT_COMPATABLE_WITH_SMS_V01   0x62
#define WMS_RP_CAUSE_INFO_ELEMENT_NOT_IMPLEMENTED_V01   0x63
#define WMS_RP_CAUSE_PROTOCOL_ERROR_V01   0x6F
#define WMS_RP_CAUSE_INTERWORKING_V01   0x7F

#define WMS_TP_CAUSE_TELE_INTERWORKING_NOT_SUPPORTED_V01   0x80
#define WMS_TP_CAUSE_SHORT_MESSAGE_TYPE_0_NOT_SUPPORTED_V01   0x81
#define WMS_TP_CAUSE_SHORT_MESSAGE_CANNOT_BE_REPLACED_V01   0x82
#define WMS_TP_CAUSE_UNSPECIFIED_PID_ERROR_V01   0x8F
#define WMS_TP_CAUSE_DCS_NOT_SUPPORTED_V01   0x90
#define WMS_TP_CAUSE_MESSAGE_CLASS_NOT_SUPPORTED_V01   0x91
#define WMS_TP_CAUSE_UNSPECIFIED_DCS_ERROR_V01   0x9F
#define WMS_TP_CAUSE_COMMAND_CANNOT_BE_ACTIONED_V01   0xA0
#define WMS_TP_CAUSE_COMMAND_UNSUPPORTED_V01   0xA1
#define WMS_TP_CAUSE_UNSPECIFIED_COMMAND_ERROR_V01   0xAF
#define WMS_TP_CAUSE_TPDU_NOT_SUPPORTED_V01   0xB0
#define WMS_TP_CAUSE_SC_BUSY_V01   0xC0
#define WMS_TP_CAUSE_NO_SC_SUBSCRIPTION_V01   0xC1
#define WMS_TP_CAUSE_SC_SYS_FAILURE_V01   0xC2
#define WMS_TP_CAUSE_INVALID_SME_ADDRESS_V01   0xC3
#define WMS_TP_CAUSE_DESTINATION_SME_BARRED_V01   0xC4
#define WMS_TP_CAUSE_SM_REJECTED_OR_DUPLICATE_V01   0xC5
#define WMS_TP_CAUSE_TP_VPF_NOT_SUPPORTED_V01   0xC6
#define WMS_TP_CAUSE_TP_VP_NOT_SUPPORTED_V01   0xC7
#define WMS_TP_CAUSE_SIM_SMS_STORAGE_FULL_V01   0xD0
#define WMS_TP_CAUSE_NO_SMS_STORAGE_CAP_IN_SIM_V01   0xD1
#define WMS_TP_CAUSE_MS_ERROR_V01   0xD2
#define WMS_TP_CAUSE_MEMORY_CAP_EXCEEDED_V01   0xD3
#define WMS_TP_CAUSE_SIM_APP_TOOLKIT_BUSY_V01   0xD4
#define WMS_TP_CAUSE_SIM_DATA_DOWNLOAD_ERROR_V01   0xD5
#define WMS_TP_CAUSE_UNSPECIFIED_ERROR_V01   0xFF
// END gw_cause_code_str

// START cause_code_str
#define WMS_TL_CAUSE_CODE_ADDR_VACANT_V01   0x00
#define WMS_TL_CAUSE_CODE_ADDR_TRANSLATION_FAILURE_V01   0x01
#define WMS_TL_CAUSE_CODE_NETWORK_RESOURCE_SHORTAGE_V01   0x02
#define WMS_TL_CAUSE_CODE_NETWORK_FAILURE_V01   0x03
#define WMS_TL_CAUSE_CODE_INVALID_TELESERVICE_ID_V01   0x04
#define WMS_TL_CAUSE_CODE_NETWORK_OTHER_V01   0x05
#define WMS_TL_CAUSE_CODE_NO_PAGE_RESPONSE_V01   0x20
#define WMS_TL_CAUSE_CODE_DEST_BUSY_V01   0x21
#define WMS_TL_CAUSE_CODE_NO_ACK_V01   0x22
#define WMS_TL_CAUSE_CODE_DEST_RESOURCE_SHORTAGE_V01   0x23
#define WMS_TL_CAUSE_CODE_SMS_DELIVERY_POSTPONED_V01   0x24
#define WMS_TL_CAUSE_CODE_DEST_OUT_OF_SERV_V01   0x25
#define WMS_TL_CAUSE_CODE_DEST_NOT_AT_ADDR_V01   0x26
#define WMS_TL_CAUSE_CODE_DEST_OTHER_V01   0x27
#define WMS_TL_CAUSE_CODE_RADIO_IF_RESOURCE_SHORTAGE_V01   0x40
#define WMS_TL_CAUSE_CODE_RADIO_IF_INCOMPATABILITY_V01   0x41
#define WMS_TL_CAUSE_CODE_RADIO_IF_OTHER_V01   0x42
#define WMS_TL_CAUSE_CODE_ENCODING_V01   0x60
#define WMS_TL_CAUSE_CODE_SMS_ORIG_DENIED_V01   0x61
#define WMS_TL_CAUSE_CODE_SMS_TERM_DENIED_V01   0x62
#define WMS_TL_CAUSE_CODE_SUPP_SERV_NOT_SUPP_V01   0x63
#define WMS_TL_CAUSE_CODE_SMS_NOT_SUPP_V01   0x64
#define WMS_TL_CAUSE_CODE_MISSING_EXPECTED_PARAM_V01   0x65
#define WMS_TL_CAUSE_CODE_MISSING_MAND_PARAM_V01   0x66
#define WMS_TL_CAUSE_CODE_UNRECOGNIZED_PARAM_VAL_V01   0x67
#define WMS_TL_CAUSE_CODE_UNEXPECTED_PARAM_VAL_V01   0x68
#define WMS_TL_CAUSE_CODE_USER_DATA_SIZE_ERR_V01   0x69
#define WMS_TL_CAUSE_CODE_GENERAL_OTHER_V01   0x6A
// END cause_code_str
// STOP

//=============================================================================
// FUNCTIONS
//=============================================================================

/// returns string representation of sms cause code for debug
const char* cause_code_str(int cause_code);

/// returns string representation of sms gw cause code for debug
const char* gw_cause_code_str(int error_code);

