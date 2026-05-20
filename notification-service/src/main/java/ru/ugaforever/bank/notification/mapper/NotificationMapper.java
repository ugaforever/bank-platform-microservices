package ru.ugaforever.bank.notification.mapper;

import org.mapstruct.Mapper;
import ru.ugaforever.bank.chassis.dto.notification.NotificationResponseDto;
import ru.ugaforever.bank.notification.model.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponseDto toDto(Notification notification);
}
