<script lang="ts" setup>
import { ref, watch, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { marked } from 'marked'
import { imageService } from '@M/forum/services/ImageService.ts'
import ImageUploader from '@M/forum/components/imageUpload/ImageUploader.vue'

/* ==================== 类型定义 ==================== */
export interface DraftInfo {
  key: string
  cacheKey: string
  content: string
  savedAt: Date
  preview: string // 内容预览（前50字）
}

const props = defineProps<{
  modelValue: string
  maxImages?: number
  placeholder?: string
  cacheKey?: string  // 本地缓存键
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'error', message: string): void
  (e: 'draftAvailable', draft: DraftInfo): void  // 有草稿可恢复
}>()

/* ==================== 配置 ==================== */
const maxImages = computed(() => props.maxImages ?? 9)
const STORAGE_KEY = computed(() => props.cacheKey ? `post_draft_${props.cacheKey}` : '')

/* ==================== 状态 ==================== */
const content = ref(props.modelValue || '')
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const previewRef = ref<HTMLDivElement | null>(null)

// 预览模式
const showPreview = ref(false)
const isFullscreen = ref(false)

// 表情选择器
const showEmojiPicker = ref(false)
const emojiPickerRef = ref<HTMLDivElement | null>(null)
const emojiWrapperRef = ref<HTMLDivElement | null>(null)

// 表情选择器位置
const emojiPickerStyle = ref({
  top: '0px',
  left: '0px',
})

// 切换表情选择器
const toggleEmojiPicker = () => {
  if (!showEmojiPicker.value) {
    // 打开时计算位置
    nextTick(() => {
      calculateEmojiPickerPosition()
    })
  }
  showEmojiPicker.value = !showEmojiPicker.value
}

// 计算表情选择器位置（防止超出屏幕）
const calculateEmojiPickerPosition = () => {
  if (!emojiWrapperRef.value) return

  const rect = emojiWrapperRef.value.getBoundingClientRect()
  const pickerWidth = 280
  const pickerHeight = 280

  let left = rect.left
  let top = rect.bottom + 8

  // 防止超出右边
  if (left + pickerWidth > window.innerWidth) {
    left = window.innerWidth - pickerWidth - 16
  }

  // 防止超出底部
  if (top + pickerHeight > window.innerHeight) {
    top = rect.top - pickerHeight - 8
  }

  // 确保不超出左边
  left = Math.max(16, left)
  top = Math.max(16, top)

  emojiPickerStyle.value = {
    top: `${top}px`,
    left: `${left}px`,
  }
}

// 缓存状态
const hasCachedContent = ref(false)
const lastSaved = ref<Date | null>(null)
const isAutoSaving = ref(false)

