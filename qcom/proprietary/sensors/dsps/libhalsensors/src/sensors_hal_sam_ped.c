/*============================================================================
  @file sensors_hal_sam_ped.c

  @brief
  File handles all requests to the SAM Rotation Vector Algorithm.

  Copyright (c) 2013 Qualcomm Technologies, Inc.  All Rights Reserved.
  Qualcomm Technologies Proprietary and Confidential.
============================================================================*/

/*===========================================================================
                           INCLUDE FILES
===========================================================================*/
#include <stdlib.h>
#include <inttypes.h>
#include <errno.h>
#include "sensors_hal.h"
#include "sns_sam_ped_v01.h"
#include "sensor1.h"
#include "fixed_point.h"

/*===========================================================================
                           PREPROCESSOR DEFINITIONS
===========================================================================*/

#define SVC_NUM SNS_SAM_PED_SVC_ID_V01

/*===========================================================================
                         STATIC VARIABLES
===========================================================================*/

/*==========================================================================
                         STATIC FUNCTION DEFINITIONS
===========================================================================*/

/*===========================================================================
  FUNCTION:  hal_sam_ped_send_enable
===========================================================================*/
static int
hal_sam_ped_send_enable( hal_sensor_control_t* sensor_ctl, uint32_t report_rate )
{
  sensor1_error_e error;
  sensor1_msg_header_s req_hdr;
  sns_sam_ped_enable_req_msg_v01 *sam_req;

  error = sensor1_alloc_msg_buf( sensor_ctl->hndl,
                                 sizeof(sns_sam_ped_enable_req_msg_v01),
                                 (void**)&sam_req );
  if( SENSOR1_SUCCESS != error )
  {
    HAL_LOG_ERROR( "%s: sensor1_alloc_msg_buf() error: %d", __FUNCTION__, error );
    return -1;
  }

  req_hdr.service_number = SVC_NUM;
  req_hdr.msg_id = SNS_SAM_PED_ENABLE_REQ_V01;
  req_hdr.msg_size = sizeof( sns_sam_ped_enable_req_msg_v01 );
  req_hdr.txn_id = 0;

  // Current config is for step detector;
  // it will be updated for proper step counter support in seperate gerrit
  sam_req->report_period = 0;


  /* set default behavior for indications during suspend */
  sam_req->notify_suspend_valid = true;
  sam_req->notify_suspend.proc_type = SNS_PROC_APPS_V01;
  sam_req->notify_suspend.send_indications_during_suspend = false;

  /* Send request */
  sensor_ctl->error = false;
  if( (error = sensor1_write( sensor_ctl->hndl, &req_hdr,
                              sam_req )) != SENSOR1_SUCCESS )
  {
    /* free the message buffer */
    sensor1_free_msg_buf( sensor_ctl->hndl, sam_req );
    HAL_LOG_ERROR( "%s: sensor1_write() error: %d", __FUNCTION__, error );
    return -1;
  }

  /* waiting for response */
  if (hal_wait_for_response( TIME_OUT_MS,
                             &sensor_ctl->cb_mutex,
                             &sensor_ctl->cb_arrived_cond,
                             &sensor_ctl->is_resp_arrived ) == false )
  {
    HAL_LOG_ERROR( "%s: ERROR: No response from ROTATION VECTOR enable request",
                   __FUNCTION__ );
    return -1;
  }

  HAL_LOG_DEBUG( "%s: Received Response: %d", __FUNCTION__, sensor_ctl->error );

  return sensor_ctl->error ? -1 : 0;
}

/*===========================================================================
  FUNCTION:  hal_sam_ped_send_batch
===========================================================================*/
static int
hal_sam_ped_send_batch( hal_sensor_control_t* sensor_ctl,
    bool batching, uint32_t batch_rate, bool wake_upon_fifo_full )
{
  sensor1_error_e  error;
  sensor1_msg_header_s req_hdr;
  sns_sam_ped_batch_req_msg_v01 *sam_req;
  float batch_rate_in_hz;

  HAL_LOG_DEBUG( "%s: batching: %d, batch_rate=%f (Hz), WUFF=%d, IID: %d",
                 __FUNCTION__, batching, FX_FIXTOFLT_Q16( batch_rate ),
                 wake_upon_fifo_full, sensor_ctl->sam_service[ SVC_NUM ].instance_id );

  error = sensor1_alloc_msg_buf( sensor_ctl->hndl,
                                 sizeof(sns_sam_ped_batch_req_msg_v01),
                                 (void**)&sam_req );
  if( SENSOR1_SUCCESS != error )
  {
    HAL_LOG_ERROR( "%s: sensor1_alloc_msg_buf() error: %d", __FUNCTION__, error );
    return -1;
  }
  req_hdr.service_number = SVC_NUM;
  req_hdr.msg_id = SNS_SAM_PED_BATCH_REQ_V01;
  req_hdr.msg_size = sizeof( sns_sam_ped_batch_req_msg_v01 );
  req_hdr.txn_id = TXN_ID_NO_RESP_SIGNALLED;

  sam_req->instance_id = sensor_ctl->sam_service[ SVC_NUM ].instance_id;

  sam_req->req_type_valid = true;
  sam_req->req_type = wake_upon_fifo_full;

  // convert batch rate from Hz in Q16 to batch period in seconds in Q16
  batch_rate_in_hz = FX_FIXTOFLT_Q16( batch_rate );
  sam_req->batch_period = batching ?
    FX_FLTTOFIX_Q16( 1.0 / batch_rate_in_hz ) :
    0 ;  // Rate of 0 means disabled

  /* Send request */
  sensor_ctl->error = false;
  if( (error = sensor1_write( sensor_ctl->hndl, &req_hdr,
                              sam_req )) != SENSOR1_SUCCESS )
  {
    /* free the message buffer */
    sensor1_free_msg_buf( sensor_ctl->hndl, sam_req );
    HAL_LOG_ERROR( "%s: sensor1_write() error: %d", __FUNCTION__, error );
    return -1;
  }

  return sensor_ctl->error ? -1 : 0;
}

