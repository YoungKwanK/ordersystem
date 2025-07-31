package com.beyond16.ordersystem.ordering.repository;

import com.beyond16.ordersystem.member.domain.Member;
import com.beyond16.ordersystem.ordering.domain.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderingRepository extends JpaRepository<Ordering, Long> {
    public List<Ordering> findAllByMember(Member member);
}
