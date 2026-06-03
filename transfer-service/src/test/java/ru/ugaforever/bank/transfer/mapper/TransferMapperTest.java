package ru.ugaforever.bank.transfer.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.ugaforever.bank.chassis.dto.transfer.TransferRequestDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferResponseDto;
import ru.ugaforever.bank.transfer.model.Transfer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferMapperTest {

    private static final Long TRASFER_ID = 1L;
    private static final String FROM_LOGIN = "ivanov";
    private static final String TO_LOGIN = "petrov";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100);

    private TransferMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TransferMapper.class);
    }

    @Test
    @DisplayName("Должен маппить TransferRequestDto в Transfer")
    void shouldMapToEntity() {
        TransferRequestDto dto = TransferRequestDto.builder()
                .fromLogin(FROM_LOGIN)
                .toLogin(TO_LOGIN)
                .amount(AMOUNT)
                .build();

        Transfer transfer = mapper.toEntity(dto);

        assertThat(transfer.getId()).isNull();
        assertThat(transfer.getFromLogin()).isEqualTo(FROM_LOGIN);
        assertThat(transfer.getToLogin()).isEqualTo(TO_LOGIN);
        assertThat(transfer.getAmount()).isEqualTo(AMOUNT);
    }

    @Test
    @DisplayName("Должен маппить Transfer в TransferResponseDto")
    void shouldMapToDto() {
        Transfer transfer = Transfer.builder()
                .id(TRASFER_ID)
                .fromLogin(FROM_LOGIN)
                .toLogin(TO_LOGIN)
                .amount(AMOUNT)
                .build();

        TransferResponseDto dto = mapper.toDto(transfer);

        assertThat(dto.getId()).isEqualTo(TRASFER_ID);
        assertThat(dto.getFromLogin()).isEqualTo(FROM_LOGIN);
        assertThat(dto.getToLogin()).isEqualTo(TO_LOGIN);
        assertThat(dto.getAmount()).isEqualTo(AMOUNT);
    }
}
