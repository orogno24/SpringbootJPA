package kopo.poly.dto;

import lombok.Builder;

@Builder
public record EventDTO(
        Long eventSeq,

        String eventSort,

        String eventPlace,

        String eventName,

        String eventDate,

        String eventWhere,

        String eventWho,

        String eventMoney,

        String eventWhoinfo,

        String eventInfo,

        String eventEtc,

        String eventPafeInfo,

        String eventImage,

        String eventStartdate,

        String eventEnddate,

        String eventTheme,

        String eventX,

        String eventY,

        String eventFree
) {

}