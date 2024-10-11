package com.jpabook.jpashop.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jpabook.jpashop.domain.Order;
import com.jpabook.jpashop.repository.OrderRepository;
import com.jpabook.jpashop.repository.OrderSearch;

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
         * (jackson-dataType-Hibernate 등을 이용해 해결할 수 있지만 애초에 엔티티를 외부에 노출시키는 상황을 만들지 않는 것이 좋다.)
         * 
         */
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        return all;
    }
    
}
