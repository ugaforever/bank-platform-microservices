package ru.ugaforever.bank.notification.contract;

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Send notification to bank.notification topic'
    name 'notification_topic'

    // 1. Consumer вызывает метод, который генерирует событие
    input {
        triggeredBy("publishNotificationEvent()")
    }

    // 2. Ожидаемое сообщение в Kafka
    outputMessage {
        sentTo("bank.notification")

        headers {
            // Ключ сообщения (request.getSource().name())
            header("kafka_messageKey", $(consumer(regex('ACCOUNT|CASH|TRANSFER')), producer('ACCOUNT')))

            // Content-Type для JSON
            header("contentType", "application/json")

            // Дополнительные заголовки для отслеживания
            header("eventType", "NOTIFICATION")
            header("source", $(consumer(regex('ACCOUNT|CASH|TRANSFER')), producer('ACCOUNT')))
            header("version", "1.0")
        }

        // 2.3. Тело сообщения (NotificationRequestDto)
        body([
                source   : $(consumer(regex('ACCOUNT|CASH|TRANSFER')), producer('ACCOUNT')),
                message  : $(consumer(anyNonEmptyString()), producer('Your operation was successful')),
                recipient: $(consumer(anyUuid()), producer(execute {
                    return UUID.randomUUID().toString()
                })),
                timestamp: $(consumer(anyDate()), producer(execute {
                    return Instant.now().toString()
                }))
        ])

        // 2.4. Матчеры для строгой валидации
        bodyMatchers {
            // source — enum с тремя значениями
            jsonPath('$.source', byRegex('ACCOUNT|CASH|TRANSFER'))

            // message — непустая строка
            jsonPath('$.message', byRegex('.+'))

            // recipient — UUID формат
            jsonPath('$.recipient', byRegex('[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}'))

            // timestamp — ISO 8601 с Z
            jsonPath('$.timestamp', byRegex('\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z'))
        }
    }
}