/* ==================== 表情数据 ==================== */
const emojiCategories = {
  smileys: ['😀', '😃', '😄', '😁', '😅', '😂', '🤣', '😊', '😇', '🙂', '😉', '😌', '😍', '🥰', '😘', '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨', '😐', '😑', '😶'],
  gestures: ['👍', '👎', '👌', '✌️', '🤞', '🤟', '🤘', '🤙', '👈', '👉', '👆', '👇', '☝️', '✋', '🤚', '🖐️', '🖖', '👋', '🤝', '✊', '👊', '🤛', '🤜', '👏', '🙌', '👐', '🤲', '🙏', '💪'],
  hearts: ['❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '♥️'],
  objects: ['🎉', '🎊', '🎁', '🎈', '🏆', '🥇', '🥈', '🥉', '⚽', '🏀', '🎮', '🎯', '🎲', '📱', '💻', '⌨️', '🖥️', '📷', '📹', '🎵', '🎶', '🎧', '🎤', '🎸', '🎹'],
  symbols: ['✅', '❌', '❓', '❗', '⭐', '🌟', '✨', '💫', '🔥', '💡', '📌', '📍', '🔖', '💰', '💵', '💳', '🔒', '🔓', '⚡', '🌈', '☀️', '🌙', '⭐', '🌍', '🚀']
}

const currentEmojiCategory = ref<keyof typeof emojiCategories>('smileys')

/* ==================== 计算属性 ==================== */
const imageCount = computed(() => imageService.countImages(content.value))
const canAddMoreImages = computed(() => imageCount.value < maxImages.value)
const wordCount = computed(() => content.value.trim().length)
const lineCount = computed(() => content.value.split('\n').length)

// 预览 HTML
const previewHtml = computed(() => {
  if (!content.value) return ''
  return marked.parse(content.value) as string
})

/* ==================== 章节目录 ==================== */
interface TocItem {
  level: number
  text: string
  id: string
}

// 从内容中提取标题
const tocItems = computed<TocItem[]>(() => {
  if (!content.value) return []
  
  const items: TocItem[] = []
  const headingRegex = /^(#{1,6})\s+(.+)$/gm
  let match
  
  while ((match = headingRegex.exec(content.value)) !== null) {
    const level = match[1]?.length ?? 1
    const text = (match[2] ?? '').trim()
    const id = `heading-${items.length}`
    
    if (text) {
      items.push({ level, text, id })
    }
  }
  
  return items
})

// 是否显示目录侧边栏
const showToc = ref(true)

// 切换目录显示
const toggleToc = () => {
  // 如果打开目录，先关闭预览
  if (!showToc.value && showPreview.value) {
    showPreview.value = false
  }
  showToc.value = !showToc.value
}

// 切换预览显示（重写以关闭目录）
const togglePreview = () => {
  // 如果打开预览，先关闭目录
  if (!showPreview.value && showToc.value) {
    showToc.value = false
  }
  showPreview.value = !showPreview.value
}

// 滚动到指定标题
const scrollToHeading = (index: number) => {
  // 在预览模式下滚动预览区域
  if (showPreview.value && previewRef.value) {
    const headings = previewRef.value.querySelectorAll('h1, h2, h3, h4, h5, h6')
    if (headings[index]) {
      headings[index].scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  } else {
    // 在编辑模式下滚动编辑区域到对应行
    const lines = (content.value ?? '').split('\n')
    let headingCount = 0
    for (let i = 0; i < lines.length; i++) {
      if (/^#{1,6}\s+/.test(lines[i] ?? '')) {
        if (headingCount === index) {
          const textarea = textareaRef.value
          if (textarea) {
            const pos = lines.slice(0, i).join('\n').length
            textarea.focus()
            textarea.setSelectionRange(pos, pos)
            textarea.scrollTop = textarea.scrollHeight * (i / lines.length)
          }
          break
        }
        headingCount++
      }
    }
  }
}

/* ==================== 监听 ==================== */
watch(() => props.modelValue, (newVal) => {
  if (newVal !== content.value) {
    content.value = newVal || ''
  }
})

/* ==================== 本地缓存 ==================== */
const DRAFT_PREFIX = 'post_draft_'

// 保存草稿
function saveToCache() {
  if (!STORAGE_KEY.value || !content.value) return

  try {
    const data = {
      content: content.value,
      savedAt: new Date().toISOString()
    }
    localStorage.setItem(STORAGE_KEY.value, JSON.stringify(data))
    lastSaved.value = new Date()
    isAutoSaving.value = true
    setTimeout(() => { isAutoSaving.value = false }, 1000)
  } catch (e) {
    console.warn('保存草稿失败:', e)
  }
}

// 加载草稿
function loadFromCache(): boolean {
  if (!STORAGE_KEY.value) return false

  try {
    const data = localStorage.getItem(STORAGE_KEY.value)
    if (data) {
      const parsed = JSON.parse(data)
      content.value = parsed.content
      lastSaved.value = new Date(parsed.savedAt)
      emit('update:modelValue', content.value)
      return true
    }
  } catch (e) {
    console.warn('加载草稿失败:', e)
  }
  return false
}

// 清除当前草稿
function clearCache() {
  if (!STORAGE_KEY.value) return
  localStorage.removeItem(STORAGE_KEY.value)
  hasCachedContent.value = false
  lastSaved.value = null
}

// 检查当前草稿状态
function checkCache() {
  if (!STORAGE_KEY.value) return
  const data = localStorage.getItem(STORAGE_KEY.value)
  hasCachedContent.value = !!data
}

// 获取所有草稿列表
function getAllDrafts(): DraftInfo[] {
  const drafts: DraftInfo[] = []
  try {
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && key.startsWith(DRAFT_PREFIX)) {
        const data = localStorage.getItem(key)
        if (data) {
          try {
            const parsed = JSON.parse(data)
            const cacheKey = key.replace(DRAFT_PREFIX, '')
            drafts.push({
              key,
              cacheKey,
              content: parsed.content,
              savedAt: new Date(parsed.savedAt),
              preview: parsed.content.substring(0, 50) + (parsed.content.length > 50 ? '...' : '')
            })
          } catch {
            // 忽略解析错误
          }
        }
      }
    }
    // 按保存时间倒序排列
    drafts.sort((a, b) => b.savedAt.getTime() - a.savedAt.getTime())
  } catch (e) {
    console.warn('获取草稿列表失败:', e)
  }
  return drafts
}

// 获取当前草稿信息
function getCurrentDraft(): DraftInfo | null {
  if (!STORAGE_KEY.value) return null
  const data = localStorage.getItem(STORAGE_KEY.value)
  if (data) {
    try {
      const parsed = JSON.parse(data)
      return {
        key: STORAGE_KEY.value,
        cacheKey: props.cacheKey || '',
        content: parsed.content,
        savedAt: new Date(parsed.savedAt),
        preview: parsed.content.substring(0, 50) + (parsed.content.length > 50 ? '...' : '')
      }
    } catch {
      return null
    }
  }
  return null
}

// 删除指定草稿
function deleteDraft(key: string) {
  localStorage.removeItem(key)
}

// 清除所有草稿
function clearAllDrafts() {
  const drafts = getAllDrafts()
  drafts.forEach(draft => {
    localStorage.removeItem(draft.key)
  })
}

// 自动保存
let saveTimeout: ReturnType<typeof setTimeout> | null = null
function scheduleAutoSave() {
  if (saveTimeout) clearTimeout(saveTimeout)
  saveTimeout = setTimeout(() => {
    saveToCache()
  }, 2000)
}

/* ==================== 编辑操作 ==================== */
function handleInput(event: Event) {
  const target = event.target as HTMLTextAreaElement
  content.value = target.value
  emit('update:modelValue', content.value)
  scheduleAutoSave()
}

/**
 * 智能插入文本
 * @param beforeWrap 光标前的文本
 * @param afterWrap 光标后的文本
 * @param placeholder 无选中文本时的占位文本
 */
function smartInsert(beforeWrap: string, afterWrap: string, placeholder: string = '') {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = content.value.substring(start, end)
  const before = content.value.substring(0, start)
  const after = content.value.substring(end)

  if (selectedText) {
    // 有选中文本：包裹选中的文本
    const newText = beforeWrap + selectedText + afterWrap
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    
    setTimeout(() => {
      textarea.focus()
      // 光标放在包裹文本的末尾
      textarea.selectionStart = textarea.selectionEnd = start + newText.length
    }, 0)
  } else if (placeholder) {
    // 无选中文本，有占位符：插入占位符并选中
    const newText = beforeWrap + placeholder + afterWrap
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    
    setTimeout(() => {
      textarea.focus()
      // 选中占位符文本
      textarea.selectionStart = start + beforeWrap.length
      textarea.selectionEnd = start + beforeWrap.length + placeholder.length
    }, 0)
  } else {
    // 无选中文本，无占位符：直接插入，光标放中间
    const newText = beforeWrap + afterWrap
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    
    setTimeout(() => {
      textarea.focus()
      // 光标放中间
      textarea.selectionStart = textarea.selectionEnd = start + beforeWrap.length
    }, 0)
  }
  
  scheduleAutoSave()
}

// 粗体
function insertBold() {
  smartInsert('**', '**', '粗体文本')
}

// 斜体
function insertItalic() {
  smartInsert('*', '*', '斜体文本')
}

// 删除线
function insertStrikethrough() {
  smartInsert('~~', '~~', '删除文本')
}

// 代码
function insertCode() {
  smartInsert('`', '`', '代码')
}

// 代码块
function insertCodeBlock() {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = content.value.substring(start, end)
  const before = content.value.substring(0, start)
  const after = content.value.substring(end)

  if (selectedText) {
    const newText = '\n```\n' + selectedText + '\n```\n'
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    setTimeout(() => { textarea.focus(); textarea.selectionStart = textarea.selectionEnd = start + newText.length }, 0)
  } else {
    const newText = '\n```language\n代码内容\n```\n'
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    setTimeout(() => { textarea.focus(); textarea.selectionStart = start + 5; textarea.selectionEnd = start + 13 }, 0)
  }
  scheduleAutoSave()
}

// 链接
function insertLink() {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = content.value.substring(start, end)
  const before = content.value.substring(0, start)
  const after = content.value.substring(end)

  if (selectedText) {
    const newText = '[' + selectedText + '](url)'
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    setTimeout(() => { textarea.focus(); textarea.selectionStart = start + selectedText.length + 3; textarea.selectionEnd = start + selectedText.length + 6 }, 0)
  } else {
    const newText = '[链接文字](url)'
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    setTimeout(() => { textarea.focus(); textarea.selectionStart = start + 1; textarea.selectionEnd = start + 5 }, 0)
  }
  scheduleAutoSave()
}

// 图片
function insertImage() {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const before = content.value.substring(0, start)
  const after = content.value.substring(start)

  const newText = '![图片描述](url)'
  content.value = before + newText + after
  emit('update:modelValue', content.value)
  setTimeout(() => { textarea.focus(); textarea.selectionStart = start + 2; textarea.selectionEnd = start + 6 }, 0)
  scheduleAutoSave()
}

// 引用
function insertQuote() {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = content.value.substring(start, end)
  const before = content.value.substring(0, start)
  const after = content.value.substring(end)

  if (selectedText) {
    const quotedText = selectedText.split('\n').map(line => '> ' + line).join('\n')
    const newText = '\n' + quotedText + '\n'
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    setTimeout(() => { textarea.focus(); textarea.selectionStart = textarea.selectionEnd = start + newText.length }, 0)
  } else {
    const newText = '\n> 引用内容\n'
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    setTimeout(() => { textarea.focus(); textarea.selectionStart = start + 3; textarea.selectionEnd = start + 7 }, 0)
  }
  scheduleAutoSave()
}

// 标题
function insertHeading(level: number) {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = content.value.substring(start, end)
  const before = content.value.substring(0, start)
  const after = content.value.substring(end)
  const prefix = '#'.repeat(level) + ' '

  if (selectedText) {
    const newText = '\n' + prefix + selectedText + '\n'
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    setTimeout(() => { textarea.focus(); textarea.selectionStart = textarea.selectionEnd = start + newText.length }, 0)
  } else {
    const placeholder = '标题' + level
    const newText = '\n' + prefix + placeholder + '\n'
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    setTimeout(() => { textarea.focus(); textarea.selectionStart = start + prefix.length + 1; textarea.selectionEnd = start + prefix.length + placeholder.length + 1 }, 0)
  }
  scheduleAutoSave()
}

// 无序列表
function insertBulletList() {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = content.value.substring(start, end)
  const before = content.value.substring(0, start)
  const after = content.value.substring(end)

  if (selectedText) {
    const listText = selectedText.split('\n').map(line => '- ' + line).join('\n')
    const newText = '\n' + listText + '\n'
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    setTimeout(() => { textarea.focus(); textarea.selectionStart = textarea.selectionEnd = start + newText.length }, 0)
  } else {
    const newText = '\n- 列表项\n'
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    setTimeout(() => { textarea.focus(); textarea.selectionStart = start + 3; textarea.selectionEnd = start + 6 }, 0)
  }
  scheduleAutoSave()
}

// 有序列表
function insertOrderedList() {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = content.value.substring(start, end)
  const before = content.value.substring(0, start)
  const after = content.value.substring(end)

  if (selectedText) {
    const lines = selectedText.split('\n')
    const listText = lines.map((line, index) => `${index + 1}. ${line}`).join('\n')
    const newText = '\n' + listText + '\n'
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    setTimeout(() => { textarea.focus(); textarea.selectionStart = textarea.selectionEnd = start + newText.length }, 0)
  } else {
    const newText = '\n1. 列表项\n'
    content.value = before + newText + after
    emit('update:modelValue', content.value)
    setTimeout(() => { textarea.focus(); textarea.selectionStart = start + 4; textarea.selectionEnd = start + 7 }, 0)
  }
  scheduleAutoSave()
}

// 分割线
function insertHorizontalRule() {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const before = content.value.substring(0, start)
  const after = content.value.substring(start)

  const newText = '\n\n---\n\n'
  content.value = before + newText + after
  emit('update:modelValue', content.value)
  setTimeout(() => { textarea.focus(); textarea.selectionStart = textarea.selectionEnd = start + newText.length }, 0)
  scheduleAutoSave()
}

// 表格
function insertTable() {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const before = content.value.substring(0, start)
  const after = content.value.substring(start)

  const newText = '\n| 列1 | 列2 | 列3 |\n| --- | --- | --- |\n| 内容 | 内容 | 内容 |\n'
  content.value = before + newText + after
  emit('update:modelValue', content.value)
  setTimeout(() => { textarea.focus(); textarea.selectionStart = start + 3; textarea.selectionEnd = start + 5 }, 0)
  scheduleAutoSave()
}

// 插入表情
function insertEmoji(emoji: string) {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const before = content.value.substring(0, start)
  const after = content.value.substring(start)

  content.value = before + emoji + after
  emit('update:modelValue', content.value)
  setTimeout(() => { textarea.focus(); textarea.selectionStart = textarea.selectionEnd = start + emoji.length }, 0)
  showEmojiPicker.value = false
  scheduleAutoSave()
}

// 插入图片 Markdown
function insertImageMarkdown(markdown: string) {
  if (!canAddMoreImages.value) {
    emit('error', `最多只能上传 ${maxImages.value} 张图片`)
    return
  }
  
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const before = content.value.substring(0, start)
  const after = content.value.substring(start)
  const newText = markdown + '\n'

  content.value = before + newText + after
  emit('update:modelValue', content.value)
  setTimeout(() => { textarea.focus(); textarea.selectionStart = textarea.selectionEnd = start + newText.length }, 0)
  scheduleAutoSave()
}

/* ==================== 快捷键 ==================== */
function handleKeydown(event: KeyboardEvent) {
  // Ctrl/Cmd + 快捷键
  if (event.ctrlKey || event.metaKey) {
    switch (event.key.toLowerCase()) {
      case 'b':
        event.preventDefault()
        insertBold()
        break
      case 'i':
        event.preventDefault()
        insertItalic()
        break
      case 'k':
        event.preventDefault()
        insertLink()
        break
      case 's':
        event.preventDefault()
        saveToCache()
        break
    }
  }
  
  // Tab 键
  if (event.key === 'Tab') {
    event.preventDefault()
    const textarea = textareaRef.value
    if (!textarea) return

    const start = textarea.selectionStart
    const before = content.value.substring(0, start)
    const after = content.value.substring(start)

    content.value = before + '  ' + after
    emit('update:modelValue', content.value)
    setTimeout(() => { textarea.focus(); textarea.selectionStart = textarea.selectionEnd = start + 2 }, 0)
  }
}

/* ==================== 全屏模式 ==================== */
function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value
  if (isFullscreen.value) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
}

