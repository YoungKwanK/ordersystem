package com.beyond16.ordersystem.member.controller;

import com.beyond16.ordersystem.common.auth.JwtTokenProvider;
import com.beyond16.ordersystem.common.dto.CommonDto;
import com.beyond16.ordersystem.member.domain.Member;
import com.beyond16.ordersystem.member.dto.*;
import com.beyond16.ordersystem.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody MemberCreateDto memberCreateDto) {
        return new ResponseEntity<>(CommonDto.builder()
                .result(memberService.save(memberCreateDto))
                .status_code(HttpStatus.CREATED.value())
                .status_message("회원가입 완료")
                .build(), HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> login(@RequestBody LoginReqDto loginReqDto) {
        Member member = memberService.login(loginReqDto);
//        AccessToken 생성
        String accessToken = jwtTokenProvider.generateAtToken(member);
//        RefreshToken 생성
        String refreshToken = jwtTokenProvider.generateRtToken(member);
        return new ResponseEntity<>(CommonDto.builder()
                .result(LoginResDto.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build())
                .status_code(HttpStatus.OK.value())
                .status_message("로그인 성공")
                .build(), HttpStatus.OK);
    }
    
//    rt를 통한 at 갱신 요청
    @PostMapping("/refresh-at")
    public ResponseEntity<?> generateNewAt(@RequestBody RefreshTokenDto refreshTokenDto){
//        rt검증 로직
        Member member = jwtTokenProvider.validateRt(refreshTokenDto.getRefreshToken());
//        at신규 생성
        String accessToken = jwtTokenProvider.generateAtToken(member);
        return new ResponseEntity<>(CommonDto.builder()
                .result(LoginResDto.builder()
                        .accessToken(accessToken)
                        .build().getAccessToken())
                .status_code(HttpStatus.OK.value())
                .status_message("AccessToken 재발급 성공")
                .build(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    @GetMapping("/list")
    public ResponseEntity<?> findAllMember() {
        return new ResponseEntity<>(CommonDto.builder()
                .result(memberService.findAllMember())
                .status_code(HttpStatus.OK.value())
                .status_message("모든 회원 조회 완료")
                .build(), HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    @GetMapping("/myinfo")
    public ResponseEntity<?> findMyInfo() {
        return new ResponseEntity<>(CommonDto.builder()
                .result(memberService.findMyInfo())
                .status_code(HttpStatus.OK.value())
                .status_message("내 정보 조회 완료")
                .build(), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteMember() {
        memberService.deleteMember();
        return new ResponseEntity<>(CommonDto.builder()
                .result("삭제")
                .status_code(HttpStatus.OK.value())
                .status_message("삭제 성공")
                .build(), HttpStatus.OK);
    }

}
