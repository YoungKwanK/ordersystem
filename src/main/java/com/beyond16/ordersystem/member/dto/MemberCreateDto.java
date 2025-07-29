package com.beyond16.ordersystem.member.dto;

import com.beyond16.ordersystem.member.domain.Member;
import com.beyond16.ordersystem.member.domain.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberCreateDto {
    @NotEmpty(message = "이름은 필수 항목입니다.")
    private String name;
    @NotEmpty(message = "이메일은 필수 항목입니다.")
    private String email;
    @NotEmpty(message = "비밀번호는 필수 항목입니다.")
    @Size(min = 8, message = "비밀번호는 8글자 이상으로 설정해주세요.")
    private String password;

    public Member toEntity(String encodePassword){
        return Member.builder()
                .email(this.email)
                .name(this.name)
                .password(encodePassword)
                .build();
    }
}
