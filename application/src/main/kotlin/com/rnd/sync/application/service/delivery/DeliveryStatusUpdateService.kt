package com.rnd.sync.application.service.delivery

import com.rnd.sync.application.domain.delivery.Delivery.DeliveryId
import com.rnd.sync.application.domain.delivery.state.DeliveryCancelledState
import com.rnd.sync.application.domain.delivery.state.DeliveryCompletedState
import com.rnd.sync.application.domain.delivery.state.DeliveryDelayedState
import com.rnd.sync.application.domain.delivery.state.DeliveryStartedState
import com.rnd.sync.application.domain.deliveryplan.DeliveryPlan
import com.rnd.sync.application.service.delivery.`in`.UpdateDeliveryStatusCase
import com.rnd.sync.application.service.delivery.`in`.UpdateDeliveryStatusCase.DeliveryStateUpdateRequest
import com.rnd.sync.application.service.deliveryplan.out.DeliveryPlanCommandRepository
import com.rnd.sync.application.service.deliveryplan.out.DeliveryPlanRepository
import org.springframework.stereotype.Service

@Service
class DeliveryStatusUpdateService(
    private val deliveryPlanRepository: DeliveryPlanRepository,
    private val deliveryPlanCommandRepository: DeliveryPlanCommandRepository
): UpdateDeliveryStatusCase {

    override fun updateState(request: DeliveryStateUpdateRequest) {
        val deliveryId = DeliveryId(request.deliveryId)
        val deliveryPlan = deliveryPlanRepository.getByDeliveryId(deliveryId)

        changeStatus(
            status = request.status,
            deliveryId = deliveryId,
            deliveryPlan = deliveryPlan,
        )

        deliveryPlanCommandRepository.update(deliveryPlan)
    }

    private fun changeStatus(
        status: String,
        deliveryId: DeliveryId,
        deliveryPlan: DeliveryPlan,
    ) {
        when (status) {
            DeliveryStartedState().name() -> deliveryPlan.startDelivery(deliveryId)
            DeliveryDelayedState().name() -> deliveryPlan.delayDelivery(deliveryId)
            DeliveryCompletedState().name() -> deliveryPlan.completeDelivery(deliveryId)
            DeliveryCancelledState().name() -> deliveryPlan.cancelDelivery(deliveryId)
            else -> throw IllegalStateException("invalid status: >> $status")
        }
    }
}