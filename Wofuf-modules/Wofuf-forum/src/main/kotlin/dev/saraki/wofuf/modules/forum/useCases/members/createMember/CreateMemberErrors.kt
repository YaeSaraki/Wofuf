package dev.saraki.wofuf.modules.forum.useCases.members.createMember

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/27 13:01
 *   @description:
 */
class CreateMemberErrors {

    // CodeError
    class CodeError() : Result.Failure<Unit>(
        exception = UseCaseError(
            code = " Code_Error",
            message = "Failed to create member, code is invalid"
        )
    )
}