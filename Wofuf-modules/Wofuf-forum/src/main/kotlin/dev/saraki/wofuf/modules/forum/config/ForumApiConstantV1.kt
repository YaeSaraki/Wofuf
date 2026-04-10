// forum/src/main/kotlin/dev/saraki/wofuf/modules/forum/config/ForumApiConstantV1.kt
package dev.saraki.wofuf.modules.forum.config

import dev.saraki.wofuf.shared.config.ApiConstantV1

object ForumApiConstantV1 {
    private const val BASE = "${ApiConstantV1.API_BASE_PATH}/forum"

    // ===================== 路径参数 =====================
    object Param {
        const val POST_ID = "postId"
        const val POST_SLUG = "postSlug"
        const val COMMENT_ID = "commentId"
        const val MEMBER_ID = "memberId"
    }

    // ===================== 基础路径 =====================
    object Base {
        /** /api/v1/forum */
        const val ROOT = BASE
    }

    // ===================== 成员相关路径 =====================
    object Members {
        /** /api/v1/forum/members */
        const val ROOT = "$BASE/members"

        /** /api/v1/forum/members/{memberId} */
        const val BY_ID = "$ROOT/{${Param.MEMBER_ID}}"

        /** /api/v1/forum/members/current */
        const val CURRENT = "$ROOT/current"

        /** /api/v1/forum/members/username/{username} */
        const val BY_USERNAME = "$ROOT/username/{username}"
    }

    // ===================== 帖子相关路径 =====================
    object Posts {
        /** /api/v1/forum/posts */
        const val ROOT = "$BASE/posts"

        /** /api/v1/forum/posts/{postId} */
        const val BY_ID = "$ROOT/{${Param.POST_ID}}"

        /** /api/v1/forum/posts/slug/{postSlug} */
        const val BY_SLUG = "$ROOT/slug/{${Param.POST_SLUG}}"

        /** /api/v1/forum/posts/{postId}/likes */
        const val LIKES = "$BY_ID/likes"

        /** /api/v1/forum/posts/{postId}/upvote */
        const val UPVOTE = "$BY_ID/upvote"

        /** /api/v1/forum/posts/{postId}/downvote */
        const val DOWNVOTE = "$BY_ID/downvote"

        /** /api/v1/forum/posts/{postId}/unvote */
        const val UNVOTE = "$BY_ID/unvote"

        /** /api/v1/forum/posts/slug/{postSlug}/upvote */
        const val UPVOTE_BY_SLUG = "$BY_SLUG/upvote"

        /** /api/v1/forum/posts/slug/{postSlug}/downvote */
        const val DOWNVOTE_BY_SLUG = "$BY_SLUG/downvote"

        /** /api/v1/forum/posts/slug/{postSlug}/unvote */
        const val UNVOTE_BY_SLUG = "$BY_SLUG/unvote"

        /** /api/v1/forum/posts/{postId}/comments */
        const val COMMENTS = "$BY_ID/comments"

        /** /api/v1/forum/posts/slug/{postSlug}/comments */
        const val COMMENTS_BY_SLUG = "$BY_SLUG/comments"

        /** /api/v1/forum/posts/{postId}/replies */
        const val REPLIES = "$BY_ID/replies"

        /** /api/v1/forum/posts/slug/{postSlug}/replies */
        const val REPLIES_BY_SLUG = "$BY_SLUG/replies"

        /** /api/v1/forum/posts/recent */
        const val RECENT = "$ROOT/recent"

        /** /api/v1/forum/posts/popular */
        const val POPULAR = "$ROOT/popular"
    }

    // ===================== 评论相关路径 =====================
    object Comments {
        /** /api/v1/forum/comments */
        const val ROOT = "$BASE/comments"

        /** /api/v1/forum/comments/{commentId} */
        const val BY_ID = "$ROOT/{${Param.COMMENT_ID}}"

        /** /api/v1/forum/comments/{commentId}/replies */
        const val REPLIES = "$BY_ID/replies"

        /** /api/v1/forum/comments/{commentId}/stats */
        const val STATS = "$BY_ID/stats"

        /** /api/v1/forum/comments/{commentId}/upvote */
        const val UPVOTE = "$BY_ID/upvote"

        /** /api/v1/forum/comments/{commentId}/downvote */
        const val DOWNVOTE = "$BY_ID/downvote"

        /** /api/v1/forum/comments/{commentId}/unvote */
        const val UNVOTE = "$BY_ID/unvote"

        /** /api/v1/forum/posts/slug/{postSlug}/comments */
        const val BY_POST_SLUG = "$BASE/posts/slug/{${Param.POST_SLUG}}/comments"
    }

    // ===================== 图片相关路径 =====================
    object Images {
        /** /api/v1/forum/images */
        const val ROOT = "$BASE/images"

        /** /api/v1/forum/images/upload */
        const val UPLOAD = "$ROOT/upload"
    }

    // ===================== 管理功能路径 =====================
    object Admin {
        private const val ADMIN_BASE = "$BASE/admin"

        /** /api/v1/forum/admin */
        const val ROOT = ADMIN_BASE

        // -------------------- 帖子管理 --------------------
        object Posts {
            private const val POSTS_BASE = "$ADMIN_BASE/posts"

            /** /api/v1/forum/admin/posts/{postId}/pin */
            const val PIN = "$POSTS_BASE/{${Param.POST_ID}}/pin"

