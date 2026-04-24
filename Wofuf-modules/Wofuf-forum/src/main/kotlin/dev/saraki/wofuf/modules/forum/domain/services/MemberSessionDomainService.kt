package dev.saraki.wofuf.modules.forum.domain.services

import dev.saraki.wofuf.modules.forum.domain.Member
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.shared.core.Result

/**
 * 成员会话领域服务 - 处理成员退出等会话相关操作
 *
 * @author YaeSaraki
 * @date 2026/4/24
 */
interface MemberSessionDomainService {

    /**
     * 处理成员退出
     * @param memberId 成员 ID
     * @param jti JWT ID，用于标识本次会话
     * @return 处理结果，包含更新后的 Member
     */
    fun handleLogout(memberId: MemberId, jti: String): Result<Member>
}