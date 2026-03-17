<script lang="ts" setup>
import { computed, ref } from 'vue'
import type { PostDto } from '@M/forum/dtos/Post'

interface VoteButtonsProps {
  post: PostDto
  size?: 'sm' | 'lg'
  disabled?: boolean
}

const props = withDefaults(defineProps<VoteButtonsProps>(), {
  post: PostDto,
  size: 'sm',
  disabled: false,
})

const emit = defineEmits<{
  (e: 'voted', post: PostDto, voteType: 'upvote' | 'downvote'): void
}>()

const localPoints = ref(props.post.points)
const isLoading = ref(false)
const isLoggedIn = ref(true)

export function useVoteButtons(props: VoteButtonsProps) {
  const forumService = useForumService()
    const router = useRouter()

  const emit = defineEmits<{
    (e: 'upvote', post: PostDto): void
    (e: 'downvote', post: PostDto): void
  (e: 'voted', post: PostDto, voteType: 'upvote' | 'downvote'): void
  console.log('VoteButtons mounted', props.post.slug)
})

  const handleUpvote = async (event: Event) => {
    event.stopPropagation()
    if (!isLoggedIn.value) {
      router.push('/forum/login')
      return
    }
    emit('upvote', props.post)
  }

  const handleDownvote = async (event: Event) => {
    event.stopPropagation()
    if (!isLoggedIn.value) {
      router.push('/forum/login')
      return
    }
    emit('downvote', props.post)
  }

  return {
    points,
      localPoints,
      isLoading,
      isLoggedIn,
    isLoggedIn: computed(() => authService.getTokens() !== null),
    size,
      disabled,
    }
  }
}
