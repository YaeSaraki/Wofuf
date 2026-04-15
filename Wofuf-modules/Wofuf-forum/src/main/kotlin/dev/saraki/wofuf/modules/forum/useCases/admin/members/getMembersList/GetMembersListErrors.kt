package dev.saraki.wofuf.modules.forum.useCases.admin.members.getMembersList

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class GetMembersListErrors {
    class MemberNotFoundError(val identifier: String) : Result.Failure<GetMembersListDto.Response>(
        exception = UseCaseError(code = "MEMBER_NOT_FOUND", message = "Member not found: $identifier")
    )
}
