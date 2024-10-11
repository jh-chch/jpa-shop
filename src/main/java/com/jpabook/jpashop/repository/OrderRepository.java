package com.jpabook.jpashop.repository;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.jpabook.jpashop.domain.Order;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAllByString(OrderSearch orderSearch) {
        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status =: status";
        }

        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class).setMaxResults(1000);

        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d",
                Order.class).getResultList();
    }

    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                "select new com.jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o"
                        +
                        " join o.member m" +
                        " join o.delivery d",
                OrderSimpleQueryDto.class).getResultList();
    }

    public List<Order> findAllWithItem(OrderSearch orderSearch) {
        /**
         * distinct를 사용하지 않으면 1대N 조회여서 row가 증가해서 조회된다.
         * 1.SQL의 distinct 추가 및 2.같은 엔티티가 조회되면 중복을 걸러준다.
         * 
         * 단, 페이징 불가 -> 하이버네이트는 모든 데이터를 가져오고 메모리에서 페이징 처리를 한다.
         * DB 입장에서는 1번이 실행된 결과로 페이징해야하고,
         * jpa 입장에서는 2번이 실행된 결과로 페이징을 해야하므로, 모든 데이터를 가져온 후 메모리에서 페이징을 한다. (데이터가 많으면 메모리
         * 부족 에러가 날 수 있다.)
         * 
         * #컬렉션 fetch join은 한개만 사용해야 한다.
         */
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.Member m" +
                        " join fetch o.delivery d" +
                        " join fetch orderItem oi" +
                        " join fetch oi.item i",
                Order.class)
                // .setFirstResult(1) 페이징 불가능!
                // .setMaxResults(50)
                .getResultList();
    }

}