/* ==================== 预览同步滚动 ==================== */
function syncScroll() {
  if (!showPreview.value || !textareaRef.value || !previewRef.value) return
  const textarea = textareaRef.value
  const preview = previewRef.value
  const scrollPercent = textarea.scrollTop / (textarea.scrollHeight - textarea.clientHeight)
  preview.scrollTop = scrollPercent * (preview.scrollHeight - preview.clientHeight)
}

/* ==================== 点击外部关闭表情选择器 ==================== */
function handleClickOutside(event: MouseEvent) {
  if (showEmojiPicker.value) {
    const target = event.target as Node
    // 检查是否点击在表情按钮或表情选择器外
    if (emojiWrapperRef.value && !emojiWrapperRef.value.contains(target)) {
      // 检查是否点击在表情选择器本身
      const emojiPicker = document.querySelector('.bf-emoji-picker-glass')
      if (!emojiPicker || !emojiPicker.contains(target)) {
        showEmojiPicker.value = false
      }
    }
  }
}

/* ==================== 生命周期 ==================== */
onMounted(() => {
  checkCache()
  document.addEventListener('click', handleClickOutside)
  
  // 检查是否有草稿可恢复
  if (props.cacheKey) {
    console.log('[MarkdownEditor] onMounted, cacheKey:', props.cacheKey)
    const draft = getCurrentDraft()
    console.log('[MarkdownEditor] draft:', draft)
    if (draft) {
      // 使用 nextTick 确保父组件已准备好
      nextTick(() => {
        console.log('[MarkdownEditor] emitting draftAvailable')
        emit('draftAvailable', draft)
      })
    }
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  if (isFullscreen.value) {
    document.body.style.overflow = ''
  }
})

// 暴露方法给父组件
defineExpose({
  loadFromCache,
  clearCache,
  hasCachedContent,
  getAllDrafts,
  getCurrentDraft,
  deleteDraft,
  clearAllDrafts
})
</script>

<template>
  <div class="bf-markdown-editor" :class="{ 'bf-editor--fullscreen': isFullscreen }">
    <!-- 工具栏 -->
    <div class="bf-editor-toolbar">
      <!-- 文本格式 -->
      <div class="bf-toolbar-group">
        <button type="button" class="bf-toolbar-btn" @click="insertBold" title="粗体 (Ctrl+B)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 4h8a4 4 0 0 1 4 4 4 4 0 0 1-4 4H6z"/>
            <path d="M6 12h9a4 4 0 0 1 4 4 4 4 0 0 1-4 4H6z"/>
          </svg>
        </button>
        <button type="button" class="bf-toolbar-btn" @click="insertItalic" title="斜体 (Ctrl+I)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="4" x2="10" y2="4"/>
            <line x1="14" y1="20" x2="5" y2="20"/>
            <line x1="15" y1="4" x2="9" y2="20"/>
          </svg>
        </button>
        <button type="button" class="bf-toolbar-btn" @click="insertStrikethrough" title="删除线">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="4" y1="12" x2="20" y2="12"/>
            <path d="M17.5 7.5c-1-1.5-3-2.5-5.5-2.5-4 0-6 2-6 4s2 3 6 3"/>
            <path d="M12 16c4 0 6-1.5 6-4"/>
          </svg>
        </button>
      </div>

      <div class="bf-toolbar-divider"></div>

      <!-- 标题 -->
      <div class="bf-toolbar-group">
        <button type="button" class="bf-toolbar-btn" @click="insertHeading(1)" title="标题1">
          <span class="bf-toolbar-text">H1</span>
        </button>
        <button type="button" class="bf-toolbar-btn" @click="insertHeading(2)" title="标题2">
          <span class="bf-toolbar-text">H2</span>
        </button>
        <button type="button" class="bf-toolbar-btn" @click="insertHeading(3)" title="标题3">
          <span class="bf-toolbar-text">H3</span>
        </button>
      </div>

      <div class="bf-toolbar-divider"></div>

      <!-- 列表和引用 -->
      <div class="bf-toolbar-group">
        <button type="button" class="bf-toolbar-btn" @click="insertBulletList" title="无序列表">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="9" y1="6" x2="20" y2="6"/>
            <line x1="9" y1="12" x2="20" y2="12"/>
            <line x1="9" y1="18" x2="20" y2="18"/>
            <circle cx="4" cy="6" r="1" fill="currentColor"/>
            <circle cx="4" cy="12" r="1" fill="currentColor"/>
            <circle cx="4" cy="18" r="1" fill="currentColor"/>
          </svg>
        </button>
        <button type="button" class="bf-toolbar-btn" @click="insertOrderedList" title="有序列表">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="10" y1="6" x2="21" y2="6"/>
            <line x1="10" y1="12" x2="21" y2="12"/>
            <line x1="10" y1="18" x2="21" y2="18"/>
            <text x="3" y="8" font-size="8" fill="currentColor">1</text>
            <text x="3" y="14" font-size="8" fill="currentColor">2</text>
            <text x="3" y="20" font-size="8" fill="currentColor">3</text>
          </svg>
        </button>
        <button type="button" class="bf-toolbar-btn" @click="insertQuote" title="引用">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 21c3 0 7-1 7-8V5c0-1.25-.756-2.017-2-2H4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2 1 0 1 0 1 1v1c0 1-1 2-2 2s-1 .008-1 1.031V21"/>
            <path d="M15 21c3 0 7-1 7-8V5c0-1.25-.757-2.017-2-2h-4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2h.75c0 2.25.25 4-2.75 4v3"/>
          </svg>
        </button>
      </div>

      <div class="bf-toolbar-divider"></div>

      <!-- 链接和图片 -->
      <div class="bf-toolbar-group">
        <button type="button" class="bf-toolbar-btn" @click="insertLink" title="链接 (Ctrl+K)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
            <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
          </svg>
        </button>
        <button type="button" class="bf-toolbar-btn" @click="insertImage" title="图片">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
          </svg>
        </button>
      </div>

      <div class="bf-toolbar-divider"></div>

      <!-- 代码 -->
      <div class="bf-toolbar-group">
        <button type="button" class="bf-toolbar-btn" @click="insertCode" title="行内代码">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="16 18 22 12 16 6"/>
            <polyline points="8 6 2 12 8 18"/>
          </svg>
        </button>
        <button type="button" class="bf-toolbar-btn" @click="insertCodeBlock" title="代码块">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <path d="M8 10l-2 2 2 2"/>
            <path d="M16 10l2 2-2 2"/>
            <line x1="13" y1="8" x2="11" y2="16"/>
          </svg>
        </button>
      </div>

      <div class="bf-toolbar-divider"></div>

      <!-- 表情 -->
      <div class="bf-toolbar-group bf-emoji-wrapper" ref="emojiWrapperRef">
        <button 
          type="button" 
          class="bf-toolbar-btn" 
          :class="{ 'bf-toolbar-btn--active': showEmojiPicker }"
          @click.stop="toggleEmojiPicker" 
          title="表情"
        >
          <span class="bf-toolbar-text">😊</span>
        </button>
      </div>

      <div class="bf-toolbar-divider"></div>

      <!-- 更多功能 -->
      <div class="bf-toolbar-group">
        <button type="button" class="bf-toolbar-btn" @click="insertTable" title="表格">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <line x1="3" y1="9" x2="21" y2="9"/>
            <line x1="3" y1="15" x2="21" y2="15"/>
            <line x1="9" y1="3" x2="9" y2="21"/>
            <line x1="15" y1="3" x2="15" y2="21"/>
          </svg>
        </button>
        <button type="button" class="bf-toolbar-btn" @click="insertHorizontalRule" title="分割线">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="3" y1="12" x2="21" y2="12"/>
          </svg>
        </button>
      </div>

      <div class="bf-toolbar-divider"></div>

      <!-- 上传图片 -->
      <div class="bf-toolbar-group">
        <ImageUploader
          :max-images="maxImages"
          folder="posts"
          @upload="insertImageMarkdown"
          @error="(msg) => emit('error', msg)"
        />
      </div>

      <!-- 右侧工具 -->
      <div class="bf-toolbar-right">
        <!-- 缓存状态 -->
        <div v-if="STORAGE_KEY" class="bf-cache-status">
          <span v-if="isAutoSaving" class="bf-cache-saving">保存中...</span>
          <span v-else-if="lastSaved" class="bf-cache-saved">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
            已保存
          </span>
        </div>

        <!-- 字数统计 -->
        <div class="bf-stats">
          <span class="bf-stats-item">{{ wordCount }} 字</span>
          <span class="bf-stats-divider">|</span>
          <span class="bf-stats-item">{{ imageCount }}/{{ maxImages }} 图</span>
        </div>

        <!-- 预览切换 -->
        <button 
          type="button" 
          class="bf-toolbar-btn"
          :class="{ 'bf-toolbar-btn--active': showPreview }"
          @click="togglePreview" 
          title="预览"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
            <circle cx="12" cy="12" r="3"/>
          </svg>
        </button>

        <!-- 目录切换（仅在有标题时显示） -->
        <button 
          v-if="tocItems.length > 0"
          type="button" 
          class="bf-toolbar-btn"
          :class="{ 'bf-toolbar-btn--active': showToc }"
          @click="toggleToc" 
          title="目录"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="8" y1="6" x2="21" y2="6"/>
            <line x1="8" y1="12" x2="21" y2="12"/>
            <line x1="8" y1="18" x2="21" y2="18"/>
            <line x1="3" y1="6" x2="3.01" y2="6"/>
            <line x1="3" y1="12" x2="3.01" y2="12"/>
            <line x1="3" y1="18" x2="3.01" y2="18"/>
          </svg>
        </button>

        <!-- 全屏 -->
        <button 
          type="button" 
          class="bf-toolbar-btn"
          :class="{ 'bf-toolbar-btn--active': isFullscreen }"
          @click="toggleFullscreen" 
          :title="isFullscreen ? '退出全屏' : '全屏'"
        >
          <svg v-if="!isFullscreen" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 3 21 3 21 9"/>
            <polyline points="9 21 3 21 3 15"/>
            <line x1="21" y1="3" x2="14" y2="10"/>
            <line x1="3" y1="21" x2="10" y2="14"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="4 14 10 14 10 20"/>
            <polyline points="20 10 14 10 14 4"/>
            <line x1="14" y1="10" x2="21" y2="3"/>
            <line x1="3" y1="21" x2="10" y2="14"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 表情选择器弹窗（液态玻璃效果） -->
    <Teleport to="body">
      <Transition name="bf-emoji-picker">
        <div 
          v-if="showEmojiPicker" 
          class="bf-emoji-picker-glass"
          :style="emojiPickerStyle"
          @click.stop
        >
          <!-- 分类标签 -->
          <div class="bf-emoji-tabs">
            <button 
              v-for="(emojis, category) in emojiCategories" 
              :key="category"
              type="button"
              class="bf-emoji-tab"
              :class="{ 'bf-emoji-tab--active': currentEmojiCategory === category }"
              @click.stop="currentEmojiCategory = category as keyof typeof emojiCategories"
            >
              {{ category === 'smileys' ? '😀' : category === 'gestures' ? '👍' : category === 'hearts' ? '❤️' : category === 'objects' ? '🎉' : '✨' }}
            </button>
          </div>
          
          <!-- 表情列表 -->
          <div class="bf-emoji-list">
            <button
              v-for="emoji in emojiCategories[currentEmojiCategory]"
              :key="emoji"
              type="button"
              class="bf-emoji-btn"
              @click.stop="insertEmoji(emoji)"
            >
              {{ emoji }}
            </button>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 编辑区域 -->
    <div class="bf-editor-body">
      <!-- 输入区 -->
      <div class="bf-editor-input" :class="{ 
        'bf-editor-input--half': showPreview,
        'bf-editor-input--with-toc': showToc && tocItems.length > 0 && !showPreview
      }">
        <textarea
          ref="textareaRef"
          :value="content"
          :placeholder="placeholder || '支持 Markdown 格式，可上传图片...\n\n快捷键：\nCtrl+B 粗体\nCtrl+I 斜体\nCtrl+K 链接\nCtrl+S 保存草稿'"
          class="bf-editor-textarea"
          @input="handleInput"
          @keydown="handleKeydown"
          @scroll="syncScroll"
        ></textarea>
      </div>

      <!-- 预览区 -->
      <Transition name="bf-preview">
        <div v-if="showPreview" ref="previewRef" class="bf-editor-preview">
          <div v-if="previewHtml" class="bf-preview-content" v-html="previewHtml"></div>
          <div v-else class="bf-preview-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="48" height="48">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
            </svg>
            <span>开始输入内容以预览</span>
          </div>
        </div>
      </Transition>

      <!-- 目录侧边栏 -->
      <Transition name="bf-toc">
        <div 
          v-if="showToc && tocItems.length > 0 && !showPreview" 
          class="bf-editor-toc"
        >
          <div class="bf-toc-header">
            <span class="bf-toc-title">目录</span>
            <span class="bf-toc-count">{{ tocItems.length }}</span>
          </div>
          <div class="bf-toc-list">
            <button
              v-for="(item, index) in tocItems"
              :key="item.id"
              type="button"
              class="bf-toc-item"
              :class="`bf-toc-item--level-${item.level}`"
              @click="scrollToHeading(index)"
            >
              {{ item.text }}
            </button>
          </div>
        </div>
      </Transition>
    </div>

    <!-- 底部状态栏 -->
    <div class="bf-editor-footer">
      <div class="bf-footer-left">
        <span class="bf-footer-hint">Markdown 支持</span>
      </div>
      <div class="bf-footer-right">
        <span class="bf-footer-lines">{{ lineCount }} 行</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bf-markdown-editor {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--bf-input-border, rgba(255, 255, 255, 0.1));
  border-radius: var(--bf-input-radius, 10px);
  overflow: hidden;
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.04));
}

