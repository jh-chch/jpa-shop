package com.jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jpabook.jpashop.domain.Address;
import com.jpabook.jpashop.domain.Order;
import com.jpabook.jpashop.domain.OrderItem;
import com.jpabook.jpashop.domain.OrderStatus;
import com.jpabook.jpashop.repository.OrderRepository;
import com.jpabook.jpashop.repository.OrderSearch;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
        return collect;
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
            this.orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto {
        private String orderName;
        private int price;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            this.orderName = orderItem.getItem().getName();
            this.price = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
        return collect;
    }

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {

        /**
         * InitDb 기준
         * default_batch_fetch_size 사용 전에는 총 7번의 쿼리가 실행됨.
         * (xToOne join 쿼리 1개, orderItem 쿼리 2개, 각 orderItem의 item 쿼리 2개씩 총 4개)
         * 
         * jpa.properties.default_batch_fetch_size 옵션을 사용하면
         * orderItem과 item을 in 쿼리로 한번에 가져온다. 총 3개의 쿼리가 나간다.
         * (xToOne join 쿼리 1개, orderItem in 쿼리 1개, item in쿼리 1개)
         * 
         * #default_batch_fetch_size가 10인데 100개의 데이터면 in쿼리가 10번 나간다.
         * #default_batch_fetch_size로 글로벌 설정을 하거나 Entity 개별로 @Batch 에노테이션으로 설정 가능하다.
         * v3의 (컬렉션 페이징 패치 조인) 페이징 문제를 해결할 수 있다.
         */
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); // xToOne fetch join 여서 페이징 처리 가능
        List<OrderDto> collect = orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
        return collect;
    }
}