/*===========================================================================
  FUNCTION:  hal_sam_ped_get_report
===========================================================================*/
static int
hal_sam_ped_get_report( hal_sensor_control_t* sensor_ctl )
{
  sensor1_error_e error;
  sensor1_msg_header_s req_hdr;
  sns_sam_ped_get_report_req_msg_v01 *sam_req;

  HAL_LOG_DEBUG( "%s", __FUNCTION__ );

  error = sensor1_alloc_msg_buf( sensor_ctl->hndl,
                                 sizeof(sns_sam_ped_get_report_req_msg_v01),
                                 (void**)&sam_req );
  if( SENSOR1_SUCCESS != error )
  {
    HAL_LOG_ERROR( "%s: sensor1_alloc_msg_buf() error: %d", __FUNCTION__, error );
    return -1;
  }
  req_hdr.service_number = SVC_NUM;
  req_hdr.msg_id = SNS_SAM_PED_GET_REPORT_REQ_V01;
  req_hdr.msg_size = sizeof( sns_sam_ped_get_report_req_msg_v01 );
  req_hdr.txn_id = TXN_ID_NO_RESP_SIGNALLED;

  sam_req->instance_id = sensor_ctl->sam_service[ SVC_NUM ].instance_id;

  /* Send request */
  sensor_ctl->error = false;
  if( (error = sensor1_write( sensor_ctl->hndl, &req_hdr,
                              sam_req )) != SENSOR1_SUCCESS )
  {
    /* free the message buffer */
    sensor1_free_msg_buf( sensor_ctl->hndl, sam_req );
    HAL_LOG_ERROR( "%s: sensor1_write() error: %d", __FUNCTION__, error );
    return -1;
  }

  return sensor_ctl->error ? -1 : 0;
}

/*===========================================================================
  FUNCTION:  hal_sam_ped_send_batch_update
===========================================================================*/
static int
hal_sam_ped_send_batch_update( hal_sensor_control_t* sensor_ctl, uint32_t batch_rate )
{
  sensor1_error_e error;
  sensor1_msg_header_s req_hdr;
  sns_sam_ped_update_batch_period_req_msg_v01 *sam_req;
  float batch_rate_in_hz;
  bool wait_for_resp = false;

  HAL_LOG_DEBUG( "%s: batch_rate=%f (Hz)", __FUNCTION__, FX_FIXTOFLT_Q16( batch_rate ) );

  /* Message Body */
  error = sensor1_alloc_msg_buf( sensor_ctl->hndl,
                                 sizeof(sns_sam_ped_update_batch_period_req_msg_v01),
                                 (void**)&sam_req );

  if( SENSOR1_SUCCESS != error )
  {
    HAL_LOG_ERROR( "%s: sensor1_alloc_msg_buf() error: %d", __FUNCTION__, error );
    return -1;
  }

  req_hdr.service_number = SVC_NUM;
  req_hdr.msg_id = SNS_SAM_PED_UPDATE_BATCH_PERIOD_REQ_V01;
  req_hdr.msg_size = sizeof( sns_sam_ped_update_batch_period_req_msg_v01 );
  req_hdr.txn_id = TXN_ID_NO_RESP_SIGNALLED;

  sam_req->instance_id = sensor_ctl->sam_service[ SVC_NUM ].instance_id;

  // convert batch rate from Hz in Q16 to batch period in seconds in Q16
  batch_rate_in_hz = FX_FIXTOFLT_Q16( batch_rate );
  sam_req->active_batch_period = FX_FLTTOFIX_Q16( 1.0 / batch_rate_in_hz );

  /* Send request */
  sensor_ctl->error = false;
  if( (error = sensor1_write( sensor_ctl->hndl, &req_hdr,
                              sam_req )) != SENSOR1_SUCCESS )
  {
    /* free the message buffer */
    sensor1_free_msg_buf( sensor_ctl->hndl, sam_req );
    HAL_LOG_ERROR( "%s: sensor1_write() error: %d", __FUNCTION__, error );
    return -1;
  }

  return sensor_ctl->error ? -1 : 0;
}