.bf-editor--fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  border-radius: 0;
  height: 100vh;
  /* 使用项目统一配色 */
  background: var(--bf-bg-elevated);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

/* 全屏模式工具栏 */
.bf-editor--fullscreen .bf-editor-toolbar {
  background: var(--bf-surface);
  border-bottom: 1px solid var(--bf-border-default);
}

/* 全屏模式按钮 */
.bf-editor--fullscreen .bf-toolbar-btn {
  color: var(--bf-text-secondary);
}

.bf-editor--fullscreen .bf-toolbar-btn:hover {
  background: var(--bf-surface-hover);
  color: var(--bf-text-primary);
}

.bf-editor--fullscreen .bf-toolbar-btn--active {
  background: var(--bf-fire-gradient-subtle);
  color: var(--bf-primary);
}

/* 全屏模式分隔线 */
.bf-editor--fullscreen .bf-toolbar-divider {
  background: var(--bf-border-default);
}

/* 全屏模式编辑区域 */
.bf-editor--fullscreen .bf-editor-textarea {
  background: transparent;
  color: var(--bf-text-primary);
}

.bf-editor--fullscreen .bf-editor-textarea::placeholder {
  color: var(--bf-text-muted);
}

/* 全屏模式预览区 */
.bf-editor--fullscreen .bf-editor-preview {
  background: var(--bf-surface);
}

