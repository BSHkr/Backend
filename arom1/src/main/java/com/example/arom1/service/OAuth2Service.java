package com.example.arom1.service;

import com.example.arom1.entity.Member;
import com.example.arom1.entity.oauth2.CustomOAuth2User;
import com.example.arom1.entity.oauth2.KakaoUserDetails;
import com.example.arom1.entity.oauth2.OAuth2UserInfo;
import com.example.arom1.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class OAuth2Service implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

        String provider = userRequest.getClientRegistration()
                .getRegistrationId();

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2UserInfo oAuth2UserInfo = findProvider(provider, oAuth2User);

        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();

        // 이메일로 가입된 회원인지 조회, 없으면 임시 멤버 생성
        Member member = memberRepository.findByEmail(email)
                .orElse(Member.builder()
                        .email(email).role("ROLE_GUEST").provider(provider).providerId(providerId).build());
        //OAuth2 로 회원가입을 안한 유저는 어떻게 처리?
        if (member.getProvider() == null) {
            System.out.println("This is a local account. Need to add a provider : " + oAuth2UserInfo.getProvider());
        }

        // 권한, 회원속성, 속성이름, Member 객체로 CustomOAuth2User 객체 반환
        return new CustomOAuth2User(Collections.singleton(new SimpleGrantedAuthority(member.getRole())),
                oAuth2User.getAttributes(), userNameAttributeName, member);
    }


    private OAuth2UserInfo findProvider(String provider, OAuth2User oAuth2User) {
        if (provider.equals("kakao")) {
            System.out.println("카카오 로그인");
        }
//        if(provider.equals("google")){
//            System.out.println("구글 로그인");
//            return new GoogleUserDetails(oAuth2User.getAttributes());
//        }
        return new KakaoUserDetails(oAuth2User.getAttributes());
    }


}