/*==========================================================================
                         PUBLIC FUNCTION DEFINITIONS
===========================================================================*/

/*===========================================================================
  FUNCTION:  hal_sam_ped_parse_ind
===========================================================================*/
hal_sam_sample_t*
hal_sam_ped_parse_ind( sensor1_msg_header_s *msg_hdr, void *msg_ptr, int *count )
{
  hal_sam_sample_t *sample_list = NULL;
  uint32_t i = 0;
  *count = 0;

  if( SNS_SAM_PED_REPORT_IND_V01 == msg_hdr->msg_id )
  {
    sns_sam_ped_report_ind_msg_v01* sam_ind =
      (sns_sam_ped_report_ind_msg_v01*)msg_ptr;

    sample_list = malloc( sizeof(hal_sam_sample_t) );
    if( NULL == sample_list )
    {
      HAL_LOG_ERROR( "%s: Malloc error", __FUNCTION__ );
    }
    else
    {
      *count = 1;

      sample_list->data[0] = sam_ind->report_data.step_count;
      sample_list->data[1] = sam_ind->report_data.step_rate;
      sample_list->data[2] = sam_ind->report_data.step_confidence;
      sample_list->data[3] = sam_ind->report_data.step_event;
      sample_list->data[4] = sam_ind->report_data.step_count_error;

      sample_list->accuracy = 0;
      sample_list->timestamp = sam_ind->timestamp;
    }
  }
  else if( SNS_SAM_PED_GET_REPORT_RESP_V01 == msg_hdr->msg_id )
  {
    sns_sam_ped_get_report_resp_msg_v01* sam_ind =
      (sns_sam_ped_get_report_resp_msg_v01*)msg_ptr;

    if( sam_ind->timestamp_valid && sam_ind->report_data_valid )
    {
      sample_list = malloc( sizeof(hal_sam_sample_t) );
      if( NULL == sample_list )
      {
        HAL_LOG_ERROR( "%s: Malloc error", __FUNCTION__ );
      }
      else
      {
        *count = 1;

        sample_list->data[0] = sam_ind->report_data.step_count;
        sample_list->data[1] = sam_ind->report_data.step_rate;
        sample_list->data[2] = sam_ind->report_data.step_confidence;
        sample_list->data[3] = 0; // We don't want to generate extra step detector events
        sample_list->data[4] = sam_ind->report_data.step_count_error;

        sample_list->accuracy = 0;
        sample_list->timestamp = sam_ind->timestamp;
      }
    }
    else
    {
      HAL_LOG_WARN( "%s: Received report with invalid data", __FUNCTION__ );
    }
  }
  else if( SNS_SAM_PED_BATCH_IND_V01 == msg_hdr->msg_id )
  {
    sns_sam_ped_batch_ind_msg_v01*  sam_ind =
      (sns_sam_ped_batch_ind_msg_v01*)msg_ptr;

    sample_list = malloc( sam_ind->items_len * sizeof(hal_sam_sample_t) );
    if( NULL == sample_list )
    {
      HAL_LOG_ERROR( "%s: Malloc error", __FUNCTION__ );
    }
    else
    {
      hal_sam_sample_t *curr_sample = sample_list;
      *count = sam_ind->items_len;

      for( i = 0; i < sam_ind->items_len; i++ )
      {
        curr_sample->data[0] = sam_ind->items[ i ].report.step_count;
        curr_sample->data[1] = sam_ind->items[ i ].report.step_rate;
        curr_sample->data[2] = sam_ind->items[ i ].report.step_confidence;
        curr_sample->data[3] = sam_ind->items[ i ].report.step_event;
        curr_sample->data[4] = sam_ind->items[ i ].report.step_count_error;

        curr_sample->accuracy = 0;
        curr_sample->timestamp = sam_ind->items[ i ].timestamp;

        curr_sample++;
      }
    }
  }
  else
  {
    HAL_LOG_ERROR( "%s: Unknown message ID = %d", __FUNCTION__, msg_hdr->msg_id );
  }

  return sample_list;
}

/*===========================================================================
  FUNCTION:  hal_sam_ped_init
===========================================================================*/
void hal_sam_ped_init( hal_sam_sensor_t *sam_sensor )
{
  sam_sensor->curr_report_rate = 0;
}

/*===========================================================================
  FUNCTION:  hal_sam_ped_info
===========================================================================*/
int
hal_sam_ped_info( hal_sam_sensor_t *sam_sensor )
{
  sam_sensor->enable_func = &hal_sam_ped_send_enable;
  sam_sensor->get_report_func = &hal_sam_ped_get_report;
  sam_sensor->batch_func = &hal_sam_ped_send_batch;
  sam_sensor->batch_update_func = &hal_sam_ped_send_batch_update;
  sam_sensor->parse_ind_func = &hal_sam_ped_parse_ind;
  sam_sensor->init_func = &hal_sam_ped_init;
  sam_sensor->svc_num = SVC_NUM;
  strlcpy( sam_sensor->algo_name, "ped", HAL_SAM_NAME_MAX_LEN - 1 );

  return 0;
}
