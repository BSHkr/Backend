package com.example.arom1.service;

import com.example.arom1.entity.Member;
import com.example.arom1.entity.MemberDetail;
import com.example.arom1.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberDetailService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override // 사용자의 이름(email)으로 사용자의 정보를 가져오는 메서드
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException((email)));

        return new MemberDetail(member);
    }
}
