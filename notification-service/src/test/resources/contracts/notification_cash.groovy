package ru.ugaforever.bank.notification.contract;

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Send cash notification'
    name 'notification_cash'

    request {
        method POST()
        url '/notification'
        headers {
            header 'Content-Type', applicationJson()
            /*header 'Authorization', value(
                    consumer(regex('Bearer\\s+.+')),
                    producer('Bearer test-token')
            )*/
        }
        body(
                source: 'CASH_SERVICE',
                message: 'Your operation was successful'
        )
    }

    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                id: 1,
                source: 'CASH_SERVICE',
                message: 'Your operation was successful',
                actionAt: '2026-01-01T01:02:03.456Z'
        )
    }
}