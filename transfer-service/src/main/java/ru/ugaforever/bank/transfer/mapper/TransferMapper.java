package ru.ugaforever.bank.transfer.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.ugaforever.bank.chassis.dto.transfer.TransferRequestDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferResponseDto;
import ru.ugaforever.bank.transfer.model.Transfer;

@Mapper(componentModel = "spring")
public interface TransferMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actionAt", ignore = true)
    Transfer toEntity(TransferRequestDto dto);

    TransferResponseDto toDto(Transfer transfer);
}

