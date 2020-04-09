package com.itechartgroup.telemedpoc.chat.web;

import com.itechartgroup.telemedpoc.chat.dto.ChatMessageDto;
import com.itechartgroup.telemedpoc.chat.dto.ChatMessageSource;
import com.itechartgroup.telemedpoc.chat.dto.ChatRoomDto;
import com.itechartgroup.telemedpoc.chat.service.ChatMessageService;
import com.itechartgroup.telemedpoc.chat.service.ChatRoomService;
import com.itechartgroup.telemedpoc.chat.utils.UserDetailsUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;

import static com.itechartgroup.telemedpoc.chat.constant.ChatConstants.SESSION_ATTR_LAST_FETCH;


/**
 * @author s.vareyko
 * @since 07.04.2020
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessageDto> send(@RequestBody final ChatMessageDto dto) {

        // only possible to send messages as user
        final Long fromId = UserDetailsUtils.currentUserId();
        dto.setAuthor(fromId);
        dto.setSource(ChatMessageSource.USER);

        return new ResponseEntity<>(chatMessageService.send(dto), HttpStatus.ACCEPTED);
    }

    @GetMapping
    public ResponseEntity<Page<ChatRoomDto>> loadRooms(@PageableDefault final Pageable pageable,
                                                       final HttpSession session) {
        setLastFetchAttribute(session);
        return new ResponseEntity<>(chatRoomService.load(pageable), HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<ChatRoomDto> createRoom(@RequestBody final List<Long> participants) {
        return new ResponseEntity<>(chatRoomService.create(participants), HttpStatus.OK);
    }

    @GetMapping("/{dialogId}")
    public ResponseEntity<Page<ChatMessageDto>> loadMessages(@PathVariable final UUID dialogId,
                                                             @PageableDefault final Pageable pageable) {
        return new ResponseEntity<>(chatMessageService.load(dialogId, pageable), HttpStatus.OK);
    }

    @GetMapping("/poll")
    public ResponseEntity<SortedSet<ChatMessageDto>> pollMessages(final HttpSession session) {
        try {
            return new ResponseEntity<>(chatMessageService.poll(getLastFetchAttribute(session)), HttpStatus.OK);
        } finally {
            setLastFetchAttribute(session);
        }
    }

    private long getLastFetchAttribute(final HttpSession session) {
        return (long) Optional.ofNullable(session.getAttribute(SESSION_ATTR_LAST_FETCH))
                .orElseGet(System::currentTimeMillis);
    }

    private void setLastFetchAttribute(final HttpSession session) {
        session.setAttribute(SESSION_ATTR_LAST_FETCH, System.currentTimeMillis());
    }
}
