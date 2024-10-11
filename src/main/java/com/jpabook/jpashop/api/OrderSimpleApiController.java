package com.jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jpabook.jpashop.domain.Address;
import com.jpabook.jpashop.domain.Order;
import com.jpabook.jpashop.domain.OrderStatus;
import com.jpabook.jpashop.repository.OrderRepository;
import com.jpabook.jpashop.repository.OrderSearch;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        /**
         * all을 그대로 리턴하면
         * 
         * 1. Order <-> Member, Delivery, OrderItem 사이에서
         * json으로 변환할 때 무한 참조가 발생한다. 따라서 참조하는 한쪽에 @JsonIgnore로 막아야 한다.
         * 
         * 2. Order 입장에서 member필드와 delivery는 LAZY 전략이므로
         * 프록시 객체가 들어가 있다. (ByteBuddyInterceptor)
         * 마찬가지로 json으로 변환할 때 프록시 객체를 json으로 변환에 실패한다.
         * (jackson-dataType-Hibernate 등을 이용해 해결할 수 있지만 애초에 엔티티를 외부에 노출시키는 상황을 만들지 않는 것이
         * 좋다.)
         * 
         */
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        // 편의상 List 타입으로 반환 [{...},{...}] 형식은 원래 좋지 않다.
        // 새로운 타입이 추가될 때 변경이 힘들다. { "xx": [..] } 이런 형식으로 사용하자.

        /**
         * order의 결과가 N번이면 최악의 경우 1 + N + N 번 실행된다. -> fetch join 을 사용해 해결한다.
         * 예를 들어 all(order)의 결과가 2개면 (order select 1)
         * 1번 루프 -> Member LAZY + Delivery LAZY select 2
         * 2번 루프 -> Member LAZY + Delivery LAZY select 2
         * 쿼리가 총 1+2+2 = 5번 나간다. 물론 같은 Member나 Delivery라면 2번 루프를 돌때 영속성 컨텍스트에서 조회를 하기때문에
         * 쿼리가 최악의 경우보다 덜 나간다.
         */
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> collect = all.stream().map(SimpleOrderDto::new).collect(Collectors.toList());
        return collect;
        // return orderRepository.findAllByString(new OrderSearch())
        // .stream()
        // .map(SimpleOrderDto::new)
        // .collect(Collectors.toList());
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화, 이때 Member 쿼리가 나간다.
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화, Delivery 쿼리가 나간다.
        }
    }
}
