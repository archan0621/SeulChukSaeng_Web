package kr.co.seulchuksaeng.seulchuksaengweb.controller;

import kr.co.seulchuksaeng.seulchuksaengweb.annotation.AdminAuthorize;
import kr.co.seulchuksaeng.seulchuksaengweb.annotation.UserAuthorize;
import kr.co.seulchuksaeng.seulchuksaengweb.domain.Member;
import kr.co.seulchuksaeng.seulchuksaengweb.dto.form.EventCreateForm;
import kr.co.seulchuksaeng.seulchuksaengweb.dto.form.EventReadForm;
import kr.co.seulchuksaeng.seulchuksaengweb.dto.form.EventUpdateForm;
import kr.co.seulchuksaeng.seulchuksaengweb.dto.result.EventCreateResult;
import kr.co.seulchuksaeng.seulchuksaengweb.dto.result.EventReadResult;
import kr.co.seulchuksaeng.seulchuksaengweb.dto.result.innerResult.EventReadInnerResult;
import kr.co.seulchuksaeng.seulchuksaengweb.dto.result.EventShowcaseResult;
import kr.co.seulchuksaeng.seulchuksaengweb.exception.event.NoEventException;
import kr.co.seulchuksaeng.seulchuksaengweb.repository.MemberRepository;
import kr.co.seulchuksaeng.seulchuksaengweb.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/event")
public class EventController {

    private final EventService eventService;

    private final MemberRepository memberRepository;

    @AdminAuthorize
    @PostMapping("/create")
    public EventCreateResult createEvent(@RequestBody EventCreateForm eventCreateForm, @AuthenticationPrincipal User user) {
        //log eventCreateForm
        try {
            eventService.create(eventCreateForm, user.getUsername());
            log.info("경기 생성에 성공하였습니다 : {}", eventCreateForm.getTitle());
            return new EventCreateResult("success", "경기 생성에 성공하였습니다");
        } catch (Exception e) { //특정 예외가 예상되지 않지만 불안하니까 일단 예외처리, 추후 테스트 단계에서 수정예정
            log.info("경기 생성에 실패하였습니다 : {}, 실패 사유 - {}", eventCreateForm.getTitle(), e.getMessage());
            return new EventCreateResult("fail", e.getMessage());
        }

    }

    @UserAuthorize
    @PostMapping("/list")
    public EventShowcaseResult listEvent(@AuthenticationPrincipal User user) {

        try {
            if (user.getAuthorities().toString().contains("ADMIN")) { // 관리자인 경우 처리
                log.info("관리자가 경기 목록을 조회하였습니다 : {}", user.getUsername());
                return eventService.list();
            } else if (user.getAuthorities().toString().contains("USER")) { // 사용자인 경우 처리
                Member member = memberRepository.findMemberById(user.getUsername());
                log.info("사용자가 경기 목록을 조회하였습니다 : {}", user.getUsername());
                return eventService.list(member.getGender());
            }
        } catch (NoEventException e) { // 조회할 경기가 없는 경우
            log.info("경기 목록 조회에 실패하였습니다 : {} {}", user.getUsername(), e.getMessage());
            return new EventShowcaseResult("fail", e.getMessage(), null, 0);
        }

        log.info("경기 목록 조회에 실패하였습니다 : {}", user.getUsername());
        return new EventShowcaseResult("fail", "조회에 오류가 발생하였습니다.", null, 0);
    }

    @UserAuthorize
    @PostMapping("/read")
    public EventReadResult readEvent(@RequestBody EventReadForm eventReadForm, @AuthenticationPrincipal User user) {

        try {
            EventReadInnerResult result = eventService.read(eventReadForm.getEventId());
            log.info("경기 조회에 성공하였습니다 : {}", user.getUsername());
            return new EventReadResult("success", "경기 조회에 성공하였습니다", result);
        } catch (NoEventException e) {
            log.info("경기 조회에 실패하였습니다 : {} {}", user.getUsername(), e.getMessage());
            return new EventReadResult("fail", e.getMessage(), null);
        }
        
    }

    @AdminAuthorize
    @PostMapping("/update")
    public EventCreateResult updateEvent(@RequestBody EventUpdateForm form, @AuthenticationPrincipal User user) {
        try {
            eventService.update(form);
            log.info("경기 내용 수정에 성공하였습니다 : {}, {}", user.getUsername(), form.getTitle());
            return new EventCreateResult("success", "경기 내용 수정에 성공하였습니다");
        } catch (NoEventException e) {
            log.info("경기 내용 수정에 실패하였습니다 : {}, {}", user.getUsername(), e.getMessage());
            return new EventCreateResult("fail", e.getMessage());
        }
    }

}
