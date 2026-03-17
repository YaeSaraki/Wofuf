<script lang="ts" setup>
import { computed, ref, watch } from 'vue'
import type { PostDto } from '@M/forum/dtos/Post'
import { forumService } from '@M/forum/services/ForumService'
import { authService } from '@M/auth/services/AuthService'
import { useRouter } from 'vue-router'

interface VoteButtonsProps {
  post: PostDto
  size?: 'sm' | 'lg'
  disabled?: boolean
}

const props = withDefaults(defineProps<VoteButtonsProps>(), {
  size: 'sm',
  disabled: false,
})

const emit = defineEmits<{
  (e: 'voted', post: PostDto, voteType: 'upvote' | 'downvote' | 'unvote'): void
}>()

const router = useRouter()
const localPoints = ref(props.post.points)
const localWasUpvotedByMe = ref(props.post.wasUpvotedByMe)
const localWasDownvotedByMe = ref(props.post.wasDownvotedByMe)
const isLoading = ref(false)

// Watch for post changes
watch(() => props.post, (newPost) => {
  localPoints.value = newPost.points
  localWasUpvotedByMe.value = newPost.wasUpvotedByMe
  localWasDownvotedByMe.value = newPost.wasDownvotedByMe
}, { immediate: true })

const isLoggedIn = computed(() => authService.getTokens() !== null)

const isUpvoted = computed(() => localWasUpvotedByMe.value === true)
const isDownvoted = computed(() => localWasDownvotedByMe.value === true)

const handleUpvote = async (event: Event) => {
  event.stopPropagation()
  
  if (!isLoggedIn.value) {
    router.push('/auth/login')
    return
  }
  
  if (isLoading.value) return
  isLoading.value = true

  try {
    if (isUpvoted.value) {
      const result = await forumService.unvotePost(props.post.postId!)
      if (result.isSuccess) {
        localPoints.value = result.getValue().points
        localWasUpvotedByMe.value = false
        emit('voted', props.post, 'unvote')
      }
    } else {
      const result = await forumService.upvotePost(props.post.postId!)
      if (result.isSuccess) {
        localPoints.value = result.getValue().points
        localWasUpvotedByMe.value = true
        localWasDownvotedByMe.value = false
        emit('voted', props.post, 'upvote')
      }
    }
  } catch (error) {
    console.error('Vote error:', error)
  } finally {
    isLoading.value = false
  }
}

const handleDownvote = async (event: Event) => {
  event.stopPropagation()
  
  if (!isLoggedIn.value) {
    router.push('/auth/login')
    return
  }
  
  if (isLoading.value) return
  isLoading.value = true

  try {
    if (isDownvoted.value) {
      const result = await forumService.unvotePost(props.post.postId!)
      if (result.isSuccess) {
        localPoints.value = result.getValue().points
        localWasDownvotedByMe.value = false
        emit('voted', props.post, 'unvote')
      }
    } else {
      const result = await forumService.downvotePost(props.post.postId!)
      if (result.isSuccess) {
        localPoints.value = result.getValue().points
        localWasDownvotedByMe.value = true
        localWasUpvotedByMe.value = false
        emit('voted', props.post, 'downvote')
      }
    }
  } catch (error) {
    console.error('Vote error:', error)
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="vote-buttons" :class="`vote-buttons--${size}`">
    <!-- Upvote Button -->
    <button
      type="button"
      class="vote-btn vote-btn--upvote"
      :class="{ 'vote-btn--active': isUpvoted }"
      :disabled="disabled || isLoading"
      @click="handleUpvote"
      :title="isUpvoted ? '取消点赞' : '点赞'"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M12 19V5M5 12l7-7 7 7" />
      </svg>
    </button>
    
    <!-- Points -->
    <span class="vote-points">{{ localPoints }}</span>
    
    <!-- Downvote Button -->
    <button
      type="button"
      class="vote-btn vote-btn--downvote"
      :class="{ 'vote-btn--active': isDownvoted }"
      :disabled="disabled || isLoading"
      @click="handleDownvote"
      :title="isDownvoted ? '取消点踩' : '点踩'"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M12 5v14M5 12l7 7 7-7" />
      </svg>
    </button>
  </div>
</template>

<style scoped>
.vote-buttons {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.vote-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.vote-btn:hover:not(:disabled) {
  background: var(--surface-hover, #f3f4f6);
}

.vote-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.vote-btn svg {
  width: 16px;
  height: 16px;
}

.vote-btn--upvote.vote-btn--active {
  color: var(--primary-color, #3b82f6);
}

.vote-btn--downvote.vote-btn--active {
  color: var(--danger-color, #ef4444);
}

.vote-points {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-color, #1f2937);
  min-width: 24px;
  text-align: center;
}

/* Size variants */
.vote-buttons--sm .vote-btn {
  width: 20px;
  height: 20px;
}

.vote-buttons--sm .vote-btn svg {
  width: 14px;
  height: 14px;
}

.vote-buttons--lg .vote-btn {
  width: 32px;
  height: 32px;
}

.vote-buttons--lg .vote-btn svg {
  width: 18px;
  height: 18px;
}
</style>
