package com.beyond16.ordersystem.member.domain;

import com.beyond16.ordersystem.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
// jpql을 제외하고 모든 조회쿼리에 where del+yn = "N" 붙이는 효과
@Where(clause = "del_yn = 'N'")
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(length = 50, unique = true, nullable = false)
    private String email;
    private String password;
    @Builder.Default
    private String delYn = "N";
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    public void updateDelYn(String delYn) {
        this.delYn = delYn;
    }
}
