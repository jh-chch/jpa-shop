package com.jpabook.jpashop.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jpabook.jpashop.domain.Member;
import com.jpabook.jpashop.service.MemberService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/api/v1/members")
    public CreateMemeberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long memberId = memberService.join(member);
        return new CreateMemeberResponse(memberId);
    }

    @PostMapping("/api/v2/members")
    public CreateMemeberResponse saveMemberV2(@RequestBody @Valid CreateMemeberRequest request) {
        Member member = new Member();
        member.setName(request.getName());
        /*
         * - v1은 엔티티를 변경하면 api 스펙이 변경된다.
         * 만약, api 스펙이 변경되어도 중간에서 파라미터와 엔티티를 매핑시켜 해결할 수 있다.
         * Member 엔티티가 name -> usename으로 변경되어도,
         * member.setUsename(request.getName());
         * 
         * - v1은 엔티티를 사용하기 때문에 어떤 값이 들어오는지, 어떤 validation이 적용되었는지 보기 쉽다.
         * CreateMemberRequest를 사용함으로써 클라이언트한테는 name필드만 받는구나, @NotEmpty 조건이 있음을 확인할 수
         * 있다.
         */

        Long memberId = memberService.join(member);
        return new CreateMemeberResponse(memberId);
    }

    @Data
    @AllArgsConstructor
    static class CreateMemeberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class CreateMemeberResponse {
        private Long id;
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse putMethodName(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberRequest {
        @NotEmpty
        private String name;
    }
}
