package dev.saraki.wofuf.modules.users.config

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/15 00:25
 *   @description:
 */
import dev.saraki.wofuf.modules.users.infra.repos.jpa.UserJpaRepo
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/15 00:19
 *   @description:
 */
@Configuration
// 核心：只有容器中没有 UserJpaRepo Bean 时，才加载该配置
@ConditionalOnMissingBean(UserJpaRepo::class)
@EnableJpaRepositories(basePackages = ["dev.saraki.wofuf.modules.users.infra.repos.jpa"])
class UserJpaConfig
