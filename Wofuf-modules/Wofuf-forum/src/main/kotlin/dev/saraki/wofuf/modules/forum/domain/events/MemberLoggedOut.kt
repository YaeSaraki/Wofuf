package dev.saraki.wofuf.modules.forum.domain.events

import dev.saraki.wofuf.modules.forum.domain.Member
import dev.saraki.wofuf.shared.domain.events.IDomainEvent

/**
 * 成员退出事件
 * 当成员主动退出时发布，其他 Forum 实例订阅以同步 JTI 黑名单
 *
 * @author YaeSaraki
 * @date 2026/4/24
 */
class MemberLoggedOut(
    member: Member,
    val jti: String  // JWT ID，从 token 中提取
) : IDomainEvent(member.memberId.value)