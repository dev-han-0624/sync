package com.rnd.sync.integration

import com.rnd.sync.application.domain.order.Order
import com.rnd.sync.application.domain.order.state.OrderCancelledState
import com.rnd.sync.application.domain.order.state.OrderCreatedState
import com.rnd.sync.application.service.order.`in`.UpdateOrderStatusCase
import com.rnd.sync.application.service.order.`in`.UpdateOrderStatusCase.StateUpdateRequest
import com.rnd.sync.application.service.order.out.OrderRepository
import com.rnd.sync.infra.web.SyncApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootTest(classes = [SyncApplication::class])
@EnableAutoConfiguration
@EntityScan(basePackages = ["com.rnd.sync"])
@EnableJpaRepositories(basePackages = ["com.rnd.sync"])
@ComponentScan(basePackages = ["com.rnd.sync"])
class UpdateOrderStatusCase {
    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var updateOrderStatusCase: UpdateOrderStatusCase

    private lateinit var savedOrder: Order

    @Test
    fun contextLoads() {
    }

    @BeforeEach
    fun setup() {
        savedOrder = saveOrder()
    }

    @Test
    fun `주문을 취소한다`() {
        val request = StateUpdateRequest(
            orderId = savedOrder.id.id,
            status = OrderCancelledState().name()
        )

        val updatedOrder = updateOrderStatusCase.updateState(request)

        assertEquals(savedOrder.id.id, updatedOrder.id.id)
        assertEquals(OrderCancelledState().name(), updatedOrder.orderStatus.name())
    }

    @Test
    fun `취소된 주문을 다시 생성상태로 바꿀 수 없다`() {
        val cancelRequest = StateUpdateRequest(
            orderId = savedOrder.id.id,
            status = OrderCancelledState().name()
        )

        val cancelledOrder = updateOrderStatusCase.updateState(cancelRequest)
        assertEquals(OrderCancelledState().name(), cancelledOrder.orderStatus.name())

        val createRequest = StateUpdateRequest(
            orderId = savedOrder.id.id,
            status = OrderCreatedState().name()
        )

        val ex = assertThrows<IllegalStateException> {
            updateOrderStatusCase.updateState(createRequest)
        }

        assertEquals("취소 상태에서 다시 생성될 수 없습니다.", ex.message)
    }

    private fun saveOrder(): Order {
        val order = Order.createNew(
            productName = "마우스",
            receiverName = "위밋",
            receiverContact = "010-1234-5678",
            receiverAddress = "조원로"
        )

        return orderRepository.save(order)
    }
}