package com.rnd.sync.application.domain.deliveryplan

import com.rnd.sync.application.domain.delivery.DeliveryComposite
import com.rnd.sync.application.domain.deliveryplan.state.DeliveryPlanCancelledState
import com.rnd.sync.application.domain.deliveryplan.state.DeliveryPlanCreatedState
import com.rnd.sync.application.domain.deliveryplan.state.DeliveryPlanState
import java.time.LocalDate

class DeliveryPlan(
    private val deliveryPlanId: DeliveryPlanId? = null,
    val workingDate: LocalDate,
    status: DeliveryPlanState
) {
    val id: DeliveryPlanId
        get() = deliveryPlanId ?: throw IllegalStateException("id should not be null")

    var status = status
        private set

    private val mutableDeliveries = mutableListOf<DeliveryComposite>()
    val deliveries: List<DeliveryComposite>
        get() = mutableDeliveries.toList()

    fun mapDelivery(deliveryComposite: DeliveryComposite) {
        this.mutableDeliveries.add(deliveryComposite)
    }

    companion object {
        fun createNewDeliveryPlan(
            workingDate: LocalDate,
        ): DeliveryPlan {
            return DeliveryPlan(
                workingDate = workingDate,
                status = DeliveryPlanCreatedState()
            )
        }

        fun createDeliveryPlan(
            id: Long,
            workingDate: LocalDate,
            status: String
        ): DeliveryPlan {
            val allStatus = listOf(DeliveryPlanCreatedState(), DeliveryPlanCancelledState())
            val selectedStatus = allStatus.find { it.name().equals(status, true) }
                ?: throw IllegalArgumentException("DeliveryPlan status $status not found")

            return DeliveryPlan(
                deliveryPlanId = DeliveryPlanId(id),
                workingDate = workingDate,
                status = selectedStatus
            )
        }
    }

    data class DeliveryPlanId(val id: Long)
}