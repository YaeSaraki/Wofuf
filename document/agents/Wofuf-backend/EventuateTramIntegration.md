# Eventuate Tram 领域事件服务集成说明

## 1. 概述

本文档介绍了如何在Wofuf项目中使用Eventuate Tram实现领域事件服务。Eventuate Tram是一个轻量级的事件总线框架，用于在微服务架构中实现可靠的事件发布和订阅。

## 2. 已实现的功能

### 2.1 核心组件

- **EventuateTramConfig**: Eventuate Tram的核心配置类
- **EventuateTramEventPublisher**: 事件发布器适配器，将DomainEvents与Eventuate Tram集成
- **EventSubscriber**: 事件订阅器基类，提供事件订阅的通用功能
- **TransactionAwareEventPublisher**: 事务感知的事件发布器，确保事件在事务提交后发布

### 2.2 示例代码

- **UserCreatedEvent**: 示例领域事件类
- **UserEventSubscriber**: 示例事件订阅器

## 3. 使用指南

### 3.1 发布领域事件

#### 3.1.1 创建领域事件类

```kotlin
package dev.saraki.wofuf.modules.example.domain.events

import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.events.IDomainEvent
import java.time.LocalDateTime

data class ExampleEvent(
    private val aggregateId: UniqueEntityId,
    val data: String,
    override val dataTimeOccurred: LocalDateTime = LocalDateTime.now()
) : IDomainEvent {
    override fun getAggregateId(): UniqueEntityId {
        return aggregateId
    }
}
```

#### 3.1.2 在聚合根中发布事件

```kotlin
package dev.saraki.wofuf.modules.example.domain

import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.modules.example.domain.events.ExampleEvent

class ExampleAggregateRoot(
    props: ExampleProps,
    id: UniqueEntityId? = null
) : AggregateRoot<ExampleProps>(props, id) {
    
    fun doSomething() {
        // 执行业务逻辑
        
        // 发布领域事件
        addDomainEvent(ExampleEvent(
            aggregateId = _id,
            data = "Example data"
        ))
    }
}
```

#### 3.1.3 在服务层中注册事件发布

```kotlin
package dev.saraki.wofuf.modules.example.useCases

import dev.saraki.wofuf.modules.example.domain.ExampleAggregateRoot
import dev.saraki.wofuf.shared.infra.events.TransactionAwareEventPublisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExampleUseCase {
    
    @Autowired
    private lateinit var transactionAwareEventPublisher: TransactionAwareEventPublisher
    
    @Autowired
    private lateinit var exampleRepo: ExampleRepo
    
    @Transactional
    fun execute() {
        val example = ExampleAggregateRoot(ExampleProps(...))
        example.doSomething()
        
        exampleRepo.save(example)
        
        // 注册事件发布，确保事务提交后发布
        transactionAwareEventPublisher.registerForPublication(example._id)
    }
}
```

### 3.2 订阅领域事件

#### 3.2.1 创建事件订阅器

```kotlin
package dev.saraki.wofuf.modules.example.infra.events

import dev.saraki.wofuf.modules.example.domain.ExampleAggregateRoot
import dev.saraki.wofuf.modules.example.domain.events.ExampleEvent
import dev.saraki.wofuf.shared.infra.events.EventSubscriber
import org.springframework.stereotype.Component

@Component
class ExampleEventSubscriber : EventSubscriber() {
    
    override fun getDomainEventHandlers(): DomainEventHandlers {
        return eventHandlers("example", ExampleAggregateRoot::class.java)
            .onEvent(ExampleEvent::class.java, this::handleExampleEvent)
            .build()
    }
    
    private fun handleExampleEvent(aggregate: ExampleAggregateRoot, event: ExampleEvent) {
        // 处理事件的业务逻辑
        println("Received ExampleEvent: ${event.data}")
    }
}
```

## 4. 配置说明

### 4.1 数据库配置

Eventuate Tram需要使用数据库来存储事件，确保数据库配置正确：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wofuf?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### 4.2 Eventuate Tram配置

Eventuate Tram的配置已经在`EventuateTramConfig`类中完成，无需额外配置。

## 5. 事务管理

为了确保事件的可靠发布，建议在服务层方法上使用`@Transactional`注解，并在方法结束时调用
`transactionAwareEventPublisher.registerForPublication(aggregateId)`来注册事件发布。这样可以确保事件在事务提交后才被发布。

## 6. 监控和调试

### 6.1 日志配置

可以通过调整日志级别来监控Eventuate Tram的运行情况：

```properties
logging.level.io.eventuate=INFO
```

### 6.2 调试技巧

- 使用`DomainEvents.register`方法注册全局事件监听器，以便在开发环境中查看所有发布的事件
- 检查数据库中的`message`表，查看已发布的事件
- 使用Eventuate Tram的管理界面（如果已安装）监控事件流

## 7. 最佳实践

1. **事件设计**: 领域事件应该是业务相关的，包含足够的信息，并且是不可变的
2. **事件命名**: 事件名称应该使用过去式，清晰地表达发生了什么，如`UserCreatedEvent`、`OrderShippedEvent`
3. **事务边界**: 确保事件发布与业务操作在同一个事务中
4. **错误处理**: 实现事件处理的错误重试机制，避免事件丢失
5. **版本管理**: 考虑事件的版本管理，以便在事件结构变更时能够向后兼容

## 8. 注意事项

1. Eventuate Tram需要数据库支持，请确保数据库已经正确配置
2. 事件发布和订阅需要在事务环境中运行，建议使用Spring的声明式事务管理
3. 对于高吞吐量的系统，考虑使用Eventuate Tram的分布式部署模式
4. 定期清理数据库中的事件表，避免数据过大影响性能

## 9. 总结

通过集成Eventuate Tram，我们可以在Wofuf项目中实现可靠的领域事件服务，支持事件驱动的架构设计。本文档提供了完整的使用指南，包括事件发布、订阅、配置和最佳实践，帮助开发人员快速上手使用Eventuate
Tram。