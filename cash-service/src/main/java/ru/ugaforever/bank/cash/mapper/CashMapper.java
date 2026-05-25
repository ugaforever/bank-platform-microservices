package ru.ugaforever.bank.cash.mapper;

import org.mapstruct.Mapper;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;
import ru.ugaforever.bank.cash.model.Cash;

@Mapper(componentModel = "spring")
public interface CashMapper {

    CashResponseDto toDto(Cash cash);
}
