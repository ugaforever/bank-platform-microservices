package ru.ugaforever.bank.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ugaforever.bank.chassis.client.GatewayClient;
import ru.ugaforever.bank.chassis.dto.transfer.TransferRequestDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferResponseDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
    
    private final GatewayClient gatewayClient;


    public TransferResponseDto submit(TransferRequestDto requestDto) {

        return gatewayClient.submit(requestDto);
    }
}
