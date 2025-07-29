package com.beyond16.ordersystem.member.service;

import com.beyond16.ordersystem.member.domain.Member;
import com.beyond16.ordersystem.member.dto.MemberCreateDto;
import com.beyond16.ordersystem.member.dto.LoginReqDto;
import com.beyond16.ordersystem.member.dto.MemberResDto;
import com.beyond16.ordersystem.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Long save(MemberCreateDto memberCreateDto){
        if (memberRepository.findByEmail(memberCreateDto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        return memberRepository.save(memberCreateDto.toEntity(
                passwordEncoder.encode(memberCreateDto.getPassword()))).getId();
    }

    public Member login(LoginReqDto loginReqDto){
        Member member = memberRepository.findByEmail(loginReqDto.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.")
        );
        if(!passwordEncoder.matches(loginReqDto.getPassword(),(member.getPassword()))){
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    public List<MemberResDto> findAllMember(){
        List<MemberResDto> memberList = memberRepository.findAll()
                .stream().map(a-> MemberResDto.fromEntity(a))
                .toList();
        return memberList;
    }

    public MemberResDto findMyInfo(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(
                ()->new EntityNotFoundException("member is not found")
        );
        return MemberResDto.fromEntity(member);
    }

    public void deleteMember(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(
                ()->new EntityNotFoundException("member is not found")
        );
        member.updateDelYn("Y");
    }
}
