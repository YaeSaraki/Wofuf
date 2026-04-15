<script lang="ts" setup>
import { computed, onMounted } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'

// 自定义渲染器
const renderer = new marked.Renderer()

// 配置 marked
marked.setOptions({
  breaks: true,
  gfm: true
})

// 自定义代码块渲染
renderer.code = function({ text, lang }: { text: string; lang?: string }) {
  const language = lang && hljs.getLanguage(lang) ? lang : ''
  const highlighted = language
    ? hljs.highlight(text, { language }).value
    : hljs.highlightAuto(text).value
  return `<pre><code class="hljs language-${language || 'auto'}">${highlighted}</code></pre>`
}

marked.use({ renderer })

const props = withDefaults(defineProps<{
  content: string
  size?: 'thumbnail' | 'normal' | 'large'
}>(), {
  size: 'normal'
})

// Emit 图片点击事件
const emit = defineEmits<{
  (e: 'image-click', src: string, alt: string): void
}>()

// 渲染后的 HTML
const renderedHtml = computed(() => {
  if (!props.content) return ''
  return marked.parse(props.content) as string
})

// 处理图片点击
function handleImageClick(event: Event) {
  const target = event.target as HTMLElement
  if (target.tagName === 'IMG') {
    const img = target as HTMLImageElement
    emit('image-click', img.src, img.alt || '')
  }
}

// 引用 highlight.js 样式
onMounted(() => {
  // 动态加载代码高亮样式
  const styleId = 'hljs-style'
  if (!document.getElementById(styleId)) {
    const link = document.createElement('link')
    link.id = styleId
    link.rel = 'stylesheet'
    link.href = 'https://cdn.jsdelivr.net/npm/highlight.js@11/styles/github-dark.min.css'
    document.head.appendChild(link)
  }
})
</script>

<template>
  <div
    class="bf-markdown-renderer"
    :class="`bf-markdown-renderer--${size}`"
    v-html="renderedHtml"
    @click="handleImageClick"
  ></div>
</template>

<style>
.bf-markdown-renderer {
  color: var(--bf-text-secondary, #B3B3B3);
  line-height: 1.7;
  word-wrap: break-word;
}

.bf-markdown-renderer h1,
.bf-markdown-renderer h2,
.bf-markdown-renderer h3,
.bf-markdown-renderer h4,
.bf-markdown-renderer h5,
.bf-markdown-renderer h6 {
  color: var(--bf-text-primary, #FFFFFF);
  margin-top: 1.5em;
  margin-bottom: 0.5em;
  font-weight: 600;
  line-height: 1.3;
}

.bf-markdown-renderer h1 { font-size: 1.75rem; }
.bf-markdown-renderer h2 { font-size: 1.5rem; }
.bf-markdown-renderer h3 { font-size: 1.25rem; }
.bf-markdown-renderer h4 { font-size: 1.125rem; }

.bf-markdown-renderer p {
  margin: 0 0 1em;
}

.bf-markdown-renderer a {
  color: var(--bf-primary, #FF6B35);
  text-decoration: none;
}

.bf-markdown-renderer a:hover {
  text-decoration: underline;
}

.bf-markdown-renderer img {
  max-width: 100%;
  height: auto;
  border-radius: var(--bf-card-radius-sm, 12px);
  margin: 1em 0;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

/* 缩略图模式 - 评论中显示小图 */
.bf-markdown-renderer--thumbnail img {
  max-width: 240px;
  max-height: 180px;
  object-fit: cover;
}

/* 普通模式 - 默认大小 */
.bf-markdown-renderer--normal img {
  max-width: 480px;
}

/* 大图模式 - 帖子中显示较大图 */
.bf-markdown-renderer--large img {
  max-width: 720px;
}

.bf-markdown-renderer img:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

/* 行内代码 - 使用项目配色 */
.bf-markdown-renderer code {
  background: var(--bf-surface-active, var(--bf-btn-secondary-bg));
  padding: 0.2em 0.4em;
  border-radius: 4px;
  font-size: 0.875em;
  font-family: 'Fira Code', 'SF Mono', Monaco, monospace;
  color: var(--bf-primary);
}

/* 代码块 - 使用项目配色 */
.bf-markdown-renderer pre {
  background: var(--bf-bg-tertiary, #242424);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-card-radius-sm, 12px);
  padding: 1em;
  overflow-x: auto;
  margin: 1em 0;
}

.bf-markdown-renderer pre code {
  background: transparent;
  padding: 0;
  color: var(--bf-text-secondary);
}

.bf-markdown-renderer blockquote {
  border-left: 3px solid var(--bf-primary, #FF6B35);
  padding-left: 1em;
  margin: 1em 0;
  color: var(--bf-text-muted, #666666);
  font-style: italic;
}

.bf-markdown-renderer ul,
.bf-markdown-renderer ol {
  padding-left: 1.5em;
  margin: 0.5em 0;
}

.bf-markdown-renderer li {
  margin: 0.25em 0;
}

.bf-markdown-renderer hr {
  border: none;
  border-top: 1px solid var(--bf-border-default);
  margin: 1.5em 0;
}

.bf-markdown-renderer table {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
}

.bf-markdown-renderer th,
.bf-markdown-renderer td {
  border: 1px solid var(--bf-border-default);
  padding: 0.5em 0.75em;
  text-align: left;
}

.bf-markdown-renderer th {
  background: var(--bf-surface-hover);
  font-weight: 600;
}
</style>
