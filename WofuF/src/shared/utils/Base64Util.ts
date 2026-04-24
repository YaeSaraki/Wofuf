// 确保 base64 字符串格式正确，添加 hash fragment 避免浏览器缓存冲突
export function addImagePrefixToBase64(base64: string): string {
  // 如果已经是 data:image 格式，添加 hash fragment 避免缓存冲突
  if (base64.startsWith('data:image')) {
    const hash = base64.substring(0, 32).replace(/[^a-zA-Z0-9]/g, '')
    return `${base64}#${hash}`
  }
  // 否则假定是 PNG 格式并添加前缀
  return `data:image/png;base64,${base64}`
}
