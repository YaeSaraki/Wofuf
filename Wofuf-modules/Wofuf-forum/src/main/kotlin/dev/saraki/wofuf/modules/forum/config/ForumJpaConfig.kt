package dev.saraki.wofuf.modules.forum.config

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/15 00:25
 *   @description:
 */
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.PostJpaRepo
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
@ConditionalOnMissingBean(PostJpaRepo::class)
@EnableJpaRepositories(basePackages = ["dev.saraki.wofuf.modules.forum.infra.repos.jpa"])
class ForumJpaConfig
