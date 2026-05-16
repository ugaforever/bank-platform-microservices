package ru.ugaforever.bank.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.ugaforever.bank.account.dto.AccountRequestDto;
import ru.ugaforever.bank.account.dto.AccountResponseDto;
import ru.ugaforever.bank.account.model.Account;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    Account toEntity(AccountRequestDto dto);

    AccountResponseDto toDto(Account account);
}

