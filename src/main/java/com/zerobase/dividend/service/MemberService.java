package com.zerobase.dividend.service;

import com.zerobase.dividend.exception.Impl.AlreadyExistUserException;
import com.zerobase.dividend.exception.Impl.NoMatchesPw;
import com.zerobase.dividend.exception.Impl.NoUserException;
import com.zerobase.dividend.model.Auth;
import com.zerobase.dividend.persist.MemberRepository;
import com.zerobase.dividend.persist.entity.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class MemberService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + username));
    }

    public MemberEntity register(Auth.SignUp member) { //회원가입 메소드
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new AlreadyExistUserException();
        }
        //멤버 정보 DB에 저장할 때 Password는 암호화 해서 넣어야함.
        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        var result = this.memberRepository.save(member.toEntity());
        return result;
    }

    public MemberEntity authenticate(Auth.SignIn member) { //로그인 시 검증하기 위한 메소드
        var user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(NoUserException::new);

        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new NoMatchesPw();
        }
        return user;
    }
}
