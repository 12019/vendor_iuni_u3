#ifndef DS_NET_QOS_DEFAULT_JSON_H
#define DS_NET_QOS_DEFAULT_JSON_H
/*===========================================================================
@file ds_Net_QoSDefaultJson.h

This file defines the class of QoS Default implimentation using JSon

Copyright (c) 2010 Qualcomm Technologies, Inc.
All Rights Reserved.
Qualcomm Technologies Confidential and Proprietary
===========================================================================*/
/*===========================================================================
  EDIT HISTORY FOR MODULE

  Please notice that the changes are listed in reverse chronological order.

  $Header: //source/qcom/qct/modem/datamodem/interface/dsnet/rel/11.03/src/ds_Net_QoSDefaultJson.h#1 $
  $DateTime: 2011/06/17 12:02:33 $$Author: zhasan $

  when       who what, where, why
  ---------- --- ------------------------------------------------------------
  2010-08-11 en  Created module.

===========================================================================*/
/*===========================================================================

INCLUDE FILES FOR MODULE

===========================================================================*/

#include "comdef.h"
#include "AEEStdErr.h"
#include "ds_Errors_Def.h"
#include "ds_Net_QoS_Def.h"
#include "ds_Net_Utils.h"
#include "ds_Net_IQoSSecondariesOutput.h"
#include "ds_Utils_CSSupport.h"
#include "ds_Net_MemManager.h"

/*===========================================================================
FORWARD DECLERATION
===========================================================================*/
struct IPrivSet;

/*===========================================================================

  PUBLIC DATA DECLARATIONS 

===========================================================================*/
namespace ds
{
  namespace Net
  {
    class QoSDefaultJson
    {
      private:
        int32      mHandle;
        IPrivSet*  mpPrivSet;
      public:
        QoSDefaultJson(int32 ifaceHandle, IPrivSet * pPrivSet);
        /*! 
        @brief
        Destructor of QoSManager object. 
        */
        virtual void Destructor
        (
          void 
        )
        throw();

        /*!
        @brief
        Dummy destructor. Only used to free memory. 

        @details
        For objects that support weak references, destruction of the object and
        freeing up of memory can happen at different times. Hence Destructor()
        is the one which performs actual destruction of the object, where as 
        ~Object() would perform memory cleanup. 
        @params     None.
        @return     None.
        */
        virtual ~QoSDefaultJson() throw();

        virtual ds::ErrorType CDECL Modify 
        (
          const char* requestedSpec, 
          QoSModifyMaskType mask, 
          char* errSpec, 
          int errSpecLen, 
          int* errSpecLenReq
        );
       
        /*-------------------------------------------------------------------------
          Overload new/delete operators.
          -------------------------------------------------------------------------*/

        DSNET_OVERLOAD_OPERATORS(PS_MEM_DS_NET_QOS_DEFAULT_JSON);

        /*-------------------------------------------------------------------------
          Macro to implement IWeakRef/IQI methods
        -------------------------------------------------------------------------*/
        DS_UTILS_IWEAKREF_IMPL_DEFAULTS_NO_QI()
    };
  };
};

#endif //DS_NET_QOS_DEFAULT_JSON_H