.bf-editor--fullscreen .bf-preview-content {
  color: var(--bf-text-secondary);
}

/* 全屏模式底部状态栏 */
.bf-editor--fullscreen .bf-editor-footer {
  background: var(--bf-surface);
  border-top: 1px solid var(--bf-border-default);
  color: var(--bf-text-muted);
}

/* 全屏模式缓存状态 */
.bf-editor--fullscreen .bf-cache-saved {
  color: var(--bf-success);
}

.bf-editor--fullscreen .bf-cache-saving {
  color: var(--bf-primary);
}

/* 全屏模式统计 */
.bf-editor--fullscreen .bf-stats {
  color: var(--bf-text-muted);
}

/* 工具栏 */
.bf-editor-toolbar {
  display: flex;
  align-items: center;
  gap: var(--bf-space-xs, 4px);
  padding: var(--bf-space-sm, 8px);
  border-bottom: 1px solid var(--bf-border-default);
  background: var(--bf-surface, var(--bf-input-bg));
  flex-wrap: wrap;
}

.bf-toolbar-group {
  display: flex;
  align-items: center;
  gap: 2px;
}

.bf-toolbar-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: var(--bf-text-secondary);
  cursor: pointer;
  transition: all var(--bf-transition-fast);
}

.bf-toolbar-btn:hover {
  background: var(--bf-surface-hover, var(--bf-btn-secondary-hover));
  color: var(--bf-text-primary);
}

