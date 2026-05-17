package ru.ugaforever.bank.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.ugaforever.bank.account.model.Account;
import ru.ugaforever.bank.chassis.dto.AccountRequestDto;
import ru.ugaforever.bank.chassis.dto.AccountResponseDto;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    Account toEntity(AccountRequestDto dto);

    AccountResponseDto toDto(Account account);
}

