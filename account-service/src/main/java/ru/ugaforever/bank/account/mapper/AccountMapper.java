package ru.ugaforever.bank.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.ugaforever.bank.account.model.Account;
import ru.ugaforever.bank.chassis.dto.account.AccountRequestDto;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    Account toEntity(AccountRequestDto dto);

    AccountResponseDto toDto(Account account);
}