.bf-toolbar-btn--active {
  background: var(--bf-fire-gradient-subtle);
  color: var(--bf-primary);
}

.bf-toolbar-btn svg {
  width: 16px;
  height: 16px;
}

.bf-toolbar-text {
  font-size: 0.875rem;
  font-weight: 600;
}

.bf-toolbar-divider {
  width: 1px;
  height: 20px;
  background: var(--bf-border-default);
  margin: 0 var(--bf-space-xs, 4px);
}

.bf-toolbar-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
}

/* 缓存状态 */
.bf-cache-status {
  font-size: 0.75rem;
  color: var(--bf-text-muted, #666666);
}

.bf-cache-saving {
  color: var(--bf-primary, #FF6B35);
}

.bf-cache-saved {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #22c55e;
}

/* 统计 */
.bf-stats {
  display: flex;
  align-items: center;
  font-size: 0.75rem;
  color: var(--bf-text-muted, #666666);
}

.bf-stats-divider {
  margin: 0 var(--bf-space-xs, 4px);
  opacity: 0.5;
}

/* 表情选择器 - 液态玻璃效果 */
.bf-emoji-wrapper {
  position: relative;
}

.bf-emoji-picker-glass {
  position: fixed;
  width: 280px;
  border-radius: 16px;
  overflow: hidden;
  z-index: 10000;
  /* 液态玻璃效果 */
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.35) 0%,
    rgba(255, 255, 255, 0.25) 50%,
    rgba(255, 255, 255, 0.35) 100%
  );
  backdrop-filter: blur(24px) saturate(200%);
  -webkit-backdrop-filter: blur(24px) saturate(200%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.2),
    0 2px 8px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.bf-emoji-tabs {
  display: flex;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(0, 0, 0, 0.1);
}

.bf-emoji-tab {
  flex: 1;
  padding: 10px 8px;
  background: transparent;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.15s ease;
}

.bf-emoji-tab:hover {
  background: rgba(255, 255, 255, 0.1);
}

.bf-emoji-tab--active {
  background: rgba(255, 107, 53, 0.2);
}

.bf-emoji-list {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 2px;
  padding: 8px;
  max-height: 220px;
  overflow-y: auto;
}

.bf-emoji-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 6px;
  font-size: 1.125rem;
  cursor: pointer;
  transition: all 0.15s ease;
}

.bf-emoji-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: scale(1.1);
}

