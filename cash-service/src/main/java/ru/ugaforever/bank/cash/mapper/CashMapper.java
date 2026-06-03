package ru.ugaforever.bank.cash.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;
import ru.ugaforever.bank.cash.model.Cash;

@Mapper(componentModel = "spring")
public interface CashMapper {

    @Mapping(target = "success", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    CashResponseDto toDto(Cash cash);
}