            /** /api/v1/forum/admin/posts/{postId}/unpin */
            const val UNPIN = "$POSTS_BASE/{${Param.POST_ID}}/unpin"

            /** /api/v1/forum/admin/posts/{postId}/feature */
            const val FEATURE = "$POSTS_BASE/{${Param.POST_ID}}/feature"

            /** /api/v1/forum/admin/posts/{postId}/unfeature */
            const val UNFEATURE = "$POSTS_BASE/{${Param.POST_ID}}/unfeature"

            /** /api/v1/forum/admin/posts/{postId}/hide */
            const val HIDE = "$POSTS_BASE/{${Param.POST_ID}}/hide"

            /** /api/v1/forum/admin/posts/{postId}/show */
            const val SHOW = "$POSTS_BASE/{${Param.POST_ID}}/show"

            /** /api/v1/forum/admin/posts/{postId}/review */
            const val REVIEW = "$POSTS_BASE/{${Param.POST_ID}}/review"

            /** /api/v1/forum/admin/posts/{postId}/approve */
            const val APPROVE = "$POSTS_BASE/{${Param.POST_ID}}/approve"

            /** /api/v1/forum/admin/posts/pinned */
            const val PINNED = "$POSTS_BASE/pinned"

            /** /api/v1/forum/admin/posts/featured */
            const val FEATURED = "$POSTS_BASE/featured"

            /** /api/v1/forum/admin/posts/hidden */
            const val HIDDEN = "$POSTS_BASE/hidden"

            /** /api/v1/forum/admin/posts/for-review */
            const val FOR_REVIEW = "$POSTS_BASE/for-review"
        }

        // -------------------- 评论管理 --------------------
        object Comments {
            private const val COMMENTS_BASE = "$ADMIN_BASE/comments"

            /** /api/v1/forum/admin/comments */
            const val ROOT = COMMENTS_BASE

            /** /api/v1/forum/admin/comments/{commentId}/hide */
            const val HIDE = "$COMMENTS_BASE/{${Param.COMMENT_ID}}/hide"

            /** /api/v1/forum/admin/comments/{commentId}/show */
            const val SHOW = "$COMMENTS_BASE/{${Param.COMMENT_ID}}/show"

            /** /api/v1/forum/admin/comments/hidden */
            const val HIDDEN = "$COMMENTS_BASE/hidden"
        }

        // -------------------- 成员管理 --------------------
        object Members {
            private const val MEMBERS_BASE = "$ADMIN_BASE/members"

            /** /api/v1/forum/admin/members/{memberId}/ban */
            const val BAN = "$MEMBERS_BASE/{${Param.MEMBER_ID}}/ban"

            /** /api/v1/forum/admin/members/{memberId}/unban */
            const val UNBAN = "$MEMBERS_BASE/{${Param.MEMBER_ID}}/unban"

            /** /api/v1/forum/admin/members/{memberId}/permissions */
            const val PERMISSIONS = "$MEMBERS_BASE/{${Param.MEMBER_ID}}/permissions"

            /** /api/v1/forum/admin/members/{memberId}/permissions/{permission} */
            const val PERMISSION_BY_NAME = "$MEMBERS_BASE/{${Param.MEMBER_ID}}/permissions/{permission}"

            /** /api/v1/forum/admin/members/banned */
            const val BANNED = "$MEMBERS_BASE/banned"

            /** /api/v1/forum/admin/members/by-permission/{permission} */
            const val BY_PERMISSION = "$MEMBERS_BASE/by-permission/{permission}"
        }

        // -------------------- 分类管理 --------------------
        object Categories {
            private const val CATEGORIES_BASE = "$ADMIN_BASE/categories"

            /** /api/v1/forum/admin/categories/{categoryId} */
            const val BY_ID = "$CATEGORIES_BASE/{categoryId}"
        }
    }

    // ===================== 工具方法 =====================
    /**
     * 构建单个帖子路径
     * @param postId 帖子ID
     * @return 完整路径，如 "/api/v1/forum/posts/1001"
     */
    fun buildPostPath(postId: String): String {
        return Posts.BY_ID.replace("{${Param.POST_ID}}", postId)
    }

    /**
     * 构建帖子点赞路径
     * @param postId 帖子ID
     * @return 完整路径，如 "/api/v1/forum/posts/1001/likes"
     */
    fun buildPostLikesPath(postId: String): String {
        return Posts.LIKES.replace("{${Param.POST_ID}}", postId)
    }

    /**
     * 构建单个评论路径
     * @param commentId 评论ID
     * @return 完整路径，如 "/api/v1/forum/comments/2001"
     */
    fun buildCommentPath(commentId: String): String {
        return Comments.BY_ID.replace("{${Param.COMMENT_ID}}", commentId)
    }

    /**
     * 构建帖子下的评论路径
     * @param postId 帖子ID
     * @return 完整路径，如 "/api/v1/forum/posts/1001/comments"
     */
    fun buildPostCommentsPath(postId: String): String {
        return Posts.COMMENTS.replace("{${Param.POST_ID}}", postId)
    }

    /**
     * 构建单个成员路径
     * @param memberId 成员ID
     * @return 完整路径，如 "/api/v1/forum/members/3001"
     */
    fun buildMemberPath(memberId: String): String {
        return Members.BY_ID.replace("{${Param.MEMBER_ID}}", memberId)
    }
}