/* 表情选择器过渡动画 */
.bf-emoji-picker-enter-active {
  animation: emojiFadeIn 0.2s ease;
}

.bf-emoji-picker-leave-active {
  animation: emojiFadeOut 0.15s ease;
}

@keyframes emojiFadeIn {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes emojiFadeOut {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-8px);
  }
}

/* 编辑区域 */
.bf-editor-body {
  display: flex;
  flex: 1;
  min-height: 450px;
}

.bf-editor-input {
  flex: 1;
  display: flex;
}

.bf-editor-input--half {
  flex: 0 0 50%;
  border-right: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
}

.bf-editor-input--with-toc {
  flex: 1;
}

.bf-editor-textarea {
  width: 100%;
  flex: 1;
  padding: var(--bf-space-lg, 20px);
  background: transparent;
  border: none;
  color: var(--bf-text-primary, #FFFFFF);
  font-size: 0.9375rem;
  line-height: 1.7;
  resize: none;
  outline: none;
  font-family: inherit;
}

.bf-editor-textarea::placeholder {
  color: var(--bf-text-muted, #666666);
  white-space: pre-line;
}

/* 目录侧边栏 */
.bf-editor-toc {
  width: 220px;
  flex-shrink: 0;
  border-left: 1px solid var(--bf-border-default);
  background: var(--bf-surface);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.bf-toc-header {
  padding: 8px 16px;
  border-bottom: 1px solid var(--bf-border-default);
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.bf-toc-title {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--bf-text-muted);
}

.bf-toc-count {
  font-size: 0.6875rem;
  padding: 2px 6px;
  background: var(--bf-fire-gradient-subtle);
  border-radius: 100px;
  color: var(--bf-primary);
}

.bf-toc-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.bf-toc-item {
  display: block;
  width: 100%;
  padding: 4px 8px;
  background: transparent;
  border: none;
  border-radius: 6px;
  text-align: left;
  font-size: 0.8125rem;
  color: var(--bf-text-secondary);
  cursor: pointer;
  transition: all var(--bf-transition-fast);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bf-toc-item:hover {
  background: var(--bf-surface-hover);
  color: var(--bf-text-primary);
}

.bf-toc-item--level-1 { padding-left: 8px; font-weight: 600; }
.bf-toc-item--level-2 { padding-left: 20px; }
.bf-toc-item--level-3 { padding-left: 32px; font-size: 0.75rem; }
.bf-toc-item--level-4 { padding-left: 44px; font-size: 0.75rem; }
.bf-toc-item--level-5 { padding-left: 56px; font-size: 0.6875rem; color: var(--bf-text-muted); }
.bf-toc-item--level-6 { padding-left: 68px; font-size: 0.6875rem; color: var(--bf-text-muted); }

/* 目录过渡动画 */
.bf-toc-enter-active,
.bf-toc-leave-active {
  transition: all 0.2s ease;
}

.bf-toc-enter-from,
.bf-toc-leave-to {
  opacity: 0;
  width: 0;
}

/* 预览区 - 可编辑 */
.bf-editor-preview {
  flex: 1;
  padding: var(--bf-space-lg, 20px);
  overflow-y: auto;
  background: var(--bf-surface);
  cursor: text;
}

.bf-preview-content {
  color: var(--bf-text-secondary);
  line-height: 1.7;
  min-height: 100%;
}

.bf-preview-content :deep(h1),
.bf-preview-content :deep(h2),
.bf-preview-content :deep(h3) {
  color: var(--bf-text-primary);
  margin-top: 1em;
  margin-bottom: 0.5em;
}

.bf-preview-content :deep(p) {
  margin: 0 0 1em;
}

.bf-preview-content :deep(code) {
  background: var(--bf-surface-active, var(--bf-btn-secondary-bg));
  padding: 0.2em 0.4em;
  border-radius: 4px;
  font-size: 0.875em;
  color: var(--bf-primary);
}

.bf-preview-content :deep(pre) {
  background: var(--bf-bg-tertiary, var(--bf-surface-active));
  padding: 1em;
  border-radius: 8px;
  overflow-x: auto;
  border: 1px solid var(--bf-border-default);
}

.bf-preview-content :deep(pre code) {
  background: transparent;
  color: var(--bf-text-secondary);
}

.bf-preview-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 0.5em 0;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.bf-preview-content :deep(img:hover) {
  transform: scale(1.02);
}

.bf-preview-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--bf-text-muted);
  gap: var(--bf-space-md, 16px);
}

/* 底部状态栏 */
.bf-editor-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--bf-space-xs, 4px) var(--bf-space-sm, 8px);
  border-top: 1px solid var(--bf-border-default);
  background: var(--bf-surface);
  font-size: 0.75rem;
  color: var(--bf-text-muted);
}

