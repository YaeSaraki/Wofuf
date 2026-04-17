<script lang="ts" setup>
import { ref, watch } from 'vue'
import { forumService } from '@M/forum/services/ForumService'
import { translate } from '@S/services/i18n'

const props = defineProps<{
  visible: boolean
  currentNickname: string
  nickname: string
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'nicknameUpdated', newNickname: string): void
}>()

const newNickname = ref(props.currentNickname)
const isLoading = ref(false)
const errorMsg = ref('')

watch(
  () => props.currentNickname,
  (val) => {
    newNickname.value = val
  },
)

watch(
  () => props.visible,
  (val) => {
    if (val) {
      newNickname.value = props.currentNickname
      errorMsg.value = ''
    }
  },
)

async function handleSave() {
  if (!newNickname.value.trim()) {
    errorMsg.value = translate('forum', 'titleRequired')
    return
  }

  if (newNickname.value === props.currentNickname) {
    emit('update:visible', false)
    return
  }

  isLoading.value = true
  errorMsg.value = ''

  const result = await forumService.updateNickname(props.nickname, newNickname.value)

  isLoading.value = false

  if (result.isSuccess) {
    emit('nicknameUpdated', newNickname.value)
    emit('update:visible', false)
  } else {
    errorMsg.value = String(result.error)
  }
}

function handleClose() {
  emit('update:visible', false)
}
</script>

<template>
  <PrimeDialog
    :visible="visible"
    :header="translate('forum', 'profile.editNickname')"
    :modal="true"
    :style="{ width: '400px' }"
    :closable="!isLoading"
    :closeOnEscape="!isLoading"
    @update:visible="emit('update:visible', $event)"
  >
    <div class="bf-nickname-form">
      <div class="bf-form-field">
        <label for="nickname">{{ translate('forum', 'profile.newNickname') }}</label>
        <InputText
          id="nickname"
          v-model="newNickname"
          :disabled="isLoading"
          :placeholder="translate('forum', 'enterTitle')"
          class="bf-input"
        />
        <small v-if="errorMsg" class="bf-error-text">{{ errorMsg }}</small>
      </div>
    </div>

    <template #footer>
      <PrimeButton
        :label="translate('forum', 'cancel')"
        severity="secondary"
        :disabled="isLoading"
        @click="handleClose"
      />
      <PrimeButton
        :label="translate('forum', 'save')"
        :loading="isLoading"
        @click="handleSave"
      />
    </template>
  </PrimeDialog>
</template>

<style scoped>
.bf-nickname-form {
  padding: var(--bf-space-2) 0;
}

.bf-form-field {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-1);
}

.bf-form-field label {
  font-size: var(--bf-text-sm);
  font-weight: 500;
  color: var(--bf-text);
}

.bf-input {
  width: 100%;
}

.bf-error-text {
  color: var(--bf-danger);
  font-size: var(--bf-text-sm);
}
</style>
