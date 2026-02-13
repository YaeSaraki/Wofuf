package dev.saraki.wofuf.modules.users.useCases.createUser

import dev.saraki.wofuf.modules.users.domain.events.CreateUser
import dev.saraki.wofuf.shared.domain.events.IDomainEvent
import dev.saraki.wofuf.shared.domain.events.IDomainEventHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component


@Component
class CreateUserEventHandler : IDomainEventHandler<CreateUser> {
    private val log = KotlinLogging.logger {}

    @PostConstruct
    fun init() {
        log.info { "CreateUserEventHandler 创建了！" }
    }

    override fun handle(event: CreateUser) {
        log.info { "用户ID：${event.getAggregateId()}" }
        log.info { "用户名：${event.username}" }
        log.info { "密码：${event.password}" }
    }

    override fun getEventType(): Class<out IDomainEvent> {
        return CreateUser::class.java
    }
}