/* 过渡动画 */
.bf-emoji-picker-enter-active,
.bf-emoji-picker-leave-active {
  transition: all 0.2s ease;
}

.bf-emoji-picker-enter-from,
.bf-emoji-picker-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.bf-preview-enter-active,
.bf-preview-leave-active {
  transition: all 0.2s ease;
}

.bf-preview-enter-from,
.bf-preview-leave-to {
  opacity: 0;
}

/* 全屏模式 */
.bf-editor--fullscreen .bf-editor-body {
  height: calc(100vh - 80px);
}

.bf-editor--fullscreen .bf-editor-textarea {
  min-height: 100%;
}

/* 滚动条 */
.bf-editor-textarea::-webkit-scrollbar,
.bf-editor-preview::-webkit-scrollbar,
.bf-emoji-list::-webkit-scrollbar {
  width: 8px;
}

.bf-editor-textarea::-webkit-scrollbar-track,
.bf-editor-preview::-webkit-scrollbar-track,
.bf-emoji-list::-webkit-scrollbar-track {
  background: transparent;
}

.bf-editor-textarea::-webkit-scrollbar-thumb,
.bf-editor-preview::-webkit-scrollbar-thumb,
.bf-emoji-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

/* 响应式 - 平板 */
@media (max-width: 768px) {
  .bf-editor-body {
    flex-direction: column;
    min-height: 350px;
  }

  .bf-editor-input--half {
    flex: none;
    border-right: none;
    border-bottom: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  }

  .bf-editor-textarea {
    min-height: 250px;
  }

  .bf-editor-preview {
    min-height: 200px;
  }

  /* 平板隐藏目录 */
  .bf-editor-toc {
    display: none;
  }

  .bf-editor-input--with-toc {
    flex: 1;
  }
}

/* 响应式 - 手机 */
@media (max-width: 640px) {
  .bf-editor-body {
    min-height: 300px;
  }

  .bf-editor-textarea {
    min-height: 200px;
    padding: var(--bf-space-md, 12px);
    font-size: 0.875rem;
  }

  .bf-editor-preview {
    padding: var(--bf-space-md, 12px);
    min-height: 150px;
  }

  .bf-toolbar-text {
    font-size: 0.75rem;
  }

  .bf-stats {
    display: none;
  }

  .bf-cache-status {
    display: none;
  }

  /* 手机端工具栏 */
  .bf-editor-toolbar {
    padding: var(--bf-space-xs, 4px);
    gap: 2px;
    overflow-x: auto;
    flex-wrap: nowrap;
    -webkit-overflow-scrolling: touch;
  }

  .bf-editor-toolbar::-webkit-scrollbar {
    display: none;
  }

  .bf-toolbar-divider {
    display: none;
  }

  .bf-toolbar-btn {
    width: 36px;
    height: 36px;
    flex-shrink: 0;
  }

  /* 手机端隐藏目录 */
  .bf-editor-toc {
    display: none;
  }

  .bf-editor-input--with-toc {
    flex: 1;
  }

  /* 手机端全屏模式 */
  .bf-editor--fullscreen .bf-editor-body {
    height: calc(100vh - 100px);
  }
}

.bf-editor-textarea::-webkit-scrollbar-thumb:hover,
.bf-editor-preview::-webkit-scrollbar-thumb:hover,
.bf-emoji-list::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style>
